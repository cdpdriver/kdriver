package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Network
import dev.kdriver.core.sampleFile
import dev.kdriver.core.tab.ReadyState
import dev.kdriver.core.tab.evaluate
import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import kotlin.io.path.deleteIfExists
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BrowserTest {

    // Cookie Tests

    @Test
    fun testSetAllAndGetAllCookies() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        browser.cookies.setAll(
            listOf(
                Network.CookieParam(
                    name = "session",
                    value = "abc123",
                    domain = "example.com",
                    path = "/",
                )
            )
        )

        val cookie = browser.cookies.getAll().find { it.name == "session" }
        assertNotNull(cookie)
        assertEquals("abc123", cookie.value)
        assertEquals("example.com", cookie.domain)

        browser.stop()
    }

    @Test
    fun testClearCookies() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        browser.cookies.setAll(
            listOf(
                Network.CookieParam(
                    name = "toDrop",
                    value = "1",
                    domain = "example.com",
                    path = "/",
                )
            )
        )
        assertTrue(browser.cookies.getAll().isNotEmpty())

        browser.cookies.clear()

        assertTrue(browser.cookies.getAll().isEmpty())
        browser.stop()
    }

    @Test
    fun testSaveAndLoadCookies() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        browser.cookies.setAll(
            listOf(
                Network.CookieParam(
                    name = "kept",
                    value = "yes",
                    domain = "example.com",
                    path = "/",
                )
            )
        )

        val tempFile = kotlin.io.path.createTempFile(prefix = "test_cookies_", suffix = ".dat")
        try {
            val path = Path(tempFile.toString())
            val saved = browser.cookies.save(path)
            assertTrue(saved.any { it.name == "kept" })

            browser.cookies.clear()
            assertTrue(browser.cookies.getAll().isEmpty())

            browser.cookies.load(path)

            val restored = browser.cookies.getAll().find { it.name == "kept" }
            assertNotNull(restored)
            assertEquals("yes", restored.value)
        } finally {
            tempFile.deleteIfExists()
        }

        browser.stop()
    }

    @Test
    fun testSavePatternOnlyKeepsMatchingCookies() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        browser.cookies.setAll(
            listOf(
                Network.CookieParam(
                    name = "wanted",
                    value = "nowsecure",
                    domain = "example.com",
                    path = "/",
                ),
                Network.CookieParam(
                    name = "ignored",
                    value = "somethingelse",
                    domain = "example.com",
                    path = "/",
                ),
            )
        )

        val tempFile = kotlin.io.path.createTempFile(prefix = "test_cookies_", suffix = ".dat")
        try {
            val saved = browser.cookies.save(Path(tempFile.toString()), Regex("nowsecure"))

            // The filter has to actually apply, which it did not in the implementation this is ported from.
            assertEquals(listOf("wanted"), saved.map { it.name })
        } finally {
            tempFile.deleteIfExists()
        }

        browser.stop()
    }

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
        // get() returns once the navigation has started: read the document once it has loaded.
        tab.waitForReadyState(ReadyState.COMPLETE)
        val content = tab.getContent()
        assertTrue(content.lowercase().startsWith("<!doctype html>"))
        browser.stop()
    }

    @Test
    fun testUpdateTargetSetsTargetTitle() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get("https://example.com")
        // Without this the title is read while the page may still be about:blank, which is what
        // makes this test fail intermittently on slower runners.
        tab.waitForReadyState(ReadyState.COMPLETE)
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

        val page1 = browser1.get(sampleFile("groceries.html"))
        // Local pages: the test is about the browsers, not about how fast a remote site answers. Read once
        // loaded, from the document: get() returns once the navigation has started, and the target's title
        // can still be its URL for a while after the load.
        page1.waitForReadyState(ReadyState.COMPLETE)
        assertNotNull(page1.targetInfo)
        assertEquals("Grocery List", page1.evaluate<String>("document.title"))

        val page2 = browser2.get(sampleFile("lookup-test.html"))
        page2.waitForReadyState(ReadyState.COMPLETE)
        assertNotNull(page2.targetInfo)
        assertEquals("Lookup", page2.evaluate<String>("document.title"))

        val page3 = browser3.get(sampleFile("profile.html"))
        page3.waitForReadyState(ReadyState.COMPLETE)
        assertNotNull(page3.targetInfo)
        assertEquals("Fetch Example", page3.evaluate<String>("document.title"))

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

}
