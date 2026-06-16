package dev.kdriver.core.connection

import dev.kdriver.cdp.CommandMode
import dev.kdriver.core.exceptions.CommandTimeoutException
import kotlinx.coroutines.CompletableDeferred
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
 * A command whose response never arrives must fail with [CommandTimeoutException] once the
 * configured command timeout elapses, instead of hanging forever.
 */
class CommandTimeoutTest {

    private class SilentTransport : WebSocketTransport {
        private val channel = Channel<String>(Channel.UNLIMITED)
        override var isActive: Boolean = false
            private set

        override suspend fun connect() {
            isActive = true
        }

        // Never produces a response: the test relies on the timeout firing.
        override suspend fun send(message: String) {}

        override fun incoming(): Flow<String> = channel.receiveAsFlow()

        override suspend fun close() {
            isActive = false
            channel.close()
        }
    }

    @Test
    fun callCommand_failsWithCommandTimeout_whenNoResponse() = runTest(UnconfinedTestDispatcher()) {
        val transport = SilentTransport()
        val connection = object : DefaultConnection("ws://stub/devtools/page/stub", this) {
            override fun createTransport(): WebSocketTransport = transport
        }

        // No owner is attached, so the connection falls back to Config.Defaults.COMMAND_TIMEOUT.
        // runTest's virtual clock advances time automatically while the call is parked on
        // withTimeout, so the timeout fires without any real waiting.
        val outcome = CompletableDeferred<Throwable>()
        launch {
            try {
                connection.callCommand("Some.method", null, CommandMode.ONE_SHOT)
            } catch (e: Throwable) {
                outcome.complete(e)
            }
        }

        val error = withTimeout(60_000) { outcome.await() }
        val timeoutError = assertIs<CommandTimeoutException>(error)
        assertEquals("Some.method", timeoutError.method)

        // Stop the receive loop so runTest doesn't report it as a leaked coroutine.
        connection.close()
    }
}
