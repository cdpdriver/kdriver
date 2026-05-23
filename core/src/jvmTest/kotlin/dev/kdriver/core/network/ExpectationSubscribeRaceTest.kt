package dev.kdriver.core.network

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.Serialization
import dev.kdriver.cdp.domain.Network
import dev.kdriver.cdp.domain.Target
import dev.kdriver.core.connection.WebSocketTransport
import dev.kdriver.core.tab.DefaultTab
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Reproduces the expectation subscribe-after-enable race (audit ISSUE-2).
 *
 * `BaseRequestExpectation.setup()` must subscribe to the network event flows *before* the events
 * it cares about can fire. The buggy version enabled the Network domain first and only then
 * launched the collectors, so an event arriving in that window was dropped and `getRequestEvent()`
 * waited forever.
 *
 * The fake transport delivers a `Network.requestWillBeSent` event **while handling the
 * `Network.enable` command** — i.e. exactly while the domain is being enabled. If the collectors
 * were subscribed before enabling (the fix), the event is captured; otherwise it is lost.
 */
class ExpectationSubscribeRaceTest {

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

    private class TestTab(
        scope: CoroutineScope,
        private val transport: FakeTransport,
    ) : DefaultTab("ws://stub/devtools/page/stub", scope, STUB_TARGET) {
        override fun createTransport(): WebSocketTransport = transport
    }

    private fun requestWillBeSentFrame(requestId: String): String {
        val params = Network.RequestWillBeSentParameter(
            requestId = requestId,
            loaderId = "loader-1",
            documentURL = "https://example.com",
            request = Network.Request(
                url = "https://example.com",
                method = "GET",
                headers = emptyMap(),
                initialPriority = Network.ResourcePriority.MEDIUM,
                referrerPolicy = "no-referrer",
            ),
            timestamp = 0.0,
            wallTime = 0.0,
            initiator = Network.Initiator(type = "other"),
            redirectHasExtraInfo = false,
        )
        val paramsJson = Serialization.json.encodeToJsonElement(params)
        return """{"method":"Network.requestWillBeSent","params":$paramsJson}"""
    }

    @Test
    fun expect_capturesEventFiredWhileEnabling() = runTest(UnconfinedTestDispatcher()) {
        val transport = FakeTransport()
        val tab = TestTab(this, transport)

        transport.onSend = { sent ->
            val obj = Serialization.json.parseToJsonElement(sent).jsonObject
            val id = obj["id"]!!.jsonPrimitive.long
            val method = obj["method"]!!.jsonPrimitive.content
            if (method == "Network.enable") {
                // Fire the event in the exact window where the domain is being enabled.
                transport.deliver(requestWillBeSentFrame("req-1"))
            }
            transport.deliver("""{"id":$id,"result":{}}""")
        }

        try {
            val event = tab.expect(urlPattern = null) {
                withTimeout(2_000) { getRequestEvent() }
            }
            assertEquals("req-1", event.requestId)
        } finally {
            tab.close()
        }
    }

    private companion object {
        val STUB_TARGET = Target.TargetInfo(
            targetId = "stub",
            type = "page",
            title = "",
            url = "about:blank",
            attached = true,
            canAccessOpener = false,
        )
    }
}
