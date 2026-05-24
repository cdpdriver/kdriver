package dev.kdriver.core.browser

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.Serialization
import dev.kdriver.cdp.domain.Target
import dev.kdriver.core.connection.DefaultConnection
import dev.kdriver.core.tab.DefaultTab
import dev.kdriver.core.tab.Tab
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.io.files.Path
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Target-registry behavior of [DefaultBrowser] (audit ISSUE-6 / ISSUE-8): page targets are
 * registered as [Tab]s and deduped, and `get()` waits for the target to appear rather than
 * assuming it is already present.
 *
 * Driven through a fake [connection] (a [DefaultConnection] whose `callCommand` is stubbed), so no
 * real browser is required.
 */
class TargetRegistryTest {

    private fun pageInfo(id: String) = Target.TargetInfo(
        targetId = id,
        type = "page",
        title = "",
        url = "about:blank",
        attached = true,
        canAccessOpener = false,
    )

    /** Fake connection: answers `Target.getTargets` with [targetInfos]; everything else is a no-op. */
    private class FakeConnection(
        scope: CoroutineScope,
        var targetInfos: List<Target.TargetInfo>,
    ) : DefaultConnection("ws://stub/devtools/browser/stub", scope) {
        override suspend fun callCommand(method: String, parameter: JsonElement?, mode: CommandMode): JsonElement? =
            when (method) {
                "Target.getTargets" ->
                    Serialization.json.encodeToJsonElement(Target.GetTargetsReturn(targetInfos = targetInfos))
                else -> null
            }
    }

    /** A [DefaultTab] whose CDP calls (e.g. `page.navigate`) are stubbed out. */
    private class FakeTab(
        scope: CoroutineScope,
        targetInfo: Target.TargetInfo,
    ) : DefaultTab("ws://stub/devtools/page/stub", scope, targetInfo) {
        override suspend fun callCommand(method: String, parameter: JsonElement?, mode: CommandMode): JsonElement? =
            when (method) {
                // navigate() decodes a non-null NavigateReturn; everything else is unused here.
                "Page.navigate" -> Serialization.json.parseToJsonElement("""{"frameId":"frame1"}""")
                else -> null
            }
    }

    private class TestBrowser(scope: CoroutineScope) : DefaultBrowser(
        scope,
        Config(browserExecutablePath = Path("/fake/chrome"), host = "127.0.0.1", port = 9222, autoDiscoverTargets = false),
    ) {
        override fun createTab(websocketUrl: String, targetInfo: Target.TargetInfo): Tab = FakeTab(coroutineScope, targetInfo)
    }

    @Test
    fun updateTargets_registersPageTargetAsTab() = runTest {
        val browser = TestBrowser(this)
        browser.connection = FakeConnection(this, listOf(pageInfo("t1")))

        browser.updateTargets()

        assertEquals(1, browser.targets.size)
        assertEquals(1, browser.tabs.size, "a page target must be registered as a Tab (visible to tabs/mainTab)")
        assertTrue(browser.targets.first() is Tab)
    }

    @Test
    fun updateTargets_dedupesByTargetId() = runTest {
        val browser = TestBrowser(this)
        browser.connection = FakeConnection(this, listOf(pageInfo("t1")))

        browser.updateTargets()
        browser.updateTargets() // same target discovered again

        assertEquals(1, browser.targets.size, "repeat discovery must not duplicate a target")
    }

    @Test
    fun get_waitsForPageTabToAppear_insteadOfThrowing() = runTest {
        val browser = TestBrowser(this)
        val connection = FakeConnection(this, emptyList()) // no page tab registered yet
        browser.connection = connection

        val outcome = CompletableDeferred<Result<Tab>>()
        launch {
            outcome.complete(runCatching { browser.get("https://example.com", newTab = false, newWindow = false) })
        }

        // Let get() start and reach its wait (no page tab exists yet).
        runCurrent()

        // The page target now appears (e.g. the initial page registering after start-up).
        connection.targetInfos = listOf(pageInfo("t1"))
        browser.updateTargets()

        advanceUntilIdle()

        val result = outcome.await()
        assertTrue(result.isSuccess, "get() should wait for the page target, not throw NoSuchElementException")
    }

    @Test
    fun awaitInitialPageTab_makesMainTabReadyOncePageAppears() = runTest {
        val browser = TestBrowser(this)
        val connection = FakeConnection(this, emptyList()) // no page target yet
        browser.connection = connection

        // Without waiting, mainTab is null right after discovery — the startup race that flakes
        // `browser.mainTab ?: error(...)`.
        browser.updateTargets()
        assertNull(browser.mainTab)

        // start()'s tail waits for the page; run it concurrently.
        val job = launch { browser.awaitInitialPageTab() }
        runCurrent()
        assertNull(browser.mainTab, "still no page registered while waiting")

        // The initial page now appears.
        connection.targetInfos = listOf(pageInfo("t1"))
        advanceUntilIdle()

        assertNotNull(browser.mainTab, "mainTab must be ready once awaitInitialPageTab returns")
        job.join()
    }
}
