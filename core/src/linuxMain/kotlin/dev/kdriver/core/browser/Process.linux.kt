package dev.kdriver.core.browser

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import platform.posix.*

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

actual fun defaultBrowserSearchConfig(): BrowserSearchConfig {
    return BrowserSearchConfig(":", searchLinuxCommonPaths = true)
}
