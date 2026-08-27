package dev.kdriver.core.browser

import kotlin.test.Test
import kotlin.test.assertTrue

class StartFailureMessageTest {

    private fun message(
        alive: Boolean = false,
        exitCode: Int? = null,
        stderr: String? = null,
        error: Throwable? = null,
    ) = browserStartFailureMessage(
        endpoint = "127.0.0.1:53997", waitedMs = 30500,
        fate = processFate(1916, alive, exitCode), lastConnectionError = error, stderr = stderr,
    )

    @Test
    fun message_alwaysNamesTheEndpointTheWaitAndThePid() {
        val m = message()
        assertTrue("127.0.0.1:53997" in m, m)
        assertTrue("30500ms" in m, m)
        assertTrue("pid=1916" in m, m)
    }

    /**
     * Reported as-is, with no interpretation: we are reading the exit status precisely because we do
     * not know yet which statuses this failure produces. A zero must not be dressed up as anything.
     */
    @Test
    fun message_reportsTheExitStatusVerbatim() {
        assertTrue("exited with 0" in message(exitCode = 0), message(exitCode = 0))
        assertTrue("exited with 21" in message(exitCode = 21), message(exitCode = 21))
    }

    @Test
    fun message_whenTheBrowserIsStillRunning_saysSoRatherThanGuessing() {
        val m = message(alive = true, exitCode = null)
        assertTrue("still running" in m, m)
    }

    @Test
    fun message_whenTheExitStatusIsUnavailable_doesNotClaimACleanExit() {
        val m = message(alive = false, exitCode = null)
        assertTrue("exit status unavailable" in m, m)
        assertTrue("exited with" !in m, "an unknown status must not be reported as an exit code: $m")
    }

    @Test
    fun message_reportsStderrWhenThereIsSomeAndSaysSoWhenThereIsNot() {
        assertTrue("<none>" in message(stderr = "   "), "blank stderr reads as none")
        assertTrue("Fontconfig error" in message(stderr = "Fontconfig error\n"), "stderr is quoted")
    }

    @Test
    fun message_reportsTheLastConnectionError() {
        val m = message(error = IllegalStateException("Connection refused"))
        assertTrue("IllegalStateException: Connection refused" in m, m)
        assertTrue("none" in message(error = null), "says none when there was no error")
    }
}
