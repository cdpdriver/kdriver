package dev.kdriver.core.dom

import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.sampleFile
import dev.kdriver.core.tab.ReadyState
import dev.kdriver.core.tab.evaluate
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that reproduce the React controlled input silent failure bug.
 *
 * React installs an instance-level value property setter on controlled inputs
 * (its _valueTracker mechanism). Direct `el.value = x` assignments go through
 * this setter, updating the tracker's "last known value". When an 'input' event
 * then fires, React checks `el.value === tracker.getValue()` — they match —
 * so React concludes nothing changed and does NOT call onChange.
 *
 * kdriver's clearInput() and clearInputByDeleting() both use direct `.value`
 * assignments, making them silently ineffective against React controlled inputs.
 * The real-world consequence is a "mixed value" when filling a pre-filled input
 * (e.g. old value "10", new value "25" → result "1025").
 */
class ReactControlledInputTest {

    /**
     * clearInput() uses `element.value = ""` which goes through React's tracker setter,
     * updating both the DOM and trackerValue to "". No 'input' event is dispatched,
     * so React's onChange check never runs. React state stays at "10".
     */
    @Test
    fun testClearInputDoesNotNotifyReact() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("react-controlled-input-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val input = tab.select("#controlled-input")

        assertEquals("10", tab.evaluate<String>("document.getElementById('state-value').textContent"))
        assertEquals("0", tab.evaluate<String>("document.getElementById('change-count').textContent"))

        input.clearInput()
        delay(100)

        // Expected: React state is "" (the clear was communicated to React)
        // Actual:   React state is still "10" (React was never notified)
        assertEquals("", tab.evaluate<String>("document.getElementById('state-value').textContent"))

        browser.stop()
    }

    /**
     * clearInputByDeleting() uses `el.value = el.value.slice(1)` on each iteration.
     * Each direct .value assignment goes through React's tracker setter, updating both
     * the DOM and trackerValue simultaneously. When the 'input' event fires afterwards,
     * React checks `el.value === trackerValue` — they match — so onChange is never called.
     * React state stays at "10".
     */
    @Test
    fun testClearInputByDeletingDoesNotNotifyReact() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("react-controlled-input-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val input = tab.select("#controlled-input")

        assertEquals("10", tab.evaluate<String>("document.getElementById('state-value').textContent"))
        assertEquals("0", tab.evaluate<String>("document.getElementById('change-count').textContent"))

        input.clearInputByDeleting()
        delay(100)

        // Expected: React state is "" (every deletion was communicated to React)
        // Actual:   React state is still "10" (silent failure — tracker matched on each event)
        assertEquals("", tab.evaluate<String>("document.getElementById('state-value').textContent"))

        browser.stop()
    }

    /**
     * The real-world consequence: after clearInputByDeleting silently fails to notify React,
     * React's async scheduler re-renders and restores the DOM to its controlled value ("10").
     * insertText("25") then inserts into "10" instead of an empty field, producing a mixed
     * value like "1025" instead of the intended "25".
     */
    @Test
    fun testFillReactControlledInputProducesMixedValue() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("react-controlled-input-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val input = tab.select("#controlled-input")

        // Step 1: Try to clear — silently fails at React level, DOM appears empty
        input.clearInputByDeleting()

        // Step 2: Simulate React's async re-render committing the old controlled state.
        // In a real app this happens automatically (React's scheduler) between operations.
        // React uses the native prototype setter to revert the DOM to "10".
        tab.evaluate<String>("window.simulateReactRerender();'done'")
        delay(50)

        // Step 3: Insert the new value — but DOM now holds "10", not ""
        input.insertText("25")
        delay(100)

        // Expected: "25"   (clear worked, so inserting into empty field gives "25")
        // Actual:   "1025" (inserted at end of "10" that React restored)
        assertEquals("25", input.getInputValue())

        browser.stop()
    }

}
