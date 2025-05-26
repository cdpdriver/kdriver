package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.heapProfiler: HeapProfiler
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(HeapProfiler(this))

public class HeapProfiler(
    private val cdp: CDP,
) : Domain {
    public val addHeapSnapshotChunk: Flow<AddHeapSnapshotChunkParameter> = cdp
        .events
        .filter {
            it.method == "HeapProfiler.addHeapSnapshotChunk"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val heapStatsUpdate: Flow<HeapStatsUpdateParameter> = cdp
        .events
        .filter {
            it.method == "HeapProfiler.heapStatsUpdate"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val lastSeenObjectId: Flow<LastSeenObjectIdParameter> = cdp
        .events
        .filter {
            it.method == "HeapProfiler.lastSeenObjectId"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val reportHeapSnapshotProgress: Flow<ReportHeapSnapshotProgressParameter> = cdp
        .events
        .filter {
            it.method == "HeapProfiler.reportHeapSnapshotProgress"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val resetProfiles: Flow<Unit> = cdp
        .events
        .filter {
            it.method == "HeapProfiler.resetProfiles"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    /**
     * Enables console to refer to the node with given id via $x (see Command Line API for more details
     * $x functions).
     */
    public suspend fun addInspectedHeapObject(args: AddInspectedHeapObjectParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("HeapProfiler.addInspectedHeapObject", parameter)
    }

    /**
     * Enables console to refer to the node with given id via $x (see Command Line API for more details
     * $x functions).
     */
    public suspend fun addInspectedHeapObject(heapObjectId: String) {
        val parameter = AddInspectedHeapObjectParameter(heapObjectId = heapObjectId)
        addInspectedHeapObject(parameter)
    }

    public suspend fun collectGarbage() {
        val parameter = null
        cdp.callCommand("HeapProfiler.collectGarbage", parameter)
    }

    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("HeapProfiler.disable", parameter)
    }

    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("HeapProfiler.enable", parameter)
    }

    public suspend fun getHeapObjectId(args: GetHeapObjectIdParameter): GetHeapObjectIdReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("HeapProfiler.getHeapObjectId", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    public suspend fun getHeapObjectId(objectId: String): GetHeapObjectIdReturn {
        val parameter = GetHeapObjectIdParameter(objectId = objectId)
        return getHeapObjectId(parameter)
    }

    public suspend fun getObjectByHeapObjectId(args: GetObjectByHeapObjectIdParameter): GetObjectByHeapObjectIdReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("HeapProfiler.getObjectByHeapObjectId", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    public suspend fun getObjectByHeapObjectId(
        objectId: String,
        objectGroup: String? = null,
    ): GetObjectByHeapObjectIdReturn {
        val parameter = GetObjectByHeapObjectIdParameter(objectId = objectId, objectGroup = objectGroup)
        return getObjectByHeapObjectId(parameter)
    }

    public suspend fun getSamplingProfile(): GetSamplingProfileReturn {
        val parameter = null
        val result = cdp.callCommand("HeapProfiler.getSamplingProfile", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    public suspend fun startSampling(args: StartSamplingParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("HeapProfiler.startSampling", parameter)
    }

    public suspend fun startSampling(
        samplingInterval: Double? = null,
        includeObjectsCollectedByMajorGC: Boolean? = null,
        includeObjectsCollectedByMinorGC: Boolean? = null,
    ) {
        val parameter = StartSamplingParameter(
            samplingInterval = samplingInterval,
            includeObjectsCollectedByMajorGC = includeObjectsCollectedByMajorGC,
            includeObjectsCollectedByMinorGC = includeObjectsCollectedByMinorGC
        )
        startSampling(parameter)
    }

    public suspend fun startTrackingHeapObjects(args: StartTrackingHeapObjectsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("HeapProfiler.startTrackingHeapObjects", parameter)
    }

    public suspend fun startTrackingHeapObjects(trackAllocations: Boolean? = null) {
        val parameter = StartTrackingHeapObjectsParameter(trackAllocations = trackAllocations)
        startTrackingHeapObjects(parameter)
    }

    public suspend fun stopSampling(): StopSamplingReturn {
        val parameter = null
        val result = cdp.callCommand("HeapProfiler.stopSampling", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    public suspend fun stopTrackingHeapObjects(args: StopTrackingHeapObjectsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("HeapProfiler.stopTrackingHeapObjects", parameter)
    }

    public suspend fun stopTrackingHeapObjects(
        reportProgress: Boolean? = null,
        treatGlobalObjectsAsRoots: Boolean? = null,
        captureNumericValue: Boolean? = null,
        exposeInternals: Boolean? = null,
    ) {
        val parameter = StopTrackingHeapObjectsParameter(
            reportProgress = reportProgress,
            treatGlobalObjectsAsRoots = treatGlobalObjectsAsRoots,
            captureNumericValue = captureNumericValue,
            exposeInternals = exposeInternals
        )
        stopTrackingHeapObjects(parameter)
    }

    public suspend fun takeHeapSnapshot(args: TakeHeapSnapshotParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("HeapProfiler.takeHeapSnapshot", parameter)
    }

    public suspend fun takeHeapSnapshot(
        reportProgress: Boolean? = null,
        treatGlobalObjectsAsRoots: Boolean? = null,
        captureNumericValue: Boolean? = null,
        exposeInternals: Boolean? = null,
    ) {
        val parameter = TakeHeapSnapshotParameter(
            reportProgress = reportProgress,
            treatGlobalObjectsAsRoots = treatGlobalObjectsAsRoots,
            captureNumericValue = captureNumericValue,
            exposeInternals = exposeInternals
        )
        takeHeapSnapshot(parameter)
    }

    /**
     * Sampling Heap Profile node. Holds callsite information, allocation statistics and child nodes.
     */
    @Serializable
    public data class SamplingHeapProfileNode(
        /**
         * Function location.
         */
        public val callFrame: Runtime.CallFrame,
        /**
         * Allocations size in bytes for the node excluding children.
         */
        public val selfSize: Double,
        /**
         * Node id. Ids are unique across all profiles collected between startSampling and stopSampling.
         */
        public val id: Int,
        /**
         * Child nodes.
         */
        public val children: List<SamplingHeapProfileNode>,
    )

    /**
     * A single sample from a sampling profile.
     */
    @Serializable
    public data class SamplingHeapProfileSample(
        /**
         * Allocation size in bytes attributed to the sample.
         */
        public val size: Double,
        /**
         * Id of the corresponding profile tree node.
         */
        public val nodeId: Int,
        /**
         * Time-ordered sample ordinal number. It is unique across all profiles retrieved
         * between startSampling and stopSampling.
         */
        public val ordinal: Double,
    )

    /**
     * Sampling profile.
     */
    @Serializable
    public data class SamplingHeapProfile(
        public val head: SamplingHeapProfileNode,
        public val samples: List<SamplingHeapProfileSample>,
    )

    @Serializable
    public data class AddHeapSnapshotChunkParameter(
        public val chunk: String,
    )

    /**
     * If heap objects tracking has been started then backend may send update for one or more fragments
     */
    @Serializable
    public data class HeapStatsUpdateParameter(
        /**
         * An array of triplets. Each triplet describes a fragment. The first integer is the fragment
         * index, the second integer is a total count of objects for the fragment, the third integer is
         * a total size of the objects for the fragment.
         */
        public val statsUpdate: List<Int>,
    )

    /**
     * If heap objects tracking has been started then backend regularly sends a current value for last
     * seen object id and corresponding timestamp. If the were changes in the heap since last event
     * then one or more heapStatsUpdate events will be sent before a new lastSeenObjectId event.
     */
    @Serializable
    public data class LastSeenObjectIdParameter(
        public val lastSeenObjectId: Int,
        public val timestamp: Double,
    )

    @Serializable
    public data class ReportHeapSnapshotProgressParameter(
        public val done: Int,
        public val total: Int,
        public val finished: Boolean? = null,
    )

    @Serializable
    public data class AddInspectedHeapObjectParameter(
        /**
         * Heap snapshot object id to be accessible by means of $x command line API.
         */
        public val heapObjectId: String,
    )

    @Serializable
    public data class GetHeapObjectIdParameter(
        /**
         * Identifier of the object to get heap object id for.
         */
        public val objectId: String,
    )

    @Serializable
    public data class GetHeapObjectIdReturn(
        /**
         * Id of the heap snapshot object corresponding to the passed remote object id.
         */
        public val heapSnapshotObjectId: String,
    )

    @Serializable
    public data class GetObjectByHeapObjectIdParameter(
        public val objectId: String,
        /**
         * Symbolic group name that can be used to release multiple objects.
         */
        public val objectGroup: String? = null,
    )

    @Serializable
    public data class GetObjectByHeapObjectIdReturn(
        /**
         * Evaluation result.
         */
        public val result: Runtime.RemoteObject,
    )

    @Serializable
    public data class GetSamplingProfileReturn(
        /**
         * Return the sampling profile being collected.
         */
        public val profile: SamplingHeapProfile,
    )

    @Serializable
    public data class StartSamplingParameter(
        /**
         * Average sample interval in bytes. Poisson distribution is used for the intervals. The
         * default value is 32768 bytes.
         */
        public val samplingInterval: Double? = null,
        /**
         * By default, the sampling heap profiler reports only objects which are
         * still alive when the profile is returned via getSamplingProfile or
         * stopSampling, which is useful for determining what functions contribute
         * the most to steady-state memory usage. This flag instructs the sampling
         * heap profiler to also include information about objects discarded by
         * major GC, which will show which functions cause large temporary memory
         * usage or long GC pauses.
         */
        public val includeObjectsCollectedByMajorGC: Boolean? = null,
        /**
         * By default, the sampling heap profiler reports only objects which are
         * still alive when the profile is returned via getSamplingProfile or
         * stopSampling, which is useful for determining what functions contribute
         * the most to steady-state memory usage. This flag instructs the sampling
         * heap profiler to also include information about objects discarded by
         * minor GC, which is useful when tuning a latency-sensitive application
         * for minimal GC activity.
         */
        public val includeObjectsCollectedByMinorGC: Boolean? = null,
    )

    @Serializable
    public data class StartTrackingHeapObjectsParameter(
        public val trackAllocations: Boolean? = null,
    )

    @Serializable
    public data class StopSamplingReturn(
        /**
         * Recorded sampling heap profile.
         */
        public val profile: SamplingHeapProfile,
    )

    @Serializable
    public data class StopTrackingHeapObjectsParameter(
        /**
         * If true 'reportHeapSnapshotProgress' events will be generated while snapshot is being taken
         * when the tracking is stopped.
         */
        public val reportProgress: Boolean? = null,
        /**
         * Deprecated in favor of `exposeInternals`.
         */
        public val treatGlobalObjectsAsRoots: Boolean? = null,
        /**
         * If true, numerical values are included in the snapshot
         */
        public val captureNumericValue: Boolean? = null,
        /**
         * If true, exposes internals of the snapshot.
         */
        public val exposeInternals: Boolean? = null,
    )

    @Serializable
    public data class TakeHeapSnapshotParameter(
        /**
         * If true 'reportHeapSnapshotProgress' events will be generated while snapshot is being taken.
         */
        public val reportProgress: Boolean? = null,
        /**
         * If true, a raw snapshot without artificial roots will be generated.
         * Deprecated in favor of `exposeInternals`.
         */
        public val treatGlobalObjectsAsRoots: Boolean? = null,
        /**
         * If true, numerical values are included in the snapshot
         */
        public val captureNumericValue: Boolean? = null,
        /**
         * If true, exposes internals of the snapshot.
         */
        public val exposeInternals: Boolean? = null,
    )
}
