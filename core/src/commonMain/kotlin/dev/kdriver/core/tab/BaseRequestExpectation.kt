package dev.kdriver.core.tab

import dev.kdriver.cdp.domain.Network
import dev.kdriver.cdp.domain.network
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * Base class for handling request and response expectations.
 * This class provides a context manager to wait for specific network requests and responses
 * based on a URL pattern. It sets up handlers for request and response events and provides
 * properties to access the request, response, and response body.
 *
 * @param tab The Tab instance to monitor.
 * @param urlPattern The URL pattern to match requests and responses.
 */
class BaseRequestExpectation(
    private val tab: Tab,
    private val urlPattern: Regex,
) {

    private var requestJob: Job? = null
    private var responseJob: Job? = null
    private var loadingFinishedJob: Job? = null

    private val requestDeferred = CompletableDeferred<Network.RequestWillBeSentParameter>()
    private val responseDeferred = CompletableDeferred<Network.ResponseReceivedParameter>()
    private val loadingFinishedDeferred = CompletableDeferred<Network.LoadingFinishedParameter>()

    private var requestId: String? = null

    private val requestHandler: suspend (Network.RequestWillBeSentParameter) -> Unit =
        requestHandler@{ event ->
            if (!urlPattern.containsMatchIn(event.request.url)) return@requestHandler
            requestId = event.requestId
            requestDeferred.complete(event)
            requestJob?.cancel()
            requestJob = null
        }

    private val responseHandler: suspend (Network.ResponseReceivedParameter) -> Unit =
        responseHandler@{ event ->
            if (event.requestId != requestId) return@responseHandler
            responseDeferred.complete(event)
            responseJob?.cancel()
            responseJob = null
        }

    private val loadingFinishedHandler: suspend (Network.LoadingFinishedParameter) -> Unit =
        loadingFinishedHandler@{ event ->
            if (event.requestId != requestId) return@loadingFinishedHandler
            loadingFinishedDeferred.complete(event)
            loadingFinishedJob?.cancel()
            loadingFinishedJob = null
        }

    /**
     * Expect a request/response that matches the given [urlPattern].
     */
    suspend fun <T> use(block: suspend BaseRequestExpectation.() -> T): T {
        val coroutineScope = CoroutineScope(coroutineContext)
        tab.network.enable()
        requestJob = coroutineScope.launch {
            tab.network.requestWillBeSent.collect { requestHandler(it) }
        }
        responseJob = coroutineScope.launch {
            tab.network.responseReceived.collect { responseHandler(it) }
        }
        loadingFinishedJob = coroutineScope.launch {
            tab.network.loadingFinished.collect { loadingFinishedHandler(it) }
        }
        try {
            return block()
        } finally {
            requestJob?.cancel()
            responseJob?.cancel()
            loadingFinishedJob?.cancel()
        }
    }

    /**
     * Returns the request event once it has been received.
     */
    suspend fun getRequestEvent(): Network.RequestWillBeSentParameter = requestDeferred.await()

    /**
     * Returns the response event once it has been received.
     */
    suspend fun getResponseEvent(): Network.ResponseReceivedParameter = responseDeferred.await()

    /**
     * Returns the request once it has been received.
     */
    suspend fun getRequest(): Network.Request = getRequestEvent().request

    /**
     * Returns the response once it has been received.
     */
    suspend fun getResponse(): Network.Response = getResponseEvent().response

    /**
     * Fetches the response body once it has been received.
     */
    suspend fun getResponseBody(): Network.GetResponseBodyReturn {
        val requestId = getResponseEvent().requestId
        loadingFinishedDeferred.await() // Ensure the loading is finished before fetching the body
        return tab.network.getResponseBody(requestId)
    }

}
