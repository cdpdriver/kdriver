@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * This domain provides experimental commands only supported in headless mode.
 */
public val CDP.headlessExperimental: HeadlessExperimental
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(HeadlessExperimental(this))

/**
 * This domain provides experimental commands only supported in headless mode.
 */
public class HeadlessExperimental(
    private val cdp: CDP,
) : Domain {
    /**
     * Sends a BeginFrame to the target and returns when the frame was completed. Optionally captures a
     * screenshot from the resulting frame. Requires that the target was created with enabled
     * BeginFrameControl. Designed for use with --run-all-compositor-stages-before-draw, see also
     * https://goo.gle/chrome-headless-rendering for more background.
     */
    public suspend fun beginFrame(
        args: BeginFrameParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): BeginFrameReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("HeadlessExperimental.beginFrame", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Sends a BeginFrame to the target and returns when the frame was completed. Optionally captures a
     * screenshot from the resulting frame. Requires that the target was created with enabled
     * BeginFrameControl. Designed for use with --run-all-compositor-stages-before-draw, see also
     * https://goo.gle/chrome-headless-rendering for more background.
     *
     * @param frameTimeTicks Timestamp of this BeginFrame in Renderer TimeTicks (milliseconds of uptime). If not set,
     * the current time will be used.
     * @param interval The interval between BeginFrames that is reported to the compositor, in milliseconds.
     * Defaults to a 60 frames/second interval, i.e. about 16.666 milliseconds.
     * @param noDisplayUpdates Whether updates should not be committed and drawn onto the display. False by default. If
     * true, only side effects of the BeginFrame will be run, such as layout and animations, but
     * any visual updates may not be visible on the display or in screenshots.
     * @param screenshot If set, a screenshot of the frame will be captured and returned in the response. Otherwise,
     * no screenshot will be captured. Note that capturing a screenshot can fail, for example,
     * during renderer initialization. In such a case, no screenshot data will be returned.
     */
    public suspend fun beginFrame(
        frameTimeTicks: Double? = null,
        interval: Double? = null,
        noDisplayUpdates: Boolean? = null,
        screenshot: ScreenshotParams? = null,
    ): BeginFrameReturn {
        val parameter = BeginFrameParameter(
            frameTimeTicks = frameTimeTicks,
            interval = interval,
            noDisplayUpdates = noDisplayUpdates,
            screenshot = screenshot
        )
        return beginFrame(parameter)
    }

    /**
     * Disables headless events for the target.
     */
    @Deprecated(message = "")
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("HeadlessExperimental.disable", parameter, mode)
    }

    /**
     * Enables headless events for the target.
     */
    @Deprecated(message = "")
    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("HeadlessExperimental.enable", parameter, mode)
    }

    /**
     * Encoding options for a screenshot.
     */
    @Serializable
    public data class ScreenshotParams(
        /**
         * Image compression format (defaults to png).
         */
        public val format: String? = null,
        /**
         * Compression quality from range [0..100] (jpeg and webp only).
         */
        public val quality: Int? = null,
        /**
         * Optimize image encoding for speed, not for resulting size (defaults to false)
         */
        public val optimizeForSpeed: Boolean? = null,
    )

    @Serializable
    public data class BeginFrameParameter(
        /**
         * Timestamp of this BeginFrame in Renderer TimeTicks (milliseconds of uptime). If not set,
         * the current time will be used.
         */
        public val frameTimeTicks: Double? = null,
        /**
         * The interval between BeginFrames that is reported to the compositor, in milliseconds.
         * Defaults to a 60 frames/second interval, i.e. about 16.666 milliseconds.
         */
        public val interval: Double? = null,
        /**
         * Whether updates should not be committed and drawn onto the display. False by default. If
         * true, only side effects of the BeginFrame will be run, such as layout and animations, but
         * any visual updates may not be visible on the display or in screenshots.
         */
        public val noDisplayUpdates: Boolean? = null,
        /**
         * If set, a screenshot of the frame will be captured and returned in the response. Otherwise,
         * no screenshot will be captured. Note that capturing a screenshot can fail, for example,
         * during renderer initialization. In such a case, no screenshot data will be returned.
         */
        public val screenshot: ScreenshotParams? = null,
    )

    @Serializable
    public data class BeginFrameReturn(
        /**
         * Whether the BeginFrame resulted in damage and, thus, a new frame was committed to the
         * display. Reported for diagnostic uses, may be removed in the future.
         */
        public val hasDamage: Boolean,
        /**
         * Base64-encoded image data of the screenshot, if one was requested and successfully taken. (Encoded as a base64 string when passed over JSON)
         */
        public val screenshotData: String?,
    )
}
