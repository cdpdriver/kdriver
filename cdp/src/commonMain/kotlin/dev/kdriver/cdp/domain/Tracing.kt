package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.tracing: Tracing
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Tracing(this))

public class Tracing(
    private val cdp: CDP,
) : Domain {
    public val bufferUsage: Flow<BufferUsageParameter> = cdp
        .events
        .filter {
            it.method == "Tracing.bufferUsage"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val dataCollected: Flow<DataCollectedParameter> = cdp
        .events
        .filter {
            it.method == "Tracing.dataCollected"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val tracingComplete: Flow<TracingCompleteParameter> = cdp
        .events
        .filter {
            it.method == "Tracing.tracingComplete"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    /**
     * Stop trace events collection.
     */
    public suspend fun end() {
        val parameter = null
        cdp.callCommand("Tracing.end", parameter)
    }

    /**
     * Gets supported tracing categories.
     */
    public suspend fun getCategories(): GetCategoriesReturn {
        val parameter = null
        val result = cdp.callCommand("Tracing.getCategories", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Record a clock sync marker in the trace.
     */
    public suspend fun recordClockSyncMarker(args: RecordClockSyncMarkerParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Tracing.recordClockSyncMarker", parameter)
    }

    /**
     * Record a clock sync marker in the trace.
     */
    public suspend fun recordClockSyncMarker(syncId: String) {
        val parameter = RecordClockSyncMarkerParameter(syncId = syncId)
        recordClockSyncMarker(parameter)
    }

    /**
     * Request a global memory dump.
     */
    public suspend fun requestMemoryDump(args: RequestMemoryDumpParameter): RequestMemoryDumpReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Tracing.requestMemoryDump", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Request a global memory dump.
     */
    public suspend fun requestMemoryDump(
        deterministic: Boolean? = null,
        levelOfDetail: MemoryDumpLevelOfDetail? = null,
    ): RequestMemoryDumpReturn {
        val parameter = RequestMemoryDumpParameter(deterministic = deterministic, levelOfDetail = levelOfDetail)
        return requestMemoryDump(parameter)
    }

    /**
     * Start trace events collection.
     */
    public suspend fun start(args: StartParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Tracing.start", parameter)
    }

    /**
     * Start trace events collection.
     */
    public suspend fun start(
        categories: String? = null,
        options: String? = null,
        bufferUsageReportingInterval: Double? = null,
        transferMode: String? = null,
        streamFormat: StreamFormat? = null,
        streamCompression: StreamCompression? = null,
        traceConfig: TraceConfig? = null,
        perfettoConfig: String? = null,
        tracingBackend: TracingBackend? = null,
    ) {
        val parameter = StartParameter(
            categories = categories,
            options = options,
            bufferUsageReportingInterval = bufferUsageReportingInterval,
            transferMode = transferMode,
            streamFormat = streamFormat,
            streamCompression = streamCompression,
            traceConfig = traceConfig,
            perfettoConfig = perfettoConfig,
            tracingBackend = tracingBackend
        )
        start(parameter)
    }

    @Serializable
    public data class TraceConfig(
        /**
         * Controls how the trace buffer stores data.
         */
        public val recordMode: String? = null,
        /**
         * Size of the trace buffer in kilobytes. If not specified or zero is passed, a default value
         * of 200 MB would be used.
         */
        public val traceBufferSizeInKb: Double? = null,
        /**
         * Turns on JavaScript stack sampling.
         */
        public val enableSampling: Boolean? = null,
        /**
         * Turns on system tracing.
         */
        public val enableSystrace: Boolean? = null,
        /**
         * Turns on argument filter.
         */
        public val enableArgumentFilter: Boolean? = null,
        /**
         * Included category filters.
         */
        public val includedCategories: List<String>? = null,
        /**
         * Excluded category filters.
         */
        public val excludedCategories: List<String>? = null,
        /**
         * Configuration to synthesize the delays in tracing.
         */
        public val syntheticDelays: List<String>? = null,
        /**
         * Configuration for memory dump triggers. Used only when "memory-infra" category is enabled.
         */
        public val memoryDumpConfig: Map<String, JsonElement>? = null,
    )

    /**
     * Data format of a trace. Can be either the legacy JSON format or the
     * protocol buffer format. Note that the JSON format will be deprecated soon.
     */
    @Serializable
    public enum class StreamFormat {
        @SerialName("json")
        JSON,

        @SerialName("proto")
        PROTO,
    }

    /**
     * Compression type to use for traces returned via streams.
     */
    @Serializable
    public enum class StreamCompression {
        @SerialName("none")
        NONE,

        @SerialName("gzip")
        GZIP,
    }

    /**
     * Details exposed when memory request explicitly declared.
     * Keep consistent with memory_dump_request_args.h and
     * memory_instrumentation.mojom
     */
    @Serializable
    public enum class MemoryDumpLevelOfDetail {
        @SerialName("background")
        BACKGROUND,

        @SerialName("light")
        LIGHT,

        @SerialName("detailed")
        DETAILED,
    }

    /**
     * Backend type to use for tracing. `chrome` uses the Chrome-integrated
     * tracing service and is supported on all platforms. `system` is only
     * supported on Chrome OS and uses the Perfetto system tracing service.
     * `auto` chooses `system` when the perfettoConfig provided to Tracing.start
     * specifies at least one non-Chrome data source; otherwise uses `chrome`.
     */
    @Serializable
    public enum class TracingBackend {
        @SerialName("auto")
        AUTO,

        @SerialName("chrome")
        CHROME,

        @SerialName("system")
        SYSTEM,
    }

    @Serializable
    public data class BufferUsageParameter(
        /**
         * A number in range [0..1] that indicates the used size of event buffer as a fraction of its
         * total size.
         */
        public val percentFull: Double? = null,
        /**
         * An approximate number of events in the trace log.
         */
        public val eventCount: Double? = null,
        /**
         * A number in range [0..1] that indicates the used size of event buffer as a fraction of its
         * total size.
         */
        public val `value`: Double? = null,
    )

    /**
     * Contains a bucket of collected trace events. When tracing is stopped collected events will be
     * sent as a sequence of dataCollected events followed by tracingComplete event.
     */
    @Serializable
    public data class DataCollectedParameter(
        public val `value`: List<Map<String, JsonElement>>,
    )

    /**
     * Signals that tracing is stopped and there is no trace buffers pending flush, all data were
     * delivered via dataCollected events.
     */
    @Serializable
    public data class TracingCompleteParameter(
        /**
         * Indicates whether some trace data is known to have been lost, e.g. because the trace ring
         * buffer wrapped around.
         */
        public val dataLossOccurred: Boolean,
        /**
         * A handle of the stream that holds resulting trace data.
         */
        public val stream: String? = null,
        /**
         * Trace data format of returned stream.
         */
        public val traceFormat: StreamFormat? = null,
        /**
         * Compression format of returned stream.
         */
        public val streamCompression: StreamCompression? = null,
    )

    @Serializable
    public data class GetCategoriesReturn(
        /**
         * A list of supported tracing categories.
         */
        public val categories: List<String>,
    )

    @Serializable
    public data class RecordClockSyncMarkerParameter(
        /**
         * The ID of this clock sync marker
         */
        public val syncId: String,
    )

    @Serializable
    public data class RequestMemoryDumpParameter(
        /**
         * Enables more deterministic results by forcing garbage collection
         */
        public val deterministic: Boolean? = null,
        /**
         * Specifies level of details in memory dump. Defaults to "detailed".
         */
        public val levelOfDetail: MemoryDumpLevelOfDetail? = null,
    )

    @Serializable
    public data class RequestMemoryDumpReturn(
        /**
         * GUID of the resulting global memory dump.
         */
        public val dumpGuid: String,
        /**
         * True iff the global memory dump succeeded.
         */
        public val success: Boolean,
    )

    @Serializable
    public data class StartParameter(
        /**
         * Category/tag filter
         */
        public val categories: String? = null,
        /**
         * Tracing options
         */
        public val options: String? = null,
        /**
         * If set, the agent will issue bufferUsage events at this interval, specified in milliseconds
         */
        public val bufferUsageReportingInterval: Double? = null,
        /**
         * Whether to report trace events as series of dataCollected events or to save trace to a
         * stream (defaults to `ReportEvents`).
         */
        public val transferMode: String? = null,
        /**
         * Trace data format to use. This only applies when using `ReturnAsStream`
         * transfer mode (defaults to `json`).
         */
        public val streamFormat: StreamFormat? = null,
        /**
         * Compression format to use. This only applies when using `ReturnAsStream`
         * transfer mode (defaults to `none`)
         */
        public val streamCompression: StreamCompression? = null,
        public val traceConfig: TraceConfig? = null,
        /**
         * Base64-encoded serialized perfetto.protos.TraceConfig protobuf message
         * When specified, the parameters `categories`, `options`, `traceConfig`
         * are ignored. (Encoded as a base64 string when passed over JSON)
         */
        public val perfettoConfig: String? = null,
        /**
         * Backend type (defaults to `auto`)
         */
        public val tracingBackend: TracingBackend? = null,
    )
}
