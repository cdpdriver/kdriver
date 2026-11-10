package dev.kdriver.core.browser

import dev.kdriver.core.tab.evaluate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Attaching to an already running browser through a reverse proxy, then detaching from it, against a
 * real browser. The proxy ([PrefixProxy]) serves the browser under a path prefix and requires an
 * authentication header, like a typical reverse proxy in front of a remote browser.
 */
class RemoteBrowserTest {

    private val prefix = "/remote/browser"
    private val auth = "Authorization" to "Bearer secret"

    private suspend fun httpStatus(url: String, headers: Map<String, String> = emptyMap()): Int =
        withContext(Dispatchers.IO) {
            val connection = URI(url).toURL().openConnection() as HttpURLConnection
            try {
                headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
                connection.responseCode
            } finally {
                connection.disconnect()
            }
        }

    @Test
    fun attachThroughPrefixedProxy_thenDetach_leavesTheBrowserRunning() = runBlocking {
        // The browser something else owns: launched here, but only ever reached through the proxy.
        val owner = createBrowser(this, headless = true, sandbox = false)
        val proxy = PrefixProxy("127.0.0.1", owner.config.port!!, prefix, auth, this).also { it.start() }
        val proxyUrl = "http://127.0.0.1:${proxy.port}$prefix"

        try {
            val attached = createBrowser(this) {
                browserUrl = proxyUrl
                connectionHeaders = mapOf(auth)
            }

            // Attached to the existing browser rather than launching one, through the proxy only.
            assertEquals(null, attached.pid, "attaching must not launch a browser")
            assertTrue(
                attached.websocketUrl.startsWith("ws://127.0.0.1:${proxy.port}$prefix/devtools/browser/"),
                "the advertised debugger URL must be moved onto the browser URL, was ${attached.websocketUrl}",
            )
            val tab = assertNotNull(attached.mainTab, "the existing page must be discovered")
            assertEquals(2, tab.evaluate<Int>("1 + 1"))
            assertTrue(
                proxy.webSocketPaths.any { it.startsWith("$prefix/devtools/browser/") } &&
                    proxy.webSocketPaths.any { it.startsWith("$prefix/devtools/page/") },
                "browser and page connections must go through the proxy, saw ${proxy.webSocketPaths}",
            )
            assertEquals(emptyList(), proxy.refusedPaths, "every request must carry the prefix and the header")

            attached.detach()

            // The browser is still there and still answers, directly and through the proxy.
            assertEquals(200, httpStatus("http://127.0.0.1:${owner.config.port}/json/version"))
            assertEquals(200, httpStatus("$proxyUrl/json/version", mapOf(auth)))
            assertTrue(owner.testConnection())
            assertEquals(4, assertNotNull(owner.mainTab).evaluate<Int>("2 + 2"))
        } finally {
            proxy.stop()
            owner.stop()
        }
    }

}
