package dev.kdriver.core

import dev.kdriver.cdp.domain.DOM
import dev.kdriver.cdp.domain.Target
import dev.kdriver.core.connection.WebSocketTransport
import dev.kdriver.core.dom.DefaultElement
import dev.kdriver.core.tab.DefaultTab
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/**
 * A browser played by a script, for tests that count and order CDP round trips without a real Chrome.
 *
 * Every frame the connection sends is recorded in [sent] with the virtual time it left at; [answer] decides the reply:
 * a [Reply] is delivered after its [Reply.latency] (virtual time, so a test can model a slow link), `null` holds the
 * command until the test calls [reply] or [fail] itself.
 */
class ScriptedTransport(
    private val scope: TestScope,
    var answer: (Sent) -> Reply? = { Reply.ok() },
) : WebSocketTransport {

    /** A command as it left the client. */
    data class Sent(val id: Long, val method: String, val params: JsonObject?, val atMillis: Long)

    /** A reply: [result] JSON object, or a CDP [error] message; delivered [latency] ms after the send. */
    data class Reply(val result: String? = "{}", val error: String? = null, val latency: Long = 0) {
        companion object {
            fun ok(result: String = "{}", latency: Long = 0) = Reply(result, null, latency)
            fun error(message: String, latency: Long = 0) = Reply(null, message, latency)
        }
    }

    private val incoming = Channel<String>(Channel.UNLIMITED)
    val sent = mutableListOf<Sent>()
    override var isActive: Boolean = false
        private set

    override suspend fun connect() {
        isActive = true
    }

    override suspend fun send(message: String) {
        val o = Json.parseToJsonElement(message).jsonObject
        val s = Sent(
            id = o["id"]!!.jsonPrimitive.long,
            method = o["method"]!!.jsonPrimitive.content,
            params = o["params"] as? JsonObject,
            atMillis = scope.testScheduler.currentTime,
        )
        sent += s
        answer(s)?.let { r -> deliver(s.id, r) }
    }

    private fun deliver(id: Long, r: Reply) {
        val frame = if (r.error != null) {
            """{"id":$id,"error":{"code":-32000,"message":${Json.encodeToString(r.error)}}}"""
        } else {
            """{"id":$id,"result":${r.result}}"""
        }
        if (r.latency <= 0) incoming.trySend(frame)
        else scope.launch {
            delay(r.latency)
            incoming.trySend(frame)
        }
    }

    /** Answers a held command with [result]. */
    fun reply(id: Long, result: String = "{}") = deliver(id, Reply.ok(result))

    /** Answers a held command with a CDP error. */
    fun fail(id: Long, message: String) = deliver(id, Reply.error(message))

    /** Sends the browser event [method] with [params] (a JSON object), after anything already on its way. */
    fun event(method: String, params: String = "{}") {
        incoming.trySend("""{"method":${Json.encodeToString(method)},"params":$params}""")
    }

    /** The methods sent, in order. */
    val methods: List<String> get() = sent.map { it.method }

    override fun incoming(): Flow<String> = incoming.receiveAsFlow()

    override suspend fun close() {
        isActive = false
        incoming.close()
    }

}

/** A [DefaultTab] on a [ScriptedTransport], its idle clock on the test scheduler. */
open class ScriptedTab(
    private val scope: TestScope,
    val transport: ScriptedTransport,
) : DefaultTab("ws://stub/devtools/page/stub", scope, TARGET) {
    override fun createTransport(): WebSocketTransport = transport
    override fun currentTimeMillis(): Long = scope.testScheduler.currentTime

    companion object {
        val TARGET = Target.TargetInfo(
            targetId = "stub",
            type = "page",
            title = "",
            url = "about:blank",
            attached = true,
            canAccessOpener = false,
        )
        const val TARGET_INFO =
            """{"targetInfo":{"targetId":"stub","type":"page","title":"t","url":"https://example.test/",""" +
                """"attached":true,"canAccessOpener":false}}"""
    }
}

/** A plain element node for scripted tests. */
fun scriptedNode(nodeId: Int, backendNodeId: Int = nodeId, name: String = "INPUT", children: List<DOM.Node>? = null) =
    DOM.Node(
        nodeId = nodeId,
        backendNodeId = backendNodeId,
        nodeType = 1,
        nodeName = name,
        localName = name.lowercase(),
        nodeValue = "",
        childNodeCount = children?.size,
        children = children,
    )

/** A [DefaultElement] on [tab] for node [nodeId]. */
fun ScriptedTab.element(nodeId: Int = 7) = DefaultElement(this, scriptedNode(nodeId))

/** A `Runtime.callFunctionOn` / `Runtime.evaluate` reply carrying [value] (JSON). */
fun valueReply(value: String) = """{"result":{"type":"object","value":$value}}"""

/** A `DOM.resolveNode` reply for object [objectId]. */
fun objectReply(objectId: String) = """{"object":{"type":"object","objectId":"$objectId"}}"""

/** The `objectId` a `Runtime.callFunctionOn` was sent with. */
val ScriptedTransport.Sent.objectId: String? get() = params?.get("objectId")?.jsonPrimitive?.content

/** The `objectId` of every command sent with [method], in order. */
fun ScriptedTransport.objectIdsOf(method: String = "Runtime.callFunctionOn"): List<String?> =
    sent.filter { it.method == method }.map { it.objectId }

/** The methods sent at virtual time [atMillis]. */
fun ScriptedTransport.methodsSentAt(atMillis: Long): Set<String> =
    sent.filter { it.atMillis == atMillis }.map { it.method }.toSet()

/** A string param of a sent command. */
fun ScriptedTransport.Sent.param(name: String): String? = (params?.get(name) as? JsonElement)?.jsonPrimitive?.content
