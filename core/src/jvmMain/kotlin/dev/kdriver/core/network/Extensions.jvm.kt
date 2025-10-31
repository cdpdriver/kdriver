package dev.kdriver.core.network

import com.github.luben.zstd.ZstdInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import kotlin.io.copyTo
import kotlin.use

actual fun ByteArray.decompressZstd(): ByteArray {
    val input = ByteArrayInputStream(this)
    val output = ByteArrayOutputStream()
    ZstdInputStream(input).use { it.copyTo(output) }
    return output.toByteArray()
}

actual fun ByteArray.decompressGzip(): ByteArray {
    val input = ByteArrayInputStream(this)
    val output = ByteArrayOutputStream()
    GZIPInputStream(input).use { it.copyTo(output) }
    return output.toByteArray()
}
