package dev.kdriver.tutorials

import dev.kdriver.cdp.cdp
import dev.kdriver.cdp.domain.Runtime
import dev.kdriver.cdp.domain.runtime
import dev.kdriver.core.browser.Browser
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class CdpTest {

    @Test
    fun testCdpFunctionality() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)
        val page = browser.get("https://slensky.com/zendriver-examples/console.html")

        // Those 4 lines are equivalent and do the same thing
        page.runtime.enable()
        page.cdp.runtime.enable()
        page.send { runtime.enable() }
        page.send { cdp.runtime.enable() }

        // Create a listener for console API calls
        val logs = mutableListOf<String>()
        val consoleHandler: (Runtime.ConsoleAPICalledParameter) -> Unit = { event ->
            logs.add("${event.type} - ${event.args.joinToString(", ") { it.value.toString() }}")
        }

        // Those 4 lines are equivalent and do the same thing
        val job1 = launch { page.runtime.consoleAPICalled.collect { consoleHandler(it) } }
        val job2 = launch { page.cdp.runtime.consoleAPICalled.collect { consoleHandler(it) } }
        val job3 = page.addHandler(this, { runtime.consoleAPICalled }, consoleHandler)
        val job4 = page.addHandler(this, { cdp.runtime.consoleAPICalled }, consoleHandler)

        page.select("#myButton").click()

        page.wait(1000) // Wait for the console messages to be printed

        // Remember to cancel the job to stop listening to console events
        job1.cancel()
        job2.cancel()
        job3.cancel()
        job4.cancel()

        assertEquals(
            List(4) { "log - \"Button clicked!\"" },
            logs
        )

        browser.stop()
    }

}
