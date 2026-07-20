package dev.kdriver.core.connection

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.Serialization
import dev.kdriver.core.exceptions.CommandTimeoutException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

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
     *  - [respondTo] / [respond] inject a CDP reply for an in-flight request,
     *  - [drop] simulates a clean socket close (FIN): `incoming()` completes, [isActive] flips false,
     *  - going silent (never responding, never dropping) simulates a half-open socket.
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

        /** When true, [send] fails as a dead Ktor session would; cleared on the next [connect]. */
        @Volatile
        var failSend: Boolean = false

        override suspend fun connect() {
            connectCount++
            // A reconnect reuses this instance, so hand out a fresh channel for the new receive loop.
            if (channel.isClosedForSend) channel = Channel(Channel.UNLIMITED)
            failSend = false
            isActive = true
        }

        override suspend fun send(message: String) {
            // A send on a dropped Ktor session throws a channel CancellationException.
            if (failSend) throw CancellationException("Channel was cancelled")
            sent.add(message)
        }

        override fun incoming(): Flow<String> = channel.receiveAsFlow()

        override suspend fun close() {
            isActive = false
            channel.close()
        }

        /** Simulate the socket dropping cleanly: the receive loop sees [incoming] complete. */
        fun drop() {
            isActive = false
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

    @Test
    fun reconnects_onNextCommand_afterCleanDrop() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport()
        val connection = TestConnection(this, transport)

        // First command opens the connection and succeeds.
        val first = async { connection.callCommand("A.first", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        assertNotNull(withTimeout(2_000) { first.await() })
        assertEquals(1, transport.connectCount)

        // The socket drops cleanly.
        transport.drop()

        // The next command must transparently re-open the transport and succeed.
        val second = async { connection.callCommand("B.second", null, CommandMode.ONE_SHOT) }
        transport.respondToLast()
        assertNotNull(withTimeout(2_000) { second.await() })
        assertEquals(2, transport.connectCount, "a clean drop must lead to a reconnect on next command")

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
    fun reusesConnection_afterHalfOpenTimeout() = runTest(UnconfinedTestDispatcher()) {
        val transport = ControllableTransport()
        val connection = TestConnection(this, transport)

        // Half-open socket: the command is sent but no reply ever comes and the socket never drops.
        // With no owner attached, the connection falls back to Config.Defaults.COMMAND_TIMEOUT; the
        // virtual clock advances it instantly.
        val first = CompletableDeferred<Throwable>()
        launch {
            try {
                connection.callCommand("A.first", null, CommandMode.ONE_SHOT)
            } catch (e: Throwable) {
                first.complete(e)
            }
        }
        assertIs<CommandTimeoutException>(withTimeout(60_000) { first.await() })

        // Characterise the seam: on a half-open socket the transport still *looks* active, so the
        // next command reuses the same (dead) socket and times out again. DefaultConnection alone
        // cannot recover here -- detection has to come from the transport reporting a disconnect.
        // That is what the WebSocket keep-alive ping does in production (KtorWebSocketTransport): a
        // missing pong closes the session, turning this case into reconnects_onNextCommand_afterCleanDrop.
        val second = CompletableDeferred<Throwable>()
        launch {
            try {
                connection.callCommand("B.second", null, CommandMode.ONE_SHOT)
            } catch (e: Throwable) {
                second.complete(e)
            }
        }
        assertIs<CommandTimeoutException>(withTimeout(60_000) { second.await() })
        assertEquals(1, transport.connectCount, "current behaviour: no reconnect after a timeout")

        connection.close()
    }
}
