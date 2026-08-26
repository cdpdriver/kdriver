package dev.kdriver.core.browser

import kotlinx.coroutines.delay
import kotlinx.io.files.Path

expect abstract class Process {
    fun isAlive(): Boolean
    fun pid(): Long
    abstract fun destroy()
}

/**
 * Reads whatever is currently buffered on the process's stderr, or null if unavailable.
 *
 * Bounded in size and time on purpose: the stream stays open for the process's whole life, so an
 * unbounded read would block until it exits rather than returning what the browser has said so far.
 *
 * Returns null where stderr is not captured (Linux, where the child simply inherits ours).
 */
expect suspend fun Process.readStderrSnapshot(
    maxBytes: Int = 64 * 1024,
    timeoutMillis: Long = 250,
): String?

/**
 * Forcibly kills the process and, where the platform supports it, its whole tree.
 *
 * [Process.destroy] only asks politely (SIGTERM / TerminateProcess on the top-level process). On
 * Windows in particular, a browser's renderer and GPU children survive it.
 */
expect fun Process.killTree()

/**
 * Terminates the process, then waits until it has actually gone.
 *
 * [Process.destroy] only *requests* termination: it returns immediately, and it does not reach the
 * browser's child processes. Those children keep open handles on the profile directory, so a browser
 * started on the same `--user-data-dir` shortly afterwards cannot take ownership of it: it hangs
 * before opening its debug port, and the start times out with no visible cause. Callers therefore
 * need to know the browser is really gone, not merely that it was asked to leave.
 *
 * Grace-waits for [gracePeriodMillis], then escalates to [killTree] and waits up to
 * [killTimeoutMillis] more. The same shape as zendriver's `Browser.stop`.
 *
 * @return true if the process exited, false if it was still alive when the timeouts elapsed.
 */
suspend fun Process.destroyAndAwaitExit(
    gracePeriodMillis: Long = 3_000,
    killTimeoutMillis: Long = 5_000,
): Boolean {
    if (!isAlive()) return true

    destroy()
    if (awaitExit(gracePeriodMillis)) return true

    killTree()
    return awaitExit(killTimeoutMillis)
}

private suspend fun Process.awaitExit(timeoutMillis: Long): Boolean {
    var waited = 0L
    while (waited < timeoutMillis) {
        if (!isAlive()) return true
        delay(EXIT_POLL_INTERVAL_MILLIS)
        waited += EXIT_POLL_INTERVAL_MILLIS
    }
    return !isAlive()
}

private const val EXIT_POLL_INTERVAL_MILLIS = 100L

expect suspend fun startProcess(exe: Path, params: List<String>): Process
expect fun addShutdownHook(hook: suspend () -> Unit)
expect fun isPosix(): Boolean
expect fun isRoot(): Boolean
expect fun tempProfileDir(): Path
expect fun exists(path: Path): Boolean
expect fun getEnv(name: String): String?
expect fun freePort(): Int?
expect fun defaultBrowserSearchConfig(): BrowserSearchConfig
