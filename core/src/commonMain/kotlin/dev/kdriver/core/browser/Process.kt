package dev.kdriver.core.browser

import kotlinx.io.files.Path

expect abstract class Process {
    fun isAlive(): Boolean
    fun pid(): Long
    abstract fun destroy()
}

expect suspend fun startProcess(exe: Path, params: List<String>): Process
expect fun addShutdownHook(hook: suspend () -> Unit)
expect fun isPosix(): Boolean
expect fun isRoot(): Boolean
expect fun tempProfileDir(): Path
expect fun exists(path: Path): Boolean
expect fun getEnv(name: String): String?
expect fun freePort(): Int?
expect fun defaultBrowserSearchConfig(): BrowserSearchConfig
