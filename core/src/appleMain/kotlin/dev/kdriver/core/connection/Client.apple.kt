package dev.kdriver.core.connection

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun getWebSocketClientEngine(): HttpClientEngineFactory<*> = Darwin
actual fun getHttpApiClientEngine(): HttpClientEngineFactory<*> = Darwin
