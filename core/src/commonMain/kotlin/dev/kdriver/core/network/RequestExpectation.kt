package dev.kdriver.core.network

import dev.kdriver.cdp.domain.Network

/**
 * Base class for handling request and response expectations.
 * This class provides a context manager to wait for specific network requests and responses
 * based on a URL pattern. It sets up handlers for request and response events and provides
 * properties to access the request, response, and response body.
 *
 * @param urlPattern The URL pattern to match requests and responses.
 */
interface RequestExpectation {

    val urlPattern: Regex

    /**
     * Expect a request/response that matches the given [urlPattern].
     */
    suspend fun <T> use(block: suspend RequestExpectation.() -> T): T

    /**
     * Returns the request event once it has been received.
     */
    suspend fun getRequestEvent(): Network.RequestWillBeSentParameter

    /**
     * Returns the response event once it has been received.
     */
    suspend fun getResponseEvent(): Network.ResponseReceivedParameter

    /**
     * Returns the request once it has been received.
     */
    suspend fun getRequest(): Network.Request

    /**
     * Returns the response once it has been received.
     */
    suspend fun getResponse(): Network.Response

    /**
     * Fetches the raw response body once it has been received.
     */
    suspend fun getRawResponseBody(): EncodedBody

}
