package dev.kdriver.core.browser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.io.InputStream
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Request-level behaviour of [HTTPApi], checked against a bare TCP listener that records the request
 * and answers a canned `/json/version` payload instead of a real browser.
 */
class HTTPApiTest {

    private val versionBody = """{"webSocketDebuggerUrl":"ws://127.0.0.1:9222/devtools/browser/abc"}"""

    /** Accepts one connection, returns its request head and answers [versionBody]. */
    private fun ServerSocket.answerOnce(): String = accept().use { socket ->
        val head = readRequestHead(socket.getInputStream())
        val body = versionBody.toByteArray()
        socket.getOutputStream().apply {
            write(
                ("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n" +
                    "Content-Length: ${body.size}\r\nConnection: close\r\n\r\n").toByteArray()
            )
            write(body)
            flush()
        }
        head
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

    @Test
    fun hostAndPort_keepTheHistoricalBaseUrl() {
        assertEquals("http://127.0.0.1:9222/", HTTPApi("127.0.0.1", 9222).baseUrl)
    }

    @Test
    fun baseUrl_withPathPrefix_andHeaders_areUsedForJsonEndpoints() = runBlocking {
        ServerSocket(0).use { server ->
            val api = HTTPApi(
                baseUrl = "http://127.0.0.1:${server.localPort}/some/prefix",
                headers = mapOf("Authorization" to "Bearer secret"),
            )
            try {
                val head = async(Dispatchers.IO) { server.answerOnce() }
                val info = withTimeout(10_000) { api.get<ContraDict>("version") }
                val lines = withTimeout(10_000) { head.await() }.lines()

                assertEquals("GET /some/prefix/json/version HTTP/1.1", lines.first())
                assertTrue("Authorization: Bearer secret" in lines, "missing Authorization header in:\n$lines")
                assertEquals("ws://127.0.0.1:9222/devtools/browser/abc", info.webSocketDebuggerUrl)
            } finally {
                api.close()
            }
        }
    }

}
