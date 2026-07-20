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
import kotlin.concurrent.Volatile
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

    companion object {
        // One initial send plus one resend after a transparent reconnect. The resend is only ever reached
        // when a send fails (bytes never left the client), so it cannot double-execute a command.
        private const val SEND_ATTEMPTS = 2
    }

    private val logger = KtorSimpleLogger("Connection")

    // Recreated on every (re)connect. A dropped socket can keep reporting a stale `isActive` for a
    // short window, so once the receive loop has observed a disconnect we throw the whole transport
    // away and build a fresh one rather than trusting it (see [connect]).
    private var transport: WebSocketTransport? = null

    @Volatile
    private var disconnected = false

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

    private suspend fun connect(): WebSocketTransport {
        transport?.let { if (it.isActive && !disconnected) return it }
        // Guard so concurrent first commands don't each open a session (which would leak the
        // duplicate sockets/listeners). Double-checked: skip the lock once connected (ISSUE-4).
        return connectMutex.withLock {
            transport?.let { if (it.isActive && !disconnected) return@withLock it }
            // Once the receive loop has reported a disconnect we don't trust the old transport's
            // `isActive` (it can lag the real socket close), so dispose it and build a fresh one. This
            // makes a command issued after a drop reconnect deterministically instead of reusing a
            // dead socket and failing (or hanging until the command timeout).
            if (disconnected) {
                transport?.let { old -> runCatching { old.close() } }
                transport = null
                disconnected = false
            }
            val t = transport ?: createTransport().also { transport = it }
            if (!t.isActive) {
                t.connect()
                startListening(t)
            }
            t
        }
    }

    private fun startListening(t: WebSocketTransport) {
        socketSubscription?.cancel()
        socketSubscription = messageListeningScope.launch {
            try {
                t.incoming().collect { text ->
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
                onTransportGone(t, ConnectionClosedException())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("WebSocket receive loop terminated: {}", e)
                onTransportGone(t, ConnectionClosedException(cause = e))
            }
        }
    }

    /**
     * Reacts to a receive loop ending because its socket went away: marks the connection for a
     * reconnect and fails any in-flight commands. Ignored if [t] is no longer the current transport
     * (a stale loop from a transport we've already replaced must not disturb the new one).
     */
    private suspend fun onTransportGone(t: WebSocketTransport, cause: Throwable) {
        if (transport !== t) return
        disconnected = true
        failPendingRequests(cause)
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
        val jsonString = Serialization.json.encodeToString(Request(requestId, method, parameter))
        val timeout = owner?.config?.commandTimeout ?: Defaults.COMMAND_TIMEOUT

        // A dropped socket can still report itself active for a short window, so the send below can
        // fail (or the next command can land on a dead transport). A failed send means the bytes
        // never left the client, so reconnecting and resending is safe — no risk of executing the
        // command twice. We only retry the *send*: once a reply is awaited we never resend, because
        // the command may already have run (that case surfaces as a ConnectionClosedException).
        var sendError: Throwable? = null
        repeat(SEND_ATTEMPTS) {
            val transport = connect()
            // Register the response waiter *before* sending, so a reply that arrives before we start
            // awaiting is still captured (the receive loop completes this deferred). Awaiting the
            // response via a replay-0 shared flow after sending could miss it and hang (ISSUE-1).
            val deferred = CompletableDeferred<Message.Response>()
            pendingRequestsMutex.withLock { pendingRequests[requestId] = deferred }

            val sent = try {
                transport.send(jsonString)
                true
            } catch (e: CancellationException) {
                // A send on a dead Ktor session surfaces as a channel CancellationException even
                // though *this* coroutine isn't cancelled; only the latter must propagate.
                if (!currentCoroutineContext().isActive) throw e
                sendError = e
                disconnected = true
                false
            } catch (e: Exception) {
                sendError = e
                disconnected = true
                false
            }
            if (!sent) {
                pendingRequestsMutex.withLock { pendingRequests.remove(requestId) }
                return@repeat // reconnect and resend on the next iteration
            }

            logger.debug("WS > CDP: ${jsonString.take(owner?.config?.debugStringLimit ?: Defaults.DEBUG_STRING_LIMIT)}")
            try {
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
        throw sendError ?: ConnectionClosedException()
    }

    @InternalCdpApi
    override suspend fun close() {
        transport?.close()
        transport = null
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
