package dev.kdriver.core.tab

import dev.kdriver.cdp.CDPException
import dev.kdriver.cdp.Serialization
import dev.kdriver.cdp.domain.*
import dev.kdriver.cdp.domain.Input
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.connection.DefaultConnection
import dev.kdriver.core.dom.DefaultElement
import dev.kdriver.core.dom.Element
import dev.kdriver.core.dom.NodeOrElement
import dev.kdriver.core.dom.filterRecurse
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.exceptions.TimeoutWaitingForReadyStateException
import dev.kdriver.core.network.*
import io.ktor.http.*
import io.ktor.util.logging.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * Represents a browser tab, which is a connection to a specific target in the browser.
 *
 * This class provides methods to interact with the tab, such as navigating to URLs,
 * managing history, evaluating JavaScript expressions, and manipulating the DOM.
 */
open class DefaultTab(
    websocketUrl: String,
    messageListeningScope: CoroutineScope,
    targetInfo: Target.TargetInfo,
    owner: Browser? = null,
) : DefaultConnection(websocketUrl, messageListeningScope, targetInfo, owner), Tab {

    private val logger = KtorSimpleLogger("Tab")

    // Track last mouse position for natural trajectories (P2 - Anti-detection)
    // Each tab maintains its own mouse position to prevent concurrent operations from interfering
    override var lastMouseX: Double? = null
    override var lastMouseY: Double? = null

    override suspend fun get(
        url: String,
        newTab: Boolean,
        newWindow: Boolean,
    ): Tab {
        val browser = owner ?: error(
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

    override suspend fun back() {
        runtime.evaluate("window.history.back()")
    }

    override suspend fun forward() {
        runtime.evaluate("window.history.forward()")
    }

    override suspend fun reload(
        ignoreCache: Boolean,
        scriptToEvaluateOnLoad: String?,
    ) {
        page.reload(
            ignoreCache = ignoreCache,
            scriptToEvaluateOnLoad = scriptToEvaluateOnLoad
        )
    }

    override suspend fun rawEvaluate(
        expression: String,
        awaitPromise: Boolean,
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

    override suspend fun setUserAgent(
        userAgent: String?,
        acceptLanguage: String?,
        platform: String?,
    ) {
        val ua = userAgent
            ?: evaluate<String>("navigator.userAgent")
            ?: error("Could not read existing user agent from navigator object")

        network.setUserAgentOverride(
            userAgent = ua,
            acceptLanguage = acceptLanguage,
            platform = platform
        )
    }

    override suspend fun getWindow(): dev.kdriver.cdp.domain.Browser.GetWindowForTargetReturn {
        return browser.getWindowForTarget(targetId)
    }

    override suspend fun getContent(): String {
        val doc = dom.getDocument(depth = -1, pierce = true)
        return dom.getOuterHTML(backendNodeId = doc.root.backendNodeId).outerHTML
    }

    override suspend fun activate() {
        val targetId = targetInfo?.targetId
            ?: throw IllegalArgumentException("target is null")
        target.activateTarget(targetId)
    }

    override suspend fun bringToFront() {
        activate()
    }

    override suspend fun maximize() {
        setWindowState(state = "maximize")
    }

    override suspend fun minimize() {
        setWindowState(state = "minimize")
    }

    override suspend fun fullscreen() {
        setWindowState(state = "fullscreen")
    }

    override suspend fun medimize() {
        setWindowState(state = "normal")
    }

    override suspend fun setWindowState(
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        state: String,
    ) {
        val availableStates = listOf("minimized", "maximized", "fullscreen", "normal")
        val (windowId, _) = getWindow()

        val stateName = availableStates.find { stateName ->
            state.lowercase().all { it in stateName }
        } ?: error(
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

    override suspend fun scrollDown(amount: Int, speed: Int) {
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

    override suspend fun scrollUp(amount: Int, speed: Int) {
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

    override suspend fun scrollTo(scrollX: Double, scrollY: Double, speed: Int?) {
        if (scrollX == 0.0 && scrollY == 0.0) {
            return
        }

        // Get current viewport dimensions for scroll origin
        val viewportJson = rawEvaluate(
            """
            ({
                width: window.innerWidth,
                height: window.innerHeight
            })
            """.trimIndent()
        )!!
        val viewportData = Serialization.json.decodeFromJsonElement<dev.kdriver.core.dom.ViewportData>(viewportJson)

        val originX = viewportData.width / 2
        val originY = viewportData.height / 2

        // Use provided speed or add natural variation (P3 - Anti-detection)
        val scrollSpeed = speed ?: kotlin.random.Random.nextInt(600, 1200)

        // Use negative distances because CDP's synthesizeScrollGesture uses inverted Y-axis
        // (positive yDistance scrolls UP, but we want positive scrollY to scroll DOWN)
        input.synthesizeScrollGesture(
            x = originX,
            y = originY,
            xDistance = -scrollX, // Negative because positive xDistance scrolls left
            yDistance = -scrollY, // Negative because positive yDistance scrolls up
            speed = scrollSpeed,
            preventFling = true,
            gestureSourceType = Input.GestureSourceType.MOUSE
        )

        // Add a small delay for the scroll animation to complete (P3 - Anti-detection)
        // Calculate duration based on distance and speed
        val distance = kotlin.math.sqrt(scrollX * scrollX + scrollY * scrollY)
        val duration = (distance / scrollSpeed * 1000).toLong() // Convert to milliseconds
        sleep(duration + kotlin.random.Random.nextLong(50, 150))
    }

    override suspend fun waitForReadyState(
        until: ReadyState,
        timeout: Long,
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

    override suspend fun find(
        text: String,
        bestMatch: Boolean,
        returnEnclosingElement: Boolean,
        timeout: Long,
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

    override suspend fun select(
        selector: String,
        timeout: Long,
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

    override suspend fun findAll(
        text: String,
        timeout: Long,
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

    override suspend fun selectAll(
        selector: String,
        timeout: Long,
        includeFrames: Boolean,
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

    override suspend fun xpath(
        xpath: String,
        timeout: Long,
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

    override suspend fun querySelectorAll(
        selector: String,
        node: NodeOrElement?,
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
            val innerNode = doc.filterRecurse { it.nodeId == nid }
            if (innerNode != null) {
                val elem = DefaultElement(this, innerNode, doc)
                items.add(elem)
            }
        }
        return items
    }

    override suspend fun querySelector(
        selector: String,
        node: NodeOrElement?,
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

        val foundNode = doc.filterRecurse { it.nodeId == nodeId }
        return foundNode?.let { DefaultElement(this, it, doc) }
    }

    override suspend fun findElementsByText(
        text: String,
        tagHint: String?,
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
            val node = doc.filterRecurse { it.nodeId == nid }
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
                val elem = DefaultElement(this, node, doc)
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
        val iframes = doc.filterRecurse { it.nodeName == "IFRAME" }
        if (iframes != null) {
            val iframeElems = listOf(DefaultElement(this, iframes, iframes.contentDocument ?: doc))
            for (iframeElem in iframeElems) {
                val iframeTextNodes = iframeElem.node.filterRecurse { n ->
                    n.nodeType == 3 && n.nodeValue.contains(trimmedText, ignoreCase = true)
                }
                if (iframeTextNodes != null) {
                    val textElem = DefaultElement(this, iframeTextNodes, iframeElem.node)
                    items.add(textElem.parent ?: textElem)
                }
            }
        }

        return items
    }

    override suspend fun findElementByText(
        text: String,
        bestMatch: Boolean,
        returnEnclosingElement: Boolean,
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

    override suspend fun disableDomAgent() {
        try {
            dom.disable()
        } catch (_: CDPException) {
            // The DOM.disable can throw an exception if not enabled,
            // but if it's already disabled, that's not a "real" error.
            logger.debug("Ignoring DOM.disable exception")
        }
    }

    override suspend fun mouseMove(x: Double, y: Double, steps: Int, flash: Boolean) {
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

    override suspend fun mouseClick(
        x: Double,
        y: Double,
        button: Input.MouseButton,
        buttons: Int,
        modifiers: Int,
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

    override suspend fun <T> expect(
        urlPattern: Regex,
        block: suspend RequestExpectation.() -> T,
    ): T {
        return BaseRequestExpectation(this, urlPattern).use(block)
    }

    override suspend fun <T> expectBatch(
        urlPatterns: List<Regex>,
        block: suspend BatchRequestExpectation.() -> T,
    ): T {
        return BaseBatchRequestExpectation(this, urlPatterns).use(block)
    }

    override suspend fun <T> intercept(
        urlPattern: String,
        requestStage: Fetch.RequestStage,
        resourceType: Network.ResourceType,
        block: suspend FetchInterception.() -> T,
    ): T {
        return BaseFetchInterception(this, urlPattern, requestStage, resourceType).use(block)
    }

    override suspend fun screenshotB64(
        format: ScreenshotFormat,
        fullPage: Boolean,
    ): String {
        if (targetInfo == null) error("target is null")

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

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun saveScreenshot(
        filename: Path?,
        format: ScreenshotFormat,
        fullPage: Boolean,
    ): String {
        val ext = when (format) {
            ScreenshotFormat.JPEG -> ".jpg"
            ScreenshotFormat.PNG -> ".png"
        }

        val path = if (filename == null) {
            val url = targetInfo?.url ?: error("target is null")
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

    override suspend fun getAllLinkedSources(): List<Element> {
        // get all elements of tag: link, a, img, script, meta
        val allAssets = querySelectorAll("a,link,img,script,meta")
        return allAssets
    }

    override suspend fun getAllUrls(absolute: Boolean): List<String> {
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
