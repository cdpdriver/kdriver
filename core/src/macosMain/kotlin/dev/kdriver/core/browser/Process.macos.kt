package dev.kdriver.core.browser

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

actual fun defaultBrowserSearchConfig(): BrowserSearchConfig {
    return BrowserSearchConfig(":", searchMacosApplications = true)
}
