package dev.kdriver.core.dom

import dev.kdriver.cdp.domain.DOM
import dev.kdriver.core.browser.filterRecurse
import dev.kdriver.core.tab.Tab

data class Element(
    val node: DOM.Node,
    val tab: Tab,
    val tree: DOM.Node? = null,
) {

    suspend fun update() {
        // TODO
    }

    val tag: String
        get() = node.nodeName.lowercase()

    val text: String
        get() = filterRecurse(
            node,
            predicate = { it.nodeType == 3 },
            getChildren = { it.children },
            getShadowRoots = { it.shadowRoots }
        )?.nodeValue ?: ""

}
