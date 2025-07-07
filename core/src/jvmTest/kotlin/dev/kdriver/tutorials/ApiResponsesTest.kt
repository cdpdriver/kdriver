package dev.kdriver.tutorials

import dev.kdriver.core.browser.Browser
import dev.kdriver.models.UserData
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiResponsesTest {

    @Test
    fun testResponseBody() = runBlocking {
        val browser = Browser.create(this, headless = true, sandbox = false)

        val page = browser.mainTab ?: return@runBlocking
        val userData = page.expect(Regex(".*/user-data.json")) {
            page.get("https://slensky.com/zendriver-examples/api-request.html")
            getResponseBody<UserData>() // Wait and decode the response body from the matching expectation
        }

        assertEquals("Zendriver", userData.name)

        browser.stop()
    }

}
