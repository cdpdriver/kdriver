package dev.kdriver.core.browser

import dev.kdriver.cdp.Serialization
import dev.kdriver.core.connection.getHttpApiClientEngine
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*

/**
 * Client for the browser's DevTools HTTP endpoints (`/json/version`, `/json/list`, …).
 *
 * @param baseUrl The DevTools HTTP address, `http://` or `https://`, optionally with a path prefix
 * (`https://example.com/browser`). The `json/…` endpoints are resolved below it.
 * @property headers Extra HTTP headers sent with every request, for instance credentials expected by a
 * reverse proxy in front of the browser.
 */
class HTTPApi(
    baseUrl: String,
    val headers: Map<String, String> = emptyMap(),
) {

    /**
     * Targets a browser listening directly on [host]:[port], over plain HTTP.
     */
    constructor(host: String, port: Int) : this("http://$host:$port/")

    val baseUrl = baseUrl.trimEnd('/') + "/"
    val client = HttpClient(getHttpApiClientEngine()) {
        // Report a non-2xx answer (e.g. a reverse proxy refusing the credentials) as the HTTP error
        // it is, rather than as a failure to parse its body.
        expectSuccess = true
        install(ContentNegotiation) {
            json(Serialization.json)
        }
    }

    suspend inline fun <reified T> get(endpoint: String): T {
        val url = baseUrl + if (endpoint.isNotEmpty()) "json/$endpoint" else "json"
        return client.get(url) {
            this@HTTPApi.headers.forEach { (name, value) -> header(name, value) }
        }.body()
    }

    suspend fun close() {
        client.close()
    }

}
