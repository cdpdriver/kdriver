package dev.kdriver.core.browser

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.posix.getenv
import platform.posix.memset
import platform.windows.*
import kotlin.concurrent.AtomicReference
import kotlin.concurrent.Volatile
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
actual abstract class Process {
    abstract val processHandle: HANDLE?
    abstract val processId: DWORD

    /** The exit code, once the process has been seen to exit; its handle is closed from then on. */
    @Volatile
    internal var exitCode: DWORD? = null
        private set

    actual fun isAlive(): Boolean {
        if (exitCode != null) return false
        val handle = processHandle ?: return false
        val code = memScoped {
            val c = alloc<DWORDVar>()
            if (GetExitCodeProcess(handle, c.ptr) == 0) return false
            c.value
        }
        if (code == STILL_ACTIVE) return true
        exitCode = code
        releaseHandle()
        return false
    }

    /**
     * Closes the process handle, once the process has exited and nothing needs it anymore. Kept open
     * until then, `destroy` included: [isAlive] reads the exit code through it, to know when the
     * process is really gone (and its profile can be deleted).
     */
    protected abstract fun releaseHandle()

    actual fun pid(): Long {
        return processId.toLong()
    }

    actual abstract fun destroy()
}

@OptIn(ExperimentalForeignApi::class)
private class WindowsProcess(
    processHandle: HANDLE?,
    override val processId: DWORD,
) : Process() {
    private val handle = AtomicReference(processHandle)

    override val processHandle: HANDLE?
        get() = handle.value

    /** Atomic, so two threads seeing the exit at once do not close the same handle twice. */
    override fun releaseHandle() {
        handle.getAndSet(null)?.let { CloseHandle(it) }
    }

    override fun destroy() {
        processHandle?.let {
            if (isAlive()) {
                TerminateProcess(it, 1u)
            }
        }
    }
}

/**
 * Not available on this target: [startProcess] here calls `CreateProcessW` without redirecting the
 * child's standard streams, so there is no pipe to read from.
 */
actual suspend fun Process.readStderrSnapshot(maxBytes: Int, timeoutMillis: Long): String? = null

/**
 * Kills this process.
 *
 * `TerminateProcess` already ends it outright, so there is nothing gentler to escalate from here.
 * Note that it does **not** reach the process's children: enumerating them on Windows means walking
 * a `CreateToolhelp32Snapshot` by parent id, which this target does not do — the JVM target, which is
 * the one running browsers in production, uses `ProcessHandle.descendants()` for that.
 */
/**
 * Reads the exit status recorded by [Process.isAlive] through `GetExitCodeProcess` — null while the
 * process still runs.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun Process.exitCodeOrNull(): Int? = if (isAlive()) null else exitCode?.toInt()

@OptIn(ExperimentalForeignApi::class)
actual fun Process.killTree() {
    processHandle?.let { if (isAlive()) TerminateProcess(it, 1u) }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun startProcess(
    exe: Path,
    params: List<String>,
): Process = withContext(Dispatchers.IO) {
    memScoped {
        val startupInfo = alloc<STARTUPINFOW>()
        val processInfo = alloc<PROCESS_INFORMATION>()

        memset(startupInfo.ptr, 0, sizeOf<STARTUPINFOW>().convert())
        startupInfo.cb = sizeOf<STARTUPINFOW>().convert()

        // Build command line: "exe" "param1" "param2" ...
        val commandLine = buildString {
            append("\"${exe}\"")
            params.forEach { param ->
                append(" \"$param\"")
            }
        }

        // Convert command line to wide string
        val cmdLineWide = commandLine.wcstr

        val result = CreateProcessW(
            lpApplicationName = null,
            lpCommandLine = cmdLineWide.ptr,
            lpProcessAttributes = null,
            lpThreadAttributes = null,
            bInheritHandles = 0,
            dwCreationFlags = 0u,
            lpEnvironment = null,
            lpCurrentDirectory = null,
            lpStartupInfo = startupInfo.ptr,
            lpProcessInformation = processInfo.ptr
        )

        if (result == 0) {
            throw RuntimeException("Failed to create process: ${GetLastError()}")
        }

        // Close thread handle as we don't need it
        CloseHandle(processInfo.hThread)

        WindowsProcess(processInfo.hProcess, processInfo.dwProcessId)
    }
}

actual fun addShutdownHook(hook: suspend () -> Unit) {
    // Windows doesn't have a direct equivalent to Java shutdown hooks
    // Could use SetConsoleCtrlHandler but it doesn't support suspend functions
    // For now, keeping it as no-op
}

actual fun isPosix(): Boolean {
    return false // Windows is not POSIX-compliant
}

actual fun isRoot(): Boolean {
    // Check if running as administrator
    // This is a simplified check - a more complete implementation would use
    // CheckTokenMembership with SECURITY_NT_AUTHORITY
    return false // TODO: Implement proper admin check
}

@OptIn(ExperimentalForeignApi::class)
actual fun tempProfileDir(): Path {
    val tempDir = getEnv("TEMP") ?: getEnv("TMP") ?: "C:\\Temp"
    val uniqueName = "kdriver_${Random.nextLong().toString(16)}"
    val profilePath = "$tempDir\\$uniqueName"

    // CreateDirectoryW will create the directory
    // We don't check the result for now
    // In real implementation, should check CreateDirectoryW return value

    return Path(profilePath)
}

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual fun exists(path: Path): Boolean {
    val attributes = GetFileAttributesW(path.toString())
    return attributes != INVALID_FILE_ATTRIBUTES
}

@OptIn(ExperimentalForeignApi::class)
actual fun getEnv(name: String): String? {
    // Use POSIX getenv which is also available on Windows with MinGW
    return getenv(name)?.toKString()
}

@OptIn(ExperimentalForeignApi::class)
actual fun freePort(): Int? {
    // TODO: Implement Windows-specific freePort using Winsock
    // For now, return a random port in the ephemeral range
    return (49152..65535).random()
}

actual fun defaultBrowserSearchConfig(): BrowserSearchConfig {
    return BrowserSearchConfig(";", searchWindowsProgramFiles = true)
}
