package dev.kdriver.core.dom

import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.sampleFile
import dev.kdriver.core.tab.ReadyState
import dev.kdriver.core.tab.evaluate
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.*

/**
 * Tests for race condition resilience in Element operations.
 *
 * These tests verify that DOM operations handle React-like re-renders
 * that can detach/replace elements between CDP operations.
 */
class RaceConditionTest {

    // ============================================================================
    // Phase 2: mouseMove() Tests
    // ============================================================================

    @Test
    fun testMouseMove_normalBehavior() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        // Find the target button
        val button = tab.select("#target-button")
        assertNotNull(button, "Target button should exist")

        // Get position before mouseMove to verify it's visible
        val position = button.getPosition()
        assertNotNull(position, "Button should have position")

        // Perform mouseMove - should succeed on stable DOM
        assertDoesNotThrow("mouseMove should succeed on connected element") {
            button.mouseMove()
        }

        browser.stop()
    }

    @Test
    fun testMouseMove_elementDetachedDuringOperation() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Detach the element
        tab.evaluate<String>("document.getElementById('target-button').remove();'removed'")
        delay(50) // Give time for DOM update

        // After fix: Should handle gracefully by returning early (logging warning)
        // This is acceptable behavior - not throwing an exception but also not performing the action
        assertDoesNotThrow("Should handle detached element gracefully") {
            button.mouseMove()
        }

        browser.stop()
    }

    @Test
    fun testMouseMove_elementReplacedByCloneDuringOperation() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Start mutations that replace element every 100ms
        tab.evaluate<String>("startMutations();'started'")
        delay(50) // Mutations are running

        // After fix: Should handle gracefully with retry or clear error
        // Before fix: May click wrong element or fail silently
        try {
            button.mouseMove()
            // If it succeeds, that's okay (retry worked)
        } catch (e: Exception) {
            // If it fails, should be a clear error about detachment
            assertTrue(
                e.message?.contains("detached", ignoreCase = true) == true ||
                        e.message?.contains("not connected", ignoreCase = true) == true,
                "Error should mention detachment, got: ${e.message}"
            )
        } finally {
            tab.evaluate<String>("stopMutations();'stopped'")
        }

        browser.stop()
    }

    @Test
    fun testMouseMove_invisibleElement() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Make element invisible
        tab.evaluate<String>("document.getElementById('target-button').style.display = 'none';'done'")
        delay(50)

        // Should handle invisible element gracefully (log warning and return)
        assertDoesNotThrow("Should handle invisible element gracefully") {
            button.mouseMove()
        }

        browser.stop()
    }

    // ============================================================================
    // Phase 3: getPosition() Tests
    // ============================================================================

    @Test
    fun testGetPosition_normalBehavior() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Get relative position
        val position = button.getPosition(abs = false)
        assertNotNull(position, "Should get position of visible element")
        assertTrue(position.width > 0, "Width should be positive")
        assertTrue(position.height > 0, "Height should be positive")

        browser.stop()
    }

    @Test
    fun testGetPosition_absolutePositionWithScroll() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Get absolute position
        val position = button.getPosition(abs = true)
        assertNotNull(position, "Should get absolute position")
        assertTrue(position.absX >= 0, "Absolute X should be non-negative")
        assertTrue(position.absY >= 0, "Absolute Y should be non-negative")

        browser.stop()
    }

    @Test
    fun testGetPosition_elementDetached() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Detach element
        tab.evaluate<String>("document.getElementById('target-button').remove();'removed'")
        delay(50)

        // After fix: Should return null or throw clear error
        // Before fix: May throw IndexOutOfBoundsException or return stale data
        val position = button.getPosition()
        assertNull(position, "Should return null for detached element")

        browser.stop()
    }

    @Test
    fun testGetPosition_invisibleElement() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Make invisible
        tab.evaluate<String>("document.getElementById('target-button').style.display = 'none';'done'")
        delay(50)

        val position = button.getPosition()
        assertNull(position, "Should return null for invisible element")

        browser.stop()
    }

    @Test
    fun testGetPosition_elementMovedBetweenCalls() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Start mutations
        tab.evaluate<String>("startMutations();'started'")
        delay(150) // Let mutations happen

        // After fix: Should get atomic snapshot of position
        // Before fix: May get inconsistent position data
        try {
            val position = button.getPosition()
            // If we get a position, it should be valid
            if (position != null) {
                assertTrue(position.width >= 0, "Width should be valid")
                assertTrue(position.height >= 0, "Height should be valid")
            }
        } finally {
            tab.evaluate<String>("stopMutations();'stopped'")
        }

        browser.stop()
    }

    // ============================================================================
    // Phase 4: click() Tests
    // ============================================================================

    @Test
    fun testClick_normalBehavior() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Get initial click count
        val initialCount = tab.evaluate<Int>("clickCount") ?: 0

        // Click should increment counter
        button.click()
        delay(100)

        val newCount = tab.evaluate<Int>("clickCount") ?: 0
        assertEquals(initialCount + 1, newCount, "Click should increment counter")

        browser.stop()
    }

    @Test
    fun testClick_elementDetached() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Detach element
        tab.evaluate<String>("document.getElementById('target-button').remove();'removed'")
        delay(50)

        // After fix: Should throw clear error
        // Before fix: May fail silently or throw cryptic error
        val exception = assertFailsWith<Exception>("Should fail on detached element") {
            button.click()
        }

        assertTrue(
            exception.message?.contains("detached", ignoreCase = true) == true ||
                    exception.message?.contains("not connected", ignoreCase = true) == true ||
                    exception is EvaluateException,
            "Should throw appropriate error for detached element"
        )

        browser.stop()
    }

    @Test
    fun testClick_elementReplacedByClone() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Replace with clone (simulates React re-render)
        tab.evaluate<String>(
            """
            const btn = document.getElementById('target-button');
            const clone = btn.cloneNode(true);
            clone.onclick = handleClick;
            btn.parentNode.replaceChild(clone, btn);
        """.trimIndent() + ";'done'"
        )
        delay(50)

        // After fix: Should throw error about detachment
        // Before fix: May click old detached element (no effect)
        assertFailsWith<Exception>("Should fail on replaced element") {
            button.click()
        }

        browser.stop()
    }

    // ============================================================================
    // Phase 5: rawApply() Tests
    // ============================================================================

    @Test
    fun testRawApply_normalBehavior() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Apply function to get button text
        val result = button.apply<String>("(elem) => elem.textContent")
        assertNotNull(result)
        assertTrue(result.contains("Click Me"), "Should get button text")

        browser.stop()
    }

    @Test
    fun testRawApply_elementDetached() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Detach element
        tab.evaluate<String>("document.getElementById('target-button').remove();'removed'")
        delay(50)

        // After fix: Should throw clear error about detachment
        // Before fix: May execute on detached element or fail cryptically
        val exception = assertFailsWith<Exception>("Should fail on detached element") {
            button.apply<String>("(elem) => elem.textContent")
        }

        assertTrue(
            exception.message?.contains("detached", ignoreCase = true) == true ||
                    exception.message?.contains("not connected", ignoreCase = true) == true ||
                    exception is EvaluateException,
            "Should indicate element detachment"
        )

        browser.stop()
    }

    @Test
    fun testRawApply_withAwaitPromise() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Apply async function
        val result = button.apply<String>(
            jsFunction = """
                async (elem) => {
                    await new Promise(resolve => setTimeout(resolve, 50));
                    return elem.id;
                }
            """.trimIndent(),
            awaitPromise = true
        )

        assertEquals("target-button", result, "Should await promise and return result")

        browser.stop()
    }

    @Test
    fun testRawApply_elementDetachedDuringPromise() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val button = tab.select("#target-button")
        assertNotNull(button)

        // Start async operation, then detach element while promise is pending
        try {
            // This async function takes 200ms
            val result = coroutineScope {
                val resultJob = async {
                    button.apply<String>(
                        jsFunction = """
                            async (elem) => {
                                await new Promise(resolve => setTimeout(resolve, 200));
                                return elem.isConnected ? 'connected' : 'detached';
                            }
                        """.trimIndent(),
                        awaitPromise = true
                    )
                }

                // Detach element while promise is pending
                delay(50)
                tab.evaluate<String>("document.getElementById('target-button').remove();'removed'")

                // Wait for result
                resultJob.await()
            }

            // Element was detached during promise execution
            // After fix: Should detect detachment
            assertEquals("detached", result, "Should detect element was detached during async operation")

        } catch (e: Exception) {
            // Throwing exception is also acceptable behavior
            assertTrue(
                e.message?.contains("detached", ignoreCase = true) == true ||
                        e is EvaluateException,
                "Exception should indicate detachment"
            )
        }

        browser.stop()
    }

    // ============================================================================
    // Phase 6: Input Method Tests
    // ============================================================================

    @Test
    fun testFocus_normalBehavior() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val input = tab.select("#test-input")
        assertNotNull(input)

        // Focus should succeed
        assertDoesNotThrow("Focus should succeed on connected element") {
            input.focus()
        }

        // Verify focus
        delay(50)
        val isFocused = tab.evaluate<Boolean>("document.activeElement.id === 'test-input'")
        assertTrue(isFocused == true, "Input should be focused")

        browser.stop()
    }

    @Test
    fun testFocus_elementDetached() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val input = tab.select("#test-input")
        assertNotNull(input)

        // Detach element
        tab.evaluate<String>("document.getElementById('test-input').remove();'removed'")
        delay(50)

        // After fix: Should throw clear error
        assertFailsWith<Exception>("Should fail on detached element") {
            input.focus()
        }

        browser.stop()
    }

    @Test
    fun testSendKeys_normalBehavior() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val input = tab.select("#test-input")
        assertNotNull(input)

        // Send keys
        input.sendKeys("Hello")
        delay(100)

        val value = input.getInputValue()
        assertEquals("Hello", value, "Input should contain typed text")

        browser.stop()
    }

    @Test
    fun testSendKeys_elementDetachedBetweenFocusAndType() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val input = tab.select("#test-input")
        assertNotNull(input)

        // This is tricky: we need to detach element between focus() and actual key dispatch
        // For now, just test that detached element fails
        tab.evaluate<String>("document.getElementById('test-input').remove();'removed'")
        delay(50)

        assertFailsWith<Exception>("Should fail on detached element") {
            input.sendKeys("Hello")
        }

        browser.stop()
    }

    @Test
    fun testInsertText_normalBehavior() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val input = tab.select("#test-input")
        assertNotNull(input)

        // Insert text
        input.insertText("World")
        delay(100)

        val value = input.getInputValue()
        assertEquals("World", value, "Input should contain inserted text")

        browser.stop()
    }

    @Test
    fun testInsertText_elementDetached() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("race-condition-test.html"))
        tab.waitForReadyState(ReadyState.COMPLETE)

        val input = tab.select("#test-input")
        assertNotNull(input)

        tab.evaluate<String>("document.getElementById('test-input').remove();'removed'")
        delay(50)

        assertFailsWith<Exception>("Should fail on detached element") {
            input.insertText("World")
        }

        browser.stop()
    }

    // ============================================================================
    // Helper Functions
    // ============================================================================

    private suspend fun <T> assertDoesNotThrow(message: String, block: suspend () -> T): T {
        return try {
            block()
        } catch (e: Exception) {
            fail("$message (threw ${e::class.simpleName}: ${e.message})")
        }
    }

}
