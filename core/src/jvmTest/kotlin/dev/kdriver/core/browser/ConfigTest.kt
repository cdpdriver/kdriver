package dev.kdriver.core.browser

import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConfigTest {

    @Test
    fun testCopyIsIndependent() {
        val original = Config(browserExecutablePath = Path("/usr/bin/chromium"))
        original.addArgument("--original-only")

        val copy = original.copy()
        copy.addArgument("--copy-only")
        copy.host = "127.0.0.1"
        copy.port = 1234

        assertTrue(copy.browserArgs.contains("--original-only"))
        assertTrue(copy.browserArgs.contains("--copy-only"))
        assertFalse(original.browserArgs.contains("--copy-only"))
        assertNull(original.host)
        assertNull(original.port)
    }

    @Test
    fun testCopyDoesNotDuplicateDefaultArguments() {
        val original = Config(browserExecutablePath = Path("/usr/bin/chromium"))

        val args = original.copy().copy().browserArgs

        assertEquals(args.distinct().size, args.size)
    }

    @Test
    fun testCopyKeepsCustomUserDataDirWithoutMaterialisingATemporaryOne() {
        val custom = Config(browserExecutablePath = Path("/usr/bin/chromium"))
        custom.userDataDir = Path("/tmp/kdriver-test-profile")
        assertEquals(custom.userDataDir, custom.copy().userDataDir)

        // A config that was never given one must not have a temp dir created for it by the copy.
        val default = Config(browserExecutablePath = Path("/usr/bin/chromium"))
        assertFalse(default.copy().usesCustomDataDir)
    }

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
