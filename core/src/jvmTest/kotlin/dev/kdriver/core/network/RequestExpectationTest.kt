package dev.kdriver.core.network

import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.sampleFile
import dev.kdriver.core.tab.ReadyState
import dev.kdriver.models.UserData
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestExpectationTest {

    @Test
    fun testExpect() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        tab.expect(Regex("groceries.html")) {
            tab.get(sampleFile("groceries.html"))

            val request = withTimeout(3000L) { this@expect.getRequest() }
            val response = withTimeout(3000L) { this@expect.getResponse() }
            val responseBody = withTimeout(3000L) { this@expect.getRawResponseBody() }

            assertEquals(request.url, response.url)
            assertEquals(200, response.status)
            assertTrue(responseBody.body.isNotEmpty())
        }

        browser.stop()
    }

    @Test
    fun testExpectWithReload() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        tab.expect(Regex("groceries.html")) {
            tab.get(sampleFile("groceries.html"))
            tab.waitForReadyState(ReadyState.COMPLETE)
            reset()
            tab.reload()
            tab.waitForReadyState(ReadyState.COMPLETE)

            val request = withTimeout(3000L) { this@expect.getRequest() }
            val response = withTimeout(3000L) { this@expect.getResponse() }
            val responseBody = withTimeout(3000L) { this@expect.getRawResponseBody() }

            assertEquals(request.url, response.url)
            assertEquals(200, response.status)
            assertTrue(responseBody.body.isNotEmpty())
        }

        browser.stop()
    }

    @Test
    fun testExpectBatch() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        val pagePattern = Regex("profile.html")
        val apiPattern = Regex("user-data.json")

        tab.expectBatch(listOf(pagePattern, apiPattern)) {
            tab.get(sampleFile("profile.html"))

            val pageExp = expectations[pagePattern] ?: error("Missing expectation for profile.html")
            val apiExp = expectations[apiPattern] ?: error("Missing expectation for user-data.json")

            val pageResponse = withTimeout(3000L) { pageExp.getResponse() }
            val apiResponse = withTimeout(3000L) { apiExp.getResponse() }

            assertEquals(200, pageResponse.status)
            assertEquals(200, apiResponse.status)

            val userData = withTimeout(3000L) { apiExp.getResponseBody<UserData>() }
            assertEquals("Zendriver", userData.name)
        }

        browser.stop()
    }

}
