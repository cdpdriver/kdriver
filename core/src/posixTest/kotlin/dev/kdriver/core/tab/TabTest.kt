package dev.kdriver.core.tab

import dev.kdriver.core.browser.createBrowser
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

/** Tab tests on the native POSIX targets (macOS, Linux). */
class TabTest {

    /** The page's HTML, once it has loaded. */
    @Test
    fun testGetContentGetsHtmlContent() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get("https://example.com")
        // get() returns once the navigation has started: read the document once it has loaded.
        tab.waitForReadyState(ReadyState.COMPLETE)
        val content = tab.getContent()
        assertTrue(content.lowercase().startsWith("<!doctype html>"))
        browser.stop()
    }

}
