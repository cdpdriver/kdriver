@file:OptIn(dev.kdriver.cdp.InternalCdpApi::class)

package dev.kdriver.core.tab

import dev.kdriver.cdp.CDPException
import dev.kdriver.core.ScriptedTab
import dev.kdriver.core.ScriptedTransport
import dev.kdriver.core.ScriptedTransport.Reply
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.methodsSentAt
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * [DefaultTab.select], [DefaultTab.find] and [DefaultTab.findAll] wait for the page to settle and refresh the target
 * info before each attempt, as `wait()` did; the refresh now overlaps the lookup instead of costing a round trip of its
 * own. Results, polling, timeouts and errors are unchanged.
 */
class SettledLookupTest {

    private val document =
        """{"root":{"nodeId":1,"backendNodeId":1,"nodeType":9,"nodeName":"#document","localName":"","nodeValue":"",""" +
            """"childNodeCount":1,"children":[{"nodeId":7,"backendNodeId":70,"nodeType":1,"nodeName":"BUTTON",""" +
            """"localName":"button","nodeValue":"","attributes":["id","go"],"childNodeCount":1,"children":[""" +
            """{"nodeId":8,"backendNodeId":80,"nodeType":3,"nodeName":"#text","localName":"","nodeValue":"Send"}]}]}}"""

    /** A page with one `<button id="go">Send</button>`; [found] false makes every lookup come back empty. */
    private fun browser(found: Boolean = true, targetError: String? = null): (ScriptedTransport.Sent) -> Reply = {
        when (it.method) {
            "Target.getTargetInfo" ->
                if (targetError != null) Reply.error(targetError, latency = 100)
                else Reply.ok(ScriptedTab.TARGET_INFO, latency = 100)
            "DOM.getDocument" -> Reply.ok(document, latency = 100)
            "DOM.querySelector" -> Reply.ok(if (found) """{"nodeId":7}""" else """{"nodeId":0}""", latency = 100)
            "DOM.performSearch" -> Reply.ok("""{"searchId":"s1","resultCount":${if (found) 1 else 0}}""", latency = 100)
            "DOM.getSearchResults" -> Reply.ok("""{"nodeIds":[7]}""", latency = 100)
            else -> Reply.ok(latency = 100)
        }
    }

    @Test
    fun select_overlapsTheTargetRefreshWithTheLookup() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, browser())
        val tab = ScriptedTab(this, transport)

        val button = tab.select("#go", timeout = 1_000)

        assertEquals("button", button.tag)
        assertEquals("Send", button.text)
        assertEquals("https://example.test/", tab.targetInfo!!.url, "the target info is still refreshed")
        val sentAtStart = transport.methodsSentAt(0)
        assertEquals(setOf("DOM.getDocument", "Target.getTargetInfo"), sentAtStart)
        assertEquals(listOf("DOM.querySelector"), transport.sent.filter { it.atMillis > 0 }.map { it.method })
        assertEquals(200, testScheduler.currentTime, "two round trips (refresh ∥ document, then query), not three")
        tab.close()
    }

    @Test
    fun select_keepsPolling_thenTimesOut() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, browser(found = false))
        val tab = ScriptedTab(this, transport)

        val error = assertFailsWith<TimeoutWaitingForElementException> { tab.select("#missing", timeout = 50) }

        assertTrue(error.message!!.contains("#missing"))
        val attempts = transport.methods.count { it == "DOM.querySelector" }
        assertTrue(attempts >= 1, "polled ($attempts)")
        assertEquals(attempts, transport.methods.count { it == "Target.getTargetInfo" }, "one refresh per attempt")
        tab.close()
    }

    @Test
    fun select_throwsWhenTheTargetRefreshFails() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, browser(targetError = "No target with given id found"))
        val tab = ScriptedTab(this, transport)

        assertFailsWith<CDPException> { tab.select("#go", timeout = 1_000) }
        tab.close()
    }

    @Test
    fun find_overlapsTheTargetRefreshWithTheTextSearch() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, browser())
        val tab = ScriptedTab(this, transport)

        val button = tab.find("Send", timeout = 1_000)

        assertEquals("button", button.tag)
        assertEquals(setOf("DOM.getDocument", "Target.getTargetInfo"), transport.methodsSentAt(0))
        tab.close()
    }

    @Test
    fun find_timesOut_whenTheTextNeverShows() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, browser(found = false))
        val tab = ScriptedTab(this, transport)

        assertFailsWith<TimeoutWaitingForElementException> { tab.find("Nowhere", timeout = 50) }
        tab.close()
    }

    @Test
    fun findAll_overlapsTheTargetRefreshWithTheTextSearch() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, browser())
        val tab = ScriptedTab(this, transport)

        val found = tab.findAll("Send", timeout = 1_000)

        assertEquals(listOf("button"), found.map { it.tag })
        assertEquals(setOf("DOM.getDocument", "Target.getTargetInfo"), transport.methodsSentAt(0))
        tab.close()
    }

    @Test
    fun findAll_timesOut_whenTheTextNeverShows() = runTest(StandardTestDispatcher()) {
        val transport = ScriptedTransport(this, browser(found = false))
        val tab = ScriptedTab(this, transport)

        assertFailsWith<TimeoutWaitingForElementException> { tab.findAll("Nowhere", timeout = 50) }
        tab.close()
    }

}
