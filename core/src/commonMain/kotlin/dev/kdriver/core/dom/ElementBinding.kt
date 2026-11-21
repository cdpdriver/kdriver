package dev.kdriver.core.dom

import dev.kdriver.cdp.domain.DOM
import dev.kdriver.core.tab.DefaultTab

/**
 * The node and the document of a [DefaultElement], and the binding of an element found in the page to the document
 * [DefaultTab] keeps from its last read, when it still matches the page, without any command: node ids, [tree] and
 * parent are then those a lookup on the document gave. The document's node is taken only when it holds the same
 * subtree as the one found (ids aside), so a read never changes what the element shows. Each kept document is looked
 * at once; without one, the element stays as found until `update` or a lookup under it.
 */
internal class ElementBinding(node: DOM.Node, tree: DOM.Node?) {

    var node: DOM.Node = node
        private set

    var tree: DOM.Node? = tree
        private set

    // The epoch of the kept document the last binding attempt looked at.
    private var checkedAt: Long = NOT_CHECKED

    fun replaceNode(value: DOM.Node) {
        node = value
        checkedAt = NOT_CHECKED
    }

    fun replaceTree(value: DOM.Node?) {
        tree = value
        checkedAt = NOT_CHECKED
    }

    /** Binds to [kept] when this element has no tree yet and [kept] was not looked at; returns this binding. */
    fun bindFrom(kept: DefaultTab.DocumentSnapshot?): ElementBinding {
        if (tree == null && kept != null && kept.epoch != checkedAt) {
            checkedAt = kept.epoch
            val found = node
            val documentNode = kept.nodeOf(found.backendNodeId)
            if (documentNode != null && documentNode.withoutIds() == found.withoutIds()) {
                tree = kept.root
                node = documentNode
            }
        }
        return this
    }

    private companion object {
        // No kept document looked at yet: epochs are never negative.
        const val NOT_CHECKED = -1L
    }
}
