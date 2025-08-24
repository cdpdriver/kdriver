package dev.kdriver.core.network

import dev.kdriver.cdp.domain.Network
import dev.kdriver.cdp.domain.network
import dev.kdriver.core.tab.Tab
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * Default implementation of [RequestExpectation].
 */
open class BaseRequestExpectation(
    private val tab: Tab,
    override val urlPattern: Regex,
) : RequestExpectation {

    private var requestJob: Job? = null
    private var responseJob: Job? = null
    private var loadingFinishedJob: Job? = null

    private var requestDeferred = CompletableDeferred<Network.RequestWillBeSentParameter>()
    private var responseDeferred = CompletableDeferred<Network.ResponseReceivedParameter>()
    private var loadingFinishedDeferred = CompletableDeferred<Network.LoadingFinishedParameter>()

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

    private suspend fun setup() {
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
    }

    private fun teardown() {
        requestJob?.cancel()
        responseJob?.cancel()
        loadingFinishedJob?.cancel()
    }

    override suspend fun reset() {
        requestDeferred = CompletableDeferred()
        responseDeferred = CompletableDeferred()
        loadingFinishedDeferred = CompletableDeferred()
        requestId = null
        teardown()
        setup()
    }

    override suspend fun <T> use(block: suspend RequestExpectation.() -> T): T {
        setup()
        try {
            return block()
        } finally {
            teardown()
        }
    }

    override suspend fun getRequestEvent(): Network.RequestWillBeSentParameter = requestDeferred.await()

    override suspend fun getResponseEvent(): Network.ResponseReceivedParameter = responseDeferred.await()

    override suspend fun getRequest(): Network.Request = getRequestEvent().request

    override suspend fun getResponse(): Network.Response = getResponseEvent().response

    override suspend fun getRawResponseBody(): EncodedBody {
        val requestId = getResponseEvent().requestId
        loadingFinishedDeferred.await() // Ensure the loading is finished before fetching the body
        return EncodedBody(tab.network.getResponseBody(requestId))
    }

}
