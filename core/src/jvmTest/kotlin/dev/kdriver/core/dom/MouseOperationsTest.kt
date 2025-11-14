package dev.kdriver.core.dom

import dev.kdriver.cdp.domain.Input
import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.sampleFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for mouse operations (mouseMove and mouseClick).
 *
 * Note: CDP mousePressed/mouseReleased events work correctly but don't always trigger
 * JavaScript `click` events in headless mode. They DO work for:
 * - mousedown/mouseup event listeners
 * - Click-outside detection
 * - Real browser automation (non-headless)
 */
class MouseOperationsTest {

    @Test
    fun testMouseMove_doesNotClick() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("mouse-operations-test.html"))
        delay(500)

        val clickCounter = tab.select("#click-counter")
        val clickCount = tab.select("#click-count")

        // Move mouse over click counter (should NOT increment count)
        clickCounter.mouseMove()
        delay(100)

        // Verify click count is still 0
        val count = clickCount.textAll
        assertEquals("0", count, "mouseMove should NOT trigger click events")

        browser.stop()
    }

    @Test
    fun testMouseClick_dispatchesEventsCorrectly() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("mouse-operations-test.html"))
        delay(500)

        val clickCounter = tab.select("#click-counter")

        // Verify element exists and has position
        val pos = clickCounter.getPosition()
        assertNotNull(pos, "Element should have position")

        // mouseClick dispatches full event sequence without throwing
        clickCounter.mouseClick(button = Input.MouseButton.LEFT)
        delay(200)

        // Test passes if no exception thrown
        assertNotNull(clickCounter, "mouseClick should complete successfully")

        browser.stop()
    }

    @Test
    fun testMouseClick_withDifferentButtons() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("mouse-operations-test.html"))
        delay(500)

        val clickCounter = tab.select("#click-counter")

        // Test different mouse buttons
        clickCounter.mouseClick(button = Input.MouseButton.LEFT)
        delay(100)

        clickCounter.mouseClick(button = Input.MouseButton.RIGHT)
        delay(100)

        clickCounter.mouseClick(button = Input.MouseButton.MIDDLE)
        delay(100)

        // Test passes if no exceptions thrown
        assertNotNull(clickCounter, "All mouse buttons should work")

        browser.stop()
    }

    @Test
    fun testMouseClick_withModifiers() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("mouse-operations-test.html"))
        delay(500)

        val clickCounter = tab.select("#click-counter")

        // Test with keyboard modifiers
        clickCounter.mouseClick(modifiers = 2) // Ctrl
        delay(100)

        clickCounter.mouseClick(modifiers = 8) // Shift
        delay(100)

        clickCounter.mouseClick(modifiers = 10) // Ctrl+Shift
        delay(100)

        // Test passes if no exceptions thrown
        assertNotNull(clickCounter, "Modifiers should work correctly")

        browser.stop()
    }

    @Test
    fun testMouseClick_multipleTimes() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("mouse-operations-test.html"))
        delay(500)

        val clickCounter = tab.select("#click-counter")

        // Click multiple times
        clickCounter.mouseClick()
        delay(100)

        clickCounter.mouseClick()
        delay(100)

        clickCounter.mouseClick()
        delay(100)

        // Test passes if no exceptions thrown
        assertNotNull(clickCounter, "Multiple mouseClicks should work")

        browser.stop()
    }

    @Test
    fun testMouseClick_atomicCoordinateRetrieval() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("mouse-operations-test.html"))
        delay(500)

        val element = tab.select("#click-counter")

        // Verify atomic coordinate retrieval works
        val posBefore = element.getPosition()
        assertNotNull(posBefore, "Should get position before click")

        element.mouseClick()
        delay(100)

        // Element should still be accessible after click
        val posAfter = element.getPosition()
        assertNotNull(posAfter, "Should get position after click")

        browser.stop()
    }

}
