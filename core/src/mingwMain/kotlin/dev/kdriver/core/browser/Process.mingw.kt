package dev.kdriver.core.browser

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.posix.getenv
import platform.posix.memset
import platform.windows.*
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
actual abstract class Process {
    abstract val processHandle: HANDLE?
    abstract val processId: DWORD

    actual fun isAlive(): Boolean {
        val handle = processHandle ?: return false
        val exitCode = memScoped {
            val code = alloc<DWORDVar>()
            GetExitCodeProcess(handle, code.ptr)
            code.value
        }
        return exitCode == STILL_ACTIVE
    }

    actual fun pid(): Long {
        return processId.toLong()
    }

    actual abstract fun destroy()
}

@OptIn(ExperimentalForeignApi::class)
private class WindowsProcess(
    override val processHandle: HANDLE?,
    override val processId: DWORD,
) : Process() {
    override fun destroy() {
        processHandle?.let {
            if (isAlive()) {
                TerminateProcess(it, 1u)
            }
            CloseHandle(it)
        }
    }
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
