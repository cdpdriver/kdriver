package dev.kdriver.core.tab

import dev.kdriver.core.browser.Browser
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.sampleFile
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

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
    fun testWaitForReadyState() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        tab.waitForReadyState(ReadyState.COMPLETE)

        val readyState = tab.evaluate<ReadyState>("document.readyState")
        assertEquals(ReadyState.COMPLETE, readyState)
        browser.stop()
    }

}
