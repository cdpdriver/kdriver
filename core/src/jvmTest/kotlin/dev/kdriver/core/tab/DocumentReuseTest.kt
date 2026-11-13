@file:OptIn(dev.kdriver.cdp.InternalCdpApi::class)

package dev.kdriver.core.tab

import dev.kdriver.cdp.CDPException
import dev.kdriver.cdp.domain.dom
import dev.kdriver.core.ScriptedTab
import dev.kdriver.core.ScriptedTransport
import dev.kdriver.core.ScriptedTransport.Reply
import dev.kdriver.core.connection.WebSocketTransport
import dev.kdriver.core.dom.DefaultElement
import dev.kdriver.core.dom.NodeOrElement
import dev.kdriver.core.objectReply
import dev.kdriver.core.scriptedNode
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * A lookup on the whole document ([DefaultTab.select], [DefaultTab.selectAll], [DefaultTab.find],
 * [DefaultTab.findAll], [DefaultTab.querySelector], [DefaultTab.querySelectorAll], `Element.update`) reads the document
 * again only when the page may have changed since the last read: a `DOM.*` event, a command that makes the browser
 * forget its node ids, or a reconnect. Otherwise a new read would return the same tree, and the round trip (the whole
 * tree, often a megabyte) is saved.
 */
class DocumentReuseTest {

    private val document =
        """{"root":{"nodeId":1,"backendNodeId":1,"nodeType":9,"nodeName":"#document","localName":"","nodeValue":"",""" +
            """"childNodeCount":2,"children":[{"nodeId":7,"backendNodeId":70,"nodeType":1,"nodeName":"BUTTON",""" +
            """"localName":"button","nodeValue":"","attributes":["id","go"],"childNodeCount":1,"children":[""" +
            """{"nodeId":8,"parentId":7,"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"",""" +
            """"nodeValue":"Send"}]},""" +
            """{"nodeId":9,"backendNodeId":90,"nodeType":1,"nodeName":"LI","localName":"li","nodeValue":"",""" +
            """"childNodeCount":1,"children":[{"nodeId":10,"backendNodeId":100,"nodeType":3,"nodeName":"#text",""" +
            """"localName":"","nodeValue":"Item","parentId":9}]}]}}"""

    /**
     * A page with `<button id="go">Send</button><li>Item</li>`. [before] runs as each command arrives, before its
     * reply is queued, so an event it sends reaches the client ahead of that reply.
     */
    private fun page(before: (ScriptedTransport.Sent) -> Unit = {}): (ScriptedTransport.Sent) -> Reply = {
        before(it)
        when (it.method) {
            "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO, latency = 10)
            "DOM.getDocument" -> Reply.ok(document, latency = 10)
            "DOM.querySelector" -> Reply.ok("""{"nodeId":7}""", latency = 10)
            "DOM.querySelectorAll" -> Reply.ok("""{"nodeIds":[7,9]}""", latency = 10)
            "DOM.performSearch" -> Reply.ok("""{"searchId":"s1","resultCount":1}""", latency = 10)
            "DOM.getSearchResults" -> Reply.ok("""{"nodeIds":[8]}""", latency = 10)
            "DOM.resolveNode" -> Reply.ok(objectReply("obj-${it.id}"), latency = 10)
            else -> Reply.ok(latency = 10)
        }
    }

    private fun TestScope.scripted(
        before: (ScriptedTransport.Sent) -> Unit = {},
    ): Pair<ScriptedTab, ScriptedTransport> {
        val transport = ScriptedTransport(this, page(before))
        return ScriptedTab(this, transport) to transport
    }

    private val ScriptedTransport.documentReads get() = methods.count { it == "DOM.getDocument" }

    @Test
    fun select_onAnUnchangedPage_readsTheDocumentOnce() = runTest(StandardTestDispatcher()) {
        val (tab, transport) = scripted()

        val first = tab.select("#go", timeout = 1_000)
        val second = tab.select("#go", timeout = 1_000)

        assertEquals(1, transport.documentReads)
        assertEquals(2, transport.methods.count { it == "DOM.querySelector" })
        assertEquals("Send", second.text)
        assertEquals(first.backendNodeId, second.backendNodeId)
        assertSame(first.tree, second.tree, "both elements sit in the same, unchanged tree")
        tab.close()
    }

    @Test
    fun selectAll_findAll_andUpdate_shareTheDocumentOfAnUnchangedPage() = runTest(StandardTestDispatcher()) {
        val (tab, transport) = scripted()

        val all = tab.selectAll("button, li", timeout = 1_000)
        val found = tab.findAll("Send", timeout = 1_000)
        all.first().update()

        assertEquals(listOf("button", "li"), all.map { it.tag })
        assertEquals(listOf("button"), found.map { it.tag }, "a text node found is replaced by its parent")
        assertEquals(1, transport.documentReads)
        tab.close()
    }

    @Test
    fun aDomEventBetweenLookups_makesTheNextOneReadTheDocumentAgain() = runTest(StandardTestDispatcher()) {
        val (tab, transport) = scripted()

        tab.select("#go", timeout = 1_000)
        transport.event("DOM.attributeModified", """{"nodeId":7,"name":"class","value":"busy"}""")
        runCurrent()
        tab.select("#go", timeout = 1_000)

        assertEquals(2, transport.documentReads)
        tab.close()
    }

    @Test
    fun otherEvents_doNotMakeTheDocumentStale() = runTest(StandardTestDispatcher()) {
        val (tab, transport) = scripted()

        tab.select("#go", timeout = 1_000)
        transport.event("Network.dataReceived", """{"requestId":"r","timestamp":1,"dataLength":1}""")
        runCurrent()
        tab.select("#go", timeout = 1_000)

        assertEquals(1, transport.documentReads)
        tab.close()
    }

    @Test
    fun disablingTheDomAgent_makesTheNextLookupReadTheDocumentAgain() = runTest(StandardTestDispatcher()) {
        val (tab, transport) = scripted()

        tab.select("#go", timeout = 1_000)
        tab.dom.disable()
        tab.select("#go", timeout = 1_000)
        tab.find("Send", timeout = 1_000) // disables the agent when it is done
        tab.select("#go", timeout = 1_000)

        assertEquals(3, transport.documentReads, "the find used the document kept since the second select")
        tab.close()
    }

    @Test
    fun aDocumentReadByHand_makesTheNextLookupReadItAgain() = runTest(StandardTestDispatcher()) {
        val (tab, transport) = scripted()

        tab.select("#go", timeout = 1_000)
        tab.dom.getDocument(depth = 1)
        tab.select("#go", timeout = 1_000)

        assertEquals(3, transport.documentReads)
        tab.close()
    }

    @Test
    fun aChangeReportedWhileTheDocumentIsRead_keepsItForThatLookupOnly() = runTest(StandardTestDispatcher()) {
        var reads = 0
        lateinit var transport: ScriptedTransport
        val (tab, t) = scripted {
            if (it.method == "DOM.getDocument" && ++reads == 1) {
                transport.event("DOM.childNodeCountUpdated", """{"nodeId":1,"childNodeCount":2}""")
            }
        }
        transport = t

        assertEquals("Send", tab.select("#go", timeout = 1_000).text)
        tab.select("#go", timeout = 1_000)
        tab.select("#go", timeout = 1_000)

        assertEquals(2, transport.documentReads, "read again once, then kept")
        tab.close()
    }

    @Test
    fun aChangeReportedDuringALookupOnAKeptDocument_repeatsItOnAFreshRead() = runTest(StandardTestDispatcher()) {
        var queries = 0
        lateinit var transport: ScriptedTransport
        val (tab, t) = scripted {
            if (it.method == "DOM.querySelector" && ++queries == 2) {
                transport.event(
                    "DOM.childNodeInserted",
                    """{"parentNodeId":1,"previousNodeId":7,"node":{"nodeId":11,"backendNodeId":110,"nodeType":1,""" +
                        """"nodeName":"P","localName":"p","nodeValue":""}}""",
                )
            }
        }
        transport = t

        tab.select("#go", timeout = 1_000)
        val again = tab.select("#go", timeout = 1_000)

        assertEquals("Send", again.text)
        assertEquals(
            listOf("DOM.getDocument", "DOM.querySelector", "DOM.querySelector", "DOM.getDocument", "DOM.querySelector"),
            transport.methods.filter { it.startsWith("DOM.") },
        )
        tab.close()
    }

    @Test
    fun aChangeReportedDuringALookupOnAFreshRead_doesNotRepeatIt() = runTest(StandardTestDispatcher()) {
        lateinit var transport: ScriptedTransport
        val (tab, t) = scripted {
            if (it.method == "DOM.querySelector") {
                transport.event("DOM.attributeModified", """{"nodeId":7,"name":"a","value":"b"}""")
            }
        }
        transport = t

        tab.select("#go", timeout = 1_000)

        assertEquals(listOf("DOM.getDocument", "DOM.querySelector"), transport.methods.filter { it.startsWith("DOM.") })
        tab.close()
    }

    @Test
    fun aReconnect_makesTheNextLookupReadTheDocumentAgain() = runTest(StandardTestDispatcher()) {
        val scope = this
        val transports = mutableListOf<ScriptedTransport>()
        fun next(): ScriptedTransport {
            val t = ScriptedTransport(scope, page())
            transports += t
            return t
        }
        val tab = object : ScriptedTab(scope, next()) {
            private var opened = 0
            override fun createTransport(): WebSocketTransport = if (opened++ == 0) transports[0] else next()
        }

        tab.select("#go", timeout = 1_000)
        transports[0].close()
        runCurrent()
        tab.select("#go", timeout = 1_000)

        assertEquals(2, transports.size, "the second lookup ran on a new session")
        assertEquals(1, transports[1].documentReads)
        tab.close()
    }

    @Test
    fun anInvalidSelector_onAKeptDocument_stillThrowsTheBrowserError() = runTest(StandardTestDispatcher()) {
        val normal = page()
        val transport = ScriptedTransport(this) {
            if (it.method == "DOM.querySelector" && it.params.toString().contains("li[")) {
                Reply.error("DOM Error while querying", latency = 10)
            } else normal(it)
        }
        val tab = ScriptedTab(this, transport)

        tab.select("#go", timeout = 1_000)
        assertFailsWith<CDPException> { tab.select("li[", timeout = 0) }

        assertEquals(1, transport.documentReads, "the failing lookup used the kept document")
        assertTrue(transport.methods.contains("DOM.disable"), "and the agent is disabled on the error, as before")
        tab.close()
    }

    @Test
    fun update_onAnotherTab_readsTheDocument() = runTest(StandardTestDispatcher()) {
        val (tab, transport) = scripted()
        val wrapped: Tab = object : Tab by tab {}

        val element = DefaultElement(wrapped, scriptedNode(7, backendNodeId = 70, name = "BUTTON"))
        element.update()
        element.update()

        assertEquals(2, transport.documentReads, "only a DefaultTab keeps the document")
        assertEquals("Send", element.text)
        tab.close()
    }

    @Test
    fun update_withANodeGiven_readsNoDocument() = runTest(StandardTestDispatcher()) {
        val (tab, transport) = scripted()
        val button = scriptedNode(7, backendNodeId = 70, name = "BUTTON")
        val root = scriptedNode(1, name = "#document", children = listOf(button))

        val element = DefaultElement(tab, button)
        element.update(root)

        assertEquals(0, transport.documentReads)
        assertSame(root, element.tree)
        tab.close()
    }

    @Test
    fun aLookupUnderANode_queriesThatNode_orTheDocumentOfAFrame() = runTest(StandardTestDispatcher()) {
        val (tab, transport) = scripted()
        val frameDocument = scriptedNode(20, name = "#document")
        val frame = scriptedNode(21, name = "IFRAME").copy(contentDocument = frameDocument)
        val emptyFrame = scriptedNode(22, name = "IFRAME")
        val div = scriptedNode(23, name = "DIV")

        for (node in listOf(frame, emptyFrame, div)) {
            tab.querySelector("p", NodeOrElement.WrappedNode(node))
            tab.querySelectorAll("p", NodeOrElement.WrappedNode(node))
        }

        assertEquals(0, transport.documentReads, "a lookup under a node never reads the whole document")
        assertEquals(
            listOf(20, 20, 22, 22, 23, 23),
            transport.sent.filter { it.method.startsWith("DOM.querySelector") }
                .map { it.params!!["nodeId"]!!.jsonPrimitive.int },
        )
        tab.close()
    }
}
