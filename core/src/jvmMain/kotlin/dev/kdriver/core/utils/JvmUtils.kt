package dev.kdriver.core.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.FileNotFoundException
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
        ?: throw FileNotFoundException("Could not find a valid Chrome browser binary. Please make sure Chrome is installed or specify the 'browserExecutablePath' parameter.")
}

actual fun freePort(): Int? {
    ServerSocket(0, 5, InetAddress.getByName("127.0.0.1")).use { socket ->
        return socket.localPort
    }
}
