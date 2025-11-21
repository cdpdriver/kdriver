package dev.kdriver.core.network

import dev.kdriver.cdp.Serialization
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Fetches the response body once it has been received.
 */
@OptIn(ExperimentalEncodingApi::class)
suspend inline fun <reified T> RequestExpectation.getResponseBody(): T {
    return Serialization.json.decodeFromString<T>(getRawResponseBody().decodedBody)
}

/**
 * Fetches the response body once it has been received.
 */
@OptIn(ExperimentalEncodingApi::class)
suspend inline fun <reified T> FetchInterception.getResponseBody(): T {
    return Serialization.json.decodeFromString<T>(getRawResponseBody().decodedBody)
}

/**
 * Checks if the byte array is compressed using Zstandard (Zstd) compression.
 *
 * @return True if the byte array is Zstd compressed, false otherwise.
 */
fun ByteArray.isZstdCompressed(): Boolean {
    val header = take(4).map { it.toUByte().toInt() }
    return header == listOf(0x28, 0xB5, 0x2F, 0xFD)
}

/**
 * Checks if the byte array is compressed using Gzip compression.
 *
 * @return True if the byte array is Gzip compressed, false otherwise.
 */
fun ByteArray.isGzipCompressed(): Boolean {
    val header = take(2).map { it.toUByte().toInt() }
    return header == listOf(0x1F, 0x8B)
}

/**
 * Decompresses the byte array if it is compressed using Zstd or Gzip compression.
 *
 * @return The decompressed byte array, or the original byte array if it is not compressed.
 */
fun ByteArray.decompressIfNeeded(): ByteArray = when {
    isZstdCompressed() -> decompressZstd()
    isGzipCompressed() -> decompressGzip()
    else -> this
}

/**
 * Decompresses a Zstd compressed byte array.
 *
 * @return The decompressed byte array.
 */
expect fun ByteArray.decompressZstd(): ByteArray

/**
 * Decompresses a Gzip compressed byte array.
 *
 * @return The decompressed byte array.
 */
expect fun ByteArray.decompressGzip(): ByteArray
