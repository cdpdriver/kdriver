package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.fetch: Fetch
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Fetch(this))

/**
 * A domain for letting clients substitute browser's network layer with client code.
 */
public class Fetch(
    private val cdp: CDP,
) : Domain {
    public val requestPaused: Flow<RequestPausedParameter> = cdp
        .events
        .filter {
            it.method == "Fetch.requestPaused"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val authRequired: Flow<AuthRequiredParameter> = cdp
        .events
        .filter {
            it.method == "Fetch.authRequired"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    /**
     * Disables the fetch domain.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Fetch.disable", parameter)
    }

    /**
     * Enables issuing of requestPaused events. A request will be paused until client
     * calls one of failRequest, fulfillRequest or continueRequest/continueWithAuth.
     */
    public suspend fun enable(args: EnableParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Fetch.enable", parameter)
    }

    /**
     * Enables issuing of requestPaused events. A request will be paused until client
     * calls one of failRequest, fulfillRequest or continueRequest/continueWithAuth.
     */
    public suspend fun enable(patterns: List<RequestPattern>? = null, handleAuthRequests: Boolean? = null) {
        val parameter = EnableParameter(patterns = patterns, handleAuthRequests = handleAuthRequests)
        enable(parameter)
    }

    /**
     * Causes the request to fail with specified reason.
     */
    public suspend fun failRequest(args: FailRequestParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Fetch.failRequest", parameter)
    }

    /**
     * Causes the request to fail with specified reason.
     */
    public suspend fun failRequest(requestId: String, errorReason: Network.ErrorReason) {
        val parameter = FailRequestParameter(requestId = requestId, errorReason = errorReason)
        failRequest(parameter)
    }

    /**
     * Provides response to the request.
     */
    public suspend fun fulfillRequest(args: FulfillRequestParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Fetch.fulfillRequest", parameter)
    }

    /**
     * Provides response to the request.
     */
    public suspend fun fulfillRequest(
        requestId: String,
        responseCode: Int,
        responseHeaders: List<HeaderEntry>? = null,
        binaryResponseHeaders: String? = null,
        body: String? = null,
        responsePhrase: String? = null,
    ) {
        val parameter = FulfillRequestParameter(
            requestId = requestId,
            responseCode = responseCode,
            responseHeaders = responseHeaders,
            binaryResponseHeaders = binaryResponseHeaders,
            body = body,
            responsePhrase = responsePhrase
        )
        fulfillRequest(parameter)
    }

    /**
     * Continues the request, optionally modifying some of its parameters.
     */
    public suspend fun continueRequest(args: ContinueRequestParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Fetch.continueRequest", parameter)
    }

    /**
     * Continues the request, optionally modifying some of its parameters.
     */
    public suspend fun continueRequest(
        requestId: String,
        url: String? = null,
        method: String? = null,
        postData: String? = null,
        headers: List<HeaderEntry>? = null,
        interceptResponse: Boolean? = null,
    ) {
        val parameter = ContinueRequestParameter(
            requestId = requestId,
            url = url,
            method = method,
            postData = postData,
            headers = headers,
            interceptResponse = interceptResponse
        )
        continueRequest(parameter)
    }

    /**
     * Continues a request supplying authChallengeResponse following authRequired event.
     */
    public suspend fun continueWithAuth(args: ContinueWithAuthParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Fetch.continueWithAuth", parameter)
    }

    /**
     * Continues a request supplying authChallengeResponse following authRequired event.
     */
    public suspend fun continueWithAuth(requestId: String, authChallengeResponse: AuthChallengeResponse) {
        val parameter = ContinueWithAuthParameter(requestId = requestId, authChallengeResponse = authChallengeResponse)
        continueWithAuth(parameter)
    }

    /**
     * Continues loading of the paused response, optionally modifying the
     * response headers. If either responseCode or headers are modified, all of them
     * must be present.
     */
    public suspend fun continueResponse(args: ContinueResponseParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Fetch.continueResponse", parameter)
    }

    /**
     * Continues loading of the paused response, optionally modifying the
     * response headers. If either responseCode or headers are modified, all of them
     * must be present.
     */
    public suspend fun continueResponse(
        requestId: String,
        responseCode: Int? = null,
        responsePhrase: String? = null,
        responseHeaders: List<HeaderEntry>? = null,
        binaryResponseHeaders: String? = null,
    ) {
        val parameter = ContinueResponseParameter(
            requestId = requestId,
            responseCode = responseCode,
            responsePhrase = responsePhrase,
            responseHeaders = responseHeaders,
            binaryResponseHeaders = binaryResponseHeaders
        )
        continueResponse(parameter)
    }

    /**
     * Causes the body of the response to be received from the server and
     * returned as a single string. May only be issued for a request that
     * is paused in the Response stage and is mutually exclusive with
     * takeResponseBodyForInterceptionAsStream. Calling other methods that
     * affect the request or disabling fetch domain before body is received
     * results in an undefined behavior.
     * Note that the response body is not available for redirects. Requests
     * paused in the _redirect received_ state may be differentiated by
     * `responseCode` and presence of `location` response header, see
     * comments to `requestPaused` for details.
     */
    public suspend fun getResponseBody(args: GetResponseBodyParameter): GetResponseBodyReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Fetch.getResponseBody", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Causes the body of the response to be received from the server and
     * returned as a single string. May only be issued for a request that
     * is paused in the Response stage and is mutually exclusive with
     * takeResponseBodyForInterceptionAsStream. Calling other methods that
     * affect the request or disabling fetch domain before body is received
     * results in an undefined behavior.
     * Note that the response body is not available for redirects. Requests
     * paused in the _redirect received_ state may be differentiated by
     * `responseCode` and presence of `location` response header, see
     * comments to `requestPaused` for details.
     */
    public suspend fun getResponseBody(requestId: String): GetResponseBodyReturn {
        val parameter = GetResponseBodyParameter(requestId = requestId)
        return getResponseBody(parameter)
    }

    /**
     * Returns a handle to the stream representing the response body.
     * The request must be paused in the HeadersReceived stage.
     * Note that after this command the request can't be continued
     * as is -- client either needs to cancel it or to provide the
     * response body.
     * The stream only supports sequential read, IO.read will fail if the position
     * is specified.
     * This method is mutually exclusive with getResponseBody.
     * Calling other methods that affect the request or disabling fetch
     * domain before body is received results in an undefined behavior.
     */
    public suspend fun takeResponseBodyAsStream(args: TakeResponseBodyAsStreamParameter): TakeResponseBodyAsStreamReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Fetch.takeResponseBodyAsStream", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns a handle to the stream representing the response body.
     * The request must be paused in the HeadersReceived stage.
     * Note that after this command the request can't be continued
     * as is -- client either needs to cancel it or to provide the
     * response body.
     * The stream only supports sequential read, IO.read will fail if the position
     * is specified.
     * This method is mutually exclusive with getResponseBody.
     * Calling other methods that affect the request or disabling fetch
     * domain before body is received results in an undefined behavior.
     */
    public suspend fun takeResponseBodyAsStream(requestId: String): TakeResponseBodyAsStreamReturn {
        val parameter = TakeResponseBodyAsStreamParameter(requestId = requestId)
        return takeResponseBodyAsStream(parameter)
    }

    /**
     * Stages of the request to handle. Request will intercept before the request is
     * sent. Response will intercept after the response is received (but before response
     * body is received).
     */
    @Serializable
    public enum class RequestStage {
        @SerialName("Request")
        REQUEST,

        @SerialName("Response")
        RESPONSE,
    }

    @Serializable
    public data class RequestPattern(
        /**
         * Wildcards (`'*'` -> zero or more, `'?'` -> exactly one) are allowed. Escape character is
         * backslash. Omitting is equivalent to `"*"`.
         */
        public val urlPattern: String? = null,
        /**
         * If set, only requests for matching resource types will be intercepted.
         */
        public val resourceType: Network.ResourceType? = null,
        /**
         * Stage at which to begin intercepting requests. Default is Request.
         */
        public val requestStage: RequestStage? = null,
    )

    /**
     * Response HTTP header entry
     */
    @Serializable
    public data class HeaderEntry(
        public val name: String,
        public val `value`: String,
    )

    /**
     * Authorization challenge for HTTP status code 401 or 407.
     */
    @Serializable
    public data class AuthChallenge(
        /**
         * Source of the authentication challenge.
         */
        public val source: String? = null,
        /**
         * Origin of the challenger.
         */
        public val origin: String,
        /**
         * The authentication scheme used, such as basic or digest
         */
        public val scheme: String,
        /**
         * The realm of the challenge. May be empty.
         */
        public val realm: String,
    )

    /**
     * Response to an AuthChallenge.
     */
    @Serializable
    public data class AuthChallengeResponse(
        /**
         * The decision on what to do in response to the authorization challenge.  Default means
         * deferring to the default behavior of the net stack, which will likely either the Cancel
         * authentication or display a popup dialog box.
         */
        public val response: String,
        /**
         * The username to provide, possibly empty. Should only be set if response is
         * ProvideCredentials.
         */
        public val username: String? = null,
        /**
         * The password to provide, possibly empty. Should only be set if response is
         * ProvideCredentials.
         */
        public val password: String? = null,
    )

    /**
     * Issued when the domain is enabled and the request URL matches the
     * specified filter. The request is paused until the client responds
     * with one of continueRequest, failRequest or fulfillRequest.
     * The stage of the request can be determined by presence of responseErrorReason
     * and responseStatusCode -- the request is at the response stage if either
     * of these fields is present and in the request stage otherwise.
     * Redirect responses and subsequent requests are reported similarly to regular
     * responses and requests. Redirect responses may be distinguished by the value
     * of `responseStatusCode` (which is one of 301, 302, 303, 307, 308) along with
     * presence of the `location` header. Requests resulting from a redirect will
     * have `redirectedRequestId` field set.
     */
    @Serializable
    public data class RequestPausedParameter(
        /**
         * Each request the page makes will have a unique id.
         */
        public val requestId: String,
        /**
         * The details of the request.
         */
        public val request: Network.Request,
        /**
         * The id of the frame that initiated the request.
         */
        public val frameId: String,
        /**
         * How the requested resource will be used.
         */
        public val resourceType: Network.ResourceType,
        /**
         * Response error if intercepted at response stage.
         */
        public val responseErrorReason: Network.ErrorReason? = null,
        /**
         * Response code if intercepted at response stage.
         */
        public val responseStatusCode: Int? = null,
        /**
         * Response status text if intercepted at response stage.
         */
        public val responseStatusText: String? = null,
        /**
         * Response headers if intercepted at the response stage.
         */
        public val responseHeaders: List<HeaderEntry>? = null,
        /**
         * If the intercepted request had a corresponding Network.requestWillBeSent event fired for it,
         * then this networkId will be the same as the requestId present in the requestWillBeSent event.
         */
        public val networkId: String? = null,
        /**
         * If the request is due to a redirect response from the server, the id of the request that
         * has caused the redirect.
         */
        public val redirectedRequestId: String? = null,
    )

    /**
     * Issued when the domain is enabled with handleAuthRequests set to true.
     * The request is paused until client responds with continueWithAuth.
     */
    @Serializable
    public data class AuthRequiredParameter(
        /**
         * Each request the page makes will have a unique id.
         */
        public val requestId: String,
        /**
         * The details of the request.
         */
        public val request: Network.Request,
        /**
         * The id of the frame that initiated the request.
         */
        public val frameId: String,
        /**
         * How the requested resource will be used.
         */
        public val resourceType: Network.ResourceType,
        /**
         * Details of the Authorization Challenge encountered.
         * If this is set, client should respond with continueRequest that
         * contains AuthChallengeResponse.
         */
        public val authChallenge: AuthChallenge,
    )

    @Serializable
    public data class EnableParameter(
        /**
         * If specified, only requests matching any of these patterns will produce
         * fetchRequested event and will be paused until clients response. If not set,
         * all requests will be affected.
         */
        public val patterns: List<RequestPattern>? = null,
        /**
         * If true, authRequired events will be issued and requests will be paused
         * expecting a call to continueWithAuth.
         */
        public val handleAuthRequests: Boolean? = null,
    )

    @Serializable
    public data class FailRequestParameter(
        /**
         * An id the client received in requestPaused event.
         */
        public val requestId: String,
        /**
         * Causes the request to fail with the given reason.
         */
        public val errorReason: Network.ErrorReason,
    )

    @Serializable
    public data class FulfillRequestParameter(
        /**
         * An id the client received in requestPaused event.
         */
        public val requestId: String,
        /**
         * An HTTP response code.
         */
        public val responseCode: Int,
        /**
         * Response headers.
         */
        public val responseHeaders: List<HeaderEntry>? = null,
        /**
         * Alternative way of specifying response headers as a \0-separated
         * series of name: value pairs. Prefer the above method unless you
         * need to represent some non-UTF8 values that can't be transmitted
         * over the protocol as text. (Encoded as a base64 string when passed over JSON)
         */
        public val binaryResponseHeaders: String? = null,
        /**
         * A response body. If absent, original response body will be used if
         * the request is intercepted at the response stage and empty body
         * will be used if the request is intercepted at the request stage. (Encoded as a base64 string when passed over JSON)
         */
        public val body: String? = null,
        /**
         * A textual representation of responseCode.
         * If absent, a standard phrase matching responseCode is used.
         */
        public val responsePhrase: String? = null,
    )

    @Serializable
    public data class ContinueRequestParameter(
        /**
         * An id the client received in requestPaused event.
         */
        public val requestId: String,
        /**
         * If set, the request url will be modified in a way that's not observable by page.
         */
        public val url: String? = null,
        /**
         * If set, the request method is overridden.
         */
        public val method: String? = null,
        /**
         * If set, overrides the post data in the request. (Encoded as a base64 string when passed over JSON)
         */
        public val postData: String? = null,
        /**
         * If set, overrides the request headers. Note that the overrides do not
         * extend to subsequent redirect hops, if a redirect happens. Another override
         * may be applied to a different request produced by a redirect.
         */
        public val headers: List<HeaderEntry>? = null,
        /**
         * If set, overrides response interception behavior for this request.
         */
        public val interceptResponse: Boolean? = null,
    )

    @Serializable
    public data class ContinueWithAuthParameter(
        /**
         * An id the client received in authRequired event.
         */
        public val requestId: String,
        /**
         * Response to  with an authChallenge.
         */
        public val authChallengeResponse: AuthChallengeResponse,
    )

    @Serializable
    public data class ContinueResponseParameter(
        /**
         * An id the client received in requestPaused event.
         */
        public val requestId: String,
        /**
         * An HTTP response code. If absent, original response code will be used.
         */
        public val responseCode: Int? = null,
        /**
         * A textual representation of responseCode.
         * If absent, a standard phrase matching responseCode is used.
         */
        public val responsePhrase: String? = null,
        /**
         * Response headers. If absent, original response headers will be used.
         */
        public val responseHeaders: List<HeaderEntry>? = null,
        /**
         * Alternative way of specifying response headers as a \0-separated
         * series of name: value pairs. Prefer the above method unless you
         * need to represent some non-UTF8 values that can't be transmitted
         * over the protocol as text. (Encoded as a base64 string when passed over JSON)
         */
        public val binaryResponseHeaders: String? = null,
    )

    @Serializable
    public data class GetResponseBodyParameter(
        /**
         * Identifier for the intercepted request to get body for.
         */
        public val requestId: String,
    )

    @Serializable
    public data class GetResponseBodyReturn(
        /**
         * Response body.
         */
        public val body: String,
        /**
         * True, if content was sent as base64.
         */
        public val base64Encoded: Boolean,
    )

    @Serializable
    public data class TakeResponseBodyAsStreamParameter(
        public val requestId: String,
    )

    @Serializable
    public data class TakeResponseBodyAsStreamReturn(
        public val stream: String,
    )
}
