package dev.kdriver.core.connection

import dev.kdriver.core.browser.WebSocketInfo
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Default [WebSocketTransport] backed by a Ktor WebSocket session.
 */
class KtorWebSocketTransport(
    private val websocketUrl: String,
) : WebSocketTransport {

    private val client = HttpClient(getWebSocketClientEngine()) {
        install(WebSockets)
    }

    private var session: ClientWebSocketSession? = null

    override val isActive: Boolean
        get() = session?.isActive == true

    override suspend fun connect() {
        if (isActive) return
        session = client.webSocketSession {
            url {
                val parsed = parseWebSocketUrl(websocketUrl)
                this.protocol = URLProtocol.WS
                this.host = parsed.host
                this.port = parsed.port
                this.path(parsed.path)
            }
        }
    }

    override suspend fun send(message: String) {
        session?.send(message)
    }

    override fun incoming(): Flow<String> = flow {
        val session = session ?: return@flow
        for (frame in session.incoming) {
            val text = (frame as? Frame.Text)?.readText() ?: continue
            emit(text)
        }
    }

    override suspend fun close() {
        session?.close()
        session = null
        // Close the engine-backed client too, otherwise its threads/connection pool leak across
        // browser create/stop cycles (ISSUE-9).
        client.close()
    }

    private fun parseWebSocketUrl(url: String): WebSocketInfo {
        val uri = Url(url)

        val host = uri.host
        val port = if (uri.port != -1) uri.port else when (uri.protocol) {
            URLProtocol.WS -> 80
            URLProtocol.WSS -> 443
            else -> throw IllegalArgumentException("Unsupported scheme: ${uri.protocol}")
        }
        val path = uri.encodedPath

        return WebSocketInfo(host, port, path)
    }

}
