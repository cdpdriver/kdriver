package dev.kdriver.core.utils

import com.github.luben.zstd.ZstdInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.util.zip.GZIPInputStream
import kotlin.io.bufferedReader
import kotlin.io.copyTo
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

actual fun findChromeExecutable(): Path? {
    val candidates = mutableListOf<Path>()
    val os = System.getProperty("os.name").lowercase()
    val paths = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
    if (isPosix()) {
        val executables = listOf(
            "google-chrome",
            "chromium",
            "chromium-browser",
            "chrome",
            "google-chrome-stable"
        )
        for (path in paths) {
            for (exe in executables) {
                val candidate = File(path, exe)
                if (candidate.exists() && candidate.canExecute()) {
                    candidates.add(Path(candidate.absolutePath))
                }
            }
        }
        if (os.contains("mac")) {
            candidates.addAll(
                listOf(
                    Path("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"),
                    Path("/Applications/Chromium.app/Contents/MacOS/Chromium")
                )
            )
        }
    } else {
        val programFiles = listOfNotNull(
            System.getenv("PROGRAMFILES"),
            System.getenv("PROGRAMFILES(X86)"),
            System.getenv("LOCALAPPDATA"),
            System.getenv("PROGRAMW6432")
        )
        val subPaths = listOf(
            "Google/Chrome/Application",
            "Google/Chrome Beta/Application",
            "Google/Chrome Canary/Application",
            "Google/Chrome SxS/Application",
        )
        for (base in programFiles) {
            for (sub in subPaths) {
                val candidate = File(base, "$sub/chrome.exe")
                candidates.add(Path(candidate.absolutePath))
            }
        }
    }
    return candidates
        .filter {
            val file = File(it.toString())
            file.exists() && file.canExecute()
        }
        .minByOrNull { it.toString().length }
}

actual fun findOperaExecutable(): Path? {
    val candidates = mutableListOf<Path>()
    val os = System.getProperty("os.name").lowercase()
    val paths = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
    if (isPosix()) {
        val executables = listOf(
            "opera"
        )
        for (path in paths) {
            for (exe in executables) {
                val candidate = File(path, exe)
                if (candidate.exists() && candidate.canExecute()) {
                    candidates.add(Path(candidate.absolutePath))
                }
            }
        }
        if (os.contains("mac")) {
            candidates.add(Path("/Applications/Opera.app/Contents/MacOS/Opera"))
        }
    } else {
        val programFiles = listOfNotNull(
            System.getenv("PROGRAMFILES"),
            System.getenv("PROGRAMFILES(X86)"),
            System.getenv("LOCALAPPDATA"),
            System.getenv("PROGRAMW6432")
        )
        val subPaths = listOf(
            "Opera",
            "Programs/Opera"
        )
        val exes = listOf("opera.exe")
        for (base in programFiles) {
            for (sub in subPaths) {
                for (exe in exes) {
                    val candidate = File(base, "$sub/$exe")
                    candidates.add(Path(candidate.absolutePath))
                }
            }
        }
    }
    return candidates
        .filter {
            val file = File(it.toString())
            file.exists() && file.canExecute()
        }
        .minByOrNull { it.toString().length }
}

actual fun findBraveExecutable(): Path? {
    val candidates = mutableListOf<Path>()
    val os = System.getProperty("os.name").lowercase()
    val paths = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
    if (isPosix()) {
        val executables = listOf(
            "brave-browser",
            "brave"
        )
        for (path in paths) {
            for (exe in executables) {
                val candidate = File(path, exe)
                if (candidate.exists() && candidate.canExecute()) {
                    candidates.add(Path(candidate.absolutePath))
                }
            }
        }
        if (os.contains("mac")) {
            candidates.add(Path("/Applications/Brave Browser.app/Contents/MacOS/Brave Browser"))
        }
    } else {
        val roots = listOfNotNull(
            System.getenv("PROGRAMFILES"),
            System.getenv("PROGRAMFILES(X86)"),
            System.getenv("LOCALAPPDATA"),
            System.getenv("PROGRAMW6432")
        )
        val subPaths = listOf(
            "BraveSoftware/Brave-Browser/Application"
        )
        for (base in roots) {
            for (sub in subPaths) {
                val candidate = File(base, "$sub/brave.exe")
                candidates.add(Path(candidate.absolutePath))
            }
        }
    }
    return candidates
        .filter {
            val file = File(it.toString())
            file.exists() && file.canExecute()
        }
        .minByOrNull { it.toString().length }
}

actual fun findEdgeExecutable(): Path? {
    val candidates = mutableListOf<Path>()
    val os = System.getProperty("os.name").lowercase()
    val paths = System.getenv("PATH")?.split(File.pathSeparator) ?: emptyList()
    if (isPosix()) {
        val executables = listOf(
            "microsoft-edge",
            "microsoft-edge-stable",
            "microsoft-edge-beta",
            "microsoft-edge-dev"
        )
        for (path in paths) {
            for (exe in executables) {
                val candidate = File(path, exe)
                if (candidate.exists() && candidate.canExecute()) {
                    candidates.add(Path(candidate.absolutePath))
                }
            }
        }
        if (os.contains("mac")) {
            candidates.add(Path("/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge"))
        }
    } else {
        val roots = listOfNotNull(
            System.getenv("PROGRAMFILES"),
            System.getenv("PROGRAMFILES(X86)"),
            System.getenv("LOCALAPPDATA"),
            System.getenv("PROGRAMW6432")
        )
        val subPaths = listOf(
            "Microsoft/Edge/Application"
        )
        for (base in roots) {
            for (sub in subPaths) {
                val candidate = File(base, "$sub/msedge.exe")
                candidates.add(Path(candidate.absolutePath))
            }
        }
    }
    return candidates
        .filter {
            val file = File(it.toString())
            file.exists() && file.canExecute()
        }
        .minByOrNull { it.toString().length }
}

actual fun freePort(): Int? {
    ServerSocket(0, 5, InetAddress.getByName("127.0.0.1")).use { socket ->
        return socket.localPort
    }
}

actual fun decompressIfNeeded(data: ByteArray): ByteArray {
    return when {
        isZstdCompressed(data) -> {
            val input = ByteArrayInputStream(data)
            val output = ByteArrayOutputStream()
            ZstdInputStream(input).use { it.copyTo(output) }
            output.toByteArray()
        }

        isGzipCompressed(data) -> {
            val input = ByteArrayInputStream(data)
            val output = ByteArrayOutputStream()
            GZIPInputStream(input).use { it.copyTo(output) }
            output.toByteArray()
        }

        else -> data
    }
}
