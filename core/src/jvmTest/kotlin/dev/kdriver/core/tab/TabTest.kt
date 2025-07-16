package dev.kdriver.core.tab

import dev.kdriver.cdp.cdp
import dev.kdriver.cdp.domain.Fetch
import dev.kdriver.cdp.domain.Network
import dev.kdriver.cdp.domain.network
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.sampleFile
import dev.kdriver.models.TodoItem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.*

class TabTest {

    @Test
    fun testSetUserAgentSetsNavigatorValues() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: throw IllegalStateException("Main tab is not available")

        tab.setUserAgent(
            userAgent = "Test user agent",
            acceptLanguage = "testLang",
            platform = "TestPlatform"
        )

        val navigatorUserAgent = tab.evaluate<String>("navigator.userAgent")
        val navigatorLanguage = tab.evaluate<String>("navigator.language")
        val navigatorPlatform = tab.evaluate<String>("navigator.platform")

        assertEquals("Test user agent", navigatorUserAgent)
        assertEquals("testLang", navigatorLanguage)
        assertEquals("TestPlatform", navigatorPlatform)
        browser.stop()
    }

    @Test
    fun testSetUserAgentDefaultsExistingUserAgent() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: throw IllegalStateException("Main tab is not available")

        val existingUserAgent = tab.evaluate<String>("navigator.userAgent")

        tab.setUserAgent(
            acceptLanguage = "testLang"
        )

        val navigatorUserAgent = tab.evaluate<String>("navigator.userAgent")
        val navigatorLanguage = tab.evaluate<String>("navigator.language")

        assertEquals(existingUserAgent, navigatorUserAgent)
        assertEquals("testLang", navigatorLanguage)
        browser.stop()
    }

    @Test
    fun testEvaluateWaitPromiseSuccess() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: throw IllegalStateException("Main tab is not available")

        val result = tab.evaluate<String>("new Promise(r => setTimeout(() => r(\"ok\")));", true)

        assertNotNull(result)
        assertEquals("ok", result)
        browser.stop()
    }

    @Test
    fun testEvaluateWaitPromiseFail() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: throw IllegalStateException("Main tab is not available")

        val result = assertFailsWith<EvaluateException> {
            tab.evaluate<String>("new Promise((_, r) => setTimeout(() => r(\"fail\")));", true)
        }

        assertEquals("fail", result.jsError)
        browser.stop()
    }

    @Test
    fun testEvaluateWaitPromiseError() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: throw IllegalStateException("Main tab is not available")

        val result = assertFailsWith<EvaluateException> {
            tab.evaluate<String>("(async() => { throw new Error(\"Custom error\") })()", true)
        }

        assertEquals("Error: Custom error\n    at <anonymous>:1:21\n    at <anonymous>:1:49", result.jsError)
        browser.stop()
    }

    @Test
    fun testFindFindsElementByText() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val result = tab.find("Apples")

        assertNotNull(result)
        assertEquals("li", result.tag)
        assertEquals("Apples", result.text)
        browser.stop()
    }

    @Test
    fun testFindTimesOutIfElementNotFound() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val exception = assertFailsWith<TimeoutWaitingForElementException> {
            tab.find("Clothes", timeout = 1000)
        }
        assertEquals("Clothes", exception.selector)
        browser.stop()
    }

    @Test
    fun testSelect() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val result = tab.select("li[aria-label^='Apples']")

        assertNotNull(result)
        assertEquals("li", result.tag)
        assertEquals("Apples", result.text)
        browser.stop()
    }

    @Test
    fun testXpath() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val results = tab.xpath("//li[@aria-label=\"Apples (42)\"]")

        assertEquals(1, results.size)
        val result = results[0]

        assertNotNull(result)
        assertEquals("li", result.tag)
        assertEquals("Apples", result.text)
        browser.stop()
    }

    @Test
    fun testHandlers() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val handle1Called = CompletableDeferred<Boolean>()
        val handle2Called = CompletableDeferred<Boolean>()

        val requestHandler1: suspend (Network.RequestWillBeSentParameter) -> Unit = {
            if (!handle1Called.isCompleted) handle1Called.complete(true)
        }
        val requestHandler2: suspend (Network.RequestWillBeSentParameter) -> Unit = {
            if (!handle2Called.isCompleted) handle2Called.complete(true)
        }

        tab.send { cdp.network.enable() }
        val job1 = tab.addHandler(this, { cdp.network.requestWillBeSent }, requestHandler1)
        val job2 = tab.addHandler(this, { cdp.network.requestWillBeSent }, requestHandler2)

        tab.reload()
        tab.waitForReadyState(ReadyState.COMPLETE)

        withTimeout(1000) { assertTrue(handle1Called.await()) }
        withTimeout(1000) { assertTrue(handle2Called.await()) }

        job1.cancel()
        job2.cancel()
        browser.stop()
    }

    @Test
    fun testWaitForReadyState() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        tab.waitForReadyState(ReadyState.COMPLETE)

        val readyState = tab.evaluate<ReadyState>("document.readyState")
        assertEquals(ReadyState.COMPLETE, readyState)
        browser.stop()
    }

    @Test
    fun testExpect() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: throw IllegalStateException("Main tab is not available")

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
    fun testIntercept() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: throw IllegalStateException("Main tab is not available")

        val todoItem = tab.intercept("*/todos/1", Fetch.RequestStage.RESPONSE, Network.ResourceType.XHR) {
            tab.get(sampleFile("todolist.html"))
            val originalResponse = withTimeout(3000L) { getResponseBody<TodoItem>() }
            withTimeout(3000L) { continueRequest() }
            originalResponse
        }

        assertEquals(1, todoItem.id)
        assertEquals(1, todoItem.userId)
        assertEquals("delectus aut autem", todoItem.title)
        assertFalse(todoItem.completed)
        browser.stop()
    }

}
