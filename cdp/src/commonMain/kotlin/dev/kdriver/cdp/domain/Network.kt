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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.network: Network
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Network(this))

/**
 * Network domain allows tracking network activities of the page. It exposes information about http,
 * file, data and other requests and responses, their headers, bodies, timing, etc.
 */
public class Network(
    private val cdp: CDP,
) : Domain {
    /**
     * Fired when data chunk was received over the network.
     */
    public val dataReceived: Flow<DataReceivedParameter> = cdp
        .events
        .filter { it.method == "Network.dataReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when EventSource message is received.
     */
    public val eventSourceMessageReceived: Flow<EventSourceMessageReceivedParameter> = cdp
        .events
        .filter { it.method == "Network.eventSourceMessageReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when HTTP request has failed to load.
     */
    public val loadingFailed: Flow<LoadingFailedParameter> = cdp
        .events
        .filter { it.method == "Network.loadingFailed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when HTTP request has finished loading.
     */
    public val loadingFinished: Flow<LoadingFinishedParameter> = cdp
        .events
        .filter { it.method == "Network.loadingFinished" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Details of an intercepted HTTP request, which must be either allowed, blocked, modified or
     * mocked.
     * Deprecated, use Fetch.requestPaused instead.
     */
    public val requestIntercepted: Flow<RequestInterceptedParameter> = cdp
        .events
        .filter { it.method == "Network.requestIntercepted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired if request ended up loading from cache.
     */
    public val requestServedFromCache: Flow<RequestServedFromCacheParameter> = cdp
        .events
        .filter { it.method == "Network.requestServedFromCache" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when page is about to send HTTP request.
     */
    public val requestWillBeSent: Flow<RequestWillBeSentParameter> = cdp
        .events
        .filter { it.method == "Network.requestWillBeSent" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when resource loading priority is changed
     */
    public val resourceChangedPriority: Flow<ResourceChangedPriorityParameter> = cdp
        .events
        .filter { it.method == "Network.resourceChangedPriority" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when a signed exchange was received over the network
     */
    public val signedExchangeReceived: Flow<SignedExchangeReceivedParameter> = cdp
        .events
        .filter { it.method == "Network.signedExchangeReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when HTTP response is available.
     */
    public val responseReceived: Flow<ResponseReceivedParameter> = cdp
        .events
        .filter { it.method == "Network.responseReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when WebSocket is closed.
     */
    public val webSocketClosed: Flow<WebSocketClosedParameter> = cdp
        .events
        .filter { it.method == "Network.webSocketClosed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired upon WebSocket creation.
     */
    public val webSocketCreated: Flow<WebSocketCreatedParameter> = cdp
        .events
        .filter { it.method == "Network.webSocketCreated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when WebSocket message error occurs.
     */
    public val webSocketFrameError: Flow<WebSocketFrameErrorParameter> = cdp
        .events
        .filter { it.method == "Network.webSocketFrameError" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when WebSocket message is received.
     */
    public val webSocketFrameReceived: Flow<WebSocketFrameReceivedParameter> = cdp
        .events
        .filter { it.method == "Network.webSocketFrameReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when WebSocket message is sent.
     */
    public val webSocketFrameSent: Flow<WebSocketFrameSentParameter> = cdp
        .events
        .filter { it.method == "Network.webSocketFrameSent" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when WebSocket handshake response becomes available.
     */
    public val webSocketHandshakeResponseReceived: Flow<WebSocketHandshakeResponseReceivedParameter> = cdp
        .events
        .filter { it.method == "Network.webSocketHandshakeResponseReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when WebSocket is about to initiate handshake.
     */
    public val webSocketWillSendHandshakeRequest: Flow<WebSocketWillSendHandshakeRequestParameter> =
        cdp
            .events
            .filter { it.method == "Network.webSocketWillSendHandshakeRequest" }
            .map { it.params }
            .filterNotNull()
            .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired upon WebTransport creation.
     */
    public val webTransportCreated: Flow<WebTransportCreatedParameter> = cdp
        .events
        .filter { it.method == "Network.webTransportCreated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when WebTransport handshake is finished.
     */
    public val webTransportConnectionEstablished: Flow<WebTransportConnectionEstablishedParameter> =
        cdp
            .events
            .filter { it.method == "Network.webTransportConnectionEstablished" }
            .map { it.params }
            .filterNotNull()
            .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when WebTransport is disposed.
     */
    public val webTransportClosed: Flow<WebTransportClosedParameter> = cdp
        .events
        .filter { it.method == "Network.webTransportClosed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired upon direct_socket.TCPSocket creation.
     */
    public val directTCPSocketCreated: Flow<DirectTCPSocketCreatedParameter> = cdp
        .events
        .filter { it.method == "Network.directTCPSocketCreated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when direct_socket.TCPSocket connection is opened.
     */
    public val directTCPSocketOpened: Flow<DirectTCPSocketOpenedParameter> = cdp
        .events
        .filter { it.method == "Network.directTCPSocketOpened" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when direct_socket.TCPSocket is aborted.
     */
    public val directTCPSocketAborted: Flow<DirectTCPSocketAbortedParameter> = cdp
        .events
        .filter { it.method == "Network.directTCPSocketAborted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when direct_socket.TCPSocket is closed.
     */
    public val directTCPSocketClosed: Flow<DirectTCPSocketClosedParameter> = cdp
        .events
        .filter { it.method == "Network.directTCPSocketClosed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when data is sent to tcp direct socket stream.
     */
    public val directTCPSocketChunkSent: Flow<DirectTCPSocketChunkSentParameter> = cdp
        .events
        .filter { it.method == "Network.directTCPSocketChunkSent" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when data is received from tcp direct socket stream.
     */
    public val directTCPSocketChunkReceived: Flow<DirectTCPSocketChunkReceivedParameter> = cdp
        .events
        .filter { it.method == "Network.directTCPSocketChunkReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired upon direct_socket.UDPSocket creation.
     */
    public val directUDPSocketCreated: Flow<DirectUDPSocketCreatedParameter> = cdp
        .events
        .filter { it.method == "Network.directUDPSocketCreated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when direct_socket.UDPSocket connection is opened.
     */
    public val directUDPSocketOpened: Flow<DirectUDPSocketOpenedParameter> = cdp
        .events
        .filter { it.method == "Network.directUDPSocketOpened" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when direct_socket.UDPSocket is aborted.
     */
    public val directUDPSocketAborted: Flow<DirectUDPSocketAbortedParameter> = cdp
        .events
        .filter { it.method == "Network.directUDPSocketAborted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when direct_socket.UDPSocket is closed.
     */
    public val directUDPSocketClosed: Flow<DirectUDPSocketClosedParameter> = cdp
        .events
        .filter { it.method == "Network.directUDPSocketClosed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when message is sent to udp direct socket stream.
     */
    public val directUDPSocketChunkSent: Flow<DirectUDPSocketChunkSentParameter> = cdp
        .events
        .filter { it.method == "Network.directUDPSocketChunkSent" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when message is received from udp direct socket stream.
     */
    public val directUDPSocketChunkReceived: Flow<DirectUDPSocketChunkReceivedParameter> = cdp
        .events
        .filter { it.method == "Network.directUDPSocketChunkReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when additional information about a requestWillBeSent event is available from the
     * network stack. Not every requestWillBeSent event will have an additional
     * requestWillBeSentExtraInfo fired for it, and there is no guarantee whether requestWillBeSent
     * or requestWillBeSentExtraInfo will be fired first for the same request.
     */
    public val requestWillBeSentExtraInfo: Flow<RequestWillBeSentExtraInfoParameter> = cdp
        .events
        .filter { it.method == "Network.requestWillBeSentExtraInfo" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when additional information about a responseReceived event is available from the network
     * stack. Not every responseReceived event will have an additional responseReceivedExtraInfo for
     * it, and responseReceivedExtraInfo may be fired before or after responseReceived.
     */
    public val responseReceivedExtraInfo: Flow<ResponseReceivedExtraInfoParameter> = cdp
        .events
        .filter { it.method == "Network.responseReceivedExtraInfo" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when 103 Early Hints headers is received in addition to the common response.
     * Not every responseReceived event will have an responseReceivedEarlyHints fired.
     * Only one responseReceivedEarlyHints may be fired for eached responseReceived event.
     */
    public val responseReceivedEarlyHints: Flow<ResponseReceivedEarlyHintsParameter> = cdp
        .events
        .filter { it.method == "Network.responseReceivedEarlyHints" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired exactly once for each Trust Token operation. Depending on
     * the type of the operation and whether the operation succeeded or
     * failed, the event is fired before the corresponding request was sent
     * or after the response was received.
     */
    public val trustTokenOperationDone: Flow<TrustTokenOperationDoneParameter> = cdp
        .events
        .filter { it.method == "Network.trustTokenOperationDone" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired once security policy has been updated.
     */
    public val policyUpdated: Flow<Unit> = cdp
        .events
        .filter { it.method == "Network.policyUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired once when parsing the .wbn file has succeeded.
     * The event contains the information about the web bundle contents.
     */
    public val subresourceWebBundleMetadataReceived:
            Flow<SubresourceWebBundleMetadataReceivedParameter> = cdp
        .events
        .filter { it.method == "Network.subresourceWebBundleMetadataReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired once when parsing the .wbn file has failed.
     */
    public val subresourceWebBundleMetadataError: Flow<SubresourceWebBundleMetadataErrorParameter> =
        cdp
            .events
            .filter { it.method == "Network.subresourceWebBundleMetadataError" }
            .map { it.params }
            .filterNotNull()
            .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when handling requests for resources within a .wbn file.
     * Note: this will only be fired for resources that are requested by the webpage.
     */
    public val subresourceWebBundleInnerResponseParsed:
            Flow<SubresourceWebBundleInnerResponseParsedParameter> = cdp
        .events
        .filter { it.method == "Network.subresourceWebBundleInnerResponseParsed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when request for resources within a .wbn file failed.
     */
    public val subresourceWebBundleInnerResponseError:
            Flow<SubresourceWebBundleInnerResponseErrorParameter> = cdp
        .events
        .filter { it.method == "Network.subresourceWebBundleInnerResponseError" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Is sent whenever a new report is added.
     * And after 'enableReportingApi' for all existing reports.
     */
    public val reportingApiReportAdded: Flow<ReportingApiReportAddedParameter> = cdp
        .events
        .filter { it.method == "Network.reportingApiReportAdded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val reportingApiReportUpdated: Flow<ReportingApiReportUpdatedParameter> = cdp
        .events
        .filter { it.method == "Network.reportingApiReportUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val reportingApiEndpointsChangedForOrigin:
            Flow<ReportingApiEndpointsChangedForOriginParameter> = cdp
        .events
        .filter { it.method == "Network.reportingApiEndpointsChangedForOrigin" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Sets a list of content encodings that will be accepted. Empty list means no encoding is accepted.
     */
    public suspend fun setAcceptedEncodings(args: SetAcceptedEncodingsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.setAcceptedEncodings", parameter)
    }

    /**
     * Sets a list of content encodings that will be accepted. Empty list means no encoding is accepted.
     *
     * @param encodings List of accepted content encodings.
     */
    public suspend fun setAcceptedEncodings(encodings: List<ContentEncoding>) {
        val parameter = SetAcceptedEncodingsParameter(encodings = encodings)
        setAcceptedEncodings(parameter)
    }

    /**
     * Clears accepted encodings set by setAcceptedEncodings
     */
    public suspend fun clearAcceptedEncodingsOverride() {
        val parameter = null
        cdp.callCommand("Network.clearAcceptedEncodingsOverride", parameter)
    }

    /**
     * Tells whether clearing browser cache is supported.
     */
    @Deprecated(message = "")
    public suspend fun canClearBrowserCache(): CanClearBrowserCacheReturn {
        val parameter = null
        val result = cdp.callCommand("Network.canClearBrowserCache", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Tells whether clearing browser cookies is supported.
     */
    @Deprecated(message = "")
    public suspend fun canClearBrowserCookies(): CanClearBrowserCookiesReturn {
        val parameter = null
        val result = cdp.callCommand("Network.canClearBrowserCookies", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Tells whether emulation of network conditions is supported.
     */
    @Deprecated(message = "")
    public suspend fun canEmulateNetworkConditions(): CanEmulateNetworkConditionsReturn {
        val parameter = null
        val result = cdp.callCommand("Network.canEmulateNetworkConditions", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Clears browser cache.
     */
    public suspend fun clearBrowserCache() {
        val parameter = null
        cdp.callCommand("Network.clearBrowserCache", parameter)
    }

    /**
     * Clears browser cookies.
     */
    public suspend fun clearBrowserCookies() {
        val parameter = null
        cdp.callCommand("Network.clearBrowserCookies", parameter)
    }

    /**
     * Response to Network.requestIntercepted which either modifies the request to continue with any
     * modifications, or blocks it, or completes it with the provided response bytes. If a network
     * fetch occurs as a result which encounters a redirect an additional Network.requestIntercepted
     * event will be sent with the same InterceptionId.
     * Deprecated, use Fetch.continueRequest, Fetch.fulfillRequest and Fetch.failRequest instead.
     */
    @Deprecated(message = "")
    public suspend fun continueInterceptedRequest(args: ContinueInterceptedRequestParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.continueInterceptedRequest", parameter)
    }

    /**
     * Response to Network.requestIntercepted which either modifies the request to continue with any
     * modifications, or blocks it, or completes it with the provided response bytes. If a network
     * fetch occurs as a result which encounters a redirect an additional Network.requestIntercepted
     * event will be sent with the same InterceptionId.
     * Deprecated, use Fetch.continueRequest, Fetch.fulfillRequest and Fetch.failRequest instead.
     *
     * @param interceptionId No description
     * @param errorReason If set this causes the request to fail with the given reason. Passing `Aborted` for requests
     * marked with `isNavigationRequest` also cancels the navigation. Must not be set in response
     * to an authChallenge.
     * @param rawResponse If set the requests completes using with the provided base64 encoded raw response, including
     * HTTP status line and headers etc... Must not be set in response to an authChallenge. (Encoded as a base64 string when passed over JSON)
     * @param url If set the request url will be modified in a way that's not observable by page. Must not be
     * set in response to an authChallenge.
     * @param method If set this allows the request method to be overridden. Must not be set in response to an
     * authChallenge.
     * @param postData If set this allows postData to be set. Must not be set in response to an authChallenge.
     * @param headers If set this allows the request headers to be changed. Must not be set in response to an
     * authChallenge.
     * @param authChallengeResponse Response to a requestIntercepted with an authChallenge. Must not be set otherwise.
     */
    @Deprecated(message = "")
    public suspend fun continueInterceptedRequest(
        interceptionId: String,
        errorReason: ErrorReason? = null,
        rawResponse: String? = null,
        url: String? = null,
        method: String? = null,
        postData: String? = null,
        headers: Map<String, JsonElement>? = null,
        authChallengeResponse: AuthChallengeResponse? = null,
    ) {
        val parameter = ContinueInterceptedRequestParameter(
            interceptionId = interceptionId,
            errorReason = errorReason,
            rawResponse = rawResponse,
            url = url,
            method = method,
            postData = postData,
            headers = headers,
            authChallengeResponse = authChallengeResponse
        )
        continueInterceptedRequest(parameter)
    }

    /**
     * Deletes browser cookies with matching name and url or domain/path/partitionKey pair.
     */
    public suspend fun deleteCookies(args: DeleteCookiesParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.deleteCookies", parameter)
    }

    /**
     * Deletes browser cookies with matching name and url or domain/path/partitionKey pair.
     *
     * @param name Name of the cookies to remove.
     * @param url If specified, deletes all the cookies with the given name where domain and path match
     * provided URL.
     * @param domain If specified, deletes only cookies with the exact domain.
     * @param path If specified, deletes only cookies with the exact path.
     * @param partitionKey If specified, deletes only cookies with the the given name and partitionKey where
     * all partition key attributes match the cookie partition key attribute.
     */
    public suspend fun deleteCookies(
        name: String,
        url: String? = null,
        domain: String? = null,
        path: String? = null,
        partitionKey: CookiePartitionKey? = null,
    ) {
        val parameter =
            DeleteCookiesParameter(name = name, url = url, domain = domain, path = path, partitionKey = partitionKey)
        deleteCookies(parameter)
    }

    /**
     * Disables network tracking, prevents network events from being sent to the client.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Network.disable", parameter)
    }

    /**
     * Activates emulation of network conditions.
     */
    public suspend fun emulateNetworkConditions(args: EmulateNetworkConditionsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.emulateNetworkConditions", parameter)
    }

    /**
     * Activates emulation of network conditions.
     *
     * @param offline True to emulate internet disconnection.
     * @param latency Minimum latency from request sent to response headers received (ms).
     * @param downloadThroughput Maximal aggregated download throughput (bytes/sec). -1 disables download throttling.
     * @param uploadThroughput Maximal aggregated upload throughput (bytes/sec).  -1 disables upload throttling.
     * @param connectionType Connection type if known.
     * @param packetLoss WebRTC packet loss (percent, 0-100). 0 disables packet loss emulation, 100 drops all the packets.
     * @param packetQueueLength WebRTC packet queue length (packet). 0 removes any queue length limitations.
     * @param packetReordering WebRTC packetReordering feature.
     */
    public suspend fun emulateNetworkConditions(
        offline: Boolean,
        latency: Double,
        downloadThroughput: Double,
        uploadThroughput: Double,
        connectionType: ConnectionType? = null,
        packetLoss: Double? = null,
        packetQueueLength: Int? = null,
        packetReordering: Boolean? = null,
    ) {
        val parameter = EmulateNetworkConditionsParameter(
            offline = offline,
            latency = latency,
            downloadThroughput = downloadThroughput,
            uploadThroughput = uploadThroughput,
            connectionType = connectionType,
            packetLoss = packetLoss,
            packetQueueLength = packetQueueLength,
            packetReordering = packetReordering
        )
        emulateNetworkConditions(parameter)
    }

    /**
     * Enables network tracking, network events will now be delivered to the client.
     */
    public suspend fun enable(args: EnableParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.enable", parameter)
    }

    /**
     * Enables network tracking, network events will now be delivered to the client.
     *
     * @param maxTotalBufferSize Buffer size in bytes to use when preserving network payloads (XHRs, etc).
     * @param maxResourceBufferSize Per-resource buffer size in bytes to use when preserving network payloads (XHRs, etc).
     * @param maxPostDataSize Longest post body size (in bytes) that would be included in requestWillBeSent notification
     * @param reportDirectSocketTraffic Whether DirectSocket chunk send/receive events should be reported.
     */
    public suspend fun enable(
        maxTotalBufferSize: Int? = null,
        maxResourceBufferSize: Int? = null,
        maxPostDataSize: Int? = null,
        reportDirectSocketTraffic: Boolean? = null,
    ) {
        val parameter = EnableParameter(
            maxTotalBufferSize = maxTotalBufferSize,
            maxResourceBufferSize = maxResourceBufferSize,
            maxPostDataSize = maxPostDataSize,
            reportDirectSocketTraffic = reportDirectSocketTraffic
        )
        enable(parameter)
    }

    /**
     * Returns all browser cookies. Depending on the backend support, will return detailed cookie
     * information in the `cookies` field.
     * Deprecated. Use Storage.getCookies instead.
     */
    @Deprecated(message = "")
    public suspend fun getAllCookies(): GetAllCookiesReturn {
        val parameter = null
        val result = cdp.callCommand("Network.getAllCookies", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the DER-encoded certificate.
     */
    public suspend fun getCertificate(args: GetCertificateParameter): GetCertificateReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.getCertificate", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the DER-encoded certificate.
     *
     * @param origin Origin to get certificate for.
     */
    public suspend fun getCertificate(origin: String): GetCertificateReturn {
        val parameter = GetCertificateParameter(origin = origin)
        return getCertificate(parameter)
    }

    /**
     * Returns all browser cookies for the current URL. Depending on the backend support, will return
     * detailed cookie information in the `cookies` field.
     */
    public suspend fun getCookies(args: GetCookiesParameter): GetCookiesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.getCookies", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns all browser cookies for the current URL. Depending on the backend support, will return
     * detailed cookie information in the `cookies` field.
     *
     * @param urls The list of URLs for which applicable cookies will be fetched.
     * If not specified, it's assumed to be set to the list containing
     * the URLs of the page and all of its subframes.
     */
    public suspend fun getCookies(urls: List<String>? = null): GetCookiesReturn {
        val parameter = GetCookiesParameter(urls = urls)
        return getCookies(parameter)
    }

    /**
     * Returns content served for the given request.
     */
    public suspend fun getResponseBody(args: GetResponseBodyParameter): GetResponseBodyReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.getResponseBody", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns content served for the given request.
     *
     * @param requestId Identifier of the network request to get content for.
     */
    public suspend fun getResponseBody(requestId: String): GetResponseBodyReturn {
        val parameter = GetResponseBodyParameter(requestId = requestId)
        return getResponseBody(parameter)
    }

    /**
     * Returns post data sent with the request. Returns an error when no data was sent with the request.
     */
    public suspend fun getRequestPostData(args: GetRequestPostDataParameter): GetRequestPostDataReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.getRequestPostData", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns post data sent with the request. Returns an error when no data was sent with the request.
     *
     * @param requestId Identifier of the network request to get content for.
     */
    public suspend fun getRequestPostData(requestId: String): GetRequestPostDataReturn {
        val parameter = GetRequestPostDataParameter(requestId = requestId)
        return getRequestPostData(parameter)
    }

    /**
     * Returns content served for the given currently intercepted request.
     */
    public suspend fun getResponseBodyForInterception(args: GetResponseBodyForInterceptionParameter): GetResponseBodyForInterceptionReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.getResponseBodyForInterception", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns content served for the given currently intercepted request.
     *
     * @param interceptionId Identifier for the intercepted request to get body for.
     */
    public suspend fun getResponseBodyForInterception(interceptionId: String): GetResponseBodyForInterceptionReturn {
        val parameter = GetResponseBodyForInterceptionParameter(interceptionId = interceptionId)
        return getResponseBodyForInterception(parameter)
    }

    /**
     * Returns a handle to the stream representing the response body. Note that after this command,
     * the intercepted request can't be continued as is -- you either need to cancel it or to provide
     * the response body. The stream only supports sequential read, IO.read will fail if the position
     * is specified.
     */
    public suspend fun takeResponseBodyForInterceptionAsStream(args: TakeResponseBodyForInterceptionAsStreamParameter): TakeResponseBodyForInterceptionAsStreamReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.takeResponseBodyForInterceptionAsStream", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns a handle to the stream representing the response body. Note that after this command,
     * the intercepted request can't be continued as is -- you either need to cancel it or to provide
     * the response body. The stream only supports sequential read, IO.read will fail if the position
     * is specified.
     *
     * @param interceptionId No description
     */
    public suspend fun takeResponseBodyForInterceptionAsStream(interceptionId: String): TakeResponseBodyForInterceptionAsStreamReturn {
        val parameter = TakeResponseBodyForInterceptionAsStreamParameter(interceptionId = interceptionId)
        return takeResponseBodyForInterceptionAsStream(parameter)
    }

    /**
     * This method sends a new XMLHttpRequest which is identical to the original one. The following
     * parameters should be identical: method, url, async, request body, extra headers, withCredentials
     * attribute, user, password.
     */
    public suspend fun replayXHR(args: ReplayXHRParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.replayXHR", parameter)
    }

    /**
     * This method sends a new XMLHttpRequest which is identical to the original one. The following
     * parameters should be identical: method, url, async, request body, extra headers, withCredentials
     * attribute, user, password.
     *
     * @param requestId Identifier of XHR to replay.
     */
    public suspend fun replayXHR(requestId: String) {
        val parameter = ReplayXHRParameter(requestId = requestId)
        replayXHR(parameter)
    }

    /**
     * Searches for given string in response content.
     */
    public suspend fun searchInResponseBody(args: SearchInResponseBodyParameter): SearchInResponseBodyReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.searchInResponseBody", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Searches for given string in response content.
     *
     * @param requestId Identifier of the network response to search.
     * @param query String to search for.
     * @param caseSensitive If true, search is case sensitive.
     * @param isRegex If true, treats string parameter as regex.
     */
    public suspend fun searchInResponseBody(
        requestId: String,
        query: String,
        caseSensitive: Boolean? = null,
        isRegex: Boolean? = null,
    ): SearchInResponseBodyReturn {
        val parameter = SearchInResponseBodyParameter(
            requestId = requestId,
            query = query,
            caseSensitive = caseSensitive,
            isRegex = isRegex
        )
        return searchInResponseBody(parameter)
    }

    /**
     * Blocks URLs from loading.
     */
    public suspend fun setBlockedURLs(args: SetBlockedURLsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.setBlockedURLs", parameter)
    }

    /**
     * Blocks URLs from loading.
     *
     * @param urls URL patterns to block. Wildcards ('*') are allowed.
     */
    public suspend fun setBlockedURLs(urls: List<String>) {
        val parameter = SetBlockedURLsParameter(urls = urls)
        setBlockedURLs(parameter)
    }

    /**
     * Toggles ignoring of service worker for each request.
     */
    public suspend fun setBypassServiceWorker(args: SetBypassServiceWorkerParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.setBypassServiceWorker", parameter)
    }

    /**
     * Toggles ignoring of service worker for each request.
     *
     * @param bypass Bypass service worker and load from network.
     */
    public suspend fun setBypassServiceWorker(bypass: Boolean) {
        val parameter = SetBypassServiceWorkerParameter(bypass = bypass)
        setBypassServiceWorker(parameter)
    }

    /**
     * Toggles ignoring cache for each request. If `true`, cache will not be used.
     */
    public suspend fun setCacheDisabled(args: SetCacheDisabledParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.setCacheDisabled", parameter)
    }

    /**
     * Toggles ignoring cache for each request. If `true`, cache will not be used.
     *
     * @param cacheDisabled Cache disabled state.
     */
    public suspend fun setCacheDisabled(cacheDisabled: Boolean) {
        val parameter = SetCacheDisabledParameter(cacheDisabled = cacheDisabled)
        setCacheDisabled(parameter)
    }

    /**
     * Sets a cookie with the given cookie data; may overwrite equivalent cookies if they exist.
     */
    public suspend fun setCookie(args: SetCookieParameter): SetCookieReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.setCookie", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Sets a cookie with the given cookie data; may overwrite equivalent cookies if they exist.
     *
     * @param name Cookie name.
     * @param value Cookie value.
     * @param url The request-URI to associate with the setting of the cookie. This value can affect the
     * default domain, path, source port, and source scheme values of the created cookie.
     * @param domain Cookie domain.
     * @param path Cookie path.
     * @param secure True if cookie is secure.
     * @param httpOnly True if cookie is http-only.
     * @param sameSite Cookie SameSite type.
     * @param expires Cookie expiration date, session cookie if not set
     * @param priority Cookie Priority type.
     * @param sameParty True if cookie is SameParty.
     * @param sourceScheme Cookie source scheme type.
     * @param sourcePort Cookie source port. Valid values are {-1, [1, 65535]}, -1 indicates an unspecified port.
     * An unspecified port value allows protocol clients to emulate legacy cookie scope for the port.
     * This is a temporary ability and it will be removed in the future.
     * @param partitionKey Cookie partition key. If not set, the cookie will be set as not partitioned.
     */
    public suspend fun setCookie(
        name: String,
        `value`: String,
        url: String? = null,
        domain: String? = null,
        path: String? = null,
        secure: Boolean? = null,
        httpOnly: Boolean? = null,
        sameSite: CookieSameSite? = null,
        expires: Double? = null,
        priority: CookiePriority? = null,
        sameParty: Boolean? = null,
        sourceScheme: CookieSourceScheme? = null,
        sourcePort: Int? = null,
        partitionKey: CookiePartitionKey? = null,
    ): SetCookieReturn {
        val parameter = SetCookieParameter(
            name = name,
            value = value,
            url = url,
            domain = domain,
            path = path,
            secure = secure,
            httpOnly = httpOnly,
            sameSite = sameSite,
            expires = expires,
            priority = priority,
            sameParty = sameParty,
            sourceScheme = sourceScheme,
            sourcePort = sourcePort,
            partitionKey = partitionKey
        )
        return setCookie(parameter)
    }

    /**
     * Sets given cookies.
     */
    public suspend fun setCookies(args: SetCookiesParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.setCookies", parameter)
    }

    /**
     * Sets given cookies.
     *
     * @param cookies Cookies to be set.
     */
    public suspend fun setCookies(cookies: List<CookieParam>) {
        val parameter = SetCookiesParameter(cookies = cookies)
        setCookies(parameter)
    }

    /**
     * Specifies whether to always send extra HTTP headers with the requests from this page.
     */
    public suspend fun setExtraHTTPHeaders(args: SetExtraHTTPHeadersParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.setExtraHTTPHeaders", parameter)
    }

    /**
     * Specifies whether to always send extra HTTP headers with the requests from this page.
     *
     * @param headers Map with extra HTTP headers.
     */
    public suspend fun setExtraHTTPHeaders(headers: Map<String, JsonElement>) {
        val parameter = SetExtraHTTPHeadersParameter(headers = headers)
        setExtraHTTPHeaders(parameter)
    }

    /**
     * Specifies whether to attach a page script stack id in requests
     */
    public suspend fun setAttachDebugStack(args: SetAttachDebugStackParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.setAttachDebugStack", parameter)
    }

    /**
     * Specifies whether to attach a page script stack id in requests
     *
     * @param enabled Whether to attach a page script stack for debugging purpose.
     */
    public suspend fun setAttachDebugStack(enabled: Boolean) {
        val parameter = SetAttachDebugStackParameter(enabled = enabled)
        setAttachDebugStack(parameter)
    }

    /**
     * Sets the requests to intercept that match the provided patterns and optionally resource types.
     * Deprecated, please use Fetch.enable instead.
     */
    @Deprecated(message = "")
    public suspend fun setRequestInterception(args: SetRequestInterceptionParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.setRequestInterception", parameter)
    }

    /**
     * Sets the requests to intercept that match the provided patterns and optionally resource types.
     * Deprecated, please use Fetch.enable instead.
     *
     * @param patterns Requests matching any of these patterns will be forwarded and wait for the corresponding
     * continueInterceptedRequest call.
     */
    @Deprecated(message = "")
    public suspend fun setRequestInterception(patterns: List<RequestPattern>) {
        val parameter = SetRequestInterceptionParameter(patterns = patterns)
        setRequestInterception(parameter)
    }

    /**
     * Allows overriding user agent with the given string.
     */
    public suspend fun setUserAgentOverride(args: SetUserAgentOverrideParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.setUserAgentOverride", parameter)
    }

    /**
     * Allows overriding user agent with the given string.
     *
     * @param userAgent User agent to use.
     * @param acceptLanguage Browser language to emulate.
     * @param platform The platform navigator.platform should return.
     * @param userAgentMetadata To be sent in Sec-CH-UA-* headers and returned in navigator.userAgentData
     */
    public suspend fun setUserAgentOverride(
        userAgent: String,
        acceptLanguage: String? = null,
        platform: String? = null,
        userAgentMetadata: Emulation.UserAgentMetadata? = null,
    ) {
        val parameter = SetUserAgentOverrideParameter(
            userAgent = userAgent,
            acceptLanguage = acceptLanguage,
            platform = platform,
            userAgentMetadata = userAgentMetadata
        )
        setUserAgentOverride(parameter)
    }

    /**
     * Enables streaming of the response for the given requestId.
     * If enabled, the dataReceived event contains the data that was received during streaming.
     */
    public suspend fun streamResourceContent(args: StreamResourceContentParameter): StreamResourceContentReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.streamResourceContent", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Enables streaming of the response for the given requestId.
     * If enabled, the dataReceived event contains the data that was received during streaming.
     *
     * @param requestId Identifier of the request to stream.
     */
    public suspend fun streamResourceContent(requestId: String): StreamResourceContentReturn {
        val parameter = StreamResourceContentParameter(requestId = requestId)
        return streamResourceContent(parameter)
    }

    /**
     * Returns information about the COEP/COOP isolation status.
     */
    public suspend fun getSecurityIsolationStatus(args: GetSecurityIsolationStatusParameter): GetSecurityIsolationStatusReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.getSecurityIsolationStatus", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns information about the COEP/COOP isolation status.
     *
     * @param frameId If no frameId is provided, the status of the target is provided.
     */
    public suspend fun getSecurityIsolationStatus(frameId: String? = null): GetSecurityIsolationStatusReturn {
        val parameter = GetSecurityIsolationStatusParameter(frameId = frameId)
        return getSecurityIsolationStatus(parameter)
    }

    /**
     * Enables tracking for the Reporting API, events generated by the Reporting API will now be delivered to the client.
     * Enabling triggers 'reportingApiReportAdded' for all existing reports.
     */
    public suspend fun enableReportingApi(args: EnableReportingApiParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.enableReportingApi", parameter)
    }

    /**
     * Enables tracking for the Reporting API, events generated by the Reporting API will now be delivered to the client.
     * Enabling triggers 'reportingApiReportAdded' for all existing reports.
     *
     * @param enable Whether to enable or disable events for the Reporting API
     */
    public suspend fun enableReportingApi(enable: Boolean) {
        val parameter = EnableReportingApiParameter(enable = enable)
        enableReportingApi(parameter)
    }

    /**
     * Fetches the resource and returns the content.
     */
    public suspend fun loadNetworkResource(args: LoadNetworkResourceParameter): LoadNetworkResourceReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Network.loadNetworkResource", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Fetches the resource and returns the content.
     *
     * @param frameId Frame id to get the resource for. Mandatory for frame targets, and
     * should be omitted for worker targets.
     * @param url URL of the resource to get content for.
     * @param options Options for the request.
     */
    public suspend fun loadNetworkResource(
        frameId: String? = null,
        url: String,
        options: LoadNetworkResourceOptions,
    ): LoadNetworkResourceReturn {
        val parameter = LoadNetworkResourceParameter(frameId = frameId, url = url, options = options)
        return loadNetworkResource(parameter)
    }

    /**
     * Sets Controls for third-party cookie access
     * Page reload is required before the new cookie behavior will be observed
     */
    public suspend fun setCookieControls(args: SetCookieControlsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Network.setCookieControls", parameter)
    }

    /**
     * Sets Controls for third-party cookie access
     * Page reload is required before the new cookie behavior will be observed
     *
     * @param enableThirdPartyCookieRestriction Whether 3pc restriction is enabled.
     * @param disableThirdPartyCookieMetadata Whether 3pc grace period exception should be enabled; false by default.
     * @param disableThirdPartyCookieHeuristics Whether 3pc heuristics exceptions should be enabled; false by default.
     */
    public suspend fun setCookieControls(
        enableThirdPartyCookieRestriction: Boolean,
        disableThirdPartyCookieMetadata: Boolean,
        disableThirdPartyCookieHeuristics: Boolean,
    ) {
        val parameter = SetCookieControlsParameter(
            enableThirdPartyCookieRestriction = enableThirdPartyCookieRestriction,
            disableThirdPartyCookieMetadata = disableThirdPartyCookieMetadata,
            disableThirdPartyCookieHeuristics = disableThirdPartyCookieHeuristics
        )
        setCookieControls(parameter)
    }

    /**
     * Resource type as it was perceived by the rendering engine.
     */
    @Serializable
    public enum class ResourceType {
        @SerialName("Document")
        DOCUMENT,

        @SerialName("Stylesheet")
        STYLESHEET,

        @SerialName("Image")
        IMAGE,

        @SerialName("Media")
        MEDIA,

        @SerialName("Font")
        FONT,

        @SerialName("Script")
        SCRIPT,

        @SerialName("TextTrack")
        TEXTTRACK,

        @SerialName("XHR")
        XHR,

        @SerialName("Fetch")
        FETCH,

        @SerialName("Prefetch")
        PREFETCH,

        @SerialName("EventSource")
        EVENTSOURCE,

        @SerialName("WebSocket")
        WEBSOCKET,

        @SerialName("Manifest")
        MANIFEST,

        @SerialName("SignedExchange")
        SIGNEDEXCHANGE,

        @SerialName("Ping")
        PING,

        @SerialName("CSPViolationReport")
        CSPVIOLATIONREPORT,

        @SerialName("Preflight")
        PREFLIGHT,

        @SerialName("FedCM")
        FEDCM,

        @SerialName("Other")
        OTHER,
    }

    /**
     * Network level fetch failure reason.
     */
    @Serializable
    public enum class ErrorReason {
        @SerialName("Failed")
        FAILED,

        @SerialName("Aborted")
        ABORTED,

        @SerialName("TimedOut")
        TIMEDOUT,

        @SerialName("AccessDenied")
        ACCESSDENIED,

        @SerialName("ConnectionClosed")
        CONNECTIONCLOSED,

        @SerialName("ConnectionReset")
        CONNECTIONRESET,

        @SerialName("ConnectionRefused")
        CONNECTIONREFUSED,

        @SerialName("ConnectionAborted")
        CONNECTIONABORTED,

        @SerialName("ConnectionFailed")
        CONNECTIONFAILED,

        @SerialName("NameNotResolved")
        NAMENOTRESOLVED,

        @SerialName("InternetDisconnected")
        INTERNETDISCONNECTED,

        @SerialName("AddressUnreachable")
        ADDRESSUNREACHABLE,

        @SerialName("BlockedByClient")
        BLOCKEDBYCLIENT,

        @SerialName("BlockedByResponse")
        BLOCKEDBYRESPONSE,
    }

    /**
     * The underlying connection technology that the browser is supposedly using.
     */
    @Serializable
    public enum class ConnectionType {
        @SerialName("none")
        NONE,

        @SerialName("cellular2g")
        CELLULAR2G,

        @SerialName("cellular3g")
        CELLULAR3G,

        @SerialName("cellular4g")
        CELLULAR4G,

        @SerialName("bluetooth")
        BLUETOOTH,

        @SerialName("ethernet")
        ETHERNET,

        @SerialName("wifi")
        WIFI,

        @SerialName("wimax")
        WIMAX,

        @SerialName("other")
        OTHER,
    }

    /**
     * Represents the cookie's 'SameSite' status:
     * https://tools.ietf.org/html/draft-west-first-party-cookies
     */
    @Serializable
    public enum class CookieSameSite {
        @SerialName("Strict")
        STRICT,

        @SerialName("Lax")
        LAX,

        @SerialName("None")
        NONE,
    }

    /**
     * Represents the cookie's 'Priority' status:
     * https://tools.ietf.org/html/draft-west-cookie-priority-00
     */
    @Serializable
    public enum class CookiePriority {
        @SerialName("Low")
        LOW,

        @SerialName("Medium")
        MEDIUM,

        @SerialName("High")
        HIGH,
    }

    /**
     * Represents the source scheme of the origin that originally set the cookie.
     * A value of "Unset" allows protocol clients to emulate legacy cookie scope for the scheme.
     * This is a temporary ability and it will be removed in the future.
     */
    @Serializable
    public enum class CookieSourceScheme {
        @SerialName("Unset")
        UNSET,

        @SerialName("NonSecure")
        NONSECURE,

        @SerialName("Secure")
        SECURE,
    }

    /**
     * Timing information for the request.
     */
    @Serializable
    public data class ResourceTiming(
        /**
         * Timing's requestTime is a baseline in seconds, while the other numbers are ticks in
         * milliseconds relatively to this requestTime.
         */
        public val requestTime: Double,
        /**
         * Started resolving proxy.
         */
        public val proxyStart: Double,
        /**
         * Finished resolving proxy.
         */
        public val proxyEnd: Double,
        /**
         * Started DNS address resolve.
         */
        public val dnsStart: Double,
        /**
         * Finished DNS address resolve.
         */
        public val dnsEnd: Double,
        /**
         * Started connecting to the remote host.
         */
        public val connectStart: Double,
        /**
         * Connected to the remote host.
         */
        public val connectEnd: Double,
        /**
         * Started SSL handshake.
         */
        public val sslStart: Double,
        /**
         * Finished SSL handshake.
         */
        public val sslEnd: Double,
        /**
         * Started running ServiceWorker.
         */
        public val workerStart: Double,
        /**
         * Finished Starting ServiceWorker.
         */
        public val workerReady: Double,
        /**
         * Started fetch event.
         */
        public val workerFetchStart: Double,
        /**
         * Settled fetch event respondWith promise.
         */
        public val workerRespondWithSettled: Double,
        /**
         * Started ServiceWorker static routing source evaluation.
         */
        public val workerRouterEvaluationStart: Double? = null,
        /**
         * Started cache lookup when the source was evaluated to `cache`.
         */
        public val workerCacheLookupStart: Double? = null,
        /**
         * Started sending request.
         */
        public val sendStart: Double,
        /**
         * Finished sending request.
         */
        public val sendEnd: Double,
        /**
         * Time the server started pushing request.
         */
        public val pushStart: Double,
        /**
         * Time the server finished pushing request.
         */
        public val pushEnd: Double,
        /**
         * Started receiving response headers.
         */
        public val receiveHeadersStart: Double,
        /**
         * Finished receiving response headers.
         */
        public val receiveHeadersEnd: Double,
    )

    /**
     * Loading priority of a resource request.
     */
    @Serializable
    public enum class ResourcePriority {
        @SerialName("VeryLow")
        VERYLOW,

        @SerialName("Low")
        LOW,

        @SerialName("Medium")
        MEDIUM,

        @SerialName("High")
        HIGH,

        @SerialName("VeryHigh")
        VERYHIGH,
    }

    /**
     * Post data entry for HTTP request
     */
    @Serializable
    public data class PostDataEntry(
        public val bytes: String? = null,
    )

    /**
     * HTTP request data.
     */
    @Serializable
    public data class Request(
        /**
         * Request URL (without fragment).
         */
        public val url: String,
        /**
         * Fragment of the requested URL starting with hash, if present.
         */
        public val urlFragment: String? = null,
        /**
         * HTTP request method.
         */
        public val method: String,
        /**
         * HTTP request headers.
         */
        public val headers: Map<String, JsonElement>,
        /**
         * HTTP POST request data.
         * Use postDataEntries instead.
         */
        public val postData: String? = null,
        /**
         * True when the request has POST data. Note that postData might still be omitted when this flag is true when the data is too long.
         */
        public val hasPostData: Boolean? = null,
        /**
         * Request body elements (post data broken into individual entries).
         */
        public val postDataEntries: List<PostDataEntry>? = null,
        /**
         * The mixed content type of the request.
         */
        public val mixedContentType: Security.MixedContentType? = null,
        /**
         * Priority of the resource request at the time request is sent.
         */
        public val initialPriority: ResourcePriority,
        /**
         * The referrer policy of the request, as defined in https://www.w3.org/TR/referrer-policy/
         */
        public val referrerPolicy: String,
        /**
         * Whether is loaded via link preload.
         */
        public val isLinkPreload: Boolean? = null,
        /**
         * Set for requests when the TrustToken API is used. Contains the parameters
         * passed by the developer (e.g. via "fetch") as understood by the backend.
         */
        public val trustTokenParams: TrustTokenParams? = null,
        /**
         * True if this resource request is considered to be the 'same site' as the
         * request corresponding to the main frame.
         */
        public val isSameSite: Boolean? = null,
    )

    /**
     * Details of a signed certificate timestamp (SCT).
     */
    @Serializable
    public data class SignedCertificateTimestamp(
        /**
         * Validation status.
         */
        public val status: String,
        /**
         * Origin.
         */
        public val origin: String,
        /**
         * Log name / description.
         */
        public val logDescription: String,
        /**
         * Log ID.
         */
        public val logId: String,
        /**
         * Issuance date. Unlike TimeSinceEpoch, this contains the number of
         * milliseconds since January 1, 1970, UTC, not the number of seconds.
         */
        public val timestamp: Double,
        /**
         * Hash algorithm.
         */
        public val hashAlgorithm: String,
        /**
         * Signature algorithm.
         */
        public val signatureAlgorithm: String,
        /**
         * Signature data.
         */
        public val signatureData: String,
    )

    /**
     * Security details about a request.
     */
    @Serializable
    public data class SecurityDetails(
        /**
         * Protocol name (e.g. "TLS 1.2" or "QUIC").
         */
        public val protocol: String,
        /**
         * Key Exchange used by the connection, or the empty string if not applicable.
         */
        public val keyExchange: String,
        /**
         * (EC)DH group used by the connection, if applicable.
         */
        public val keyExchangeGroup: String? = null,
        /**
         * Cipher name.
         */
        public val cipher: String,
        /**
         * TLS MAC. Note that AEAD ciphers do not have separate MACs.
         */
        public val mac: String? = null,
        /**
         * Certificate ID value.
         */
        public val certificateId: Int,
        /**
         * Certificate subject name.
         */
        public val subjectName: String,
        /**
         * Subject Alternative Name (SAN) DNS names and IP addresses.
         */
        public val sanList: List<String>,
        /**
         * Name of the issuing CA.
         */
        public val issuer: String,
        /**
         * Certificate valid from date.
         */
        public val validFrom: Double,
        /**
         * Certificate valid to (expiration) date
         */
        public val validTo: Double,
        /**
         * List of signed certificate timestamps (SCTs).
         */
        public val signedCertificateTimestampList: List<SignedCertificateTimestamp>,
        /**
         * Whether the request complied with Certificate Transparency policy
         */
        public val certificateTransparencyCompliance: CertificateTransparencyCompliance,
        /**
         * The signature algorithm used by the server in the TLS server signature,
         * represented as a TLS SignatureScheme code point. Omitted if not
         * applicable or not known.
         */
        public val serverSignatureAlgorithm: Int? = null,
        /**
         * Whether the connection used Encrypted ClientHello
         */
        public val encryptedClientHello: Boolean,
    )

    /**
     * Whether the request complied with Certificate Transparency policy.
     */
    @Serializable
    public enum class CertificateTransparencyCompliance {
        @SerialName("unknown")
        UNKNOWN,

        @SerialName("not-compliant")
        NOT_COMPLIANT,

        @SerialName("compliant")
        COMPLIANT,
    }

    /**
     * The reason why request was blocked.
     */
    @Serializable
    public enum class BlockedReason {
        @SerialName("other")
        OTHER,

        @SerialName("csp")
        CSP,

        @SerialName("mixed-content")
        MIXED_CONTENT,

        @SerialName("origin")
        ORIGIN,

        @SerialName("inspector")
        INSPECTOR,

        @SerialName("integrity")
        INTEGRITY,

        @SerialName("subresource-filter")
        SUBRESOURCE_FILTER,

        @SerialName("content-type")
        CONTENT_TYPE,

        @SerialName("coep-frame-resource-needs-coep-header")
        COEP_FRAME_RESOURCE_NEEDS_COEP_HEADER,

        @SerialName("coop-sandboxed-iframe-cannot-navigate-to-coop-page")
        COOP_SANDBOXED_IFRAME_CANNOT_NAVIGATE_TO_COOP_PAGE,

        @SerialName("corp-not-same-origin")
        CORP_NOT_SAME_ORIGIN,

        @SerialName("corp-not-same-origin-after-defaulted-to-same-origin-by-coep")
        CORP_NOT_SAME_ORIGIN_AFTER_DEFAULTED_TO_SAME_ORIGIN_BY_COEP,

        @SerialName("corp-not-same-origin-after-defaulted-to-same-origin-by-dip")
        CORP_NOT_SAME_ORIGIN_AFTER_DEFAULTED_TO_SAME_ORIGIN_BY_DIP,

        @SerialName("corp-not-same-origin-after-defaulted-to-same-origin-by-coep-and-dip")
        CORP_NOT_SAME_ORIGIN_AFTER_DEFAULTED_TO_SAME_ORIGIN_BY_COEP_AND_DIP,

        @SerialName("corp-not-same-site")
        CORP_NOT_SAME_SITE,

        @SerialName("sri-message-signature-mismatch")
        SRI_MESSAGE_SIGNATURE_MISMATCH,
    }

    /**
     * The reason why request was blocked.
     */
    @Serializable
    public enum class CorsError {
        @SerialName("DisallowedByMode")
        DISALLOWEDBYMODE,

        @SerialName("InvalidResponse")
        INVALIDRESPONSE,

        @SerialName("WildcardOriginNotAllowed")
        WILDCARDORIGINNOTALLOWED,

        @SerialName("MissingAllowOriginHeader")
        MISSINGALLOWORIGINHEADER,

        @SerialName("MultipleAllowOriginValues")
        MULTIPLEALLOWORIGINVALUES,

        @SerialName("InvalidAllowOriginValue")
        INVALIDALLOWORIGINVALUE,

        @SerialName("AllowOriginMismatch")
        ALLOWORIGINMISMATCH,

        @SerialName("InvalidAllowCredentials")
        INVALIDALLOWCREDENTIALS,

        @SerialName("CorsDisabledScheme")
        CORSDISABLEDSCHEME,

        @SerialName("PreflightInvalidStatus")
        PREFLIGHTINVALIDSTATUS,

        @SerialName("PreflightDisallowedRedirect")
        PREFLIGHTDISALLOWEDREDIRECT,

        @SerialName("PreflightWildcardOriginNotAllowed")
        PREFLIGHTWILDCARDORIGINNOTALLOWED,

        @SerialName("PreflightMissingAllowOriginHeader")
        PREFLIGHTMISSINGALLOWORIGINHEADER,

        @SerialName("PreflightMultipleAllowOriginValues")
        PREFLIGHTMULTIPLEALLOWORIGINVALUES,

        @SerialName("PreflightInvalidAllowOriginValue")
        PREFLIGHTINVALIDALLOWORIGINVALUE,

        @SerialName("PreflightAllowOriginMismatch")
        PREFLIGHTALLOWORIGINMISMATCH,

        @SerialName("PreflightInvalidAllowCredentials")
        PREFLIGHTINVALIDALLOWCREDENTIALS,

        @SerialName("PreflightMissingAllowExternal")
        PREFLIGHTMISSINGALLOWEXTERNAL,

        @SerialName("PreflightInvalidAllowExternal")
        PREFLIGHTINVALIDALLOWEXTERNAL,

        @SerialName("PreflightMissingAllowPrivateNetwork")
        PREFLIGHTMISSINGALLOWPRIVATENETWORK,

        @SerialName("PreflightInvalidAllowPrivateNetwork")
        PREFLIGHTINVALIDALLOWPRIVATENETWORK,

        @SerialName("InvalidAllowMethodsPreflightResponse")
        INVALIDALLOWMETHODSPREFLIGHTRESPONSE,

        @SerialName("InvalidAllowHeadersPreflightResponse")
        INVALIDALLOWHEADERSPREFLIGHTRESPONSE,

        @SerialName("MethodDisallowedByPreflightResponse")
        METHODDISALLOWEDBYPREFLIGHTRESPONSE,

        @SerialName("HeaderDisallowedByPreflightResponse")
        HEADERDISALLOWEDBYPREFLIGHTRESPONSE,

        @SerialName("RedirectContainsCredentials")
        REDIRECTCONTAINSCREDENTIALS,

        @SerialName("InsecurePrivateNetwork")
        INSECUREPRIVATENETWORK,

        @SerialName("InvalidPrivateNetworkAccess")
        INVALIDPRIVATENETWORKACCESS,

        @SerialName("UnexpectedPrivateNetworkAccess")
        UNEXPECTEDPRIVATENETWORKACCESS,

        @SerialName("NoCorsRedirectModeNotFollow")
        NOCORSREDIRECTMODENOTFOLLOW,

        @SerialName("PreflightMissingPrivateNetworkAccessId")
        PREFLIGHTMISSINGPRIVATENETWORKACCESSID,

        @SerialName("PreflightMissingPrivateNetworkAccessName")
        PREFLIGHTMISSINGPRIVATENETWORKACCESSNAME,

        @SerialName("PrivateNetworkAccessPermissionUnavailable")
        PRIVATENETWORKACCESSPERMISSIONUNAVAILABLE,

        @SerialName("PrivateNetworkAccessPermissionDenied")
        PRIVATENETWORKACCESSPERMISSIONDENIED,

        @SerialName("LocalNetworkAccessPermissionDenied")
        LOCALNETWORKACCESSPERMISSIONDENIED,
    }

    @Serializable
    public data class CorsErrorStatus(
        public val corsError: CorsError,
        public val failedParameter: String,
    )

    /**
     * Source of serviceworker response.
     */
    @Serializable
    public enum class ServiceWorkerResponseSource {
        @SerialName("cache-storage")
        CACHE_STORAGE,

        @SerialName("http-cache")
        HTTP_CACHE,

        @SerialName("fallback-code")
        FALLBACK_CODE,

        @SerialName("network")
        NETWORK,
    }

    /**
     * Determines what type of Trust Token operation is executed and
     * depending on the type, some additional parameters. The values
     * are specified in third_party/blink/renderer/core/fetch/trust_token.idl.
     */
    @Serializable
    public data class TrustTokenParams(
        public val operation: TrustTokenOperationType,
        /**
         * Only set for "token-redemption" operation and determine whether
         * to request a fresh SRR or use a still valid cached SRR.
         */
        public val refreshPolicy: String,
        /**
         * Origins of issuers from whom to request tokens or redemption
         * records.
         */
        public val issuers: List<String>? = null,
    )

    @Serializable
    public enum class TrustTokenOperationType {
        @SerialName("Issuance")
        ISSUANCE,

        @SerialName("Redemption")
        REDEMPTION,

        @SerialName("Signing")
        SIGNING,
    }

    /**
     * The reason why Chrome uses a specific transport protocol for HTTP semantics.
     */
    @Serializable
    public enum class AlternateProtocolUsage {
        @SerialName("alternativeJobWonWithoutRace")
        ALTERNATIVEJOBWONWITHOUTRACE,

        @SerialName("alternativeJobWonRace")
        ALTERNATIVEJOBWONRACE,

        @SerialName("mainJobWonRace")
        MAINJOBWONRACE,

        @SerialName("mappingMissing")
        MAPPINGMISSING,

        @SerialName("broken")
        BROKEN,

        @SerialName("dnsAlpnH3JobWonWithoutRace")
        DNSALPNH3JOBWONWITHOUTRACE,

        @SerialName("dnsAlpnH3JobWonRace")
        DNSALPNH3JOBWONRACE,

        @SerialName("unspecifiedReason")
        UNSPECIFIEDREASON,
    }

    /**
     * Source of service worker router.
     */
    @Serializable
    public enum class ServiceWorkerRouterSource {
        @SerialName("network")
        NETWORK,

        @SerialName("cache")
        CACHE,

        @SerialName("fetch-event")
        FETCH_EVENT,

        @SerialName("race-network-and-fetch-handler")
        RACE_NETWORK_AND_FETCH_HANDLER,

        @SerialName("race-network-and-cache")
        RACE_NETWORK_AND_CACHE,
    }

    @Serializable
    public data class ServiceWorkerRouterInfo(
        /**
         * ID of the rule matched. If there is a matched rule, this field will
         * be set, otherwiser no value will be set.
         */
        public val ruleIdMatched: Int? = null,
        /**
         * The router source of the matched rule. If there is a matched rule, this
         * field will be set, otherwise no value will be set.
         */
        public val matchedSourceType: ServiceWorkerRouterSource? = null,
        /**
         * The actual router source used.
         */
        public val actualSourceType: ServiceWorkerRouterSource? = null,
    )

    /**
     * HTTP response data.
     */
    @Serializable
    public data class Response(
        /**
         * Response URL. This URL can be different from CachedResource.url in case of redirect.
         */
        public val url: String,
        /**
         * HTTP response status code.
         */
        public val status: Int,
        /**
         * HTTP response status text.
         */
        public val statusText: String,
        /**
         * HTTP response headers.
         */
        public val headers: Map<String, JsonElement>,
        /**
         * HTTP response headers text. This has been replaced by the headers in Network.responseReceivedExtraInfo.
         */
        public val headersText: String? = null,
        /**
         * Resource mimeType as determined by the browser.
         */
        public val mimeType: String,
        /**
         * Resource charset as determined by the browser (if applicable).
         */
        public val charset: String,
        /**
         * Refined HTTP request headers that were actually transmitted over the network.
         */
        public val requestHeaders: Map<String, JsonElement>? = null,
        /**
         * HTTP request headers text. This has been replaced by the headers in Network.requestWillBeSentExtraInfo.
         */
        public val requestHeadersText: String? = null,
        /**
         * Specifies whether physical connection was actually reused for this request.
         */
        public val connectionReused: Boolean,
        /**
         * Physical connection id that was actually used for this request.
         */
        public val connectionId: Double,
        /**
         * Remote IP address.
         */
        public val remoteIPAddress: String? = null,
        /**
         * Remote port.
         */
        public val remotePort: Int? = null,
        /**
         * Specifies that the request was served from the disk cache.
         */
        public val fromDiskCache: Boolean? = null,
        /**
         * Specifies that the request was served from the ServiceWorker.
         */
        public val fromServiceWorker: Boolean? = null,
        /**
         * Specifies that the request was served from the prefetch cache.
         */
        public val fromPrefetchCache: Boolean? = null,
        /**
         * Specifies that the request was served from the prefetch cache.
         */
        public val fromEarlyHints: Boolean? = null,
        /**
         * Information about how ServiceWorker Static Router API was used. If this
         * field is set with `matchedSourceType` field, a matching rule is found.
         * If this field is set without `matchedSource`, no matching rule is found.
         * Otherwise, the API is not used.
         */
        public val serviceWorkerRouterInfo: ServiceWorkerRouterInfo? = null,
        /**
         * Total number of bytes received for this request so far.
         */
        public val encodedDataLength: Double,
        /**
         * Timing information for the given request.
         */
        public val timing: ResourceTiming? = null,
        /**
         * Response source of response from ServiceWorker.
         */
        public val serviceWorkerResponseSource: ServiceWorkerResponseSource? = null,
        /**
         * The time at which the returned response was generated.
         */
        public val responseTime: Double? = null,
        /**
         * Cache Storage Cache Name.
         */
        public val cacheStorageCacheName: String? = null,
        /**
         * Protocol used to fetch this request.
         */
        public val protocol: String? = null,
        /**
         * The reason why Chrome uses a specific transport protocol for HTTP semantics.
         */
        public val alternateProtocolUsage: AlternateProtocolUsage? = null,
        /**
         * Security state of the request resource.
         */
        public val securityState: Security.SecurityState,
        /**
         * Security details for the request.
         */
        public val securityDetails: SecurityDetails? = null,
    )

    /**
     * WebSocket request data.
     */
    @Serializable
    public data class WebSocketRequest(
        /**
         * HTTP request headers.
         */
        public val headers: Map<String, JsonElement>,
    )

    /**
     * WebSocket response data.
     */
    @Serializable
    public data class WebSocketResponse(
        /**
         * HTTP response status code.
         */
        public val status: Int,
        /**
         * HTTP response status text.
         */
        public val statusText: String,
        /**
         * HTTP response headers.
         */
        public val headers: Map<String, JsonElement>,
        /**
         * HTTP response headers text.
         */
        public val headersText: String? = null,
        /**
         * HTTP request headers.
         */
        public val requestHeaders: Map<String, JsonElement>? = null,
        /**
         * HTTP request headers text.
         */
        public val requestHeadersText: String? = null,
    )

    /**
     * WebSocket message data. This represents an entire WebSocket message, not just a fragmented frame as the name suggests.
     */
    @Serializable
    public data class WebSocketFrame(
        /**
         * WebSocket message opcode.
         */
        public val opcode: Double,
        /**
         * WebSocket message mask.
         */
        public val mask: Boolean,
        /**
         * WebSocket message payload data.
         * If the opcode is 1, this is a text message and payloadData is a UTF-8 string.
         * If the opcode isn't 1, then payloadData is a base64 encoded string representing binary data.
         */
        public val payloadData: String,
    )

    /**
     * Information about the cached resource.
     */
    @Serializable
    public data class CachedResource(
        /**
         * Resource URL. This is the url of the original network request.
         */
        public val url: String,
        /**
         * Type of this resource.
         */
        public val type: ResourceType,
        /**
         * Cached response data.
         */
        public val response: Response? = null,
        /**
         * Cached response body size.
         */
        public val bodySize: Double,
    )

    /**
     * Information about the request initiator.
     */
    @Serializable
    public data class Initiator(
        /**
         * Type of this initiator.
         */
        public val type: String,
        /**
         * Initiator JavaScript stack trace, set for Script only.
         * Requires the Debugger domain to be enabled.
         */
        public val stack: Runtime.StackTrace? = null,
        /**
         * Initiator URL, set for Parser type or for Script type (when script is importing module) or for SignedExchange type.
         */
        public val url: String? = null,
        /**
         * Initiator line number, set for Parser type or for Script type (when script is importing
         * module) (0-based).
         */
        public val lineNumber: Double? = null,
        /**
         * Initiator column number, set for Parser type or for Script type (when script is importing
         * module) (0-based).
         */
        public val columnNumber: Double? = null,
        /**
         * Set if another request triggered this request (e.g. preflight).
         */
        public val requestId: String? = null,
    )

    /**
     * cookiePartitionKey object
     * The representation of the components of the key that are created by the cookiePartitionKey class contained in net/cookies/cookie_partition_key.h.
     */
    @Serializable
    public data class CookiePartitionKey(
        /**
         * The site of the top-level URL the browser was visiting at the start
         * of the request to the endpoint that set the cookie.
         */
        public val topLevelSite: String,
        /**
         * Indicates if the cookie has any ancestors that are cross-site to the topLevelSite.
         */
        public val hasCrossSiteAncestor: Boolean,
    )

    /**
     * Cookie object
     */
    @Serializable
    public data class Cookie(
        /**
         * Cookie name.
         */
        public val name: String,
        /**
         * Cookie value.
         */
        public val `value`: String,
        /**
         * Cookie domain.
         */
        public val domain: String,
        /**
         * Cookie path.
         */
        public val path: String,
        /**
         * Cookie expiration date as the number of seconds since the UNIX epoch.
         */
        public val expires: Double,
        /**
         * Cookie size.
         */
        public val size: Int,
        /**
         * True if cookie is http-only.
         */
        public val httpOnly: Boolean,
        /**
         * True if cookie is secure.
         */
        public val secure: Boolean,
        /**
         * True in case of session cookie.
         */
        public val session: Boolean,
        /**
         * Cookie SameSite type.
         */
        public val sameSite: CookieSameSite? = null,
        /**
         * Cookie Priority
         */
        public val priority: CookiePriority,
        /**
         * True if cookie is SameParty.
         */
        public val sameParty: Boolean,
        /**
         * Cookie source scheme type.
         */
        public val sourceScheme: CookieSourceScheme,
        /**
         * Cookie source port. Valid values are {-1, [1, 65535]}, -1 indicates an unspecified port.
         * An unspecified port value allows protocol clients to emulate legacy cookie scope for the port.
         * This is a temporary ability and it will be removed in the future.
         */
        public val sourcePort: Int,
        /**
         * Cookie partition key.
         */
        public val partitionKey: CookiePartitionKey? = null,
        /**
         * True if cookie partition key is opaque.
         */
        public val partitionKeyOpaque: Boolean? = null,
    )

    /**
     * Types of reasons why a cookie may not be stored from a response.
     */
    @Serializable
    public enum class SetCookieBlockedReason {
        @SerialName("SecureOnly")
        SECUREONLY,

        @SerialName("SameSiteStrict")
        SAMESITESTRICT,

        @SerialName("SameSiteLax")
        SAMESITELAX,

        @SerialName("SameSiteUnspecifiedTreatedAsLax")
        SAMESITEUNSPECIFIEDTREATEDASLAX,

        @SerialName("SameSiteNoneInsecure")
        SAMESITENONEINSECURE,

        @SerialName("UserPreferences")
        USERPREFERENCES,

        @SerialName("ThirdPartyPhaseout")
        THIRDPARTYPHASEOUT,

        @SerialName("ThirdPartyBlockedInFirstPartySet")
        THIRDPARTYBLOCKEDINFIRSTPARTYSET,

        @SerialName("SyntaxError")
        SYNTAXERROR,

        @SerialName("SchemeNotSupported")
        SCHEMENOTSUPPORTED,

        @SerialName("OverwriteSecure")
        OVERWRITESECURE,

        @SerialName("InvalidDomain")
        INVALIDDOMAIN,

        @SerialName("InvalidPrefix")
        INVALIDPREFIX,

        @SerialName("UnknownError")
        UNKNOWNERROR,

        @SerialName("SchemefulSameSiteStrict")
        SCHEMEFULSAMESITESTRICT,

        @SerialName("SchemefulSameSiteLax")
        SCHEMEFULSAMESITELAX,

        @SerialName("SchemefulSameSiteUnspecifiedTreatedAsLax")
        SCHEMEFULSAMESITEUNSPECIFIEDTREATEDASLAX,

        @SerialName("SamePartyFromCrossPartyContext")
        SAMEPARTYFROMCROSSPARTYCONTEXT,

        @SerialName("SamePartyConflictsWithOtherAttributes")
        SAMEPARTYCONFLICTSWITHOTHERATTRIBUTES,

        @SerialName("NameValuePairExceedsMaxSize")
        NAMEVALUEPAIREXCEEDSMAXSIZE,

        @SerialName("DisallowedCharacter")
        DISALLOWEDCHARACTER,

        @SerialName("NoCookieContent")
        NOCOOKIECONTENT,
    }

    /**
     * Types of reasons why a cookie may not be sent with a request.
     */
    @Serializable
    public enum class CookieBlockedReason {
        @SerialName("SecureOnly")
        SECUREONLY,

        @SerialName("NotOnPath")
        NOTONPATH,

        @SerialName("DomainMismatch")
        DOMAINMISMATCH,

        @SerialName("SameSiteStrict")
        SAMESITESTRICT,

        @SerialName("SameSiteLax")
        SAMESITELAX,

        @SerialName("SameSiteUnspecifiedTreatedAsLax")
        SAMESITEUNSPECIFIEDTREATEDASLAX,

        @SerialName("SameSiteNoneInsecure")
        SAMESITENONEINSECURE,

        @SerialName("UserPreferences")
        USERPREFERENCES,

        @SerialName("ThirdPartyPhaseout")
        THIRDPARTYPHASEOUT,

        @SerialName("ThirdPartyBlockedInFirstPartySet")
        THIRDPARTYBLOCKEDINFIRSTPARTYSET,

        @SerialName("UnknownError")
        UNKNOWNERROR,

        @SerialName("SchemefulSameSiteStrict")
        SCHEMEFULSAMESITESTRICT,

        @SerialName("SchemefulSameSiteLax")
        SCHEMEFULSAMESITELAX,

        @SerialName("SchemefulSameSiteUnspecifiedTreatedAsLax")
        SCHEMEFULSAMESITEUNSPECIFIEDTREATEDASLAX,

        @SerialName("SamePartyFromCrossPartyContext")
        SAMEPARTYFROMCROSSPARTYCONTEXT,

        @SerialName("NameValuePairExceedsMaxSize")
        NAMEVALUEPAIREXCEEDSMAXSIZE,

        @SerialName("PortMismatch")
        PORTMISMATCH,

        @SerialName("SchemeMismatch")
        SCHEMEMISMATCH,

        @SerialName("AnonymousContext")
        ANONYMOUSCONTEXT,
    }

    /**
     * Types of reasons why a cookie should have been blocked by 3PCD but is exempted for the request.
     */
    @Serializable
    public enum class CookieExemptionReason {
        @SerialName("None")
        NONE,

        @SerialName("UserSetting")
        USERSETTING,

        @SerialName("TPCDMetadata")
        TPCDMETADATA,

        @SerialName("TPCDDeprecationTrial")
        TPCDDEPRECATIONTRIAL,

        @SerialName("TopLevelTPCDDeprecationTrial")
        TOPLEVELTPCDDEPRECATIONTRIAL,

        @SerialName("TPCDHeuristics")
        TPCDHEURISTICS,

        @SerialName("EnterprisePolicy")
        ENTERPRISEPOLICY,

        @SerialName("StorageAccess")
        STORAGEACCESS,

        @SerialName("TopLevelStorageAccess")
        TOPLEVELSTORAGEACCESS,

        @SerialName("Scheme")
        SCHEME,

        @SerialName("SameSiteNoneCookiesInSandbox")
        SAMESITENONECOOKIESINSANDBOX,
    }

    /**
     * A cookie which was not stored from a response with the corresponding reason.
     */
    @Serializable
    public data class BlockedSetCookieWithReason(
        /**
         * The reason(s) this cookie was blocked.
         */
        public val blockedReasons: List<SetCookieBlockedReason>,
        /**
         * The string representing this individual cookie as it would appear in the header.
         * This is not the entire "cookie" or "set-cookie" header which could have multiple cookies.
         */
        public val cookieLine: String,
        /**
         * The cookie object which represents the cookie which was not stored. It is optional because
         * sometimes complete cookie information is not available, such as in the case of parsing
         * errors.
         */
        public val cookie: Cookie? = null,
    )

    /**
     * A cookie should have been blocked by 3PCD but is exempted and stored from a response with the
     * corresponding reason. A cookie could only have at most one exemption reason.
     */
    @Serializable
    public data class ExemptedSetCookieWithReason(
        /**
         * The reason the cookie was exempted.
         */
        public val exemptionReason: CookieExemptionReason,
        /**
         * The string representing this individual cookie as it would appear in the header.
         */
        public val cookieLine: String,
        /**
         * The cookie object representing the cookie.
         */
        public val cookie: Cookie,
    )

    /**
     * A cookie associated with the request which may or may not be sent with it.
     * Includes the cookies itself and reasons for blocking or exemption.
     */
    @Serializable
    public data class AssociatedCookie(
        /**
         * The cookie object representing the cookie which was not sent.
         */
        public val cookie: Cookie,
        /**
         * The reason(s) the cookie was blocked. If empty means the cookie is included.
         */
        public val blockedReasons: List<CookieBlockedReason>,
        /**
         * The reason the cookie should have been blocked by 3PCD but is exempted. A cookie could
         * only have at most one exemption reason.
         */
        public val exemptionReason: CookieExemptionReason? = null,
    )

    /**
     * Cookie parameter object
     */
    @Serializable
    public data class CookieParam(
        /**
         * Cookie name.
         */
        public val name: String,
        /**
         * Cookie value.
         */
        public val `value`: String,
        /**
         * The request-URI to associate with the setting of the cookie. This value can affect the
         * default domain, path, source port, and source scheme values of the created cookie.
         */
        public val url: String? = null,
        /**
         * Cookie domain.
         */
        public val domain: String? = null,
        /**
         * Cookie path.
         */
        public val path: String? = null,
        /**
         * True if cookie is secure.
         */
        public val secure: Boolean? = null,
        /**
         * True if cookie is http-only.
         */
        public val httpOnly: Boolean? = null,
        /**
         * Cookie SameSite type.
         */
        public val sameSite: CookieSameSite? = null,
        /**
         * Cookie expiration date, session cookie if not set
         */
        public val expires: Double? = null,
        /**
         * Cookie Priority.
         */
        public val priority: CookiePriority? = null,
        /**
         * True if cookie is SameParty.
         */
        public val sameParty: Boolean? = null,
        /**
         * Cookie source scheme type.
         */
        public val sourceScheme: CookieSourceScheme? = null,
        /**
         * Cookie source port. Valid values are {-1, [1, 65535]}, -1 indicates an unspecified port.
         * An unspecified port value allows protocol clients to emulate legacy cookie scope for the port.
         * This is a temporary ability and it will be removed in the future.
         */
        public val sourcePort: Int? = null,
        /**
         * Cookie partition key. If not set, the cookie will be set as not partitioned.
         */
        public val partitionKey: CookiePartitionKey? = null,
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
     * Stages of the interception to begin intercepting. Request will intercept before the request is
     * sent. Response will intercept after the response is received.
     */
    @Serializable
    public enum class InterceptionStage {
        @SerialName("Request")
        REQUEST,

        @SerialName("HeadersReceived")
        HEADERSRECEIVED,
    }

    /**
     * Request pattern for interception.
     */
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
        public val resourceType: ResourceType? = null,
        /**
         * Stage at which to begin intercepting requests. Default is Request.
         */
        public val interceptionStage: InterceptionStage? = null,
    )

    /**
     * Information about a signed exchange signature.
     * https://wicg.github.io/webpackage/draft-yasskin-httpbis-origin-signed-exchanges-impl.html#rfc.section.3.1
     */
    @Serializable
    public data class SignedExchangeSignature(
        /**
         * Signed exchange signature label.
         */
        public val label: String,
        /**
         * The hex string of signed exchange signature.
         */
        public val signature: String,
        /**
         * Signed exchange signature integrity.
         */
        public val integrity: String,
        /**
         * Signed exchange signature cert Url.
         */
        public val certUrl: String? = null,
        /**
         * The hex string of signed exchange signature cert sha256.
         */
        public val certSha256: String? = null,
        /**
         * Signed exchange signature validity Url.
         */
        public val validityUrl: String,
        /**
         * Signed exchange signature date.
         */
        public val date: Int,
        /**
         * Signed exchange signature expires.
         */
        public val expires: Int,
        /**
         * The encoded certificates.
         */
        public val certificates: List<String>? = null,
    )

    /**
     * Information about a signed exchange header.
     * https://wicg.github.io/webpackage/draft-yasskin-httpbis-origin-signed-exchanges-impl.html#cbor-representation
     */
    @Serializable
    public data class SignedExchangeHeader(
        /**
         * Signed exchange request URL.
         */
        public val requestUrl: String,
        /**
         * Signed exchange response code.
         */
        public val responseCode: Int,
        /**
         * Signed exchange response headers.
         */
        public val responseHeaders: Map<String, JsonElement>,
        /**
         * Signed exchange response signature.
         */
        public val signatures: List<SignedExchangeSignature>,
        /**
         * Signed exchange header integrity hash in the form of `sha256-<base64-hash-value>`.
         */
        public val headerIntegrity: String,
    )

    /**
     * Field type for a signed exchange related error.
     */
    @Serializable
    public enum class SignedExchangeErrorField {
        @SerialName("signatureSig")
        SIGNATURESIG,

        @SerialName("signatureIntegrity")
        SIGNATUREINTEGRITY,

        @SerialName("signatureCertUrl")
        SIGNATURECERTURL,

        @SerialName("signatureCertSha256")
        SIGNATURECERTSHA256,

        @SerialName("signatureValidityUrl")
        SIGNATUREVALIDITYURL,

        @SerialName("signatureTimestamps")
        SIGNATURETIMESTAMPS,
    }

    /**
     * Information about a signed exchange response.
     */
    @Serializable
    public data class SignedExchangeError(
        /**
         * Error message.
         */
        public val message: String,
        /**
         * The index of the signature which caused the error.
         */
        public val signatureIndex: Int? = null,
        /**
         * The field which caused the error.
         */
        public val errorField: SignedExchangeErrorField? = null,
    )

    /**
     * Information about a signed exchange response.
     */
    @Serializable
    public data class SignedExchangeInfo(
        /**
         * The outer response of signed HTTP exchange which was received from network.
         */
        public val outerResponse: Response,
        /**
         * Whether network response for the signed exchange was accompanied by
         * extra headers.
         */
        public val hasExtraInfo: Boolean,
        /**
         * Information about the signed exchange header.
         */
        public val `header`: SignedExchangeHeader? = null,
        /**
         * Security details for the signed exchange header.
         */
        public val securityDetails: SecurityDetails? = null,
        /**
         * Errors occurred while handling the signed exchange.
         */
        public val errors: List<SignedExchangeError>? = null,
    )

    /**
     * List of content encodings supported by the backend.
     */
    @Serializable
    public enum class ContentEncoding {
        @SerialName("deflate")
        DEFLATE,

        @SerialName("gzip")
        GZIP,

        @SerialName("br")
        BR,

        @SerialName("zstd")
        ZSTD,
    }

    @Serializable
    public enum class DirectSocketDnsQueryType {
        @SerialName("ipv4")
        IPV4,

        @SerialName("ipv6")
        IPV6,
    }

    @Serializable
    public data class DirectTCPSocketOptions(
        /**
         * TCP_NODELAY option
         */
        public val noDelay: Boolean,
        /**
         * Expected to be unsigned integer.
         */
        public val keepAliveDelay: Double? = null,
        /**
         * Expected to be unsigned integer.
         */
        public val sendBufferSize: Double? = null,
        /**
         * Expected to be unsigned integer.
         */
        public val receiveBufferSize: Double? = null,
        public val dnsQueryType: DirectSocketDnsQueryType? = null,
    )

    @Serializable
    public data class DirectUDPSocketOptions(
        public val remoteAddr: String? = null,
        /**
         * Unsigned int 16.
         */
        public val remotePort: Int? = null,
        public val localAddr: String? = null,
        /**
         * Unsigned int 16.
         */
        public val localPort: Int? = null,
        public val dnsQueryType: DirectSocketDnsQueryType? = null,
        /**
         * Expected to be unsigned integer.
         */
        public val sendBufferSize: Double? = null,
        /**
         * Expected to be unsigned integer.
         */
        public val receiveBufferSize: Double? = null,
    )

    @Serializable
    public data class DirectUDPMessage(
        public val `data`: String,
        /**
         * Null for connected mode.
         */
        public val remoteAddr: String? = null,
        /**
         * Null for connected mode.
         * Expected to be unsigned integer.
         */
        public val remotePort: Int? = null,
    )

    @Serializable
    public enum class PrivateNetworkRequestPolicy {
        @SerialName("Allow")
        ALLOW,

        @SerialName("BlockFromInsecureToMorePrivate")
        BLOCKFROMINSECURETOMOREPRIVATE,

        @SerialName("WarnFromInsecureToMorePrivate")
        WARNFROMINSECURETOMOREPRIVATE,

        @SerialName("PreflightBlock")
        PREFLIGHTBLOCK,

        @SerialName("PreflightWarn")
        PREFLIGHTWARN,

        @SerialName("PermissionBlock")
        PERMISSIONBLOCK,

        @SerialName("PermissionWarn")
        PERMISSIONWARN,
    }

    @Serializable
    public enum class IPAddressSpace {
        @SerialName("Loopback")
        LOOPBACK,

        @SerialName("Private")
        PRIVATE,

        @SerialName("Public")
        PUBLIC,

        @SerialName("Unknown")
        UNKNOWN,
    }

    @Serializable
    public data class ConnectTiming(
        /**
         * Timing's requestTime is a baseline in seconds, while the other numbers are ticks in
         * milliseconds relatively to this requestTime. Matches ResourceTiming's requestTime for
         * the same request (but not for redirected requests).
         */
        public val requestTime: Double,
    )

    @Serializable
    public data class ClientSecurityState(
        public val initiatorIsSecureContext: Boolean,
        public val initiatorIPAddressSpace: IPAddressSpace,
        public val privateNetworkRequestPolicy: PrivateNetworkRequestPolicy,
    )

    @Serializable
    public enum class CrossOriginOpenerPolicyValue {
        @SerialName("SameOrigin")
        SAMEORIGIN,

        @SerialName("SameOriginAllowPopups")
        SAMEORIGINALLOWPOPUPS,

        @SerialName("RestrictProperties")
        RESTRICTPROPERTIES,

        @SerialName("UnsafeNone")
        UNSAFENONE,

        @SerialName("SameOriginPlusCoep")
        SAMEORIGINPLUSCOEP,

        @SerialName("RestrictPropertiesPlusCoep")
        RESTRICTPROPERTIESPLUSCOEP,

        @SerialName("NoopenerAllowPopups")
        NOOPENERALLOWPOPUPS,
    }

    @Serializable
    public data class CrossOriginOpenerPolicyStatus(
        public val `value`: CrossOriginOpenerPolicyValue,
        public val reportOnlyValue: CrossOriginOpenerPolicyValue,
        public val reportingEndpoint: String? = null,
        public val reportOnlyReportingEndpoint: String? = null,
    )

    @Serializable
    public enum class CrossOriginEmbedderPolicyValue {
        @SerialName("None")
        NONE,

        @SerialName("Credentialless")
        CREDENTIALLESS,

        @SerialName("RequireCorp")
        REQUIRECORP,
    }

    @Serializable
    public data class CrossOriginEmbedderPolicyStatus(
        public val `value`: CrossOriginEmbedderPolicyValue,
        public val reportOnlyValue: CrossOriginEmbedderPolicyValue,
        public val reportingEndpoint: String? = null,
        public val reportOnlyReportingEndpoint: String? = null,
    )

    @Serializable
    public enum class ContentSecurityPolicySource {
        @SerialName("HTTP")
        HTTP,

        @SerialName("Meta")
        META,
    }

    @Serializable
    public data class ContentSecurityPolicyStatus(
        public val effectiveDirectives: String,
        public val isEnforced: Boolean,
        public val source: ContentSecurityPolicySource,
    )

    @Serializable
    public data class SecurityIsolationStatus(
        public val coop: CrossOriginOpenerPolicyStatus? = null,
        public val coep: CrossOriginEmbedderPolicyStatus? = null,
        public val csp: List<ContentSecurityPolicyStatus>? = null,
    )

    /**
     * The status of a Reporting API report.
     */
    @Serializable
    public enum class ReportStatus {
        @SerialName("Queued")
        QUEUED,

        @SerialName("Pending")
        PENDING,

        @SerialName("MarkedForRemoval")
        MARKEDFORREMOVAL,

        @SerialName("Success")
        SUCCESS,
    }

    /**
     * An object representing a report generated by the Reporting API.
     */
    @Serializable
    public data class ReportingApiReport(
        public val id: String,
        /**
         * The URL of the document that triggered the report.
         */
        public val initiatorUrl: String,
        /**
         * The name of the endpoint group that should be used to deliver the report.
         */
        public val destination: String,
        /**
         * The type of the report (specifies the set of data that is contained in the report body).
         */
        public val type: String,
        /**
         * When the report was generated.
         */
        public val timestamp: Double,
        /**
         * How many uploads deep the related request was.
         */
        public val depth: Int,
        /**
         * The number of delivery attempts made so far, not including an active attempt.
         */
        public val completedAttempts: Int,
        public val body: Map<String, JsonElement>,
        public val status: ReportStatus,
    )

    @Serializable
    public data class ReportingApiEndpoint(
        /**
         * The URL of the endpoint to which reports may be delivered.
         */
        public val url: String,
        /**
         * Name of the endpoint group.
         */
        public val groupName: String,
    )

    /**
     * An object providing the result of a network resource load.
     */
    @Serializable
    public data class LoadNetworkResourcePageResult(
        public val success: Boolean,
        /**
         * Optional values used for error reporting.
         */
        public val netError: Double? = null,
        public val netErrorName: String? = null,
        public val httpStatusCode: Double? = null,
        /**
         * If successful, one of the following two fields holds the result.
         */
        public val stream: String? = null,
        /**
         * Response headers.
         */
        public val headers: Map<String, JsonElement>? = null,
    )

    /**
     * An options object that may be extended later to better support CORS,
     * CORB and streaming.
     */
    @Serializable
    public data class LoadNetworkResourceOptions(
        public val disableCache: Boolean,
        public val includeCredentials: Boolean,
    )

    /**
     * Fired when data chunk was received over the network.
     */
    @Serializable
    public data class DataReceivedParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * Data chunk length.
         */
        public val dataLength: Int,
        /**
         * Actual bytes received (might be less than dataLength for compressed encodings).
         */
        public val encodedDataLength: Int,
        /**
         * Data that was received. (Encoded as a base64 string when passed over JSON)
         */
        public val `data`: String? = null,
    )

    /**
     * Fired when EventSource message is received.
     */
    @Serializable
    public data class EventSourceMessageReceivedParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * Message type.
         */
        public val eventName: String,
        /**
         * Message identifier.
         */
        public val eventId: String,
        /**
         * Message content.
         */
        public val `data`: String,
    )

    /**
     * Fired when HTTP request has failed to load.
     */
    @Serializable
    public data class LoadingFailedParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * Resource type.
         */
        public val type: ResourceType,
        /**
         * Error message. List of network errors: https://cs.chromium.org/chromium/src/net/base/net_error_list.h
         */
        public val errorText: String,
        /**
         * True if loading was canceled.
         */
        public val canceled: Boolean? = null,
        /**
         * The reason why loading was blocked, if any.
         */
        public val blockedReason: BlockedReason? = null,
        /**
         * The reason why loading was blocked by CORS, if any.
         */
        public val corsErrorStatus: CorsErrorStatus? = null,
    )

    /**
     * Fired when HTTP request has finished loading.
     */
    @Serializable
    public data class LoadingFinishedParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * Total number of bytes received for this request.
         */
        public val encodedDataLength: Double,
    )

    /**
     * Details of an intercepted HTTP request, which must be either allowed, blocked, modified or
     * mocked.
     * Deprecated, use Fetch.requestPaused instead.
     */
    @Serializable
    public data class RequestInterceptedParameter(
        /**
         * Each request the page makes will have a unique id, however if any redirects are encountered
         * while processing that fetch, they will be reported with the same id as the original fetch.
         * Likewise if HTTP authentication is needed then the same fetch id will be used.
         */
        public val interceptionId: String,
        public val request: Request,
        /**
         * The id of the frame that initiated the request.
         */
        public val frameId: String,
        /**
         * How the requested resource will be used.
         */
        public val resourceType: ResourceType,
        /**
         * Whether this is a navigation request, which can abort the navigation completely.
         */
        public val isNavigationRequest: Boolean,
        /**
         * Set if the request is a navigation that will result in a download.
         * Only present after response is received from the server (i.e. HeadersReceived stage).
         */
        public val isDownload: Boolean? = null,
        /**
         * Redirect location, only sent if a redirect was intercepted.
         */
        public val redirectUrl: String? = null,
        /**
         * Details of the Authorization Challenge encountered. If this is set then
         * continueInterceptedRequest must contain an authChallengeResponse.
         */
        public val authChallenge: AuthChallenge? = null,
        /**
         * Response error if intercepted at response stage or if redirect occurred while intercepting
         * request.
         */
        public val responseErrorReason: ErrorReason? = null,
        /**
         * Response code if intercepted at response stage or if redirect occurred while intercepting
         * request or auth retry occurred.
         */
        public val responseStatusCode: Int? = null,
        /**
         * Response headers if intercepted at the response stage or if redirect occurred while
         * intercepting request or auth retry occurred.
         */
        public val responseHeaders: Map<String, JsonElement>? = null,
        /**
         * If the intercepted request had a corresponding requestWillBeSent event fired for it, then
         * this requestId will be the same as the requestId present in the requestWillBeSent event.
         */
        public val requestId: String? = null,
    )

    /**
     * Fired if request ended up loading from cache.
     */
    @Serializable
    public data class RequestServedFromCacheParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
    )

    /**
     * Fired when page is about to send HTTP request.
     */
    @Serializable
    public data class RequestWillBeSentParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Loader identifier. Empty string if the request is fetched from worker.
         */
        public val loaderId: String,
        /**
         * URL of the document this request is loaded for.
         */
        public val documentURL: String,
        /**
         * Request data.
         */
        public val request: Request,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * Timestamp.
         */
        public val wallTime: Double,
        /**
         * Request initiator.
         */
        public val initiator: Initiator,
        /**
         * In the case that redirectResponse is populated, this flag indicates whether
         * requestWillBeSentExtraInfo and responseReceivedExtraInfo events will be or were emitted
         * for the request which was just redirected.
         */
        public val redirectHasExtraInfo: Boolean,
        /**
         * Redirect response data.
         */
        public val redirectResponse: Response? = null,
        /**
         * Type of this resource.
         */
        public val type: ResourceType? = null,
        /**
         * Frame identifier.
         */
        public val frameId: String? = null,
        /**
         * Whether the request is initiated by a user gesture. Defaults to false.
         */
        public val hasUserGesture: Boolean? = null,
    )

    /**
     * Fired when resource loading priority is changed
     */
    @Serializable
    public data class ResourceChangedPriorityParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * New priority
         */
        public val newPriority: ResourcePriority,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
    )

    /**
     * Fired when a signed exchange was received over the network
     */
    @Serializable
    public data class SignedExchangeReceivedParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Information about the signed exchange response.
         */
        public val info: SignedExchangeInfo,
    )

    /**
     * Fired when HTTP response is available.
     */
    @Serializable
    public data class ResponseReceivedParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Loader identifier. Empty string if the request is fetched from worker.
         */
        public val loaderId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * Resource type.
         */
        public val type: ResourceType,
        /**
         * Response data.
         */
        public val response: Response,
        /**
         * Indicates whether requestWillBeSentExtraInfo and responseReceivedExtraInfo events will be
         * or were emitted for this request.
         */
        public val hasExtraInfo: Boolean,
        /**
         * Frame identifier.
         */
        public val frameId: String? = null,
    )

    /**
     * Fired when WebSocket is closed.
     */
    @Serializable
    public data class WebSocketClosedParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
    )

    /**
     * Fired upon WebSocket creation.
     */
    @Serializable
    public data class WebSocketCreatedParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * WebSocket request URL.
         */
        public val url: String,
        /**
         * Request initiator.
         */
        public val initiator: Initiator? = null,
    )

    /**
     * Fired when WebSocket message error occurs.
     */
    @Serializable
    public data class WebSocketFrameErrorParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * WebSocket error message.
         */
        public val errorMessage: String,
    )

    /**
     * Fired when WebSocket message is received.
     */
    @Serializable
    public data class WebSocketFrameReceivedParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * WebSocket response data.
         */
        public val response: WebSocketFrame,
    )

    /**
     * Fired when WebSocket message is sent.
     */
    @Serializable
    public data class WebSocketFrameSentParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * WebSocket response data.
         */
        public val response: WebSocketFrame,
    )

    /**
     * Fired when WebSocket handshake response becomes available.
     */
    @Serializable
    public data class WebSocketHandshakeResponseReceivedParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * WebSocket response data.
         */
        public val response: WebSocketResponse,
    )

    /**
     * Fired when WebSocket is about to initiate handshake.
     */
    @Serializable
    public data class WebSocketWillSendHandshakeRequestParameter(
        /**
         * Request identifier.
         */
        public val requestId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * UTC Timestamp.
         */
        public val wallTime: Double,
        /**
         * WebSocket request data.
         */
        public val request: WebSocketRequest,
    )

    /**
     * Fired upon WebTransport creation.
     */
    @Serializable
    public data class WebTransportCreatedParameter(
        /**
         * WebTransport identifier.
         */
        public val transportId: String,
        /**
         * WebTransport request URL.
         */
        public val url: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
        /**
         * Request initiator.
         */
        public val initiator: Initiator? = null,
    )

    /**
     * Fired when WebTransport handshake is finished.
     */
    @Serializable
    public data class WebTransportConnectionEstablishedParameter(
        /**
         * WebTransport identifier.
         */
        public val transportId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
    )

    /**
     * Fired when WebTransport is disposed.
     */
    @Serializable
    public data class WebTransportClosedParameter(
        /**
         * WebTransport identifier.
         */
        public val transportId: String,
        /**
         * Timestamp.
         */
        public val timestamp: Double,
    )

    /**
     * Fired upon direct_socket.TCPSocket creation.
     */
    @Serializable
    public data class DirectTCPSocketCreatedParameter(
        public val identifier: String,
        public val remoteAddr: String,
        /**
         * Unsigned int 16.
         */
        public val remotePort: Int,
        public val options: DirectTCPSocketOptions,
        public val timestamp: Double,
        public val initiator: Initiator? = null,
    )

    /**
     * Fired when direct_socket.TCPSocket connection is opened.
     */
    @Serializable
    public data class DirectTCPSocketOpenedParameter(
        public val identifier: String,
        public val remoteAddr: String,
        /**
         * Expected to be unsigned integer.
         */
        public val remotePort: Int,
        public val timestamp: Double,
        public val localAddr: String? = null,
        /**
         * Expected to be unsigned integer.
         */
        public val localPort: Int? = null,
    )

    /**
     * Fired when direct_socket.TCPSocket is aborted.
     */
    @Serializable
    public data class DirectTCPSocketAbortedParameter(
        public val identifier: String,
        public val errorMessage: String,
        public val timestamp: Double,
    )

    /**
     * Fired when direct_socket.TCPSocket is closed.
     */
    @Serializable
    public data class DirectTCPSocketClosedParameter(
        public val identifier: String,
        public val timestamp: Double,
    )

    /**
     * Fired when data is sent to tcp direct socket stream.
     */
    @Serializable
    public data class DirectTCPSocketChunkSentParameter(
        public val identifier: String,
        public val `data`: String,
        public val timestamp: Double,
    )

    /**
     * Fired when data is received from tcp direct socket stream.
     */
    @Serializable
    public data class DirectTCPSocketChunkReceivedParameter(
        public val identifier: String,
        public val `data`: String,
        public val timestamp: Double,
    )

    /**
     * Fired upon direct_socket.UDPSocket creation.
     */
    @Serializable
    public data class DirectUDPSocketCreatedParameter(
        public val identifier: String,
        public val options: DirectUDPSocketOptions,
        public val timestamp: Double,
        public val initiator: Initiator? = null,
    )

    /**
     * Fired when direct_socket.UDPSocket connection is opened.
     */
    @Serializable
    public data class DirectUDPSocketOpenedParameter(
        public val identifier: String,
        public val localAddr: String,
        /**
         * Expected to be unsigned integer.
         */
        public val localPort: Int,
        public val timestamp: Double,
        public val remoteAddr: String? = null,
        /**
         * Expected to be unsigned integer.
         */
        public val remotePort: Int? = null,
    )

    /**
     * Fired when direct_socket.UDPSocket is aborted.
     */
    @Serializable
    public data class DirectUDPSocketAbortedParameter(
        public val identifier: String,
        public val errorMessage: String,
        public val timestamp: Double,
    )

    /**
     * Fired when direct_socket.UDPSocket is closed.
     */
    @Serializable
    public data class DirectUDPSocketClosedParameter(
        public val identifier: String,
        public val timestamp: Double,
    )

    /**
     * Fired when message is sent to udp direct socket stream.
     */
    @Serializable
    public data class DirectUDPSocketChunkSentParameter(
        public val identifier: String,
        public val message: DirectUDPMessage,
        public val timestamp: Double,
    )

    /**
     * Fired when message is received from udp direct socket stream.
     */
    @Serializable
    public data class DirectUDPSocketChunkReceivedParameter(
        public val identifier: String,
        public val message: DirectUDPMessage,
        public val timestamp: Double,
    )

    /**
     * Fired when additional information about a requestWillBeSent event is available from the
     * network stack. Not every requestWillBeSent event will have an additional
     * requestWillBeSentExtraInfo fired for it, and there is no guarantee whether requestWillBeSent
     * or requestWillBeSentExtraInfo will be fired first for the same request.
     */
    @Serializable
    public data class RequestWillBeSentExtraInfoParameter(
        /**
         * Request identifier. Used to match this information to an existing requestWillBeSent event.
         */
        public val requestId: String,
        /**
         * A list of cookies potentially associated to the requested URL. This includes both cookies sent with
         * the request and the ones not sent; the latter are distinguished by having blockedReasons field set.
         */
        public val associatedCookies: List<AssociatedCookie>,
        /**
         * Raw request headers as they will be sent over the wire.
         */
        public val headers: Map<String, JsonElement>,
        /**
         * Connection timing information for the request.
         */
        public val connectTiming: ConnectTiming,
        /**
         * The client security state set for the request.
         */
        public val clientSecurityState: ClientSecurityState? = null,
        /**
         * Whether the site has partitioned cookies stored in a partition different than the current one.
         */
        public val siteHasCookieInOtherPartition: Boolean? = null,
    )

    /**
     * Fired when additional information about a responseReceived event is available from the network
     * stack. Not every responseReceived event will have an additional responseReceivedExtraInfo for
     * it, and responseReceivedExtraInfo may be fired before or after responseReceived.
     */
    @Serializable
    public data class ResponseReceivedExtraInfoParameter(
        /**
         * Request identifier. Used to match this information to another responseReceived event.
         */
        public val requestId: String,
        /**
         * A list of cookies which were not stored from the response along with the corresponding
         * reasons for blocking. The cookies here may not be valid due to syntax errors, which
         * are represented by the invalid cookie line string instead of a proper cookie.
         */
        public val blockedCookies: List<BlockedSetCookieWithReason>,
        /**
         * Raw response headers as they were received over the wire.
         * Duplicate headers in the response are represented as a single key with their values
         * concatentated using `\n` as the separator.
         * See also `headersText` that contains verbatim text for HTTP/1.*.
         */
        public val headers: Map<String, JsonElement>,
        /**
         * The IP address space of the resource. The address space can only be determined once the transport
         * established the connection, so we can't send it in `requestWillBeSentExtraInfo`.
         */
        public val resourceIPAddressSpace: IPAddressSpace,
        /**
         * The status code of the response. This is useful in cases the request failed and no responseReceived
         * event is triggered, which is the case for, e.g., CORS errors. This is also the correct status code
         * for cached requests, where the status in responseReceived is a 200 and this will be 304.
         */
        public val statusCode: Int,
        /**
         * Raw response header text as it was received over the wire. The raw text may not always be
         * available, such as in the case of HTTP/2 or QUIC.
         */
        public val headersText: String? = null,
        /**
         * The cookie partition key that will be used to store partitioned cookies set in this response.
         * Only sent when partitioned cookies are enabled.
         */
        public val cookiePartitionKey: CookiePartitionKey? = null,
        /**
         * True if partitioned cookies are enabled, but the partition key is not serializable to string.
         */
        public val cookiePartitionKeyOpaque: Boolean? = null,
        /**
         * A list of cookies which should have been blocked by 3PCD but are exempted and stored from
         * the response with the corresponding reason.
         */
        public val exemptedCookies: List<ExemptedSetCookieWithReason>? = null,
    )

    /**
     * Fired when 103 Early Hints headers is received in addition to the common response.
     * Not every responseReceived event will have an responseReceivedEarlyHints fired.
     * Only one responseReceivedEarlyHints may be fired for eached responseReceived event.
     */
    @Serializable
    public data class ResponseReceivedEarlyHintsParameter(
        /**
         * Request identifier. Used to match this information to another responseReceived event.
         */
        public val requestId: String,
        /**
         * Raw response headers as they were received over the wire.
         * Duplicate headers in the response are represented as a single key with their values
         * concatentated using `\n` as the separator.
         * See also `headersText` that contains verbatim text for HTTP/1.*.
         */
        public val headers: Map<String, JsonElement>,
    )

    /**
     * Fired exactly once for each Trust Token operation. Depending on
     * the type of the operation and whether the operation succeeded or
     * failed, the event is fired before the corresponding request was sent
     * or after the response was received.
     */
    @Serializable
    public data class TrustTokenOperationDoneParameter(
        /**
         * Detailed success or error status of the operation.
         * 'AlreadyExists' also signifies a successful operation, as the result
         * of the operation already exists und thus, the operation was abort
         * preemptively (e.g. a cache hit).
         */
        public val status: String,
        public val type: TrustTokenOperationType,
        public val requestId: String,
        /**
         * Top level origin. The context in which the operation was attempted.
         */
        public val topLevelOrigin: String? = null,
        /**
         * Origin of the issuer in case of a "Issuance" or "Redemption" operation.
         */
        public val issuerOrigin: String? = null,
        /**
         * The number of obtained Trust Tokens on a successful "Issuance" operation.
         */
        public val issuedTokenCount: Int? = null,
    )

    /**
     * Fired once when parsing the .wbn file has succeeded.
     * The event contains the information about the web bundle contents.
     */
    @Serializable
    public data class SubresourceWebBundleMetadataReceivedParameter(
        /**
         * Request identifier. Used to match this information to another event.
         */
        public val requestId: String,
        /**
         * A list of URLs of resources in the subresource Web Bundle.
         */
        public val urls: List<String>,
    )

    /**
     * Fired once when parsing the .wbn file has failed.
     */
    @Serializable
    public data class SubresourceWebBundleMetadataErrorParameter(
        /**
         * Request identifier. Used to match this information to another event.
         */
        public val requestId: String,
        /**
         * Error message
         */
        public val errorMessage: String,
    )

    /**
     * Fired when handling requests for resources within a .wbn file.
     * Note: this will only be fired for resources that are requested by the webpage.
     */
    @Serializable
    public data class SubresourceWebBundleInnerResponseParsedParameter(
        /**
         * Request identifier of the subresource request
         */
        public val innerRequestId: String,
        /**
         * URL of the subresource resource.
         */
        public val innerRequestURL: String,
        /**
         * Bundle request identifier. Used to match this information to another event.
         * This made be absent in case when the instrumentation was enabled only
         * after webbundle was parsed.
         */
        public val bundleRequestId: String? = null,
    )

    /**
     * Fired when request for resources within a .wbn file failed.
     */
    @Serializable
    public data class SubresourceWebBundleInnerResponseErrorParameter(
        /**
         * Request identifier of the subresource request
         */
        public val innerRequestId: String,
        /**
         * URL of the subresource resource.
         */
        public val innerRequestURL: String,
        /**
         * Error message
         */
        public val errorMessage: String,
        /**
         * Bundle request identifier. Used to match this information to another event.
         * This made be absent in case when the instrumentation was enabled only
         * after webbundle was parsed.
         */
        public val bundleRequestId: String? = null,
    )

    /**
     * Is sent whenever a new report is added.
     * And after 'enableReportingApi' for all existing reports.
     */
    @Serializable
    public data class ReportingApiReportAddedParameter(
        public val report: ReportingApiReport,
    )

    @Serializable
    public data class ReportingApiReportUpdatedParameter(
        public val report: ReportingApiReport,
    )

    @Serializable
    public data class ReportingApiEndpointsChangedForOriginParameter(
        /**
         * Origin of the document(s) which configured the endpoints.
         */
        public val origin: String,
        public val endpoints: List<ReportingApiEndpoint>,
    )

    @Serializable
    public data class SetAcceptedEncodingsParameter(
        /**
         * List of accepted content encodings.
         */
        public val encodings: List<ContentEncoding>,
    )

    @Serializable
    public data class CanClearBrowserCacheReturn(
        /**
         * True if browser cache can be cleared.
         */
        public val result: Boolean,
    )

    @Serializable
    public data class CanClearBrowserCookiesReturn(
        /**
         * True if browser cookies can be cleared.
         */
        public val result: Boolean,
    )

    @Serializable
    public data class CanEmulateNetworkConditionsReturn(
        /**
         * True if emulation of network conditions is supported.
         */
        public val result: Boolean,
    )

    @Serializable
    public data class ContinueInterceptedRequestParameter(
        public val interceptionId: String,
        /**
         * If set this causes the request to fail with the given reason. Passing `Aborted` for requests
         * marked with `isNavigationRequest` also cancels the navigation. Must not be set in response
         * to an authChallenge.
         */
        public val errorReason: ErrorReason? = null,
        /**
         * If set the requests completes using with the provided base64 encoded raw response, including
         * HTTP status line and headers etc... Must not be set in response to an authChallenge. (Encoded as a base64 string when passed over JSON)
         */
        public val rawResponse: String? = null,
        /**
         * If set the request url will be modified in a way that's not observable by page. Must not be
         * set in response to an authChallenge.
         */
        public val url: String? = null,
        /**
         * If set this allows the request method to be overridden. Must not be set in response to an
         * authChallenge.
         */
        public val method: String? = null,
        /**
         * If set this allows postData to be set. Must not be set in response to an authChallenge.
         */
        public val postData: String? = null,
        /**
         * If set this allows the request headers to be changed. Must not be set in response to an
         * authChallenge.
         */
        public val headers: Map<String, JsonElement>? = null,
        /**
         * Response to a requestIntercepted with an authChallenge. Must not be set otherwise.
         */
        public val authChallengeResponse: AuthChallengeResponse? = null,
    )

    @Serializable
    public data class DeleteCookiesParameter(
        /**
         * Name of the cookies to remove.
         */
        public val name: String,
        /**
         * If specified, deletes all the cookies with the given name where domain and path match
         * provided URL.
         */
        public val url: String? = null,
        /**
         * If specified, deletes only cookies with the exact domain.
         */
        public val domain: String? = null,
        /**
         * If specified, deletes only cookies with the exact path.
         */
        public val path: String? = null,
        /**
         * If specified, deletes only cookies with the the given name and partitionKey where
         * all partition key attributes match the cookie partition key attribute.
         */
        public val partitionKey: CookiePartitionKey? = null,
    )

    @Serializable
    public data class EmulateNetworkConditionsParameter(
        /**
         * True to emulate internet disconnection.
         */
        public val offline: Boolean,
        /**
         * Minimum latency from request sent to response headers received (ms).
         */
        public val latency: Double,
        /**
         * Maximal aggregated download throughput (bytes/sec). -1 disables download throttling.
         */
        public val downloadThroughput: Double,
        /**
         * Maximal aggregated upload throughput (bytes/sec).  -1 disables upload throttling.
         */
        public val uploadThroughput: Double,
        /**
         * Connection type if known.
         */
        public val connectionType: ConnectionType? = null,
        /**
         * WebRTC packet loss (percent, 0-100). 0 disables packet loss emulation, 100 drops all the packets.
         */
        public val packetLoss: Double? = null,
        /**
         * WebRTC packet queue length (packet). 0 removes any queue length limitations.
         */
        public val packetQueueLength: Int? = null,
        /**
         * WebRTC packetReordering feature.
         */
        public val packetReordering: Boolean? = null,
    )

    @Serializable
    public data class EnableParameter(
        /**
         * Buffer size in bytes to use when preserving network payloads (XHRs, etc).
         */
        public val maxTotalBufferSize: Int? = null,
        /**
         * Per-resource buffer size in bytes to use when preserving network payloads (XHRs, etc).
         */
        public val maxResourceBufferSize: Int? = null,
        /**
         * Longest post body size (in bytes) that would be included in requestWillBeSent notification
         */
        public val maxPostDataSize: Int? = null,
        /**
         * Whether DirectSocket chunk send/receive events should be reported.
         */
        public val reportDirectSocketTraffic: Boolean? = null,
    )

    @Serializable
    public data class GetAllCookiesReturn(
        /**
         * Array of cookie objects.
         */
        public val cookies: List<Cookie>,
    )

    @Serializable
    public data class GetCertificateParameter(
        /**
         * Origin to get certificate for.
         */
        public val origin: String,
    )

    @Serializable
    public data class GetCertificateReturn(
        public val tableNames: List<String>,
    )

    @Serializable
    public data class GetCookiesParameter(
        /**
         * The list of URLs for which applicable cookies will be fetched.
         * If not specified, it's assumed to be set to the list containing
         * the URLs of the page and all of its subframes.
         */
        public val urls: List<String>? = null,
    )

    @Serializable
    public data class GetCookiesReturn(
        /**
         * Array of cookie objects.
         */
        public val cookies: List<Cookie>,
    )

    @Serializable
    public data class GetResponseBodyParameter(
        /**
         * Identifier of the network request to get content for.
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
    public data class GetRequestPostDataParameter(
        /**
         * Identifier of the network request to get content for.
         */
        public val requestId: String,
    )

    @Serializable
    public data class GetRequestPostDataReturn(
        /**
         * Request body string, omitting files from multipart requests
         */
        public val postData: String,
    )

    @Serializable
    public data class GetResponseBodyForInterceptionParameter(
        /**
         * Identifier for the intercepted request to get body for.
         */
        public val interceptionId: String,
    )

    @Serializable
    public data class GetResponseBodyForInterceptionReturn(
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
    public data class TakeResponseBodyForInterceptionAsStreamParameter(
        public val interceptionId: String,
    )

    @Serializable
    public data class TakeResponseBodyForInterceptionAsStreamReturn(
        public val stream: String,
    )

    @Serializable
    public data class ReplayXHRParameter(
        /**
         * Identifier of XHR to replay.
         */
        public val requestId: String,
    )

    @Serializable
    public data class SearchInResponseBodyParameter(
        /**
         * Identifier of the network response to search.
         */
        public val requestId: String,
        /**
         * String to search for.
         */
        public val query: String,
        /**
         * If true, search is case sensitive.
         */
        public val caseSensitive: Boolean? = null,
        /**
         * If true, treats string parameter as regex.
         */
        public val isRegex: Boolean? = null,
    )

    @Serializable
    public data class SearchInResponseBodyReturn(
        /**
         * List of search matches.
         */
        public val result: List<Debugger.SearchMatch>,
    )

    @Serializable
    public data class SetBlockedURLsParameter(
        /**
         * URL patterns to block. Wildcards ('*') are allowed.
         */
        public val urls: List<String>,
    )

    @Serializable
    public data class SetBypassServiceWorkerParameter(
        /**
         * Bypass service worker and load from network.
         */
        public val bypass: Boolean,
    )

    @Serializable
    public data class SetCacheDisabledParameter(
        /**
         * Cache disabled state.
         */
        public val cacheDisabled: Boolean,
    )

    @Serializable
    public data class SetCookieParameter(
        /**
         * Cookie name.
         */
        public val name: String,
        /**
         * Cookie value.
         */
        public val `value`: String,
        /**
         * The request-URI to associate with the setting of the cookie. This value can affect the
         * default domain, path, source port, and source scheme values of the created cookie.
         */
        public val url: String? = null,
        /**
         * Cookie domain.
         */
        public val domain: String? = null,
        /**
         * Cookie path.
         */
        public val path: String? = null,
        /**
         * True if cookie is secure.
         */
        public val secure: Boolean? = null,
        /**
         * True if cookie is http-only.
         */
        public val httpOnly: Boolean? = null,
        /**
         * Cookie SameSite type.
         */
        public val sameSite: CookieSameSite? = null,
        /**
         * Cookie expiration date, session cookie if not set
         */
        public val expires: Double? = null,
        /**
         * Cookie Priority type.
         */
        public val priority: CookiePriority? = null,
        /**
         * True if cookie is SameParty.
         */
        public val sameParty: Boolean? = null,
        /**
         * Cookie source scheme type.
         */
        public val sourceScheme: CookieSourceScheme? = null,
        /**
         * Cookie source port. Valid values are {-1, [1, 65535]}, -1 indicates an unspecified port.
         * An unspecified port value allows protocol clients to emulate legacy cookie scope for the port.
         * This is a temporary ability and it will be removed in the future.
         */
        public val sourcePort: Int? = null,
        /**
         * Cookie partition key. If not set, the cookie will be set as not partitioned.
         */
        public val partitionKey: CookiePartitionKey? = null,
    )

    @Serializable
    public data class SetCookieReturn(
        /**
         * Always set to true. If an error occurs, the response indicates protocol error.
         */
        public val success: Boolean,
    )

    @Serializable
    public data class SetCookiesParameter(
        /**
         * Cookies to be set.
         */
        public val cookies: List<CookieParam>,
    )

    @Serializable
    public data class SetExtraHTTPHeadersParameter(
        /**
         * Map with extra HTTP headers.
         */
        public val headers: Map<String, JsonElement>,
    )

    @Serializable
    public data class SetAttachDebugStackParameter(
        /**
         * Whether to attach a page script stack for debugging purpose.
         */
        public val enabled: Boolean,
    )

    @Serializable
    public data class SetRequestInterceptionParameter(
        /**
         * Requests matching any of these patterns will be forwarded and wait for the corresponding
         * continueInterceptedRequest call.
         */
        public val patterns: List<RequestPattern>,
    )

    @Serializable
    public data class SetUserAgentOverrideParameter(
        /**
         * User agent to use.
         */
        public val userAgent: String,
        /**
         * Browser language to emulate.
         */
        public val acceptLanguage: String? = null,
        /**
         * The platform navigator.platform should return.
         */
        public val platform: String? = null,
        /**
         * To be sent in Sec-CH-UA-* headers and returned in navigator.userAgentData
         */
        public val userAgentMetadata: Emulation.UserAgentMetadata? = null,
    )

    @Serializable
    public data class StreamResourceContentParameter(
        /**
         * Identifier of the request to stream.
         */
        public val requestId: String,
    )

    @Serializable
    public data class StreamResourceContentReturn(
        /**
         * Data that has been buffered until streaming is enabled. (Encoded as a base64 string when passed over JSON)
         */
        public val bufferedData: String,
    )

    @Serializable
    public data class GetSecurityIsolationStatusParameter(
        /**
         * If no frameId is provided, the status of the target is provided.
         */
        public val frameId: String? = null,
    )

    @Serializable
    public data class GetSecurityIsolationStatusReturn(
        public val status: SecurityIsolationStatus,
    )

    @Serializable
    public data class EnableReportingApiParameter(
        /**
         * Whether to enable or disable events for the Reporting API
         */
        public val enable: Boolean,
    )

    @Serializable
    public data class LoadNetworkResourceParameter(
        /**
         * Frame id to get the resource for. Mandatory for frame targets, and
         * should be omitted for worker targets.
         */
        public val frameId: String? = null,
        /**
         * URL of the resource to get content for.
         */
        public val url: String,
        /**
         * Options for the request.
         */
        public val options: LoadNetworkResourceOptions,
    )

    @Serializable
    public data class LoadNetworkResourceReturn(
        public val resource: LoadNetworkResourcePageResult,
    )

    @Serializable
    public data class SetCookieControlsParameter(
        /**
         * Whether 3pc restriction is enabled.
         */
        public val enableThirdPartyCookieRestriction: Boolean,
        /**
         * Whether 3pc grace period exception should be enabled; false by default.
         */
        public val disableThirdPartyCookieMetadata: Boolean,
        /**
         * Whether 3pc heuristics exceptions should be enabled; false by default.
         */
        public val disableThirdPartyCookieHeuristics: Boolean,
    )
}
