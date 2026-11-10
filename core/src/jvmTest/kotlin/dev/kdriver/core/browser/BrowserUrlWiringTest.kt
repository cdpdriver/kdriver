package dev.kdriver.core.browser

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.Serialization
import dev.kdriver.cdp.domain.Target
import dev.kdriver.core.connection.DefaultConnection
import dev.kdriver.core.tab.Tab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.io.files.Path
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.io.InputStream
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * [Config.browserUrl] and [Config.connectionHeaders] reach the connections [DefaultBrowser] opens.
 * Driven without a real browser.
 */
class BrowserUrlWiringTest {

    private val chrome = Path("/fake/chrome")

    private fun pageInfo(id: String) = Target.TargetInfo(
        targetId = id,
        type = "page",
        title = "",
        url = "about:blank",
        attached = true,
        canAccessOpener = false,
    )

    private class FakeConnection(
        scope: CoroutineScope,
        val targetInfos: List<Target.TargetInfo>,
    ) : DefaultConnection("ws://stub/devtools/browser/stub", scope) {
        override suspend fun callCommand(method: String, parameter: JsonElement?, mode: CommandMode): JsonElement? =
            when (method) {
                "Target.getTargets" ->
                    Serialization.json.encodeToJsonElement(Target.GetTargetsReturn(targetInfos = targetInfos))
                else -> null
            }
    }

    /** Records the WebSocket URL each tab would be opened on, without opening it. */
    private class RecordingBrowser(scope: CoroutineScope, config: Config) : DefaultBrowser(scope, config) {
        val tabUrls = mutableListOf<String>()
        override fun createTab(websocketUrl: String, targetInfo: Target.TargetInfo): Tab {
            tabUrls += websocketUrl
            return super.createTab(websocketUrl, targetInfo)
        }
    }

    @Test
    fun targets_areOpenedOnTheBrowserUrl() = runTest {
        val browser = RecordingBrowser(
            this,
            Config(
                browserExecutablePath = chrome,
                browserUrl = "https://example.com/team/one",
                autoDiscoverTargets = false,
            ),
        )
        browser.connection = FakeConnection(this, listOf(pageInfo("t1")))

        browser.updateTargets()

        assertEquals(listOf("wss://example.com/team/one/devtools/page/t1"), browser.tabUrls)
    }

    @Test
    fun targets_withoutBrowserUrl_stayOnHostAndPort() = runTest {
        val browser = RecordingBrowser(
            this,
            Config(browserExecutablePath = chrome, host = "127.0.0.1", port = 9222, autoDiscoverTargets = false),
        )
        browser.connection = FakeConnection(this, listOf(pageInfo("t1")))

        browser.updateTargets()

        assertEquals(listOf("ws://127.0.0.1:9222/devtools/page/t1"), browser.tabUrls)
    }

    @Test
    fun connections_sendTheConnectionHeadersInTheirHandshake() = runBlocking {
        ServerSocket(0).use { server ->
            val browser = DefaultBrowser(
                this,
                Config(browserExecutablePath = chrome, connectionHeaders = mapOf("Authorization" to "Bearer secret")),
            )
            val connection = DefaultConnection(
                "ws://127.0.0.1:${server.localPort}/devtools/page/t1",
                this,
                owner = browser,
            )
            val requestHead = async(Dispatchers.IO) {
                server.accept().use { readRequestHead(it.getInputStream()) }
            }
            val command = launch {
                runCatching { connection.callCommand("Runtime.enable", null, CommandMode.ONE_SHOT) }
            }
            try {
                val lines = withTimeout(10_000) { requestHead.await() }.lines()
                assertTrue("Authorization: Bearer secret" in lines, "missing Authorization header in:\n$lines")
            } finally {
                command.cancel()
                withContext(Dispatchers.IO) { runCatching { connection.close() } }
            }
        }
    }

    private fun readRequestHead(input: InputStream): String {
        val bytes = StringBuilder()
        while (!bytes.endsWith("\r\n\r\n")) {
            val b = input.read()
            if (b < 0) break
            bytes.append(b.toChar())
        }
        return bytes.toString().trimEnd().replace("\r\n", "\n")
    }

}
