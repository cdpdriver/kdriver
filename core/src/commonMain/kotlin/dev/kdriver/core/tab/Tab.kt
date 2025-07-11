package dev.kdriver.core.tab

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDPException
import dev.kdriver.cdp.domain.*
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.BrowserTarget
import dev.kdriver.core.connection.Connection
import dev.kdriver.core.dom.Element
import dev.kdriver.core.dom.NodeOrElement
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.exceptions.TimeoutWaitingForReadyStateException
import dev.kdriver.core.network.BaseFetchInterception
import dev.kdriver.core.network.BaseRequestExpectation
import dev.kdriver.core.utils.filterRecurse
import io.ktor.http.*
import io.ktor.util.logging.*
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.String
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.abs
import kotlin.text.String
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
    suspend fun rawEvaluate(
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
        val raw = rawEvaluate(expression, awaitPromise) ?: return null
        return Serialization.json.decodeFromJsonElement<T>(raw)
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
     * Restores the tab's window to its normal state.
     *
     * This method sets the window state to "normal", which typically restores the browser window to its default size and position.
     */
    suspend fun medimize() {
        setWindowState(state = "normal")
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
     * @param until The desired ready state to wait for. Can be LOADING, INTERACTIVE, or COMPLETE. Defaults to INTERACTIVE.
     *
     * @throws TimeoutWaitingForReadyStateException if the timeout is reached before the ready state matches.
     *
     * @return True if the ready state matches the specified state before the timeout, false otherwise.
     */
    suspend fun waitForReadyState(
        until: ReadyState = ReadyState.INTERACTIVE,
        timeout: Long = 10_000,
    ): Boolean {
        val startTime = Clock.System.now().toEpochMilliseconds()

        while (true) {
            val readyState = evaluate<ReadyState>("document.readyState")
            if (readyState == until) return true

            val elapsed = Clock.System.now().toEpochMilliseconds() - startTime
            if (elapsed > timeout) throw TimeoutWaitingForReadyStateException(until, timeout, readyState)

            delay(100) // wait 100 ms
        }
    }

    /**
     * Finds a single element by its text content, optionally waiting for it to appear.
     *
     * @param text The text to search for. Note: script contents are also considered text.
     * @param bestMatch If true (default), returns the element with the most comparable string length.
     *                  This helps when searching for common terms (e\.g\., "login") to get the most relevant element,
     *                  such as a login button, instead of unrelated elements containing the text.
     *                  If false, returns the first match found, which is faster.
     * @param returnEnclosingElement Since the function often returns text nodes (children of elements like "span", "p", etc\.),
     *                  this flag (default: true) returns the containing element instead of the text node itself.
     *                  Set to false if you want the text node or for cases like elements with "placeholder=" property.
     *                  If the found node is not a text node but a regular element, the flag is ignored and the element is returned.
     * @param timeout The maximum time in milliseconds to wait for the element to appear before raising a timeout exception.
     *
     * @return The found [Element], or null if no matching element is found within the timeout.
     */
    suspend fun find(
        text: String,
        bestMatch: Boolean = true,
        returnEnclosingElement: Boolean = true,
        timeout: Long = 10_000,
    ): Element {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val trimmedText = text.trim()
        while (true) {
            wait()
            findElementByText(trimmedText, bestMatch, returnEnclosingElement)?.let { return it }
            val elapsed = Clock.System.now().toEpochMilliseconds() - startTime
            if (elapsed > timeout) throw TimeoutWaitingForElementException(trimmedText, timeout)
            delay(500)
        }
    }

    /**
     * Selects an element in the DOM using a CSS selector.
     *
     * This method waits for the element to appear in the DOM, retrying every 500 milliseconds until the timeout is reached.
     *
     * @param selector The CSS selector of the element to select.
     * @param timeout The maximum time in milliseconds to wait for the element to appear. Defaults to 10 seconds.
     *
     * @throws TimeoutWaitingForElementException if the element is not found before the timeout.
     *
     * @return The selected [Element] if found before the timeout, otherwise throws a [TimeoutWaitingForElementException].
     */
    suspend fun select(
        selector: String,
        timeout: Long = 10_000,
    ): Element {
        val trimmedSelector = selector.trim()
        val startTime = Clock.System.now().toEpochMilliseconds()

        while (true) {
            wait()
            querySelector(trimmedSelector)?.let { return it }
            val elapsed = Clock.System.now().toEpochMilliseconds() - startTime
            if (elapsed > timeout) throw TimeoutWaitingForElementException(selector, timeout)
            delay(500) // sleep for 0.5 seconds
        }
    }

    /**
     * Finds multiple elements by their text content, optionally waiting for them to appear.
     *
     * This method searches for all elements containing the specified text. If no elements are found,
     * it waits and retries until at least one element is found or the timeout is reached.
     *
     * @param text The text to search for. Note: script contents are also considered text.
     * @param timeout The maximum time in milliseconds to wait for elements to appear before raising a timeout exception.
     *
     * @return A list of found [Element]s.
     *
     * @throws TimeoutWaitingForElementException if no elements are found within the timeout.
     */
    suspend fun findAll(
        text: String,
        timeout: Long = 10_000,
    ): List<Element> {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val trimmedText = text.trim()
        while (true) {
            wait()
            findElementsByText(trimmedText).takeIf { it.isNotEmpty() }?.let { return it }
            val elapsed = Clock.System.now().toEpochMilliseconds() - startTime
            if (elapsed > timeout) throw TimeoutWaitingForElementException(trimmedText, timeout)
            delay(500)
        }
    }

    /**
     * Finds multiple elements by CSS selector, optionally waiting for them to appear.
     *
     * Can also be used to wait for such elements to appear. Optionally includes results from iframes.
     *
     * @param selector CSS selector, e\.g\., `a[href]`, `button[class*=close]`, `a > img[src]`
     * @param timeout Raise timeout exception when after this many milliseconds nothing is found.
     * @param includeFrames Whether to include results in iframes.
     *
     * @return List of found [Element]s.
     * @throws TimeoutWaitingForElementException if no elements are found within the timeout.
     */
    suspend fun selectAll(
        selector: String,
        timeout: Long = 10_000,
        includeFrames: Boolean = false,
    ): List<Element> {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val trimmedSelector = selector.trim()

        while (true) {
            val items = mutableListOf<Element>()
            if (includeFrames) {
                val frames = querySelectorAll("iframe")
                for (fr in frames) items.addAll(fr.querySelectorAll(trimmedSelector))
            }
            items.addAll(querySelectorAll(trimmedSelector))
            items.takeIf { it.isNotEmpty() }?.let { return it }

            val elapsed = Clock.System.now().toEpochMilliseconds() - startTime
            if (elapsed > timeout) throw TimeoutWaitingForElementException(trimmedSelector, timeout)
            delay(500)
        }
    }

    /**
     * Finds elements by XPath string.
     *
     * If not immediately found, retries are attempted until [timeout] is reached (default 10 seconds).
     * In case nothing is found, it returns an empty list. It will not throw.
     * This timeout mechanism helps when relying on some element to appear before continuing your script.
     *
     * Example usage:
     *
     * ```kotlin
     * // Find all the inline scripts (script elements without src attribute)
     * tab.xpath("//script[not(@src)]")
     *
     * // More complex, case-insensitive text search
     * tab.xpath("//text()[ contains( translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'test')]")
     * ```
     *
     * @param xpath The XPath string to search for.
     * @param timeout The maximum time in milliseconds to wait for elements to appear before returning. Default is 10 seconds.
     *
     * @return List of found [Element]s, or an empty list if none found within the timeout.
     */
    suspend fun xpath(
        xpath: String,
        timeout: Long = 10_000,
    ): List<Element> {
        val items = mutableListOf<Element>()
        try {
            dom.enable()
            items.addAll(findAll(xpath, timeout = 0))
            if (items.isEmpty()) {
                val startTime = Clock.System.now().toEpochMilliseconds()
                while (items.isEmpty()) {
                    items.addAll(findAll(xpath, timeout = 0))
                    delay(100)
                    val elapsed = Clock.System.now().toEpochMilliseconds() - startTime
                    if (elapsed > timeout) break
                }
            }
        } finally {
            disableDomAgent()
        }
        return items
    }

    /**
     * Equivalent of JavaScript's `document.querySelectorAll`.
     * This is considered one of the main methods to use in this package.
     *
     * It returns all matching [Element] objects.
     *
     * @param selector CSS selector. (first time? See https://www.w3schools.com/cssref/css_selectors.php)
     * @param node For internal use. The node to start the search from. Defaults to the document root.
     *
     * @return List of matching [Element]s.
     */
    suspend fun querySelectorAll(
        selector: String,
        node: NodeOrElement? = null,
    ): List<Element> {
        val lastMap = mutableMapOf<Int, Boolean>()

        val doc = if (node == null) {
            dom.getDocument(-1, true).root
        } else {
            if (node.node.nodeName == "IFRAME") node.node.contentDocument ?: node.node
            else node.node
        }

        val nodeIds = try {
            dom.querySelectorAll(doc.nodeId, selector)
        } catch (e: Exception) {
            if (node != null && e.message?.contains("could not find node", ignoreCase = true) == true) {
                val last = lastMap[node.node.nodeId]

                if (last == true) {
                    // Remove the marker to avoid infinite recursion
                    lastMap.remove(node.node.nodeId)
                    return emptyList()
                }

                if (node is NodeOrElement.WrappedElement) node.element.update()

                // Mark as retried once
                lastMap[node.node.nodeId] = true

                return querySelectorAll(selector, node)
            } else {
                disableDomAgent()
                throw e
            }
        }.nodeIds

        if (nodeIds.isEmpty()) return emptyList()

        val items = mutableListOf<Element>()
        for (nid in nodeIds) {
            val innerNode = filterRecurse(doc) { it.nodeId == nid }
            if (innerNode != null) {
                val elem = Element(this, innerNode, doc)
                items.add(elem)
            }
        }
        return items
    }

    /**
     * Finds a single element based on a CSS selector string.
     *
     * @param selector CSS selector(s).
     * @param node For internal use. The node to start the search from. Defaults to the document root.
     *
     * @return The found [Element], or null if no matching element is found.
     */
    suspend fun querySelector(
        selector: String,
        node: NodeOrElement? = null,
    ): Element? {
        val lastMap = mutableMapOf<Int, Boolean>()

        val trimmedSelector = selector.trim()

        val doc = if (node == null) {
            dom.getDocument(-1, true).root
        } else {
            if (node.node.nodeName == "IFRAME") node.node.contentDocument ?: node.node
            else node.node
        }

        val nodeId = try {
            dom.querySelector(doc.nodeId, trimmedSelector)
        } catch (e: Exception) {
            if (node != null && e.message?.contains("could not find node", ignoreCase = true) == true) {
                val last = lastMap[node.node.nodeId]

                if (last == true) {
                    // Remove the marker to avoid infinite recursion
                    lastMap.remove(node.node.nodeId)
                    return null
                }

                if (node is NodeOrElement.WrappedElement) node.element.update()

                // Mark as retried once
                lastMap[node.node.nodeId] = true

                return querySelector(trimmedSelector, node)
            } else if (e.message?.contains("could not find node", ignoreCase = true) == true) {
                return null
            } else {
                disableDomAgent()
                throw e
            }
        }.nodeId

        if (nodeId == null) return null

        val foundNode = filterRecurse(doc) { it.nodeId == nodeId }
        return foundNode?.let { Element(this, it, doc) }
    }

    /**
     * Returns elements which match the given text.
     * Please note: this may (or will) also return any other element (like inline scripts),
     * which happen to contain that text.
     *
     * @param text The text to search for.
     * @param tagHint When provided, narrows down search to only elements which match given tag (e.g., a, div, script, span).
     * @return List of matching [Element]s.
     */
    suspend fun findElementsByText(
        text: String,
        tagHint: String? = null,
    ): List<Element> {
        val trimmedText = text.trim()
        val doc = dom.getDocument(-1, true).root
        val search = dom.performSearch(trimmedText, true)
        val nodeIds =
            if (search.resultCount > 0) dom.getSearchResults(search.searchId, 0, search.resultCount).nodeIds
            else emptyList()
        dom.discardSearchResults(search.searchId)

        val items = mutableListOf<Element>()
        for (nid in nodeIds) {
            val node = filterRecurse(doc) { it.nodeId == nid }
            if (node == null) {
                // Try to resolve the node if not found in the local tree
                val resolvedNode = try {
                    dom.resolveNode(nodeId = nid)
                } catch (_: Exception) {
                    null
                }
                if (resolvedNode == null) continue
                // Optionally, you could resolve backendNodeId to nodeId here if needed
                // val remoteObject = dom.resolveNode(backendNodeId = resolvedNode.backendNodeId)
                // val resolvedNodeId = dom.requestNode(objectId = remoteObject.objectId)
                // node = filterRecurse(doc, { it.nodeId == resolvedNodeId })
                continue
            }
            try {
                val elem = Element(this, node, doc)
                if (elem.nodeType == 3) {
                    // if found element is a text node (which is plain text, and useless for our purpose),
                    // we return the parent element of the node (which is often a tag which can have text between their
                    // opening and closing tags (that is most tags, except for example "img" and "video", "br")

                    elem.update() // check if parent actually has a parent and update it to be absolutely sure
                    items.add(elem.parent ?: elem) // when it really has no parent, use the text node itself
                } else {
                    // just add the element itself
                    items.add(elem)
                }
            } catch (_: Exception) {
                continue
            }
        }

        // since we already fetched the entire doc, including shadow and frames
        // let's also search through the iframes
        val iframes = filterRecurse(doc) { it.nodeName == "IFRAME" }
        if (iframes != null) {
            val iframeElems = listOf(Element(this, iframes, iframes.contentDocument ?: doc))
            for (iframeElem in iframeElems) {
                val iframeTextNodes = filterRecurse(iframeElem.node) { n ->
                    n.nodeType == 3 && n.nodeValue.contains(trimmedText, ignoreCase = true)
                }
                if (iframeTextNodes != null) {
                    val textElem = Element(this, iframeTextNodes, iframeElem.node)
                    items.add(textElem.parent ?: textElem)
                }
            }
        }

        return items
    }

    /**
     * Finds and returns the first element containing the specified [text], or the best match if [bestMatch] is true.
     *
     * @param text The text to search for within elements.
     * @param bestMatch If true, finds the closest match based on length, which is more expensive and slower.
     *                  This is useful when searching for common terms (e.g., "login") to get the most relevant element,
     *                  such as a login button, instead of unrelated elements containing the text.
     * @param returnEnclosingElement If true, returns the enclosing element of the found text node.
     *
     * @return The found [Element], or null if no matching element is found.
     */
    suspend fun findElementByText(
        text: String,
        bestMatch: Boolean = false,
        returnEnclosingElement: Boolean = true,
    ): Element? {
        val items = findElementsByText(text)

        val trimmedText = text.trim()
        try {
            if (items.isEmpty()) return null
            return if (bestMatch) {
                items.minByOrNull { abs(trimmedText.length - it.textAll.length) } ?: items.firstOrNull()
            } else {
                // naively just return the first result
                items.firstOrNull()
            }
        } finally {
            disableDomAgent()
        }
    }

    /**
     * Disables the DOM agent to stop receiving DOM-related events.
     *
     * This method is useful when you no longer need to interact with the DOM or want to clean up resources.
     */
    suspend fun disableDomAgent() {
        try {
            dom.disable()
        } catch (_: CDPException) {
            // The DOM.disable can throw an exception if not enabled,
            // but if it's already disabled, that's not a "real" error.
            logger.debug("Ignoring DOM.disable exception")
        }
    }

    /**
     * Moves the mouse cursor to the specified \[x, y\] coordinates, optionally in multiple steps and with a flash effect.
     *
     * @param x The target x coordinate.
     * @param y The target y coordinate.
     * @param steps The number of steps to move the mouse (default: 10). If less than 1, moves in a single step.
     * @param flash If true, flashes the point at each step.
     */
    suspend fun mouseMove(x: Double, y: Double, steps: Int = 10, flash: Boolean = false) {
        // Probably the worst way of calculating this, but couldn't think of a better solution today.
        val actualSteps = if (steps < 1) 1 else steps
        if (actualSteps > 1) {
            val stepSizeX = x / actualSteps
            val stepSizeY = y / actualSteps
            val pathway = (0..actualSteps).map { i -> Pair(stepSizeX * i, stepSizeY * i) }
            for ((px, py) in pathway) {
                if (flash) flashPoint(px, py)
                input.dispatchMouseEvent("mouseMoved", px, py)
            }
        } else input.dispatchMouseEvent("mouseMoved", x, y)
        if (flash) flashPoint(x, y)
        else delay(50)
        input.dispatchMouseEvent("mouseReleased", x, y)
        if (flash) flashPoint(x, y)
    }

    /**
     * Performs a native mouse click at the specified \[x, y\] coordinates.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param button The mouse button (LEFT, RIGHT, MIDDLE). Default is LEFT.
     * @param buttons Which button (default 1 = left).
     * @param modifiers Bit field for pressed modifier keys. Alt=1, Ctrl=2, Meta/Command=4, Shift=8 (default: 0).
     */
    suspend fun mouseClick(
        x: Double,
        y: Double,
        button: Input.MouseButton = Input.MouseButton.LEFT,
        buttons: Int = 1,
        modifiers: Int = 0,
    ) {
        input.dispatchMouseEvent(
            type = "mousePressed",
            x = x,
            y = y,
            modifiers = modifiers,
            button = button,
            buttons = buttons,
            clickCount = 1
        )
        input.dispatchMouseEvent(
            type = "mouseReleased",
            x = x,
            y = y,
            modifiers = modifiers,
            button = button,
            buttons = buttons,
            clickCount = 1
        )
    }

    private suspend fun flashPoint(x: Double, y: Double, duration: Long = 250) {
        // TODO: Do we really need this?
        // displays for a short time a red dot on the element
    }

    /**
     * Expects a request to match the given [urlPattern].
     *
     * For example, capturing the response body of a specific API call can be done like this:
     * ```kotlin
     * val responseBody = tab.expect(Regex("https://api.example.com/data")) {
     *     tab.get("https://example.com")
     *     getResponseBody<UserData>()
     * }
     * ```
     *
     * @param urlPattern The regex pattern to match the request URL.
     * @param block The block to execute during which the expectation is active.
     */
    suspend fun <T> expect(urlPattern: Regex, block: suspend BaseRequestExpectation.() -> T): T {
        return BaseRequestExpectation(this, urlPattern).use(block)
    }

    /**
     * Intercepts network requests matching the given [urlPattern] and [requestStage].
     * This allows you to modify requests, responses, or block them entirely.
     *
     * Example usage:
     * ```kotlin
     * tab.intercept("https://api.example.com/data", Fetch.RequestStage.RESPONSE, Network.ResourceType.XHR) {
     *     tab.get("https://example.com")
     *
     *     // Can modify the request or response here, or intercept the body before it gets unavailable
     *     val originalResponse = getResponseBody<UserData>()
     *     continueRequest()
     * }
     */
    suspend fun <T> intercept(
        urlPattern: String,
        requestStage: Fetch.RequestStage,
        resourceType: Network.ResourceType,
        block: suspend BaseFetchInterception.() -> T,
    ): T {
        return BaseFetchInterception(this, urlPattern, requestStage, resourceType).use(block)
    }

    /**
     * Takes a screenshot of the page and returns the result as a base64 encoded string.
     * This is not the same as [Element.screenshotB64], which takes a screenshot of a single element only.
     *
     * @param format "jpeg" or "png" (defaults to "jpeg").
     * @param fullPage When false (default), captures the current viewport. When true, captures the entire page.
     * @return Screenshot data as a base64 encoded string.
     */
    suspend fun screenshotB64(
        format: ScreenshotFormat = ScreenshotFormat.JPEG,
        fullPage: Boolean = false,
    ): String {
        if (targetInfo == null) throw IllegalStateException("target is null")

        wait() // update the target's url

        val fmt = when (format) {
            ScreenshotFormat.JPEG -> "jpeg"
            ScreenshotFormat.PNG -> "png"
        }

        return page.captureScreenshot(
            format = fmt,
            captureBeyondViewport = fullPage
        ).data
    }

    /**
     * Saves a screenshot of the page.
     * This is not the same as [Element.saveScreenshot], which saves a screenshot of a single element only.
     *
     * @param filename Uses this as the save path. If "auto" or null, generates a filename based on the page URL and timestamp.
     * @param format "jpeg" or "png" (defaults to "jpeg").
     * @param fullPage When false (default), captures the current viewport. When true, captures the entire page.
     * @return The path/filename of the saved screenshot.
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun saveScreenshot(
        filename: Path? = null,
        format: ScreenshotFormat = ScreenshotFormat.JPEG,
        fullPage: Boolean = false,
    ): String {
        val ext = when (format) {
            ScreenshotFormat.JPEG -> ".jpg"
            ScreenshotFormat.PNG -> ".png"
        }

        val path = if (filename == null) {
            val url = targetInfo?.url ?: throw IllegalStateException("target is null")
            val uri = Url(url)
            val lastPart = uri.fullPath.substringAfterLast('/').substringBefore('?')
            val dtStr = Clock.System.now().toString().replace(":", "-").replace("T", "_").substringBefore('.')
            val candidate = "${uri.host}__${lastPart}_$dtStr"
            Path(candidate + ext)
        } else filename
        //Files.createDirectories(path.parent) // No KMP equivalent for now

        val data = screenshotB64(format = format, fullPage = fullPage)
        val dataBytes = Base64.decode(data)
        SystemFileSystem.sink(path).buffered().use { sink ->
            sink.writeFully(dataBytes)
        }
        return path.toString()
    }

    /**
     * Gets all elements of tag: link, a, img, script, meta.
     *
     * @return List of [Element]s matching the asset tags.
     */
    suspend fun getAllLinkedSources(): List<Element> {
        // get all elements of tag: link, a, img, script, meta
        val allAssets = querySelectorAll("a,link,img,script,meta")
        return allAssets
    }

    /**
     * Convenience function, which returns all links (a, link, img, script, meta).
     *
     * @param absolute If true, tries to build all the links in absolute form instead of "as is" (often relative).
     * @return List of URLs as [String].
     */
    suspend fun getAllUrls(absolute: Boolean = true): List<String> {
        val res = mutableListOf<String>()
        val allAssets = querySelectorAll("a,link,img,script,meta")
        for (asset in allAssets) {
            if (!absolute) {
                res.add(asset["src"] ?: asset["href"] ?: continue)
            } else {
                for (key in asset.attrs) {
                    if (key == "src" || key == "href") {
                        val value = asset[key] ?: continue
                        if ('#' in value) continue
                        if (!listOf("http", "//", "/").any { it in value }) continue
                        val baseUrl = this.targetInfo?.url ?: continue
                        //val absUrl = java.net.URL(java.net.URL(baseUrl), value).toString()
                        //val absUrl = Url(baseUrl).resolve(value).toString()
                        val absUrl = baseUrl + value // TODO: Fix this
                        if (!absUrl.startsWith("http") && !absUrl.startsWith("//") && !absUrl.startsWith("ws")) continue
                        res.add(absUrl)
                    }
                }
            }
        }
        return res
    }

    override fun toString(): String {
        val extra = targetInfo?.url?.takeIf { it.isNotEmpty() }?.let { "[url: $it]" } ?: ""
        return "<${this::class.simpleName} [${targetInfo?.targetId}] [${targetInfo?.type}] $extra>"
    }

}
