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
class LookupsInPageTest: LookupsInPageFixtures() {

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
