package dev.kdriver.core.browser

import io.ktor.util.logging.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The browsers this process started and has not stopped (nor detached) yet.
 *
 * When the host process exits without calling [Browser.stop], they are stopped from a single
 * shutdown hook (see [addShutdownHook]), which also deletes their temporary profiles. Without it, a
 * program that simply returns from `main` leaves a `kdriver_*` profile behind on every run.
 *
 * Only the JVM has such a hook: [addShutdownHook] does nothing on the native and JS targets, where
 * a browser that is never stopped keeps its profile on disk.
 *
 * One hook for the whole process rather than one per browser: a hook cannot be removed once added,
 * so a hook per browser would keep every browser ever created reachable until the process exits.
 * A browser leaves this registry as soon as it is stopped or detached.
 */
internal object StartedBrowsers {

    private val logger = KtorSimpleLogger("StartedBrowsers")
    private val mutex = Mutex()
    private val browsers = LinkedHashSet<Browser>()
    private var hookInstalled = false

    suspend fun register(browser: Browser) = mutex.withLock {
        if (!hookInstalled) {
            addShutdownHook { stopAll() }
            hookInstalled = true
        }
        browsers += browser
    }

    suspend fun unregister(browser: Browser) = mutex.withLock {
        browsers -= browser
    }

    suspend operator fun contains(browser: Browser): Boolean = mutex.withLock { browser in browsers }

    /** Stops every browser still registered, concurrently, so their grace periods overlap. */
    internal suspend fun stopAll() {
        val remaining = mutex.withLock { browsers.toList() }
        coroutineScope {
            remaining.forEach { browser ->
                launch {
                    try {
                        browser.stop()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.warn("Could not stop browser ${browser.pid} on exit: ${e.message}")
                    }
                }
            }
        }
    }

}
