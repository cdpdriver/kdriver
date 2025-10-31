package dev.kdriver.core.utils

import io.ktor.client.engine.*
import io.ktor.client.engine.winhttp.*

actual fun getWebSocketClientEngine(): HttpClientEngineFactory<*> = WinHttp
actual fun getHttpApiClientEngine(): HttpClientEngineFactory<*> = WinHttp
