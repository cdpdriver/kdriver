package dev.kdriver.core.dom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PositionTest {

    @Test
    fun testPositionFromValidPoints() {
        val points = listOf(10.0, 20.0, 10.0, 120.0, 110.0, 120.0, 110.0, 20.0)
        val position = Position(points)

        assertEquals(10.0, position.left)
        assertEquals(20.0, position.top)
        assertEquals(110.0, position.right)
        assertEquals(120.0, position.bottom)
        assertEquals(10.0, position.x)
        assertEquals(20.0, position.y)
        assertEquals(100.0, position.width)
        assertEquals(100.0, position.height)
        assertEquals(Pair(60.0, 70.0), position.center)
    }

    @Test
    fun testPositionInvalidSize() {
        val points = listOf(10.0, 20.0, 30.0)
        assertFailsWith<IllegalArgumentException> {
            Position(points)
        }
    }

    @Test
    fun testPositionWithAbsoluteCoordinates() {
        val points = listOf(10.0, 20.0, 10.0, 120.0, 110.0, 120.0, 110.0, 20.0)
        val position = Position(points)

        position.absX = 5.0
        position.absY = 15.0

        assertEquals(5.0, position.absX)
        assertEquals(15.0, position.absY)
    }

    @Test
    fun testToViewport() {
        val points = listOf(10.0, 20.0, 10.0, 120.0, 110.0, 120.0, 110.0, 20.0)
        val position = Position(points)

        val viewport = position.toViewport()
        assertEquals(10.0, viewport.x)
        assertEquals(20.0, viewport.y)
        assertEquals(100.0, viewport.width)
        assertEquals(100.0, viewport.height)
        assertEquals(1.0, viewport.scale)
    }

    @Test
    fun testToViewportWithScale() {
        val points = listOf(10.0, 20.0, 10.0, 120.0, 110.0, 120.0, 110.0, 20.0)
        val position = Position(points)

        val viewport = position.toViewport(2.0)
        assertEquals(10.0, viewport.x)
        assertEquals(20.0, viewport.y)
        assertEquals(100.0, viewport.width)
        assertEquals(100.0, viewport.height)
        assertEquals(2.0, viewport.scale)
    }

    @Test
    fun testCoordinateResult() {
        val result = CoordinateResult(100.5, 200.7)
        assertEquals(100.5, result.x)
        assertEquals(200.7, result.y)
    }

    @Test
    fun testPositionData() {
        val data = PositionData(
            left = 10.0,
            top = 20.0,
            right = 110.0,
            bottom = 120.0,
            scrollX = 0.0,
            scrollY = 50.0
        )

        assertEquals(10.0, data.left)
        assertEquals(20.0, data.top)
        assertEquals(110.0, data.right)
        assertEquals(120.0, data.bottom)
        assertEquals(0.0, data.scrollX)
        assertEquals(50.0, data.scrollY)
    }

}
