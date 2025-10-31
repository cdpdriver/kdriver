package dev.kdriver.core.connection

import io.ktor.client.engine.*

expect fun getWebSocketClientEngine(): HttpClientEngineFactory<*>
expect fun getHttpApiClientEngine(): HttpClientEngineFactory<*>
