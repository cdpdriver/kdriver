package dev.kdriver.core.utils

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.Foundation.*
import platform.posix.*
import kotlin.random.Random

actual abstract class Process {
    abstract val processIdentifier: Int

    actual fun isAlive(): Boolean {
        // Check if process exists by sending signal 0
        return kill(processIdentifier, 0) == 0
    }

    actual fun pid(): Long {
        return processIdentifier.toLong()
    }

    actual abstract fun destroy()
}

private class NSTaskProcess(private val task: NSTask) : Process() {
    override val processIdentifier: Int
        get() = task.processIdentifier

    override fun destroy() {
        if (task.isRunning()) {
            task.terminate()
        }
    }
}

actual suspend fun startProcess(
    exe: Path,
    params: List<String>,
): Process = withContext(Dispatchers.IO) {
    val task = NSTask()
    task.launchPath = exe.toString()
    task.arguments = params

    // Set up pipes for stdin, stdout, stderr
    task.standardInput = NSPipe()
    task.standardOutput = NSPipe()
    task.standardError = NSPipe()

    task.launch()
    NSTaskProcess(task)
}

actual fun addShutdownHook(hook: suspend () -> Unit) {
    // macOS doesn't have a direct equivalent to Java shutdown hooks in the same way
    // We could use atexit() but it doesn't support suspend functions
    // For now, keeping it as no-op, similar to the original placeholder
}

actual fun isPosix(): Boolean {
    return true // macOS is always POSIX-compliant
}

actual fun isRoot(): Boolean {
    return getuid() == 0u
}

@OptIn(ExperimentalForeignApi::class)
actual fun tempProfileDir(): Path {
    val tempDir = NSTemporaryDirectory()
    val uniqueName = "kdriver_${Random.nextLong().toString(16)}"
    val profilePath = tempDir + uniqueName

    NSFileManager.defaultManager.createDirectoryAtPath(
        profilePath,
        withIntermediateDirectories = true,
        attributes = null,
        error = null
    )

    return Path(profilePath)
}

actual fun exists(path: Path): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(path.toString())
}

@OptIn(ExperimentalForeignApi::class)
actual fun getEnv(name: String): String? {
    return getenv(name)?.toKString()
}

actual fun findChromeExecutable(): Path? = findBrowserExecutableCommon(
    config = BrowserSearchConfig(searchMacosApplications = true),
    pathSeparator = ":",
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
)

actual fun findOperaExecutable(): Path? = findBrowserExecutableCommon(
    config = BrowserSearchConfig(searchMacosApplications = true),
    pathSeparator = ":",
    pathEnv = getEnv("PATH"),
    executableNames = listOf("opera"),
    macosAppPaths = listOf(
        "/Applications/Opera.app/Contents/MacOS/Opera"
    ),
)

actual fun findBraveExecutable(): Path? = findBrowserExecutableCommon(
    config = BrowserSearchConfig(searchMacosApplications = true),
    pathSeparator = ":",
    pathEnv = getEnv("PATH"),
    executableNames = listOf("brave-browser", "brave"),
    macosAppPaths = listOf(
        "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser"
    ),
)

actual fun findEdgeExecutable(): Path? = findBrowserExecutableCommon(
    config = BrowserSearchConfig(searchMacosApplications = true),
    pathSeparator = ":",
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
)

@OptIn(ExperimentalForeignApi::class)
actual fun freePort(): Int? {
    return memScoped {
        val sockfd = socket(AF_INET, SOCK_STREAM, 0)
        if (sockfd < 0) return null

        try {
            val addr = alloc<sockaddr_in>()
            memset(addr.ptr, 0, sizeOf<sockaddr_in>().convert())
            addr.sin_family = AF_INET.convert()
            addr.sin_addr.s_addr = 0x0100007Fu // 127.0.0.1 in network byte order (little-endian)
            addr.sin_port = 0u // Let the OS choose a port

            if (bind(sockfd, addr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()) < 0) {
                return null
            }

            val len = alloc<UIntVar>()
            len.value = sizeOf<sockaddr_in>().convert()

            if (getsockname(sockfd, addr.ptr.reinterpret(), len.ptr) < 0) {
                return null
            }

            // Convert port from network byte order to host byte order
            val portBytes = addr.sin_port
            ((portBytes.toInt() shr 8) and 0xFF) or ((portBytes.toInt() and 0xFF) shl 8)
        } finally {
            close(sockfd)
        }
    }
}

actual fun decompressIfNeeded(data: ByteArray): ByteArray {
    return when {
        isGzipCompressed(data) -> decompressGzip(data)
        isZstdCompressed(data) -> {
            // Zstandard decompression is not readily available in Kotlin/Native without additional libraries
            // For now, return data as-is or throw an exception
            throw UnsupportedOperationException("Zstandard decompression not yet supported on macOS native")
        }

        else -> data
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun decompressGzip(data: ByteArray): ByteArray {
    // Create NSData from ByteArray
    val nsData = data.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = data.size.toULong())
    }

    // Use the /usr/bin/gunzip command as a workaround
    // This is not ideal but works without needing zlib cinterop definitions
    val tempDir = NSTemporaryDirectory()
    val inputPath = tempDir + "input.gz"
    val outputPath = tempDir + "output"

    // Write compressed data to temp file
    nsData.writeToFile(inputPath, atomically = false)

    // Run gunzip command
    val task = NSTask()
    task.launchPath = "/usr/bin/gunzip"
    task.arguments = listOf("-c", inputPath)

    val outputPipe = NSPipe()
    task.standardOutput = outputPipe
    task.standardError = NSPipe()

    task.launch()
    task.waitUntilExit()

    // Read decompressed data
    val outputData = outputPipe.fileHandleForReading.readDataToEndOfFile()

    // Clean up temp file
    NSFileManager.defaultManager.removeItemAtPath(inputPath, error = null)

    // Convert NSData back to ByteArray
    val size = outputData.length.toInt()
    return if (size > 0) {
        val result = ByteArray(size)
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), outputData.bytes, size.toULong())
        }
        result
    } else {
        data // Return original if decompression failed
    }
}
