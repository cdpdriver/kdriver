package dev.kdriver.core.connection

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.ServerSocket
import kotlin.test.Test
import kotlin.test.assertEquals

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

}
