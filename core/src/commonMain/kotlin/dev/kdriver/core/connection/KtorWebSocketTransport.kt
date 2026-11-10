package dev.kdriver.core.connection

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

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
            // Take the whole URL as given — scheme included — so a wss:// endpoint (for instance a
            // browser reached through a TLS-terminating reverse proxy) is opened over TLS.
            url.takeFrom(websocketUrl)
        }
    }

    override suspend fun send(message: String) {
        // Fail loudly instead of silently succeeding when there is no session: a false success would
        // make the caller wait for a reply to a frame that was never sent.
        val session = session ?: throw IllegalStateException("WebSocket session is not connected")
        session.send(message)
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

}
