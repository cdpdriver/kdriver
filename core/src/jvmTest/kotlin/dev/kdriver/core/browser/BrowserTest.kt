package dev.kdriver.core.browser

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BrowserTest {

    @Test
    fun testGetContentGetsHtmlContent() = runBlocking {
        val browser = Browser.create(headless = true, sandbox = false)
        val tab = browser.get("https://example.com")
        val content = tab.getContent()
        assertTrue(content.lowercase().startsWith("<!doctype html>"))
        browser.stop()
    }

    @Test
    fun testUpdateTargetSetsTargetTitle() = runBlocking {
        val browser = Browser.create(headless = true, sandbox = false)
        val tab = browser.get("https://example.com")
        tab.updateTarget()
        assertNotNull(tab.targetInfo)
        assertEquals("Example Domain", tab.targetInfo?.title)
        browser.stop()
    }

}
