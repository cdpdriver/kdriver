package dev.kdriver.core.connection

import dev.kdriver.cdp.*
import dev.kdriver.cdp.domain.*
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.Config.Defaults
import dev.kdriver.core.exceptions.CommandTimeoutException
import dev.kdriver.core.exceptions.ConnectionClosedException
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlin.reflect.KClass
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

/**
 * Default implementation of the [Connection] interface.
 */
open class DefaultConnection(
    private val websocketUrl: String,
    private val messageListeningScope: CoroutineScope,
    override var targetInfo: Target.TargetInfo? = null,
    override var owner: Browser? = null,
) : OwnedConnection {

    private val logger = KtorSimpleLogger("Connection")

    private val transport: WebSocketTransport by lazy { createTransport() }

    private var socketSubscription: Job? = null

    private val connectMutex = Mutex()
    private val prepareMutex = Mutex()

    private val currentIdMutex = Mutex()
    private var currentId = 0L

    private val pendingRequestsMutex = Mutex()
    private val pendingRequests = mutableMapOf<Long, CompletableDeferred<Message.Response>>()

    /**
     * Creates the [WebSocketTransport] used to talk to the browser.
     *
     * Overridable so tests can inject a fake transport without a real browser.
     */
    protected open fun createTransport(): WebSocketTransport = KtorWebSocketTransport(websocketUrl)

    private var prepareHeadlessDone = false
    private var prepareExpertDone = false

    private val allMessages = MutableSharedFlow<Message>(extraBufferCapacity = Channel.UNLIMITED)

    @InternalCdpApi
    override val events: Flow<Message.Event> = allMessages.filterIsInstance()

    @InternalCdpApi
    override val responses: Flow<Message.Response> = allMessages.filterIsInstance()

    @InternalCdpApi
    override val generatedDomains: MutableMap<KClass<out Domain>, Domain> = mutableMapOf()

    private suspend fun connect() {
        if (transport.isActive) return
        // Guard so concurrent first commands don't each open a session (which would leak the
        // duplicate sockets/listeners). Double-checked: skip the lock once connected (ISSUE-4).
        connectMutex.withLock {
            if (transport.isActive) return@withLock
            transport.connect()
            startListening()
        }
    }

    private fun startListening() {
        socketSubscription?.cancel()
        socketSubscription = messageListeningScope.launch {
            try {
                transport.incoming().collect { text ->
                    try {
                        logger.debug("WS < CDP: ${text.take(owner?.config?.debugStringLimit ?: Defaults.DEBUG_STRING_LIMIT)}")
                        val received = Serialization.json.decodeFromString<Message>(text)
                        if (received is Message.Response) {
                            pendingRequestsMutex.withLock { pendingRequests.remove(received.id) }
                                ?.complete(received)
                        }
                        allMessages.emit(received)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.debug("WebSocket exception while receiving message: {}", e)
                    }
                }
                // incoming() completed without error => the socket was closed. Fail any in-flight
                // commands so their callers observe the disconnect instead of hanging (ISSUE-3).
                failPendingRequests(ConnectionClosedException())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("WebSocket receive loop terminated: {}", e)
                failPendingRequests(ConnectionClosedException(cause = e))
            }
        }
    }

    /**
     * Completes every in-flight request waiter exceptionally and clears the registry, so callers
     * parked in [callCommand] observe a failure rather than hanging when the connection goes away.
     */
    private suspend fun failPendingRequests(cause: Throwable) {
        val pending = pendingRequestsMutex.withLock {
            val snapshot = pendingRequests.values.toList()
            pendingRequests.clear()
            snapshot
        }
        pending.forEach { it.completeExceptionally(cause) }
    }

    @InternalCdpApi
    override suspend fun callCommand(method: String, parameter: JsonElement?, mode: CommandMode): JsonElement? {
        connect()

        if (mode == CommandMode.DEFAULT) owner?.let { browser ->
            // Serialize preparation so concurrent first commands run it once, not N times (ISSUE-4).
            // prepare* issue ONE_SHOT commands, which skip this block, so prepareMutex isn't re-entered.
            prepareMutex.withLock {
                if (browser.config.expert) prepareExpert()
                if (browser.config.headless) prepareHeadless()
            }
        }

        val requestId = currentIdMutex.withLock { currentId++ }
        // Register the response waiter *before* sending, so a reply that arrives before we start
        // awaiting is still captured (the receive loop completes this deferred). Awaiting the
        // response via a replay-0 shared flow after sending could miss it and hang (ISSUE-1).
        val deferred = CompletableDeferred<Message.Response>()
        pendingRequestsMutex.withLock { pendingRequests[requestId] = deferred }
        try {
            val jsonString = Serialization.json.encodeToString(Request(requestId, method, parameter))
            transport.send(jsonString)
            logger.debug("WS > CDP: ${jsonString.take(owner?.config?.debugStringLimit ?: Defaults.DEBUG_STRING_LIMIT)}")

            val timeout = owner?.config?.commandTimeout ?: Defaults.COMMAND_TIMEOUT
            // A non-null Message.Response means success; null can only come from the timeout, so
            // there's no ambiguity with a legitimate value. A timeout <= 0 waits indefinitely.
            val result =
                if (timeout > 0) withTimeoutOrNull(timeout.milliseconds) { deferred.await() }
                    ?: throw CommandTimeoutException(method, requestId, timeout)
                else deferred.await()
            result.error?.throwAsException(method)
            return result.result
        } finally {
            pendingRequestsMutex.withLock { pendingRequests.remove(requestId) }
        }
    }

    @InternalCdpApi
    override suspend fun close() {
        transport.close()
        socketSubscription?.cancel()
        socketSubscription = null
        // Fail any commands still awaiting a reply that will now never come (ISSUE-3).
        failPendingRequests(ConnectionClosedException("Connection closed"))
    }

    override suspend fun updateTarget() {
        val targetInfo = target.getTargetInfo(targetId)
        this.targetInfo = targetInfo.targetInfo
    }

    override suspend fun wait(t: Long?) {
        updateTarget()
        val idleEvent: suspend () -> Boolean = {
            withTimeoutOrNull(100.milliseconds) { events.first() } == null
        }

        if (t != null) {
            val start = Clock.System.now().toEpochMilliseconds()
            withTimeoutOrNull(t.milliseconds) {
                // Wait for idle event or timeout
                while (true) {
                    if (idleEvent()) break
                    delay(50.milliseconds)
                }
            }
            // Ensure total wait time is at least t milliseconds
            val elapsed = Clock.System.now().toEpochMilliseconds() - start
            if (elapsed < t) delay((t - elapsed).milliseconds)
        } else {
            // Wait indefinitely for idle event
            while (true) {
                if (idleEvent()) break
                delay(50.milliseconds)
            }
        }
    }

    override suspend fun sleep(t: Long) {
        updateTarget()
        delay(t.milliseconds)
    }

    private suspend fun prepareHeadless() {
        try {
            if (prepareHeadlessDone) return
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
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    private suspend fun prepareExpert() {
        try {
            if (prepareExpertDone) return
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
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    override fun toString(): String {
        return "Connection: ${targetInfo?.toString() ?: "no target"}"
    }

}
