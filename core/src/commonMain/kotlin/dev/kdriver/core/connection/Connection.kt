package dev.kdriver.core.connection

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import dev.kdriver.cdp.domain.Target
import dev.kdriver.cdp.domain.target
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.BrowserTarget
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

    private val client = HttpClient(getWebSocketClientEngine()) {
        install(WebSockets)
    }

    private var wsSession: ClientWebSocketSession? = null

    private var socketSubscription: Job? = null

    private val currentIdMutex = Mutex()
    private var currentId = 0L

    private var allMessages = MutableSharedFlow<Message>(extraBufferCapacity = eventsBufferSize)

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

    /**
     * Internal method to call a CDP command.
     *
     * This should not be called directly, but rather through typed methods (like `cdp.network.enable()`).
     */
    @InternalCdpApi
    override suspend fun callCommand(method: String, parameter: JsonElement?): JsonElement? {
        connect()
        val requestId = currentIdMutex.withLock { currentId++ }
        val jsonString = Json.encodeToString(Request(requestId, method, parameter))
        wsSession?.send(jsonString)
        logger.debug("WS > CDP: ${jsonString.take(debugStringLimit)}")
        val result = responses.first { it.id == requestId }
        result.error?.throwAsException(method)
        return result.result
    }

    /**
     * Closes the websocket connection. Should not be called manually by users.
     */
    @InternalCdpApi
    suspend fun close() {
        wsSession?.close()
        wsSession = null
        socketSubscription?.cancel()
        socketSubscription = null
    }

    /**
     * Updates the target information by fetching it from the CDP.
     *
     * This is useful to refresh the target info after some operations that might change it.
     */
    suspend fun updateTarget() {
        val targetInfo = target.getTargetInfo(targetId)
        this.targetInfo = targetInfo.targetInfo
    }

    /**
     * Waits until the event listener reports idle (no new events received in a certain timespan).
     * When \`t\` is provided, ensures waiting for \`t\` milliseconds, no matter what.
     *
     * @param t Time in milliseconds to wait, or null to wait until idle.
     */
    suspend fun wait(t: Long? = null) {
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

    /**
     * Suspends the coroutine for a specified time in milliseconds.
     *
     * This is a convenience method to ensure that the target information is updated before sleeping.
     *
     * @param t Time in milliseconds to sleep.
     */
    suspend fun sleep(t: Long) {
        updateTarget()
        delay(t)
    }

    /**
     * Sends a CDP command and waits for the response.
     *
     * This is an alias so that you can use cdp the same way as zendriver does:
     * ```kotlin
     * // send a network.enable command with kdriver
     * tab.send { cdp.network.enable() }
     * ```
     * That would be equivalent to this with zendriver:
     * ```python
     * # send a network.enable command with zendriver
     * tab.send(cdp.network.enable())
     * ```
     *
     * Although you can directly call the CDP methods on the tab (recommended way of doing it):
     * ```kotlin
     * // send a network.enable command with kdriver, directly
     * tab.network.enable()
     * ```
     *
     * @param command The command to send. This is a suspending function that can call any CDP method.
     *
     * @return The result of the command, deserialized to type T.
     */
    inline fun <T> send(command: CDP.() -> T): T {
        return this.command()
    }

    /**
     * Adds a handler for a specific CDP event.
     *
     * This is an alias so that you can use cdp the same way as zendriver does:
     * ```kotlin
     * // add a handler for the consoleAPICalled event with kdriver
     * tab.addHandler(this, { cdp.runtime.consoleAPICalled }) { event ->
     *     println(event)
     * }
     * ```
     * That would be equivalent to this with zendriver:
     * ```python
     * # add a handler for the consoleAPICalled event with zendriver
     * tab.add_handler(cdp.runtime.consoleAPICalled, lambda event: print(event))
     * ```
     *
     * Although you can directly collect the events from the tab (recommended way of doing it):
     * ```kotlin
     * // add a handler for the consoleAPICalled event with kdriver, directly
     * launch {
     *     tab.runtime.consoleAPICalled.collect { event ->
     *         println(event)
     *     }
     * }
     * ```
     *
     * @param coroutineScope The coroutine scope in which the handler will run.
     * @param event A lambda that returns a Flow of the event type to listen to.
     * @param handler A suspend function that will be called with each event of the specified type.
     *
     * @return A Job that can be used to cancel the handler.
     */
    inline fun <T> addHandler(
        coroutineScope: CoroutineScope,
        crossinline event: CDP.() -> Flow<T>,
        crossinline handler: suspend (T) -> Unit,
    ): Job {
        return coroutineScope.launch {
            event().collect { handler(it) }
        }
    }

    override fun toString(): String {
        return "Connection: ${targetInfo?.toString() ?: "no target"}"
    }

}
