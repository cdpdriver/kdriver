package dev.kdriver.core.browser

import kotlinx.coroutines.delay

/**
 * Polls [isConnected] until it succeeds, at most [maxTries] times, waiting [intervalMillis] after
 * each failed attempt.
 *
 * Extracted from `DefaultBrowser.start` so the polling rule can be tested without a real browser.
 * It used to be written as `repeat(maxTries) { if (testConnection()) return@repeat; delay(…) }`,
 * which does not do what it reads like: `return@repeat` returns from the *lambda*, so it is a
 * `continue`, not a `break`. The loop therefore never stopped early — and since the skipped line was
 * the `delay`, a browser that answered on the first try still got all the remaining attempts fired
 * at it back to back, with no wait in between.
 *
 * @return true as soon as [isConnected] succeeded, false if all [maxTries] attempts failed.
 */
internal suspend fun awaitConnection(
    maxTries: Int,
    intervalMillis: Long,
    isConnected: suspend () -> Boolean,
): Boolean {
    repeat(maxTries) {
        if (isConnected()) return true
        delay(intervalMillis)
    }
    return false
}
