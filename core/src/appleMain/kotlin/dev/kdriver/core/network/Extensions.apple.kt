package dev.kdriver.core.network

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*
import platform.posix.memcpy

actual fun ByteArray.decompressZstd(): ByteArray {
    // Zstandard decompression is not readily available in Kotlin/Native without additional libraries
    // For now, return data as-is or throw an exception
    throw UnsupportedOperationException("Zstandard decompression not yet supported on macOS native")
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun ByteArray.decompressGzip(): ByteArray {
    // Create NSData from ByteArray
    val nsData = usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }

    // Use the /usr/bin/gunzip command as a workaround
    // This is not ideal but works without needing zlib cinterop definitions
    val tempDir = NSTemporaryDirectory()
    val inputPath = tempDir + "input.gz"

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
        this // Return original if decompression failed
    }
}
