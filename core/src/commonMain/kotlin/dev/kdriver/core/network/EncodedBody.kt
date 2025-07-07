package dev.kdriver.core.network

import dev.kdriver.cdp.domain.Fetch
import dev.kdriver.cdp.domain.Network
import dev.kdriver.core.utils.decompressIfNeeded
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Represents an encoded body of a network response.
 *
 * This class encapsulates the body of a response, indicating whether it is base64 encoded or not.
 * It also provides a way to decode the body, handling both base64 encoded and plain text responses.
 */
data class EncodedBody(
    /**
     * The body of the response, which may be base64 encoded or plain text.
     * Use the [decodedBody] property to get the decoded version of this body.
     */
    val body: String,
    /**
     * Indicates whether the body is base64 encoded.
     * If true, the body should be decoded using Base64 decoding.
     * Use the [decodedBody] property to access the decoded body.
     */
    val base64Encoded: Boolean,
) {

    /**
     * Constructs an EncodedBody from a Network.GetResponseBodyReturn object.
     *
     * @param getResponseBodyReturn The Network.GetResponseBodyReturn object containing the response body and encoding information.
     */
    constructor(getResponseBodyReturn: Network.GetResponseBodyReturn) : this(
        body = getResponseBodyReturn.body,
        base64Encoded = getResponseBodyReturn.base64Encoded
    )

    /**
     * Constructs an EncodedBody from a Fetch.GetResponseBodyReturn object.
     *
     * @param getResponseBodyReturn The Fetch.GetResponseBodyReturn object containing the response body and encoding information.
     */
    constructor(getResponseBodyReturn: Fetch.GetResponseBodyReturn) : this(
        body = getResponseBodyReturn.body,
        base64Encoded = getResponseBodyReturn.base64Encoded
    )

    /**
     * Decodes the body of the response.
     *
     * If the body is base64 encoded, it decodes it using Base64 decoding.
     * Otherwise, it returns the body as is.
     *
     * @return The decoded body as a String.
     */
    @OptIn(ExperimentalEncodingApi::class)
    val decodedBody: String
        get() {
            val rawBytes = if (base64Encoded) Base64.decode(body) else body.encodeToByteArray()
            val decompressedBytes = decompressIfNeeded(rawBytes)
            return decompressedBytes.decodeToString()
        }

}
