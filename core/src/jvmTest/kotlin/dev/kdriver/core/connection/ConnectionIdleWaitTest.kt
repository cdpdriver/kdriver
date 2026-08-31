package dev.kdriver.core.connection

import dev.kdriver.cdp.CommandMode
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Covers [DefaultConnection.wait]'s idle detection.
 *
 * `wait()` does not wait for a *result*: it waits for the page to settle. A page that never settles
 * is therefore not an error — the caller should carry on. Two properties matter:
 *
 * 1. it must always hand control back, even on a page that never goes quiet;
 * 2. "quiet" must be a fact about the connection, not about whichever 100 ms window a poll happened
 *    to open.
 */
class ConnectionIdleWaitTest {

    private class FakeTransport : WebSocketTransport {
        private val channel = Channel<String>(Channel.RENDEZVOUS)
        override var isActive: Boolean = false
            private set

        override suspend fun connect() {
            isActive = true
        }

        override suspend fun send(message: String) = Unit
        override fun incoming(): Flow<String> = channel.receiveAsFlow()
        suspend fun deliver(frame: String) = channel.send(frame)

        override suspend fun close() {
            isActive = false
            channel.close()
        }
    }

    /**
     * `updateTarget()` is stubbed out: it issues a CDP command, which is not what these tests are
     * about, and a fake transport that answers nothing would hang there rather than in the idle loop.
     */
    private class TestConnection(
        scope: TestScope,
        private val transport: FakeTransport,
    ) : DefaultConnection("ws://stub/devtools/page/stub", scope) {
        private val scheduler = scope.testScheduler
        override fun createTransport(): WebSocketTransport = transport
        override suspend fun updateTarget() = Unit

        /** Idleness is measured on the scheduler's clock, so these tests are not timing-dependent. */
        override fun currentTimeMillis(): Long = scheduler.currentTime

        /** Connecting is lazy, on the first command. The reply never comes and is not needed here. */
        suspend fun open() {
            withTimeoutOrNull(1) { callCommand("Stub.noop", null, CommandMode.ONE_SHOT) }
        }
    }

    /**
     * A page that never goes quiet must not trap the caller forever.
     *
     * The stream here is deliberately busier than the idle threshold, so the connection is never
     * idle by any definition. `wait()` must still return, bounded by its idle timeout.
     */
    @Test
    fun wait_returnsOnAPageThatNeverGoesQuiet() = runTest(StandardTestDispatcher()) {
        val transport = FakeTransport()
        val connection = TestConnection(this, transport)
        connection.open()

        val noise = launch {
            while (true) {
                transport.deliver("""{"method":"Network.dataReceived","params":{}}""")
                delay(50)
            }
        }
        // Let the first frame land, so the connection has actually seen traffic before we wait.
        runCurrent()

        try {
            val before = testScheduler.currentTime
            val returned = withTimeoutOrNull(60_000) { connection.wait(idleTimeout = 10_000) }
            val spent = testScheduler.currentTime - before

            assertNotNull(returned, "wait() must give control back on a page that never settles")
            // Right at the bound: any sooner would mean the traffic went unnoticed and the
            // connection was mistaken for idle.
            assertTrue(
                spent >= 10_000,
                "traffic kept arriving, so wait() should have watched for the full bound, not ${spent}ms",
            )
        } finally {
            noise.cancelAndJoin()
            connection.close()
        }
    }

    /**
     * On a silent connection idleness is already a fact — nothing has arrived. `wait()` should say
     * so at once instead of spending a polling window rediscovering it.
     */
    @Test
    fun wait_onASilentConnection_doesNotSpendAPollingWindow() = runTest(StandardTestDispatcher()) {
        val transport = FakeTransport()
        val connection = TestConnection(this, transport)
        connection.open()

        val before = testScheduler.currentTime
        connection.wait()
        val spent = testScheduler.currentTime - before

        assertTrue(
            spent < 100,
            "a silent connection is idle by definition; wait() spent ${spent}ms rediscovering it",
        )
        connection.close()
    }

    /**
     * `t` is a floor, not a deadline. A connection that settles at once must still be left alone for
     * the requested time — otherwise callers asking for breathing room silently stop getting it.
     */
    @Test
    fun wait_withAMinimum_honoursItEvenWhenAlreadyIdle() = runTest(StandardTestDispatcher()) {
        val transport = FakeTransport()
        val connection = TestConnection(this, transport)
        connection.open()

        val before = testScheduler.currentTime
        connection.wait(t = 500)
        val spent = testScheduler.currentTime - before

        assertTrue(spent >= 500, "wait(500) returned after only ${spent}ms")
        connection.close()
    }
}
