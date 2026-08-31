package dev.kdriver.core.connection

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.Serialization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * A socket that is still open but whose receive loop has died is not a healthy connection: nobody
 * delivers replies any more, so commands sent on it can only time out.
 *
 * The loop runs in a scope handed in from outside, so its lifetime is not the connection's to
 * control — it can be cancelled without the socket ever closing. That leaves the transport
 * reporting `isActive`, no disconnect ever observed, and every later command waiting on its own
 * timeout for an answer that cannot arrive.
 */
class ReceiveLoopLivenessTest {

    /** Answers every request immediately, so a live receive loop is the only thing in question. */
    private class EchoTransport : WebSocketTransport {
        private val channel = Channel<String>(Channel.RENDEZVOUS)
        override var isActive: Boolean = false
            private set

        override suspend fun connect() {
            isActive = true
        }

        override suspend fun send(message: String) {
            val id = Serialization.json.parseToJsonElement(message).jsonObject["id"]!!.jsonPrimitive.long
            channel.send("""{"id":$id,"result":{"value":1}}""")
        }

        override fun incoming(): Flow<String> = channel.receiveAsFlow()

        override suspend fun close() {
            isActive = false
            channel.close()
        }
    }

    private class TestConnection(
        scope: CoroutineScope,
        private val transports: MutableList<EchoTransport>,
    ) : DefaultConnection("ws://stub/devtools/page/stub", scope) {
        override fun createTransport(): WebSocketTransport = EchoTransport().also { transports += it }
        override suspend fun updateTarget() = Unit
    }

    @Test
    fun command_afterTheReceiveLoopWasCancelled_reconnectsInsteadOfTimingOut() =
        runTest(StandardTestDispatcher()) {
            val transports = mutableListOf<EchoTransport>()
            // A scope of its own, so it can be cancelled the way an owner's scope would be.
            val listeningScope = CoroutineScope(coroutineContext + Job())
            val connection = TestConnection(listeningScope, transports)

            // First command opens the socket and starts the receive loop.
            val first = withTimeoutOrNull(5_000) {
                connection.callCommand("Some.method", null, CommandMode.ONE_SHOT)
            }
            assertNotNull(first, "the connection should work before its receive loop is cancelled")

            // The loop dies, the socket does not: this is the state that used to go unnoticed.
            listeningScope.coroutineContext[Job]!!.cancelChildren()
            runCurrent()

            val second = withTimeoutOrNull(5_000) {
                connection.callCommand("Some.method", null, CommandMode.ONE_SHOT)
            }

            assertNotNull(second, "a socket nobody reads must be replaced, not reused")
            assertEquals(2, transports.size, "the dead connection should have been re-established")

            connection.close()
            listeningScope.coroutineContext[Job]!!.cancel()
        }

    /** A healthy connection must not be torn down and rebuilt on every command. */
    @Test
    fun consecutiveCommands_reuseTheSameLiveConnection() = runTest(StandardTestDispatcher()) {
        val transports = mutableListOf<EchoTransport>()
        val listeningScope = CoroutineScope(coroutineContext + Job())
        val connection = TestConnection(listeningScope, transports)

        repeat(3) {
            assertNotNull(
                withTimeoutOrNull(5_000) {
                    connection.callCommand("Some.method", null, CommandMode.ONE_SHOT)
                },
                "command $it should have been answered",
            )
        }

        assertEquals(1, transports.size, "a live connection must be reused, not rebuilt each time")

        connection.close()
        listeningScope.coroutineContext[Job]!!.cancel()
    }
}
