package dev.kdriver.core.tab

import dev.kdriver.core.browser.Browser
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class TabTest {

    @Test
    fun testSetUserAgentSetsNavigatorValues() = runBlocking {
        val browser = Browser.create(headless = true)
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
    }

    @Test
    fun testSetUserAgentDefaultsExistingUserAgent() = runBlocking {
        val browser = Browser.create(headless = true)
        val tab = browser.mainTab ?: throw IllegalStateException("Main tab is not available")

        val existingUserAgent = (tab.evaluate("navigator.userAgent") as JsonPrimitive).content

        tab.setUserAgent(
            acceptLanguage = "testLang"
        )

        val navigatorUserAgent = (tab.evaluate("navigator.userAgent") as JsonPrimitive).content
        val navigatorLanguage = (tab.evaluate("navigator.language") as JsonPrimitive).content

        assertEquals(existingUserAgent, navigatorUserAgent)
        assertEquals("testLang", navigatorLanguage)
    }

}
