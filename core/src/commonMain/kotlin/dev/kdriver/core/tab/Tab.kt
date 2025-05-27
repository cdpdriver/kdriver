package dev.kdriver.core.tab

import dev.kdriver.cdp.domain.*
import dev.kdriver.cdp.domain.Target
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.BrowserTarget
import dev.kdriver.core.connection.Connection
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import javax.naming.NameNotFoundException

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

    suspend fun getWindow(): dev.kdriver.cdp.domain.Browser.GetWindowForTargetReturn {
        return browser.getWindowForTarget(targetId)
    }

    suspend fun getContent(): String {
        val doc = dom.getDocument(depth = -1, pierce = true)
        return dom.getOuterHTML(backendNodeId = doc.root.backendNodeId).outerHTML
    }

    suspend fun maximize() {
        setWindowState(state = "maximize")
    }

    suspend fun minimize() {
        setWindowState(state = "minimize")
    }

    suspend fun fullscreen() {
        setWindowState(state = "fullscreen")
    }

    suspend fun setWindowState(
        left: Int = 0,
        top: Int = 0,
        width: Int = 1280,
        height: Int = 720,
        state: String = "normal",
    ) {
        val availableStates = listOf("minimized", "maximized", "fullscreen", "normal")
        val (windowId, _) = getWindow()

        val stateName = availableStates.find { stateName ->
            state.lowercase().all { it in stateName }
        } ?: throw NameNotFoundException(
            "Could not determine any of $availableStates from input '$state'"
        )

        val windowState = dev.kdriver.cdp.domain.Browser.WindowState.valueOf(stateName.uppercase())

        val bounds = if (windowState == dev.kdriver.cdp.domain.Browser.WindowState.NORMAL) {
            dev.kdriver.cdp.domain.Browser.Bounds(
                left = left,
                top = top,
                width = width,
                height = height,
                windowState = windowState
            )
        } else {
            // Ensure we're in NORMAL state before switching to others
            setWindowState(state = "normal")
            dev.kdriver.cdp.domain.Browser.Bounds(windowState = windowState)
        }

        browser.setWindowBounds(windowId, bounds)
    }

    suspend fun scrollDown(amount: Int = 25) {
        val (_, bounds) = getWindow()
        val yDistance = bounds.height?.times((amount / 100.0))?.unaryMinus()

        input.synthesizeScrollGesture(
            x = 0.0,
            y = 0.0,
            yDistance = yDistance,
            yOverscroll = 0.0,
            xOverscroll = 0.0,
            preventFling = true,
            repeatDelayMs = 0,
            speed = 7777
        )
    }

    suspend fun scrollUp(amount: Int = 25) {
        val (_, bounds) = getWindow()
        val yDistance = bounds.height?.times((amount / 100.0))

        input.synthesizeScrollGesture(
            x = 0.0,
            y = 0.0,
            yDistance = yDistance,
            yOverscroll = 0.0,
            xOverscroll = 0.0,
            preventFling = true,
            repeatDelayMs = 0,
            speed = 7777
        )
    }

    suspend fun waitForReadyState(
        until: String = "interactive", // "loading", "interactive", or "complete"
        timeoutSeconds: Int = 10,
    ): Boolean {
        val startTime = System.nanoTime()
        val timeoutNanos = timeoutSeconds * 1_000_000_000L

        while (true) {
            val readyState = (evaluate("document.readyState") as? JsonPrimitive)?.content
            if (readyState == until) {
                return true
            }

            if (System.nanoTime() - startTime > timeoutNanos) {
                throw IllegalStateException("Timeout waiting for readyState == $until")
            }

            delay(100) // wait 100 ms
        }
    }

    override fun toString(): String {
        return "Tab: ${this@Tab.targetInfo?.toString() ?: "no target"}"
    }

}
