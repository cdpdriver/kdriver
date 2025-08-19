package dev.kdriver.core.network

import dev.kdriver.cdp.domain.Fetch
import dev.kdriver.cdp.domain.Fetch.HeaderEntry
import dev.kdriver.cdp.domain.Network

/**
 * Base class to wait for a Fetch response matching a URL pattern.
 * Use this to collect and decode a paused fetch response, while keeping
 * the use block clean and returning its own result.
 *
 * @param urlPattern The URL pattern to match requests and responses.
 * @param requestStage The stage of the fetch request to intercept (e.g., request or response).
 * @param resourceType The type of resource to intercept (e.g., document, script, etc.).
 */
interface FetchInterception {

    val urlPattern: String
    val requestStage: Fetch.RequestStage
    val resourceType: Network.ResourceType

    /**
     * Start intercepting fetch responses matching the pattern,
     * run [block], and clean up after.
     */
    suspend fun <R> use(block: suspend FetchInterception.() -> R): R

    /**
     * Returns the request event once it has been received.
     */
    suspend fun getRequestEvent(): Fetch.RequestPausedParameter

    /**
     * Returns the request once it has been received.
     */
    suspend fun getRequest(): Network.Request

    /**
     * Fetches the raw response body once it has been received.
     */
    suspend fun getRawResponseBody(): EncodedBody

    /**
     * Causes the request to fail with specified reason.
     *
     * @param errorReason Causes the request to fail with the given reason.
     */
    suspend fun failRequest(errorReason: Network.ErrorReason)

    /**
     * Continues the request, optionally modifying some of its parameters.
     *
     * @param url If set, the request url will be modified in a way that's not observable by page.
     * @param method If set, the request method is overridden.
     * @param postData If set, overrides the post data in the request. (Encoded as a base64 string when passed over JSON)
     * @param headers If set, overrides the request headers. Note that the overrides do not
     * extend to subsequent redirect hops, if a redirect happens. Another override
     * may be applied to a different request produced by a redirect.
     * @param interceptResponse If set, overrides response interception behavior for this request.
     */
    suspend fun continueRequest(
        url: String? = null,
        method: String? = null,
        postData: String? = null,
        headers: List<HeaderEntry>? = null,
        interceptResponse: Boolean? = null,
    )

    /**
     * Provides response to the request.
     *
     * @param responseCode An HTTP response code.
     * @param responseHeaders Response headers.
     * @param binaryResponseHeaders Alternative way of specifying response headers as a \0-separated
     * series of name: value pairs. Prefer the above method unless you
     * need to represent some non-UTF8 values that can't be transmitted
     * over the protocol as text. (Encoded as a base64 string when passed over JSON)
     * @param body A response body. If absent, original response body will be used if
     * the request is intercepted at the response stage and empty body
     * will be used if the request is intercepted at the request stage. (Encoded as a base64 string when passed over JSON)
     * @param responsePhrase A textual representation of responseCode.
     * If absent, a standard phrase matching responseCode is used.
     */
    suspend fun fulfillRequest(
        responseCode: Int,
        responseHeaders: List<HeaderEntry>? = null,
        binaryResponseHeaders: String? = null,
        body: String? = null,
        responsePhrase: String? = null,
    )

    /**
     * Continues loading of the paused response, optionally modifying the
     * response headers. If either responseCode or headers are modified, all of them
     * must be present.
     *
     * @param responseCode An HTTP response code. If absent, original response code will be used.
     * @param responsePhrase A textual representation of responseCode.
     * If absent, a standard phrase matching responseCode is used.
     * @param responseHeaders Response headers. If absent, original response headers will be used.
     * @param binaryResponseHeaders Alternative way of specifying response headers as a \0-separated
     * series of name: value pairs. Prefer the above method unless you
     * need to represent some non-UTF8 values that can't be transmitted
     * over the protocol as text. (Encoded as a base64 string when passed over JSON)
     */
    suspend fun continueResponse(
        responseCode: Int? = null,
        responsePhrase: String? = null,
        responseHeaders: List<HeaderEntry>? = null,
        binaryResponseHeaders: String? = null,
    )

}
