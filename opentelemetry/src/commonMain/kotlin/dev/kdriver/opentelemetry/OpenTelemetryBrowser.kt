package dev.kdriver.opentelemetry

import dev.kdriver.core.browser.Browser
import dev.kdriver.core.tab.Tab

class OpenTelemetryBrowser(
    private val browser: Browser,
) : Browser by browser {

    override suspend fun get(url: String, newTab: Boolean, newWindow: Boolean): Tab {
        val result = browser.get(url, newTab, newWindow)
        return if (newTab !is OpenTelemetryTab) OpenTelemetryTab(result) else result
    }

}
