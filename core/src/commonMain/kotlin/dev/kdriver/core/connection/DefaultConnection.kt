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
    // away and build a fresh one rather than trusting it (see [connect]). @Volatile because it is
    // read off-lock on the fast path of [connect], from the receive-loop coroutine in [onTransportGone],
    // and in [close].
    @Volatile
    private var transport: WebSocketTransport? = null

    @Volatile
    private var disconnected = false

    // Set once [close] has run; terminal. [connect] refuses to re-open a closed connection so a
    // command issued after close fails instead of silently resurrecting the socket.
    @Volatile
    private var closed = false

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

    // Session state to re-apply after a reconnect (a new CDP session loses enabled domains, overrides,
    // injected scripts, …). Guarded by [reconnectRestoreMutex]; [needsRestore] flips true on reconnect.
    private val reconnectRestoreMutex = Mutex()
    private val reconnectRestores = LinkedHashMap<Any, suspend () -> Unit>()

    @Volatile
    private var needsRestore = false

    private val allMessages = MutableSharedFlow<Message>(extraBufferCapacity = Channel.UNLIMITED)

    @InternalCdpApi
    override val events: Flow<Message.Event> = allMessages.filterIsInstance()

    @InternalCdpApi
    override val responses: Flow<Message.Response> = allMessages.filterIsInstance()

    @InternalCdpApi
    override val generatedDomains: MutableMap<KClass<out Domain>, Domain> = mutableMapOf()

    private suspend fun connect(): WebSocketTransport {
        if (closed) throw ConnectionClosedException("Connection closed")
        transport?.let { if (it.isActive && !disconnected) return it }
        // Guard so concurrent first commands don't each open a session (which would leak the
        // duplicate sockets/listeners). Double-checked: skip the lock once connected (ISSUE-4).
        return connectMutex.withLock {
            if (closed) throw ConnectionClosedException("Connection closed")
            transport?.let { if (it.isActive && !disconnected) return@withLock it }
            // Once the receive loop has reported a disconnect we don't trust the old transport's
            // `isActive` (it can lag the real socket close), so dispose it and build a fresh one. This
            // makes a command issued after a drop reconnect deterministically instead of reusing a
            // dead socket and failing (or hanging until the command timeout).
            if (disconnected) {
                // Clear `transport` and cancel the old receive loop *before* closing the old transport,
                // so the loop ending on that close sees `transport !== old` and no-ops instead of
                // spuriously re-flagging `disconnected` (which would cascade into extra reconnects).
                val old = transport
                transport = null
                socketSubscription?.cancel()
                socketSubscription = null
                old?.let { runCatching { it.close() } }
                disconnected = false
                // A reconnect opens a *new* CDP session, so all session state applied on the old one
                // is gone. Re-run the built-in preparation, and flag the registered restorers (enabled
                // domains, overrides, …) to be replayed before the next command proceeds.
                prepareHeadlessDone = false
                prepareExpertDone = false
                needsRestore = true
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

    override suspend fun registerReconnectRestore(key: Any, restore: suspend () -> Unit) {
        reconnectRestoreMutex.withLock { reconnectRestores[key] = restore }
    }

    override suspend fun unregisterReconnectRestore(key: Any) {
        reconnectRestoreMutex.withLock { reconnectRestores.remove(key) }
    }

    /**
     * Re-applies the registered session state once after a reconnect. Snapshots the restorers and
     * clears [needsRestore] under the lock, then runs them *outside* it: a restorer only re-issues
     * CDP commands (whose [callCommand] sees [needsRestore] already false and so won't recurse here),
     * and running off-lock avoids a deadlock if one of those commands itself triggers a reconnect.
     */
    private suspend fun runReconnectRestores() {
        val restores = reconnectRestoreMutex.withLock {
            if (!needsRestore) return
            needsRestore = false
            reconnectRestores.values.toList()
        }
        for (restore in restores) {
            try {
                restore()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("Reconnect restore failed: {}", e)
            }
        }
    }

    /**
     * Runs the built-in browser-side preparation for a DEFAULT command, on the current session.
     * prepare* issue ONE_SHOT commands, which skip this step, so [prepareMutex] isn't re-entered.
     * Serialized so concurrent first commands run it once, not N times (ISSUE-4).
     */
    private suspend fun ensurePrepared(mode: CommandMode) {
        if (mode != CommandMode.DEFAULT) return
        owner?.let { browser ->
            prepareMutex.withLock {
                if (browser.config.expert) prepareExpert()
                if (browser.config.headless) prepareHeadless()
            }
        }
    }

    @InternalCdpApi
    override suspend fun callCommand(method: String, parameter: JsonElement?, mode: CommandMode): JsonElement? {
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
            connect()
            // Restore + prepare the (possibly freshly reconnected) session *before* sending on it, so
            // the command always runs against a fully re-established session — this covers a reconnect
            // that happens here in the retry loop, not just one detected before it.
            //
            // Restore MUST run before (and outside of) ensurePrepared: prepare* issue their own inner
            // commands, which would otherwise trigger the restore while prepareMutex is held, and a
            // restorer's DEFAULT command re-entering the non-reentrant prepareMutex would deadlock.
            // Running restore first is also correct precedence-wise — a restorer's own DEFAULT command
            // re-runs prepare internally, so e.g. a user user-agent override still lands last.
            if (needsRestore) runReconnectRestores()
            ensurePrepared(mode)
            // Fetch the transport only now: prepare/restore issue their own commands, which may have
            // reconnected, so a transport captured before them could be the stale, disposed one.
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
                false
            } catch (e: Exception) {
                sendError = e
                false
            }
            if (!sent) {
                // Treat the failed send as this transport going away: mark it for reconnect and fail
                // any *other* commands in flight on it (ISSUE-3), but only if it is still the current
                // transport — a send that fails late on an already-replaced transport must not tear
                // down the healthy one that superseded it. onTransportGone clears our own just-added
                // waiter too; we resend on the next iteration.
                onTransportGone(transport, ConnectionClosedException(cause = sendError))
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
        // Every send attempt failed: the connection is gone. Surface it as a ConnectionClosedException
        // (never the raw send failure, which may be a CancellationException a caller would mistake for
        // its own cancellation), keeping the original send error as the cause.
        throw ConnectionClosedException(cause = sendError)
    }

    @InternalCdpApi
    override suspend fun close() {
        // Take connectMutex so close is ordered against an in-progress (re)connect: without it, a
        // concurrent connect() could reassign `transport`/`socketSubscription` right after we cleared
        // them, resurrecting a live socket and receive loop that outlive close().
        connectMutex.withLock {
            closed = true
            transport?.close()
            transport = null
            socketSubscription?.cancel()
            socketSubscription = null
        }
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
