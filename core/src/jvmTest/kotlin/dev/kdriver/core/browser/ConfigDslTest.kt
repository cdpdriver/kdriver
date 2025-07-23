package dev.kdriver.core.browser

import dev.kdriver.core.tab.ReadyState
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
        assertEquals(cfg, browser.config)
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