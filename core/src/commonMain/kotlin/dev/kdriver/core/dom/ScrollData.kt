package dev.kdriver.core.dom

import kotlinx.serialization.Serializable

/**
 * Result from atomic scroll calculation operation.
 * Used by mouseClick() to determine if scrolling is needed and by how much.
 */
@Serializable
data class ScrollData(
    val x: Double,
    val y: Double,
    val scrollX: Double,
    val scrollY: Double,
    val needsScroll: Boolean,
)
