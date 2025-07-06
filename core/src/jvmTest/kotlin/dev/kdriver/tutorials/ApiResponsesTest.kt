package dev.kdriver.tutorials

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.domain.network
import dev.kdriver.core.browser.Browser
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiResponsesTest {

    @Test
    fun testResponse() = runBlocking {
        val browser = Browser.create(this, headless = true)

        val page = browser.mainTab ?: return@runBlocking
        val response = page.expect(Regex(".*/user-data.json")) {
            page.get("https://slensky.com/zendriver-examples/api-request.html")
            getResponseEvent()
        }

        val requestId = response.requestId
        val body = page.network.getResponseBody(requestId).body
        val userData = Serialization.json.parseToJsonElement(body)

        assertEquals("Zendriver", userData.jsonObject["name"]?.jsonPrimitive?.content)

        browser.stop()
    }

    @Test
    fun testResponseBody() = runBlocking {
        val browser = Browser.create(this, headless = true)

        val page = browser.mainTab ?: return@runBlocking
        val body = page.expect(Regex(".*/user-data.json")) {
            page.get("https://slensky.com/zendriver-examples/api-request.html")
            getResponseBody().body
        }
        val userData = Serialization.json.parseToJsonElement(body)

        assertEquals("Zendriver", userData.jsonObject["name"]?.jsonPrimitive?.content)

        browser.stop()
    }

}
