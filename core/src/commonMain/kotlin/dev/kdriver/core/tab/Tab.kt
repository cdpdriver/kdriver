package dev.kdriver.core.tab

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.domain.*
import dev.kdriver.cdp.domain.Target
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.BrowserTarget
import dev.kdriver.core.connection.Connection
import dev.kdriver.core.dom.Element
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.exceptions.TimeoutWaitingForReadyStateException
import dev.kdriver.core.utils.filterRecurse
import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.time.Duration.Companion.seconds

/**
 * Represents a browser tab, which is a connection to a specific target in the browser.
 *
 * This class provides methods to interact with the tab, such as navigating to URLs,
 * managing history, evaluating JavaScript expressions, and manipulating the DOM.
 */
class Tab(
    websocketUrl: String,
    messageListeningScope: CoroutineScope,
    eventsBufferSize: Int,
    targetInfo: Target.TargetInfo,
    owner: Browser? = null,
) : Connection(websocketUrl, messageListeningScope, eventsBufferSize, targetInfo, owner), BrowserTarget {

    private val logger = KtorSimpleLogger("Tab")

    /**
     * Navigate to a specified URL in the tab.
     *
     * @param url The URL to navigate to. Defaults to "about:blank".
     * @param newTab If true, opens the URL in a new tab instead of the current one.
     * @param newWindow If true, opens the URL in a new window instead of the current one.
     *
     * @return The current tab instance after navigation.
     */
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

    /**
     * Navigate back in the tab's history.
     */
    suspend fun back() {
        runtime.evaluate("window.history.back()")
    }

    /**
     * Navigate forward in the tab's history.
     */
    suspend fun forward() {
        runtime.evaluate("window.history.forward()")
    }

    /**
     * Reload the current page in the tab.
     *
     * @param ignoreCache If true, reloads the page without using the cache.
     * @param scriptToEvaluateOnLoad Optional JavaScript to evaluate after the page loads.
     */
    suspend fun reload(
        ignoreCache: Boolean = true,
        scriptToEvaluateOnLoad: String? = null,
    ) {
        page.reload(
            ignoreCache = ignoreCache,
            scriptToEvaluateOnLoad = scriptToEvaluateOnLoad
        )
    }

    /**
     * Evaluates a JavaScript expression in the context of the tab.
     *
     * @param expression The JavaScript expression to evaluate.
     * @param awaitPromise If true, waits for any promises to resolve before returning the result.
     *
     * @return The result of the evaluation, deserialized to type T, or null if no result is returned.
     */
    suspend inline fun <reified T> evaluate(
        expression: String,
        awaitPromise: Boolean = false,
    ): T? {
        val result = runtime.evaluate(
            expression = expression,
            returnByValue = true,
            userGesture = true,
            awaitPromise = awaitPromise,
            allowUnsafeEvalBlockedByCSP = true,
        )
        result.exceptionDetails?.let { throw EvaluateException(it) }
        return result.result.value?.let {
            Serialization.json.decodeFromJsonElement<T>(it)
        }
    }

    /**
     * Sets the user agent for the tab.
     *
     * This method allows you to override the user agent string, accept language, and platform
     * for the tab's network requests. If no user agent is provided, it will read the existing user agent
     * from the navigator object.
     *
     * @param userAgent The user agent string to set. If null, the existing user agent will be used.
     * @param acceptLanguage The accept language string to set. If null, it will not override the existing value.
     * @param platform The platform string to set. If null, it will not override the existing value.
     */
    suspend fun setUserAgent(
        userAgent: String? = null,
        acceptLanguage: String? = null,
        platform: String? = null,
    ) {
        val ua = userAgent
            ?: evaluate<String>("navigator.userAgent")
            ?: throw IllegalStateException("Could not read existing user agent from navigator object")

        network.setUserAgentOverride(
            userAgent = ua,
            acceptLanguage = acceptLanguage,
            platform = platform
        )
    }

    /**
     * Gets the current window information for the tab.
     *
     * @return A [dev.kdriver.cdp.domain.Browser.GetWindowForTargetReturn] object containing the window ID and bounds.
     */
    suspend fun getWindow(): dev.kdriver.cdp.domain.Browser.GetWindowForTargetReturn {
        return browser.getWindowForTarget(targetId)
    }

    /**
     * Gets the content of the tab as a string.
     *
     * This method retrieves the outer HTML of the document in the tab, which includes the entire HTML structure.
     *
     * @return The outer HTML of the document as a string.
     */
    suspend fun getContent(): String {
        val doc = dom.getDocument(depth = -1, pierce = true)
        return dom.getOuterHTML(backendNodeId = doc.root.backendNodeId).outerHTML
    }

    /**
     * Activates the tab, bringing it to the foreground.
     *
     * This method uses the target's ID to activate the target in the browser.
     *
     * @throws IllegalArgumentException if the target is null.
     */
    suspend fun activate() {
        val targetId = targetInfo?.targetId
            ?: throw IllegalArgumentException("target is null")
        target.activateTarget(targetId)
    }

    /**
     * Brings the tab to the front, activating it.
     *
     * This method is a convenience method that calls [activate].
     */
    suspend fun bringToFront() {
        activate()
    }

    /**
     * Maximizes the tab's window.
     *
     * This method sets the window state to "maximize", which typically maximizes the browser window.
     */
    suspend fun maximize() {
        setWindowState(state = "maximize")
    }

    /**
     * Minimizes the tab's window.
     *
     * This method sets the window state to "minimize", which typically minimizes the browser window.
     */
    suspend fun minimize() {
        setWindowState(state = "minimize")
    }

    /**
     * Sets the tab's window to fullscreen mode.
     *
     * This method sets the window state to "fullscreen", which typically makes the browser window occupy the entire screen.
     */
    suspend fun fullscreen() {
        setWindowState(state = "fullscreen")
    }

    /**
     * Sets the tab's window state, including position and size.
     *
     * This method allows you to set the window's position (left, top), size (width, height), and state (normal, minimized, maximized, fullscreen).
     *
     * @param left The left position of the window. Defaults to 0.
     * @param top The top position of the window. Defaults to 0.
     * @param width The width of the window. Defaults to 1280.
     * @param height The height of the window. Defaults to 720.
     * @param state The state of the window. Defaults to "normal". Valid values are "minimized", "maximized", "fullscreen", and "normal".
     */
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
        } ?: throw IllegalStateException(
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

    /**
     * Scrolls the tab down by a specified amount.
     *
     * This method simulates a scroll gesture downwards by a percentage of the viewport height.
     *
     * @param amount The percentage of the viewport height to scroll down. Defaults to 25%. 25 is a quarter of page, 50 half, and 1000 is 10x the page
     * @param speed Swipe speed in pixels per second (default: 800).
     */
    suspend fun scrollDown(amount: Int = 25, speed: Int = 800) {
        val (_, bounds) = getWindow()
        val yDistance = bounds.height?.times(amount / 100.0) ?: return

        input.synthesizeScrollGesture(
            x = 0.0,
            y = 0.0,
            yDistance = yDistance.unaryMinus(),
            yOverscroll = 0.0,
            xOverscroll = 0.0,
            preventFling = true,
            repeatDelayMs = 0,
            speed = speed
        )
        delay((yDistance / speed).seconds)
    }

    /**
     * Scrolls the tab up by a specified amount.
     *
     * This method simulates a scroll gesture upwards by a percentage of the viewport height.
     *
     * @param amount The percentage of the viewport height to scroll up. Defaults to 25%. 25 is a quarter of page, 50 half, and 1000 is 10x the page
     * @param speed Swipe speed in pixels per second (default: 800).
     */
    suspend fun scrollUp(amount: Int = 25, speed: Int = 800) {
        val (_, bounds) = getWindow()
        val yDistance = bounds.height?.times(amount / 100.0) ?: return

        input.synthesizeScrollGesture(
            x = 0.0,
            y = 0.0,
            yDistance = yDistance,
            yOverscroll = 0.0,
            xOverscroll = 0.0,
            preventFling = true,
            repeatDelayMs = 0,
            speed = speed
        )
        delay((yDistance / speed).seconds)
    }

    /**
     * Waits for the document's ready state to reach a specified state.
     *
     * This method continuously checks the document's ready state until it matches the specified state or a timeout occurs.
     *
     * @param until The desired ready state to wait for. Can be "loading", "interactive", or "complete". Defaults to "interactive".
     *
     * @throws TimeoutWaitingForReadyStateException if the timeout is reached before the ready state matches.
     *
     * @return True if the ready state matches the specified state before the timeout, false otherwise.
     */
    suspend fun waitForReadyState(
        until: ReadyState = ReadyState.INTERACTIVE, // "loading", "interactive", or "complete"
        timeoutSeconds: Int = 10,
    ): Boolean {
        val startTime = Clock.System.now().toEpochMilliseconds()

        while (true) {
            val readyState = evaluate<ReadyState>("document.readyState")
            if (readyState == until) return true

            val elapsed = (Clock.System.now().toEpochMilliseconds() - startTime) / 1000
            if (elapsed > timeoutSeconds)
                throw TimeoutWaitingForReadyStateException(until, timeoutSeconds, readyState)

            delay(100) // wait 100 ms
        }
    }

    /**
     * Selects an element in the DOM using a CSS selector.
     *
     * This method waits for the element to appear in the DOM, retrying every 500 milliseconds until the timeout is reached.
     *
     * @param selector The CSS selector of the element to select.
     * @param timeoutSeconds The maximum time in seconds to wait for the element to appear. Defaults to 10 seconds.
     *
     * @throws TimeoutWaitingForElementException if the element is not found before the timeout.
     *
     * @return The selected [Element] if found before the timeout, otherwise throws a [TimeoutWaitingForElementException].
     */
    suspend fun select(
        selector: String,
        timeoutSeconds: Int = 10,
    ): Element {
        val trimmedSelector = selector.trim()
        val startTime = Clock.System.now().toEpochMilliseconds()

        var item = querySelector(trimmedSelector)
        while (item == null) {
            wait()

            item = querySelector(trimmedSelector)

            val elapsed = (Clock.System.now().toEpochMilliseconds() - startTime) / 1000
            if (elapsed > timeoutSeconds)
                throw TimeoutWaitingForElementException(selector, timeoutSeconds)

            delay(500) // sleep for 0.5 seconds
        }
        return item
    }

    suspend fun querySelectorAll(
        selector: String,
        node: DOM.Node? = null,
    ): List<Element> {
        val lastMap = mutableMapOf<Int, Boolean>()

        val doc = if (node == null) {
            dom.getDocument(-1, true).root
        } else {
            var docNode = node
            if (node.nodeName == "IFRAME") {
                docNode = node.contentDocument ?: node
            }
            docNode
        }

        val nodeIds = try {
            dom.querySelectorAll(doc.nodeId, selector)
        } catch (e: Exception) {
            if (node != null && e.message?.contains("could not find node", ignoreCase = true) == true) {
                val last = lastMap[node.nodeId]

                if (last == true) {
                    // Remove the marker to avoid infinite recursion
                    lastMap.remove(node.nodeId)
                    return emptyList()
                }

                if (node is Element) node.update()

                // Mark as retried once
                lastMap[node.nodeId] = true

                return querySelectorAll(selector, node)
            } else {
                disableDomAgent()
                throw e
            }
        }.nodeIds

        if (nodeIds.isEmpty()) return emptyList()

        val items = mutableListOf<Element>()
        for (nid in nodeIds) {
            val innerNode = filterRecurse(
                doc,
                predicate = { it.nodeId == nid },
                getChildren = { it.children },
                getShadowRoots = { it.shadowRoots }
            )
            if (innerNode != null) {
                val elem = Element(innerNode, this, doc)
                items.add(elem)
            }
        }
        return items
    }

    suspend fun querySelector(
        selector: String,
        node: DOM.Node? = null,
    ): Element? {
        val lastMap = mutableMapOf<Int, Boolean>()

        val trimmedSelector = selector.trim()

        val doc = if (node == null) {
            dom.getDocument(-1, true).root
        } else {
            if (node.nodeName == "IFRAME") node.contentDocument ?: node
            else node
        }

        val nodeId = try {
            dom.querySelector(doc.nodeId, trimmedSelector)
        } catch (e: Exception) {
            if (node != null && e.message?.contains("could not find node", ignoreCase = true) == true) {
                val last = lastMap[node.nodeId]

                if (last == true) {
                    // Remove the marker to avoid infinite recursion
                    lastMap.remove(node.nodeId)
                    return null
                }

                if (node is Element) node.update()

                // Mark as retried once
                lastMap[node.nodeId] = true

                return querySelector(trimmedSelector, node)
            } else {
                disableDomAgent()
                throw e
            }
        }.nodeId

        if (nodeId == null) return null

        val foundNode = filterRecurse(
            doc,
            predicate = { it.nodeId == nodeId },
            getChildren = { it.children },
            getShadowRoots = { it.shadowRoots }
        )
        return foundNode?.let { Element(it, this, doc) }
    }

    suspend fun disableDomAgent() {
        try {
            dom.disable()
        } catch (_: Exception) {
            logger.debug("Ignoring DOM.disable exception")
        }
    }

    override fun toString(): String {
        return "Tab: ${this@Tab.targetInfo?.toString() ?: "no target"}"
    }

}
