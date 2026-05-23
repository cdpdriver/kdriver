package dev.kdriver.opentelemetry

import dev.kdriver.cdp.domain.Target
import dev.kdriver.core.tab.Tab
import io.mockk.every
import io.mockk.mockk
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Guards that [OpenTelemetryTab.targetInfo] delegates to the wrapped tab rather than capturing a
 * one-time snapshot at construction (audit ISSUE-12).
 *
 * The buggy implementation declared `override var targetInfo = tab.targetInfo`, a stored property
 * initialized once. As a result a traced tab kept reporting stale target info after navigation, and
 * writes to it never reached the underlying tab — unlike `lastMouseX`/`lastMouseY`, which delegate.
 */
class OpenTelemetryTabTargetInfoTest {

    companion object {
        @JvmField
        @RegisterExtension
        val otelTesting = OpenTelemetryExtension.create()
    }

    private fun info(url: String) = Target.TargetInfo(
        targetId = "t",
        type = "page",
        title = "",
        url = url,
        attached = true,
        canAccessOpener = false,
    )

    @Test
    fun `targetInfo getter reflects updates to the wrapped tab`() = runTest {
        val tracer = otelTesting.openTelemetry.getTracer("test")
        var backing: Target.TargetInfo? = info("about:blank")
        val mockTab = mockk<Tab>(relaxed = true)
        every { mockTab.targetInfo } answers { backing }

        // Construction must not freeze a snapshot.
        val traced = mockTab.withTracing(tracer)

        // The underlying tab navigates after the wrapper was created.
        backing = info("https://example.com")

        assertEquals(
            "https://example.com",
            traced.targetInfo?.url,
            "targetInfo should reflect the current value of the wrapped tab, not a snapshot",
        )
    }

    @Test
    fun `targetInfo setter propagates to the wrapped tab`() = runTest {
        val tracer = otelTesting.openTelemetry.getTracer("test")
        var backing: Target.TargetInfo? = info("about:blank")
        val mockTab = mockk<Tab>(relaxed = true)
        every { mockTab.targetInfo } answers { backing }
        every { mockTab.targetInfo = any() } answers { backing = firstArg() }

        val traced = mockTab.withTracing(tracer)

        traced.targetInfo = info("https://example.com")

        assertEquals(
            "https://example.com",
            backing?.url,
            "Writing targetInfo on the wrapper should propagate to the wrapped tab",
        )
    }
}
