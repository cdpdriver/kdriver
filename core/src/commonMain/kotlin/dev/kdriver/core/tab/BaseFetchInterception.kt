package dev.kdriver.core.tab

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.domain.Fetch
import dev.kdriver.cdp.domain.Fetch.HeaderEntry
import dev.kdriver.cdp.domain.Network
import dev.kdriver.cdp.domain.fetch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Base class to wait for a Fetch response matching a URL pattern.
 * Use this to collect and decode a paused fetch response, while keeping
 * the use block clean and returning its own result.
 *
 * @param tab The Tab instance to monitor.
 * @param urlPattern The URL pattern to match requests and responses.
 * @param requestStage The stage of the fetch request to intercept (e.g., request or response).
 * @param resourceType The type of resource to intercept (e.g., document, script, etc.).
 */
class BaseFetchInterception(
    private val tab: Tab,
    private val urlPattern: String,
    private val requestStage: Fetch.RequestStage,
    private val resourceType: Network.ResourceType,
) {

    private val responseDeferred = CompletableDeferred<Fetch.RequestPausedParameter>()
    private var job: Job? = null

    private val handler: suspend (Fetch.RequestPausedParameter) -> Unit = handler@{ event ->
        responseDeferred.complete(event)
        job?.cancel()
        job = null
    }

    /**
     * Start intercepting fetch responses matching the pattern,
     * run [block], and clean up after.
     */
    suspend fun <R> use(block: suspend BaseFetchInterception.() -> R): R {
        val coroutineScope = CoroutineScope(coroutineContext)
        tab.fetch.enable(
            listOf(
                Fetch.RequestPattern(
                    urlPattern = urlPattern,
                    resourceType = resourceType,
                    requestStage = requestStage
                )
            )
        )
        job = coroutineScope.launch {
            tab.fetch.requestPaused.collect { handler(it) }
        }
        try {
            return block()
        } finally {
            job?.cancel()
            tab.fetch.disable()
        }
    }

    /**
     * Returns the request event once it has been received.
     */
    suspend fun getRequestEvent(): Fetch.RequestPausedParameter = responseDeferred.await()

    /**
     * Fetches the raw response body once it has been received.
     */
    suspend fun getRawResponseBody(): Fetch.GetResponseBodyReturn {
        val requestId = getRequestEvent().requestId
        return tab.fetch.getResponseBody(requestId)
    }

    /**
     * Fetches the response body once it has been received.
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend inline fun <reified T> getResponseBody(): T {
        val rawBody = getRawResponseBody()
        val body = if (rawBody.base64Encoded) Base64.decode(rawBody.body).decodeToString() else rawBody.body
        return Serialization.json.decodeFromString<T>(body)
    }

    /**
     * Causes the request to fail with specified reason.
     *
     * @param errorReason Causes the request to fail with the given reason.
     */
    suspend fun failRequest(errorReason: Network.ErrorReason) {
        val requestId = getRequestEvent().requestId
        tab.fetch.failRequest(requestId, errorReason)
    }

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
    ) {
        val requestId = getRequestEvent().requestId
        tab.fetch.continueRequest(
            requestId = requestId,
            url = url,
            method = method,
            postData = postData,
            headers = headers,
            interceptResponse = interceptResponse
        )
    }

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
    ) {
        val requestId = getRequestEvent().requestId
        tab.fetch.fulfillRequest(
            requestId = requestId,
            responseCode = responseCode,
            responseHeaders = responseHeaders,
            binaryResponseHeaders = binaryResponseHeaders,
            body = body,
            responsePhrase = responsePhrase
        )
    }

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
    ) {
        val requestId = getRequestEvent().requestId
        tab.fetch.continueResponse(
            requestId = requestId,
            responseCode = responseCode,
            responsePhrase = responsePhrase,
            responseHeaders = responseHeaders,
            binaryResponseHeaders = binaryResponseHeaders
        )
    }

}
