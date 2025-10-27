package dev.kdriver.core.dom

import kotlinx.serialization.Serializable

/**
 * Result from atomic coordinate retrieval operation.
 * Used by mouseMove() to get element center coordinates atomically.
 */
@Serializable
data class CoordinateResult(
    val x: Double,
    val y: Double,
)
