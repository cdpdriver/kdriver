package dev.kdriver.core.browser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
