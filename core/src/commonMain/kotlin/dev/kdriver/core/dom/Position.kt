package dev.kdriver.core.dom

import dev.kdriver.cdp.domain.Page

class Position(points: List<Double>) {

    val left: Double
    val top: Double
    val right: Double
    val bottom: Double

    var absX: Double = 0.0
    var absY: Double = 0.0
    val x: Double
    val y: Double
    val height: Double
    val width: Double
    val center: Pair<Double, Double>

    init {
        require(points.size == 8) { "Points list must have exactly 8 elements" }

        left = points[0]
        top = points[1]
        right = points[4]
        bottom = points[5]

        x = left
        y = top

        height = bottom - top
        width = right - left

        center = Pair(left + width / 2, top + height / 2)
    }

    fun toViewport(scale: Double = 1.0) = Page.Viewport(
        x = x,
        y = y,
        width = width,
        height = height,
        scale = scale
    )

}
