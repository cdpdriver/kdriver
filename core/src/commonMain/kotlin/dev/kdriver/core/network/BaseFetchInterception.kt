package dev.kdriver.core.network

import dev.kdriver.cdp.domain.Fetch
import dev.kdriver.cdp.domain.Fetch.HeaderEntry
import dev.kdriver.cdp.domain.Network
import dev.kdriver.cdp.domain.fetch
import dev.kdriver.core.tab.Tab
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * Default implementation of [FetchInterception].
 */
open class BaseFetchInterception(
    private val tab: Tab,
    override val urlPattern: String,
    override val requestStage: Fetch.RequestStage,
    override val resourceType: Network.ResourceType,
) : FetchInterception {

    private val responseDeferred = CompletableDeferred<Fetch.RequestPausedParameter>()
    private var job: Job? = null

    private val handler: suspend (Fetch.RequestPausedParameter) -> Unit = handler@{ event ->
        responseDeferred.complete(event)
        job?.cancel()
        job = null
    }

    override suspend fun <R> use(block: suspend FetchInterception.() -> R): R {
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

    override suspend fun getRequestEvent(): Fetch.RequestPausedParameter = responseDeferred.await()

    override suspend fun getRequest(): Network.Request = getRequestEvent().request

    override suspend fun getRawResponseBody(): EncodedBody {
        val requestId = getRequestEvent().requestId
        return EncodedBody(tab.fetch.getResponseBody(requestId))
    }

    override suspend fun failRequest(errorReason: Network.ErrorReason) {
        val requestId = getRequestEvent().requestId
        tab.fetch.failRequest(requestId, errorReason)
    }

    override suspend fun continueRequest(
        url: String?,
        method: String?,
        postData: String?,
        headers: List<HeaderEntry>?,
        interceptResponse: Boolean?,
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

    override suspend fun fulfillRequest(
        responseCode: Int,
        responseHeaders: List<HeaderEntry>?,
        binaryResponseHeaders: String?,
        body: String?,
        responsePhrase: String?,
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

    override suspend fun continueResponse(
        responseCode: Int?,
        responsePhrase: String?,
        responseHeaders: List<HeaderEntry>?,
        binaryResponseHeaders: String?,
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
