package dev.kdriver.core.browser

import kotlinx.cinterop.*
import kotlinx.io.files.Path
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

actual fun addShutdownHook(hook: suspend () -> Unit) {
    // POSIX doesn't have a direct equivalent to Java shutdown hooks
    // Could use atexit() but it doesn't support suspend functions
    // For now, keeping it as no-op
}

actual fun isPosix(): Boolean {
    return true // POSIX-compliant platforms
}

@OptIn(ExperimentalForeignApi::class)
actual fun isRoot(): Boolean {
    return getuid() == 0u
}

@OptIn(ExperimentalForeignApi::class)
actual fun getEnv(name: String): String? {
    return getenv(name)?.toKString()
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
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

@OptIn(ExperimentalForeignApi::class)
actual fun exists(path: Path): Boolean {
    return access(path.toString(), F_OK) == 0
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual fun tempProfileDir(): Path {
    val tempDir = getenv("TMPDIR")?.toKString() ?: "/tmp"
    val uniqueName = "kdriver_${Random.nextLong().toString(16)}"
    val profilePath = "$tempDir/$uniqueName"

    mkdir(profilePath, 0x1FFu) // 0777 permissions

    return Path(profilePath)
}
