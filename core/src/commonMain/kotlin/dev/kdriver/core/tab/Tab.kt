package dev.kdriver.core.tab

import dev.kdriver.cdp.domain.*
import dev.kdriver.cdp.domain.Target
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.BrowserTarget
import dev.kdriver.core.connection.Connection
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class Tab(
    websocketUrl: String,
    targetInfo: Target.TargetInfo,
    var owner: Browser? = null,
) : Connection(websocketUrl, targetInfo), BrowserTarget {

    suspend fun get(
        url: String = "about:blank",
        newTab: Boolean = false,
        newWindow: Boolean = false,
    ): Tab {
        val browser = owner ?: throw IllegalStateException(
            "This tab has no browser reference, so you can't use get()"
        )

        val openNewTab = newWindow || newTab

        return if (openNewTab) {
            browser.get(url, newTab = true, newWindow = newWindow)
        } else {
            page.navigate(url)
            wait()
            this
        }
    }

    suspend fun back() {
        runtime.evaluate("window.history.back()")
    }

    suspend fun forward() {
        runtime.evaluate("window.history.forward()")
    }

    suspend fun reload(
        ignoreCache: Boolean = true,
        scriptToEvaluateOnLoad: String? = null,
    ) {
        page.reload(
            ignoreCache = ignoreCache,
            scriptToEvaluateOnLoad = scriptToEvaluateOnLoad
        )
    }

    suspend fun evaluate(
        expression: String,
        awaitPromise: Boolean = false,
    ): JsonElement? {
        val result = runtime.evaluate(
            expression = expression,
            returnByValue = true,
            userGesture = true,
            awaitPromise = awaitPromise,
            allowUnsafeEvalBlockedByCSP = true,
        )
        result.exceptionDetails?.let { throw EvaluateException(it) }
        return result.result.value
    }

    suspend fun setUserAgent(
        userAgent: String? = null,
        acceptLanguage: String? = null,
        platform: String? = null,
    ) {
        val ua = userAgent
            ?: (evaluate("navigator.userAgent") as? JsonPrimitive)?.content
            ?: throw IllegalStateException("Could not read existing user agent from navigator object")

        network.setUserAgentOverride(
            userAgent = ua,
            acceptLanguage = acceptLanguage,
            platform = platform
        )
    }

    suspend fun getContent(): String {
        val doc = dom.getDocument(depth = -1, pierce = true)
        return dom.getOuterHTML(backendNodeId = doc.root.backendNodeId).outerHTML
    }

    override fun toString(): String {
        return "Tab: ${this@Tab.targetInfo?.toString() ?: "no target"}"
    }

}
