package dev.kdriver.opentelemetry

import dev.kdriver.cdp.domain.Fetch
import dev.kdriver.cdp.domain.Network
import dev.kdriver.core.network.BatchRequestExpectation
import dev.kdriver.core.network.FetchInterception
import dev.kdriver.core.network.RequestExpectation
import dev.kdriver.core.tab.Tab
import io.mockk.coEvery
import io.mockk.mockk
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class OpenTelemetryTabTest {

    companion object {
        @JvmField
        @RegisterExtension
        val otelTesting = OpenTelemetryExtension.create()
    }

    @Test
    fun `withTracing wraps existing tab`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val tab = mockk<Tab>(relaxed = true)

        // Wrap with tracing
        val tracedTab = tab.withTracing(tracer)

        assertNotNull(tracedTab)
        assert(tracedTab is OpenTelemetryTab)
    }

    @Test
    fun `get adds event to current span`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery { get(any(), any(), any()) } returns this
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Create a parent span to capture events
        val parentSpan = tracer.spanBuilder("test-parent")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan()

        withContext(parentSpan.asContextElement()) {
            try {
                // Execute
                tracedTab.get("https://example.com", newTab = false, newWindow = false)

                // The event should be added to the current span
                parentSpan.setStatus(StatusCode.OK)
            } finally {
                parentSpan.end()
            }
        }

        // Verify the parent span was created
        val spans = otelTesting.spans
        val testSpan = spans.find { it.name == "test-parent" }
        assertNotNull(testSpan)

        // Verify the event was added
        val events = testSpan.events
        val getEvent = events.find { it.name == "kdriver.tab.get" }
        assertNotNull(getEvent, "get event should be added to current span")

        val attributes = getEvent.attributes.asMap()
        assertEquals("https://example.com", attributes[stringKey("url")])
        assertEquals(false, attributes[boolKey("newTab")])
        assertEquals(false, attributes[boolKey("newWindow")])
    }

    @Test
    fun `back adds event to current span`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery { back() } returns Unit
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Create a parent span
        val parentSpan = tracer.spanBuilder("test-parent")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan()

        withContext(parentSpan.asContextElement()) {
            try {
                tracedTab.back()
                parentSpan.setStatus(StatusCode.OK)
            } finally {
                parentSpan.end()
            }
        }

        // Verify event
        val testSpan = otelTesting.spans.find { it.name == "test-parent" }
        assertNotNull(testSpan)
        val backEvent = testSpan.events.find { it.name == "kdriver.tab.back" }
        assertNotNull(backEvent, "back event should be added to current span")
    }

    @Test
    fun `forward adds event to current span`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery { forward() } returns Unit
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Create a parent span
        val parentSpan = tracer.spanBuilder("test-parent")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan()

        withContext(parentSpan.asContextElement()) {
            try {
                tracedTab.forward()
                parentSpan.setStatus(StatusCode.OK)
            } finally {
                parentSpan.end()
            }
        }

        // Verify event
        val testSpan = otelTesting.spans.find { it.name == "test-parent" }
        assertNotNull(testSpan)
        val forwardEvent = testSpan.events.find { it.name == "kdriver.tab.forward" }
        assertNotNull(forwardEvent, "forward event should be added to current span")
    }

    @Test
    fun `reload adds event with attributes to current span`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery { reload(any(), any()) } returns Unit
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Create a parent span
        val parentSpan = tracer.spanBuilder("test-parent")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan()

        withContext(parentSpan.asContextElement()) {
            try {
                tracedTab.reload(ignoreCache = true, scriptToEvaluateOnLoad = null)
                parentSpan.setStatus(StatusCode.OK)
            } finally {
                parentSpan.end()
            }
        }

        // Verify event
        val testSpan = otelTesting.spans.find { it.name == "test-parent" }
        assertNotNull(testSpan)
        val reloadEvent = testSpan.events.find { it.name == "kdriver.tab.reload" }
        assertNotNull(reloadEvent, "reload event should be added to current span")

        val attributes = reloadEvent.attributes.asMap()
        assertEquals(true, attributes[boolKey("ignoreCache")])
    }

    @Test
    fun `expect creates span with correct attributes`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val urlPattern = Regex("https://example\\.com/.*")
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery { expect(any<Regex>(), any<suspend RequestExpectation.() -> String>()) } coAnswers {
                val block = secondArg<suspend RequestExpectation.() -> String>()
                val mockExpectation = mockk<RequestExpectation>(relaxed = true)
                block(mockExpectation)
            }
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Execute
        val result = tracedTab.expect(urlPattern) { "test-result" }

        // Verify
        assertEquals("test-result", result)

        val spans = otelTesting.spans
        val expectSpan = spans.find { it.name == "kdriver.tab.expect" }
        assertNotNull(expectSpan, "expect span should be created")
        assertEquals(SpanKind.INTERNAL, expectSpan.kind)
        assertEquals(StatusCode.OK, expectSpan.status.statusCode)

        val attributes = expectSpan.attributes.asMap()
        assertEquals("https://example\\.com/.*", attributes[stringKey("urlPattern")])
    }

    @Test
    fun `expectBatch creates span with correct attributes`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val urlPatterns = listOf(Regex("https://example\\.com/.*"), Regex("https://test\\.com/.*"))
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery { expectBatch(any(), any<suspend BatchRequestExpectation.() -> String>()) } coAnswers {
                val block = secondArg<suspend BatchRequestExpectation.() -> String>()
                val mockExpectation = mockk<BatchRequestExpectation>(relaxed = true)
                block(mockExpectation)
            }
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Execute
        val result = tracedTab.expectBatch(urlPatterns) { "batch-result" }

        // Verify
        assertEquals("batch-result", result)

        val spans = otelTesting.spans
        val expectBatchSpan = spans.find { it.name == "kdriver.tab.expectBatch" }
        assertNotNull(expectBatchSpan, "expectBatch span should be created")
        assertEquals(SpanKind.INTERNAL, expectBatchSpan.kind)
        assertEquals(StatusCode.OK, expectBatchSpan.status.statusCode)

        val attributes = expectBatchSpan.attributes.asMap()
        assertEquals("https://example\\.com/.*,https://test\\.com/.*", attributes[stringKey("urlPatterns")])
    }

    @Test
    fun `intercept creates span with correct attributes`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery {
                intercept(
                    any(),
                    any(),
                    any(),
                    any<suspend FetchInterception.() -> String>()
                )
            } coAnswers {
                val block = arg<suspend FetchInterception.() -> String>(3)
                val mockInterception = mockk<FetchInterception>(relaxed = true)
                block(mockInterception)
            }
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Execute
        val result = tracedTab.intercept(
            urlPattern = "https://example.com/*",
            requestStage = Fetch.RequestStage.REQUEST,
            resourceType = Network.ResourceType.DOCUMENT
        ) { "intercept-result" }

        // Verify
        assertEquals("intercept-result", result)

        val spans = otelTesting.spans
        val interceptSpan = spans.find { it.name == "kdriver.tab.intercept" }
        assertNotNull(interceptSpan, "intercept span should be created")
        assertEquals(SpanKind.INTERNAL, interceptSpan.kind)
        assertEquals(StatusCode.OK, interceptSpan.status.statusCode)

        val attributes = interceptSpan.attributes.asMap()
        assertEquals("https://example.com/*", attributes[stringKey("urlPattern")])
        assertEquals("REQUEST", attributes[stringKey("requestStage")])
        assertEquals("DOCUMENT", attributes[stringKey("resourceType")])
    }

    @Test
    fun `expect handles errors correctly`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val urlPattern = Regex("https://example\\.com/.*")
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery { expect(any<Regex>(), any<suspend RequestExpectation.() -> String>()) } coAnswers {
                val block = secondArg<suspend RequestExpectation.() -> String>()
                val mockExpectation = mockk<RequestExpectation>(relaxed = true)
                block(mockExpectation)
            }
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Execute and verify exception is thrown
        assertFailsWith<RuntimeException> {
            tracedTab.expect(urlPattern) {
                throw RuntimeException("Test error")
            }
        }

        // Verify error span was recorded
        val spans = otelTesting.spans
        val errorSpan = spans.find {
            it.name == "kdriver.tab.expect" && it.status.statusCode == StatusCode.ERROR
        }

        assertNotNull(errorSpan, "Error span should have been recorded")
        assertEquals("Test error", errorSpan.status.description)
    }

    @Test
    fun `expectBatch handles errors correctly`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val urlPatterns = listOf(Regex("https://example\\.com/.*"))
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery { expectBatch(any(), any<suspend BatchRequestExpectation.() -> String>()) } coAnswers {
                val block = secondArg<suspend BatchRequestExpectation.() -> String>()
                val mockExpectation = mockk<BatchRequestExpectation>(relaxed = true)
                block(mockExpectation)
            }
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Execute and verify exception is thrown
        assertFailsWith<RuntimeException> {
            tracedTab.expectBatch(urlPatterns) {
                throw RuntimeException("Batch error")
            }
        }

        // Verify error span was recorded
        val spans = otelTesting.spans
        val errorSpan = spans.find {
            it.name == "kdriver.tab.expectBatch" && it.status.statusCode == StatusCode.ERROR
        }

        assertNotNull(errorSpan, "Error span should have been recorded")
        assertEquals("Batch error", errorSpan.status.description)
    }

    @Test
    fun `intercept handles errors correctly`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery {
                intercept(
                    any(),
                    any(),
                    any(),
                    any<suspend FetchInterception.() -> String>()
                )
            } coAnswers {
                val block = arg<suspend FetchInterception.() -> String>(3)
                val mockInterception = mockk<FetchInterception>(relaxed = true)
                block(mockInterception)
            }
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Execute and verify exception is thrown
        assertFailsWith<RuntimeException> {
            tracedTab.intercept(
                urlPattern = "https://example.com/*",
                requestStage = Fetch.RequestStage.REQUEST,
                resourceType = Network.ResourceType.DOCUMENT
            ) {
                throw RuntimeException("Intercept error")
            }
        }

        // Verify error span was recorded
        val spans = otelTesting.spans
        val errorSpan = spans.find {
            it.name == "kdriver.tab.intercept" && it.status.statusCode == StatusCode.ERROR
        }

        assertNotNull(errorSpan, "Error span should have been recorded")
        assertEquals("Intercept error", errorSpan.status.description)
    }

    @Test
    fun `context propagation works across coroutine suspensions`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery { expect(any<Regex>(), any<suspend RequestExpectation.() -> String>()) } coAnswers {
                val block = secondArg<suspend RequestExpectation.() -> String>()
                val mockExpectation = mockk<RequestExpectation>(relaxed = true)
                block(mockExpectation)
            }
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Execute with multiple suspensions
        tracedTab.expect(Regex("https://example\\.com/.*")) {
            // Create child span to verify context is propagated
            val childSpan = tracer.spanBuilder("child-operation")
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan()

            withContext(childSpan.asContextElement()) {
                try {
                    // Multiple suspension points
                    delay(10)
                    delay(10)
                    childSpan.setStatus(StatusCode.OK)
                } finally {
                    childSpan.end()
                }
            }

            "result"
        }

        // Verify span structure
        val spans = otelTesting.spans
        val parentSpan = spans.find { it.name == "kdriver.tab.expect" }
        val childSpan = spans.find { it.name == "child-operation" }

        assertNotNull(parentSpan, "Parent span should be created")
        assertNotNull(childSpan, "Child span should be created")

        // Verify they're in the same trace
        assertEquals(parentSpan.traceId, childSpan.traceId, "Spans should share same trace ID")
        assertEquals(parentSpan.spanId, childSpan.parentSpanId, "Child should have parent as parent")
    }

    @Test
    fun `mouseClick adds event with correct attributes`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val mockTab = mockk<Tab>(relaxed = true) {
            coEvery { mouseClick(any(), any(), any(), any(), any()) } returns Unit
        }
        val tracedTab = mockTab.withTracing(tracer)

        // Create a parent span
        val parentSpan = tracer.spanBuilder("test-parent")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan()

        withContext(parentSpan.asContextElement()) {
            try {
                tracedTab.mouseClick(100.0, 200.0, dev.kdriver.cdp.domain.Input.MouseButton.LEFT, 1, 0)
                parentSpan.setStatus(StatusCode.OK)
            } finally {
                parentSpan.end()
            }
        }

        // Verify event
        val testSpan = otelTesting.spans.find { it.name == "test-parent" }
        assertNotNull(testSpan)
        val clickEvent = testSpan.events.find { it.name == "kdriver.tab.mouseClick" }
        assertNotNull(clickEvent, "mouseClick event should be added to current span")

        val attributes = clickEvent.attributes.asMap()
        assertEquals(100.0, attributes[doubleKey("x")])
        assertEquals(200.0, attributes[doubleKey("y")])
        assertEquals("LEFT", attributes[stringKey("button")])
    }

    // Helper functions to create OpenTelemetry AttributeKeys
    private fun stringKey(name: String) = io.opentelemetry.api.common.AttributeKey.stringKey(name)
    private fun boolKey(name: String) = io.opentelemetry.api.common.AttributeKey.booleanKey(name)
    private fun doubleKey(name: String) = io.opentelemetry.api.common.AttributeKey.doubleKey(name)
}
