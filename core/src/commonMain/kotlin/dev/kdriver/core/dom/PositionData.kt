package dev.kdriver.core.dom

import kotlinx.serialization.Serializable

/**
 * Result from atomic position data retrieval operation.
 * Used by getPosition() to get element bounds and scroll offsets atomically.
 */
@Serializable
data class PositionData(
    val left: Double,
    val top: Double,
    val right: Double,
    val bottom: Double,
    val scrollX: Double,
    val scrollY: Double,
)
