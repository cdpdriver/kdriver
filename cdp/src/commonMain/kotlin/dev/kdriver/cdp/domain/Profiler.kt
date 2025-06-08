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

public val CDP.profiler: Profiler
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Profiler(this))

public class Profiler(
    private val cdp: CDP,
) : Domain {
    public val consoleProfileFinished: Flow<ConsoleProfileFinishedParameter> = cdp
        .events
        .filter { it.method == "Profiler.consoleProfileFinished" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Sent when new profile recording is started using console.profile() call.
     */
    public val consoleProfileStarted: Flow<ConsoleProfileStartedParameter> = cdp
        .events
        .filter { it.method == "Profiler.consoleProfileStarted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Reports coverage delta since the last poll (either from an event like this, or from
     * `takePreciseCoverage` for the current isolate. May only be sent if precise code
     * coverage has been started. This event can be trigged by the embedder to, for example,
     * trigger collection of coverage data immediately at a certain point in time.
     */
    public val preciseCoverageDeltaUpdate: Flow<PreciseCoverageDeltaUpdateParameter> = cdp
        .events
        .filter { it.method == "Profiler.preciseCoverageDeltaUpdate" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Profiler.disable", parameter)
    }

    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("Profiler.enable", parameter)
    }

    /**
     * Collect coverage data for the current isolate. The coverage data may be incomplete due to
     * garbage collection.
     */
    public suspend fun getBestEffortCoverage(): GetBestEffortCoverageReturn {
        val parameter = null
        val result = cdp.callCommand("Profiler.getBestEffortCoverage", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Changes CPU profiler sampling interval. Must be called before CPU profiles recording started.
     */
    public suspend fun setSamplingInterval(args: SetSamplingIntervalParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Profiler.setSamplingInterval", parameter)
    }

    /**
     * Changes CPU profiler sampling interval. Must be called before CPU profiles recording started.
     *
     * @param interval New sampling interval in microseconds.
     */
    public suspend fun setSamplingInterval(interval: Int) {
        val parameter = SetSamplingIntervalParameter(interval = interval)
        setSamplingInterval(parameter)
    }

    public suspend fun start() {
        val parameter = null
        cdp.callCommand("Profiler.start", parameter)
    }

    /**
     * Enable precise code coverage. Coverage data for JavaScript executed before enabling precise code
     * coverage may be incomplete. Enabling prevents running optimized code and resets execution
     * counters.
     */
    public suspend fun startPreciseCoverage(args: StartPreciseCoverageParameter): StartPreciseCoverageReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Profiler.startPreciseCoverage", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Enable precise code coverage. Coverage data for JavaScript executed before enabling precise code
     * coverage may be incomplete. Enabling prevents running optimized code and resets execution
     * counters.
     *
     * @param callCount Collect accurate call counts beyond simple 'covered' or 'not covered'.
     * @param detailed Collect block-based coverage.
     * @param allowTriggeredUpdates Allow the backend to send updates on its own initiative
     */
    public suspend fun startPreciseCoverage(
        callCount: Boolean? = null,
        detailed: Boolean? = null,
        allowTriggeredUpdates: Boolean? = null,
    ): StartPreciseCoverageReturn {
        val parameter = StartPreciseCoverageParameter(
            callCount = callCount,
            detailed = detailed,
            allowTriggeredUpdates = allowTriggeredUpdates
        )
        return startPreciseCoverage(parameter)
    }

    public suspend fun stop(): StopReturn {
        val parameter = null
        val result = cdp.callCommand("Profiler.stop", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Disable precise code coverage. Disabling releases unnecessary execution count records and allows
     * executing optimized code.
     */
    public suspend fun stopPreciseCoverage() {
        val parameter = null
        cdp.callCommand("Profiler.stopPreciseCoverage", parameter)
    }

    /**
     * Collect coverage data for the current isolate, and resets execution counters. Precise code
     * coverage needs to have started.
     */
    public suspend fun takePreciseCoverage(): TakePreciseCoverageReturn {
        val parameter = null
        val result = cdp.callCommand("Profiler.takePreciseCoverage", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Profile node. Holds callsite information, execution statistics and child nodes.
     */
    @Serializable
    public data class ProfileNode(
        /**
         * Unique id of the node.
         */
        public val id: Int,
        /**
         * Function location.
         */
        public val callFrame: Runtime.CallFrame,
        /**
         * Number of samples where this node was on top of the call stack.
         */
        public val hitCount: Int? = null,
        /**
         * Child node ids.
         */
        public val children: List<Int>? = null,
        /**
         * The reason of being not optimized. The function may be deoptimized or marked as don't
         * optimize.
         */
        public val deoptReason: String? = null,
        /**
         * An array of source position ticks.
         */
        public val positionTicks: List<PositionTickInfo>? = null,
    )

    /**
     * Profile.
     */
    @Serializable
    public data class Profile(
        /**
         * The list of profile nodes. First item is the root node.
         */
        public val nodes: List<ProfileNode>,
        /**
         * Profiling start timestamp in microseconds.
         */
        public val startTime: Double,
        /**
         * Profiling end timestamp in microseconds.
         */
        public val endTime: Double,
        /**
         * Ids of samples top nodes.
         */
        public val samples: List<Int>? = null,
        /**
         * Time intervals between adjacent samples in microseconds. The first delta is relative to the
         * profile startTime.
         */
        public val timeDeltas: List<Int>? = null,
    )

    /**
     * Specifies a number of samples attributed to a certain source position.
     */
    @Serializable
    public data class PositionTickInfo(
        /**
         * Source line number (1-based).
         */
        public val line: Int,
        /**
         * Number of samples attributed to the source line.
         */
        public val ticks: Int,
    )

    /**
     * Coverage data for a source range.
     */
    @Serializable
    public data class CoverageRange(
        /**
         * JavaScript script source offset for the range start.
         */
        public val startOffset: Int,
        /**
         * JavaScript script source offset for the range end.
         */
        public val endOffset: Int,
        /**
         * Collected execution count of the source range.
         */
        public val count: Int,
    )

    /**
     * Coverage data for a JavaScript function.
     */
    @Serializable
    public data class FunctionCoverage(
        /**
         * JavaScript function name.
         */
        public val functionName: String,
        /**
         * Source ranges inside the function with coverage data.
         */
        public val ranges: List<CoverageRange>,
        /**
         * Whether coverage data for this function has block granularity.
         */
        public val isBlockCoverage: Boolean,
    )

    /**
     * Coverage data for a JavaScript script.
     */
    @Serializable
    public data class ScriptCoverage(
        /**
         * JavaScript script id.
         */
        public val scriptId: String,
        /**
         * JavaScript script name or url.
         */
        public val url: String,
        /**
         * Functions contained in the script that has coverage data.
         */
        public val functions: List<FunctionCoverage>,
    )

    @Serializable
    public data class ConsoleProfileFinishedParameter(
        public val id: String,
        /**
         * Location of console.profileEnd().
         */
        public val location: Debugger.Location,
        public val profile: Profile,
        /**
         * Profile title passed as an argument to console.profile().
         */
        public val title: String? = null,
    )

    /**
     * Sent when new profile recording is started using console.profile() call.
     */
    @Serializable
    public data class ConsoleProfileStartedParameter(
        public val id: String,
        /**
         * Location of console.profile().
         */
        public val location: Debugger.Location,
        /**
         * Profile title passed as an argument to console.profile().
         */
        public val title: String? = null,
    )

    /**
     * Reports coverage delta since the last poll (either from an event like this, or from
     * `takePreciseCoverage` for the current isolate. May only be sent if precise code
     * coverage has been started. This event can be trigged by the embedder to, for example,
     * trigger collection of coverage data immediately at a certain point in time.
     */
    @Serializable
    public data class PreciseCoverageDeltaUpdateParameter(
        /**
         * Monotonically increasing time (in seconds) when the coverage update was taken in the backend.
         */
        public val timestamp: Double,
        /**
         * Identifier for distinguishing coverage events.
         */
        public val occasion: String,
        /**
         * Coverage data for the current isolate.
         */
        public val result: List<ScriptCoverage>,
    )

    @Serializable
    public data class GetBestEffortCoverageReturn(
        /**
         * Coverage data for the current isolate.
         */
        public val result: List<ScriptCoverage>,
    )

    @Serializable
    public data class SetSamplingIntervalParameter(
        /**
         * New sampling interval in microseconds.
         */
        public val interval: Int,
    )

    @Serializable
    public data class StartPreciseCoverageParameter(
        /**
         * Collect accurate call counts beyond simple 'covered' or 'not covered'.
         */
        public val callCount: Boolean? = null,
        /**
         * Collect block-based coverage.
         */
        public val detailed: Boolean? = null,
        /**
         * Allow the backend to send updates on its own initiative
         */
        public val allowTriggeredUpdates: Boolean? = null,
    )

    @Serializable
    public data class StartPreciseCoverageReturn(
        /**
         * Monotonically increasing time (in seconds) when the coverage update was taken in the backend.
         */
        public val timestamp: Double,
    )

    @Serializable
    public data class StopReturn(
        /**
         * Recorded profile.
         */
        public val profile: Profile,
    )

    @Serializable
    public data class TakePreciseCoverageReturn(
        /**
         * Coverage data for the current isolate.
         */
        public val result: List<ScriptCoverage>,
        /**
         * Monotonically increasing time (in seconds) when the coverage update was taken in the backend.
         */
        public val timestamp: Double,
    )
}
