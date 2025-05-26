package dev.kdriver.core.browser

import dev.kaccelero.serializers.Serialization
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*

class HTTPApi(host: String, port: Int) {

    val baseUrl = "http://$host:$port/"
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Serialization.json)
        }
    }

    suspend inline fun <reified T> get(endpoint: String): T {
        val url = baseUrl + if (endpoint.isNotEmpty()) "json/$endpoint" else "json"
        return client.get(url).body()
    }

    suspend fun close() {
        client.close()
    }

}
