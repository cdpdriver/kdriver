package dev.kdriver.core.browser

import kotlinx.coroutines.runBlocking
import kotlinx.io.files.Path
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The temporary profile kdriver creates when no `userDataDir` is given must be deleted once the
 * browser is stopped; a profile the caller supplied must never be.
 */
@OptIn(ExperimentalPathApi::class)
class TemporaryProfileTest {

    @Test
    fun stop_deletesTheTemporaryProfile() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val profile = File(browser.config.userDataDir.toString())
        assertFalse(browser.config.usesCustomDataDir)
        assertTrue(profile.isDirectory, "the browser runs on a temporary profile")
        assertTrue(profile.name.startsWith("kdriver_"))

        browser.stop()

        assertFalse(profile.exists(), "the temporary profile must be deleted by stop()")
        assertFalse(browser in StartedBrowsers, "a stopped browser is no longer stopped on exit")
    }

    @Test
    fun stop_keepsACustomUserDataDir() = runBlocking {
        val custom = createTempDirectory(prefix = "kdriver_custom_test_")
        try {
            val browser = createBrowser(this, userDataDir = Path(custom.toString()), headless = true, sandbox = false)
            assertTrue(browser.config.usesCustomDataDir)

            browser.stop()
            browser.cleanupTemporaryProfile()

            assertTrue(custom.exists(), "a userDataDir given by the caller must never be deleted")
            assertTrue(custom.toFile().list().orEmpty().isNotEmpty(), "the browser's files in it are kept too")
        } finally {
            // With retries: on Windows the browser's children may hold its files for a moment more.
            deleteTemporaryProfile(Path(custom.toString()))
        }
    }

    @Test
    fun secondStopAndCleanupAreHarmless() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val profile = File(browser.config.userDataDir.toString())

        browser.stop()
        browser.stop()
        browser.cleanupTemporaryProfile()
        browser.cleanupTemporaryProfile()

        assertFalse(profile.exists())
        assertTrue(browser.stopped)
    }

    @Test
    fun exitHook_stopsRunningBrowsersAndDeletesTheirProfiles() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val profile = File(browser.config.userDataDir.toString())
        assertTrue(browser in StartedBrowsers, "a launched browser is stopped on exit")

        // What the shutdown hook runs when the host process exits without calling stop().
        StartedBrowsers.stopAll()

        assertTrue(browser.stopped)
        assertFalse(profile.exists(), "the temporary profile must be deleted on exit")
    }

    @Test
    fun copyOfAConfigDoesNotOwnItsTemporaryProfile() {
        val original = Config(browserExecutablePath = Path("/usr/bin/chromium"))
        val profile = original.userDataDir
        try {
            assertTrue(original.createdTemporaryProfile)
            val copy = original.copy()
            assertEquals(profile, copy.userDataDir, "the copy shares the profile…")
            assertFalse(copy.createdTemporaryProfile, "…so it must not delete it")
        } finally {
            deleteRecursively(profile)
        }
    }

    @Test
    fun deleteRecursively_removesNestedContent() {
        val root = createTempDirectory(prefix = "kdriver_delete_test_")
        Files.createDirectories(root.resolve("Default/Cache"))
        root.resolve("Default/Cache/data").writeText("x")
        root.resolve("Local State").writeText("{}")

        assertTrue(deleteRecursively(Path(root.toString())))
        assertFalse(root.exists())
    }

    @Test
    fun deleteRecursively_doesNotFollowSymbolicLinks() {
        val outside = createTempDirectory(prefix = "kdriver_outside_test_")
        val kept = outside.resolve("kept").also { it.writeText("keep me") }
        val root = createTempDirectory(prefix = "kdriver_delete_test_")
        try {
            val linked = runCatching { Files.createSymbolicLink(root.resolve("link"), outside) }
            // Creating a symbolic link needs a privilege Windows does not grant by default.
            if (linked.isFailure) return

            assertTrue(deleteRecursively(Path(root.toString())))

            assertFalse(root.exists())
            assertTrue(kept.exists(), "what a link points to must never be deleted")
        } finally {
            root.deleteRecursively()
            outside.deleteRecursively()
        }
    }

    @Test
    fun deleteRecursively_isTrueForAMissingDirectory() {
        val missing = createTempDirectory(prefix = "kdriver_missing_test_").also { Files.delete(it) }

        assertTrue(deleteRecursively(Path(missing.toString())))
    }

}
