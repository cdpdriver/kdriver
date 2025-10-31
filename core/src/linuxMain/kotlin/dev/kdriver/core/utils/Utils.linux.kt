package dev.kdriver.core.utils

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
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

private class PosixProcess(override val processIdentifier: Int) : Process() {
    override fun destroy() {
        if (isAlive()) {
            kill(processIdentifier, SIGTERM)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun startProcess(
    exe: Path,
    params: List<String>,
): Process = withContext(Dispatchers.IO) {
    val pid = fork()

    when {
        pid < 0 -> throw RuntimeException("Failed to fork process")
        pid == 0 -> {
            // Child process
            memScoped {
                // Build argv array for execvp
                val argc = params.size + 2 // exe + params + null
                val argv = allocArray<CPointerVar<ByteVar>>(argc)
                argv[0] = exe.toString().cstr.ptr
                params.forEachIndexed { index, param ->
                    argv[index + 1] = param.cstr.ptr
                }
                argv[argc - 1] = null

                // Execute the process
                execvp(exe.toString(), argv)

                // If we reach here, execvp failed
                exit(1)
            }
            // This line will never be reached but is needed for type checking
            throw IllegalStateException("Child process should have exited")
        }

        else -> {
            // Parent process
            PosixProcess(pid)
        }
    }
}

actual fun addShutdownHook(hook: suspend () -> Unit) {
    // Linux doesn't have a direct equivalent to Java shutdown hooks
    // Could use atexit() but it doesn't support suspend functions
    // For now, keeping it as no-op
}

actual fun isPosix(): Boolean {
    return true // Linux is POSIX-compliant
}

@OptIn(ExperimentalForeignApi::class)
actual fun isRoot(): Boolean {
    return getuid() == 0u
}

@OptIn(ExperimentalForeignApi::class)
actual fun tempProfileDir(): Path {
    val tempDir = getenv("TMPDIR")?.toKString() ?: "/tmp"
    val uniqueName = "kdriver_${Random.nextLong().toString(16)}"
    val profilePath = "$tempDir/$uniqueName"

    mkdir(profilePath, 0x1FFu) // 0777 permissions

    return Path(profilePath)
}

@OptIn(ExperimentalForeignApi::class)
actual fun exists(path: Path): Boolean {
    return access(path.toString(), F_OK) == 0
}

@OptIn(ExperimentalForeignApi::class)
actual fun getEnv(name: String): String? {
    return getenv(name)?.toKString()
}

@OptIn(ExperimentalForeignApi::class)
private fun isExecutable(path: Path): Boolean {
    return access(path.toString(), X_OK) == 0
}

actual fun findChromeExecutable(): Path? = findBrowserExecutableCommon(
    config = BrowserSearchConfig(searchLinuxCommonPaths = true),
    pathSeparator = ":",
    pathEnv = getEnv("PATH"),
    executableNames = listOf(
        "google-chrome",
        "chromium",
        "chromium-browser",
        "chrome",
        "google-chrome-stable"
    ),
    linuxCommonPaths = listOf(
        "/usr/bin/google-chrome",
        "/usr/bin/chromium",
        "/usr/bin/chromium-browser",
        "/snap/bin/chromium",
        "/opt/google/chrome/chrome",
    ),
)

actual fun findOperaExecutable(): Path? = findBrowserExecutableCommon(
    config = BrowserSearchConfig(searchLinuxCommonPaths = true),
    pathSeparator = ":",
    pathEnv = getEnv("PATH"),
    executableNames = listOf("opera"),
    linuxCommonPaths = listOf(
        "/usr/bin/opera",
        "/usr/local/bin/opera",
    ),
)

actual fun findBraveExecutable(): Path? = findBrowserExecutableCommon(
    config = BrowserSearchConfig(searchLinuxCommonPaths = true),
    pathSeparator = ":",
    pathEnv = getEnv("PATH"),
    executableNames = listOf("brave-browser", "brave"),
    linuxCommonPaths = listOf(
        "/usr/bin/brave-browser",
        "/usr/bin/brave",
        "/snap/bin/brave",
    ),
)

actual fun findEdgeExecutable(): Path? = findBrowserExecutableCommon(
    config = BrowserSearchConfig(searchLinuxCommonPaths = true),
    pathSeparator = ":",
    pathEnv = getEnv("PATH"),
    executableNames = listOf(
        "microsoft-edge",
        "microsoft-edge-stable",
        "microsoft-edge-beta",
        "microsoft-edge-dev"
    ),
    linuxCommonPaths = listOf(
        "/usr/bin/microsoft-edge",
        "/usr/bin/microsoft-edge-stable",
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
            // For now, throw an exception
            throw UnsupportedOperationException("Zstandard decompression not yet supported on Linux native")
        }

        else -> data
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun decompressGzip(data: ByteArray): ByteArray {
    // Write to temp file and use gunzip command as a workaround
    val tempDir = getenv("TMPDIR")?.toKString() ?: "/tmp"
    val inputPath = "$tempDir/kdriver_input_${Random.nextLong().toString(16)}.gz"
    val outputPath = "$tempDir/kdriver_output_${Random.nextLong().toString(16)}"

    // Write compressed data to temp file
    val file = fopen(inputPath, "wb")
    if (file == null) return data

    try {
        data.usePinned { pinned ->
            fwrite(pinned.addressOf(0), 1u, data.size.toULong(), file)
        }
        fclose(file)

        // Fork and exec gunzip
        val pid = fork()
        return when {
            pid < 0 -> data // Fork failed
            pid == 0 -> {
                // Child process - redirect output to file
                memScoped {
                    val outFile = fopen(outputPath, "wb")
                    if (outFile != null) {
                        dup2(fileno(outFile), STDOUT_FILENO)
                        fclose(outFile)
                    }

                    val argv = allocArray<CPointerVar<ByteVar>>(4)
                    argv[0] = "gunzip".cstr.ptr
                    argv[1] = "-c".cstr.ptr
                    argv[2] = inputPath.cstr.ptr
                    argv[3] = null

                    execvp("gunzip", argv)
                    exit(1)
                }
                throw IllegalStateException("Child process should have exited")
            }

            else -> {
                // Parent process - wait for child
                memScoped {
                    val status = alloc<IntVar>()
                    waitpid(pid, status.ptr, 0)
                }

                // Read decompressed data
                val outFile = fopen(outputPath, "rb")
                if (outFile == null) {
                    unlink(inputPath)
                    return data
                }

                try {
                    // Get file size
                    fseek(outFile, 0, SEEK_END)
                    val size = ftell(outFile).toInt()
                    fseek(outFile, 0, SEEK_SET)

                    if (size <= 0) return data

                    // Read data
                    val result = ByteArray(size)
                    result.usePinned { pinned ->
                        fread(pinned.addressOf(0), 1u, size.toULong(), outFile)
                    }

                    result
                } finally {
                    fclose(outFile)
                    unlink(inputPath)
                    unlink(outputPath)
                }
            }
        }
    } catch (e: Exception) {
        unlink(inputPath)
        return data
    }
}
