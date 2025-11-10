package dev.kdriver.core.browser

import dev.kdriver.core.extensions.addExtension
import dev.kdriver.core.sampleFile
import dev.kdriver.core.samplePath
import dev.kdriver.core.tab.ReadyState
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BrowserTest {

    @Test
    fun testBrowserScanBotDetection() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get("https://www.browserscan.net/bot-detection")
        tab.waitForReadyState(ReadyState.COMPLETE)
        tab.wait(2000)
        val element = tab.findElementByText("Test Results:")
        assertNotNull(element)
        assertNotNull(element.parent)
        assertEquals("Normal", element.parent!!.children.last().text)
        browser.stop()
    }

    @Test
    fun testGetContentGetsHtmlContent() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get("https://example.com")
        val content = tab.getContent()
        assertTrue(content.lowercase().startsWith("<!doctype html>"))
        browser.stop()
    }

    @Test
    fun testUpdateTargetSetsTargetTitle() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get("https://example.com")
        tab.updateTarget()
        assertNotNull(tab.targetInfo)
        assertEquals("Example Domain", tab.targetInfo?.title)
        browser.stop()
    }

    @Test
    fun testMultipleBrowsersDiffUserData() = runBlocking {
        val browser1 = createBrowser(this, headless = true, sandbox = false)
        val browser2 = createBrowser(this, headless = true, sandbox = false)
        val browser3 = createBrowser(this, headless = true, sandbox = false)

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

    @Test
    fun testBrowserWait() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)

        val result = browser.wait(100)

        assertNotNull(result)
        browser.stop()
    }

    @Test
    fun testGetWebsocketUrl() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)

        val url = browser.websocketUrl

        assertNotNull(url)
        assertTrue(url.startsWith("ws://"))
        browser.stop()
    }

    @Test
    fun testGetTabs() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        browser.get(sampleFile("groceries.html"))

        val tabs = browser.tabs

        assertNotNull(tabs)
        assertTrue(tabs.isNotEmpty())
        browser.stop()
    }

    @Test
    fun testGetTargets() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)

        val targets = browser.targets

        assertNotNull(targets)
        browser.stop()
    }

    @Test
    fun testUpdateTargets() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        browser.get(sampleFile("groceries.html"))

        browser.updateTargets()

        assertTrue(browser.targets.isNotEmpty())
        browser.stop()
    }

    @Test
    fun testGetWithNewTab() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)

        val initialTabCount = browser.tabs.size
        browser.get(sampleFile("profile.html"), newTab = true)

        assertTrue(browser.tabs.size > initialTabCount)
        browser.stop()
    }

    @Test
    fun testExtensionLoading() = runBlocking {
        val config = Config(headless = true, sandbox = false)
        config.addExtension(samplePath("extension"))

        assertTrue(config.extensions.isNotEmpty())
        assertEquals(1, config.extensions.size)

        val browser = createBrowser(this, config)
        assertNotNull(browser)
        assertNotNull(browser.connection)

        // Navigate to a test page
        val tab = browser.get("https://example.com")
        tab.waitForReadyState(ReadyState.COMPLETE)

        // Wait for extension to inject content
        tab.wait(5000)

        // Verify the extension marker element exists
        val markerElement = tab.select("#kdriver-extension-marker", timeout = 10000)
        assertNotNull(
            markerElement,
            "Extension marker element should exist, indicating the extension loaded successfully"
        )
        assertEquals("KDriver Extension Active", markerElement.text)

        // Verify the data attribute
        val dataAttr = markerElement["data-extension-loaded"]
        assertEquals("true", dataAttr)

        browser.stop()
    }

}
