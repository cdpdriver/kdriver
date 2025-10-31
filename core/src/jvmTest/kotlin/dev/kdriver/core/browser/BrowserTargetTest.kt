package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Target
import kotlin.test.Test
import kotlin.test.assertEquals

class BrowserTargetTest {

    @Test
    fun testBrowserTargetProperties() {
        class TestBrowserTarget : BrowserTarget {
            override var targetInfo: Target.TargetInfo? = Target.TargetInfo(
                targetId = "target-123",
                type = "page",
                title = "Test Page",
                url = "https://example.com",
                attached = true,
                openerId = "opener-456",
                canAccessOpener = false,
                openerFrameId = "frame-789",
                browserContextId = "context-abc",
                subtype = "prerender"
            )
        }

        val target = TestBrowserTarget()
        assertEquals("target-123", target.targetId)
        assertEquals("page", target.type)
        assertEquals("Test Page", target.title)
        assertEquals("https://example.com", target.url)
        assertEquals(true, target.attached)
        assertEquals("opener-456", target.openerId)
        assertEquals(false, target.canAccessOpener)
        assertEquals("frame-789", target.openerFrameId)
        assertEquals("context-abc", target.browserContextId)
        assertEquals("prerender", target.subtype)
    }

    @Test
    fun testBrowserTargetNullProperties() {
        class TestBrowserTarget : BrowserTarget {
            override var targetInfo: Target.TargetInfo? = null
        }

        val target = TestBrowserTarget()
        assertEquals(null, target.targetId)
        assertEquals(null, target.type)
        assertEquals(null, target.title)
        assertEquals(null, target.url)
        assertEquals(null, target.attached)
        assertEquals(null, target.openerId)
        assertEquals(null, target.canAccessOpener)
        assertEquals(null, target.openerFrameId)
        assertEquals(null, target.browserContextId)
        assertEquals(null, target.subtype)
    }

}
