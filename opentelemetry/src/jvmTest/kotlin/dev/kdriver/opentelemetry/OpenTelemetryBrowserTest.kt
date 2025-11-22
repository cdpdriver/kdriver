package dev.kdriver.opentelemetry

import dev.kdriver.core.browser.Browser
import dev.kdriver.core.tab.Tab
import io.mockk.coEvery
import io.mockk.mockk
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OpenTelemetryBrowserTest {

    companion object {
        @JvmField
        @RegisterExtension
        val otelTesting = OpenTelemetryExtension.create()
    }

    @Test
    fun `withTracing wraps existing browser`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val browser = mockk<Browser>()

        // Wrap with tracing
        val tracedBrowser = browser.withTracing(tracer)

        assertNotNull(tracedBrowser)
        assertTrue(tracedBrowser is OpenTelemetryBrowser)
    }

    @Test
    fun `get returns instrumented tab`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true)
        val browser = mockk<Browser> {
            coEvery { get(any(), any(), any()) } returns mockTab
        }

        val tracedBrowser = browser.withTracing(tracer)

        // Execute
        val tab = tracedBrowser.get("https://example.com")

        // Verify
        assertNotNull(tab)
        assertTrue(tab is OpenTelemetryTab)
    }

    @Test
    fun `get with newTab returns instrumented tab`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true)
        val browser = mockk<Browser> {
            coEvery { get(any(), any(), any()) } returns mockTab
        }

        val tracedBrowser = browser.withTracing(tracer)

        // Execute
        val tab = tracedBrowser.get("https://example.com", newTab = true)

        // Verify
        assertNotNull(tab)
        assertTrue(tab is OpenTelemetryTab)
    }

    @Test
    fun `get with newWindow returns instrumented tab`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true)
        val browser = mockk<Browser> {
            coEvery { get(any(), any(), any()) } returns mockTab
        }

        val tracedBrowser = browser.withTracing(tracer)

        // Execute
        val tab = tracedBrowser.get("https://example.com", newWindow = true)

        // Verify
        assertNotNull(tab)
        assertTrue(tab is OpenTelemetryTab)
    }
}
