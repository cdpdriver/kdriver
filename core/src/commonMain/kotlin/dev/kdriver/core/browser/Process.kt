package dev.kdriver.core.browser

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

expect suspend fun startProcess(exe: Path, params: List<String>): Process
expect fun addShutdownHook(hook: suspend () -> Unit)
expect fun isPosix(): Boolean
expect fun isRoot(): Boolean
expect fun tempProfileDir(): Path
expect fun exists(path: Path): Boolean
expect fun getEnv(name: String): String?
expect fun freePort(): Int?
expect fun defaultBrowserSearchConfig(): BrowserSearchConfig
