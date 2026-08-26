package dev.kdriver.core.browser

import dev.kdriver.core.tab.ReadyState
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConfigDslTest {

    @Test
    fun testCreateBrowserWithDslConfig() = runBlocking {
        val cfg = config {
            headless = true
            sandbox = false
        }

        val browser = createBrowser(this, cfg)

        val tab = browser.get("https://example.com")

        assertTrue(cfg.headless)
        // The browser works on its own copy, so starting it leaves the caller's config untouched
        // and the same instance can be reused for another browser.
        assertNotSame(cfg, browser.config)
        assertEquals(cfg.headless, browser.config.headless)
        assertEquals(cfg.sandbox, browser.config.sandbox)
        assertNull(cfg.port)
        assertNotNull(browser.config.port)
        assertNotNull(tab.getContent())

        browser.stop()
    }

    @Test
    fun testCreateBrowserWithConvenienceOverload() = runBlocking {
        val browser = createBrowser(this) {
            headless = true
            sandbox = false
            userAgent = "Test Browser"
        }

        val tab = browser.get("https://example.com")

        assertTrue(browser.config.headless)
        assertEquals(false, browser.config.sandbox)
        assertEquals("Test Browser", browser.config.userAgent)
        assertNotNull(tab.getContent())

        browser.stop()
    }

}