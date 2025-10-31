package dev.kdriver.core.tab

import dev.kdriver.cdp.cdp
import dev.kdriver.cdp.domain.Network
import dev.kdriver.cdp.domain.network
import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.connection.addHandler
import dev.kdriver.core.connection.send
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.sampleFile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.io.path.deleteIfExists
import kotlin.test.*

class TabTest {

    // User Agent Tests

    @Test
    fun testSetUserAgentSetsNavigatorValues() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

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
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

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

    // Evaluation Tests

    @Test
    fun testEvaluateWaitPromiseSuccess() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        val result = tab.evaluate<String>("new Promise(r => setTimeout(() => r(\"ok\")));", true)

        assertNotNull(result)
        assertEquals("ok", result)
        browser.stop()
    }

    @Test
    fun testEvaluateWaitPromiseFail() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        val result = assertFailsWith<EvaluateException> {
            tab.evaluate<String>("new Promise((_, r) => setTimeout(() => r(\"fail\")));", true)
        }

        assertEquals("fail", result.jsError)
        browser.stop()
    }

    @Test
    fun testEvaluateWaitPromiseError() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        val result = assertFailsWith<EvaluateException> {
            tab.evaluate<String>("(async() => { throw new Error(\"Custom error\") })()", true)
        }

        assertEquals("Error: Custom error\n    at <anonymous>:1:21\n    at <anonymous>:1:49", result.jsError)
        browser.stop()
    }

    // Element Selection Tests

    @Test
    fun testFindFindsElementByText() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val result = tab.find("Apples")

        assertNotNull(result)
        assertEquals("li", result.tag)
        assertEquals("Apples", result.text)
        browser.stop()
    }

    @Test
    fun testFindTimesOutIfElementNotFound() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val exception = assertFailsWith<TimeoutWaitingForElementException> {
            tab.find("Clothes", timeout = 1000)
        }
        assertEquals("Clothes", exception.selector)
        browser.stop()
    }

    @Test
    fun testSelect() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val result = tab.select("li[aria-label^='Apples']")

        assertNotNull(result)
        assertEquals("li", result.tag)
        assertEquals("Apples", result.text)
        browser.stop()
    }

    @Test
    fun testXpath() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val results = tab.xpath("//li[@aria-label=\"Apples (42)\"]")

        assertEquals(1, results.size)
        val result = results[0]

        assertNotNull(result)
        assertEquals("li", result.tag)
        assertEquals("Apples", result.text)
        browser.stop()
    }

    // Event Handler Tests

    @Test
    fun testHandlers() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
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
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        tab.waitForReadyState(ReadyState.COMPLETE)

        val readyState = tab.evaluate<ReadyState>("document.readyState")
        assertEquals(ReadyState.COMPLETE, readyState)
        browser.stop()
    }

    // Navigation Tests

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

    // Screenshot Tests

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

    // Window Management Tests

    @Test
    fun testMaximize() = runBlocking {
        // Skip on CI environments without display
        if (System.getenv("CI") != null && System.getenv("DISPLAY") == null) {
            return@runBlocking
        }

        val browser = createBrowser(this, headless = false, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        tab.maximize()
        // Verify maximize was called - no exception thrown

        browser.stop()
    }

    @Test
    fun testMinimize() = runBlocking {
        // Skip on CI environments without display
        if (System.getenv("CI") != null && System.getenv("DISPLAY") == null) {
            return@runBlocking
        }

        val browser = createBrowser(this, headless = false, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        tab.minimize()
        // Verify minimize was called - no exception thrown

        browser.stop()
    }

    @Test
    fun testFullscreen() = runBlocking {
        // Skip on CI environments without display
        if (System.getenv("CI") != null && System.getenv("DISPLAY") == null) {
            return@runBlocking
        }

        val browser = createBrowser(this, headless = false, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        tab.fullscreen()
        // Verify fullscreen was called - no exception thrown

        browser.stop()
    }

    @Test
    fun testGetWindow() = runBlocking {
        // Skip on CI environments without display
        if (System.getenv("CI") != null && System.getenv("DISPLAY") == null) {
            return@runBlocking
        }

        val browser = createBrowser(this, headless = false, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        val window = tab.getWindow()
        assertNotNull(window)
        assertNotNull(window.bounds)

        browser.stop()
    }

    @Test
    fun testSetWindowState() = runBlocking {
        // Skip on CI environments without display
        if (System.getenv("CI") != null && System.getenv("DISPLAY") == null) {
            return@runBlocking
        }

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

    // Scroll Tests

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

    // URL and Source Tests

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
