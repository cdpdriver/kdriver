package dev.kdriver.core.dom

import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.sampleFile
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ElementTest {

    @Test
    fun testInput() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val input = tab.select("#my_input")

        assertEquals("", input.getInputValue())
        input.insertText("Hello World")
        assertEquals("Hello World", input.getInputValue())
        input.clearInput()
        assertEquals("", input.getInputValue())
        input.sendKeys("KDriver")
        assertEquals("KDriver", input.getInputValue())
        input.clearInputByDeleting()
        assertEquals("", input.getInputValue())

        browser.stop()
    }

}
