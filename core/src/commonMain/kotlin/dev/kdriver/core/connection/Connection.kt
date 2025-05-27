package dev.kdriver.core.connection

import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.Message
import dev.kdriver.cdp.Request
import dev.kdriver.cdp.domain.Target
import dev.kdriver.cdp.domain.target
import dev.kdriver.core.browser.BrowserTarget
import dev.kdriver.core.browser.parseWebSocketUrl
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

open class Connection(
    private val websocketUrl: String,
    override var targetInfo: Target.TargetInfo? = null,
    private val messageListeningScope: CoroutineScope = GlobalScope,
) : BrowserTarget, CDP {

    private val client = HttpClient(CIO) {
        install(WebSockets)
    }

    private var wsSession: ClientWebSocketSession? = null

    @Deprecated("Just remove cdp().")
    fun cdp(isUpdate: Boolean = false): CDP {
        return this
    }

    private var socketSubscription: Job? = null

    private fun startListening() {
        socketSubscription?.cancel()
        socketSubscription = messageListeningScope.launch {
            try {
                for (frame in wsSession?.incoming ?: return@launch) {
                    val received: Message = when (frame) {
                        is Frame.Text -> Json.decodeFromString(frame.readText())
                        else -> error("Unsupported websocket frame type: $frame")
                    }
                    allMessages.emit(received)
                }
            } catch (e: Exception) {
                // Handle disconnect, maybe trigger reconnect logic here
            }
        }
    }

    private var currentID = 0

    private var allMessages = MutableSharedFlow<Message>()

    override val events: Flow<Message.Event> = allMessages.filterIsInstance()
    override val responses: Flow<Message.Response> = allMessages.filterIsInstance()

    override val generatedDomains: MutableMap<KClass<out Domain>, Domain> = mutableMapOf()

    private suspend fun connect() {
        if (wsSession != null) return
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
        val result = responses.first { it.id == requestID }
        result.error?.throwAsException()
        return result.result
    }

    fun close() {
        socketSubscription?.cancel()
    }

    suspend fun updateTarget() {
        val targetInfo = cdp(isUpdate = true).target.getTargetInfo(targetId)
        this.targetInfo = targetInfo.targetInfo
    }

    suspend fun wait(t: Double? = null) {
        updateTarget()

        // TODO
    }

    override fun toString(): String {
        return "Connection: ${targetInfo?.toString() ?: "no target"}"
    }

}
