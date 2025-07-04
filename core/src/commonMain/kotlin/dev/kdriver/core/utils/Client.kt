package dev.kdriver.core.utils

import io.ktor.client.engine.*

expect fun getWebSocketClientEngine(): HttpClientEngineFactory<*>
expect fun getHttpApiClientEngine(): HttpClientEngineFactory<*>
