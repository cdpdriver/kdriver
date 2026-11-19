@file:OptIn(dev.kdriver.cdp.InternalCdpApi::class)

package dev.kdriver.core.tab

import dev.kdriver.core.ScriptedTab
import dev.kdriver.core.ScriptedTransport
import dev.kdriver.core.ScriptedTransport.Reply
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.Config
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.objectIdsOf
import dev.kdriver.core.param
import dev.kdriver.core.valueReply
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * With [Config.lookupsInPage], `select`, `selectAll`, `querySelector` and `querySelectorAll` without a node ask the page
 * (`Runtime.evaluate`) and describe the matches (`DOM.describeNode`) instead of reading the whole document; without it
 * they read the document as before.
 */
class LookupsInPageTest {

    private val button =
        """{"node":{"nodeId":0,"backendNodeId":70,"nodeType":1,"nodeName":"BUTTON","localName":"button",""" +
            """"nodeValue":"","attributes":["id","go"],"childNodeCount":1,"children":[{"nodeId":0,""" +
            """"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"","nodeValue":"Send"}]}}"""

    private fun item(backendNodeId: Int, text: String) =
        """{"node":{"nodeId":0,"backendNodeId":$backendNodeId,"nodeType":1,"nodeName":"LI","localName":"li",""" +
            """"nodeValue":"","childNodeCount":1,"children":[{"nodeId":0,"backendNodeId":${backendNodeId + 1},""" +
            """"nodeType":3,"nodeName":"#text","localName":"","nodeValue":"$text"}]}}"""

    private val nothing = """{"result":{"type":"object","subtype":"null","value":null}}"""

    private fun found(objectId: String, subtype: String = "node") =
        """{"result":{"type":"object","subtype":"$subtype","objectId":"$objectId"}}"""

    private val document =
        """{"root":{"nodeId":1,"backendNodeId":1,"nodeType":9,"nodeName":"#document","localName":"","nodeValue":"",""" +
            """"childNodeCount":1,"children":[{"nodeId":7,"backendNodeId":70,"nodeType":1,"nodeName":"BUTTON",""" +
            """"localName":"button","nodeValue":"","attributes":["id","go"],"childNodeCount":1,"children":[""" +
            """{"nodeId":8,"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"","nodeValue":"Send"}]}]}}"""

    private fun TestScope.tab(lookupsInPage: Boolean, answer: (ScriptedTransport.Sent) -> Reply): ScriptedTab {
        val config = mockk<Config>(relaxed = true)
        every { config.lookupsInPage } returns lookupsInPage
        every { config.commandTimeout } returns Config.Defaults.COMMAND_TIMEOUT
        val browser = mockk<Browser>(relaxed = true)
        every { browser.config } returns config
        return ScriptedTab(this, ScriptedTransport(this, answer)).also { it.owner = browser }
    }

    private val ScriptedTab.sent get() = transport.methods

    @Test
    fun select_asksThePage_andDescribesTheMatch_withoutReadingTheDocument() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(button)
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }

        val element = tab.select("  #go ", timeout = 1_000)

        assertEquals("button", element.tag)
        assertEquals("Send", element.text)
        assertEquals("go", element["id"])
        assertEquals(0, element.node.nodeId, "no node id: no document was requested")
        assertNull(element.tree)
        assertEquals(listOf("Runtime.evaluate", "DOM.describeNode"), tab.sent.filter { it != "Target.getTargetInfo" })
        val evaluate = tab.transport.sent.first { it.method == "Runtime.evaluate" }
        assertEquals("document.querySelector(\"#go\")", evaluate.param("expression"))
        val describe = tab.transport.sent.first { it.method == "DOM.describeNode" }
        assertEquals("obj-1", describe.param("objectId"))
        assertEquals("-1", describe.param("depth"))
        assertEquals("true", describe.param("pierce"))
        tab.close()
    }

    @Test
    fun select_firstCallUsesTheObjectOfTheLookup_withoutResolvingTheNode() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(button)
                "Runtime.callFunctionOn" -> Reply.ok(valueReply("null"))
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }

        tab.select("#go", timeout = 1_000).click()

        assertEquals(listOf("obj-1"), tab.transport.objectIdsOf())
        assertTrue("DOM.resolveNode" !in tab.sent)
        tab.close()
    }

    @Test
    fun select_resolvesTheNodeAgain_whenTheObjectOfTheLookupIsStale() = runTest(StandardTestDispatcher()) {
        var calls = 0
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(button)
                "Runtime.callFunctionOn" ->
                    if (calls++ == 0) Reply.error("Could not find object with given id") else Reply.ok(valueReply("null"))
                "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","objectId":"obj-2"}}""")
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }

        tab.select("#go", timeout = 1_000).click()

        assertEquals(listOf("obj-1", "obj-2"), tab.transport.objectIdsOf())
        assertEquals("70", tab.transport.sent.single { it.method == "DOM.resolveNode" }.param("backendNodeId"))
        tab.close()
    }

    @Test
    fun select_triesAgain_whileThePageHasNoMatch_thenTimesOut() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(nothing)
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }

        assertFailsWith<TimeoutWaitingForElementException> { tab.select("#missing", timeout = 1_200) }

        assertTrue(tab.sent.count { it == "Runtime.evaluate" } >= 3, "polled every 500 ms")
        assertTrue("DOM.describeNode" !in tab.sent && "DOM.getDocument" !in tab.sent)
        tab.close()
    }

    @Test
    fun select_triesAgain_whenTheMatchIsGoneBeforeItIsDescribed() = runTest(StandardTestDispatcher()) {
        var describes = 0
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-${describes + 1}"))
                "DOM.describeNode" ->
                    if (describes++ == 0) Reply.error("Could not find object with given id") else Reply.ok(button)
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }

        val element = tab.select("#go", timeout = 5_000)

        assertEquals("button", element.tag)
        assertEquals(2, tab.sent.count { it == "Runtime.evaluate" })
        tab.close()
    }

    @Test
    fun select_withAnInvalidSelector_throwsThePageError() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(
                    """{"result":{"type":"object","subtype":"error","objectId":"err"},"exceptionDetails":""" +
                        """{"exceptionId":1,"text":"Uncaught","lineNumber":0,"columnNumber":9,"exception":""" +
                        """{"type":"object","subtype":"error","description":"SyntaxError: 'li[' is not a valid selector."}}}"""
                )
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }

        val error = assertFailsWith<EvaluateException> { tab.select("li[", timeout = 0) }

        assertTrue(error.jsError.startsWith("SyntaxError"), error.jsError)
        tab.close()
    }

    @Test
    fun querySelector_passesTheSelectorAsOneJsonStringLiteral() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) { Reply.ok(nothing) }
        val selector = "a[title=\"x\\\") + alert(1) + (\"\n\u2028\"]"

        assertNull(tab.querySelector(selector))

        val expression = tab.transport.sent.single().param("expression")!!
        assertEquals(
            "document.querySelector(\"a[title=\\\"x\\\\\\\") + alert(1) + (\\\"\\n\u2028\\\"]\")",
            expression,
        )
        tab.close()
    }

    @Test
    fun querySelectorAll_withNoMatch_costsOneRoundTrip() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) { Reply.ok(nothing) }

        assertEquals(emptyList(), tab.querySelectorAll(".item"))

        assertEquals(listOf("Runtime.evaluate"), tab.sent)
        assertEquals(
            "(() => { const all = Array.from(document.querySelectorAll(\".item\")); return all.length ? all : null; })()",
            tab.transport.sent.single().param("expression"),
        )
        tab.close()
    }

    @Test
    fun querySelectorAll_describesEveryMatch_inDocumentOrder() = runTest(StandardTestDispatcher()) {
        val items = mapOf("li-0" to item(10, "Alpha"), "li-1" to item(20, "Beta"), "li-2" to item(30, "Gamma"))
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(
                    """{"result":[""" +
                        """{"name":"2","configurable":true,"enumerable":true,"value":{"type":"object","subtype":"node","objectId":"li-2"}},""" +
                        """{"name":"0","configurable":true,"enumerable":true,"value":{"type":"object","subtype":"node","objectId":"li-0"}},""" +
                        """{"name":"1","configurable":true,"enumerable":true,"value":{"type":"object","subtype":"node","objectId":"li-1"}},""" +
                        """{"name":"3","configurable":true,"enumerable":true,"get":{"type":"function","objectId":"getter"}},""" +
                        """{"name":"4","configurable":true,"enumerable":true,"value":{"type":"number","value":4}},""" +
                        """{"name":"length","configurable":false,"enumerable":false,"value":{"type":"number","value":3}},""" +
                        """{"name":"__proto__","configurable":true,"enumerable":false,"value":{"type":"object","objectId":"proto"}}""" +
                        """]}"""
                )
                "DOM.describeNode" -> Reply.ok(items.getValue(it.param("objectId")!!), latency = 100)
                else -> Reply.ok()
            }
        }

        val found = tab.querySelectorAll(".item")

        assertEquals(listOf("Alpha", "Beta", "Gamma"), found.map { it.text })
        assertEquals(listOf(10, 20, 30), found.map { it.backendNodeId })
        assertEquals("array", tab.transport.sent.single { it.method == "Runtime.getProperties" }.param("objectId"))
        assertEquals("true", tab.transport.sent.single { it.method == "Runtime.getProperties" }.param("ownProperties"))
        assertEquals(100, testScheduler.currentTime, "the descriptions are sent together, one round trip for all")
        assertEquals(
            "array", tab.transport.sent.single { it.method == "Runtime.releaseObject" }.param("objectId"),
            "the array is released while the matches are described"
        )
        assertTrue("DOM.getDocument" !in tab.sent)
        tab.close()
    }

    @Test
    fun querySelectorAll_leavesOutAMatchGoneBeforeItIsDescribed() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(
                    """{"result":[""" +
                        """{"name":"0","configurable":true,"enumerable":true,"value":{"type":"object","subtype":"node","objectId":"li-0"}},""" +
                        """{"name":"1","configurable":true,"enumerable":true,"value":{"type":"object","subtype":"node","objectId":"li-1"}}""" +
                        """]}"""
                )
                "DOM.describeNode" ->
                    if (it.param("objectId") == "li-0") Reply.error("Could not find object with given id")
                    else Reply.ok(item(20, "Beta"))
                "Runtime.releaseObject" -> Reply.error("Could not find object with given id")
                else -> Reply.ok()
            }
        }

        assertEquals(listOf("Beta"), tab.querySelectorAll(".item").map { it.text })
        tab.close()
    }

    @Test
    fun selectAll_asksThePage_andTimesOutWhenNothingMatches() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) { Reply.ok(nothing) }

        assertFailsWith<TimeoutWaitingForElementException> { tab.selectAll(".item", timeout = 1_200) }

        assertTrue(tab.sent.isNotEmpty() && tab.sent.all { it == "Runtime.evaluate" }, tab.sent.toString())
        tab.close()
    }

    @Test
    fun withTheSettingOff_selectReadsTheDocumentAsBefore() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = false) {
            when (it.method) {
                "DOM.getDocument" -> Reply.ok(document)
                "DOM.querySelector" -> Reply.ok("""{"nodeId":7}""")
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }

        val element = tab.select("#go", timeout = 1_000)

        assertEquals(7, element.node.nodeId)
        assertEquals(listOf("DOM.getDocument", "DOM.querySelector"), tab.sent.filter { it != "Target.getTargetInfo" })
        tab.close()
    }

    @Test
    fun withTheSettingOff_querySelectorAllReadsTheDocumentAsBefore() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = false) {
            when (it.method) {
                "DOM.getDocument" -> Reply.ok(document)
                "DOM.querySelectorAll" -> Reply.ok("""{"nodeIds":[7]}""")
                else -> Reply.ok()
            }
        }

        assertEquals(listOf(7), tab.querySelectorAll("#go").map { it.node.nodeId })
        assertEquals(listOf("DOM.getDocument", "DOM.querySelectorAll"), tab.sent)
        tab.close()
    }

    @Test
    fun withoutABrowser_lookupsReadTheDocument() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this) {
            when (it.method) {
                "DOM.getDocument" -> Reply.ok(document)
                "DOM.querySelector" -> Reply.ok("""{"nodeId":7}""")
                else -> Reply.ok()
            }
        }
        val tab = ScriptedTab(this, transport)

        assertEquals(7, tab.querySelector("#go")!!.node.nodeId)
        assertEquals(listOf("DOM.getDocument", "DOM.querySelector"), transport.methods)
        tab.close()
    }

    @Test
    fun underANode_lookupsAreUnchanged_evenWithTheSettingOn() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "DOM.querySelector" -> Reply.ok("""{"nodeId":8}""")
                else -> Reply.ok()
            }
        }
        val parent = dev.kdriver.core.scriptedNode(7, name = "DIV", children = listOf(dev.kdriver.core.scriptedNode(8)))

        val child = tab.querySelector("input", dev.kdriver.core.dom.NodeOrElement.WrappedNode(parent))

        assertEquals(8, child!!.node.nodeId)
        assertEquals(listOf("DOM.querySelector"), tab.sent)
        tab.close()
    }

    private val arrayOfOne = """{"result":[""" +
        """{"name":"0","configurable":true,"enumerable":true,"value":{"type":"object","subtype":"node","objectId":"li-0"}}""" +
        """]}"""

    @Test
    fun querySelectorAll_findsNothing_whenTheArrayIsGoneBeforeItIsRead() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.error("Could not find object with given id")
                else -> Reply.ok()
            }
        }

        assertEquals(emptyList(), tab.querySelectorAll(".item"))
        assertEquals(listOf("Runtime.evaluate", "Runtime.getProperties"), tab.sent)
        tab.close()
    }

    @Test
    fun querySelector_findsNothing_whenTheContextIsGone() = runTest(StandardTestDispatcher()) {
        for (error in listOf("Cannot find context with specified id", "Execution context was destroyed.")) {
            val tab = tab(lookupsInPage = true) {
                when (it.method) {
                    "Runtime.evaluate" -> Reply.error(error)
                    else -> Reply.ok()
                }
            }

            assertNull(tab.querySelector("#go"), error)
            assertEquals(listOf("Runtime.evaluate"), tab.sent)
            tab.close()
        }
    }

    @Test
    fun querySelector_findsNothing_whenTheNodeIsGoneBeforeItIsDescribed() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.error("No node with given id found")
                else -> Reply.ok()
            }
        }

        assertNull(tab.querySelector("#go"))
        tab.close()
    }

    @Test
    fun anyOtherProtocolError_isThrown() = runTest(StandardTestDispatcher()) {
        val describing = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.error("Message is too large")
                else -> Reply.ok()
            }
        }
        assertEquals(
            "Message is too large",
            assertFailsWith<dev.kdriver.cdp.CDPException> { describing.querySelector("#go") }.originalMessage
        )
        describing.close()

        val listing = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(arrayOfOne)
                "DOM.describeNode" -> Reply.ok(item(10, "Alpha"))
                "Runtime.releaseObject" -> Reply.error("Session closed")
                else -> Reply.ok()
            }
        }
        assertEquals("Session closed", assertFailsWith<dev.kdriver.cdp.CDPException> { listing.querySelectorAll(".item") }.originalMessage)
        listing.close()

        val evaluating = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.error("Session closed")
                else -> Reply.ok()
            }
        }
        assertFailsWith<dev.kdriver.cdp.CDPException> { evaluating.querySelectorAll(".item") }
        evaluating.close()
    }

    @Test
    fun selectAllWithFrames_findsTheFramesInTheDocument_andTheMatchesInThePage() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "DOM.getDocument" -> Reply.ok(document)
                "DOM.querySelectorAll" -> Reply.ok("""{"nodeIds":[]}""")
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(arrayOfOne)
                "DOM.describeNode" -> Reply.ok(item(10, "Alpha"))
                else -> Reply.ok()
            }
        }

        assertEquals(listOf("Alpha"), tab.selectAll(".item", timeout = 1_000, includeFrames = true).map { it.text })
        assertEquals("iframe", tab.transport.sent.single { it.method == "DOM.querySelectorAll" }.param("selector"))
        assertTrue("DOM.getDocument" in tab.sent)
        tab.close()
    }

    @Test
    fun aLookupUnderAnElementFoundInThePage_updatesItFromTheDocumentFirst() = runTest(StandardTestDispatcher()) {
        val tab = tab(lookupsInPage = true) {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(button)
                "DOM.getDocument" -> Reply.ok(document)
                "DOM.querySelector" -> Reply.ok("""{"nodeId":8}""")
                "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","subtype":"node","objectId":"obj-2"}}""")
                else -> Reply.ok()
            }
        }
        val button = tab.querySelector("#go")!!

        val text = tab.querySelector("*", dev.kdriver.core.dom.NodeOrElement.WrappedElement(button))

        assertEquals(7, button.node.nodeId, "updated from the document")
        assertEquals("7", tab.transport.sent.single { it.method == "DOM.querySelector" }.param("nodeId"))
        assertEquals(8, text!!.node.nodeId)
        val resolvesBefore = tab.sent.count { it == "DOM.resolveNode" }
        tab.querySelector("*", dev.kdriver.core.dom.NodeOrElement.WrappedElement(button))
        assertEquals(resolvesBefore, tab.sent.count { it == "DOM.resolveNode" }, "a bound element is not updated again")
        assertEquals(2, tab.sent.count { it == "DOM.querySelector" })
        tab.close()
    }

}
