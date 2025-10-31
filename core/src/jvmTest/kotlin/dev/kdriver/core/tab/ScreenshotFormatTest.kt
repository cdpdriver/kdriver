package dev.kdriver.core.tab

import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenshotFormatTest {

    @Test
    fun testScreenshotFormatEnum() {
        assertEquals("JPEG", ScreenshotFormat.JPEG.name)
        assertEquals("PNG", ScreenshotFormat.PNG.name)
        assertEquals(2, ScreenshotFormat.entries.size)
    }

}
