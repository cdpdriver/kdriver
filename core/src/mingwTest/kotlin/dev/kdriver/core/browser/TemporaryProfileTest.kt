package dev.kdriver.core.browser

import kotlinx.coroutines.runBlocking
import kotlinx.io.files.SystemFileSystem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TemporaryProfileTest {

    @Test
    fun stop_deletesTheTemporaryProfile() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val profile = browser.config.userDataDir
        assertTrue(SystemFileSystem.exists(profile), "the browser runs on a temporary profile")

        browser.stop()
        assertFalse(SystemFileSystem.exists(profile), "the temporary profile must be deleted by stop()")

        // Stopping or cleaning up again is harmless.
        browser.stop()
        browser.cleanupTemporaryProfile()
        assertFalse(SystemFileSystem.exists(profile))
    }

}
