package dev.kdriver.core.browser

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.Serialization
import dev.kdriver.cdp.domain.Target
import dev.kdriver.core.connection.DefaultConnection
import dev.kdriver.core.tab.DefaultTab
import dev.kdriver.core.tab.Tab
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * [Browser.detach] lets go of a browser without closing it, unlike [Browser.stop]. Driven through
 * fake connections, so no real browser is required.
 */
class DetachTest {

    private fun pageInfo(id: String) = Target.TargetInfo(
        targetId = id,
        type = "page",
        title = "",
        url = "about:blank",
        attached = true,
        canAccessOpener = false,
    )

    /** Records every CDP method sent and whether it was closed. */
    private class RecordingConnection(
        scope: CoroutineScope,
        var targetInfos: List<Target.TargetInfo>,
    ) : DefaultConnection("ws://stub/devtools/browser/stub", scope) {
        val methods = mutableListOf<String>()
        var closed = false
        override suspend fun callCommand(method: String, parameter: JsonElement?, mode: CommandMode): JsonElement? {
            methods += method
            return when (method) {
                "Target.getTargets" ->
                    Serialization.json.encodeToJsonElement(Target.GetTargetsReturn(targetInfos = targetInfos))
                else -> null
            }
        }

        override suspend fun close() {
            closed = true
        }
    }

    private class RecordingTab(
        scope: CoroutineScope,
        targetInfo: Target.TargetInfo,
        private val closeGate: CompletableDeferred<Unit>?,
    ) : DefaultTab("ws://stub/devtools/page/stub", scope, targetInfo) {
        val methods = mutableListOf<String>()
        var closed = false
        override suspend fun callCommand(method: String, parameter: JsonElement?, mode: CommandMode): JsonElement? {
            methods += method
            return null
        }

        override suspend fun close() {
            closeGate?.await()
            closed = true
        }
    }

    private class TestBrowser(
        scope: CoroutineScope,
        private val closeGate: CompletableDeferred<Unit>? = null,
    ) : DefaultBrowser(
        scope,
        Config(browserExecutablePath = Path("/fake/chrome"), host = "127.0.0.1", port = 9222, autoDiscoverTargets = false),
    ) {
        val createdTabs = mutableListOf<RecordingTab>()
        override fun createTab(websocketUrl: String, targetInfo: Target.TargetInfo): Tab =
            RecordingTab(coroutineScope, targetInfo, closeGate).also { createdTabs += it }
    }

    /** A scope of its own, so the browser cancelling it does not cancel the test. */
    private fun TestScope.browserScope() = CoroutineScope(coroutineContext + SupervisorJob(coroutineContext[Job]))

    @Test
    fun detach_closesConnectionsWithoutClosingTheBrowser() = runTest {
        val scope = browserScope()
        val browser = TestBrowser(scope)
        val connection = RecordingConnection(scope, listOf(pageInfo("t1")))
        browser.connection = connection
        browser.updateTargets()
        val tab = browser.createdTabs.single()

        browser.detach()

        assertFalse("Browser.close" in connection.methods, "detach must not ask the browser to close")
        assertFalse("Page.close" in tab.methods, "detach must not close the pages")
        assertFalse("Target.closeTarget" in connection.methods, "detach must not close the pages")
        assertTrue(connection.closed, "the browser connection must be closed")
        assertTrue(tab.closed, "the page connections must be closed")
        assertNull(browser.connection)
        assertEquals(emptyList(), browser.targets)
        assertFalse(scope.isActive, "the browser's coroutines must be released")
    }

    @Test
    fun detach_ignoresTargetsDiscoveredWhileItRuns() = runTest {
        val scope = browserScope()
        val closeGate = CompletableDeferred<Unit>()
        val browser = TestBrowser(scope, closeGate)
        val connection = RecordingConnection(scope, listOf(pageInfo("t1")))
        browser.connection = connection
        browser.updateTargets()

        // detach() is now busy closing the first page's connection…
        val detaching = launch { browser.detach() }
        runCurrent()
        // …when another page shows up (a target event or a poll racing the detach).
        connection.targetInfos = listOf(pageInfo("t1"), pageInfo("t2"))
        browser.updateTargets()
        closeGate.complete(Unit)
        detaching.join()

        assertEquals(emptyList(), browser.targets, "nothing may be registered once detaching started")
        assertEquals(1, browser.createdTabs.size, "no connection may be opened once detaching started")
    }

    @Test
    fun stop_stillClosesTheBrowser() = runTest {
        val scope = browserScope()
        val browser = TestBrowser(scope)
        val connection = RecordingConnection(scope, emptyList())
        browser.connection = connection

        browser.stop()

        assertTrue("Browser.close" in connection.methods, "stop keeps asking the browser to close")
    }

}
