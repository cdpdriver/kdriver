package dev.kdriver.core.network

actual fun ByteArray.decompressZstd(): ByteArray {
    throw UnsupportedOperationException()
}

actual fun ByteArray.decompressGzip(): ByteArray {
    throw UnsupportedOperationException()
}
