package dev.kdriver.core.connection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.InputStream
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Handshake-level behaviour of [KtorWebSocketTransport], checked against a bare TCP listener that
 * records what the client sends instead of a real browser.
 */
class KtorWebSocketTransportTest {

    /** First byte of a TLS record carrying a handshake message (the ClientHello). */
    private val tlsHandshakeRecord = 0x16

    @Test
    fun wssUrl_opensTheConnectionWithTls() = runBlocking {
        ServerSocket(0).use { server ->
            val transport = KtorWebSocketTransport("wss://127.0.0.1:${server.localPort}/devtools/browser/abc")
            val firstByte = async(Dispatchers.IO) {
                server.accept().use { it.getInputStream().read() }
            }
            val connecting = launch { runCatching { transport.connect() } }
            try {
                // A plain-text handshake would start with "GET" (0x47); a wss:// URL must start with TLS.
                assertEquals(tlsHandshakeRecord, withTimeout(10_000) { firstByte.await() })
            } finally {
                connecting.cancel()
                withContext(Dispatchers.IO) { runCatching { transport.close() } }
            }
        }
    }

    @Test
    fun connect_sendsTheExtraHeadersAndThePathPrefixInTheHandshake() = runBlocking {
        ServerSocket(0).use { server ->
            val transport = KtorWebSocketTransport(
                "ws://127.0.0.1:${server.localPort}/some/prefix/devtools/browser/abc",
                headers = mapOf("Authorization" to "Bearer secret", "X-Custom" to "value"),
            )
            val requestHead = async(Dispatchers.IO) {
                server.accept().use { readRequestHead(it.getInputStream()) }
            }
            val connecting = launch { runCatching { transport.connect() } }
            try {
                val head = withTimeout(10_000) { requestHead.await() }
                val lines = head.lines()
                assertEquals("GET /some/prefix/devtools/browser/abc HTTP/1.1", lines.first())
                assertTrue("Authorization: Bearer secret" in lines, "missing Authorization header in:\n$head")
                assertTrue("X-Custom: value" in lines, "missing X-Custom header in:\n$head")
            } finally {
                connecting.cancel()
                withContext(Dispatchers.IO) { runCatching { transport.close() } }
            }
        }
    }

    /** Reads an HTTP request up to (excluding) the blank line that ends its headers. */
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
