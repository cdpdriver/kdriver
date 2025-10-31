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

actual fun getEnv(name: String): String? {
    return System.getenv(name)
}

actual fun findChromeExecutable(): Path? {
    val os = System.getProperty("os.name").lowercase()

    val config = when {
        os.contains("mac") -> BrowserSearchConfig(searchMacosApplications = true)
        isPosix() -> BrowserSearchConfig(searchLinuxCommonPaths = true)
        else -> BrowserSearchConfig(searchWindowsProgramFiles = true)
    }

    return findBrowserExecutableCommon(
        config = config,
        pathSeparator = File.pathSeparator,
        pathEnv = getEnv("PATH"),
        executableNames = listOf(
            "google-chrome",
            "chromium",
            "chromium-browser",
            "chrome",
            "google-chrome-stable"
        ),
        macosAppPaths = listOf(
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
            "/Applications/Chromium.app/Contents/MacOS/Chromium"
        ),
        linuxCommonPaths = listOf(
            "/usr/bin/google-chrome",
            "/usr/bin/chromium",
            "/usr/bin/chromium-browser",
            "/snap/bin/chromium",
            "/opt/google/chrome/chrome",
        ),
        windowsProgramFilesSuffixes = listOf(
            "Google/Chrome/Application",
            "Google/Chrome Beta/Application",
            "Google/Chrome Canary/Application",
            "Google/Chrome SxS/Application",
        ),
        windowsExecutableNames = listOf("chrome.exe"),
        windowsProgramFilesGetter = {
            listOfNotNull(
                getEnv("PROGRAMFILES"),
                getEnv("PROGRAMFILES(X86)"),
                getEnv("LOCALAPPDATA"),
                getEnv("PROGRAMW6432")
            )
        }
    )
}

actual fun findOperaExecutable(): Path? {
    val os = System.getProperty("os.name").lowercase()

    val config = when {
        os.contains("mac") -> BrowserSearchConfig(searchMacosApplications = true)
        isPosix() -> BrowserSearchConfig(searchLinuxCommonPaths = true)
        else -> BrowserSearchConfig(searchWindowsProgramFiles = true)
    }

    return findBrowserExecutableCommon(
        config = config,
        pathSeparator = File.pathSeparator,
        pathEnv = getEnv("PATH"),
        executableNames = listOf("opera"),
        macosAppPaths = listOf(
            "/Applications/Opera.app/Contents/MacOS/Opera"
        ),
        linuxCommonPaths = listOf(
            "/usr/bin/opera",
            "/usr/local/bin/opera",
        ),
        windowsProgramFilesSuffixes = listOf(
            "Opera",
            "Programs/Opera"
        ),
        windowsExecutableNames = listOf("opera.exe"),
        windowsProgramFilesGetter = {
            listOfNotNull(
                getEnv("PROGRAMFILES"),
                getEnv("PROGRAMFILES(X86)"),
                getEnv("LOCALAPPDATA"),
                getEnv("PROGRAMW6432")
            )
        }
    )
}

actual fun findBraveExecutable(): Path? {
    val os = System.getProperty("os.name").lowercase()

    val config = when {
        os.contains("mac") -> BrowserSearchConfig(searchMacosApplications = true)
        isPosix() -> BrowserSearchConfig(searchLinuxCommonPaths = true)
        else -> BrowserSearchConfig(searchWindowsProgramFiles = true)
    }

    return findBrowserExecutableCommon(
        config = config,
        pathSeparator = File.pathSeparator,
        pathEnv = getEnv("PATH"),
        executableNames = listOf("brave-browser", "brave"),
        macosAppPaths = listOf(
            "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser"
        ),
        linuxCommonPaths = listOf(
            "/usr/bin/brave-browser",
            "/usr/bin/brave",
            "/snap/bin/brave",
        ),
        windowsProgramFilesSuffixes = listOf(
            "BraveSoftware/Brave-Browser/Application"
        ),
        windowsExecutableNames = listOf("brave.exe"),
        windowsProgramFilesGetter = {
            listOfNotNull(
                getEnv("PROGRAMFILES"),
                getEnv("PROGRAMFILES(X86)"),
                getEnv("LOCALAPPDATA"),
                getEnv("PROGRAMW6432")
            )
        }
    )
}

actual fun findEdgeExecutable(): Path? {
    val os = System.getProperty("os.name").lowercase()

    val config = when {
        os.contains("mac") -> BrowserSearchConfig(searchMacosApplications = true)
        isPosix() -> BrowserSearchConfig(searchLinuxCommonPaths = true)
        else -> BrowserSearchConfig(searchWindowsProgramFiles = true)
    }

    return findBrowserExecutableCommon(
        config = config,
        pathSeparator = File.pathSeparator,
        pathEnv = getEnv("PATH"),
        executableNames = listOf(
            "microsoft-edge",
            "microsoft-edge-stable",
            "microsoft-edge-beta",
            "microsoft-edge-dev"
        ),
        macosAppPaths = listOf(
            "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge"
        ),
        linuxCommonPaths = listOf(
            "/usr/bin/microsoft-edge",
            "/usr/bin/microsoft-edge-stable",
        ),
        windowsProgramFilesSuffixes = listOf(
            "Microsoft/Edge/Application"
        ),
        windowsExecutableNames = listOf("msedge.exe"),
        windowsProgramFilesGetter = {
            listOfNotNull(
                getEnv("PROGRAMFILES"),
                getEnv("PROGRAMFILES(X86)"),
                getEnv("LOCALAPPDATA"),
                getEnv("PROGRAMW6432")
            )
        }
    )
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
