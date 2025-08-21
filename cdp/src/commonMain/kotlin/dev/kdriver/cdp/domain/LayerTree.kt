@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.layerTree: LayerTree
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(LayerTree(this))

public class LayerTree(
    private val cdp: CDP,
) : Domain {
    public val layerPainted: Flow<LayerPaintedParameter> = cdp
        .events
        .filter { it.method == "LayerTree.layerPainted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val layerTreeDidChange: Flow<LayerTreeDidChangeParameter> = cdp
        .events
        .filter { it.method == "LayerTree.layerTreeDidChange" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Provides the reasons why the given layer was composited.
     */
    public suspend fun compositingReasons(
        args: CompositingReasonsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): CompositingReasonsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("LayerTree.compositingReasons", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Provides the reasons why the given layer was composited.
     *
     * @param layerId The id of the layer for which we want to get the reasons it was composited.
     */
    public suspend fun compositingReasons(layerId: String): CompositingReasonsReturn {
        val parameter = CompositingReasonsParameter(layerId = layerId)
        return compositingReasons(parameter)
    }

    /**
     * Disables compositing tree inspection.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("LayerTree.disable", parameter, mode)
    }

    /**
     * Enables compositing tree inspection.
     */
    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("LayerTree.enable", parameter, mode)
    }

    /**
     * Returns the snapshot identifier.
     */
    public suspend fun loadSnapshot(
        args: LoadSnapshotParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): LoadSnapshotReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("LayerTree.loadSnapshot", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the snapshot identifier.
     *
     * @param tiles An array of tiles composing the snapshot.
     */
    public suspend fun loadSnapshot(tiles: List<PictureTile>): LoadSnapshotReturn {
        val parameter = LoadSnapshotParameter(tiles = tiles)
        return loadSnapshot(parameter)
    }

    /**
     * Returns the layer snapshot identifier.
     */
    public suspend fun makeSnapshot(
        args: MakeSnapshotParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): MakeSnapshotReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("LayerTree.makeSnapshot", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the layer snapshot identifier.
     *
     * @param layerId The id of the layer.
     */
    public suspend fun makeSnapshot(layerId: String): MakeSnapshotReturn {
        val parameter = MakeSnapshotParameter(layerId = layerId)
        return makeSnapshot(parameter)
    }

    public suspend fun profileSnapshot(
        args: ProfileSnapshotParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): ProfileSnapshotReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("LayerTree.profileSnapshot", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     *
     *
     * @param snapshotId The id of the layer snapshot.
     * @param minRepeatCount The maximum number of times to replay the snapshot (1, if not specified).
     * @param minDuration The minimum duration (in seconds) to replay the snapshot.
     * @param clipRect The clip rectangle to apply when replaying the snapshot.
     */
    public suspend fun profileSnapshot(
        snapshotId: String,
        minRepeatCount: Int? = null,
        minDuration: Double? = null,
        clipRect: DOM.Rect? = null,
    ): ProfileSnapshotReturn {
        val parameter = ProfileSnapshotParameter(
            snapshotId = snapshotId,
            minRepeatCount = minRepeatCount,
            minDuration = minDuration,
            clipRect = clipRect
        )
        return profileSnapshot(parameter)
    }

    /**
     * Releases layer snapshot captured by the back-end.
     */
    public suspend fun releaseSnapshot(args: ReleaseSnapshotParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("LayerTree.releaseSnapshot", parameter, mode)
    }

    /**
     * Releases layer snapshot captured by the back-end.
     *
     * @param snapshotId The id of the layer snapshot.
     */
    public suspend fun releaseSnapshot(snapshotId: String) {
        val parameter = ReleaseSnapshotParameter(snapshotId = snapshotId)
        releaseSnapshot(parameter)
    }

    /**
     * Replays the layer snapshot and returns the resulting bitmap.
     */
    public suspend fun replaySnapshot(
        args: ReplaySnapshotParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): ReplaySnapshotReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("LayerTree.replaySnapshot", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Replays the layer snapshot and returns the resulting bitmap.
     *
     * @param snapshotId The id of the layer snapshot.
     * @param fromStep The first step to replay from (replay from the very start if not specified).
     * @param toStep The last step to replay to (replay till the end if not specified).
     * @param scale The scale to apply while replaying (defaults to 1).
     */
    public suspend fun replaySnapshot(
        snapshotId: String,
        fromStep: Int? = null,
        toStep: Int? = null,
        scale: Double? = null,
    ): ReplaySnapshotReturn {
        val parameter =
            ReplaySnapshotParameter(snapshotId = snapshotId, fromStep = fromStep, toStep = toStep, scale = scale)
        return replaySnapshot(parameter)
    }

    /**
     * Replays the layer snapshot and returns canvas log.
     */
    public suspend fun snapshotCommandLog(
        args: SnapshotCommandLogParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): SnapshotCommandLogReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("LayerTree.snapshotCommandLog", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Replays the layer snapshot and returns canvas log.
     *
     * @param snapshotId The id of the layer snapshot.
     */
    public suspend fun snapshotCommandLog(snapshotId: String): SnapshotCommandLogReturn {
        val parameter = SnapshotCommandLogParameter(snapshotId = snapshotId)
        return snapshotCommandLog(parameter)
    }

    /**
     * Rectangle where scrolling happens on the main thread.
     */
    @Serializable
    public data class ScrollRect(
        /**
         * Rectangle itself.
         */
        public val rect: DOM.Rect,
        /**
         * Reason for rectangle to force scrolling on the main thread
         */
        public val type: String,
    )

    /**
     * Sticky position constraints.
     */
    @Serializable
    public data class StickyPositionConstraint(
        /**
         * Layout rectangle of the sticky element before being shifted
         */
        public val stickyBoxRect: DOM.Rect,
        /**
         * Layout rectangle of the containing block of the sticky element
         */
        public val containingBlockRect: DOM.Rect,
        /**
         * The nearest sticky layer that shifts the sticky box
         */
        public val nearestLayerShiftingStickyBox: String? = null,
        /**
         * The nearest sticky layer that shifts the containing block
         */
        public val nearestLayerShiftingContainingBlock: String? = null,
    )

    /**
     * Serialized fragment of layer picture along with its offset within the layer.
     */
    @Serializable
    public data class PictureTile(
        /**
         * Offset from owning layer left boundary
         */
        public val x: Double,
        /**
         * Offset from owning layer top boundary
         */
        public val y: Double,
        /**
         * Base64-encoded snapshot data. (Encoded as a base64 string when passed over JSON)
         */
        public val picture: String,
    )

    /**
     * Information about a compositing layer.
     */
    @Serializable
    public data class Layer(
        /**
         * The unique id for this layer.
         */
        public val layerId: String,
        /**
         * The id of parent (not present for root).
         */
        public val parentLayerId: String? = null,
        /**
         * The backend id for the node associated with this layer.
         */
        public val backendNodeId: Int? = null,
        /**
         * Offset from parent layer, X coordinate.
         */
        public val offsetX: Double,
        /**
         * Offset from parent layer, Y coordinate.
         */
        public val offsetY: Double,
        /**
         * Layer width.
         */
        public val width: Double,
        /**
         * Layer height.
         */
        public val height: Double,
        /**
         * Transformation matrix for layer, default is identity matrix
         */
        public val transform: List<Double>? = null,
        /**
         * Transform anchor point X, absent if no transform specified
         */
        public val anchorX: Double? = null,
        /**
         * Transform anchor point Y, absent if no transform specified
         */
        public val anchorY: Double? = null,
        /**
         * Transform anchor point Z, absent if no transform specified
         */
        public val anchorZ: Double? = null,
        /**
         * Indicates how many time this layer has painted.
         */
        public val paintCount: Int,
        /**
         * Indicates whether this layer hosts any content, rather than being used for
         * transform/scrolling purposes only.
         */
        public val drawsContent: Boolean,
        /**
         * Set if layer is not visible.
         */
        public val invisible: Boolean? = null,
        /**
         * Rectangles scrolling on main thread only.
         */
        public val scrollRects: List<ScrollRect>? = null,
        /**
         * Sticky position constraint information
         */
        public val stickyPositionConstraint: StickyPositionConstraint? = null,
    )

    @Serializable
    public data class LayerPaintedParameter(
        /**
         * The id of the painted layer.
         */
        public val layerId: String,
        /**
         * Clip rectangle.
         */
        public val clip: DOM.Rect,
    )

    @Serializable
    public data class LayerTreeDidChangeParameter(
        /**
         * Layer tree, absent if not in the compositing mode.
         */
        public val layers: List<Layer>? = null,
    )

    @Serializable
    public data class CompositingReasonsParameter(
        /**
         * The id of the layer for which we want to get the reasons it was composited.
         */
        public val layerId: String,
    )

    @Serializable
    public data class CompositingReasonsReturn(
        /**
         * A list of strings specifying reasons for the given layer to become composited.
         */
        public val compositingReasons: List<String>,
        /**
         * A list of strings specifying reason IDs for the given layer to become composited.
         */
        public val compositingReasonIds: List<String>,
    )

    @Serializable
    public data class LoadSnapshotParameter(
        /**
         * An array of tiles composing the snapshot.
         */
        public val tiles: List<PictureTile>,
    )

    @Serializable
    public data class LoadSnapshotReturn(
        /**
         * The id of the snapshot.
         */
        public val snapshotId: String,
    )

    @Serializable
    public data class MakeSnapshotParameter(
        /**
         * The id of the layer.
         */
        public val layerId: String,
    )

    @Serializable
    public data class MakeSnapshotReturn(
        /**
         * The id of the layer snapshot.
         */
        public val snapshotId: String,
    )

    @Serializable
    public data class ProfileSnapshotParameter(
        /**
         * The id of the layer snapshot.
         */
        public val snapshotId: String,
        /**
         * The maximum number of times to replay the snapshot (1, if not specified).
         */
        public val minRepeatCount: Int? = null,
        /**
         * The minimum duration (in seconds) to replay the snapshot.
         */
        public val minDuration: Double? = null,
        /**
         * The clip rectangle to apply when replaying the snapshot.
         */
        public val clipRect: DOM.Rect? = null,
    )

    @Serializable
    public data class ProfileSnapshotReturn(
        /**
         * The array of paint profiles, one per run.
         */
        public val timings: List<List<Double>>,
    )

    @Serializable
    public data class ReleaseSnapshotParameter(
        /**
         * The id of the layer snapshot.
         */
        public val snapshotId: String,
    )

    @Serializable
    public data class ReplaySnapshotParameter(
        /**
         * The id of the layer snapshot.
         */
        public val snapshotId: String,
        /**
         * The first step to replay from (replay from the very start if not specified).
         */
        public val fromStep: Int? = null,
        /**
         * The last step to replay to (replay till the end if not specified).
         */
        public val toStep: Int? = null,
        /**
         * The scale to apply while replaying (defaults to 1).
         */
        public val scale: Double? = null,
    )

    @Serializable
    public data class ReplaySnapshotReturn(
        /**
         * A data: URL for resulting image.
         */
        public val dataURL: String,
    )

    @Serializable
    public data class SnapshotCommandLogParameter(
        /**
         * The id of the layer snapshot.
         */
        public val snapshotId: String,
    )

    @Serializable
    public data class SnapshotCommandLogReturn(
        /**
         * The array of canvas function calls.
         */
        public val commandLog: List<Map<String, JsonElement>>,
    )
}
