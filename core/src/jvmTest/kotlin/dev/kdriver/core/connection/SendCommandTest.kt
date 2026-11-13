@file:OptIn(dev.kdriver.cdp.InternalCdpApi::class)

package dev.kdriver.core.connection

import dev.kdriver.cdp.CDPException
import dev.kdriver.cdp.CommandMode
import dev.kdriver.core.ScriptedTab
import dev.kdriver.core.ScriptedTransport
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.browser.Config
import dev.kdriver.core.exceptions.CommandTimeoutException
import dev.kdriver.core.exceptions.ConnectionClosedException
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * [DefaultConnection.sendCommand] / [PendingCommand]: a command is written without waiting for its reply, the reply is
 * read later, and [DefaultConnection.callCommand] keeps behaving as before (it is `sendCommand(...).await()`).
 */
class SendCommandTest {

    @Test
    fun sendCommand_returnsOnceWritten_andRepliesAreReadLater() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this) { ScriptedTransport.Reply.ok("""{"n":${it.id}}""", latency = 100) }
        val tab = ScriptedTab(this, transport)

        val a = tab.sendCommand("A.one", null, CommandMode.ONE_SHOT)
        val b = tab.sendCommand("B.two", null, CommandMode.ONE_SHOT)

        // Both are on the wire, in order, before any reply came back: no time has passed.
        assertEquals(listOf("A.one", "B.two"), transport.methods)
        assertEquals(0, testScheduler.currentTime)
        assertEquals("A.one", a.method)

        assertEquals("0", a.await()!!.jsonObject["n"]!!.jsonPrimitive.content)
        assertEquals("1", b.await()!!.jsonObject["n"]!!.jsonPrimitive.content)
        // The two round trips overlapped: one latency for both.
        assertEquals(100, testScheduler.currentTime)
        tab.close()
    }

    @Test
    fun callCommand_isSendThenAwait() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this) { ScriptedTransport.Reply.ok("""{"ok":true}""", latency = 40) }
        val tab = ScriptedTab(this, transport)

        val result = tab.callCommand("A.one", null, CommandMode.ONE_SHOT)

        assertEquals("true", result!!.jsonObject["ok"]!!.jsonPrimitive.content)
        assertEquals(40, testScheduler.currentTime)
        tab.close()
    }

    @Test
    fun await_throwsTheBrowserError() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this) { ScriptedTransport.Reply.error("No node with given id found") }
        val tab = ScriptedTab(this, transport)

        val error = assertFailsWith<CDPException> { tab.sendCommand("DOM.x", null, CommandMode.ONE_SHOT).await() }

        assertTrue(error.message!!.contains("No node with given id found"))
        tab.close()
    }

    @Test
    fun await_timesOut_withTheConnectionCommandTimeout() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this) { null } // never answered
        val tab = ScriptedTab(this, transport)

        val pending = tab.sendCommand("Slow.method", null, CommandMode.ONE_SHOT)
        val error = assertFailsWith<CommandTimeoutException> { pending.await() }

        assertEquals("Slow.method", error.method)
        assertEquals(Config.Defaults.COMMAND_TIMEOUT, testScheduler.currentTime)
        tab.close()
    }

    @Test
    fun await_waitsIndefinitely_whenTheTimeoutIsNotPositive() = runTest(StandardTestDispatcher()) {
        val config = mockk<Config>(relaxed = true)
        every { config.commandTimeout } returns 0
        every { config.headless } returns false
        every { config.expert } returns false
        val browser = mockk<Browser>(relaxed = true)
        every { browser.config } returns config
        val transport = ScriptedTransport(this) { null }
        val tab = ScriptedTab(this, transport).also { it.owner = browser }

        val pending = tab.sendCommand("Slow.method", null, CommandMode.ONE_SHOT)
        val waiting = async { pending.await() }
        advanceTimeBy(10 * Config.Defaults.COMMAND_TIMEOUT)
        runCurrent()
        assertFalse(waiting.isCompleted, "a timeout <= 0 never fires")

        transport.reply(transport.sent.single().id, """{"late":1}""")
        assertEquals("1", waiting.await()!!.jsonObject["late"]!!.jsonPrimitive.content)
        tab.close()
    }

    @Test
    fun throwIfFailed_isSilentWhileInFlightAndOnSuccess_andThrowsOnError() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this) { null }
        val tab = ScriptedTab(this, transport)

        val ok = tab.sendCommand("A.ok", null, CommandMode.ONE_SHOT)
        val bad = tab.sendCommand("A.bad", null, CommandMode.ONE_SHOT)
        ok.throwIfFailed()
        bad.throwIfFailed() // still in flight: nothing to say yet

        transport.reply(transport.sent[0].id)
        transport.fail(transport.sent[1].id, "Cannot find context with specified id")
        runCurrent()

        ok.throwIfFailed()
        val error = assertFailsWith<CDPException> { bad.throwIfFailed() }
        assertTrue(error.message!!.contains("Cannot find context"))
        tab.close()
    }

    @Test
    fun throwIfFailed_throwsWhenTheConnectionFailedTheCommand() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this) { null }
        val tab = ScriptedTab(this, transport)

        val pending = tab.sendCommand("A.held", null, CommandMode.ONE_SHOT)
        tab.close()

        assertIs<ConnectionClosedException>(assertFailsWith<ConnectionClosedException> { pending.throwIfFailed() })
    }

    @Test
    fun sleep_refreshesTheTargetDuringTheDelay_notBeforeIt() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this) { ScriptedTransport.Reply.ok(ScriptedTab.TARGET_INFO, latency = 300) }
        val tab = ScriptedTab(this, transport)

        tab.sleep(1_000)

        assertEquals(listOf("Target.getTargetInfo"), transport.methods)
        assertEquals("https://example.test/", tab.targetInfo!!.url, "the target info is fresh when sleep returns")
        assertEquals(1_000, testScheduler.currentTime, "the refresh overlaps the delay instead of adding to it")
        tab.close()
    }

    @Test
    fun sleep_lastsTheRefresh_whenItIsLongerThanTheDelay() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this) { ScriptedTransport.Reply.ok(ScriptedTab.TARGET_INFO, latency = 300) }
        val tab = ScriptedTab(this, transport)

        tab.sleep(10)

        assertEquals("https://example.test/", tab.targetInfo!!.url)
        assertEquals(300, testScheduler.currentTime)
        tab.close()
    }

    @Test
    fun sleep_throwsWhenTheRefreshFails() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this) { ScriptedTransport.Reply.error("No target with given id found", latency = 50) }
        val tab = ScriptedTab(this, transport)

        assertFailsWith<CDPException> { tab.sleep(1_000) }
        assertEquals(50, testScheduler.currentTime, "the delay stops with the failure")
        tab.close()
    }

}
