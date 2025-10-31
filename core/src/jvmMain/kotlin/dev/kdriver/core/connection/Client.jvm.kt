package dev.kdriver.core.connection

import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.engine.cio.*

actual fun getWebSocketClientEngine(): HttpClientEngineFactory<*> = CIO
actual fun getHttpApiClientEngine(): HttpClientEngineFactory<*> = Apache
