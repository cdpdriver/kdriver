package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.dOMSnapshot: DOMSnapshot
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(DOMSnapshot(this))

/**
 * This domain facilitates obtaining document snapshots with DOM, layout, and style information.
 */
public class DOMSnapshot(
    private val cdp: CDP,
) : Domain {
    /**
     * Disables DOM snapshot agent for the given page.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("DOMSnapshot.disable", parameter)
    }

    /**
     * Enables DOM snapshot agent for the given page.
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("DOMSnapshot.enable", parameter)
    }

    /**
     * Returns a document snapshot, including the full DOM tree of the root node (including iframes,
     * template contents, and imported documents) in a flattened array, as well as layout and
     * white-listed computed style information for the nodes. Shadow DOM in the returned DOM tree is
     * flattened.
     */
    @Deprecated(message = "")
    public suspend fun getSnapshot(args: GetSnapshotParameter): GetSnapshotReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOMSnapshot.getSnapshot", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns a document snapshot, including the full DOM tree of the root node (including iframes,
     * template contents, and imported documents) in a flattened array, as well as layout and
     * white-listed computed style information for the nodes. Shadow DOM in the returned DOM tree is
     * flattened.
     */
    @Deprecated(message = "")
    public suspend fun getSnapshot(
        computedStyleWhitelist: List<String>,
        includeEventListeners: Boolean? = null,
        includePaintOrder: Boolean? = null,
        includeUserAgentShadowTree: Boolean? = null,
    ): GetSnapshotReturn {
        val parameter = GetSnapshotParameter(
            computedStyleWhitelist = computedStyleWhitelist,
            includeEventListeners = includeEventListeners,
            includePaintOrder = includePaintOrder,
            includeUserAgentShadowTree = includeUserAgentShadowTree
        )
        return getSnapshot(parameter)
    }

    /**
     * Returns a document snapshot, including the full DOM tree of the root node (including iframes,
     * template contents, and imported documents) in a flattened array, as well as layout and
     * white-listed computed style information for the nodes. Shadow DOM in the returned DOM tree is
     * flattened.
     */
    public suspend fun captureSnapshot(args: CaptureSnapshotParameter): CaptureSnapshotReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOMSnapshot.captureSnapshot", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns a document snapshot, including the full DOM tree of the root node (including iframes,
     * template contents, and imported documents) in a flattened array, as well as layout and
     * white-listed computed style information for the nodes. Shadow DOM in the returned DOM tree is
     * flattened.
     */
    public suspend fun captureSnapshot(
        computedStyles: List<String>,
        includePaintOrder: Boolean? = null,
        includeDOMRects: Boolean? = null,
        includeBlendedBackgroundColors: Boolean? = null,
        includeTextColorOpacities: Boolean? = null,
    ): CaptureSnapshotReturn {
        val parameter = CaptureSnapshotParameter(
            computedStyles = computedStyles,
            includePaintOrder = includePaintOrder,
            includeDOMRects = includeDOMRects,
            includeBlendedBackgroundColors = includeBlendedBackgroundColors,
            includeTextColorOpacities = includeTextColorOpacities
        )
        return captureSnapshot(parameter)
    }

    /**
     * A Node in the DOM tree.
     */
    @Serializable
    public data class DOMNode(
        /**
         * `Node`'s nodeType.
         */
        public val nodeType: Int,
        /**
         * `Node`'s nodeName.
         */
        public val nodeName: String,
        /**
         * `Node`'s nodeValue.
         */
        public val nodeValue: String,
        /**
         * Only set for textarea elements, contains the text value.
         */
        public val textValue: String? = null,
        /**
         * Only set for input elements, contains the input's associated text value.
         */
        public val inputValue: String? = null,
        /**
         * Only set for radio and checkbox input elements, indicates if the element has been checked
         */
        public val inputChecked: Boolean? = null,
        /**
         * Only set for option elements, indicates if the element has been selected
         */
        public val optionSelected: Boolean? = null,
        /**
         * `Node`'s id, corresponds to DOM.Node.backendNodeId.
         */
        public val backendNodeId: Int,
        /**
         * The indexes of the node's child nodes in the `domNodes` array returned by `getSnapshot`, if
         * any.
         */
        public val childNodeIndexes: List<Int>? = null,
        /**
         * Attributes of an `Element` node.
         */
        public val attributes: List<NameValue>? = null,
        /**
         * Indexes of pseudo elements associated with this node in the `domNodes` array returned by
         * `getSnapshot`, if any.
         */
        public val pseudoElementIndexes: List<Int>? = null,
        /**
         * The index of the node's related layout tree node in the `layoutTreeNodes` array returned by
         * `getSnapshot`, if any.
         */
        public val layoutNodeIndex: Int? = null,
        /**
         * Document URL that `Document` or `FrameOwner` node points to.
         */
        public val documentURL: String? = null,
        /**
         * Base URL that `Document` or `FrameOwner` node uses for URL completion.
         */
        public val baseURL: String? = null,
        /**
         * Only set for documents, contains the document's content language.
         */
        public val contentLanguage: String? = null,
        /**
         * Only set for documents, contains the document's character set encoding.
         */
        public val documentEncoding: String? = null,
        /**
         * `DocumentType` node's publicId.
         */
        public val publicId: String? = null,
        /**
         * `DocumentType` node's systemId.
         */
        public val systemId: String? = null,
        /**
         * Frame ID for frame owner elements and also for the document node.
         */
        public val frameId: String? = null,
        /**
         * The index of a frame owner element's content document in the `domNodes` array returned by
         * `getSnapshot`, if any.
         */
        public val contentDocumentIndex: Int? = null,
        /**
         * Type of a pseudo element node.
         */
        public val pseudoType: DOM.PseudoType? = null,
        /**
         * Shadow root type.
         */
        public val shadowRootType: DOM.ShadowRootType? = null,
        /**
         * Whether this DOM node responds to mouse clicks. This includes nodes that have had click
         * event listeners attached via JavaScript as well as anchor tags that naturally navigate when
         * clicked.
         */
        public val isClickable: Boolean? = null,
        /**
         * Details of the node's event listeners, if any.
         */
        public val eventListeners: List<DOMDebugger.EventListener>? = null,
        /**
         * The selected url for nodes with a srcset attribute.
         */
        public val currentSourceURL: String? = null,
        /**
         * The url of the script (if any) that generates this node.
         */
        public val originURL: String? = null,
        /**
         * Scroll offsets, set when this node is a Document.
         */
        public val scrollOffsetX: Double? = null,
        public val scrollOffsetY: Double? = null,
    )

    /**
     * Details of post layout rendered text positions. The exact layout should not be regarded as
     * stable and may change between versions.
     */
    @Serializable
    public data class InlineTextBox(
        /**
         * The bounding box in document coordinates. Note that scroll offset of the document is ignored.
         */
        public val boundingBox: DOM.Rect,
        /**
         * The starting index in characters, for this post layout textbox substring. Characters that
         * would be represented as a surrogate pair in UTF-16 have length 2.
         */
        public val startCharacterIndex: Int,
        /**
         * The number of characters in this post layout textbox substring. Characters that would be
         * represented as a surrogate pair in UTF-16 have length 2.
         */
        public val numCharacters: Int,
    )

    /**
     * Details of an element in the DOM tree with a LayoutObject.
     */
    @Serializable
    public data class LayoutTreeNode(
        /**
         * The index of the related DOM node in the `domNodes` array returned by `getSnapshot`.
         */
        public val domNodeIndex: Int,
        /**
         * The bounding box in document coordinates. Note that scroll offset of the document is ignored.
         */
        public val boundingBox: DOM.Rect,
        /**
         * Contents of the LayoutText, if any.
         */
        public val layoutText: String? = null,
        /**
         * The post-layout inline text nodes, if any.
         */
        public val inlineTextNodes: List<InlineTextBox>? = null,
        /**
         * Index into the `computedStyles` array returned by `getSnapshot`.
         */
        public val styleIndex: Int? = null,
        /**
         * Global paint order index, which is determined by the stacking order of the nodes. Nodes
         * that are painted together will have the same index. Only provided if includePaintOrder in
         * getSnapshot was true.
         */
        public val paintOrder: Int? = null,
        /**
         * Set to true to indicate the element begins a new stacking context.
         */
        public val isStackingContext: Boolean? = null,
    )

    /**
     * A subset of the full ComputedStyle as defined by the request whitelist.
     */
    @Serializable
    public data class ComputedStyle(
        /**
         * Name/value pairs of computed style properties.
         */
        public val properties: List<NameValue>,
    )

    /**
     * A name/value pair.
     */
    @Serializable
    public data class NameValue(
        /**
         * Attribute/property name.
         */
        public val name: String,
        /**
         * Attribute/property value.
         */
        public val `value`: String,
    )

    /**
     * Data that is only present on rare nodes.
     */
    @Serializable
    public data class RareStringData(
        public val index: List<Int>,
        public val `value`: List<Int>,
    )

    @Serializable
    public data class RareBooleanData(
        public val index: List<Int>,
    )

    @Serializable
    public data class RareIntegerData(
        public val index: List<Int>,
        public val `value`: List<Int>,
    )

    /**
     * Document snapshot.
     */
    @Serializable
    public data class DocumentSnapshot(
        /**
         * Document URL that `Document` or `FrameOwner` node points to.
         */
        public val documentURL: Int,
        /**
         * Document title.
         */
        public val title: Int,
        /**
         * Base URL that `Document` or `FrameOwner` node uses for URL completion.
         */
        public val baseURL: Int,
        /**
         * Contains the document's content language.
         */
        public val contentLanguage: Int,
        /**
         * Contains the document's character set encoding.
         */
        public val encodingName: Int,
        /**
         * `DocumentType` node's publicId.
         */
        public val publicId: Int,
        /**
         * `DocumentType` node's systemId.
         */
        public val systemId: Int,
        /**
         * Frame ID for frame owner elements and also for the document node.
         */
        public val frameId: Int,
        /**
         * A table with dom nodes.
         */
        public val nodes: NodeTreeSnapshot,
        /**
         * The nodes in the layout tree.
         */
        public val layout: LayoutTreeSnapshot,
        /**
         * The post-layout inline text nodes.
         */
        public val textBoxes: TextBoxSnapshot,
        /**
         * Horizontal scroll offset.
         */
        public val scrollOffsetX: Double? = null,
        /**
         * Vertical scroll offset.
         */
        public val scrollOffsetY: Double? = null,
        /**
         * Document content width.
         */
        public val contentWidth: Double? = null,
        /**
         * Document content height.
         */
        public val contentHeight: Double? = null,
    )

    /**
     * Table containing nodes.
     */
    @Serializable
    public data class NodeTreeSnapshot(
        /**
         * Parent node index.
         */
        public val parentIndex: List<Int>? = null,
        /**
         * `Node`'s nodeType.
         */
        public val nodeType: List<Int>? = null,
        /**
         * Type of the shadow root the `Node` is in. String values are equal to the `ShadowRootType` enum.
         */
        public val shadowRootType: RareStringData? = null,
        /**
         * `Node`'s nodeName.
         */
        public val nodeName: List<Int>? = null,
        /**
         * `Node`'s nodeValue.
         */
        public val nodeValue: List<Int>? = null,
        /**
         * `Node`'s id, corresponds to DOM.Node.backendNodeId.
         */
        public val backendNodeId: List<Int>? = null,
        /**
         * Attributes of an `Element` node. Flatten name, value pairs.
         */
        public val attributes: List<List<Double>>? = null,
        /**
         * Only set for textarea elements, contains the text value.
         */
        public val textValue: RareStringData? = null,
        /**
         * Only set for input elements, contains the input's associated text value.
         */
        public val inputValue: RareStringData? = null,
        /**
         * Only set for radio and checkbox input elements, indicates if the element has been checked
         */
        public val inputChecked: RareBooleanData? = null,
        /**
         * Only set for option elements, indicates if the element has been selected
         */
        public val optionSelected: RareBooleanData? = null,
        /**
         * The index of the document in the list of the snapshot documents.
         */
        public val contentDocumentIndex: RareIntegerData? = null,
        /**
         * Type of a pseudo element node.
         */
        public val pseudoType: RareStringData? = null,
        /**
         * Pseudo element identifier for this node. Only present if there is a
         * valid pseudoType.
         */
        public val pseudoIdentifier: RareStringData? = null,
        /**
         * Whether this DOM node responds to mouse clicks. This includes nodes that have had click
         * event listeners attached via JavaScript as well as anchor tags that naturally navigate when
         * clicked.
         */
        public val isClickable: RareBooleanData? = null,
        /**
         * The selected url for nodes with a srcset attribute.
         */
        public val currentSourceURL: RareStringData? = null,
        /**
         * The url of the script (if any) that generates this node.
         */
        public val originURL: RareStringData? = null,
    )

    /**
     * Table of details of an element in the DOM tree with a LayoutObject.
     */
    @Serializable
    public data class LayoutTreeSnapshot(
        /**
         * Index of the corresponding node in the `NodeTreeSnapshot` array returned by `captureSnapshot`.
         */
        public val nodeIndex: List<Int>,
        /**
         * Array of indexes specifying computed style strings, filtered according to the `computedStyles` parameter passed to `captureSnapshot`.
         */
        public val styles: List<List<Double>>,
        /**
         * The absolute position bounding box.
         */
        public val bounds: List<List<Double>>,
        /**
         * Contents of the LayoutText, if any.
         */
        public val text: List<Int>,
        /**
         * Stacking context information.
         */
        public val stackingContexts: RareBooleanData,
        /**
         * Global paint order index, which is determined by the stacking order of the nodes. Nodes
         * that are painted together will have the same index. Only provided if includePaintOrder in
         * captureSnapshot was true.
         */
        public val paintOrders: List<Int>? = null,
        /**
         * The offset rect of nodes. Only available when includeDOMRects is set to true
         */
        public val offsetRects: List<List<Double>>? = null,
        /**
         * The scroll rect of nodes. Only available when includeDOMRects is set to true
         */
        public val scrollRects: List<List<Double>>? = null,
        /**
         * The client rect of nodes. Only available when includeDOMRects is set to true
         */
        public val clientRects: List<List<Double>>? = null,
        /**
         * The list of background colors that are blended with colors of overlapping elements.
         */
        public val blendedBackgroundColors: List<Int>? = null,
        /**
         * The list of computed text opacities.
         */
        public val textColorOpacities: List<Double>? = null,
    )

    /**
     * Table of details of the post layout rendered text positions. The exact layout should not be regarded as
     * stable and may change between versions.
     */
    @Serializable
    public data class TextBoxSnapshot(
        /**
         * Index of the layout tree node that owns this box collection.
         */
        public val layoutIndex: List<Int>,
        /**
         * The absolute position bounding box.
         */
        public val bounds: List<List<Double>>,
        /**
         * The starting index in characters, for this post layout textbox substring. Characters that
         * would be represented as a surrogate pair in UTF-16 have length 2.
         */
        public val start: List<Int>,
        /**
         * The number of characters in this post layout textbox substring. Characters that would be
         * represented as a surrogate pair in UTF-16 have length 2.
         */
        public val length: List<Int>,
    )

    @Serializable
    public data class GetSnapshotParameter(
        /**
         * Whitelist of computed styles to return.
         */
        public val computedStyleWhitelist: List<String>,
        /**
         * Whether or not to retrieve details of DOM listeners (default false).
         */
        public val includeEventListeners: Boolean? = null,
        /**
         * Whether to determine and include the paint order index of LayoutTreeNodes (default false).
         */
        public val includePaintOrder: Boolean? = null,
        /**
         * Whether to include UA shadow tree in the snapshot (default false).
         */
        public val includeUserAgentShadowTree: Boolean? = null,
    )

    @Serializable
    public data class GetSnapshotReturn(
        /**
         * The nodes in the DOM tree. The DOMNode at index 0 corresponds to the root document.
         */
        public val domNodes: List<DOMNode>,
        /**
         * The nodes in the layout tree.
         */
        public val layoutTreeNodes: List<LayoutTreeNode>,
        /**
         * Whitelisted ComputedStyle properties for each node in the layout tree.
         */
        public val computedStyles: List<ComputedStyle>,
    )

    @Serializable
    public data class CaptureSnapshotParameter(
        /**
         * Whitelist of computed styles to return.
         */
        public val computedStyles: List<String>,
        /**
         * Whether to include layout object paint orders into the snapshot.
         */
        public val includePaintOrder: Boolean? = null,
        /**
         * Whether to include DOM rectangles (offsetRects, clientRects, scrollRects) into the snapshot
         */
        public val includeDOMRects: Boolean? = null,
        /**
         * Whether to include blended background colors in the snapshot (default: false).
         * Blended background color is achieved by blending background colors of all elements
         * that overlap with the current element.
         */
        public val includeBlendedBackgroundColors: Boolean? = null,
        /**
         * Whether to include text color opacity in the snapshot (default: false).
         * An element might have the opacity property set that affects the text color of the element.
         * The final text color opacity is computed based on the opacity of all overlapping elements.
         */
        public val includeTextColorOpacities: Boolean? = null,
    )

    @Serializable
    public data class CaptureSnapshotReturn(
        /**
         * The nodes in the DOM tree. The DOMNode at index 0 corresponds to the root document.
         */
        public val documents: List<DocumentSnapshot>,
        /**
         * Shared string table that all string properties refer to with indexes.
         */
        public val strings: List<String>,
    )
}
