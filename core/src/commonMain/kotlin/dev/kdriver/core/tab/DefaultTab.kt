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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.concurrent.Volatile
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
        // A reconnect opens a new CDP session that would drop this override, so re-apply it then.
        // Keyed so a later setUserAgent replaces (rather than stacks) the restorer.
        registerReconnectRestore("userAgent") {
            network.setUserAgentOverride(
                userAgent = ua,
                acceptLanguage = acceptLanguage,
                platform = platform
            )
        }
    }

    override suspend fun getWindow(): dev.kdriver.cdp.domain.Browser.GetWindowForTargetReturn {
        return browser.getWindowForTarget(targetId)
    }

    override suspend fun getContent(): String {
        val doc = dom.getDocument(depth = -1, pierce = true)
        return dom.getOuterHTML(backendNodeId = doc.root.backendNodeId).outerHTML
    }

    /**
     * Local storage is partitioned per origin, so every call has to name the origin of the page
     * currently loaded rather than its full URL.
     */
    private fun localStorageId(): DOMStorage.StorageId {
        val url = targetInfo?.url ?: error("target is null")
        return DOMStorage.StorageId(
            securityOrigin = Url(url).protocolWithAuthority,
            isLocalStorage = true,
        )
    }

    override suspend fun getLocalStorage(): Map<String, String> {
        // Each entry comes back as a two-element [key, value] array.
        return dOMStorage.getDOMStorageItems(localStorageId()).entries
            .mapNotNull { entry ->
                val key = entry.getOrNull(0) ?: return@mapNotNull null
                key to entry.getOrElse(1) { "" }
            }
            .toMap()
    }

    override suspend fun setLocalStorage(items: Map<String, String>) {
        if (items.isEmpty()) return
        val storageId = localStorageId()
        coroutineScope {
            items.map { (key, value) ->
                async { dOMStorage.setDOMStorageItem(storageId, key, value) }
            }.awaitAll()
        }
    }

    override suspend fun clearLocalStorage() {
        dOMStorage.clear(localStorageId())
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
            settledThen { findElementByText(trimmedText, bestMatch, returnEnclosingElement) }?.let { return it }
            val elapsed = Clock.System.now().toEpochMilliseconds() - startTime
            if (elapsed > timeout) throw TimeoutWaitingForElementException(trimmedText, timeout)
            delay(500)
        }
    }

    /**
     * What [wait] does before each attempt of [find], [select] and [findAll], with one round trip less: the page is
     * left to settle first, then the target info is refreshed while [lookup] runs, instead of before it.
     *
     * The order of the watching and the lookup is the same as with `wait()` followed by the lookup; only the refresh
     * of the target info overlaps the lookup. A failure of either is thrown.
     */
    private suspend fun <T> settledThen(lookup: suspend () -> T): T {
        awaitIdle(null, dev.kdriver.core.browser.Config.Defaults.IDLE_WAIT_TIMEOUT)
        return coroutineScope {
            val refresh = async { updateTarget() }
            val found = lookup()
            refresh.await()
            found
        }
    }

    override suspend fun select(
        selector: String,
        timeout: Long,
    ): Element {
        val trimmedSelector = selector.trim()
        val startTime = Clock.System.now().toEpochMilliseconds()

        while (true) {
            settledThen { querySelector(trimmedSelector) }?.let { return it }
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
            settledThen { findElementsByText(trimmedText) }.takeIf { it.isNotEmpty() }?.let { return it }
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
                // Searching inside the frames reads the document (Element.querySelectorAll), so the whole lookup goes
                // through it: the frames and the page's own matches come from one read.
                for (fr in querySelectorAllInDocument("iframe")) items.addAll(fr.querySelectorAll(trimmedSelector))
                items.addAll(querySelectorAllInDocument(trimmedSelector))
            } else {
                items.addAll(querySelectorAll(trimmedSelector))
            }
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
    ): List<Element> =
        if (node == null) querySelectorAllInPage(selector)
        else bound(node).let { querySelectorAllInternal(selector, it, retried = false, rootOf(it)) }

    // querySelectorAll on the whole document read (kept while the page has not changed): the frames of
    // selectAll(includeFrames = true), which need the document to be searched.
    private suspend fun querySelectorAllInDocument(selector: String): List<Element> =
        onDocument { doc -> querySelectorAllInternal(selector, null, retried = false, doc) }

    /**
     * `document.querySelectorAll(selector)` run by the page, each match then described with its subtree: see
     * [Tab.querySelector]. One round trip when nothing matches (the page answers `null`), otherwise three: the
     * evaluation, the array's own properties, and the descriptions, sent together.
     */
    private suspend fun querySelectorAllInPage(selector: String): List<Element> {
        val array = evaluateLookup(
            "(() => { const all = Array.from(document.querySelectorAll(${jsStringLiteral(selector)}));" +
                " return all.length ? all : null; })()",
            protocolMethod = "DOM.querySelectorAll",
        ) ?: return emptyList()
        val arrayId = array.objectId!!
        // The array is a cleanup to release on every path, cancellation included (NonCancellable); while the matches
        // are described, in parallel with them, at no extra wait. Gone with the page, it needs no release.
        var release: Deferred<Unit>? = null
        var gone = false
        try {
            val properties = unlessGone { runtime.getProperties(objectId = arrayId, ownProperties = true).result }
            if (properties == null) {
                gone = true
                return emptyList()
            }
            val matches = properties
                .mapNotNull { property -> property.name.toIntOrNull()?.let { index -> index to property.value } }
                .sortedBy { it.first }
                .mapNotNull { (_, value) -> value?.takeIf { it.objectId != null } }
            return coroutineScope {
                release = async(NonCancellable) { releaseQuietly(arrayId) }
                val described = matches.map { match -> async { describeMatch(match) } }.awaitAll()
                // A match gone before it was described means the page navigated during the lookup: no match at all,
                // as for one lookup, rather than a part of the old page's matches.
                if (null in described) emptyList() else described.filterNotNull()
            }
        } finally {
            val pending = release
            withContext(NonCancellable) {
                if (pending != null) pending.await() else if (!gone) releaseQuietly(arrayId)
            }
        }
    }

    // Releases a remote object the lookup no longer needs. A cleanup, always run under NonCancellable, so a
    // CancellationException here is the command's own timeout, never the caller's cancellation: no failure of it ever
    // fails the lookup (the page drops the object at its next navigation anyway).
    private suspend fun releaseQuietly(objectId: String) {
        try {
            runtime.releaseObject(objectId)
        } catch (_: Exception) {
            // Already gone, timed out, or the connection is closing: nothing to clean up.
        }
    }

    /**
     * `document.querySelector(selector)` run by the page, the match then described with its subtree: see
     * [Tab.querySelector]. Two round trips when something matches, one otherwise.
     */
    private suspend fun querySelectorInPage(selector: String): Element? {
        val match = evaluateLookup(
            "document.querySelector(${jsStringLiteral(selector)})",
            protocolMethod = "DOM.querySelector",
        ) ?: return null
        return describeMatch(match)
    }

    /**
     * Evaluates [expression] in the page and returns the object it yields, or null when it yields no object (`null`).
     *
     * An invalid selector makes the page throw a `SyntaxError`: it is thrown as the error the browser gives
     * [protocolMethod] (`DOM.querySelector` or `DOM.querySelectorAll`) for that selector, a [CDPException] with code
     * -32000 and the message "DOM Error while querying", after disabling the DOM agent, as a lookup on the document
     * did. Any other exception of the page (a page that replaced `document.querySelector`) is thrown as an
     * [EvaluateException].
     */
    private suspend fun evaluateLookup(expression: String, protocolMethod: String): Runtime.RemoteObject? {
        val evaluated = unlessGone { runtime.evaluate(expression = expression) } ?: return null
        evaluated.exceptionDetails?.let { details ->
            if (details.isSyntaxError()) {
                disableDomAgent()
                throw CDPException(protocolMethod, INVALID_SELECTOR_CODE, INVALID_SELECTOR_MESSAGE, null)
            }
            throw EvaluateException(details)
        }
        return evaluated.result.takeIf { it.objectId != null }
    }

    // The `SyntaxError` (a DOMException) the page throws for a selector it cannot parse.
    private fun Runtime.ExceptionDetails.isSyntaxError(): Boolean =
        exception?.description?.startsWith("SyntaxError") == true

    /**
     * The element of [match], a remote object the page returned: its node and subtree (`DOM.describeNode`, depth -1,
     * piercing), with [match] kept so its first call needs no `DOM.resolveNode`. Null when the browser no longer knows
     * the object (the page navigated in between): the match is gone, and `select` tries again as for no match.
     */
    private suspend fun describeMatch(match: Runtime.RemoteObject): Element? {
        val node = unlessGone { dom.describeNode(objectId = match.objectId, depth = -1, pierce = true).node }
            ?: return null
        return DefaultElement(this, node).also { it.keepRemoteObject(match) }
    }

    /**
     * Runs [command] of an in-page lookup, or returns null when the browser no longer knows what it refers to: the
     * object or the execution context was dropped by a navigation in between, so the lookup found nothing and `select`
     * tries again. Any other protocol error is thrown.
     */
    private suspend fun <T> unlessGone(command: suspend () -> T): T? =
        try {
            command()
        } catch (e: CDPException) {
            if (GONE.any { e.originalMessage.contains(it, ignoreCase = true) }) null else throw e
        }

    // A JavaScript string literal for [value]: its JSON encoding, which escapes quotes, backslashes and control
    // characters, so the value can only ever be read as one string.
    private fun jsStringLiteral(value: String): String =
        Serialization.json.encodeToString(String.serializer(), value)

    // `retried` carries the "already retried once" state across the recursive call. It must be a
    // parameter, not a local: a local would be reset on every recursion and the guard would never
    // trip, recursing without bound on a persistent "could not find node" (ISSUE-7).
    private suspend fun querySelectorAllInternal(
        selector: String,
        node: NodeOrElement?,
        retried: Boolean,
        doc: DOM.Node,
    ): List<Element> {
        val nodeIds = try {
            dom.querySelectorAll(doc.nodeId, selector)
        } catch (e: Exception) {
            if (node != null && e.message?.contains("could not find node", ignoreCase = true) == true) {
                if (retried) return emptyList()
                if (node is NodeOrElement.WrappedElement) node.element.update()
                return querySelectorAllInternal(selector, node, retried = true, rootOf(node))
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
        val trimmedSelector = selector.trim()
        return if (node == null) {
            querySelectorInPage(trimmedSelector)
        } else {
            bound(node).let { querySelectorInternal(trimmedSelector, it, retried = false, rootOf(it)) }
        }
    }

    /**
     * [node] ready for a lookup under it, which queries it by node id. A node found in the page has none yet (see
     * [Tab.querySelector]): it is first bound to the document, an element filled from it
     * ([DefaultElement.bindToDocument], [Element.update] for another implementation), a bare node replaced by the
     * document's node of the same backend node id.
     */
    private suspend fun bound(node: NodeOrElement): NodeOrElement {
        if (node.node.nodeId != 0) return node
        return when (node) {
            is NodeOrElement.WrappedElement -> {
                val element = node.element
                if (element is DefaultElement) element.bindToDocument() else element.update()
                node
            }

            is NodeOrElement.WrappedNode -> currentDocument().first
                .filterRecurse { it.backendNodeId == node.node.backendNodeId }
                ?.let { NodeOrElement.WrappedNode(it) } ?: node
        }
    }

    // The node a lookup under [node] queries: the document of a frame, the node itself otherwise.
    private fun rootOf(node: NodeOrElement): DOM.Node =
        if (node.node.nodeName == "IFRAME") node.node.contentDocument ?: node.node else node.node

    // A lookup under [node] (the page runs the lookups on the whole document). See querySelectorAllInternal:
    // `retried` must be a parameter so the retry-once guard survives the recursive call (ISSUE-7).
    private suspend fun querySelectorInternal(
        selector: String,
        node: NodeOrElement,
        retried: Boolean,
        doc: DOM.Node,
    ): Element? {
        val nodeId = try {
            dom.querySelector(doc.nodeId, selector)
        } catch (e: Exception) {
            if (e.message?.contains("could not find node", ignoreCase = true) == true) {
                if (retried) return null
                if (node is NodeOrElement.WrappedElement) node.element.update()
                return querySelectorInternal(selector, node, retried = true, rootOf(node))
            } else {
                disableDomAgent()
                throw e
            }
        }.nodeId

        if (nodeId == null) return null

        val foundNode = doc.filterRecurse { it.nodeId == nodeId }
        return foundNode?.let { DefaultElement(this, it, doc) }
    }

    /**
     * The last whole document read for a lookup (`DOM.getDocument(depth = -1, pierce = true)`), and the
     * [documentEpoch] it still matches the page at.
     */
    private class DocumentSnapshot(val root: DOM.Node, val epoch: Long)

    @Volatile
    private var documentSnapshot: DocumentSnapshot? = null

    /**
     * The whole document, as `DOM.getDocument(depth = -1, pierce = true)` returns it.
     *
     * Read again only when the page may have changed since the last read: once the document was read, the browser
     * reports every change to it as a `DOM.*` event, and [documentEpoch] counts those, the commands that make the
     * browser forget its node ids, and reconnects. While none happened, a new read would return the same tree.
     *
     * @return the root of the document, and whether it is the copy kept from an earlier read.
     */
    internal suspend fun currentDocument(): Pair<DOM.Node, Boolean> {
        documentSnapshot?.takeIf { it.epoch == documentEpoch }?.let { return it.root to true }
        return readDocument() to false
    }

    private suspend fun readDocument(): DOM.Node {
        val before = documentEpoch
        val root = dom.getDocument(-1, true).root
        // The read itself counts once. Anything more (an event, another read, a reconnect) means the page or its ids
        // may have moved while the read was in flight: this copy serves the current lookup only.
        val after = documentEpoch
        documentSnapshot = if (after == before + 1) DocumentSnapshot(root, after) else null
        return root
    }

    /**
     * Runs [lookup] on the whole document ([currentDocument]).
     *
     * A lookup on a kept copy is checked afterwards: when the browser reported a change while it ran, its answer may
     * name nodes the copy does not have, so the document is read again and [lookup] runs once more, as it would have
     * on a fresh read. A lookup on a fresh read is not repeated.
     */
    private suspend fun <T> onDocument(lookup: suspend (DOM.Node) -> T): T {
        val (root, kept) = currentDocument()
        val epoch = documentEpoch
        val result = lookup(root)
        return if (kept && documentEpoch != epoch) lookup(readDocument()) else result
    }

    override suspend fun findElementsByText(
        text: String,
        tagHint: String?,
    ): List<Element> {
        val trimmedText = text.trim()
        return onDocument { doc -> findElementsByTextIn(doc, trimmedText) }
    }

    private suspend fun findElementsByTextIn(doc: DOM.Node, trimmedText: String): List<Element> {
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
                } catch (e: CancellationException) {
                    throw e
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
            } catch (e: CancellationException) {
                throw e
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
        urlPattern: Regex?,
        block: suspend RequestExpectation.() -> T,
    ): T {
        return BaseRequestExpectation(this, urlPattern).use(block)
    }

    override suspend fun <T> expectBatch(
        urlPatterns: List<Regex?>,
        block: suspend BatchRequestExpectation.() -> T,
    ): T {
        return BaseBatchRequestExpectation(this, urlPatterns).use(block)
    }

    override suspend fun <T> intercept(
        urlPattern: String?,
        requestStage: Fetch.RequestStage?,
        resourceType: Network.ResourceType?,
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

/**
 * Error texts of the browser for an object, a node or an execution context it no longer knows (a navigation dropped
 * it): "Could not find object with given id", "No node with given id found", "Cannot find context with specified id",
 * "Execution context was destroyed". Not "Inspected target navigated or closed", also sent for a closed tab, which is an
 * error and not a lookup to try again.
 */
private val GONE = listOf(
    "could not find object",
    "could not find node",
    "no node with given id",
    "cannot find context",
    "context was destroyed",
)

/** The code and message Chrome answers `DOM.querySelector(All)` with for a selector it cannot parse. */
private const val INVALID_SELECTOR_CODE = -32000
private const val INVALID_SELECTOR_MESSAGE = "DOM Error while querying"

