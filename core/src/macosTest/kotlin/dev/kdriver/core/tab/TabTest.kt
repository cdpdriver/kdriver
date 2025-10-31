package dev.kdriver.core.tab

import dev.kdriver.core.browser.createBrowser
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class TabTest {

    @Test
    fun testGetContentGetsHtmlContent() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get("https://example.com")
        val content = tab.getContent()
        assertTrue(content.lowercase().startsWith("<!doctype html>"))
        browser.stop()
    }

}
