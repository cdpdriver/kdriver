package dev.kdriver.core.connection

import dev.kdriver.cdp.CommandMode
import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.exceptions.CommandTimeoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

/**
 * End-to-end reconnection behaviour against a real Chrome, with a [TcpProxy] in the middle so the
 * test can cut or freeze the socket at will.
 *
 * A real browser is launched (the "host"), and the test opens its own [DefaultConnection] to one of
 * its page targets *through the proxy* — the per-target WebSocket URL is built from host:port, so it
 * always goes through the proxy regardless of what Chrome advertises for the browser endpoint.
 */
class ReconnectionIntegrationTest {

    private class ProxiedConnection(
        private val wsUrl: String,
        scope: CoroutineScope,
    ) : DefaultConnection(wsUrl, scope) {
        override fun createTransport(): WebSocketTransport = KtorWebSocketTransport(wsUrl)
    }

    private fun evalParam(expression: String): JsonObject = buildJsonObject {
        put("expression", expression)
        put("returnByValue", true)
    }

    private suspend fun DefaultConnection.evaluate(expression: String): Int {
        val result = callCommand("Runtime.evaluate", evalParam(expression), CommandMode.ONE_SHOT)
        return result!!.jsonObject["result"]!!.jsonObject["value"]!!.jsonPrimitive.int
    }

    /**
     * Evaluates, retrying through a transient reconnect. The single command *in flight* when the
     * socket is cut may itself be the casualty (its send lands on the dying socket, then the reply
     * never comes) — by design we never resend an awaited command. The connection self-heals, so the
     * next attempt reconnects and succeeds, exactly as kdriver's high-level retry loops rely on.
     */
    private suspend fun DefaultConnection.evaluateRecovering(expression: String, timeoutMs: Long = 15_000): Int =
        withTimeout(timeoutMs) {
            while (true) {
                val result = runCatching { evaluate(expression) }
                if (result.isSuccess) return@withTimeout result.getOrThrow()
            }
            error("unreachable")
        }

    @Test
    fun reconnects_afterHardCut_throughProxy() = runBlocking {
        val host = createBrowser(this, headless = true, sandbox = false)
        val proxy = TcpProxy("127.0.0.1", host.config.port!!, this).also { it.start() }
        val targetId = assertNotNull(host.mainTab?.targetId, "host browser should have a page target")
        val wsUrl = "ws://127.0.0.1:${proxy.port}/devtools/page/$targetId"
        val conn = ProxiedConnection(wsUrl, this)

        try {
            // Sanity: the proxied connection works end-to-end.
            assertEquals(2, conn.evaluate("1 + 1"))

            // Hard cut: the socket is closed under the connection.
            proxy.severAll()

            // The connection reconnects through the proxy on its own and commands succeed again.
            assertEquals(4, conn.evaluateRecovering("2 + 2"))
        } finally {
            conn.close()
            proxy.stop()
            host.stop()
        }
    }

    @Test
    fun runsRegisteredRestore_afterHardCut_throughProxy() = runBlocking {
        val host = createBrowser(this, headless = true, sandbox = false)
        val proxy = TcpProxy("127.0.0.1", host.config.port!!, this).also { it.start() }
        val targetId = assertNotNull(host.mainTab?.targetId, "host browser should have a page target")
        val wsUrl = "ws://127.0.0.1:${proxy.port}/devtools/page/$targetId"
        val conn = ProxiedConnection(wsUrl, this)
        val restoreCount = AtomicInteger(0)

        try {
            assertEquals(2, conn.evaluate("1 + 1"))

            // A feature (e.g. FetchInterception) registers a restorer to re-establish its session
            // state after a reconnect. Here it just counts its runs.
            conn.registerReconnectRestore("probe") { restoreCount.incrementAndGet() }

            proxy.severAll()

            // The connection reconnects once; the restorer must run exactly once as part of that
            // single reconnect, no matter how many commands drive the recovery.
            assertEquals(4, conn.evaluateRecovering("2 + 2"))
            assertEquals(9, conn.evaluateRecovering("3 + 3 + 3"))
            assertEquals(1, restoreCount.get(), "the registered restorer must run once per reconnect")
        } finally {
            conn.close()
            proxy.stop()
            host.stop()
        }
    }

    @Test
    fun halfOpenSocket_isBoundedByCommandTimeout_andRecovers() = runBlocking {
        // Short command timeout so a half-open socket fails fast instead of hanging for 30s.
        val host = createBrowser(this) {
            headless = true
            sandbox = false
            commandTimeout = 3_000
        }
        val proxy = TcpProxy("127.0.0.1", host.config.port!!, this).also { it.start() }
        val targetId = assertNotNull(host.mainTab?.targetId, "host browser should have a page target")
        val wsUrl = "ws://127.0.0.1:${proxy.port}/devtools/page/$targetId"
        // owner = host so the connection picks up the short commandTimeout from its config.
        val conn = ProxiedConnection(wsUrl, this).also { it.owner = host }

        try {
            assertEquals(2, conn.evaluate("1 + 1"))

            // Half-open: sockets stay open but no application bytes flow, so the reply never comes
            // and the socket never closes. This is the case keep-alive pings would be needed to
            // detect; for now it is merely *bounded* by the command timeout rather than hanging.
            proxy.freeze()
            val start = System.currentTimeMillis()
            val error = runCatching { conn.evaluate("2 + 2") }.exceptionOrNull()
            val elapsed = System.currentTimeMillis() - start
            assertIs<CommandTimeoutException>(error, "a half-open socket must fail with a command timeout, not hang")
            assertTrue(elapsed in 3_000..15_000, "should fail around the 3s command timeout (was ${elapsed}ms)")

            // Once traffic flows again the same session is healthy, so commands resume working.
            proxy.unfreeze()
            assertEquals(9, withTimeout(15_000) { conn.evaluate("3 + 3 + 3") })
        } finally {
            conn.close()
            proxy.stop()
            host.stop()
        }
    }
}
