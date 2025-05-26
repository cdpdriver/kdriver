package dev.kdriver.core.browser

import kotlinx.serialization.Serializable

@Serializable
data class ContraDict(
    val webSocketDebuggerUrl: String,
)
