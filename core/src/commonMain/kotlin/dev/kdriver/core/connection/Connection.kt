package dev.kdriver.core.connection

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.Message
import dev.kdriver.cdp.Request
import dev.kdriver.cdp.domain.Target
import dev.kdriver.cdp.domain.target
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.BrowserTarget
import dev.kdriver.core.utils.parseWebSocketUrl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.util.logging.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass

open class Connection(
    private val websocketUrl: String,
    private val messageListeningScope: CoroutineScope,
    private val eventsBufferSize: Int,
    override var targetInfo: Target.TargetInfo? = null,
    var owner: Browser? = null,
) : BrowserTarget, CDP {

    private val logger = KtorSimpleLogger("Connection")
    private val debugStringLimit = 64

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private var wsSession: ClientWebSocketSession? = null

    private var socketSubscription: Job? = null

    private fun startListening() {
        socketSubscription?.cancel()
        socketSubscription = messageListeningScope.launch {
            try {
                for (frame in wsSession?.incoming ?: return@launch) {
                    try {
                        frame as? Frame.Text ?: continue
                        val text = frame.readText()
                        logger.debug("WS < CDP: ${text.take(debugStringLimit)}")
                        val received = Serialization.json.decodeFromString<Message>(text)
                        allMessages.emit(received)
                    } catch (e: Exception) {
                        logger.debug("WebSocket exception while receiving message: {}", e.message)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle disconnect, maybe trigger reconnect logic here
            }
        }
    }

    private var currentID = 0

    private var allMessages = MutableSharedFlow<Message>(extraBufferCapacity = eventsBufferSize)

    override val events: Flow<Message.Event> = allMessages.filterIsInstance()
    override val responses: Flow<Message.Response> = allMessages.filterIsInstance()

    override val generatedDomains: MutableMap<KClass<out Domain>, Domain> = mutableMapOf()

    private suspend fun connect() {
        if (wsSession != null && wsSession?.isActive == true) return
        wsSession = client.webSocketSession {
            url {
                val parsed = parseWebSocketUrl(websocketUrl)
                this.protocol = URLProtocol.WS
                this.host = parsed.host
                this.port = parsed.port
                this.path(parsed.path)
            }
        }
        startListening()
    }

    override suspend fun callCommand(method: String, parameter: JsonElement?): JsonElement? {
        connect()
        val requestID = currentID++
        val jsonString = Json.encodeToString(Request(requestID, method, parameter))
        wsSession?.send(jsonString)
        logger.debug("WS > CDP: ${jsonString.take(debugStringLimit)}")
        val result = responses.first { it.id == requestID }
        result.error?.throwAsException()
        return result.result
    }

    suspend fun close() {
        wsSession?.close()
        wsSession = null
        socketSubscription?.cancel()
        socketSubscription = null
    }

    suspend fun updateTarget() {
        val targetInfo = target.getTargetInfo(targetId)
        this.targetInfo = targetInfo.targetInfo
    }

    suspend fun wait(t: Long? = null) {
        updateTarget()

        // TODO
    }

    suspend fun sleep(t: Long) {
        updateTarget()
        delay(t)
    }

    override fun toString(): String {
        return "Connection: ${targetInfo?.toString() ?: "no target"}"
    }

}
