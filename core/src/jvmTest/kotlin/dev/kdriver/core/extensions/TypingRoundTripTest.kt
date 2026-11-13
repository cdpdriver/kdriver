@file:OptIn(dev.kdriver.cdp.InternalCdpApi::class)

package dev.kdriver.core.extensions

import dev.kdriver.cdp.CDPException
import dev.kdriver.core.ScriptedTab
import dev.kdriver.core.ScriptedTransport
import dev.kdriver.core.ScriptedTransport.Reply
import dev.kdriver.core.element
import dev.kdriver.core.objectReply
import dev.kdriver.core.param
import dev.kdriver.core.valueReply
import dev.kdriver.core.dom.Element
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * [sendKeysWithSpecialChars]: one focus, then the keys written back to back (paced by the optional delay between
 * grapheme clusters); printable ASCII as `char` key events, anything else inserted as text, as before.
 */
class TypingRoundTripTest {

    private fun page(latency: Long = 0, keyError: Boolean = false): (ScriptedTransport.Sent) -> Reply? = {
        when (it.method) {
            "DOM.resolveNode" -> Reply.ok(objectReply("obj-1"), latency)
            "Runtime.callFunctionOn" -> Reply.ok(valueReply("null"), latency)
            "Input.dispatchKeyEvent" ->
                if (keyError) Reply.error("Invalid parameters", latency) else Reply.ok(latency = latency)
            else -> Reply.ok(latency = latency)
        }
    }

    private fun ScriptedTransport.typed() = sent.filter { it.method.startsWith("Input.") }
        .map { if (it.method == "Input.insertText") "insert:" + it.param("text") else "char:" + it.param("text") }

    @Test
    fun sendKeysWithSpecialChars_focusesOnce_andWritesTheTextInOneRoundTrip() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page(latency = 100))
        val tab = ScriptedTab(this, transport)

        tab.element().sendKeysWithSpecialChars("ab é!\t~\u007f")

        assertEquals(1, transport.methods.count { it == "Runtime.callFunctionOn" }, "a single focus")
        assertEquals(
            listOf("char:a", "char:b", "char: ", "insert:é", "char:!", "insert:\t", "char:~", "insert:\u007f"),
            transport.typed(),
        )
        assertEquals(300, testScheduler.currentTime, "resolve, focus, then one round trip for the whole text")
        tab.close()
    }

    @Test
    fun sendKeysWithSpecialChars_pacesTheClusters_withoutWaitingForReplies() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page(latency = 100))
        val tab = ScriptedTab(this, transport)
        var pauses = 0

        tab.element().sendKeysWithSpecialChars("abc") { pauses++; 50L }

        assertEquals(2, pauses, "a pause between two clusters, none before the first")
        val times = transport.sent.filter { it.method == "Input.dispatchKeyEvent" }.map { it.atMillis }
        assertEquals(listOf(200L, 250L, 300L), times, "the keys leave at the pace of the delays, not of the replies")
        assertEquals(400, testScheduler.currentTime, "one round trip after the last key")
        tab.close()
    }

    @Test
    fun sendKeysWithSpecialChars_throwsTheKeyError() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, page(keyError = true))
        val tab = ScriptedTab(this, transport)

        assertFailsWith<CDPException> { tab.element().sendKeysWithSpecialChars("ab") { 10L } }
        tab.close()
    }

    @Test
    fun sendKeysWithSpecialChars_onACustomElement_usesItsOwnSendKeysAndInsertText() =
        runTest(StandardTestDispatcher()) {
            val element = mockk<Element>(relaxed = true)
            coEvery { element.rawApply(any(), any()) } returns null

            element.sendKeysWithSpecialChars("aé") { 30L }

            coVerifyOrder {
                element.rawApply("(elem) => elem.focus()", false)
                element.sendKeys("a")
                element.insertText("é")
            }
            assertEquals(30, testScheduler.currentTime)
        }

    @Test
    fun sendKeysWithSpecialChars_withoutDelay_onACustomElement() = runTest(StandardTestDispatcher()) {
        val element = mockk<Element>(relaxed = true)
        coEvery { element.rawApply(any(), any()) } returns null

        element.sendKeysWithSpecialChars("hi")

        coVerifyOrder {
            element.rawApply("(elem) => elem.focus()", false)
            element.sendKeys("h")
            element.sendKeys("i")
        }
    }

}
