package dev.kdriver.core.tab

import dev.kdriver.core.browser.Browser
import dev.kdriver.core.sampleFile
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
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

        val navigatorUserAgent = (tab.evaluate("navigator.userAgent") as JsonPrimitive).content
        val navigatorLanguage = (tab.evaluate("navigator.language") as JsonPrimitive).content
        val navigatorPlatform = (tab.evaluate("navigator.platform") as JsonPrimitive).content

        assertEquals("Test user agent", navigatorUserAgent)
        assertEquals("testLang", navigatorLanguage)
        assertEquals("TestPlatform", navigatorPlatform)
        browser.stop()
    }

    @Test
    fun testSetUserAgentDefaultsExistingUserAgent() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: throw IllegalStateException("Main tab is not available")

        val existingUserAgent = (tab.evaluate("navigator.userAgent") as JsonPrimitive).content

        tab.setUserAgent(
            acceptLanguage = "testLang"
        )

        val navigatorUserAgent = (tab.evaluate("navigator.userAgent") as JsonPrimitive).content
        val navigatorLanguage = (tab.evaluate("navigator.language") as JsonPrimitive).content

        assertEquals(existingUserAgent, navigatorUserAgent)
        assertEquals("testLang", navigatorLanguage)
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

        tab.waitForReadyState("complete")

        val readyState = tab.evaluate("document.readyState") as? JsonPrimitive
        assertEquals("complete", readyState?.content)
        browser.stop()
    }

}
