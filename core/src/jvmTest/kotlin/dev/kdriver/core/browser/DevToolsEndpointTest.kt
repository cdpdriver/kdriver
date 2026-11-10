package dev.kdriver.core.browser

import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * How the DevTools HTTP and WebSocket addresses are derived from a [Config]: either the historical
 * `host` + `port` pair, or a [Config.browserUrl] for a browser reached through a reverse proxy.
 */
class DevToolsEndpointTest {

    private val chrome = Path("/usr/bin/chromium")

    @Test
    fun hostAndPort_keepTheHistoricalAddresses() {
        val endpoint = DevToolsEndpoint.from(Config(browserExecutablePath = chrome, host = "127.0.0.1", port = 9222))

        assertEquals("http://127.0.0.1:9222/", endpoint.httpBaseUrl)
        assertEquals("ws://127.0.0.1:9222/devtools/page/t1", endpoint.webSocketUrl("/devtools/page/t1"))
        // Without a browserUrl the address the browser advertises is used untouched, as before.
        assertEquals(
            "ws://127.0.0.1:9222/devtools/browser/abc",
            endpoint.rewriteWebSocketDebuggerUrl("ws://127.0.0.1:9222/devtools/browser/abc"),
        )
    }

    @Test
    fun httpsBrowserUrl_withPathPrefix_mapsToWssUnderTheSamePrefix() {
        val endpoint = DevToolsEndpoint.from(
            Config(browserExecutablePath = chrome, browserUrl = "https://browsers.example.com/team/one/")
        )

        assertEquals("https://browsers.example.com/team/one/", endpoint.httpBaseUrl)
        assertEquals(
            "wss://browsers.example.com/team/one/devtools/page/t1",
            endpoint.webSocketUrl("/devtools/page/t1"),
        )
        // The browser only knows its own, internal, address: keep its path, swap the rest.
        assertEquals(
            "wss://browsers.example.com/team/one/devtools/browser/abc",
            endpoint.rewriteWebSocketDebuggerUrl("ws://127.0.0.1:9222/devtools/browser/abc"),
        )
    }

    @Test
    fun httpBrowserUrl_withExplicitPort_mapsToWsOnThatPort() {
        val endpoint = DevToolsEndpoint.from(Config(browserExecutablePath = chrome, browserUrl = "http://10.0.0.5:8080"))

        assertEquals("http://10.0.0.5:8080/", endpoint.httpBaseUrl)
        assertEquals(
            "ws://10.0.0.5:8080/devtools/browser/abc",
            endpoint.rewriteWebSocketDebuggerUrl("ws://localhost:9222/devtools/browser/abc"),
        )
    }

    @Test
    fun browserUrl_takesPrecedenceOverHostAndPort() {
        val endpoint = DevToolsEndpoint.from(
            Config(browserExecutablePath = chrome, host = "127.0.0.1", port = 9222, browserUrl = "https://example.com/b")
        )

        assertEquals("https://example.com/b/", endpoint.httpBaseUrl)
    }

    @Test
    fun browserUrl_mustBeHttpOrHttps() {
        val error = assertFailsWith<IllegalArgumentException> {
            Config(browserExecutablePath = chrome, browserUrl = "ftp://example.com/")
        }
        assertTrue(error.message!!.contains("browserUrl"), error.message)
    }

    @Test
    fun browserUrl_withQueryFragmentOrCredentials_isRejected() {
        // The json/… and devtools/… paths are appended below browserUrl, which a query or fragment
        // would swallow; credentials belong in connectionHeaders.
        listOf(
            "https://example.com/b?token=abc",
            "https://example.com/b#section",
            "https://user:password@example.com/b",
        ).forEach { url ->
            assertFailsWith<IllegalArgumentException>(url) {
                Config(browserExecutablePath = chrome, browserUrl = url)
            }
        }
    }

    @Test
    fun advertisedUrl_alreadyOnTheBrowserUrl_isKeptAsIs() {
        // A proxy may already rewrite webSocketDebuggerUrl onto its own address: don't prefix twice.
        val endpoint = DevToolsEndpoint.from(
            Config(browserExecutablePath = chrome, browserUrl = "https://browsers.example.com/team/one")
        )

        assertEquals(
            "wss://browsers.example.com/team/one/devtools/browser/abc",
            endpoint.rewriteWebSocketDebuggerUrl("wss://browsers.example.com/team/one/devtools/browser/abc"),
        )
    }

    @Test
    fun copy_keepsBrowserUrlAndConnectionHeaders() {
        val original = Config(
            browserExecutablePath = chrome,
            browserUrl = "https://example.com/b",
            connectionHeaders = mapOf("Authorization" to "Bearer secret"),
        )

        val copy = original.copy()

        assertEquals("https://example.com/b", copy.browserUrl)
        assertEquals(mapOf("Authorization" to "Bearer secret"), copy.connectionHeaders)
    }

    @Test
    fun builder_setsBrowserUrlAndConnectionHeaders() {
        val config = config {
            browserExecutablePath = chrome
            browserUrl = "https://example.com/b"
            connectionHeaders = mapOf("Authorization" to "Bearer secret")
        }

        assertEquals("https://example.com/b", config.browserUrl)
        assertEquals(mapOf("Authorization" to "Bearer secret"), config.connectionHeaders)
    }

    @Test
    fun defaults_haveNoBrowserUrlAndNoHeaders() {
        val config = Config(browserExecutablePath = chrome)

        assertEquals(null, config.browserUrl)
        assertEquals(emptyMap<String, String>(), config.connectionHeaders)
    }

}
