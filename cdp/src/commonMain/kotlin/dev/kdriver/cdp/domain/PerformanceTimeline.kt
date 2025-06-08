package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.performanceTimeline: PerformanceTimeline
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(PerformanceTimeline(this))

/**
 * Reporting of performance timeline events, as specified in
 * https://w3c.github.io/performance-timeline/#dom-performanceobserver.
 */
public class PerformanceTimeline(
    private val cdp: CDP,
) : Domain {
    /**
     * Sent when a performance timeline event is added. See reportPerformanceTimeline method.
     */
    public val timelineEventAdded: Flow<TimelineEventAddedParameter> = cdp
        .events
        .filter { it.method == "PerformanceTimeline.timelineEventAdded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Previously buffered events would be reported before method returns.
     * See also: timelineEventAdded
     */
    public suspend fun enable(args: EnableParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("PerformanceTimeline.enable", parameter)
    }

    /**
     * Previously buffered events would be reported before method returns.
     * See also: timelineEventAdded
     *
     * @param eventTypes The types of event to report, as specified in
     * https://w3c.github.io/performance-timeline/#dom-performanceentry-entrytype
     * The specified filter overrides any previous filters, passing empty
     * filter disables recording.
     * Note that not all types exposed to the web platform are currently supported.
     */
    public suspend fun enable(eventTypes: List<String>) {
        val parameter = EnableParameter(eventTypes = eventTypes)
        enable(parameter)
    }

    /**
     * See https://github.com/WICG/LargestContentfulPaint and largest_contentful_paint.idl
     */
    @Serializable
    public data class LargestContentfulPaint(
        public val renderTime: Double,
        public val loadTime: Double,
        /**
         * The number of pixels being painted.
         */
        public val size: Double,
        /**
         * The id attribute of the element, if available.
         */
        public val elementId: String? = null,
        /**
         * The URL of the image (may be trimmed).
         */
        public val url: String? = null,
        public val nodeId: Int? = null,
    )

    @Serializable
    public data class LayoutShiftAttribution(
        public val previousRect: DOM.Rect,
        public val currentRect: DOM.Rect,
        public val nodeId: Int? = null,
    )

    /**
     * See https://wicg.github.io/layout-instability/#sec-layout-shift and layout_shift.idl
     */
    @Serializable
    public data class LayoutShift(
        /**
         * Score increment produced by this event.
         */
        public val `value`: Double,
        public val hadRecentInput: Boolean,
        public val lastInputTime: Double,
        public val sources: List<LayoutShiftAttribution>,
    )

    @Serializable
    public data class TimelineEvent(
        /**
         * Identifies the frame that this event is related to. Empty for non-frame targets.
         */
        public val frameId: String,
        /**
         * The event type, as specified in https://w3c.github.io/performance-timeline/#dom-performanceentry-entrytype
         * This determines which of the optional "details" fiedls is present.
         */
        public val type: String,
        /**
         * Name may be empty depending on the type.
         */
        public val name: String,
        /**
         * Time in seconds since Epoch, monotonically increasing within document lifetime.
         */
        public val time: Double,
        /**
         * Event duration, if applicable.
         */
        public val duration: Double? = null,
        public val lcpDetails: LargestContentfulPaint? = null,
        public val layoutShiftDetails: LayoutShift? = null,
    )

    /**
     * Sent when a performance timeline event is added. See reportPerformanceTimeline method.
     */
    @Serializable
    public data class TimelineEventAddedParameter(
        public val event: TimelineEvent,
    )

    @Serializable
    public data class EnableParameter(
        /**
         * The types of event to report, as specified in
         * https://w3c.github.io/performance-timeline/#dom-performanceentry-entrytype
         * The specified filter overrides any previous filters, passing empty
         * filter disables recording.
         * Note that not all types exposed to the web platform are currently supported.
         */
        public val eventTypes: List<String>,
    )
}
