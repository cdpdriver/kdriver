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

    private val requestDeferred = CompletableDeferred<Network.RequestWillBeSentParameter>()
    private val responseDeferred = CompletableDeferred<Network.ResponseReceivedParameter>()

    private var requestId: String? = null

    private val requestHandler: suspend (Network.RequestWillBeSentParameter) -> Unit = requestHandler@{ event ->
        if (!urlPattern.containsMatchIn(event.request.url)) return@requestHandler
        requestId = event.requestId
        requestDeferred.complete(event)
        requestJob?.cancel()
        requestJob = null
    }

    private val responseHandler: suspend (Network.ResponseReceivedParameter) -> Unit = responseHandler@{ event ->
        if (event.requestId != requestId) return@responseHandler
        responseDeferred.complete(event)
        responseJob?.cancel()
        responseJob = null
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
        try {
            return block()
        } finally {
            requestJob?.cancel()
            responseJob?.cancel()
        }
    }

    /**
     * Returns the request once it has been received.
     */
    suspend fun getRequest(): Network.Request = requestDeferred.await().request

    /**
     * Returns the response once it has been received.
     */
    suspend fun getResponse(): Network.Response = responseDeferred.await().response

    /**
     * Fetches the response body once it has been received.
     */
    suspend fun getResponseBody(): Network.GetResponseBodyReturn {
        val reqId = requestDeferred.await().requestId
        return tab.network.getResponseBody(reqId)
    }

}
