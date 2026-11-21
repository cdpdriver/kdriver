package dev.kdriver.core

import dev.kdriver.cdp.domain.target
import dev.kdriver.core.browser.Browser
import dev.kdriver.core.tab.Tab
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

fun sampleFile(name: String): String {
    val resource = checkNotNull(Thread.currentThread().contextClassLoader.getResource(name)) {
        "Resource '$name' not found"
    }
    return resource.toURI().toString()
}

/**
 * Waits until the browser reports [title] for this tab's target, then refreshes [Tab.targetInfo].
 *
 * `Browser.get` returns once the navigation has started, while the target's title is still its URL:
 * Chrome only reports the page's title once the page has parsed it. This waits for that event, with
 * no fixed sleep, and fails after [timeoutMillis]: generous, since the first page load of a run, on a
 * cold runner, has been seen to take more than ten seconds.
 */
suspend fun Tab.awaitTitle(browser: Browser, title: String, timeoutMillis: Long = 30_000) {
    val targetId = checkNotNull(targetInfo?.targetId) { "the tab has no target" }
    val events = checkNotNull(browser.connection) { "the browser is not started" }.target.targetInfoChanged
    val loaded = withTimeoutOrNull(timeoutMillis) {
        coroutineScope {
            // Subscribed before the current title is read, so the event cannot slip in between.
            val titled = async(start = CoroutineStart.UNDISPATCHED) {
                events.first { it.targetInfo.targetId == targetId && it.targetInfo.title == title }
            }
            updateTarget()
            if (targetInfo?.title != title) {
                titled.await()
                updateTarget()
            }
            titled.cancel()
        }
    }
    checkNotNull(loaded) { "the target still reports the title '${targetInfo?.title}' after $timeoutMillis ms, not '$title'" }
}
