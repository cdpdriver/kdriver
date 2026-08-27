package dev.kdriver.core.browser

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AwaitConnectionTest {

    @Test
    fun awaitConnection_whenTheBrowserAnswersImmediately_stopsPolling() = runTest {
        var attempts = 0

        val connected = awaitConnection(maxTries = 60, intervalMillis = 500) {
            attempts++
            true
        }

        assertTrue(connected)
        // The whole point: one answer is enough. The previous `repeat { … return@repeat }` kept
        // going for all 60 tries — and skipped the delay while doing so, so the browser got 60
        // requests back to back right after it started.
        assertEquals(1, attempts)
    }

    @Test
    fun awaitConnection_whenTheBrowserAnswersLate_stopsAtThatAttempt() = runTest {
        var attempts = 0

        val connected = awaitConnection(maxTries = 60, intervalMillis = 500) {
            attempts++
            attempts >= 3
        }

        assertTrue(connected)
        assertEquals(3, attempts)
    }

    @Test
    fun awaitConnection_whenTheBrowserNeverAnswers_triesExactlyMaxTimes() = runTest {
        var attempts = 0

        val connected = awaitConnection(maxTries = 5, intervalMillis = 500) {
            attempts++
            false
        }

        assertFalse(connected)
        assertEquals(5, attempts)
    }

    @Test
    fun awaitConnection_waitsBetweenAttempts() = runTest {
        var attempts = 0
        val start = testScheduler.currentTime

        awaitConnection(maxTries = 4, intervalMillis = 500) {
            attempts++
            false
        }

        // One interval per failed attempt, the last one included: that trailing wait is what the
        // caller's "waited for N ms" error message counts.
        assertEquals(4 * 500L, testScheduler.currentTime - start)
    }
}
