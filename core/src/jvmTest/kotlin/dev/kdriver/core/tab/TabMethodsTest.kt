package dev.kdriver.core.tab

import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.sampleFile
import kotlinx.coroutines.runBlocking
import kotlin.io.path.deleteIfExists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TabMethodsTest {

    @Test
    fun testBringToFront() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        tab.bringToFront()
        // Verify the tab is brought to front - no exception thrown

        browser.stop()
    }

    @Test
    fun testActivate() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        tab.activate()
        // Verify the tab is activated - no exception thrown

        browser.stop()
    }

    @Test
    fun testBackAndForward() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val url2 = sampleFile("profile.html")
        tab.get(url2)
        tab.waitForReadyState(ReadyState.COMPLETE)

        tab.back()
        tab.waitForReadyState(ReadyState.COMPLETE)

        tab.forward()
        tab.waitForReadyState(ReadyState.COMPLETE)

        browser.stop()
    }

    @Test
    fun testScreenshotB64() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val screenshot = tab.screenshotB64()
        assertNotNull(screenshot)
        assertTrue(screenshot.isNotEmpty())

        browser.stop()
    }

    @Test
    fun testScreenshotB64WithFormat() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val screenshot = tab.screenshotB64(format = ScreenshotFormat.PNG)
        assertNotNull(screenshot)
        assertTrue(screenshot.isNotEmpty())

        browser.stop()
    }

    @Test
    fun testSaveScreenshot() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val tempFile = kotlin.io.path.createTempFile(prefix = "test_screenshot_", suffix = ".png")
        try {
            val path = tab.saveScreenshot(kotlinx.io.files.Path(tempFile.toString()))
            assertNotNull(path)
            assertTrue(tempFile.toFile().exists())
            assertTrue(tempFile.toFile().length() > 0)
        } finally {
            tempFile.deleteIfExists()
        }

        browser.stop()
    }

    @Test
    fun testMaximize() = runBlocking {
        val browser = createBrowser(this, headless = false, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        tab.maximize()
        // Verify maximize was called - no exception thrown

        browser.stop()
    }

    @Test
    fun testMinimize() = runBlocking {
        val browser = createBrowser(this, headless = false, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        tab.minimize()
        // Verify minimize was called - no exception thrown

        browser.stop()
    }

    @Test
    fun testFullscreen() = runBlocking {
        val browser = createBrowser(this, headless = false, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        tab.fullscreen()
        // Verify fullscreen was called - no exception thrown

        browser.stop()
    }

    @Test
    fun testScrollUpAndDown() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        tab.scrollDown(100)
        // Verify scroll down was performed

        tab.scrollUp(100)
        // Verify scroll up was performed

        browser.stop()
    }

    @Test
    fun testGetWindow() = runBlocking {
        val browser = createBrowser(this, headless = false, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        val window = tab.getWindow()
        assertNotNull(window)
        assertNotNull(window.bounds)

        browser.stop()
    }

    @Test
    fun testSetWindowState() = runBlocking {
        val browser = createBrowser(this, headless = false, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        val initialWindow = tab.getWindow()
        assertNotNull(initialWindow.bounds)

        tab.setWindowState(
            left = initialWindow.bounds.left ?: 0,
            top = initialWindow.bounds.top ?: 0,
            width = 800,
            height = 600
        )

        val updatedWindow = tab.getWindow()
        assertEquals(800, updatedWindow.bounds.width)
        assertEquals(600, updatedWindow.bounds.height)

        browser.stop()
    }

    @Test
    fun testGetAllUrls() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("profile.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val urls = tab.getAllUrls()
        assertNotNull(urls)
        // URLs list should exist (may be empty if no links/assets in the page)

        browser.stop()
    }

    @Test
    fun testGetAllLinkedSources() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("profile.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val sources = tab.getAllLinkedSources()
        assertNotNull(sources)

        browser.stop()
    }

}
