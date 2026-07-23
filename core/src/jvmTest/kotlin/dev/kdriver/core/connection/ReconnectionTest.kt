package dev.kdriver.core.connection

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.Serialization
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.Config
import dev.kdriver.core.exceptions.CommandTimeoutException
import dev.kdriver.core.exceptions.ConnectionClosedException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.*

/**
 * Behaviour of [DefaultConnection] when the underlying transport disconnects and a later command
 * must transparently re-establish the connection.
 *
 * The production symptom (commands timing out at 30s across a session) points at a connection that
 * has gone away without the caller being able to recover. These tests pin down what happens at the
 * transport seam so we can reason about reconnection independently of a real browser.
 */
class ReconnectionTest {

    /**
     * A transport the test fully drives:
     *  - [respond] / [respondToLast] inject a CDP reply for an in-flight request,
     *  - [drop] simulates a dropped socket that still *reports* active (the production race): only the
     *    receive loop ending signals the disconnect,
     *  - [failSend] fails the next send like a dead Ktor session (cleared on reconnect); [keepFailingSend]
     *    keeps failing across reconnects,
     *  - [autoRespond] echoes a canned reply for every sent frame (used to drive the prepare* path).
     *
     * [connectCount] lets a test assert whether a *new* connection was opened (i.e. a reconnect).
     */
    private class ControllableTransport : WebSocketTransport {
        private var channel = Channel<String>(Channel.UNLIMITED)

        @Volatile
        override var isActive: Boolean = false
            private set

        var connectCount: Int = 0
            private set

        val sent = mutableListOf<String>()

        /** Fails the next send as a dropped Ktor session would; cleared on the next [connect]. */
        @Volatile
        var failSend: Boolean = false

        /** Like [failSend] but persists across reconnects, to exercise exhausted retries. */
        @Volatile
        var keepFailingSend: Boolean = false

        /** When true, every sent frame gets an immediate canned reply (drives the prepare* path). */
        @Volatile
        var autoRespond: Boolean = false

        override suspend fun connect() {
            connectCount++
            // A reconnect reuses this instance, so hand out a fresh channel for the new receive loop.
            if (channel.isClosedForSend) channel = Channel(Channel.UNLIMITED)
            failSend = false
            isActive = true
        }

        override suspend fun send(message: String) {
            // A send on a dropped Ktor session throws a channel CancellationException.
            if (failSend || keepFailingSend) throw CancellationException("Channel was cancelled")
            sent.add(message)
            if (autoRespond) {
                val request = Serialization.json.parseToJsonElement(message).jsonObject
                val id = request["id"]!!.jsonPrimitive.long
                val method = request["method"]!!.jsonPrimitive.content
                // Runtime.evaluate must parse into an EvaluateReturn with a value so prepareHeadless
                // completes (and sets its done flag); anything else just needs a well-formed reply.
                val result = if (method == "Runtime.evaluate")
                    """{"result":{"type":"string","value":"HeadlessChrome/1.0"}}"""
                else "{}"
                channel.trySend("""{"id":$id,"result":$result}""")
            }
        }

        override fun incoming(): Flow<String> = channel.receiveAsFlow()

        override suspend fun close() {
            isActive = false
            channel.close()
        }

        /** Simulate a dropped socket that still reports active: only the receive loop ends. */
        fun drop() {
            channel.close()
        }

        /** Deliver a successful CDP reply for the request with the given [id]. */
        fun respond(id: Long, result: String = """{}""") {
            channel.trySend("""{"id":$id,"result":$result}""")
        }

        /** Deliver a successful CDP reply for the most recently sent request. */
        fun respondToLast(result: String = """{}""") {
            val id = Serialization.json.parseToJsonElement(sent.last())
                .jsonObject["id"]!!.jsonPrimitive.long
            respond(id, result)
        }
    }

    private class TestConnection(
        scope: CoroutineScope,
        private val transport: ControllableTransport,
    ) : DefaultConnection("ws://stub/devtools/page/stub", scope) {
        override fun createTransport(): WebSocketTransport = transport
    }

    /** Runs [block] and captures its thrown exception without failing the test scope. */
    private fun CoroutineScope.captureError(block: suspend () -> Unit): CompletableDeferred<Throwable> {
        val error = CompletableDeferred<Throwable>()
        launch {
            try {
                block()
            } catch (e: Throwable) {
                error.complete(e)
            }
        }
        return error
    }

    @Test
    fun reconnects_onNextCommand_afterCleanDrop() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport()
        val connection = TestConnection(this, transport)

        // First command opens the connection and succeeds.
        val first = async { connection.callCommand("A.first", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        assertNotNull(withTimeout(2_000) { first.await() })
        assertEquals(1, transport.connectCount)

        // The socket drops but keeps reporting active (the production race): only the receive loop
        // ending flags the disconnect. The next command must reconnect via that signal.
        transport.drop()

        val second = async { connection.callCommand("B.second", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        assertNotNull(withTimeout(2_000) { second.await() })
        assertEquals(2, transport.connectCount, "a drop must lead to a reconnect on the next command")

        connection.close()
    }

    @Test
    fun reconnectsAndResends_whenSendFailsOnDeadSocket() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport()
        val connection = TestConnection(this, transport)

        val first = async { connection.callCommand("A.first", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        assertNotNull(withTimeout(2_000) { first.await() })

        // The socket is dead but still reports active (the race window): the send fails. The command
        // must reconnect and resend transparently rather than surfacing the failure.
        transport.failSend = true
        val second = async { connection.callCommand("B.second", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        assertNotNull(withTimeout(2_000) { second.await() })
        assertEquals(2, transport.connectCount, "a failed send must trigger a reconnect and resend")

        connection.close()
    }

    @Test
    fun failsOtherInFlightCommand_whenASendTriggersReconnect() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport()
        val connection = TestConnection(this, transport)

        // Command A is sent and parks awaiting its reply.
        val aError = captureError { connection.callCommand("A.first", null, CommandMode.ONE_SHOT) }

        // Command B's send fails on the (now dead) socket. The reconnect it triggers must also fail A,
        // which is in flight on that same dead transport, instead of leaving it to hang to the timeout.
        transport.failSend = true
        val second = async { connection.callCommand("B.second", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()

        assertNotNull(withTimeout(2_000) { second.await() })
        assertIs<ConnectionClosedException>(withTimeout(2_000) { aError.await() })
        assertEquals(2, transport.connectCount)

        connection.close()
    }

    @Test
    fun doesNotResend_afterAnAwaitedReplyDies() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport()
        val connection = TestConnection(this, transport)

        // Command is sent (successfully) and parks awaiting its reply — the resend-unsafe window.
        val error = captureError { connection.callCommand("A.first", null, CommandMode.ONE_SHOT) }

        // The socket dies while awaiting. The command must surface the failure, NOT resend (it may
        // already have executed).
        transport.drop()

        assertIs<ConnectionClosedException>(withTimeout(2_000) { error.await() })
        assertEquals(1, transport.connectCount, "a command must not resend once it is awaiting a reply")

        connection.close()
    }

    @Test
    fun throwsConnectionClosed_whenAllSendAttemptsFail() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport().apply { keepFailingSend = true }
        val connection = TestConnection(this, transport)

        val error = captureError { connection.callCommand("A.first", null, CommandMode.ONE_SHOT) }

        // Bounded retries: initial connect + one reconnect attempt, then give up with a clear error
        // (never the raw CancellationException from the failed send).
        assertIs<ConnectionClosedException>(withTimeout(2_000) { error.await() })
        assertEquals(2, transport.connectCount)

        connection.close()
    }

    @Test
    fun callCommandAfterClose_throwsAndDoesNotReconnect() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport()
        val connection = TestConnection(this, transport)

        val first = async { connection.callCommand("A.first", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        assertNotNull(withTimeout(2_000) { first.await() })

        connection.close()

        val error = captureError { connection.callCommand("B.second", null, CommandMode.ONE_SHOT) }
        assertIs<ConnectionClosedException>(withTimeout(2_000) { error.await() })
        assertEquals(1, transport.connectCount, "close is terminal: no silent reconnect afterwards")
    }

    @Test
    fun replaysPreparation_afterReconnect() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport().apply { autoRespond = true }
        // A headless browser triggers prepareHeadless (a Runtime.evaluate + Network.setUserAgentOverride).
        val config = mockk<Config>(relaxed = true)
        every { config.headless } returns true
        every { config.expert } returns false
        val browser = mockk<Browser>(relaxed = true)
        every { browser.config } returns config
        val connection = TestConnection(this, transport).also { it.owner = browser }

        fun evaluateCount() = transport.sent.count { it.contains("Runtime.evaluate") }

        // First DEFAULT command runs the preparation once.
        connection.callCommand("Some.method", null, CommandMode.DEFAULT)
        assertEquals(1, evaluateCount())

        // While still connected, preparation is not repeated.
        connection.callCommand("Some.method", null, CommandMode.DEFAULT)
        assertEquals(1, evaluateCount(), "preparation must not repeat while the session is alive")

        // After a reconnect the new CDP session has lost the preparation, so it must be re-applied.
        transport.drop()
        connection.callCommand("Some.method", null, CommandMode.DEFAULT)
        assertEquals(2, evaluateCount(), "preparation must be replayed on the new session after a reconnect")

        connection.close()
    }

    @Test
    fun runsRegisteredRestore_oncePerReconnect() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport()
        val connection = TestConnection(this, transport)
        var restoreCount = 0
        connection.registerReconnectRestore("k") { restoreCount++ }

        // A command that just connects (no reconnect) must not run the restore.
        val c1 = async { connection.callCommand("A", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        c1.await()
        assertEquals(0, restoreCount, "restore must not run without a reconnect")

        // A command after a drop reconnects and runs the restore exactly once.
        transport.drop()
        val c2 = async { connection.callCommand("B", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        c2.await()
        assertEquals(1, restoreCount)

        // A further command while still connected does not run it again.
        val c3 = async { connection.callCommand("C", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        c3.await()
        assertEquals(1, restoreCount, "restore runs once per reconnect, not once per command")

        // A second drop reconnects again and re-runs the restore.
        transport.drop()
        val c4 = async { connection.callCommand("D", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        c4.await()
        assertEquals(2, restoreCount)

        connection.close()
    }

    @Test
    fun restoreThatIssuesCommands_runsAfterReconnect() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport().apply { autoRespond = true }
        val connection = TestConnection(this, transport)
        connection.registerReconnectRestore("k") {
            connection.callCommand("Restore.enable", null, CommandMode.ONE_SHOT)
        }

        connection.callCommand("A", null, CommandMode.ONE_SHOT)
        assertEquals(0, transport.sent.count { it.contains("Restore.enable") })

        transport.drop()
        connection.callCommand("B", null, CommandMode.ONE_SHOT)
        assertEquals(
            1,
            transport.sent.count { it.contains("Restore.enable") },
            "a restorer that re-issues a CDP command must run it once on the reconnected session",
        )

        connection.close()
    }

    @Test
    fun doesNotRun_unregisteredRestore_afterReconnect() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport()
        val connection = TestConnection(this, transport)
        var restoreCount = 0
        connection.registerReconnectRestore("k") { restoreCount++ }

        val c1 = async { connection.callCommand("A", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        c1.await()

        connection.unregisterReconnectRestore("k")

        transport.drop()
        val c2 = async { connection.callCommand("B", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        c2.await()
        assertEquals(0, restoreCount, "an unregistered restore must not run")

        connection.close()
    }

    @Test
    fun replaysPreparationAndRestore_afterReconnect_withoutDeadlock() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport().apply { autoRespond = true }
        // Headless browser => prepareHeadless runs (a Runtime.evaluate under prepareMutex).
        val config = mockk<Config>(relaxed = true)
        every { config.headless } returns true
        every { config.expert } returns false
        val browser = mockk<Browser>(relaxed = true)
        every { browser.config } returns config
        val connection = TestConnection(this, transport).also { it.owner = browser }

        var restoreCount = 0
        // A restorer that issues a DEFAULT command: this is the combination that used to deadlock, as
        // prepare's own (ONE_SHOT) command would trigger the restore while prepareMutex was held, and
        // the restorer's DEFAULT command then re-entered the non-reentrant prepareMutex.
        connection.registerReconnectRestore("k") {
            connection.callCommand("Restore.enable", null, CommandMode.DEFAULT)
            restoreCount++
        }

        connection.callCommand("Some.method", null, CommandMode.DEFAULT)
        assertEquals(1, transport.sent.count { it.contains("Runtime.evaluate") })
        assertEquals(0, restoreCount)

        transport.drop()
        // Must complete (no deadlock): re-prepares the new session AND runs the restorer.
        withTimeout(5_000) { connection.callCommand("Some.method", null, CommandMode.DEFAULT) }
        assertEquals(
            2,
            transport.sent.count { it.contains("Runtime.evaluate") },
            "preparation replayed after reconnect"
        )
        assertEquals(1, restoreCount, "restorer ran after reconnect")
        assertTrue(transport.sent.any { it.contains("Restore.enable") }, "the restorer's DEFAULT command was sent")

        connection.close()
    }

    @Test
    fun reusesConnection_afterHalfOpenTimeout() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport()
        val connection = TestConnection(this, transport)

        // Half-open socket: the command is sent but no reply ever comes and the socket never drops.
        // With no owner attached, the connection falls back to Config.Defaults.COMMAND_TIMEOUT; the
        // virtual clock advances it instantly.
        val first = captureError { connection.callCommand("A.first", null, CommandMode.ONE_SHOT) }
        assertIs<CommandTimeoutException>(withTimeout(60_000) { first.await() })

        // Characterise the seam: on a half-open socket the transport still *looks* active and its
        // receive loop never ends, so nothing flags a disconnect — the next command reuses the same
        // socket and times out again. DefaultConnection alone cannot recover here; detection would
        // require the transport itself to report the disconnect (e.g. a keep-alive ping), which the
        // current build does not do, so this case is only bounded by the command timeout.
        val second = captureError { connection.callCommand("B.second", null, CommandMode.ONE_SHOT) }
        assertIs<CommandTimeoutException>(withTimeout(60_000) { second.await() })
        assertEquals(1, transport.connectCount, "no reconnect after a half-open timeout")

        connection.close()
    }
}
