package dev.kdriver.core.network

actual fun ByteArray.decompressZstd(): ByteArray {
    // Zstandard decompression not readily available in Kotlin/Native for Windows
    throw UnsupportedOperationException("Zstandard decompression not yet supported on Windows native")
}

actual fun ByteArray.decompressGzip(): ByteArray {
    // Gzip decompression not readily available in Kotlin/Native for Windows
    throw UnsupportedOperationException("Gzip decompression not yet supported on Windows native")
}
