package dev.kdriver.core.dom

import dev.kdriver.cdp.Serialization
import dev.kdriver.cdp.domain.*
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.tab.Tab
import io.ktor.util.logging.*
import kotlinx.io.files.Path
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.random.Random

/**
 * Default implementation of the [Element] interface.
 */
open class DefaultElement(
    val tab: Tab,
    override var node: DOM.Node,
    override var tree: DOM.Node? = null,
) : Element {

    private val logger = KtorSimpleLogger("Element")

    private var remoteObject: Runtime.RemoteObject? = null

    override val tag: String
        get() = node.nodeName.lowercase()

    override val text: String
        get() = node.filterRecurse { it.nodeType == 3 }?.nodeValue ?: ""

    override val textAll: String
        get() = node.filterRecurseAll { it.nodeType == 3 }.joinToString(" ") { it.nodeValue }

    override val backendNodeId: Int
        get() = node.backendNodeId

    override val nodeType: Int
        get() = node.nodeType

    override val objectId: String?
        get() = remoteObject?.objectId

    override val parentId: Int?
        get() = node.parentId

    override val parent: Element?
        get() {
            val tree = this.tree ?: throw RuntimeException("could not get parent since the element has no tree set")
            val parentNode = tree.filterRecurse { node -> node.nodeId == parentId } ?: return null
            return DefaultElement(tab, parentNode, tree)
        }

    override val children: List<Element>
        get() {
            // Handle iframe special case
            if (node.nodeName == "IFRAME") {
                val frame = node.contentDocument
                if (frame == null || frame.childNodeCount == null) return emptyList()
                val frameChildren = frame.children ?: return emptyList()
                return frameChildren.mapNotNull { child ->
                    try {
                        DefaultElement(tab, child, frame)
                    } catch (_: Exception) {
                        null
                    }
                }
            }
            // Normal children
            if (node.childNodeCount == null || node.childNodeCount == 0) {
                return emptyList()
            }
            return node.children?.mapNotNull { child ->
                try {
                    DefaultElement(tab, child, tree)
                } catch (_: Exception) {
                    null
                }
            } ?: emptyList()
        }

    override val attrs: List<String>
        get() = node.attributes?.chunked(2)?.map { it.first() } ?: emptyList()

    override suspend fun updateRemoteObject(): Runtime.RemoteObject? {
        remoteObject = tab.dom.resolveNode(backendNodeId = backendNodeId).`object`
        return remoteObject
    }

    override suspend fun update(nodeOverride: DOM.Node?): Element {
        val doc = nodeOverride ?: tab.dom.getDocument(depth = -1, pierce = true).root
        val updatedNode = doc.filterRecurse { it.backendNodeId == node.backendNodeId }
        if (updatedNode != null) {
            logger.debug("node seems changed, and has now been updated.")
            this.node = updatedNode
        }
        this.tree = doc

        remoteObject = tab.dom.resolveNode(backendNodeId = node.backendNodeId).`object`

        if (node.nodeName != "IFRAME") {
            val parentNode = doc.filterRecurse { it.nodeId == node.parentId }
            if (parentNode != null) {
                // What's the point of this? (object is never used)
                val _parent = DefaultElement(tab, parentNode, tree)
            }
        }
        return this
    }

    private suspend fun flash(duration: Long = 250) {
        // TODO: Do we really need this?
        // displays for a short time a red dot on the element (only if the element itself is visible)
        // could be linked to Tab.flashPoint
    }

    override operator fun get(name: String): String? {
        val keyIndex = node.attributes?.indexOfFirst { it == name }?.takeIf { it != -1 } ?: return null
        return node.attributes?.get(keyIndex + 1)
    }

    override suspend fun click() {
        flash()
        apply<Unit>(
            jsFunction = """
                function() {
                    if (!this || !this.isConnected) {
                        throw new Error('Element is detached from DOM');
                    }
                    this.click();
                }
            """.trimIndent()
        )
    }

    /**
     * Gets stable element coordinates by waiting for position to stabilize across multiple frames.
     * This prevents race conditions on slow systems where scroll or layout changes may still be in progress.
     *
     * @return Stable coordinates, or null if element is not visible/connected
     */
    private suspend fun getStableCoordinates(): CoordinateResult? {
        return try {
            apply<CoordinateResult?>(
                jsFunction = """
                    function() {
                        if (!this || !this.isConnected) return null;

                        return new Promise(resolve => {
                            let lastTop = null;
                            let lastLeft = null;
                            let stableFrames = 0;
                            const maxAttempts = 10;
                            let attempts = 0;

                            const checkStable = () => {
                                attempts++;
                                const rect = this.getBoundingClientRect();

                                if (rect.width === 0 || rect.height === 0) {
                                    resolve(null);
                                    return;
                                }

                                if (lastTop !== null &&
                                    Math.abs(rect.top - lastTop) < 1 &&
                                    Math.abs(rect.left - lastLeft) < 1) {
                                    stableFrames++;
                                    if (stableFrames >= 2) {
                                        resolve({
                                            x: rect.left + rect.width / 2,
                                            y: rect.top + rect.height / 2
                                        });
                                        return;
                                    }
                                } else {
                                    stableFrames = 0;
                                }

                                lastTop = rect.top;
                                lastLeft = rect.left;

                                if (attempts < maxAttempts) {
                                    requestAnimationFrame(checkStable);
                                } else {
                                    // Timeout: use current position
                                    resolve({
                                        x: rect.left + rect.width / 2,
                                        y: rect.top + rect.height / 2
                                    });
                                }
                            };

                            requestAnimationFrame(checkStable);
                        });
                    }
                """.trimIndent(),
                awaitPromise = true
            )
        } catch (e: EvaluateException) {
            logger.warn("Could not get stable coordinates for $this: ${e.jsError}")
            null
        }
    }

    /**
     * Moves the mouse to the target coordinates using a natural Bezier curve trajectory (P2 - Anti-detection).
     * This creates smooth, human-like mouse movements instead of instant teleportation.
     *
     * @param targetX Target X coordinate
     * @param targetY Target Y coordinate
     */
    private suspend fun mouseMoveWithTrajectory(targetX: Double, targetY: Double) {
        val startX: Double
        val startY: Double

        if (tab.lastMouseX != null && tab.lastMouseY != null) {
            startX = tab.lastMouseX!!
            startY = tab.lastMouseY!!
        } else {
            // Get actual viewport dimensions to avoid placing mouse outside visible area
            val viewportData = try {
                val viewportJson = tab.rawEvaluate(
                    """
                    ({
                        width: window.innerWidth,
                        height: window.innerHeight
                    })
                    """.trimIndent()
                )
                if (viewportJson != null) {
                    Serialization.json.decodeFromJsonElement<dev.kdriver.core.dom.ViewportData>(viewportJson)
                } else null
            } catch (e: Exception) {
                null
            }

            if (viewportData != null) {
                // Use random position within viewport bounds, with margins
                val maxX = (viewportData.width - 50).coerceAtLeast(100.0)
                val maxY = (viewportData.height - 50).coerceAtLeast(100.0)
                startX = Random.nextDouble(50.0, maxX)
                startY = Random.nextDouble(50.0, maxY)
            } else {
                // Fallback: use target coordinates with offset if viewport query fails
                startX = (targetX - Random.nextDouble(50.0, 150.0)).coerceAtLeast(0.0)
                startY = (targetY - Random.nextDouble(50.0, 150.0)).coerceAtLeast(0.0)
            }
        }

        // Don't create trajectory if we're already at the target
        if (startX == targetX && startY == targetY) {
            return
        }

        // Random number of steps for natural variation (8-15 steps)
        val steps = Random.nextInt(8, 15)

        // Control point for quadratic Bezier curve with random offset
        val ctrlX = (startX + targetX) / 2 + Random.nextDouble(-30.0, 30.0)
        val ctrlY = (startY + targetY) / 2 + Random.nextDouble(-20.0, 20.0)

        logger.debug("Mouse trajectory from ($startX, $startY) to ($targetX, $targetY) via control point ($ctrlX, $ctrlY) in $steps steps")

        for (i in 0..steps) {
            val t = i.toDouble() / steps

            // Quadratic Bezier curve formula: B(t) = (1-t)²P0 + 2(1-t)tP1 + t²P2
            val x = (1 - t) * (1 - t) * startX + 2 * (1 - t) * t * ctrlX + t * t * targetX
            val y = (1 - t) * (1 - t) * startY + 2 * (1 - t) * t * ctrlY + t * t * targetY

            tab.input.dispatchMouseEvent(type = "mouseMoved", x = x, y = y)

            // Random delay between steps for natural variation
            if (i < steps) tab.sleep(Random.nextLong(8, 25))
        }

        // Update last position
        tab.lastMouseX = targetX
        tab.lastMouseY = targetY
    }

    override suspend fun mouseMove() {
        // Execute position query atomically in a single JavaScript call
        // This prevents race conditions where the element could be detached
        // between getting position and dispatching mouse events
        val coordinates = try {
            apply<CoordinateResult?>(
                jsFunction = """
                    function() {
                        if (!this || !this.isConnected) return null;
                        const rect = this.getBoundingClientRect();
                        if (rect.width === 0 || rect.height === 0) return null;
                        return {
                            x: rect.left + rect.width / 2,
                            y: rect.top + rect.height / 2
                        };
                    }
                """.trimIndent()
            )
        } catch (e: EvaluateException) {
            logger.warn("Could not get coordinates for $this: ${e.jsError}")
            return
        }

        if (coordinates == null) {
            logger.warn("Could not find location for $this, not moving mouse")
            return
        }

        val (centerX, centerY) = coordinates

        // Add jitter to mouse coordinates (P1 - Anti-detection)
        val jitterX = (Random.nextDouble() * 10 - 5)  // -5 to +5 pixels
        val jitterY = (Random.nextDouble() * 6 - 3)   // -3 to +3 pixels
        val x = centerX + jitterX
        val y = centerY + jitterY

        logger.debug("Mouse move to location $x, $y (center: $centerX, $centerY, jitter: $jitterX, $jitterY) where $this is located")

        // Use natural trajectory instead of instant teleportation (P2 - Anti-detection)
        mouseMoveWithTrajectory(x, y)
    }

    override suspend fun mouseClick(
        button: Input.MouseButton,
        modifiers: Int,
        clickCount: Int,
    ) {
        // Execute position query atomically in a single JavaScript call
        // This prevents race conditions where the element could be detached
        // between getting position and dispatching mouse events
        val scrollData = try {
            apply<ScrollData?>(
                jsFunction = """
                    function() {
                        if (!this || !this.isConnected) return null;

                        const rect = this.getBoundingClientRect();
                        if (rect.width === 0 || rect.height === 0) return null;

                        // Check if element is visible in viewport
                        const viewportHeight = window.innerHeight;
                        const viewportWidth = window.innerWidth;
                        const elementCenterY = rect.top + rect.height / 2;
                        const elementCenterX = rect.left + rect.width / 2;

                        // Calculate if we need to scroll
                        const needsScrollY = elementCenterY < 0 || elementCenterY > viewportHeight;
                        const needsScrollX = elementCenterX < 0 || elementCenterX > viewportWidth;

                        // Calculate scroll distances to center the element
                        const scrollY = needsScrollY ? elementCenterY - viewportHeight / 2 : 0;
                        const scrollX = needsScrollX ? elementCenterX - viewportWidth / 2 : 0;

                        return {
                            x: rect.left + rect.width / 2,
                            y: rect.top + rect.height / 2,
                            scrollX: scrollX,
                            scrollY: scrollY,
                            needsScroll: needsScrollY || needsScrollX
                        };
                    }
                """.trimIndent()
            )
        } catch (e: EvaluateException) {
            logger.warn("Could not get coordinates for $this: ${e.jsError}")
            return
        }

        if (scrollData == null) {
            logger.warn("Could not find location for $this, not clicking")
            return
        }

        // Scroll element into view naturally if needed (P3 - Anti-detection)
        if (scrollData.needsScroll) {
            logger.debug("Scrolling by (${scrollData.scrollX}, ${scrollData.scrollY}) to bring $this into view")
            tab.scrollTo(scrollData.scrollX, scrollData.scrollY)
        }

        // Get updated coordinates after scrolling, waiting for position stability
        // This is critical on slow systems where scroll may not complete immediately
        val coordinates = getStableCoordinates()

        if (coordinates == null) {
            logger.warn("Could not find location for $this, not clicking")
            return
        }

        val (centerX, centerY) = coordinates

        // Add jitter to click coordinates (P1 - Anti-detection)
        // Humans don't click exactly at the mathematical center
        val jitterX = (Random.nextDouble() * 10 - 5)  // -5 to +5 pixels
        val jitterY = (Random.nextDouble() * 6 - 3)   // -3 to +3 pixels
        val x = centerX + jitterX
        val y = centerY + jitterY

        logger.debug("Mouse click at location $x, $y (center: $centerX, $centerY, jitter: $jitterX, $jitterY) where $this is located (button=$button, modifiers=$modifiers, clickCount=$clickCount)")

        // Dispatch complete mouse event sequence
        // 1. Move mouse to position with natural trajectory (P2 - Anti-detection)
        mouseMoveWithTrajectory(x, y)

        // Randomized delay to make it more realistic (P1 - Anti-detection)
        tab.sleep(Random.nextLong(5, 20))

        // 2. Verify element hasn't moved during trajectory (handles React re-renders, animations, etc.)
        val finalCoordinates = try {
            apply<CoordinateResult?>(
                jsFunction = """
                    function() {
                        if (!this || !this.isConnected) return null;
                        const rect = this.getBoundingClientRect();
                        if (rect.width === 0 || rect.height === 0) return null;
                        return {
                            x: rect.left + rect.width / 2,
                            y: rect.top + rect.height / 2
                        };
                    }
                """.trimIndent()
            )
        } catch (e: EvaluateException) {
            null
        }

        // Adjust click position if element moved significantly (>5px threshold)
        val finalX: Double
        val finalY: Double
        if (finalCoordinates != null) {
            val (finalCenterX, finalCenterY) = finalCoordinates
            val deltaX = finalCenterX - centerX
            val deltaY = finalCenterY - centerY
            val moved = kotlin.math.sqrt(deltaX * deltaX + deltaY * deltaY) > 5.0

            if (moved) {
                logger.debug("Element moved during trajectory by ($deltaX, $deltaY), adjusting click position")
                finalX = finalCenterX + jitterX
                finalY = finalCenterY + jitterY
                // Move mouse to adjusted position
                tab.input.dispatchMouseEvent(type = "mouseMoved", x = finalX, y = finalY)
                tab.lastMouseX = finalX
                tab.lastMouseY = finalY
            } else {
                finalX = x
                finalY = y
            }
        } else {
            // Element disappeared or error occurred, use original position
            finalX = x
            finalY = y
        }

        // 3. Press and release mouse button with guaranteed cleanup
        // Use try-finally to ensure button state is always cleaned up even on error
        try {
            tab.input.dispatchMouseEvent(
                type = "mousePressed",
                x = finalX,
                y = finalY,
                button = button,
                buttons = button.buttonsMask,
                clickCount = clickCount,
                modifiers = modifiers
            )

            // Randomized delay between press and release (P1 - Anti-detection)
            tab.sleep(Random.nextLong(40, 120))

            // 4. Release mouse button
            tab.input.dispatchMouseEvent(
                type = "mouseReleased",
                x = finalX,
                y = finalY,
                button = button,
                buttons = button.buttonsMask,
                clickCount = clickCount,
                modifiers = modifiers
            )
        } catch (e: Exception) {
            // Ensure button is released even on error to prevent stuck button state
            runCatching {
                tab.input.dispatchMouseEvent(
                    type = "mouseReleased",
                    x = finalX,
                    y = finalY,
                    button = button
                )
            }
            throw e
        }
    }

    override suspend fun focus() {
        apply<Unit>(
            jsFunction = """
                function() {
                    if (!this || !this.isConnected) {
                        throw new Error('Element is detached from DOM');
                    }
                    this.focus();
                }
            """.trimIndent()
        )
    }

    override suspend fun sendKeys(text: String) {
        focus()
        for (char in text) tab.input.dispatchKeyEvent(
            type = "char",
            text = char.toString()
        )
    }

    override suspend fun insertText(text: String) {
        focus()
        tab.input.insertText(text)
    }

    override suspend fun sendFile(paths: List<Path>) {
        tab.dom.setFileInputFiles(
            files = paths.map { it.toString() },
            backendNodeId = backendNodeId,
            objectId = objectId,
        )
    }

    override suspend fun getInputValue(): String? {
        return apply<String>("(el) => el.value")
    }

    override suspend fun clearInput() {
        apply<Unit>("function (element) { element.value = \"\" }")
    }

    override suspend fun clearInputByDeleting() {
        focus()

        // Set selection range to the beginning and get initial value length atomically
        val initialLength = apply<Int>(
            """
            (el) => {
                el.setSelectionRange(0, 0);
                return el.value.length;
            }
            """.trimIndent()
        ) ?: 0

        // Delete each character using CDP Input.dispatchKeyEvent (P3 - Anti-detection)
        // This generates isTrusted: true events unlike JavaScript KeyboardEvent dispatch
        var remaining = initialLength
        while (remaining > 0) {
            // Dispatch keydown event
            tab.input.dispatchKeyEvent(
                type = "keyDown",
                key = "Delete",
                code = "Delete",
                windowsVirtualKeyCode = 46,
                nativeVirtualKeyCode = 46
            )

            // Dispatch keyup event
            tab.input.dispatchKeyEvent(
                type = "keyUp",
                key = "Delete",
                code = "Delete",
                windowsVirtualKeyCode = 46,
                nativeVirtualKeyCode = 46
            )

            // Actually remove the character from the input value and get remaining length
            remaining = apply<Int>(
                """
                (el) => {
                    el.value = el.value.slice(1);
                    el.dispatchEvent(new Event('input', { bubbles: true }));
                    return el.value.length;
                }
                """.trimIndent()
            ) ?: 0

            // Random delay between deletions (50-100ms) for natural variation
            if (remaining > 0) tab.sleep(Random.nextLong(50, 100))
        }
    }

    override suspend fun rawApply(
        jsFunction: String,
        awaitPromise: Boolean,
    ): JsonElement? {
        val remoteObject = updateRemoteObject()

        // Wrap user's function with connection validation
        // This ensures the element is still connected before executing user code
        val wrappedFunction = wrapSafe(jsFunction, validateVisible = false)

        val result = tab.runtime.callFunctionOn(
            functionDeclaration = wrappedFunction,
            objectId = remoteObject?.objectId,
            arguments = listOf(
                Runtime.CallArgument(objectId = remoteObject?.objectId)
            ),
            returnByValue = true,
            userGesture = true,
            awaitPromise = awaitPromise,
        )
        result.exceptionDetails?.let { throw EvaluateException(it) }
        return result.result.value
    }

    override suspend fun querySelectorAll(selector: String): List<Element> {
        update()
        return tab.querySelectorAll(selector, node = NodeOrElement.WrappedNode(this.node))
    }

    override suspend fun querySelector(selector: String): Element? {
        update()
        return tab.querySelector(selector, node = NodeOrElement.WrappedNode(this.node))
    }

    override suspend fun getPosition(abs: Boolean): Position? {
        // Execute everything atomically in a single JavaScript call
        // This prevents race conditions where:
        // 1. Element could detach between updateRemoteObject() and getContentQuads()
        // 2. Element could move between getBoundingRect() and scroll position query
        return try {
            val positionData = apply<PositionData?>(
                jsFunction = """
                    function() {
                        if (!this || !this.isConnected) return null;
                        const rect = this.getBoundingClientRect();
                        if (rect.width === 0 || rect.height === 0) return null;
                        return {
                            left: rect.left,
                            top: rect.top,
                            right: rect.right,
                            bottom: rect.bottom,
                            scrollX: ${if (abs) "window.scrollX" else "0"},
                            scrollY: ${if (abs) "window.scrollY" else "0"}
                        };
                    }
                """.trimIndent()
            ) ?: return null

            // Convert to Position object
            val points = listOf(
                positionData.left, positionData.top,
                positionData.right, positionData.top,
                positionData.right, positionData.bottom,
                positionData.left, positionData.bottom
            )

            Position(points).also { pos ->
                if (abs) {
                    pos.absX = positionData.left + positionData.scrollX + (positionData.right - positionData.left) / 2
                    pos.absY = positionData.top + positionData.scrollY + (positionData.bottom - positionData.top) / 2
                }
            }
        } catch (e: EvaluateException) {
            logger.debug("Could not get position for $this: ${e.jsError}")
            null
        }
    }

    override fun toString(): String {
        var content = ""

        // Collect all text from this leaf
        val childNodeCount = node.childNodeCount ?: 0
        if (childNodeCount > 0) {
            val children = this.children
            if (childNodeCount == 1 && children.isNotEmpty()) {
                content += children[0].toString()
            } else if (childNodeCount > 1) {
                for (child in children) {
                    content += child.toString()
                }
            }
        }

        if (nodeType == 3) { // text node
            content += node.nodeValue
            return content
        }

        val attrs = node.attributes?.chunked(2)?.joinToString(" ") {
            val key = it[0]
            val value = it.getOrNull(1) ?: ""
            """$key="$value""""
        } ?: ""

        return if (attrs.isNotBlank()) "<$tag $attrs>$content</$tag>"
        else "<$tag>$content</$tag>"
    }

    /**
     * Wraps a user JavaScript function with connection validation.
     *
     * @param userFunction The JavaScript function to wrap (can be arrow function or function declaration)
     * @param validateVisible If true, also validates element visibility
     * @return A wrapped function that validates element state before executing user code
     */
    private fun wrapSafe(userFunction: String, validateVisible: Boolean = false): String {
        val checks = buildString {
            append(
                """
                    if (!this || !this.isConnected) {
                        throw new Error('Element is detached from DOM');
                    }
                """.trimIndent()
            )
            if (validateVisible) append(
                """
                    const rect = this.getBoundingClientRect();
                    if (rect.width === 0 || rect.height === 0) {
                        throw new Error('Element is not visible');
                    }
                """.trimIndent()
            )
        }

        return """
            function(elem) {
                $checks
                const userFn = $userFunction;
                return userFn.call(elem, elem);
            }
        """.trimIndent()
    }

}
