package dev.kdriver.core.browser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.files.Path
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import kotlin.io.bufferedReader
import kotlin.io.path.createTempDirectory
import kotlin.io.readText
import kotlin.use

actual typealias Process = java.lang.Process

actual suspend fun startProcess(
    exe: Path,
    params: List<String>,
): Process {
    val isPosix = isPosix()
    return withContext(Dispatchers.IO) {
        val command = listOf(exe.toString()) + params

        val builder = ProcessBuilder(command)
        builder.redirectInput(ProcessBuilder.Redirect.PIPE)
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE)
        builder.redirectError(ProcessBuilder.Redirect.PIPE)
        if (isPosix) builder.redirectErrorStream(false)

        val process = builder.start()
        process
    }
}

actual fun addShutdownHook(hook: suspend () -> Unit) {
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking {
            hook()
        }
    })
}

actual fun isPosix(): Boolean {
    val os = System.getProperty("os.name").lowercase()
    return os.contains("nix") || os.contains("nux") || os.contains("mac")
}

actual fun isRoot(): Boolean {
    return try {
        val process = ProcessBuilder("id", "-u").start()
        val result = process.inputStream.bufferedReader().readText().trim()
        result == "0"
    } catch (_: Exception) {
        false
    }
}

actual fun tempProfileDir(): Path {
    return Path(createTempDirectory(prefix = "kdriver_").toFile().absolutePath)
}

actual fun exists(path: Path): Boolean {
    return try {
        val file = File(path.toString())
        file.exists() && file.canRead()
    } catch (_: Exception) {
        false
    }
}

actual fun getEnv(name: String): String? {
    return System.getenv(name)
}

/**
 * Reads whatever is currently buffered on the process's stderr, bounded in both size and time.
 *
 * Both bounds matter: the stream stays open for as long as the process lives, so an unbounded read
 * would block until it exits. That is why the equivalent block used to be commented out with
 * "seems to block indefinitely on CI".
 */
actual suspend fun Process.readStderrSnapshot(maxBytes: Int, timeoutMillis: Long): String? =
    withTimeoutOrNull(timeoutMillis) {
        withContext(Dispatchers.IO) {
            runCatching {
                val buffer = ByteArray(maxBytes)
                val read = errorStream.read(buffer)
                if (read > 0) String(buffer, 0, read) else null
            }.getOrNull()
        }
    }

/**
 * Kills this process and every descendant.
 *
 * `Process.destroyForcibly()` on its own only reaches the top-level process; on Windows the browser's
 * renderer and GPU children outlive it and keep holding the profile's files. Descendants are snapshot
 * first, because killing the parent detaches them and they can no longer be enumerated.
 */
actual fun Process.killTree() {
    val descendants = runCatching { toHandle().descendants().toList() }.getOrDefault(emptyList())
    destroyForcibly()
    descendants.forEach { runCatching { it.destroyForcibly() } }
}

actual fun freePort(): Int? {
    ServerSocket(0, 5, InetAddress.getByName("127.0.0.1")).use { socket ->
        return socket.localPort
    }
}


actual fun defaultBrowserSearchConfig(): BrowserSearchConfig {
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("mac") -> BrowserSearchConfig(File.pathSeparator, searchMacosApplications = true)
        isPosix() -> BrowserSearchConfig(File.pathSeparator, searchLinuxCommonPaths = true)
        else -> BrowserSearchConfig(File.pathSeparator, searchWindowsProgramFiles = true)
    }
}

/**
 * Reads the exit status, or null while the process is still running.
 *
 * `exitValue()` throws `IllegalThreadStateException` rather than returning a sentinel when the
 * process is alive, which is exactly the "still running" case we report as null.
 */
actual fun Process.exitCodeOrNull(): Int? = runCatching { exitValue() }.getOrNull()
