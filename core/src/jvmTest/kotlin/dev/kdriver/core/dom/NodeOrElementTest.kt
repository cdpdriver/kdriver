package dev.kdriver.core.dom

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeOrElementTest {

    @Test
    fun testNodeOrElementWrappedNode() {
        val node = dev.kdriver.cdp.domain.DOM.Node(
            nodeId = 1,
            backendNodeId = 2,
            nodeType = 1,
            nodeName = "DIV",
            localName = "div",
            nodeValue = ""
        )
        val wrapped = NodeOrElement.WrappedNode(node)
        assertEquals(node, wrapped.node)
        assertEquals(1, wrapped.node.nodeId)
    }

}
