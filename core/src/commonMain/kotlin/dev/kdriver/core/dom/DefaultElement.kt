package dev.kdriver.core.dom

import dev.kdriver.cdp.domain.*
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.tab.Tab
import dev.kdriver.core.tab.evaluate
import dev.kdriver.core.utils.filterRecurse
import dev.kdriver.core.utils.filterRecurseAll
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
        get() = filterRecurse(node) { it.nodeType == 3 }?.nodeValue ?: ""

    override val textAll: String
        get() = filterRecurseAll(node) { it.nodeType == 3 }.joinToString(" ") { it.nodeValue }

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
            val parentNode = filterRecurse(tree) { node -> node.nodeId == parentId } ?: return null
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
        val updatedNode = filterRecurse(doc) { it.backendNodeId == node.backendNodeId }
        if (updatedNode != null) {
            logger.debug("node seems changed, and has now been updated.")
            this.node = updatedNode
        }
        this.tree = doc

        remoteObject = tab.dom.resolveNode(backendNodeId = node.backendNodeId).`object`

        if (node.nodeName != "IFRAME") {
            val parentNode = filterRecurse(doc) { it.nodeId == node.parentId }
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
        updateRemoteObject()
        val objectId = remoteObject?.objectId ?: error("Could not resolve object id for $this")

        val arguments = listOf(Runtime.CallArgument(objectId = objectId))

        flash()
        tab.runtime.callFunctionOn(
            functionDeclaration = "(el) => el.click()",
            objectId = objectId,
            arguments = arguments,
            awaitPromise = true,
            userGesture = true,
            returnByValue = true
        )
    }

    override suspend fun mouseMove() {
        val position = getPosition()
        if (position == null) {
            logger.warn("Could not find location for $this, not moving mouse")
            return
        }
        val (x, y) = position.center
        logger.debug("Mouse move to location $x, $y where $this is located")

        tab.input.dispatchMouseEvent(
            type = "mouseMoved",
            x = x,
            y = y
        )
        tab.sleep(50)
        tab.input.dispatchMouseEvent(
            type = "mouseReleased",
            x = x,
            y = y
        )
    }

    override suspend fun focus() {
        apply<Unit>("(elem) => elem.focus()")
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
        val result = tab.runtime.callFunctionOn(
            functionDeclaration = jsFunction,
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
        updateRemoteObject()

        return try {
            val quads = tab.dom.getContentQuads(objectId = remoteObject!!.objectId).quads
            if (quads.isEmpty()) {
                throw Exception("could not find position for $this")
            }
            val pos = Position(quads[0])
            if (abs) {
                val scrollY = tab.evaluate<Double>("window.scrollY") ?: 0.0
                val scrollX = tab.evaluate<Double>("window.scrollX") ?: 0.0
                val absX = pos.left + scrollX + pos.width / 2
                val absY = pos.top + scrollY + pos.height / 2
                pos.absX = absX
                pos.absY = absY
            }
            pos
        } catch (_: IndexOutOfBoundsException) {
            logger.debug("no content quads for $this. mostly caused by element which is not 'in plain sight'")
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

}
