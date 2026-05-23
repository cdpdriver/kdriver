package dev.kdriver.core.connection

import dev.kdriver.cdp.CommandMode
import dev.kdriver.core.exceptions.ConnectionClosedException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Lifecycle behavior of [DefaultConnection]: failing in-flight commands on disconnect/close
 * (ISSUE-3) and not opening duplicate sessions under concurrent first commands (ISSUE-4).
 */
class ConnectionLifecycleTest {

    private class FakeTransport(
        /** When set, [connect] awaits this before marking the connection active. */
        private val connectGate: CompletableDeferred<Unit>? = null,
    ) : WebSocketTransport {
        private val channel = Channel<String>(Channel.UNLIMITED)
        override var isActive: Boolean = false
            private set
        var connectCount: Int = 0
            private set

        override suspend fun connect() {
            connectCount++
            connectGate?.await()
            isActive = true
        }

        override suspend fun send(message: String) {
            // No-op: the test controls when/whether a response or disconnect happens.
        }

        override fun incoming(): Flow<String> = channel.receiveAsFlow()

        /** Simulate the socket dropping (no graceful close()). */
        fun simulateDisconnect() = channel.close()

        override suspend fun close() {
            isActive = false
            channel.close()
        }
    }

    private class TestConnection(
        scope: CoroutineScope,
        private val transport: FakeTransport,
    ) : DefaultConnection("ws://stub/devtools/page/stub", scope) {
        override fun createTransport(): WebSocketTransport = transport
    }

    @Test
    fun callCommand_failsWithConnectionClosed_onDisconnect() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeTransport()
        val connection = TestConnection(this, transport)

        // Sends, then parks awaiting a reply that never comes. Capture the outcome here rather than
        // via async{}.await(), whose exception would also propagate to (and fail) the test scope.
        val outcome = CompletableDeferred<Throwable>()
        launch {
            try {
                connection.callCommand("Some.method", null, CommandMode.ONE_SHOT)
            } catch (e: Throwable) {
                outcome.complete(e)
            }
        }

        // The socket drops with the command still in flight.
        transport.simulateDisconnect()

        assertIs<ConnectionClosedException>(withTimeout(2_000) { outcome.await() })
    }

    @Test
    fun callCommand_failsWithConnectionClosed_onClose() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeTransport()
        val connection = TestConnection(this, transport)

        val outcome = CompletableDeferred<Throwable>()
        launch {
            try {
                connection.callCommand("Some.method", null, CommandMode.ONE_SHOT)
            } catch (e: Throwable) {
                outcome.complete(e)
            }
        }

        connection.close()

        assertIs<ConnectionClosedException>(withTimeout(2_000) { outcome.await() })
    }

    @Test
    fun connect_opensTransportOnce_underConcurrentFirstCommands() = runTest(UnconfinedTestDispatcher()) {
        val gate = CompletableDeferred<Unit>()
        val transport = FakeTransport(connectGate = gate)
        val connection = TestConnection(this, transport)

        // Two first commands race into connect() while it's still in progress (isActive == false).
        val c1 = launch { runCatching { connection.callCommand("A.x", null, CommandMode.ONE_SHOT) } }
        val c2 = launch { runCatching { connection.callCommand("B.y", null, CommandMode.ONE_SHOT) } }

        // Let the first connect complete; the second must reuse it, not open a new session.
        gate.complete(Unit)

        assertEquals(1, transport.connectCount, "connect() must open the transport exactly once")

        c1.cancel()
        c2.cancel()
        connection.close()
    }
}
