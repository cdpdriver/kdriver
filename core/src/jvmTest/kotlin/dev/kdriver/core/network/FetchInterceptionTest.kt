package dev.kdriver.core.network

import dev.kdriver.cdp.domain.Fetch
import dev.kdriver.cdp.domain.Network
import dev.kdriver.core.browser.createBrowser
import dev.kdriver.core.sampleFile
import dev.kdriver.models.UserData
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchInterceptionTest {

    @Test
    fun testIntercept() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        val userData = tab.intercept(
            "*/user-data.json",
            Fetch.RequestStage.RESPONSE,
            Network.ResourceType.XHR
        ) {
            tab.get(sampleFile("profile.html"))
            val originalResponse = withTimeout(3000L) { getResponseBody<UserData>() }
            withTimeout(3000L) { continueRequest() }
            originalResponse
        }

        assertEquals("Zendriver", userData.name)
        browser.stop()
    }

    @Test
    fun testInterceptWithReload() = runBlocking {
        val browser = createBrowser(this, headless = true, sandbox = false)
        val tab = browser.mainTab ?: error("Main tab is not available")

        val userData = tab.intercept(
            "*/user-data.json",
            Fetch.RequestStage.RESPONSE,
            Network.ResourceType.XHR
        ) {
            tab.get(sampleFile("profile.html"))
            withTimeout(3000L) { continueRequest() }

            reset()
            tab.reload()
            val originalResponse = withTimeout(3000L) { getResponseBody<UserData>() }
            withTimeout(3000L) { continueRequest() }
            originalResponse
        }

        assertEquals("Zendriver", userData.name)
        browser.stop()
    }

}
