package dev.kdriver.core.network

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.random.Random

actual fun ByteArray.decompressZstd(): ByteArray {
    // Zstandard decompression is not readily available in Kotlin/Native without additional libraries
    // For now, throw an exception
    throw UnsupportedOperationException("Zstandard decompression not yet supported on Linux native")
}

@OptIn(ExperimentalForeignApi::class)
actual fun ByteArray.decompressGzip(): ByteArray {
    // Write to temp file and use gunzip command as a workaround
    val tempDir = getenv("TMPDIR")?.toKString() ?: "/tmp"
    val inputPath = "$tempDir/kdriver_input_${Random.nextLong().toString(16)}.gz"
    val outputPath = "$tempDir/kdriver_output_${Random.nextLong().toString(16)}"

    // Write compressed data to temp file
    val file = fopen(inputPath, "wb")
    if (file == null) return this

    try {
        usePinned { pinned ->
            fwrite(pinned.addressOf(0), 1u, size.toULong(), file)
        }
        fclose(file)

        // Fork and exec gunzip
        val pid = fork()
        return when {
            pid < 0 -> this // Fork failed
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
                    return this
                }

                try {
                    // Get file size
                    fseek(outFile, 0, SEEK_END)
                    val size = ftell(outFile).toInt()
                    fseek(outFile, 0, SEEK_SET)

                    if (size <= 0) return this

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
        return this
    }
}
