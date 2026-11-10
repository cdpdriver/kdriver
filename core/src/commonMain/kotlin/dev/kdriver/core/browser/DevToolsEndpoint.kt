package dev.kdriver.core.browser

import io.ktor.http.*

/**
 * Where the browser's DevTools endpoints are reached: the HTTP base used for `/json/…` requests,
 * and the base the WebSocket connections are opened on.
 *
 * Built from [Config.browserUrl] when it is set — a browser behind a reverse proxy, possibly over
 * TLS and under a path prefix — or else from [Config.host] and [Config.port], as before.
 */
internal class DevToolsEndpoint private constructor(
    /** HTTP(S) base address, always ending with `/`. */
    val httpBaseUrl: String,
    /** WS(S) base address without a trailing `/`. */
    private val webSocketBaseUrl: String,
    /** Whether the `webSocketDebuggerUrl` advertised by the browser must be moved onto [webSocketBaseUrl]. */
    private val rewritesAdvertisedUrls: Boolean,
) {

    /**
     * The WebSocket URL of a DevTools [path] (`/devtools/page/<id>`, …) on this endpoint.
     */
    fun webSocketUrl(path: String): String = webSocketBaseUrl + "/" + path.trimStart('/')

    /**
     * Moves the `webSocketDebuggerUrl` advertised by the browser onto this endpoint.
     *
     * The browser builds that URL from its own listening address, which a client behind a reverse
     * proxy cannot reach: only its path (and query, if any) is kept. Without [Config.browserUrl] the
     * advertised URL is returned unchanged.
     */
    fun rewriteWebSocketDebuggerUrl(advertised: String): String {
        if (!rewritesAdvertisedUrls) return advertised
        return webSocketUrl(Url(advertised).encodedPathAndQuery)
    }

    companion object {

        fun from(config: Config): DevToolsEndpoint {
            val browserUrl = config.browserUrl
                ?: return DevToolsEndpoint(
                    httpBaseUrl = "http://${config.host}:${config.port}/",
                    webSocketBaseUrl = "ws://${config.host}:${config.port}",
                    rewritesAdvertisedUrls = false,
                )
            val http = URLBuilder(browserUrl.trimEnd('/'))
            val webSocket = URLBuilder(http).apply {
                protocol = if (http.protocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
            }
            return DevToolsEndpoint(
                httpBaseUrl = http.buildString() + "/",
                webSocketBaseUrl = webSocket.buildString(),
                rewritesAdvertisedUrls = true,
            )
        }

    }

}
