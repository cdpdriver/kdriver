@file:OptIn(dev.kdriver.cdp.InternalCdpApi::class)

package dev.kdriver.core.tab

import dev.kdriver.cdp.CDPException
import dev.kdriver.core.ScriptedTab
import dev.kdriver.core.ScriptedTransport
import dev.kdriver.core.ScriptedTransport.Reply
import dev.kdriver.core.dom.DefaultElement
import dev.kdriver.core.dom.Element
import dev.kdriver.core.dom.NodeOrElement
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.objectIdsOf
import dev.kdriver.core.param
import dev.kdriver.core.valueReply
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `select`, `selectAll`, `querySelector` and `querySelectorAll` without a node ask the page (`Runtime.evaluate`) and
 * describe the matches (`DOM.describeNode`) instead of reading the whole document. A found element gets its node ids,
 * tree and parent from the document when it is updated or a lookup runs under it, the same as a lookup on the
 * document gave them; an invalid selector throws the browser's error of `DOM.querySelector(All)`.
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

    // An own property of the array of `querySelectorAll`: the match at [index], as Runtime.getProperties lists it.
    private fun match(index: Int, objectId: String) =
        """{"name":"$index","configurable":true,"enumerable":true,""" +
            """"value":{"type":"object","subtype":"node","objectId":"$objectId"}}"""

    private val nothing = """{"result":{"type":"object","subtype":"null","value":null}}"""

    private fun found(objectId: String, subtype: String = "node") =
        """{"result":{"type":"object","subtype":"$subtype","objectId":"$objectId"}}"""

    private val document =
        """{"root":{"nodeId":1,"backendNodeId":1,"nodeType":9,"nodeName":"#document","localName":"","nodeValue":"",""" +
            """"childNodeCount":1,"children":[{"nodeId":7,"backendNodeId":70,"nodeType":1,"nodeName":"BUTTON",""" +
            """"localName":"button","nodeValue":"","attributes":["id","go"],"childNodeCount":1,"children":[""" +
            """{"nodeId":8,"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"","nodeValue":"Send"}]}]}}"""

    private fun TestScope.tab(answer: (ScriptedTransport.Sent) -> Reply): ScriptedTab =
        ScriptedTab(this, ScriptedTransport(this, answer))

    private val ScriptedTab.sent get() = transport.methods

    @Test
    fun select_asksThePage_andDescribesTheMatch_withoutReadingTheDocument() = runTest(StandardTestDispatcher()) {
        val tab = tab {
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
        assertEquals(
            listOf("Runtime.evaluate", "DOM.describeNode", "Runtime.releaseObjectGroup"),
            tab.sent.filter { it != "Target.getTargetInfo" },
        )
        val evaluate = tab.transport.sent.first { it.method == "Runtime.evaluate" }
        assertEquals("document.querySelector(\"#go\")", evaluate.param("expression"))
        val group = evaluate.param("objectGroup")!!
        assertTrue(group.startsWith("kdriver-lookup-"), group)
        val release = tab.transport.sent.single { it.method == "Runtime.releaseObjectGroup" }
        assertEquals(group, release.param("objectGroup"))
        assertEquals(
            tab.transport.sent.single { it.method == "DOM.describeNode" }.atMillis,
            tab.transport.sent.single { it.method == "Runtime.releaseObjectGroup" }.atMillis,
            "the release goes with the description, after it",
        )
        val describe = tab.transport.sent.first { it.method == "DOM.describeNode" }
        assertEquals("obj-1", describe.param("objectId"))
        assertEquals("-1", describe.param("depth"))
        assertEquals("true", describe.param("pierce"))
        tab.close()
    }

    @Test
    fun select_leavesNoObject_andTheFirstCallResolvesTheNode_asBefore() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(button)
                "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","objectId":"obj-2"}}""")
                "Runtime.callFunctionOn" -> Reply.ok(valueReply("null"))
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }

        val element = tab.select("#go", timeout = 1_000)
        assertNull(element.objectId)
        element.click()

        assertEquals(listOf("obj-2"), tab.transport.objectIdsOf())
        assertEquals("70", tab.transport.sent.single { it.method == "DOM.resolveNode" }.param("backendNodeId"))
        tab.close()
    }

    @Test
    fun theReleaseOfTheObjects_holdsTheLookupTwoSecondsAtMost() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(button)
                "Runtime.releaseObjectGroup" -> Reply.ok(latency = 60_000)
                else -> Reply.ok()
            }
        }

        assertEquals("button", tab.querySelector("#go")?.tag)

        assertEquals(2_000, testScheduler.currentTime)
        tab.close()
    }

    @Test
    fun select_triesAgain_whileThePageHasNoMatch_thenTimesOut() = runTest(StandardTestDispatcher()) {
        val tab = tab {
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
        val tab = tab {
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

    private fun syntaxError(selector: String, method: String) =
        """{"result":{"type":"object","subtype":"error","className":"DOMException","objectId":"err"},""" +
            """"exceptionDetails":{"exceptionId":1,"text":"Uncaught","lineNumber":0,"columnNumber":9,"exception":""" +
            """{"type":"object","subtype":"error","className":"DOMException","description":"SyntaxError: Failed to """ +
            """execute '$method' on 'Document': '$selector' is not a valid selector.\\n    at <anonymous>:1:10"}}}"""

    @Test
    fun anInvalidSelector_throwsTheErrorOfDomQuerySelector_andDisablesDom() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(syntaxError("li[", "querySelector"))
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }

        val error = assertFailsWith<CDPException> { tab.select("li[", timeout = 0) }

        assertEquals("DOM.querySelector", error.method)
        assertEquals(-32000, error.code)
        assertEquals("DOM Error while querying", error.originalMessage)
        assertNull(error.data)
        assertEquals(
            "Error while calling command DOM.querySelector: DOM Error while querying (code: -32000)",
            error.message,
        )
        assertTrue("DOM.disable" in tab.sent, "the DOM agent is disabled on the error, as before")
        tab.close()
    }

    @Test
    fun anInvalidSelector_throwsTheErrorOfDomQuerySelectorAll_forTheListMethods() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(syntaxError("li[", "querySelectorAll"))
                else -> Reply.ok()
            }
        }

        val lookups = listOf<suspend () -> Unit>({ tab.querySelectorAll("li[") }, { tab.selectAll("li[", timeout = 0) })
        for (lookup in lookups) {
            val error = assertFailsWith<CDPException> { lookup() }
            assertEquals("DOM.querySelectorAll", error.method)
            assertEquals(-32000, error.code)
            assertEquals("DOM Error while querying", error.originalMessage)
        }
        tab.close()
    }

    @Test
    fun anotherExceptionOfThePage_isThrownAsAnEvaluateException() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(
                    """{"result":{"type":"object","subtype":"error","objectId":"err"},"exceptionDetails":""" +
                        """{"exceptionId":1,"text":"Uncaught","lineNumber":0,"columnNumber":9,"exception":""" +
                        """{"type":"object","subtype":"error","description":"TypeError: replaced by the page"}}}"""
                )
                else -> Reply.ok()
            }
        }

        val error = assertFailsWith<EvaluateException> { tab.querySelector("#go") }

        assertTrue(error.jsError.startsWith("TypeError"), error.jsError)
        assertTrue("DOM.disable" !in tab.sent)
        tab.close()
    }

    @Test
    fun querySelector_passesTheSelectorAsOneJsonStringLiteral() = runTest(StandardTestDispatcher()) {
        val tab = tab { Reply.ok(nothing) }
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
        val tab = tab { Reply.ok(nothing) }

        assertEquals(emptyList(), tab.querySelectorAll(".item"))

        assertEquals(listOf("Runtime.evaluate"), tab.sent)
        assertEquals(
            "(() => { const all = document.querySelectorAll(\".item\");" +
                " if (!all.length) return null;" +
                " const most = document.getElementsByTagName('*').length; const matches = []; let described = 0;" +
                " for (let i = 0; i < all.length; i++) {" +
                " described += 1 + all[i].getElementsByTagName('*').length;" +
                " if (described > most) return 'document'; matches[i] = all[i]; }" +
                " return matches; })()",
            tab.transport.sent.single().param("expression"),
        )
        tab.close()
    }

    @Test
    fun querySelectorAll_describesEveryMatch_inDocumentOrder() = runTest(StandardTestDispatcher()) {
        val items = mapOf("li-0" to item(10, "Alpha"), "li-1" to item(20, "Beta"), "li-2" to item(30, "Gamma"))
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(
                    """{"result":[""" +
                        """${match(2, "li-2")},""" +
                        """${match(0, "li-0")},""" +
                        """${match(1, "li-1")},""" +
                        """{"name":"3","configurable":true,"enumerable":true,""" +
                        """"get":{"type":"function","objectId":"getter"}},""" +
                        """{"name":"4","configurable":true,"enumerable":true,"value":{"type":"number","value":4}},""" +
                        """{"name":"length","configurable":false,"enumerable":false,""" +
                        """"value":{"type":"number","value":3}},""" +
                        """{"name":"__proto__","configurable":true,"enumerable":false,""" +
                        """"value":{"type":"object","objectId":"proto"}}""" +
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
        val group = tab.transport.sent.single { it.method == "Runtime.evaluate" }.param("objectGroup")!!
        assertTrue(group.startsWith("kdriver-lookup-"), group)
        assertEquals(
            listOf("DOM.describeNode", "DOM.describeNode", "DOM.describeNode", "Runtime.releaseObjectGroup"),
            tab.sent.dropWhile { it != "DOM.describeNode" },
            "the objects of the lookup are released in the same round trip, after the descriptions",
        )
        val released = tab.transport.sent.single { it.method == "Runtime.releaseObjectGroup" }
        assertEquals(group, released.param("objectGroup"))
        assertEquals(listOf(null, null, null), found.map { it.objectId }, "no object of the page is kept")
        assertTrue("DOM.getDocument" !in tab.sent)
        tab.close()
    }

    @Test
    fun querySelectorAll_findsNothing_whenAMatchIsGoneBeforeItIsDescribed() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(
                    """{"result":[""" +
                        """${match(0, "li-0")},""" +
                        """${match(1, "li-1")}""" +
                        """]}"""
                )
                "DOM.describeNode" ->
                    if (it.param("objectId") == "li-0") Reply.error("Could not find object with given id")
                    else Reply.ok(item(20, "Beta"))
                "Runtime.releaseObjectGroup" -> Reply.error("Could not find object with given id")
                else -> Reply.ok()
            }
        }

        assertEquals(emptyList(), tab.querySelectorAll(".item"), "the page navigated: none of its old matches")
        assertTrue("Runtime.releaseObjectGroup" in tab.sent)
        tab.close()
    }

    @Test
    fun selectAll_asksThePage_andTimesOutWhenNothingMatches() = runTest(StandardTestDispatcher()) {
        val tab = tab { Reply.ok(nothing) }

        assertFailsWith<TimeoutWaitingForElementException> { tab.selectAll(".item", timeout = 1_200) }

        assertTrue(tab.sent.isNotEmpty() && tab.sent.all { it == "Runtime.evaluate" }, tab.sent.toString())
        tab.close()
    }

    @Test
    fun underANode_lookupsReadTheDocument_asBefore() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "DOM.querySelector" -> Reply.ok("""{"nodeId":8}""")
                else -> Reply.ok()
            }
        }
        val parent = dev.kdriver.core.scriptedNode(7, name = "DIV", children = listOf(dev.kdriver.core.scriptedNode(8)))

        val child = tab.querySelector("input", NodeOrElement.WrappedNode(parent))

        assertEquals(8, child!!.node.nodeId)
        assertEquals(listOf("DOM.querySelector"), tab.sent)
        tab.close()
    }

    private val arrayOfOne = """{"result":[""" +
        """${match(0, "li-0")}""" +
        """]}"""

    @Test
    fun querySelectorAll_findsNothing_whenTheArrayIsGoneBeforeItIsRead() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.error("Could not find object with given id")
                else -> Reply.ok()
            }
        }

        assertEquals(emptyList(), tab.querySelectorAll(".item"))
        assertEquals(listOf("Runtime.evaluate", "Runtime.getProperties", "Runtime.releaseObjectGroup"), tab.sent)
        tab.close()
    }

    @Test
    fun querySelector_findsNothing_whenTheContextIsGone() = runTest(StandardTestDispatcher()) {
        for (error in listOf(
            "Cannot find context with specified id",
            "Execution context was destroyed.",
        )) {
            val tab = tab {
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
        val tab = tab {
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
    fun aClosedTarget_isAnError_notANoMatch() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.error("Inspected target navigated or closed")
                else -> Reply.ok()
            }
        }

        assertFailsWith<CDPException> { tab.querySelector("#go") }
        tab.close()
    }

    @Test
    fun aDescriptionThatFails_isThrown_onceEveryReplyIsRead() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(
                    """{"result":[""" +
                        """${match(0, "li-0")},""" +
                        """${match(1, "li-1")}""" +
                        """]}"""
                )
                "DOM.describeNode" ->
                    if (it.param("objectId") == "li-0") Reply.error("Message is too large", latency = 10)
                    else Reply.ok(item(20, "Beta"), latency = 1_000)
                "Runtime.releaseObjectGroup" -> Reply.ok(latency = 50)
                else -> Reply.ok()
            }
        }

        assertFailsWith<CDPException> { tab.querySelectorAll(".item") }
        assertTrue("Runtime.releaseObjectGroup" in tab.sent)
        assertEquals(1_000, testScheduler.currentTime, "every reply was read before the error was thrown")
        tab.close()
    }

    @Test
    fun aLookupCancelledWhileTheArrayIsRead_stillReleasesItsObjects() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(arrayOfOne, latency = 1_000)
                else -> Reply.ok()
            }
        }

        val lookup = launch { tab.querySelectorAll(".item") }
        advanceTimeBy(100)
        lookup.cancel()
        advanceUntilIdle()

        assertTrue(lookup.isCancelled)
        assertEquals(
            tab.transport.sent.single { it.method == "Runtime.evaluate" }.param("objectGroup"),
            tab.transport.sent.single { it.method == "Runtime.releaseObjectGroup" }.param("objectGroup"),
        )
        tab.close()
    }

    @Test
    fun aLookupCancelledWhileThePageEvaluates_stillReleasesItsObjects() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"), latency = 1_000)
                else -> Reply.ok()
            }
        }

        val lookup = launch { tab.querySelector("#go") }
        advanceTimeBy(100)
        lookup.cancel()
        advanceUntilIdle()

        assertTrue(lookup.isCancelled)
        assertEquals(
            tab.transport.sent.single { it.method == "Runtime.evaluate" }.param("objectGroup"),
            tab.transport.sent.single { it.method == "Runtime.releaseObjectGroup" }.param("objectGroup"),
            "the page may still run an evaluation cut in flight",
        )
        tab.close()
    }

    @Test
    fun aLookupThatFindsNothing_sendsNoRelease() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(nothing)
                else -> Reply.ok()
            }
        }

        assertNull(tab.querySelector("#go"))
        assertEquals(emptyList(), tab.querySelectorAll("#go"))

        assertTrue("Runtime.releaseObjectGroup" !in tab.sent)
        tab.close()
    }

    @Test
    fun anyOtherProtocolError_isThrown() = runTest(StandardTestDispatcher()) {
        val describing = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.error("Message is too large")
                else -> Reply.ok()
            }
        }
        assertEquals(
            "Message is too large",
            assertFailsWith<CDPException> { describing.querySelector("#go") }.originalMessage
        )
        describing.close()

        val listing = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.error("Message is too large")
                else -> Reply.ok()
            }
        }
        assertEquals(
            "Message is too large",
            assertFailsWith<CDPException> { listing.querySelectorAll(".item") }.originalMessage
        )
        assertTrue("Runtime.releaseObjectGroup" in listing.sent, "the objects are released before the error is thrown")
        listing.close()

        val evaluating = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.error("Session closed")
                else -> Reply.ok()
            }
        }
        assertFailsWith<CDPException> { evaluating.querySelectorAll(".item") }
        evaluating.close()
    }

    @Test
    fun querySelectorAll_keepsItsMatches_whenTheReleaseOfTheArrayFails() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(arrayOfOne)
                "DOM.describeNode" -> Reply.ok(item(10, "Alpha"))
                "Runtime.releaseObjectGroup" -> Reply.error("Session closed")
                else -> Reply.ok()
            }
        }

        assertEquals(listOf("Alpha"), tab.querySelectorAll(".item").map { it.text })
        tab.close()
    }

    private val documentWithAFrame =
        """{"root":{"nodeId":1,"backendNodeId":1,"nodeType":9,"nodeName":"#document","localName":"","nodeValue":"",""" +
            """"childNodeCount":2,"children":[{"nodeId":7,"backendNodeId":70,"nodeType":1,"nodeName":"P",""" +
            """"localName":"p","nodeValue":"","childNodeCount":1,"children":[""" +
            """{"nodeId":8,"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"",""" +
            """"nodeValue":"Outer"}]},""" +
            """{"nodeId":9,"backendNodeId":90,"nodeType":1,"nodeName":"IFRAME","localName":"iframe","nodeValue":"",""" +
            """"childNodeCount":0,"contentDocument":{"nodeId":10,"backendNodeId":100,"nodeType":9,""" +
            """"nodeName":"#document","localName":"","nodeValue":"","childNodeCount":1,"children":[""" +
            """{"nodeId":11,"backendNodeId":110,"nodeType":1,"nodeName":"P","localName":"p","nodeValue":"",""" +
            """"childNodeCount":1,"children":[{"nodeId":12,"backendNodeId":120,"nodeType":3,"nodeName":"#text",""" +
            """"localName":"","nodeValue":"Inner"}]}]}}]}}"""

    @Test
    fun selectAllWithAFrame_findsInsideItAndInThePage_fromOneDocumentRead() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "DOM.getDocument" -> Reply.ok(documentWithAFrame)
                "DOM.querySelectorAll" -> when (it.param("nodeId")) {
                    "1" ->
                        if (it.param("selector") == "iframe") Reply.ok("""{"nodeIds":[9]}""")
                        else Reply.ok("""{"nodeIds":[7]}""")
                    "10" -> Reply.ok("""{"nodeIds":[11]}""")
                    else -> Reply.error("Could not find node with given id")
                }
                "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","subtype":"node","objectId":"frame"}}""")
                else -> Reply.ok()
            }
        }

        val found = tab.selectAll("p", timeout = 1_000, includeFrames = true)

        assertEquals(listOf("Inner", "Outer"), found.map { it.text })
        assertEquals(1, tab.sent.count { it == "DOM.getDocument" }, "one read for the frame and the page")
        assertTrue("Runtime.evaluate" !in tab.sent)
        tab.close()
    }

    @Test
    fun selectAllWithFrames_readsTheDocument() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "DOM.getDocument" -> Reply.ok(document)
                "DOM.querySelectorAll" ->
                    if (it.param("selector") == "iframe") Reply.ok("""{"nodeIds":[]}""")
                    else Reply.ok("""{"nodeIds":[7]}""")
                else -> Reply.ok()
            }
        }

        assertEquals(listOf("Send"), tab.selectAll("#go", timeout = 1_000, includeFrames = true).map { it.text })
        assertEquals(
            listOf("iframe", "#go"),
            tab.transport.sent.filter { it.method == "DOM.querySelectorAll" }.map { it.param("selector") }
        )
        assertEquals(1, tab.sent.count { it == "DOM.getDocument" }, "one read for the frames and the page")
        assertTrue("Runtime.evaluate" !in tab.sent)
        tab.close()
    }

    @Test
    fun aLookupUnderAnElementFoundInThePage_bindsItToTheDocumentFirst() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(button)
                "DOM.getDocument" -> Reply.ok(document)
                "DOM.querySelector" -> Reply.ok("""{"nodeId":8}""")
                else -> Reply.ok()
            }
        }
        val button = tab.querySelector("#go")!!

        val text = tab.querySelector("*", NodeOrElement.WrappedElement(button))

        assertEquals(7, button.node.nodeId, "bound to the document")
        assertEquals(1, button.tree?.nodeId)
        assertEquals("7", tab.transport.sent.single { it.method == "DOM.querySelector" }.param("nodeId"))
        assertEquals(8, text!!.node.nodeId)
        assertNull(button.objectId, "bound without resolving its object")
        tab.querySelector("*", NodeOrElement.WrappedElement(button))
        assertEquals(1, tab.sent.count { it == "DOM.getDocument" }, "a bound element is not bound again")
        assertEquals(2, tab.sent.count { it == "DOM.querySelector" })
        assertTrue("DOM.resolveNode" !in tab.sent)
        tab.close()
    }

    /** `<ul id="list"><li class="item">Beta</li></ul>`, as the document read gives it. */
    private val list =
        """{"root":{"nodeId":1,"backendNodeId":1,"nodeType":9,"nodeName":"#document","localName":"","nodeValue":"",""" +
            """"childNodeCount":1,"children":[{"nodeId":5,"parentId":1,"backendNodeId":50,"nodeType":1,""" +
            """"nodeName":"UL","localName":"ul","nodeValue":"","attributes":["id","list"],"childNodeCount":1,""" +
            """"children":[{"nodeId":7,"parentId":5,"backendNodeId":70,"nodeType":1,"nodeName":"LI",""" +
            """"localName":"li","nodeValue":"","attributes":["class","item"],"childNodeCount":1,"children":[""" +
            """{"nodeId":8,"parentId":7,"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"",""" +
            """"nodeValue":"Beta"}]}]}]}}"""

    /** The same `li`, as `DOM.describeNode` gives it: no node ids, no parent id. */
    private val describedItem =
        """{"node":{"nodeId":0,"backendNodeId":70,"nodeType":1,"nodeName":"LI","localName":"li","nodeValue":"",""" +
            """"attributes":["class","item"],"childNodeCount":1,"children":[{"nodeId":0,"parentId":0,""" +
            """"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"","nodeValue":"Beta"}]}}"""

    private fun TestScope.listTab() = tab {
        when (it.method) {
            "Runtime.evaluate" -> Reply.ok(found("obj-1"))
            "DOM.describeNode" -> Reply.ok(describedItem)
            "DOM.getDocument" -> Reply.ok(list)
            "DOM.querySelector" -> Reply.ok("""{"nodeId":7}""")
            "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","subtype":"node","objectId":"obj-2"}}""")
            "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
            else -> Reply.ok()
        }
    }

    // What a lookup on the whole document gave: DOM.querySelector on the document read, the match taken from it.
    private suspend fun ScriptedTab.onTheDocument(selector: String): Element =
        querySelector(selector, NodeOrElement.WrappedNode(currentDocument().first))!!

    @Test
    fun anElementFoundInThePage_hasNoNodeIdTreeNorParent_untilUpdated() = runTest(StandardTestDispatcher()) {
        val tab = listTab()

        val item = tab.select("li.item", timeout = 1_000)

        assertEquals(0, item.node.nodeId)
        assertNull(item.parentId)
        assertNull(item.tree)
        assertEquals(
            "could not get parent since the element has no tree set",
            assertFailsWith<RuntimeException> { item.parent }.message,
        )
        assertEquals(listOf(0), item.children.map { it.node.nodeId })
        assertTrue("DOM.getDocument" !in tab.sent)
        tab.close()
    }

    @Test
    fun update_givesTheNodeIdsTreeAndParent_aLookupOnTheDocumentGave() = runTest(StandardTestDispatcher()) {
        val tab = listTab()
        val item = tab.select("li.item", timeout = 1_000)

        item.update()
        val before = tab.onTheDocument("li.item")

        assertEquals(before.node, item.node)
        assertEquals(7, item.node.nodeId)
        assertEquals(5, item.parentId)
        assertEquals(before.tree, item.tree)
        assertEquals(before.parent!!.node, item.parent!!.node)
        assertEquals("ul", item.parent!!.tag)
        assertEquals(before.children.map { it.node }, item.children.map { it.node })
        assertEquals(1, tab.sent.count { it == "DOM.getDocument" }, "one document read for both")
        tab.close()
    }

    @Test
    fun anElementFoundInThePage_fillsItsNodeIdsTreeAndParent_onFirstRead_fromAKeptDocument() =
        runTest(StandardTestDispatcher()) {
            val tab = listTab()
            val before = tab.onTheDocument("li.item")
            val item = tab.select("li.item", timeout = 1_000)
            val sentBefore = tab.sent.size

            assertEquals(before.node, item.node)
            assertEquals(7, item.node.nodeId)
            assertEquals(5, item.parentId)
            assertEquals(before.tree, item.tree)
            assertEquals("ul", item.parent!!.tag)
            assertEquals(listOf(8), item.children.map { it.node.nodeId })
            assertEquals(sentBefore, tab.sent.size, "filled from the kept document, with no command")
            assertEquals(1, tab.sent.count { it == "DOM.getDocument" })
            tab.close()
        }

    @Test
    fun anElementFoundInThePage_isNotFilledFromADocumentThePageChangedSince() = runTest(StandardTestDispatcher()) {
        val tab = listTab()
        tab.onTheDocument("li.item")
        tab.transport.event("DOM.attributeModified", """{"nodeId":7,"name":"class","value":"busy"}""")
        runCurrent()

        val item = tab.select("li.item", timeout = 1_000)

        assertEquals(0, item.node.nodeId)
        assertNull(item.tree)
        item.update()
        assertEquals(7, item.node.nodeId, "update() still reads the document")
        assertEquals(2, tab.sent.count { it == "DOM.getDocument" })
        tab.close()
    }

    @Test
    fun anElementFoundInThePage_whoseNodeTheKeptDocumentLacks_staysAsFound() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(item(30, "Gone"))
                "DOM.getDocument" -> Reply.ok(list)
                "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO)
                else -> Reply.ok()
            }
        }
        tab.currentDocument()
        val gone = tab.select("li", timeout = 1_000)

        assertEquals(0, gone.node.nodeId)
        assertNull(gone.tree)
        assertEquals("Gone", gone.text)
        (gone as DefaultElement).node = gone.node.copy(nodeName = "P")
        assertEquals("p", gone.tag, "a node set by hand is kept")
        tab.close()
    }

    @Test
    fun aBareNodeFoundInThePage_isReplacedByTheDocumentNode_forALookupUnderIt() = runTest(StandardTestDispatcher()) {
        val tab = listTab()
        val item = tab.querySelector("li.item")!!

        tab.querySelector("*", NodeOrElement.WrappedNode(item.node))

        assertEquals("7", tab.transport.sent.single { it.method == "DOM.querySelector" }.param("nodeId"))
        tab.close()
    }

    @Test
    fun aNodeFoundInThePage_butGoneFromTheDocument_isQueriedAsItIs() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("obj-1"))
                "DOM.describeNode" -> Reply.ok(item(30, "Gone"))
                "DOM.getDocument" -> Reply.ok(list)
                "DOM.querySelector" -> Reply.error("Could not find node with given id")
                "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","subtype":"node","objectId":"obj-2"}}""")
                else -> Reply.ok()
            }
        }
        val gone = tab.querySelector("li")!!

        assertNull(tab.querySelector("*", NodeOrElement.WrappedNode(gone.node)))
        assertNull(tab.querySelector("*", NodeOrElement.WrappedElement(gone)))

        assertEquals(0, gone.node.nodeId, "no node of the document to take")
        assertEquals(1, gone.tree?.nodeId, "the element is bound to the document all the same, as update() does")
        val queried = tab.transport.sent.filter { it.method == "DOM.querySelector" }.map { it.param("nodeId") }
        assertEquals(listOf("0", "0"), queried.take(2))
        tab.close()
    }

    @Test
    fun anotherElementImplementation_isUpdated_beforeALookupUnderIt() = runTest(StandardTestDispatcher()) {
        val tab = listTab()
        val found = tab.querySelector("li.item")!!
        val wrapper: Element = object : Element by found {}

        tab.querySelector("*", NodeOrElement.WrappedElement(wrapper))

        assertEquals(7, wrapper.node.nodeId)
        assertTrue("DOM.resolveNode" in tab.sent, "Element.update resolves the object, as it always did")
        tab.close()
    }

    @Test
    fun querySelectorAll_readsTheDocument_whenTheMatchesOutweighIt() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok("""{"result":{"type":"string","value":"document"}}""")
                "DOM.getDocument" -> Reply.ok(list)
                "DOM.querySelectorAll" -> Reply.ok("""{"nodeIds":[5,7]}""")
                else -> Reply.ok()
            }
        }

        val found = tab.querySelectorAll("ul, li")

        assertEquals(listOf(5, 7), found.map { it.node.nodeId }, "read from the document, as before")
        assertEquals(listOf(1, 1), found.map { it.tree?.nodeId })
        assertEquals(listOf("Runtime.evaluate", "DOM.getDocument", "DOM.querySelectorAll"), tab.sent)
        tab.close()
    }

    @Test
    fun anElementOfQuerySelectorAll_resolvesItsNodeAtItsFirstCall() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(found("array", subtype = "array"))
                "Runtime.getProperties" -> Reply.ok(arrayOfOne)
                "DOM.describeNode" -> Reply.ok(item(10, "Alpha"))
                "DOM.resolveNode" -> Reply.ok("""{"object":{"type":"object","subtype":"node","objectId":"obj-2"}}""")
                "Runtime.callFunctionOn" -> Reply.ok(valueReply("null"))
                else -> Reply.ok()
            }
        }

        tab.querySelectorAll(".item").single().click()

        assertEquals("10", tab.transport.sent.single { it.method == "DOM.resolveNode" }.param("backendNodeId"))
        assertEquals(listOf("obj-2"), tab.transport.objectIdsOf())
        tab.close()
    }

    @Test
    fun aSyntaxErrorThrownByAScriptOfThePage_isAnEvaluateException() = runTest(StandardTestDispatcher()) {
        val tab = tab {
            when (it.method) {
                "Runtime.evaluate" -> Reply.ok(
                    """{"result":{"type":"object","subtype":"error","objectId":"err"},"exceptionDetails":""" +
                        """{"exceptionId":1,"text":"Uncaught","lineNumber":0,"columnNumber":9,"exception":""" +
                        """{"type":"object","subtype":"error","className":"SyntaxError",""" +
                        """"description":"SyntaxError: thrown by the page"}}}"""
                )
                else -> Reply.ok()
            }
        }

        assertFailsWith<EvaluateException> { tab.querySelector("#go") }
        tab.close()
    }

    @Test
    fun aStringAnsweredForQuerySelector_isNoMatch() = runTest(StandardTestDispatcher()) {
        val tab = tab { Reply.ok("""{"result":{"type":"string","value":"document"}}""") }

        assertNull(tab.querySelector("#go"))
        assertEquals(listOf("Runtime.evaluate"), tab.sent)
        tab.close()
    }

}
