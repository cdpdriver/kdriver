package dev.kdriver.opentelemetry

import dev.kdriver.core.browser.Browser
import dev.kdriver.core.tab.Tab
import io.opentelemetry.api.trace.Tracer

/**
 * Wraps an existing browser with OpenTelemetry instrumentation.
 *
 * This extension function allows you to add tracing to an existing Browser.
 * All tabs opened through the wrapped browser will automatically be traced.
 *
 * @param tracer The OpenTelemetry tracer to use for creating spans.
 * @return An instrumented Browser that wraps this browser.
 *
 * @sample
 * ```kotlin
 * val browser = createBrowser(this, config)
 * val tracedBrowser = browser.withTracing(tracer)
 * ```
 */
fun Browser.withTracing(
    tracer: Tracer,
): Browser {
    return OpenTelemetryBrowser(this, tracer)
}

/**
 * Wraps an existing tab with OpenTelemetry instrumentation.
 *
 * This extension function allows you to add tracing to an existing Tab.
 * All actions performed on the wrapped tab will automatically be traced.
 *
 * @param tracer The OpenTelemetry tracer to use for creating spans.
 * @return An instrumented Tab that wraps this tab.
 *
 * @sample
 * ```kotlin
 * val tab = browser.get("https://example.com")
 * val tracedTab = tab.withTracing(tracer)
 * ```
 */
fun Tab.withTracing(
    tracer: Tracer,
): Tab {
    return OpenTelemetryTab(this, tracer)
}
