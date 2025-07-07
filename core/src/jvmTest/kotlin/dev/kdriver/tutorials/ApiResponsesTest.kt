package dev.kdriver.tutorials

import dev.kdriver.core.browser.Browser
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiResponsesTest {

    @Serializable
    data class UserData(
        val name: String,
        val title: String,
        val email: String,
        val location: String,
        val avatar: String,
        val bio: String,
        val skills: List<String>,
    )

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
