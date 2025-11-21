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

        val (x, y) = coordinates
        logger.debug("Mouse move to location $x, $y where $this is located")

        tab.input.dispatchMouseEvent(
            type = "mouseMoved",
            x = x,
            y = y
        )
    }

    override suspend fun mouseClick(
        button: Input.MouseButton,
        modifiers: Int,
        clickCount: Int,
    ) {
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
            logger.warn("Could not find location for $this, not clicking")
            return
        }

        val (x, y) = coordinates
        logger.debug("Mouse click at location $x, $y where $this is located (button=$button, modifiers=$modifiers, clickCount=$clickCount)")

        // Dispatch complete mouse event sequence
        // 1. Move mouse to position
        tab.input.dispatchMouseEvent(
            type = "mouseMoved",
            x = x,
            y = y
        )

        // Small delay to make it more realistic
        tab.sleep(10)

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

        // Delay between press and release (realistic click timing)
        tab.sleep(50)

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
        apply<Unit>(
            jsFunction = """
                async function clearByDeleting(n, d = 50) {
                    n.focus();
                    n.setSelectionRange(0, 0);
                    while (n.value.length > 0) {
                        n.dispatchEvent(
                            new KeyboardEvent("keydown", {
                                key: "Delete",
                                code: "Delete",
                                keyCode: 46,
                                which: 46,
                                bubbles: !0,
                                cancelable: !0,
                            })
                        );
                        n.value = n.value.slice(1);
                        await new Promise((r) => setTimeout(r, d));
                    }
                    n.dispatchEvent(new Event("input", { bubbles: !0 }));
                }
            """.trimIndent(),
            awaitPromise = true
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
