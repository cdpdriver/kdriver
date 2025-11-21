package dev.kdriver.opentelemetry

import dev.kdriver.core.browser.Browser
import dev.kdriver.core.tab.Tab
import io.opentelemetry.api.trace.Tracer

class OpenTelemetryBrowser(
    private val browser: Browser,
    private val tracer: Tracer,
) : Browser by browser {

    override suspend fun get(url: String, newTab: Boolean, newWindow: Boolean): Tab {
        val result = browser.get(url, newTab, newWindow)
        return if (newTab !is OpenTelemetryTab) OpenTelemetryTab(result, tracer) else result
    }

}
