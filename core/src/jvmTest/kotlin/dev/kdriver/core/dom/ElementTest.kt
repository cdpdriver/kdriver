package dev.kdriver.core.dom

import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.sampleFile
import kotlinx.coroutines.runBlocking
import kotlin.test.*

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

    @Test
    fun testQuerySelector() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val list = tab.select("ul")
        val firstItem = list.querySelector("li")

        assertNotNull(firstItem)
        assertEquals("li", firstItem.tag)
        browser.stop()
    }

    @Test
    fun testQuerySelectorNotFound() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val list = tab.select("ul")
        val notFound = list.querySelector("table")

        assertNull(notFound)
        browser.stop()
    }

    @Test
    fun testQuerySelectorAll() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val list = tab.select("ul")
        val items = list.querySelectorAll("li")

        assertTrue(items.isNotEmpty())
        assertTrue(items.all { it.tag == "li" })
        browser.stop()
    }

    @Test
    fun testGetChildren() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val list = tab.select("ul")
        val children = list.children

        assertNotNull(children)
        assertTrue(children.isNotEmpty())
        browser.stop()
    }

    @Test
    fun testElementGet() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val element = tab.select("li[aria-label^='Apples']")
        val ariaLabel = element["aria-label"]

        assertNotNull(ariaLabel)
        assertTrue(ariaLabel.contains("Apples"))
        browser.stop()
    }

    @Test
    fun testElementToString() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.get(sampleFile("groceries.html"))

        val element = tab.select("li")
        val str = element.toString()

        assertNotNull(str)
        assertTrue(str.contains("li"))
        browser.stop()
    }

}
