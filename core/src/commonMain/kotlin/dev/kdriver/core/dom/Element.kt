package dev.kdriver.core.dom

import dev.kdriver.cdp.domain.*
import dev.kdriver.core.browser.filterRecurse
import dev.kdriver.core.tab.Tab
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.util.logging.Logger

data class Element(
    val node: DOM.Node,
    val tab: Tab,
    val tree: DOM.Node? = null,
) {

    private val logger = Logger.getLogger(Element::class.java.name)

    private var remoteObject: Runtime.RemoteObject? = null

    val tag: String
        get() = node.nodeName.lowercase()

    val text: String
        get() = filterRecurse(
            node,
            predicate = { it.nodeType == 3 },
            getChildren = { it.children },
            getShadowRoots = { it.shadowRoots }
        )?.nodeValue ?: ""

    val backendNodeId: Int
        get() = node.backendNodeId

    val objectId: String?
        get() = remoteObject?.objectId

    val parentId: Int?
        get() = node.parentId

    val parent: Element?
        get() {
            val tree = this.tree ?: throw RuntimeException("could not get parent since the element has no tree set")
            val parentNode = filterRecurse(
                tree,
                predicate = { node -> node.nodeId == this.parentId },
                getChildren = { node.children },
                getShadowRoots = { it.shadowRoots }
            ) ?: return null
            return Element(parentNode, tab = this.tab, tree = this.tree)
        }


    suspend fun update() {
        // TODO
    }

    private suspend fun flash(durationSeconds: Double) {
        // TODO: Do we really need this?
        // displays for a short time a red dot on the element (only if the element itself is visible)
    }

    suspend fun click() {
        remoteObject = tab.dom.resolveNode(backendNodeId = backendNodeId).`object`
        val objectId = remoteObject?.objectId ?: throw IllegalStateException("Could not resolve object id for $this")

        val arguments = listOf(Runtime.CallArgument(objectId = objectId))

        flash(0.25)
        tab.runtime.callFunctionOn(
            functionDeclaration = "(el) => el.click()",
            objectId = objectId,
            arguments = arguments,
            awaitPromise = true,
            userGesture = true,
            returnByValue = true
        )
    }

    suspend fun mouseMove() {
        val position = getPosition()
        if (position == null) {
            logger.warning("Could not find location for $this, not moving mouse")
            return
        }
        val (x, y) = position.center
        logger.fine("Mouse move to location %.2f, %.2f where %s is located".format(x, y, this))

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

    suspend fun sendKeys(text: String) {
        apply("(elem) => elem.focus()")
        for (char in text) tab.input.dispatchKeyEvent(
            type = "char",
            text = char.toString()
        )
    }

    suspend fun apply(
        jsFunction: String,
    ): JsonElement? {
        remoteObject = tab.dom.resolveNode(backendNodeId = backendNodeId).`object`

        val result = tab.runtime.callFunctionOn(
            functionDeclaration = jsFunction,
            objectId = remoteObject?.objectId,
            arguments = listOf(
                Runtime.CallArgument(objectId = remoteObject?.objectId)
            ),
            returnByValue = true,
            userGesture = true
        )

        return result.result.value
    }

    suspend fun getPosition(abs: Boolean = false): Position? {
        if (remoteObject == null || parent == null || objectId == null) {
            remoteObject = tab.dom.resolveNode(backendNodeId = backendNodeId).`object`
        }

        return try {
            val quads = tab.dom.getContentQuads(objectId = remoteObject!!.objectId).quads
            if (quads.isEmpty()) {
                throw Exception("could not find position for $this")
            }
            val pos = Position(quads[0])
            if (abs) {
                val scrollY = (tab.evaluate("window.scrollY") as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                val scrollX = (tab.evaluate("window.scrollX") as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
                val absX = pos.left + scrollX + pos.width / 2
                val absY = pos.top + scrollY + pos.height / 2
                pos.absX = absX
                pos.absY = absY
            }
            pos
        } catch (e: IndexOutOfBoundsException) {
            logger.fine("no content quads for $this. mostly caused by element which is not 'in plain sight'")
            null
        }
    }

}
