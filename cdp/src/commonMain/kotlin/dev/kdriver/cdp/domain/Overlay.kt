@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kdriver.cdp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * This domain provides various functionality related to drawing atop the inspected page.
 */
public val CDP.overlay: Overlay
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Overlay(this))

/**
 * This domain provides various functionality related to drawing atop the inspected page.
 */
public class Overlay(
    private val cdp: CDP,
) : Domain {
    /**
     * Fired when the node should be inspected. This happens after call to `setInspectMode` or when
     * user manually inspects an element.
     */
    public val inspectNodeRequested: Flow<InspectNodeRequestedParameter> = cdp
        .events
        .filter { it.method == "Overlay.inspectNodeRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when the node should be highlighted. This happens after call to `setInspectMode`.
     */
    public val nodeHighlightRequested: Flow<NodeHighlightRequestedParameter> = cdp
        .events
        .filter { it.method == "Overlay.nodeHighlightRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when user asks to capture screenshot of some area on the page.
     */
    public val screenshotRequested: Flow<ScreenshotRequestedParameter> = cdp
        .events
        .filter { it.method == "Overlay.screenshotRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when user cancels the inspect mode.
     */
    public val inspectModeCanceled: Flow<Unit> = cdp
        .events
        .filter { it.method == "Overlay.inspectModeCanceled" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Disables domain notifications.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Overlay.disable", parameter, mode)
    }

    /**
     * Enables domain notifications.
     */
    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Overlay.enable", parameter, mode)
    }

    /**
     * For testing.
     */
    public suspend fun getHighlightObjectForTest(
        args: GetHighlightObjectForTestParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetHighlightObjectForTestReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Overlay.getHighlightObjectForTest", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * For testing.
     *
     * @param nodeId Id of the node to get highlight object for.
     * @param includeDistance Whether to include distance info.
     * @param includeStyle Whether to include style info.
     * @param colorFormat The color format to get config with (default: hex).
     * @param showAccessibilityInfo Whether to show accessibility info (default: true).
     */
    public suspend fun getHighlightObjectForTest(
        nodeId: Int,
        includeDistance: Boolean? = null,
        includeStyle: Boolean? = null,
        colorFormat: ColorFormat? = null,
        showAccessibilityInfo: Boolean? = null,
    ): GetHighlightObjectForTestReturn {
        val parameter = GetHighlightObjectForTestParameter(
            nodeId = nodeId,
            includeDistance = includeDistance,
            includeStyle = includeStyle,
            colorFormat = colorFormat,
            showAccessibilityInfo = showAccessibilityInfo
        )
        return getHighlightObjectForTest(parameter)
    }

    /**
     * For Persistent Grid testing.
     */
    public suspend fun getGridHighlightObjectsForTest(
        args: GetGridHighlightObjectsForTestParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetGridHighlightObjectsForTestReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Overlay.getGridHighlightObjectsForTest", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * For Persistent Grid testing.
     *
     * @param nodeIds Ids of the node to get highlight object for.
     */
    public suspend fun getGridHighlightObjectsForTest(nodeIds: List<Int>): GetGridHighlightObjectsForTestReturn {
        val parameter = GetGridHighlightObjectsForTestParameter(nodeIds = nodeIds)
        return getGridHighlightObjectsForTest(parameter)
    }

    /**
     * For Source Order Viewer testing.
     */
    public suspend fun getSourceOrderHighlightObjectForTest(
        args: GetSourceOrderHighlightObjectForTestParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetSourceOrderHighlightObjectForTestReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Overlay.getSourceOrderHighlightObjectForTest", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * For Source Order Viewer testing.
     *
     * @param nodeId Id of the node to highlight.
     */
    public suspend fun getSourceOrderHighlightObjectForTest(nodeId: Int): GetSourceOrderHighlightObjectForTestReturn {
        val parameter = GetSourceOrderHighlightObjectForTestParameter(nodeId = nodeId)
        return getSourceOrderHighlightObjectForTest(parameter)
    }

    /**
     * Hides any highlight.
     */
    public suspend fun hideHighlight(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Overlay.hideHighlight", parameter, mode)
    }

    /**
     * Highlights owner element of the frame with given id.
     * Deprecated: Doesn't work reliably and cannot be fixed due to process
     * separation (the owner node might be in a different process). Determine
     * the owner node in the client and use highlightNode.
     */
    @Deprecated(message = "")
    public suspend fun highlightFrame(args: HighlightFrameParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.highlightFrame", parameter, mode)
    }

    /**
     * Highlights owner element of the frame with given id.
     * Deprecated: Doesn't work reliably and cannot be fixed due to process
     * separation (the owner node might be in a different process). Determine
     * the owner node in the client and use highlightNode.
     *
     * @param frameId Identifier of the frame to highlight.
     * @param contentColor The content box highlight fill color (default: transparent).
     * @param contentOutlineColor The content box highlight outline color (default: transparent).
     */
    @Deprecated(message = "")
    public suspend fun highlightFrame(
        frameId: String,
        contentColor: DOM.RGBA? = null,
        contentOutlineColor: DOM.RGBA? = null,
    ) {
        val parameter = HighlightFrameParameter(
            frameId = frameId,
            contentColor = contentColor,
            contentOutlineColor = contentOutlineColor
        )
        highlightFrame(parameter)
    }

    /**
     * Highlights DOM node with given id or with the given JavaScript object wrapper. Either nodeId or
     * objectId must be specified.
     */
    public suspend fun highlightNode(args: HighlightNodeParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.highlightNode", parameter, mode)
    }

    /**
     * Highlights DOM node with given id or with the given JavaScript object wrapper. Either nodeId or
     * objectId must be specified.
     *
     * @param highlightConfig A descriptor for the highlight appearance.
     * @param nodeId Identifier of the node to highlight.
     * @param backendNodeId Identifier of the backend node to highlight.
     * @param objectId JavaScript object id of the node to be highlighted.
     * @param selector Selectors to highlight relevant nodes.
     */
    public suspend fun highlightNode(
        highlightConfig: HighlightConfig,
        nodeId: Int? = null,
        backendNodeId: Int? = null,
        objectId: String? = null,
        selector: String? = null,
    ) {
        val parameter = HighlightNodeParameter(
            highlightConfig = highlightConfig,
            nodeId = nodeId,
            backendNodeId = backendNodeId,
            objectId = objectId,
            selector = selector
        )
        highlightNode(parameter)
    }

    /**
     * Highlights given quad. Coordinates are absolute with respect to the main frame viewport.
     */
    public suspend fun highlightQuad(args: HighlightQuadParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.highlightQuad", parameter, mode)
    }

    /**
     * Highlights given quad. Coordinates are absolute with respect to the main frame viewport.
     *
     * @param quad Quad to highlight
     * @param color The highlight fill color (default: transparent).
     * @param outlineColor The highlight outline color (default: transparent).
     */
    public suspend fun highlightQuad(
        quad: List<Double>,
        color: DOM.RGBA? = null,
        outlineColor: DOM.RGBA? = null,
    ) {
        val parameter = HighlightQuadParameter(quad = quad, color = color, outlineColor = outlineColor)
        highlightQuad(parameter)
    }

    /**
     * Highlights given rectangle. Coordinates are absolute with respect to the main frame viewport.
     */
    public suspend fun highlightRect(args: HighlightRectParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.highlightRect", parameter, mode)
    }

    /**
     * Highlights given rectangle. Coordinates are absolute with respect to the main frame viewport.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Rectangle width
     * @param height Rectangle height
     * @param color The highlight fill color (default: transparent).
     * @param outlineColor The highlight outline color (default: transparent).
     */
    public suspend fun highlightRect(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: DOM.RGBA? = null,
        outlineColor: DOM.RGBA? = null,
    ) {
        val parameter = HighlightRectParameter(
            x = x,
            y = y,
            width = width,
            height = height,
            color = color,
            outlineColor = outlineColor
        )
        highlightRect(parameter)
    }

    /**
     * Highlights the source order of the children of the DOM node with given id or with the given
     * JavaScript object wrapper. Either nodeId or objectId must be specified.
     */
    public suspend fun highlightSourceOrder(
        args: HighlightSourceOrderParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.highlightSourceOrder", parameter, mode)
    }

    /**
     * Highlights the source order of the children of the DOM node with given id or with the given
     * JavaScript object wrapper. Either nodeId or objectId must be specified.
     *
     * @param sourceOrderConfig A descriptor for the appearance of the overlay drawing.
     * @param nodeId Identifier of the node to highlight.
     * @param backendNodeId Identifier of the backend node to highlight.
     * @param objectId JavaScript object id of the node to be highlighted.
     */
    public suspend fun highlightSourceOrder(
        sourceOrderConfig: SourceOrderConfig,
        nodeId: Int? = null,
        backendNodeId: Int? = null,
        objectId: String? = null,
    ) {
        val parameter = HighlightSourceOrderParameter(
            sourceOrderConfig = sourceOrderConfig,
            nodeId = nodeId,
            backendNodeId = backendNodeId,
            objectId = objectId
        )
        highlightSourceOrder(parameter)
    }

    /**
     * Enters the 'inspect' mode. In this mode, elements that user is hovering over are highlighted.
     * Backend then generates 'inspectNodeRequested' event upon element selection.
     */
    public suspend fun setInspectMode(args: SetInspectModeParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setInspectMode", parameter, mode)
    }

    /**
     * Enters the 'inspect' mode. In this mode, elements that user is hovering over are highlighted.
     * Backend then generates 'inspectNodeRequested' event upon element selection.
     *
     * @param mode Set an inspection mode.
     * @param highlightConfig A descriptor for the highlight appearance of hovered-over nodes. May be omitted if `enabled
     * == false`.
     */
    public suspend fun setInspectMode(mode: InspectMode, highlightConfig: HighlightConfig? = null) {
        val parameter = SetInspectModeParameter(mode = mode, highlightConfig = highlightConfig)
        setInspectMode(parameter)
    }

    /**
     * Highlights owner element of all frames detected to be ads.
     */
    public suspend fun setShowAdHighlights(
        args: SetShowAdHighlightsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowAdHighlights", parameter, mode)
    }

    /**
     * Highlights owner element of all frames detected to be ads.
     *
     * @param show True for showing ad highlights
     */
    public suspend fun setShowAdHighlights(show: Boolean) {
        val parameter = SetShowAdHighlightsParameter(show = show)
        setShowAdHighlights(parameter)
    }

    public suspend fun setPausedInDebuggerMessage(
        args: SetPausedInDebuggerMessageParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setPausedInDebuggerMessage", parameter, mode)
    }

    /**
     *
     *
     * @param message The message to display, also triggers resume and step over controls.
     */
    public suspend fun setPausedInDebuggerMessage(message: String? = null) {
        val parameter = SetPausedInDebuggerMessageParameter(message = message)
        setPausedInDebuggerMessage(parameter)
    }

    /**
     * Requests that backend shows debug borders on layers
     */
    public suspend fun setShowDebugBorders(
        args: SetShowDebugBordersParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowDebugBorders", parameter, mode)
    }

    /**
     * Requests that backend shows debug borders on layers
     *
     * @param show True for showing debug borders
     */
    public suspend fun setShowDebugBorders(show: Boolean) {
        val parameter = SetShowDebugBordersParameter(show = show)
        setShowDebugBorders(parameter)
    }

    /**
     * Requests that backend shows the FPS counter
     */
    public suspend fun setShowFPSCounter(args: SetShowFPSCounterParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowFPSCounter", parameter, mode)
    }

    /**
     * Requests that backend shows the FPS counter
     *
     * @param show True for showing the FPS counter
     */
    public suspend fun setShowFPSCounter(show: Boolean) {
        val parameter = SetShowFPSCounterParameter(show = show)
        setShowFPSCounter(parameter)
    }

    /**
     * Highlight multiple elements with the CSS Grid overlay.
     */
    public suspend fun setShowGridOverlays(
        args: SetShowGridOverlaysParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowGridOverlays", parameter, mode)
    }

    /**
     * Highlight multiple elements with the CSS Grid overlay.
     *
     * @param gridNodeHighlightConfigs An array of node identifiers and descriptors for the highlight appearance.
     */
    public suspend fun setShowGridOverlays(gridNodeHighlightConfigs: List<GridNodeHighlightConfig>) {
        val parameter = SetShowGridOverlaysParameter(gridNodeHighlightConfigs = gridNodeHighlightConfigs)
        setShowGridOverlays(parameter)
    }

    public suspend fun setShowFlexOverlays(
        args: SetShowFlexOverlaysParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowFlexOverlays", parameter, mode)
    }

    /**
     *
     *
     * @param flexNodeHighlightConfigs An array of node identifiers and descriptors for the highlight appearance.
     */
    public suspend fun setShowFlexOverlays(flexNodeHighlightConfigs: List<FlexNodeHighlightConfig>) {
        val parameter = SetShowFlexOverlaysParameter(flexNodeHighlightConfigs = flexNodeHighlightConfigs)
        setShowFlexOverlays(parameter)
    }

    public suspend fun setShowScrollSnapOverlays(
        args: SetShowScrollSnapOverlaysParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowScrollSnapOverlays", parameter, mode)
    }

    /**
     *
     *
     * @param scrollSnapHighlightConfigs An array of node identifiers and descriptors for the highlight appearance.
     */
    public suspend fun setShowScrollSnapOverlays(scrollSnapHighlightConfigs: List<ScrollSnapHighlightConfig>) {
        val parameter = SetShowScrollSnapOverlaysParameter(scrollSnapHighlightConfigs = scrollSnapHighlightConfigs)
        setShowScrollSnapOverlays(parameter)
    }

    public suspend fun setShowContainerQueryOverlays(
        args: SetShowContainerQueryOverlaysParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowContainerQueryOverlays", parameter, mode)
    }

    /**
     *
     *
     * @param containerQueryHighlightConfigs An array of node identifiers and descriptors for the highlight appearance.
     */
    public suspend fun setShowContainerQueryOverlays(containerQueryHighlightConfigs: List<ContainerQueryHighlightConfig>) {
        val parameter =
            SetShowContainerQueryOverlaysParameter(containerQueryHighlightConfigs = containerQueryHighlightConfigs)
        setShowContainerQueryOverlays(parameter)
    }

    /**
     * Requests that backend shows paint rectangles
     */
    public suspend fun setShowPaintRects(args: SetShowPaintRectsParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowPaintRects", parameter, mode)
    }

    /**
     * Requests that backend shows paint rectangles
     *
     * @param result True for showing paint rectangles
     */
    public suspend fun setShowPaintRects(result: Boolean) {
        val parameter = SetShowPaintRectsParameter(result = result)
        setShowPaintRects(parameter)
    }

    /**
     * Requests that backend shows layout shift regions
     */
    public suspend fun setShowLayoutShiftRegions(
        args: SetShowLayoutShiftRegionsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowLayoutShiftRegions", parameter, mode)
    }

    /**
     * Requests that backend shows layout shift regions
     *
     * @param result True for showing layout shift regions
     */
    public suspend fun setShowLayoutShiftRegions(result: Boolean) {
        val parameter = SetShowLayoutShiftRegionsParameter(result = result)
        setShowLayoutShiftRegions(parameter)
    }

    /**
     * Requests that backend shows scroll bottleneck rects
     */
    public suspend fun setShowScrollBottleneckRects(
        args: SetShowScrollBottleneckRectsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowScrollBottleneckRects", parameter, mode)
    }

    /**
     * Requests that backend shows scroll bottleneck rects
     *
     * @param show True for showing scroll bottleneck rects
     */
    public suspend fun setShowScrollBottleneckRects(show: Boolean) {
        val parameter = SetShowScrollBottleneckRectsParameter(show = show)
        setShowScrollBottleneckRects(parameter)
    }

    /**
     * Deprecated, no longer has any effect.
     */
    @Deprecated(message = "")
    public suspend fun setShowHitTestBorders(
        args: SetShowHitTestBordersParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowHitTestBorders", parameter, mode)
    }

    /**
     * Deprecated, no longer has any effect.
     *
     * @param show True for showing hit-test borders
     */
    @Deprecated(message = "")
    public suspend fun setShowHitTestBorders(show: Boolean) {
        val parameter = SetShowHitTestBordersParameter(show = show)
        setShowHitTestBorders(parameter)
    }

    /**
     * Deprecated, no longer has any effect.
     */
    @Deprecated(message = "")
    public suspend fun setShowWebVitals(args: SetShowWebVitalsParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowWebVitals", parameter, mode)
    }

    /**
     * Deprecated, no longer has any effect.
     *
     * @param show No description
     */
    @Deprecated(message = "")
    public suspend fun setShowWebVitals(show: Boolean) {
        val parameter = SetShowWebVitalsParameter(show = show)
        setShowWebVitals(parameter)
    }

    /**
     * Paints viewport size upon main frame resize.
     */
    public suspend fun setShowViewportSizeOnResize(
        args: SetShowViewportSizeOnResizeParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowViewportSizeOnResize", parameter, mode)
    }

    /**
     * Paints viewport size upon main frame resize.
     *
     * @param show Whether to paint size or not.
     */
    public suspend fun setShowViewportSizeOnResize(show: Boolean) {
        val parameter = SetShowViewportSizeOnResizeParameter(show = show)
        setShowViewportSizeOnResize(parameter)
    }

    /**
     * Add a dual screen device hinge
     */
    public suspend fun setShowHinge(args: SetShowHingeParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowHinge", parameter, mode)
    }

    /**
     * Add a dual screen device hinge
     *
     * @param hingeConfig hinge data, null means hideHinge
     */
    public suspend fun setShowHinge(hingeConfig: HingeConfig? = null) {
        val parameter = SetShowHingeParameter(hingeConfig = hingeConfig)
        setShowHinge(parameter)
    }

    /**
     * Show elements in isolation mode with overlays.
     */
    public suspend fun setShowIsolatedElements(
        args: SetShowIsolatedElementsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowIsolatedElements", parameter, mode)
    }

    /**
     * Show elements in isolation mode with overlays.
     *
     * @param isolatedElementHighlightConfigs An array of node identifiers and descriptors for the highlight appearance.
     */
    public suspend fun setShowIsolatedElements(isolatedElementHighlightConfigs: List<IsolatedElementHighlightConfig>) {
        val parameter =
            SetShowIsolatedElementsParameter(isolatedElementHighlightConfigs = isolatedElementHighlightConfigs)
        setShowIsolatedElements(parameter)
    }

    /**
     * Show Window Controls Overlay for PWA
     */
    public suspend fun setShowWindowControlsOverlay(
        args: SetShowWindowControlsOverlayParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Overlay.setShowWindowControlsOverlay", parameter, mode)
    }

    /**
     * Show Window Controls Overlay for PWA
     *
     * @param windowControlsOverlayConfig Window Controls Overlay data, null means hide Window Controls Overlay
     */
    public suspend fun setShowWindowControlsOverlay(windowControlsOverlayConfig: WindowControlsOverlayConfig? = null) {
        val parameter = SetShowWindowControlsOverlayParameter(windowControlsOverlayConfig = windowControlsOverlayConfig)
        setShowWindowControlsOverlay(parameter)
    }

    /**
     * Configuration data for drawing the source order of an elements children.
     */
    @Serializable
    public data class SourceOrderConfig(
        /**
         * the color to outline the given element in.
         */
        public val parentOutlineColor: DOM.RGBA,
        /**
         * the color to outline the child elements in.
         */
        public val childOutlineColor: DOM.RGBA,
    )

    /**
     * Configuration data for the highlighting of Grid elements.
     */
    @Serializable
    public data class GridHighlightConfig(
        /**
         * Whether the extension lines from grid cells to the rulers should be shown (default: false).
         */
        public val showGridExtensionLines: Boolean? = null,
        /**
         * Show Positive line number labels (default: false).
         */
        public val showPositiveLineNumbers: Boolean? = null,
        /**
         * Show Negative line number labels (default: false).
         */
        public val showNegativeLineNumbers: Boolean? = null,
        /**
         * Show area name labels (default: false).
         */
        public val showAreaNames: Boolean? = null,
        /**
         * Show line name labels (default: false).
         */
        public val showLineNames: Boolean? = null,
        /**
         * Show track size labels (default: false).
         */
        public val showTrackSizes: Boolean? = null,
        /**
         * The grid container border highlight color (default: transparent).
         */
        public val gridBorderColor: DOM.RGBA? = null,
        /**
         * The cell border color (default: transparent). Deprecated, please use rowLineColor and columnLineColor instead.
         */
        public val cellBorderColor: DOM.RGBA? = null,
        /**
         * The row line color (default: transparent).
         */
        public val rowLineColor: DOM.RGBA? = null,
        /**
         * The column line color (default: transparent).
         */
        public val columnLineColor: DOM.RGBA? = null,
        /**
         * Whether the grid border is dashed (default: false).
         */
        public val gridBorderDash: Boolean? = null,
        /**
         * Whether the cell border is dashed (default: false). Deprecated, please us rowLineDash and columnLineDash instead.
         */
        public val cellBorderDash: Boolean? = null,
        /**
         * Whether row lines are dashed (default: false).
         */
        public val rowLineDash: Boolean? = null,
        /**
         * Whether column lines are dashed (default: false).
         */
        public val columnLineDash: Boolean? = null,
        /**
         * The row gap highlight fill color (default: transparent).
         */
        public val rowGapColor: DOM.RGBA? = null,
        /**
         * The row gap hatching fill color (default: transparent).
         */
        public val rowHatchColor: DOM.RGBA? = null,
        /**
         * The column gap highlight fill color (default: transparent).
         */
        public val columnGapColor: DOM.RGBA? = null,
        /**
         * The column gap hatching fill color (default: transparent).
         */
        public val columnHatchColor: DOM.RGBA? = null,
        /**
         * The named grid areas border color (Default: transparent).
         */
        public val areaBorderColor: DOM.RGBA? = null,
        /**
         * The grid container background color (Default: transparent).
         */
        public val gridBackgroundColor: DOM.RGBA? = null,
    )

    /**
     * Configuration data for the highlighting of Flex container elements.
     */
    @Serializable
    public data class FlexContainerHighlightConfig(
        /**
         * The style of the container border
         */
        public val containerBorder: LineStyle? = null,
        /**
         * The style of the separator between lines
         */
        public val lineSeparator: LineStyle? = null,
        /**
         * The style of the separator between items
         */
        public val itemSeparator: LineStyle? = null,
        /**
         * Style of content-distribution space on the main axis (justify-content).
         */
        public val mainDistributedSpace: BoxStyle? = null,
        /**
         * Style of content-distribution space on the cross axis (align-content).
         */
        public val crossDistributedSpace: BoxStyle? = null,
        /**
         * Style of empty space caused by row gaps (gap/row-gap).
         */
        public val rowGapSpace: BoxStyle? = null,
        /**
         * Style of empty space caused by columns gaps (gap/column-gap).
         */
        public val columnGapSpace: BoxStyle? = null,
        /**
         * Style of the self-alignment line (align-items).
         */
        public val crossAlignment: LineStyle? = null,
    )

    /**
     * Configuration data for the highlighting of Flex item elements.
     */
    @Serializable
    public data class FlexItemHighlightConfig(
        /**
         * Style of the box representing the item's base size
         */
        public val baseSizeBox: BoxStyle? = null,
        /**
         * Style of the border around the box representing the item's base size
         */
        public val baseSizeBorder: LineStyle? = null,
        /**
         * Style of the arrow representing if the item grew or shrank
         */
        public val flexibilityArrow: LineStyle? = null,
    )

    /**
     * Style information for drawing a line.
     */
    @Serializable
    public data class LineStyle(
        /**
         * The color of the line (default: transparent)
         */
        public val color: DOM.RGBA? = null,
        /**
         * The line pattern (default: solid)
         */
        public val pattern: String? = null,
    )

    /**
     * Style information for drawing a box.
     */
    @Serializable
    public data class BoxStyle(
        /**
         * The background color for the box (default: transparent)
         */
        public val fillColor: DOM.RGBA? = null,
        /**
         * The hatching color for the box (default: transparent)
         */
        public val hatchColor: DOM.RGBA? = null,
    )

    @Serializable
    public enum class ContrastAlgorithm {
        @SerialName("aa")
        AA,

        @SerialName("aaa")
        AAA,

        @SerialName("apca")
        APCA,
    }

    /**
     * Configuration data for the highlighting of page elements.
     */
    @Serializable
    public data class HighlightConfig(
        /**
         * Whether the node info tooltip should be shown (default: false).
         */
        public val showInfo: Boolean? = null,
        /**
         * Whether the node styles in the tooltip (default: false).
         */
        public val showStyles: Boolean? = null,
        /**
         * Whether the rulers should be shown (default: false).
         */
        public val showRulers: Boolean? = null,
        /**
         * Whether the a11y info should be shown (default: true).
         */
        public val showAccessibilityInfo: Boolean? = null,
        /**
         * Whether the extension lines from node to the rulers should be shown (default: false).
         */
        public val showExtensionLines: Boolean? = null,
        /**
         * The content box highlight fill color (default: transparent).
         */
        public val contentColor: DOM.RGBA? = null,
        /**
         * The padding highlight fill color (default: transparent).
         */
        public val paddingColor: DOM.RGBA? = null,
        /**
         * The border highlight fill color (default: transparent).
         */
        public val borderColor: DOM.RGBA? = null,
        /**
         * The margin highlight fill color (default: transparent).
         */
        public val marginColor: DOM.RGBA? = null,
        /**
         * The event target element highlight fill color (default: transparent).
         */
        public val eventTargetColor: DOM.RGBA? = null,
        /**
         * The shape outside fill color (default: transparent).
         */
        public val shapeColor: DOM.RGBA? = null,
        /**
         * The shape margin fill color (default: transparent).
         */
        public val shapeMarginColor: DOM.RGBA? = null,
        /**
         * The grid layout color (default: transparent).
         */
        public val cssGridColor: DOM.RGBA? = null,
        /**
         * The color format used to format color styles (default: hex).
         */
        public val colorFormat: ColorFormat? = null,
        /**
         * The grid layout highlight configuration (default: all transparent).
         */
        public val gridHighlightConfig: GridHighlightConfig? = null,
        /**
         * The flex container highlight configuration (default: all transparent).
         */
        public val flexContainerHighlightConfig: FlexContainerHighlightConfig? = null,
        /**
         * The flex item highlight configuration (default: all transparent).
         */
        public val flexItemHighlightConfig: FlexItemHighlightConfig? = null,
        /**
         * The contrast algorithm to use for the contrast ratio (default: aa).
         */
        public val contrastAlgorithm: ContrastAlgorithm? = null,
        /**
         * The container query container highlight configuration (default: all transparent).
         */
        public val containerQueryContainerHighlightConfig:
        ContainerQueryContainerHighlightConfig? = null,
    )

    @Serializable
    public enum class ColorFormat {
        @SerialName("rgb")
        RGB,

        @SerialName("hsl")
        HSL,

        @SerialName("hwb")
        HWB,

        @SerialName("hex")
        HEX,
    }

    /**
     * Configurations for Persistent Grid Highlight
     */
    @Serializable
    public data class GridNodeHighlightConfig(
        /**
         * A descriptor for the highlight appearance.
         */
        public val gridHighlightConfig: GridHighlightConfig,
        /**
         * Identifier of the node to highlight.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class FlexNodeHighlightConfig(
        /**
         * A descriptor for the highlight appearance of flex containers.
         */
        public val flexContainerHighlightConfig: FlexContainerHighlightConfig,
        /**
         * Identifier of the node to highlight.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class ScrollSnapContainerHighlightConfig(
        /**
         * The style of the snapport border (default: transparent)
         */
        public val snapportBorder: LineStyle? = null,
        /**
         * The style of the snap area border (default: transparent)
         */
        public val snapAreaBorder: LineStyle? = null,
        /**
         * The margin highlight fill color (default: transparent).
         */
        public val scrollMarginColor: DOM.RGBA? = null,
        /**
         * The padding highlight fill color (default: transparent).
         */
        public val scrollPaddingColor: DOM.RGBA? = null,
    )

    @Serializable
    public data class ScrollSnapHighlightConfig(
        /**
         * A descriptor for the highlight appearance of scroll snap containers.
         */
        public val scrollSnapContainerHighlightConfig: ScrollSnapContainerHighlightConfig,
        /**
         * Identifier of the node to highlight.
         */
        public val nodeId: Int,
    )

    /**
     * Configuration for dual screen hinge
     */
    @Serializable
    public data class HingeConfig(
        /**
         * A rectangle represent hinge
         */
        public val rect: DOM.Rect,
        /**
         * The content box highlight fill color (default: a dark color).
         */
        public val contentColor: DOM.RGBA? = null,
        /**
         * The content box highlight outline color (default: transparent).
         */
        public val outlineColor: DOM.RGBA? = null,
    )

    /**
     * Configuration for Window Controls Overlay
     */
    @Serializable
    public data class WindowControlsOverlayConfig(
        /**
         * Whether the title bar CSS should be shown when emulating the Window Controls Overlay.
         */
        public val showCSS: Boolean,
        /**
         * Selected platforms to show the overlay.
         */
        public val selectedPlatform: String,
        /**
         * The theme color defined in app manifest.
         */
        public val themeColor: String,
    )

    @Serializable
    public data class ContainerQueryHighlightConfig(
        /**
         * A descriptor for the highlight appearance of container query containers.
         */
        public val containerQueryContainerHighlightConfig: ContainerQueryContainerHighlightConfig,
        /**
         * Identifier of the container node to highlight.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class ContainerQueryContainerHighlightConfig(
        /**
         * The style of the container border.
         */
        public val containerBorder: LineStyle? = null,
        /**
         * The style of the descendants' borders.
         */
        public val descendantBorder: LineStyle? = null,
    )

    @Serializable
    public data class IsolatedElementHighlightConfig(
        /**
         * A descriptor for the highlight appearance of an element in isolation mode.
         */
        public val isolationModeHighlightConfig: IsolationModeHighlightConfig,
        /**
         * Identifier of the isolated element to highlight.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class IsolationModeHighlightConfig(
        /**
         * The fill color of the resizers (default: transparent).
         */
        public val resizerColor: DOM.RGBA? = null,
        /**
         * The fill color for resizer handles (default: transparent).
         */
        public val resizerHandleColor: DOM.RGBA? = null,
        /**
         * The fill color for the mask covering non-isolated elements (default: transparent).
         */
        public val maskColor: DOM.RGBA? = null,
    )

    @Serializable
    public enum class InspectMode {
        @SerialName("searchForNode")
        SEARCHFORNODE,

        @SerialName("searchForUAShadowDOM")
        SEARCHFORUASHADOWDOM,

        @SerialName("captureAreaScreenshot")
        CAPTUREAREASCREENSHOT,

        @SerialName("none")
        NONE,
    }

    /**
     * Fired when the node should be inspected. This happens after call to `setInspectMode` or when
     * user manually inspects an element.
     */
    @Serializable
    public data class InspectNodeRequestedParameter(
        /**
         * Id of the node to inspect.
         */
        public val backendNodeId: Int,
    )

    /**
     * Fired when the node should be highlighted. This happens after call to `setInspectMode`.
     */
    @Serializable
    public data class NodeHighlightRequestedParameter(
        public val nodeId: Int,
    )

    /**
     * Fired when user asks to capture screenshot of some area on the page.
     */
    @Serializable
    public data class ScreenshotRequestedParameter(
        /**
         * Viewport to capture, in device independent pixels (dip).
         */
        public val viewport: Page.Viewport,
    )

    @Serializable
    public data class GetHighlightObjectForTestParameter(
        /**
         * Id of the node to get highlight object for.
         */
        public val nodeId: Int,
        /**
         * Whether to include distance info.
         */
        public val includeDistance: Boolean? = null,
        /**
         * Whether to include style info.
         */
        public val includeStyle: Boolean? = null,
        /**
         * The color format to get config with (default: hex).
         */
        public val colorFormat: ColorFormat? = null,
        /**
         * Whether to show accessibility info (default: true).
         */
        public val showAccessibilityInfo: Boolean? = null,
    )

    @Serializable
    public data class GetHighlightObjectForTestReturn(
        /**
         * Highlight data for the node.
         */
        public val highlight: Map<String, JsonElement>,
    )

    @Serializable
    public data class GetGridHighlightObjectsForTestParameter(
        /**
         * Ids of the node to get highlight object for.
         */
        public val nodeIds: List<Int>,
    )

    @Serializable
    public data class GetGridHighlightObjectsForTestReturn(
        /**
         * Grid Highlight data for the node ids provided.
         */
        public val highlights: Map<String, JsonElement>,
    )

    @Serializable
    public data class GetSourceOrderHighlightObjectForTestParameter(
        /**
         * Id of the node to highlight.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class GetSourceOrderHighlightObjectForTestReturn(
        /**
         * Source order highlight data for the node id provided.
         */
        public val highlight: Map<String, JsonElement>,
    )

    @Serializable
    public data class HighlightFrameParameter(
        /**
         * Identifier of the frame to highlight.
         */
        public val frameId: String,
        /**
         * The content box highlight fill color (default: transparent).
         */
        public val contentColor: DOM.RGBA? = null,
        /**
         * The content box highlight outline color (default: transparent).
         */
        public val contentOutlineColor: DOM.RGBA? = null,
    )

    @Serializable
    public data class HighlightNodeParameter(
        /**
         * A descriptor for the highlight appearance.
         */
        public val highlightConfig: HighlightConfig,
        /**
         * Identifier of the node to highlight.
         */
        public val nodeId: Int? = null,
        /**
         * Identifier of the backend node to highlight.
         */
        public val backendNodeId: Int? = null,
        /**
         * JavaScript object id of the node to be highlighted.
         */
        public val objectId: String? = null,
        /**
         * Selectors to highlight relevant nodes.
         */
        public val selector: String? = null,
    )

    @Serializable
    public data class HighlightQuadParameter(
        /**
         * Quad to highlight
         */
        public val quad: List<Double>,
        /**
         * The highlight fill color (default: transparent).
         */
        public val color: DOM.RGBA? = null,
        /**
         * The highlight outline color (default: transparent).
         */
        public val outlineColor: DOM.RGBA? = null,
    )

    @Serializable
    public data class HighlightRectParameter(
        /**
         * X coordinate
         */
        public val x: Int,
        /**
         * Y coordinate
         */
        public val y: Int,
        /**
         * Rectangle width
         */
        public val width: Int,
        /**
         * Rectangle height
         */
        public val height: Int,
        /**
         * The highlight fill color (default: transparent).
         */
        public val color: DOM.RGBA? = null,
        /**
         * The highlight outline color (default: transparent).
         */
        public val outlineColor: DOM.RGBA? = null,
    )

    @Serializable
    public data class HighlightSourceOrderParameter(
        /**
         * A descriptor for the appearance of the overlay drawing.
         */
        public val sourceOrderConfig: SourceOrderConfig,
        /**
         * Identifier of the node to highlight.
         */
        public val nodeId: Int? = null,
        /**
         * Identifier of the backend node to highlight.
         */
        public val backendNodeId: Int? = null,
        /**
         * JavaScript object id of the node to be highlighted.
         */
        public val objectId: String? = null,
    )

    @Serializable
    public data class SetInspectModeParameter(
        /**
         * Set an inspection mode.
         */
        public val mode: InspectMode,
        /**
         * A descriptor for the highlight appearance of hovered-over nodes. May be omitted if `enabled
         * == false`.
         */
        public val highlightConfig: HighlightConfig? = null,
    )

    @Serializable
    public data class SetShowAdHighlightsParameter(
        /**
         * True for showing ad highlights
         */
        public val show: Boolean,
    )

    @Serializable
    public data class SetPausedInDebuggerMessageParameter(
        /**
         * The message to display, also triggers resume and step over controls.
         */
        public val message: String? = null,
    )

    @Serializable
    public data class SetShowDebugBordersParameter(
        /**
         * True for showing debug borders
         */
        public val show: Boolean,
    )

    @Serializable
    public data class SetShowFPSCounterParameter(
        /**
         * True for showing the FPS counter
         */
        public val show: Boolean,
    )

    @Serializable
    public data class SetShowGridOverlaysParameter(
        /**
         * An array of node identifiers and descriptors for the highlight appearance.
         */
        public val gridNodeHighlightConfigs: List<GridNodeHighlightConfig>,
    )

    @Serializable
    public data class SetShowFlexOverlaysParameter(
        /**
         * An array of node identifiers and descriptors for the highlight appearance.
         */
        public val flexNodeHighlightConfigs: List<FlexNodeHighlightConfig>,
    )

    @Serializable
    public data class SetShowScrollSnapOverlaysParameter(
        /**
         * An array of node identifiers and descriptors for the highlight appearance.
         */
        public val scrollSnapHighlightConfigs: List<ScrollSnapHighlightConfig>,
    )

    @Serializable
    public data class SetShowContainerQueryOverlaysParameter(
        /**
         * An array of node identifiers and descriptors for the highlight appearance.
         */
        public val containerQueryHighlightConfigs: List<ContainerQueryHighlightConfig>,
    )

    @Serializable
    public data class SetShowPaintRectsParameter(
        /**
         * True for showing paint rectangles
         */
        public val result: Boolean,
    )

    @Serializable
    public data class SetShowLayoutShiftRegionsParameter(
        /**
         * True for showing layout shift regions
         */
        public val result: Boolean,
    )

    @Serializable
    public data class SetShowScrollBottleneckRectsParameter(
        /**
         * True for showing scroll bottleneck rects
         */
        public val show: Boolean,
    )

    @Serializable
    public data class SetShowHitTestBordersParameter(
        /**
         * True for showing hit-test borders
         */
        public val show: Boolean,
    )

    @Serializable
    public data class SetShowWebVitalsParameter(
        public val show: Boolean,
    )

    @Serializable
    public data class SetShowViewportSizeOnResizeParameter(
        /**
         * Whether to paint size or not.
         */
        public val show: Boolean,
    )

    @Serializable
    public data class SetShowHingeParameter(
        /**
         * hinge data, null means hideHinge
         */
        public val hingeConfig: HingeConfig? = null,
    )

    @Serializable
    public data class SetShowIsolatedElementsParameter(
        /**
         * An array of node identifiers and descriptors for the highlight appearance.
         */
        public val isolatedElementHighlightConfigs: List<IsolatedElementHighlightConfig>,
    )

    @Serializable
    public data class SetShowWindowControlsOverlayParameter(
        /**
         * Window Controls Overlay data, null means hide Window Controls Overlay
         */
        public val windowControlsOverlayConfig: WindowControlsOverlayConfig? = null,
    )
}
