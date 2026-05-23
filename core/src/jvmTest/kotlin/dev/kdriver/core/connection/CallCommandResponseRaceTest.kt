package dev.kdriver.core.connection

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.Serialization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Reproduces the `callCommand` response race (audit ISSUE-1).
 *
 * The browser's reply can arrive between `send` and the point where `callCommand` starts listening
 * for it. Because responses were delivered through a `replay = 0` shared flow, a reply emitted
 * while no collector is subscribed is dropped and `callCommand` waits forever.
 *
 * [FakeTransport] delivers the response **synchronously inside `send()`** — i.e. exactly in that
 * window, before `callCommand` resumes to await it. On [UnconfinedTestDispatcher] the receive loop
 * processes the delivered frame eagerly during `send`, making the interleaving deterministic.
 */
class CallCommandResponseRaceTest {

    /**
     * Fake transport whose [send] hook lets the test inject a frame at send time. Incoming frames
     * are delivered through a rendezvous channel so the connection's receive loop consumes them
     * cooperatively.
     */
    private class FakeTransport : WebSocketTransport {
        private val channel = Channel<String>(Channel.RENDEZVOUS)
        override var isActive: Boolean = false
            private set
        var onSend: (suspend (String) -> Unit)? = null

        override suspend fun connect() {
            isActive = true
        }

        override suspend fun send(message: String) {
            onSend?.invoke(message)
        }

        override fun incoming(): Flow<String> = channel.receiveAsFlow()

        suspend fun deliver(frame: String) = channel.send(frame)

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
    fun callCommand_receivesResponseDeliveredAtSendTime() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeTransport()
        val connection = TestConnection(this, transport)

        // The browser "replies" the instant the request is sent — the worst-case window.
        transport.onSend = { sent ->
            val id = Serialization.json.parseToJsonElement(sent).jsonObject["id"]!!.jsonPrimitive.long
            transport.deliver("""{"id":$id,"result":{"value":42}}""")
        }

        try {
            val result = withTimeout(2_000) {
                connection.callCommand("Some.method", null, CommandMode.ONE_SHOT)
            }

            assertNotNull(result, "callCommand must return the response that arrived at send time")
            assertEquals(42, result.jsonObject["value"]!!.jsonPrimitive.int)
        } finally {
            connection.close()
        }
    }
}
