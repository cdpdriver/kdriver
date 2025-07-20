package dev.kdriver.core.connection

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import dev.kdriver.cdp.domain.*
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.utils.getWebSocketClientEngine
import dev.kdriver.core.utils.parseWebSocketUrl
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.util.logging.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KClass

/**
 * Default implementation of the [Connection] interface.
 */
open class DefaultConnection(
    private val websocketUrl: String,
    private val messageListeningScope: CoroutineScope,
    private val eventsBufferSize: Int,
    override var targetInfo: Target.TargetInfo? = null,
    var owner: Browser? = null,
) : Connection {

    private val logger = KtorSimpleLogger("Connection")
    private val debugStringLimit = 64

    private val client = HttpClient(getWebSocketClientEngine()) {
        install(WebSockets)
    }

    private var wsSession: ClientWebSocketSession? = null

    private var socketSubscription: Job? = null

    private val currentIdMutex = Mutex()
    private var currentId = 0L

    private var prepareHeadlessDone = false
    private var prepareExpertDone = false

    private val allMessages = MutableSharedFlow<Message>(extraBufferCapacity = eventsBufferSize)

    @InternalCdpApi
    override val events: Flow<Message.Event> = allMessages.filterIsInstance()

    @InternalCdpApi
    override val responses: Flow<Message.Response> = allMessages.filterIsInstance()

    @InternalCdpApi
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
                        logger.debug("WebSocket exception while receiving message: {}", e)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle disconnect, maybe trigger reconnect logic here
            }
        }
    }

    @InternalCdpApi
    override suspend fun callCommand(method: String, parameter: JsonElement?, mode: CommandMode): JsonElement? {
        connect()

        if (mode == CommandMode.DEFAULT) owner?.let { browser ->
            if (browser.config.expert) prepareExpert()
            if (browser.config.headless) prepareHeadless()
        }

        val requestId = currentIdMutex.withLock { currentId++ }
        val jsonString = Serialization.json.encodeToString(Request(requestId, method, parameter))
        wsSession?.send(jsonString)
        logger.debug("WS > CDP: ${jsonString.take(debugStringLimit)}")

        val result = responses.first { it.id == requestId }
        result.error?.throwAsException(method)
        return result.result
    }

    @InternalCdpApi
    override suspend fun close() {
        wsSession?.close()
        wsSession = null
        socketSubscription?.cancel()
        socketSubscription = null
    }

    override suspend fun updateTarget() {
        val targetInfo = target.getTargetInfo(targetId)
        this.targetInfo = targetInfo.targetInfo
    }

    override suspend fun wait(t: Long?) {
        updateTarget()
        val idleEvent: suspend () -> Boolean = {
            withTimeoutOrNull(100) { events.first() } == null
        }

        if (t != null) {
            val start = Clock.System.now().toEpochMilliseconds()
            withTimeoutOrNull(t) {
                // Wait for idle event or timeout
                while (true) {
                    if (idleEvent()) break
                    delay(50)
                }
            }
            // Ensure total wait time is at least t milliseconds
            val elapsed = Clock.System.now().toEpochMilliseconds() - start
            if (elapsed < t) delay(t - elapsed)
        } else {
            // Wait indefinitely for idle event
            while (true) {
                if (idleEvent()) break
                delay(50)
            }
        }
    }

    override suspend fun sleep(t: Long) {
        updateTarget()
        delay(t)
    }

    private suspend fun prepareHeadless() = runCatching {
        if (prepareHeadlessDone) return@runCatching
        val response = runtime.evaluate(
            Runtime.EvaluateParameter(
                expression = "navigator.userAgent",
                userGesture = true,
                awaitPromise = true,
                returnByValue = true,
                allowUnsafeEvalBlockedByCSP = true
            ),
            CommandMode.ONE_SHOT
        )
        response.result.value?.jsonPrimitive?.content?.let { ua ->
            network.setUserAgentOverride(
                Network.SetUserAgentOverrideParameter(
                    userAgent = ua.replace("Headless", "")
                ),
                CommandMode.ONE_SHOT
            )
        }
        prepareHeadlessDone = true
    }

    private suspend fun prepareExpert() = runCatching {
        if (prepareExpertDone) return@runCatching
        owner?.let {
            page.addScriptToEvaluateOnNewDocument(
                Page.AddScriptToEvaluateOnNewDocumentParameter(
                    source = """
                    Element.prototype._attachShadow = Element.prototype.attachShadow;
                    Element.prototype.attachShadow = function () {
                        return this._attachShadow( { mode: "open" } );
                    };
                    """.trimIndent()
                ),
                CommandMode.ONE_SHOT
            )
            page.enable(
                Page.EnableParameter(),
                CommandMode.ONE_SHOT
            )
        }
        prepareExpertDone = true
    }

    override fun toString(): String {
        return "Connection: ${targetInfo?.toString() ?: "no target"}"
    }

}
