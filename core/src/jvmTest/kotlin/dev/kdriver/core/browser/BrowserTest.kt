package dev.kdriver.core.browser

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BrowserTest {

    @Test
    fun testGetContentGetsHtmlContent() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.get("https://example.com")
        val content = tab.getContent()
        assertTrue(content.lowercase().startsWith("<!doctype html>"))
        browser.stop()
    }

    @Test
    fun testUpdateTargetSetsTargetTitle() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val tab = browser.get("https://example.com")
        tab.updateTarget()
        assertNotNull(tab.targetInfo)
        assertEquals("Example Domain", tab.targetInfo?.title)
        browser.stop()
    }

    @Test
    fun testMultipleBrowsersDiffUserData() = runBlocking {
        val browser1 = Browser.create(this, headless = true, sandbox = false)
        val browser2 = Browser.create(this, headless = true, sandbox = false)
        val browser3 = Browser.create(this, headless = true, sandbox = false)

        assertTrue(!browser1.config.usesCustomDataDir)
        assertTrue(!browser2.config.usesCustomDataDir)
        assertTrue(!browser3.config.usesCustomDataDir)

        val ports = setOf(
            browser1.config.port,
            browser2.config.port,
            browser3.config.port
        )
        assertEquals(3, ports.size)

        val userDataDirs = setOf(
            browser1.config.userDataDir,
            browser2.config.userDataDir,
            browser3.config.userDataDir
        )
        assertEquals(3, userDataDirs.size)

        val page1 = browser1.get("https://example.com/one")
        page1.wait()
        assertNotNull(page1.targetInfo)
        assertEquals("Example Domain", page1.targetInfo?.title)

        val page2 = browser2.get("https://example.com/two")
        page2.wait()
        assertNotNull(page2.targetInfo)
        assertEquals("Example Domain", page2.targetInfo?.title)

        val page3 = browser3.get("https://example.com/three")
        page3.wait()
        assertNotNull(page3.targetInfo)
        assertEquals("Example Domain", page3.targetInfo?.title)

        browser1.stop()
        browser2.stop()
        browser3.stop()
    }

}
