package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.browser
import io.ktor.util.logging.Logger
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.files.Path
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteRecursively
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

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
    fun cleanup_keepsTheProfileWhileTheBrowserStillRuns() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false) as DefaultBrowser
        val profile = File(browser.config.userDataDir.toString())
        val logger = mockk<Logger>(relaxed = true)
        browser.logger = logger
        browser.profileReleaseTimeoutMillis = 300
        try {
            // Not stopped: the browser keeps writing to its profile, which must not be deleted under it.
            val waited = measureTime { browser.cleanupTemporaryProfile() }

            assertTrue(profile.isDirectory, "a running browser's profile must be kept")
            assertTrue(waited >= 300.milliseconds, "it waits for the process to exit first, waited $waited")
            verify { logger.warn(match<String> { "is still running" in it && profile.path in it }) }
        } finally {
            browser.stop()
        }
        assertFalse(profile.exists(), "stop() deletes it once the browser is gone")
    }

    @Test
    fun stop_keepsATemporaryProfileInheritedThroughCopy() = runBlocking {
        val config = Config(headless = true, sandbox = false)
        // Reading it creates the temporary profile on this config: the browser's own copy inherits it.
        val profile = File(config.userDataDir.toString())
        try {
            val browser = createBrowser(this, config)
            assertEquals(config.userDataDir, browser.config.userDataDir, "the browser runs on the inherited profile")
            assertFalse(browser.config.createdTemporaryProfile)

            browser.stop()

            assertTrue(profile.isDirectory, "a profile the browser did not create must not be deleted")
            assertFalse(browser in StartedBrowsers)
        } finally {
            deleteTemporaryProfile(Path(profile.path))
        }
    }

    @Test
    fun start_deletesTheProfileWhenTheProcessCannotBeLaunched() = runBlocking {
        // An existing file that is not an executable: launching it fails on every platform.
        val notABrowser = createTempFile(prefix = "kdriver_not_a_browser_", suffix = ".txt")
        notABrowser.writeText("not a browser")
        try {
            val browser = DefaultBrowser(this, Config(browserExecutablePath = Path(notABrowser.toString())))

            assertFailsWith<IOException> { browser.start() }

            val profile = File(browser.config.userDataDir.toString())
            assertTrue(profile.name.startsWith("kdriver_"), "the start created a temporary profile")
            assertFalse(profile.exists(), "a profile no browser ever ran on is deleted at once")
            assertFalse(browser in StartedBrowsers)
        } finally {
            Files.deleteIfExists(notABrowser)
        }
    }

    @Test
    fun create_stopsTheBrowserItLaunchedWhenTheStartFailsAfterTheLaunch() = runBlocking {
        var launched: DefaultBrowser? = null
        val failure = assertFailsWith<IllegalStateException> {
            DefaultBrowser.create(this, Config(headless = true, sandbox = false)) { scope, config ->
                object : DefaultBrowser(scope, config) {
                    override suspend fun start(): Browser {
                        super.start()
                        error("target discovery failed")
                    }
                }.also { launched = it }
            }
        }
        assertEquals("target discovery failed", failure.message)

        // The caller never got this instance, so create() had to stop it.
        val browser = assertNotNull(launched)
        assertTrue(browser.stopped, "the browser process must be stopped")
        assertFalse(File(browser.config.userDataDir.toString()).exists(), "and its profile deleted")
        assertFalse(browser in StartedBrowsers)
    }

    @Test
    fun start_afterTheProcessDied_relaunchesOnTheSameProfileAndHandsItsDeletionOver() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val profile = File(browser.config.userDataDir.toString())
        killFromOutside(browser)

        val restarted = browser.start()
        try {
            assertNotSame(browser, restarted)
            assertFalse(restarted.stopped, "a new browser process runs")
            assertEquals(browser.config.userDataDir, restarted.config.userDataDir, "on the same profile")
            assertNotNull(restarted.pid, "it launched a process, rather than connecting to the dead one's port")
            assertFalse(browser in StartedBrowsers, "the dead browser is no longer stopped on exit")
            assertTrue(restarted in StartedBrowsers)

            browser.cleanupTemporaryProfile()
            assertTrue(profile.isDirectory, "the dead browser no longer deletes the profile the new one runs on")
        } finally {
            restarted.stop()
            browser.stop()
        }
        assertFalse(profile.exists(), "the new browser deletes it when it stops")
    }

    @Test
    fun stop_afterTheProcessDied_stillDeletesTheProfile() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val profile = File(browser.config.userDataDir.toString())
        killFromOutside(browser)

        // Nothing answers the graceful close anymore: that must not keep stop() from cleaning up.
        browser.stop()

        assertFalse(profile.exists(), "a crashed browser's profile is deleted by stop() too")
        assertFalse(browser in StartedBrowsers)
    }

    @Test
    fun start_afterTheProcessDied_deletesTheProfileWhenTheRelaunchFails() = runBlocking {
        val browser = DefaultBrowser.create(this, Config(headless = true, sandbox = false)) { scope, config ->
            object : DefaultBrowser(scope, config) {
                override suspend fun relaunch(config: Config): DefaultBrowser = throw IOException("no browser")
            }
        }
        val profile = File(browser.config.userDataDir.toString())
        try {
            killFromOutside(browser)

            assertFailsWith<IOException> { browser.start() }

            assertFalse(profile.exists(), "nothing runs on the profile anymore: it is deleted")
            assertFalse(browser in StartedBrowsers)
        } finally {
            browser.stop()
        }
    }

    @Test
    fun stop_whenTheProfileCannotBeDeleted_keepsTheBrowserForTheExitHook() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false) as DefaultBrowser
        val profile = File(browser.config.userDataDir.toString())
        val logger = mockk<Logger>(relaxed = true)
        browser.logger = logger
        var attempts = 0
        browser.deleteProfile = {
            attempts++
            false
        }

        browser.stop()

        assertTrue(browser.stopped)
        assertEquals(1, attempts)
        assertTrue(profile.isDirectory, "a profile that could not be deleted stays on disk")
        verify { logger.warn(match<String> { "Could not delete temporary profile" in it && profile.path in it }) }
        assertTrue(browser in StartedBrowsers, "the exit hook must try again")

        // The lock is gone by the time the host process exits.
        browser.deleteProfile = { deleteTemporaryProfile(it) }
        StartedBrowsers.stopAll()

        assertFalse(profile.exists(), "the exit hook deletes it")
        assertFalse(browser in StartedBrowsers)
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
    fun deleteTemporaryProfile_givesUpOnAnEntryItCannotDelete() = runBlocking {
        // A read-only directory protects its entries on POSIX only, and never from root.
        if (!isPosix() || isRoot()) return@runBlocking
        val root = createTempDirectory(prefix = "kdriver_delete_test_")
        val locked = Files.createDirectories(root.resolve("locked"))
        locked.resolve("file").writeText("x")
        locked.toFile().setWritable(false)
        try {
            assertFalse(deleteTemporaryProfile(Path(root.toString()), attempts = 2, retryDelayMillis = 10))
            assertTrue(locked.resolve("file").exists())
        } finally {
            locked.toFile().setWritable(true)
            root.deleteRecursively()
        }
    }

    @Test
    fun deleteRecursively_isTrueForAMissingDirectory() {
        val missing = createTempDirectory(prefix = "kdriver_missing_test_").also { Files.delete(it) }

        assertTrue(deleteRecursively(Path(missing.toString())))
    }

    @Test
    fun deleteTemporaryProfile_retriesUntilTheProfileIsGone() = runTest {
        val profile = Path("kdriver_profile")
        val seen = mutableListOf<Path>()

        val gone = deleteTemporaryProfile(profile, attempts = 5, retryDelayMillis = 200) {
            seen += it
            seen.size == 3
        }

        assertTrue(gone)
        assertEquals(listOf(profile, profile, profile), seen, "stops at the first attempt that succeeds")
        assertEquals(400, currentTime, "waits between two attempts")
    }

    @Test
    fun deleteTemporaryProfile_givesUpAfterItsLastAttempt() = runTest {
        var attempts = 0

        val gone = deleteTemporaryProfile(Path("kdriver_profile"), attempts = 4, retryDelayMillis = 200) {
            attempts++
            false
        }

        assertFalse(gone)
        assertEquals(4, attempts)
        assertEquals(600, currentTime, "no wait after the last attempt")
    }

    @Test
    fun deleteTemporaryProfile_triesTenTimesOverAboutTwoSecondsByDefault() = runTest {
        var attempts = 0

        assertFalse(deleteTemporaryProfile(Path("kdriver_profile")) { attempts++; false })

        assertEquals(10, attempts)
        assertEquals(1_800, currentTime)
    }

    @Test
    fun exitHook_stopsTheOtherBrowsersWhenOneFailsToStop() = runBlocking {
        val scope = this
        val unused = Config(browserExecutablePath = Path("unused"))
        val failing = object : DefaultBrowser(scope, unused) {
            override suspend fun stop() {
                throw IllegalStateException("cannot stop")
            }
        }
        val stopped = mutableListOf<Browser>()
        val others = List(2) {
            object : DefaultBrowser(scope, unused) {
                override suspend fun stop() {
                    stopped += this
                }
            }
        }
        val all = listOf(failing) + others
        all.forEach { StartedBrowsers.register(it) }
        try {
            StartedBrowsers.stopAll()

            assertEquals(others.toSet(), stopped.toSet(), "a failure must not keep the other browsers running")
        } finally {
            all.forEach { StartedBrowsers.unregister(it) }
        }
    }

    /** Ends the browser process behind kdriver's back, as a crash would, and waits until it is gone. */
    private suspend fun killFromOutside(browser: Browser) {
        try {
            withTimeoutOrNull(5.seconds) { browser.connection?.browser?.close() }
        } catch (_: IOException) {
            // The browser may drop the connection before it answers: its exit is what is awaited below.
        }
        withTimeout(10.seconds) {
            while (!browser.stopped) delay(50)
        }
    }

}
