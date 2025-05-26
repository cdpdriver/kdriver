package dev.kdriver.cdp

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass

/**
 * Thin wrapper of Chrome DevTools Protocol.
 */
open class CDP(
    private val wsSession: ClientWebSocketSession,
    messageListeningScope: CoroutineScope = GlobalScope,
) {

    companion object {

        /**
         * Create and return a CDPClient instance.
         * Caller is responsible for closing the client.
         */
        suspend fun create(
            host: String,
            port: Int,
            path: String,
            messageListeningScope: CoroutineScope = GlobalScope,
        ): CDP {
            val client = HttpClient(CIO) {
                install(WebSockets)
            }
            val wsSession = client.webSocketSession {
                url {
                    this.protocol = URLProtocol.WS
                    this.host = host
                    this.port = port
                    this.path(path)
                }
            }
            return CDP(wsSession, messageListeningScope)
        }

        /**
         * Open [block] with [CDP].
         *
         * It always try to connect to peer with ws:// scheme.
         */
        suspend fun use(
            host: String,
            port: Int,
            path: String,
            messageListeningScope: CoroutineScope = GlobalScope,
            block: suspend CDP.() -> Unit,
        ) {
            val client = create(host, port, path, messageListeningScope)
            client.block()
            client.close()
        }

    }

    private val socketSubscription: Job = messageListeningScope.launch {
        for (frame in wsSession.incoming) {
            val received: Message = when (frame) {
                is Frame.Text -> Json.decodeFromString(frame.readText())
                else -> error("Unsupported websocket frame type: $frame")
            }
            allMessages.emit(received)
        }
    }
    private var currentID = 0

    private var allMessages = MutableSharedFlow<Message>()

    internal val events: Flow<Message.Event> = allMessages.filterIsInstance()

    private val responses: Flow<Message.Response> = allMessages.filterIsInstance()

    private val generatedDomains: MutableMap<KClass<out Domain>, Domain> = mutableMapOf()
    internal inline fun <reified T : Domain> getGeneratedDomain(): T? =
        if (generatedDomains.containsKey(T::class)) {
            generatedDomains[T::class] as T
        } else null

    internal inline fun <reified T : Domain> cacheGeneratedDomain(domain: T): T {
        generatedDomains[T::class] = domain
        return domain
    }

    internal suspend fun callCommand(method: String, parameter: JsonElement?): JsonElement? {
        val requestID = currentID++
        val jsonString = Json.encodeToString(Request(requestID, method, parameter))
        wsSession.send(jsonString)
        val result = responses.first { it.id == requestID }
        result.error?.throwAsException()
        return result.result
    }

    /**
     * End the session between the browser.
     *
     * It' wont close WebSocket session. Please close that manually.
     */
    fun close() {
        socketSubscription.cancel()
    }

}
