package dev.kdriver.core.exceptions

import dev.kdriver.core.tab.ReadyState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ExceptionsTest {

    @Test
    fun testBrowserExecutableNotFoundException() {
        val exception = BrowserExecutableNotFoundException()
        assertNotNull(exception.message)
        assert(exception.message!!.contains("Could not determine browser executable"))
    }

    @Test
    fun testNoBrowserExecutablePathException() {
        val exception = NoBrowserExecutablePathException()
        assertNotNull(exception.message)
        assert(exception.message!!.contains("Browser executable path is not set"))
    }

    @Test
    fun testFailedToConnectToBrowserException() {
        val exception = FailedToConnectToBrowserException()
        assertNotNull(exception.message)
        assert(exception.message!!.contains("Failed to connect to browser"))
    }

    @Test
    fun testTimeoutWaitingForElementException() {
        val exception = TimeoutWaitingForElementException("div.my-class", 5000)
        assertEquals("div.my-class", exception.selector)
        assertEquals(5000, exception.timeout)
        assertNotNull(exception.message)
        assert(exception.message!!.contains("div.my-class"))
    }

    @Test
    fun testTimeoutWaitingForReadyStateException() {
        val exception = TimeoutWaitingForReadyStateException(ReadyState.COMPLETE, 10000, ReadyState.INTERACTIVE)
        assertEquals(ReadyState.COMPLETE, exception.until)
        assertEquals(10000, exception.timeout)
        assertEquals(ReadyState.INTERACTIVE, exception.lastKnownReadyState)
        assertNotNull(exception.message)
        assert(exception.message!!.contains("COMPLETE"))
    }

}
