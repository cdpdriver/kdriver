package dev.kdriver.core.dom

import kotlinx.serialization.Serializable

/**
 * Viewport dimensions and scroll position data.
 * Used for calculating natural scroll gestures.
 */
@Serializable
data class ViewportData(
    val width: Double,
    val height: Double,
    val scrollX: Double = 0.0,
    val scrollY: Double = 0.0,
)
