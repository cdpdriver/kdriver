package dev.kdriver.core.dom

import dev.kdriver.cdp.domain.*
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.tab.Tab
import io.ktor.util.logging.*
import kotlinx.io.files.Path
import kotlinx.serialization.json.JsonElement

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

    // Track last mouse position for natural trajectories (P2 - Anti-detection)
    companion object {
        private var lastMouseX: Double? = null
        private var lastMouseY: Double? = null
    }

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
     * Moves the mouse to the target coordinates using a natural Bezier curve trajectory (P2 - Anti-detection).
     * This creates smooth, human-like mouse movements instead of instant teleportation.
     *
     * @param targetX Target X coordinate
     * @param targetY Target Y coordinate
     */
    private suspend fun mouseMoveWithTrajectory(targetX: Double, targetY: Double) {
        val startX = lastMouseX ?: kotlin.random.Random.nextDouble(100.0, 400.0)
        val startY = lastMouseY ?: kotlin.random.Random.nextDouble(100.0, 300.0)

        // Don't create trajectory if we're already at the target
        if (startX == targetX && startY == targetY) {
            return
        }

        // Random number of steps for natural variation (8-15 steps)
        val steps = kotlin.random.Random.nextInt(8, 15)

        // Control point for quadratic Bezier curve with random offset
        val ctrlX = (startX + targetX) / 2 + kotlin.random.Random.nextDouble(-30.0, 30.0)
        val ctrlY = (startY + targetY) / 2 + kotlin.random.Random.nextDouble(-20.0, 20.0)

        logger.debug("Mouse trajectory from ($startX, $startY) to ($targetX, $targetY) via control point ($ctrlX, $ctrlY) in $steps steps")

        for (i in 0..steps) {
            val t = i.toDouble() / steps

            // Quadratic Bezier curve formula: B(t) = (1-t)²P0 + 2(1-t)tP1 + t²P2
            val x = (1 - t) * (1 - t) * startX + 2 * (1 - t) * t * ctrlX + t * t * targetX
            val y = (1 - t) * (1 - t) * startY + 2 * (1 - t) * t * ctrlY + t * t * targetY

            tab.input.dispatchMouseEvent(type = "mouseMoved", x = x, y = y)

            // Random delay between steps for natural variation
            if (i < steps) {
                tab.sleep(kotlin.random.Random.nextLong(8, 25))
            }
        }

        // Update last position
        lastMouseX = targetX
        lastMouseY = targetY
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
        val jitterX = (kotlin.random.Random.nextDouble() * 10 - 5)  // -5 to +5 pixels
        val jitterY = (kotlin.random.Random.nextDouble() * 6 - 3)   // -3 to +3 pixels
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

        // Get updated coordinates after scrolling
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
            logger.warn("Could not find location for $this, not clicking")
            return
        }

        val (centerX, centerY) = coordinates

        // Add jitter to click coordinates (P1 - Anti-detection)
        // Humans don't click exactly at the mathematical center
        val jitterX = (kotlin.random.Random.nextDouble() * 10 - 5)  // -5 to +5 pixels
        val jitterY = (kotlin.random.Random.nextDouble() * 6 - 3)   // -3 to +3 pixels
        val x = centerX + jitterX
        val y = centerY + jitterY

        logger.debug("Mouse click at location $x, $y (center: $centerX, $centerY, jitter: $jitterX, $jitterY) where $this is located (button=$button, modifiers=$modifiers, clickCount=$clickCount)")

        // Dispatch complete mouse event sequence
        // 1. Move mouse to position with natural trajectory (P2 - Anti-detection)
        mouseMoveWithTrajectory(x, y)

        // Randomized delay to make it more realistic (P1 - Anti-detection)
        tab.sleep(kotlin.random.Random.nextLong(5, 20))

        // 2. Press mouse button
        tab.input.dispatchMouseEvent(
            type = "mousePressed",
            x = x,
            y = y,
            button = button,
            buttons = button.buttonsMask,
            clickCount = clickCount,
            modifiers = modifiers
        )

        // Randomized delay between press and release (P1 - Anti-detection)
        tab.sleep(kotlin.random.Random.nextLong(40, 120))

        // 3. Release mouse button
        tab.input.dispatchMouseEvent(
            type = "mouseReleased",
            x = x,
            y = y,
            button = button,
            buttons = button.buttonsMask,
            clickCount = clickCount,
            modifiers = modifiers
        )
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
        // Focus the element first
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
                    return el.value.length;
                }
            """.trimIndent()
            ) ?: 0

            // Random delay between deletions (50-100ms) for natural variation
            if (remaining > 0) {
                tab.sleep(kotlin.random.Random.nextLong(50, 100))
            }
        }

        // Dispatch input event to notify the page of the change
        apply<String?>(
            """
            (el) => {
                el.dispatchEvent(new Event('input', { bubbles: true }));
                return null;
            }
        """.trimIndent()
        )
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
