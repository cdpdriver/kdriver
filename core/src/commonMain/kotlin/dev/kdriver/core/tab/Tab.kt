package dev.kdriver.core.tab

import dev.kdriver.cdp.domain.Target
import dev.kdriver.cdp.domain.dom
import dev.kdriver.core.browser.BrowserTarget
import dev.kdriver.core.connection.Connection

class Tab(
    websocketUrl: String,
    target: Target.TargetInfo,
) : Connection(websocketUrl, target), BrowserTarget {

    suspend fun getContent(): String {
        val doc = cdp().dom.getDocument(depth = -1, pierce = true)
        return cdp().dom.getOuterHTML(backendNodeId = doc.root.backendNodeId).outerHTML
    }

    override fun toString(): String {
        return "Tab: ${target?.toString() ?: "no target"}"
    }

}
