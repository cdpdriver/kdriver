@file:OptIn(dev.kdriver.cdp.InternalCdpApi::class)

package dev.kdriver.core.dom

import dev.kdriver.cdp.CDPException
import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.domain.Input
import dev.kdriver.core.ScriptedTab
import dev.kdriver.core.ScriptedTransport
import dev.kdriver.core.ScriptedTransport.Reply
import dev.kdriver.core.element
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.objectId
import dev.kdriver.core.objectIdsOf
import dev.kdriver.core.objectReply
import dev.kdriver.core.param
import dev.kdriver.core.scriptedNode
import dev.kdriver.core.tab.Tab
import dev.kdriver.core.valueReply
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Round trips of [DefaultElement]: the remote object reused by [DefaultElement.rawApply], input gestures written
 * back to back ([InputPipeline]), and plain delays instead of `tab.sleep()` inside a gesture. Each test pins the
 * commands sent, their order and the result or error, so the behaviour is the one of the previous implementation.
 */
class ElementRoundTripTest {

    private var objects = 0

    /** A page with one element: every resolve gives a new object id, `callFunctionOn` answers [value]. */
    private fun page(
        latency: Long = 0,
        value: (ScriptedTransport.Sent) -> String = { "\"abc\"" },
        override: (ScriptedTransport.Sent) -> Reply? = { null },
    ): (ScriptedTransport.Sent) -> Reply? = { sent ->
        override(sent) ?: when (sent.method) {
            "DOM.resolveNode" -> Reply.ok(objectReply("obj-${++objects}"), latency)
            "Runtime.callFunctionOn" -> Reply.ok(valueReply(value(sent)), latency)
            "Runtime.evaluate" -> Reply.ok(valueReply("""{"width":800,"height":600}"""), latency)
            "Target.getTargetInfo" -> Reply.ok(ScriptedTab.TARGET_INFO, latency)
            else -> Reply.ok(latency = latency)
        }
    }

    // --- rawApply: the remote object is resolved once and reused while valid ---

    @Test
    fun rawApply_resolvesOnce_thenReusesTheObject() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page())
        val el = ScriptedTab(this, transport).element()

        assertEquals("abc", el.getInputValue())
        assertEquals("abc", el.getInputValue())

        assertEquals(listOf("DOM.resolveNode", "Runtime.callFunctionOn", "Runtime.callFunctionOn"), transport.methods)
        assertEquals(listOf("obj-1", "obj-1"), transport.objectIdsOf())
        assertEquals("obj-1", el.objectId)
        (el.tab as ScriptedTab).close()
    }

    @Test
    fun rawApply_resolvesAgain_whenTheCachedObjectIsStale() = runTest(StandardTestDispatcher()) {
        var calls = 0
        val transport = ScriptedTransport(this, page(override = {
            if (it.method == "Runtime.callFunctionOn" && ++calls == 2) {
                Reply.error("Could not find object with given id")
            } else {
                null
            }
        }))
        val el = ScriptedTab(this, transport).element()

        el.getInputValue()
        assertEquals("abc", el.getInputValue())

        assertEquals(
            listOf(
                "DOM.resolveNode",
                "Runtime.callFunctionOn",
                "Runtime.callFunctionOn",
                "DOM.resolveNode",
                "Runtime.callFunctionOn",
            ),
            transport.methods,
        )
        assertEquals(listOf("obj-1", "obj-1", "obj-2"), transport.objectIdsOf())
        (el.tab as ScriptedTab).close()
    }

    @Test
    fun rawApply_throwsTheResolveError_whenTheNodeIsGone() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page())
        val el = ScriptedTab(this, transport).element()
        el.getInputValue()
        // The page navigated: the object and the node are both gone.
        transport.answer = page(override = {
            when (it.method) {
                "Runtime.callFunctionOn" -> Reply.error("Cannot find context with specified id")
                "DOM.resolveNode" -> Reply.error("No node with given id found")
                else -> null
            }
        })

        val error = assertFailsWith<CDPException> { el.getInputValue() }

        assertTrue(error.message!!.contains("No node with given id found"))
        assertEquals(2, transport.methods.count { it == "DOM.resolveNode" })
        (el.tab as ScriptedTab).close()
    }

    @Test
    fun rawApply_resolvesAgain_whenTheNodeWasSwapped() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page())
        val el = ScriptedTab(this, transport).element(nodeId = 7)

        el.getInputValue()
        el.node = scriptedNode(9)
        el.getInputValue()

        val resolved = transport.sent.filter { it.method == "DOM.resolveNode" }.map { it.param("backendNodeId") }
        assertEquals(listOf("7", "9"), resolved)
        assertEquals(listOf("obj-1", "obj-2"), transport.objectIdsOf())
        (el.tab as ScriptedTab).close()
    }

    @Test
    fun rawApply_reusesTheObjectOfUpdate() = runTest(StandardTestDispatcher()) {
        val doc = """{"root":{"nodeId":1,"backendNodeId":1,"nodeType":9,"nodeName":"#document","localName":"",""" +
            """"nodeValue":"","childNodeCount":1,"children":[{"nodeId":7,"backendNodeId":7,"nodeType":1,""" +
            """"nodeName":"INPUT","localName":"input","nodeValue":""}]}}"""
        val transport = ScriptedTransport(
            this,
            page(override = { if (it.method == "DOM.getDocument") Reply.ok(doc) else null }),
        )
        val el = ScriptedTab(this, transport).element(nodeId = 7)

        el.update()
        el.getInputValue()

        assertEquals(listOf("DOM.getDocument", "DOM.resolveNode", "Runtime.callFunctionOn"), transport.methods)
        (el.tab as ScriptedTab).close()
    }

    @Test
    fun rawApply_jsErrorIsAnEvaluateException_andIsNotRetried() = runTest(StandardTestDispatcher()) {
        val jsError = """{"result":{"type":"object"},"exceptionDetails":{"exceptionId":1,"text":"Uncaught",""" +
            """"lineNumber":0,""" +
            """"columnNumber":0,"exception":{"type":"object","description":"Error: Element is detached from DOM"}}}"""
        var calls = 0
        val transport = ScriptedTransport(this, page(override = {
            if (it.method == "Runtime.callFunctionOn" && ++calls == 2) Reply.ok(jsError) else null
        }))
        val el = ScriptedTab(this, transport).element()

        el.getInputValue()
        assertFailsWith<EvaluateException> { el.getInputValue() }

        val resolves = transport.methods.count { it == "DOM.resolveNode" }
        assertEquals(1, resolves, "a JS error ran the function: no second try")
        (el.tab as ScriptedTab).close()
    }

    @Test
    fun rawApply_cancelledDuringTheCachedCall_isNotRetried() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page())
        val el = ScriptedTab(this, transport).element()
        el.getInputValue()
        transport.answer = { null } // the next call hangs

        val job = launch { el.getInputValue() }
        runCurrent()
        job.cancel()
        runCurrent()

        assertTrue(job.isCancelled)
        assertEquals(1, transport.methods.count { it == "DOM.resolveNode" })
        (el.tab as ScriptedTab).close()
    }

    @Test
    fun rawApply_withoutAnObjectId_resolvesEveryTime() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page(override = {
            if (it.method == "DOM.resolveNode") Reply.ok("""{"object":{"type":"object"}}""") else null
        }))
        val el = ScriptedTab(this, transport).element()

        el.getInputValue()
        el.getInputValue()

        assertEquals(2, transport.methods.count { it == "DOM.resolveNode" }, "nothing to reuse: resolved as before")
        (el.tab as ScriptedTab).close()
    }

    @Test
    fun rawApply_callsWithoutObject_whenASubclassResolvesNothing() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page())
        val tab = ScriptedTab(this, transport)
        val el = object : DefaultElement(tab, scriptedNode(7)) {
            override suspend fun updateRemoteObject(): dev.kdriver.cdp.domain.Runtime.RemoteObject? = null
        }

        assertEquals("abc", el.getInputValue())

        assertEquals(null, transport.sent.single { it.method == "Runtime.callFunctionOn" }.objectId)
        tab.close()
    }

    // --- input gestures: written back to back, paced by plain delays ---

    @Test
    fun sendKeys_focusesThenWritesEveryCharWithoutWaiting() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page(latency = 100, value = { "null" }))
        val el = ScriptedTab(this, transport).element()

        el.sendKeys("hey")

        assertEquals(
            listOf("DOM.resolveNode", "Runtime.callFunctionOn") + List(3) { "Input.dispatchKeyEvent" },
            transport.methods,
        )
        val keys = transport.sent.filter { it.method == "Input.dispatchKeyEvent" }
        assertEquals(listOf("h", "e", "y"), keys.map { it.param("text") })
        assertEquals(setOf("char"), keys.map { it.param("type") }.toSet())
        assertEquals(1, keys.map { it.atMillis }.toSet().size, "all keys left together")
        assertEquals(300, testScheduler.currentTime, "resolve, focus, then one round trip for the three keys")
        (el.tab as ScriptedTab).close()
    }

    @Test
    fun sendKeys_throwsTheFirstKeyError() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page(value = { "null" }, override = {
            if (it.method == "Input.dispatchKeyEvent") Reply.error("Invalid parameters") else null
        }))
        val el = ScriptedTab(this, transport).element()

        assertFailsWith<CDPException> { el.sendKeys("ab") }
        (el.tab as ScriptedTab).close()
    }

    @Test
    fun mouseMove_sendsTheTrajectoryWithoutWaitingNorRefreshingTheTarget() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page(latency = 100, value = { """{"x":400.0,"y":300.0}""" }))
        val tab = ScriptedTab(this, transport)
        tab.lastMouseX = 10.0
        tab.lastMouseY = 10.0
        val el = tab.element()

        el.mouseMove()

        val moves = transport.sent.filter { it.method == "Input.dispatchMouseEvent" }
        assertTrue(moves.size in 9..15, "8 to 14 steps plus the start (${moves.size})")
        assertFalse("Target.getTargetInfo" in transport.methods, "no target refresh between steps")
        val trajectory = moves.last().atMillis - moves.first().atMillis
        assertTrue(trajectory <= 25L * (moves.size - 1), "paced by the 8-25 ms delays only ($trajectory ms)")
        assertEquals(moves.last().atMillis + 100, testScheduler.currentTime, "the trajectory ends with its last reply")
        assertTrue(tab.lastMouseX!! in 395.0..405.0)
        tab.close()
    }

    @Test
    fun mouseMove_startsFromInsideTheViewport_whenTheMouseWasNeverMoved() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page(value = { """{"x":400.0,"y":300.0}""" }))
        val tab = ScriptedTab(this, transport)

        tab.element().mouseMove()

        assertTrue("Runtime.evaluate" in transport.methods, "the viewport is read once")
        val first = transport.sent.first { it.method == "Input.dispatchMouseEvent" }
        val x = first.params!!["x"]!!.jsonPrimitive.content.toDouble()
        assertTrue(x in 50.0..750.0)
        tab.close()
    }

    @Test
    fun mouseMove_stopsAtTheNextStep_afterAStepFailed() = runTest(StandardTestDispatcher()) {
        var moves = 0
        val transport = ScriptedTransport(this, page(value = { """{"x":400.0,"y":300.0}""" }, override = {
            if (it.method == "Input.dispatchMouseEvent" && ++moves == 2) Reply.error("Invalid parameters") else null
        }))
        val tab = ScriptedTab(this, transport)
        tab.lastMouseX = 10.0
        tab.lastMouseY = 10.0

        assertFailsWith<CDPException> { tab.element().mouseMove() }

        // The error of step 2 is back during the pause that follows it: step 3 is never sent.
        assertEquals(2, transport.methods.count { it == "Input.dispatchMouseEvent" }, "stopped at the next step")
        tab.close()
    }

    private fun clickPage(latency: Long = 0, pressError: Boolean = false): (ScriptedTransport.Sent) -> Reply? = page(
        latency = latency,
        value = {
            if (it.param("functionDeclaration")!!.contains("needsScroll")) {
                """{"x":400.0,"y":300.0,"scrollX":0.0,"scrollY":0.0,"needsScroll":false}"""
            } else """{"x":400.0,"y":300.0}"""
        },
        override = {
            if (pressError && it.method == "Input.dispatchMouseEvent" && it.param("type") == "mousePressed") {
                Reply.error("Invalid parameters")
            } else null
        },
    )

    @Test
    fun mouseClick_pressesAndReleasesWithoutRefreshingTheTarget() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, clickPage(latency = 100))
        val tab = ScriptedTab(this, transport)
        tab.lastMouseX = 390.0
        tab.lastMouseY = 290.0

        tab.element().mouseClick(Input.MouseButton.LEFT)

        assertFalse("Target.getTargetInfo" in transport.methods)
        val events = transport.sent.filter { it.method == "Input.dispatchMouseEvent" }
        val buttons = events.map { it.param("type") }.filter { it != "mouseMoved" }
        assertEquals(listOf("mousePressed", "mouseReleased"), buttons)
        val press = events.first { it.param("type") == "mousePressed" }
        val release = events.first { it.param("type") == "mouseReleased" }
        assertEquals("left", press.param("button"))
        assertEquals("1", press.param("clickCount"))
        assertTrue(release.atMillis - press.atMillis in 40..120, "released after the press delay, not after its reply")
        tab.close()
    }

    @Test
    fun mouseClick_releasesTheButton_whenThePressFails() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, clickPage(pressError = true))
        val tab = ScriptedTab(this, transport)
        tab.lastMouseX = 390.0
        tab.lastMouseY = 290.0

        assertFailsWith<CDPException> { tab.element().mouseClick(Input.MouseButton.LEFT) }

        assertEquals("mouseReleased", transport.sent.last().param("type"), "the button is never left pressed")
        tab.close()
    }

    @Test
    fun clearInputByDeleting_sendsEachBackspacePairTogether() = runTest(StandardTestDispatcher()) {
        var length = 2
        val transport = ScriptedTransport(this, page(latency = 100, value = {
            if (it.param("functionDeclaration")!!.contains("setSelectionRange")) {
                val n = length
                length = (length - 1).coerceAtLeast(0)
                "$n"
            } else "null"
        }))
        val tab = ScriptedTab(this, transport)

        tab.element().clearInputByDeleting()

        val keys = transport.sent.filter { it.method == "Input.dispatchKeyEvent" }
        assertEquals(listOf("keyDown", "keyUp", "keyDown", "keyUp"), keys.map { it.param("type") })
        assertEquals(setOf("Backspace"), keys.map { it.param("key") }.toSet())
        assertEquals(keys[0].atMillis, keys[1].atMillis, "keyUp leaves with its keyDown")
        assertFalse("Target.getTargetInfo" in transport.methods)
        tab.close()
    }

    // --- the pipeline on a tab that is not a DefaultConnection ---

    @Test
    fun pipeline_awaitsEachEvent_onACustomTab() = runTest(StandardTestDispatcher()) {
        val tab = mockk<Tab>()
        coEvery { tab.callCommand(any(), any(), any()) } returns null

        tab.pipelineInput {
            mouse(Input.DispatchMouseEventParameter(type = "mouseMoved", x = 1.0, y = 2.0))
            key(Input.DispatchKeyEventParameter(type = "char", text = "a"))
            insertText("é")
        }

        coVerify(exactly = 1) { tab.callCommand("Input.dispatchMouseEvent", any<JsonElement>(), CommandMode.DEFAULT) }
        coVerify(exactly = 1) { tab.callCommand("Input.dispatchKeyEvent", any<JsonElement>(), CommandMode.DEFAULT) }
        coVerify(exactly = 1) { tab.callCommand("Input.insertText", any<JsonElement>(), CommandMode.DEFAULT) }
    }

    @Test
    fun pipeline_returnsTheBlockResult_afterTheReplies() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page(latency = 100))
        val tab = ScriptedTab(this, transport)

        val result = tab.pipelineInput {
            insertText("x")
            42
        }

        assertEquals(42, result)
        assertEquals(100, testScheduler.currentTime)
        tab.close()
    }

}
