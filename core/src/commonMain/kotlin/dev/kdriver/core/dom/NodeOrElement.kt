package dev.kdriver.core.dom

import dev.kdriver.cdp.domain.DOM

sealed class NodeOrElement {

    data class WrappedNode(override val node: DOM.Node) : NodeOrElement()

    data class WrappedElement(val element: Element) : NodeOrElement() {
        override val node: DOM.Node
            get() = element.node
    }

    abstract val node: DOM.Node

}
