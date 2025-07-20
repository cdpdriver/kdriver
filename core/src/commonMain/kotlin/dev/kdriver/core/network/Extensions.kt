package dev.kdriver.core.network

import dev.kaccelero.serializers.Serialization
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
