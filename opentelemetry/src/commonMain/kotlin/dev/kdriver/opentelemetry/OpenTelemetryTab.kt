package dev.kdriver.opentelemetry

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.InternalCdpApi
import dev.kdriver.cdp.Message
import dev.kdriver.cdp.domain.*
import dev.kdriver.cdp.domain.Target
import dev.kdriver.core.dom.Element
import dev.kdriver.core.dom.NodeOrElement
import dev.kdriver.core.network.BatchRequestExpectation
import dev.kdriver.core.network.FetchInterception
import dev.kdriver.core.network.RequestExpectation
import dev.kdriver.core.tab.ReadyState
import dev.kdriver.core.tab.ScreenshotFormat
import dev.kdriver.core.tab.Tab
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.serialization.json.JsonElement
import kotlin.reflect.KClass

class OpenTelemetryTab(
    private val tab: Tab,
    private val tracer: Tracer,
) : Tab {

    override var lastMouseX: Double?
        get() = tab.lastMouseX
        set(value) {
            tab.lastMouseX = value
        }

    override var lastMouseY: Double?
        get() = tab.lastMouseY
        set(value) {
            tab.lastMouseY = value
        }

    override suspend fun get(
        url: String,
        newTab: Boolean,
        newWindow: Boolean,
    ): Tab = tab.get(url, newTab, newWindow).also {
        Span.current().addEvent(
            "kdriver.tab.get", Attributes.builder()
                .put("url", url)
                .put("newTab", newTab)
                .put("newWindow", newWindow)
                .build()
        )
    }

    override suspend fun back() = tab.back().also {
        Span.current().addEvent("kdriver.tab.back")
    }

    override suspend fun forward() = tab.forward().also {
        Span.current().addEvent("kdriver.tab.forward")
    }

    override suspend fun reload(
        ignoreCache: Boolean,
        scriptToEvaluateOnLoad: String?,
    ) = tab.reload(ignoreCache, scriptToEvaluateOnLoad).also {
        Span.current().addEvent(
            "kdriver.tab.reload", Attributes.builder()
                .put("ignoreCache", ignoreCache)
                .build()
        )
    }

    override suspend fun rawEvaluate(
        expression: String,
        awaitPromise: Boolean,
    ): JsonElement? = tab.rawEvaluate(expression, awaitPromise).also {
        Span.current().addEvent(
            "kdriver.tab.rawEvaluate", Attributes.builder()
                .put("expression", expression)
                .put("awaitPromise", awaitPromise)
                .build()
        )
    }

    override suspend fun setUserAgent(
        userAgent: String?,
        acceptLanguage: String?,
        platform: String?,
    ) = tab.setUserAgent(userAgent, acceptLanguage, platform).also {
        Span.current().addEvent(
            "kdriver.tab.setUserAgent", Attributes.builder()
                .put("userAgent", userAgent)
                .put("acceptLanguage", acceptLanguage)
                .put("platform", platform)
                .build()
        )
    }

    override suspend fun getWindow(): Browser.GetWindowForTargetReturn = tab.getWindow()

    override suspend fun getContent(): String = tab.getContent()

    override suspend fun activate() = tab.activate().also {
        Span.current().addEvent("kdriver.tab.activate")
    }

    override suspend fun bringToFront() = tab.bringToFront().also {
        Span.current().addEvent("kdriver.tab.bringToFront")
    }

    override suspend fun maximize() = tab.maximize().also {
        Span.current().addEvent("kdriver.tab.maximize")
    }

    override suspend fun minimize() = tab.minimize().also {
        Span.current().addEvent("kdriver.tab.minimize")
    }

    override suspend fun fullscreen() = tab.fullscreen().also {
        Span.current().addEvent("kdriver.tab.fullscreen")
    }

    override suspend fun medimize() = tab.medimize().also {
        Span.current().addEvent("kdriver.tab.medimize")
    }

    override suspend fun setWindowState(
        left: Int,
        top: Int,
        width: Int,
        height: Int,
        state: String,
    ) = tab.setWindowState(left, top, width, height, state).also {
        Span.current().addEvent(
            "kdriver.tab.setWindowState", Attributes.builder()
                .put("left", left.toLong())
                .put("top", top.toLong())
                .put("width", width.toLong())
                .put("height", height.toLong())
                .put("state", state)
                .build()
        )
    }

    override suspend fun scrollDown(amount: Int, speed: Int) = tab.scrollDown(amount, speed).also {
        Span.current().addEvent(
            "kdriver.tab.scrollDown", Attributes.builder()
                .put("amount", amount.toLong())
                .put("speed", speed.toLong())
                .build()
        )
    }

    override suspend fun scrollUp(amount: Int, speed: Int) = tab.scrollUp(amount, speed).also {
        Span.current().addEvent(
            "kdriver.tab.scrollUp", Attributes.builder()
                .put("amount", amount.toLong())
                .put("speed", speed.toLong())
                .build()
        )
    }

    override suspend fun scrollTo(scrollX: Double, scrollY: Double, speed: Int?) =
        tab.scrollTo(scrollX, scrollY, speed).also {
            Span.current().addEvent(
                "kdriver.tab.scrollTo", Attributes.builder()
                    .put("scrollX", scrollX)
                    .put("scrollY", scrollY)
                    .put("speed", speed?.toLong() ?: 0L)
                    .build()
            )
        }

    override suspend fun waitForReadyState(
        until: ReadyState,
        timeout: Long,
    ): Boolean = tab.waitForReadyState(until, timeout).also {
        Span.current().addEvent(
            "kdriver.tab.waitForReadyState", Attributes.builder()
                .put("until", until.name)
                .put("timeout", timeout)
                .build()
        )
    }

    override suspend fun find(
        text: String,
        bestMatch: Boolean,
        returnEnclosingElement: Boolean,
        timeout: Long,
    ): Element = tab.find(text, bestMatch, returnEnclosingElement, timeout).also {
        Span.current().addEvent(
            "kdriver.tab.find", Attributes.builder()
                .put("text", text)
                .put("bestMatch", bestMatch)
                .put("returnEnclosingElement", returnEnclosingElement)
                .put("timeout", timeout)
                .build()
        )
    }

    override suspend fun select(selector: String, timeout: Long): Element = tab.select(selector, timeout).also {
        Span.current().addEvent(
            "kdriver.tab.select", Attributes.builder()
                .put("selector", selector)
                .put("timeout", timeout)
                .build()
        )
    }

    override suspend fun findAll(
        text: String,
        timeout: Long,
    ): List<Element> = tab.findAll(text, timeout).also {
        Span.current().addEvent(
            "kdriver.tab.findAll", Attributes.builder()
                .put("text", text)
                .put("timeout", timeout)
                .build()
        )
    }

    override suspend fun selectAll(
        selector: String,
        timeout: Long,
        includeFrames: Boolean,
    ): List<Element> = tab.selectAll(selector, timeout, includeFrames).also {
        Span.current().addEvent(
            "kdriver.tab.selectAll", Attributes.builder()
                .put("selector", selector)
                .put("timeout", timeout)
                .put("includeFrames", includeFrames)
                .build()
        )
    }

    override suspend fun xpath(
        xpath: String,
        timeout: Long,
    ): List<Element> = tab.xpath(xpath, timeout).also {
        Span.current().addEvent(
            "kdriver.tab.xpath", Attributes.builder()
                .put("xpath", xpath)
                .put("timeout", timeout)
                .build()
        )
    }

    override suspend fun querySelectorAll(
        selector: String,
        node: NodeOrElement?,
    ): List<Element> = tab.querySelectorAll(selector, node).also {
        Span.current().addEvent(
            "kdriver.tab.querySelectorAll", Attributes.builder()
                .put("selector", selector)
                .build()
        )
    }

    override suspend fun querySelector(
        selector: String,
        node: NodeOrElement?,
    ): Element? = tab.querySelector(selector, node).also {
        Span.current().addEvent(
            "kdriver.tab.querySelector", Attributes.builder()
                .put("selector", selector)
                .build()
        )
    }

    override suspend fun findElementsByText(
        text: String,
        tagHint: String?,
    ): List<Element> = tab.findElementsByText(text, tagHint).also {
        Span.current().addEvent(
            "kdriver.tab.findElementsByText", Attributes.builder()
                .put("text", text)
                .put("tagHint", tagHint ?: "null")
                .build()
        )
    }

    override suspend fun findElementByText(
        text: String,
        bestMatch: Boolean,
        returnEnclosingElement: Boolean,
    ): Element? = tab.findElementByText(text, bestMatch, returnEnclosingElement).also {
        Span.current().addEvent(
            "kdriver.tab.findElementByText", Attributes.builder()
                .put("text", text)
                .put("bestMatch", bestMatch)
                .put("returnEnclosingElement", returnEnclosingElement)
                .build()
        )
    }

    override suspend fun disableDomAgent() = tab.disableDomAgent().also {
        Span.current().addEvent("kdriver.tab.disableDomAgent")
    }

    override suspend fun mouseMove(
        x: Double,
        y: Double,
        steps: Int,
        flash: Boolean,
    ) = tab.mouseMove(x, y, steps, flash).also {
        Span.current().addEvent(
            "kdriver.tab.mouseMove", Attributes.builder()
                .put("x", x)
                .put("y", y)
                .put("steps", steps.toLong())
                .put("flash", flash)
                .build()
        )
    }

    override suspend fun mouseClick(
        x: Double,
        y: Double,
        button: Input.MouseButton,
        buttons: Int,
        modifiers: Int,
    ) = tab.mouseClick(x, y, button, buttons, modifiers).also {
        Span.current().addEvent(
            "kdriver.tab.mouseClick", Attributes.builder()
                .put("x", x)
                .put("y", y)
                .put("button", button.name)
                .put("buttons", buttons.toLong())
                .put("modifiers", modifiers.toLong())
                .build()
        )
    }

    override suspend fun <T> expect(
        urlPattern: Regex,
        block: suspend RequestExpectation.() -> T,
    ): T = executeInSpan("kdriver.tab.expect", SpanKind.INTERNAL) { span ->
        span.setAttribute("urlPattern", urlPattern.pattern)
        tab.expect(urlPattern, block)
    }

    override suspend fun <T> expectBatch(
        urlPatterns: List<Regex>,
        block: suspend BatchRequestExpectation.() -> T,
    ): T = executeInSpan("kdriver.tab.expectBatch", SpanKind.INTERNAL) { span ->
        span.setAttribute("urlPatterns", urlPatterns.joinToString(",") { it.pattern })
        tab.expectBatch(urlPatterns, block)
    }

    override suspend fun <T> intercept(
        urlPattern: String,
        requestStage: Fetch.RequestStage,
        resourceType: Network.ResourceType,
        block: suspend FetchInterception.() -> T,
    ): T = executeInSpan("kdriver.tab.intercept", SpanKind.INTERNAL) { span ->
        span.setAttribute("urlPattern", urlPattern)
        span.setAttribute("requestStage", requestStage.name)
        span.setAttribute("resourceType", resourceType.name)
        tab.intercept(urlPattern, requestStage, resourceType, block)
    }

    override suspend fun screenshotB64(
        format: ScreenshotFormat,
        fullPage: Boolean,
    ): String = tab.screenshotB64(format, fullPage).also {
        Span.current().addEvent(
            "kdriver.tab.screenshotB64", Attributes.builder()
                .put("format", format.name)
                .put("fullPage", fullPage)
                .build()
        )
    }

    override suspend fun saveScreenshot(
        filename: Path?,
        format: ScreenshotFormat,
        fullPage: Boolean,
    ): String = tab.saveScreenshot(filename, format, fullPage).also {
        Span.current().addEvent(
            "kdriver.tab.saveScreenshot", Attributes.builder()
                .put("filename", filename?.toString() ?: "null")
                .put("format", format.name)
                .put("fullPage", fullPage)
                .build()
        )
    }

    override suspend fun getAllLinkedSources(): List<Element> = tab.getAllLinkedSources()

    override suspend fun getAllUrls(absolute: Boolean): List<String> = tab.getAllUrls(absolute)

    @InternalCdpApi
    override suspend fun callCommand(
        method: String,
        parameter: JsonElement?,
        mode: CommandMode,
    ): JsonElement? = tab.callCommand(method, parameter, mode)

    @InternalCdpApi
    override suspend fun close() = tab.close().also {
        Span.current().addEvent("kdriver.tab.close")
    }

    override suspend fun updateTarget() = tab.updateTarget()

    override suspend fun wait(t: Long?) = tab.wait(t)

    override suspend fun sleep(t: Long) = tab.sleep(t)

    override var targetInfo: Target.TargetInfo? = tab.targetInfo

    @InternalCdpApi
    override val events: Flow<Message.Event> = tab.events

    @InternalCdpApi
    override val responses: Flow<Message.Response> = tab.responses

    @InternalCdpApi
    override val generatedDomains: MutableMap<KClass<out Domain>, Domain> = tab.generatedDomains

    /**
     * Helper function to execute code within a span.
     */
    private suspend fun <T> executeInSpan(
        spanName: String,
        spanKind: SpanKind,
        block: suspend (Span) -> T,
    ): T {
        val span = tracer.spanBuilder(spanName)
            .setSpanKind(spanKind)
            .startSpan()
        return try {
            withContext(span.asContextElement()) {
                val result = block(span)
                span.setStatus(StatusCode.OK)
                result
            }
        } catch (e: Exception) {
            span.recordException(e)
            span.setStatus(StatusCode.ERROR, e.message ?: "Error in operation")
            throw e
        } finally {
            span.end()
        }
    }

}
