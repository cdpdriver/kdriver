package dev.kdriver.core.utils

import io.ktor.client.engine.*
import io.ktor.client.engine.curl.*

actual fun getWebSocketClientEngine(): HttpClientEngineFactory<*> = Curl
actual fun getHttpApiClientEngine(): HttpClientEngineFactory<*> = Curl
