package dev.kdriver.core.browser

import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConfigTest {

    @Test
    fun testAddArgument() {
        val config = Config(browserExecutablePath = Path("/usr/bin/chromium"))

        config.addArgument("--disable-gpu")

        assertTrue(config.browserArgs.contains("--disable-gpu"))
    }

    @Test
    fun testAddArgumentForbiddenHeadless() {
        val config = Config(browserExecutablePath = Path("/usr/bin/chromium"))

        assertFailsWith<IllegalArgumentException> {
            config.addArgument("--headless")
        }
    }

    @Test
    fun testAddArgumentForbiddenDataDir() {
        val config = Config(browserExecutablePath = Path("/usr/bin/chromium"))

        assertFailsWith<IllegalArgumentException> {
            config.addArgument("--user-data-dir=/tmp/test")
        }
    }

    @Test
    fun testAddArgumentForbiddenSandbox() {
        val config = Config(browserExecutablePath = Path("/usr/bin/chromium"))

        assertFailsWith<IllegalArgumentException> {
            config.addArgument("--no-sandbox")
        }
    }

    @Test
    fun testGetBrowserArgs() {
        val config = Config(
            browserExecutablePath = Path("/usr/bin/chromium"),
            browserArgs = listOf("--disable-gpu", "--window-size=1920,1080")
        )

        val args = config.browserArgs
        assertTrue(args.contains("--disable-gpu"))
        assertTrue(args.contains("--window-size=1920,1080"))
        assertTrue(args.contains("--remote-allow-origins=*"))
    }

    @Test
    fun testSetUserDataDir() {
        val config = Config(browserExecutablePath = Path("/usr/bin/chromium"))
        val customDir = Path("/tmp/custom-profile")

        config.userDataDir = customDir

        assertEquals(customDir, config.userDataDir)
        assertTrue(config.usesCustomDataDir)
    }

    @Test
    fun testConfigInvokeBasic() {
        val config = Config(
            browserExecutablePath = Path("/usr/bin/chromium"),
            headless = false,
            sandbox = true
        )

        val args = config.invoke()

        assertTrue(args.contains("--remote-allow-origins=*"))
        assertTrue(args.any { it.startsWith("--user-data-dir=") })
    }

    @Test
    fun testConfigInvokeHeadless() {
        val config = Config(
            browserExecutablePath = Path("/usr/bin/chromium"),
            headless = true
        )

        val args = config.invoke()

        assertTrue(args.contains("--headless=new"))
    }

    @Test
    fun testConfigInvokeNoSandbox() {
        val config = Config(
            browserExecutablePath = Path("/usr/bin/chromium"),
            sandbox = false
        )

        val args = config.invoke()

        assertTrue(args.contains("--no-sandbox"))
    }

    @Test
    fun testConfigInvokeWithUserAgent() {
        val config = Config(
            browserExecutablePath = Path("/usr/bin/chromium"),
            userAgent = "Custom User Agent"
        )

        val args = config.invoke()

        assertTrue(args.any { it.contains("--user-agent=Custom User Agent") })
    }

    @Test
    fun testConfigInvokeWithHostAndPort() {
        val config = Config(
            browserExecutablePath = Path("/usr/bin/chromium"),
            host = "127.0.0.1",
            port = 9222
        )

        val args = config.invoke()

        assertTrue(args.contains("--remote-debugging-host=127.0.0.1"))
        assertTrue(args.contains("--remote-debugging-port=9222"))
    }

    @Test
    fun testConfigInvokeExpertMode() {
        val config = Config(
            browserExecutablePath = Path("/usr/bin/chromium"),
            expert = true
        )

        val args = config.invoke()

        assertTrue(args.contains("--disable-web-security"))
        assertTrue(args.contains("--disable-site-isolation-trials"))
    }

}
