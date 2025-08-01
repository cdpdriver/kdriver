package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
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

public val CDP.dom: DOM
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(DOM(this))

/**
 * This domain exposes DOM read/write operations. Each DOM Node is represented with its mirror object
 * that has an `id`. This `id` can be used to get additional information on the Node, resolve it into
 * the JavaScript object wrapper, etc. It is important that client receives DOM events only for the
 * nodes that are known to the client. Backend keeps track of the nodes that were sent to the client
 * and never sends the same node twice. It is client's responsibility to collect information about
 * the nodes that were sent to the client. Note that `iframe` owner elements will return
 * corresponding document elements as their child nodes.
 */
public class DOM(
    private val cdp: CDP,
) : Domain {
    /**
     * Fired when `Element`'s attribute is modified.
     */
    public val attributeModified: Flow<AttributeModifiedParameter> = cdp
        .events
        .filter { it.method == "DOM.attributeModified" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when `Element`'s attribute is removed.
     */
    public val attributeRemoved: Flow<AttributeRemovedParameter> = cdp
        .events
        .filter { it.method == "DOM.attributeRemoved" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Mirrors `DOMCharacterDataModified` event.
     */
    public val characterDataModified: Flow<CharacterDataModifiedParameter> = cdp
        .events
        .filter { it.method == "DOM.characterDataModified" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when `Container`'s child node count has changed.
     */
    public val childNodeCountUpdated: Flow<ChildNodeCountUpdatedParameter> = cdp
        .events
        .filter { it.method == "DOM.childNodeCountUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Mirrors `DOMNodeInserted` event.
     */
    public val childNodeInserted: Flow<ChildNodeInsertedParameter> = cdp
        .events
        .filter { it.method == "DOM.childNodeInserted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Mirrors `DOMNodeRemoved` event.
     */
    public val childNodeRemoved: Flow<ChildNodeRemovedParameter> = cdp
        .events
        .filter { it.method == "DOM.childNodeRemoved" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Called when distribution is changed.
     */
    public val distributedNodesUpdated: Flow<DistributedNodesUpdatedParameter> = cdp
        .events
        .filter { it.method == "DOM.distributedNodesUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when `Document` has been totally updated. Node ids are no longer valid.
     */
    public val documentUpdated: Flow<Unit> = cdp
        .events
        .filter { it.method == "DOM.documentUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when `Element`'s inline style is modified via a CSS property modification.
     */
    public val inlineStyleInvalidated: Flow<InlineStyleInvalidatedParameter> = cdp
        .events
        .filter { it.method == "DOM.inlineStyleInvalidated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Called when a pseudo element is added to an element.
     */
    public val pseudoElementAdded: Flow<PseudoElementAddedParameter> = cdp
        .events
        .filter { it.method == "DOM.pseudoElementAdded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Called when top layer elements are changed.
     */
    public val topLayerElementsUpdated: Flow<Unit> = cdp
        .events
        .filter { it.method == "DOM.topLayerElementsUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when a node's scrollability state changes.
     */
    public val scrollableFlagUpdated: Flow<ScrollableFlagUpdatedParameter> = cdp
        .events
        .filter { it.method == "DOM.scrollableFlagUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Called when a pseudo element is removed from an element.
     */
    public val pseudoElementRemoved: Flow<PseudoElementRemovedParameter> = cdp
        .events
        .filter { it.method == "DOM.pseudoElementRemoved" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when backend wants to provide client with the missing DOM structure. This happens upon
     * most of the calls requesting node ids.
     */
    public val setChildNodes: Flow<SetChildNodesParameter> = cdp
        .events
        .filter { it.method == "DOM.setChildNodes" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Called when shadow root is popped from the element.
     */
    public val shadowRootPopped: Flow<ShadowRootPoppedParameter> = cdp
        .events
        .filter { it.method == "DOM.shadowRootPopped" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Called when shadow root is pushed into the element.
     */
    public val shadowRootPushed: Flow<ShadowRootPushedParameter> = cdp
        .events
        .filter { it.method == "DOM.shadowRootPushed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Collects class names for the node with given id and all of it's child nodes.
     */
    public suspend fun collectClassNamesFromSubtree(
        args: CollectClassNamesFromSubtreeParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): CollectClassNamesFromSubtreeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.collectClassNamesFromSubtree", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Collects class names for the node with given id and all of it's child nodes.
     *
     * @param nodeId Id of the node to collect class names.
     */
    public suspend fun collectClassNamesFromSubtree(nodeId: Int): CollectClassNamesFromSubtreeReturn {
        val parameter = CollectClassNamesFromSubtreeParameter(nodeId = nodeId)
        return collectClassNamesFromSubtree(parameter)
    }

    /**
     * Creates a deep copy of the specified node and places it into the target container before the
     * given anchor.
     */
    public suspend fun copyTo(args: CopyToParameter, mode: CommandMode = CommandMode.DEFAULT): CopyToReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.copyTo", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Creates a deep copy of the specified node and places it into the target container before the
     * given anchor.
     *
     * @param nodeId Id of the node to copy.
     * @param targetNodeId Id of the element to drop the copy into.
     * @param insertBeforeNodeId Drop the copy before this node (if absent, the copy becomes the last child of
     * `targetNodeId`).
     */
    public suspend fun copyTo(
        nodeId: Int,
        targetNodeId: Int,
        insertBeforeNodeId: Int? = null,
    ): CopyToReturn {
        val parameter =
            CopyToParameter(nodeId = nodeId, targetNodeId = targetNodeId, insertBeforeNodeId = insertBeforeNodeId)
        return copyTo(parameter)
    }

    /**
     * Describes node given its id, does not require domain to be enabled. Does not start tracking any
     * objects, can be used for automation.
     */
    public suspend fun describeNode(
        args: DescribeNodeParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): DescribeNodeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.describeNode", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Describes node given its id, does not require domain to be enabled. Does not start tracking any
     * objects, can be used for automation.
     *
     * @param nodeId Identifier of the node.
     * @param backendNodeId Identifier of the backend node.
     * @param objectId JavaScript object id of the node wrapper.
     * @param depth The maximum depth at which children should be retrieved, defaults to 1. Use -1 for the
     * entire subtree or provide an integer larger than 0.
     * @param pierce Whether or not iframes and shadow roots should be traversed when returning the subtree
     * (default is false).
     */
    public suspend fun describeNode(
        nodeId: Int? = null,
        backendNodeId: Int? = null,
        objectId: String? = null,
        depth: Int? = null,
        pierce: Boolean? = null,
    ): DescribeNodeReturn {
        val parameter = DescribeNodeParameter(
            nodeId = nodeId,
            backendNodeId = backendNodeId,
            objectId = objectId,
            depth = depth,
            pierce = pierce
        )
        return describeNode(parameter)
    }

    /**
     * Scrolls the specified rect of the given node into view if not already visible.
     * Note: exactly one between nodeId, backendNodeId and objectId should be passed
     * to identify the node.
     */
    public suspend fun scrollIntoViewIfNeeded(
        args: ScrollIntoViewIfNeededParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.scrollIntoViewIfNeeded", parameter, mode)
    }

    /**
     * Scrolls the specified rect of the given node into view if not already visible.
     * Note: exactly one between nodeId, backendNodeId and objectId should be passed
     * to identify the node.
     *
     * @param nodeId Identifier of the node.
     * @param backendNodeId Identifier of the backend node.
     * @param objectId JavaScript object id of the node wrapper.
     * @param rect The rect to be scrolled into view, relative to the node's border box, in CSS pixels.
     * When omitted, center of the node will be used, similar to Element.scrollIntoView.
     */
    public suspend fun scrollIntoViewIfNeeded(
        nodeId: Int? = null,
        backendNodeId: Int? = null,
        objectId: String? = null,
        rect: Rect? = null,
    ) {
        val parameter = ScrollIntoViewIfNeededParameter(
            nodeId = nodeId,
            backendNodeId = backendNodeId,
            objectId = objectId,
            rect = rect
        )
        scrollIntoViewIfNeeded(parameter)
    }

    /**
     * Disables DOM agent for the given page.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("DOM.disable", parameter, mode)
    }

    /**
     * Discards search results from the session with the given id. `getSearchResults` should no longer
     * be called for that search.
     */
    public suspend fun discardSearchResults(
        args: DiscardSearchResultsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.discardSearchResults", parameter, mode)
    }

    /**
     * Discards search results from the session with the given id. `getSearchResults` should no longer
     * be called for that search.
     *
     * @param searchId Unique search session identifier.
     */
    public suspend fun discardSearchResults(searchId: String) {
        val parameter = DiscardSearchResultsParameter(searchId = searchId)
        discardSearchResults(parameter)
    }

    /**
     * Enables DOM agent for the given page.
     */
    public suspend fun enable(args: EnableParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.enable", parameter, mode)
    }

    /**
     * Enables DOM agent for the given page.
     *
     * @param includeWhitespace Whether to include whitespaces in the children array of returned Nodes.
     */
    public suspend fun enable(includeWhitespace: String? = null) {
        val parameter = EnableParameter(includeWhitespace = includeWhitespace)
        enable(parameter)
    }

    /**
     * Focuses the given element.
     */
    public suspend fun focus(args: FocusParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.focus", parameter, mode)
    }

    /**
     * Focuses the given element.
     *
     * @param nodeId Identifier of the node.
     * @param backendNodeId Identifier of the backend node.
     * @param objectId JavaScript object id of the node wrapper.
     */
    public suspend fun focus(
        nodeId: Int? = null,
        backendNodeId: Int? = null,
        objectId: String? = null,
    ) {
        val parameter = FocusParameter(nodeId = nodeId, backendNodeId = backendNodeId, objectId = objectId)
        focus(parameter)
    }

    /**
     * Returns attributes for the specified node.
     */
    public suspend fun getAttributes(
        args: GetAttributesParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetAttributesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getAttributes", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns attributes for the specified node.
     *
     * @param nodeId Id of the node to retrieve attributes for.
     */
    public suspend fun getAttributes(nodeId: Int): GetAttributesReturn {
        val parameter = GetAttributesParameter(nodeId = nodeId)
        return getAttributes(parameter)
    }

    /**
     * Returns boxes for the given node.
     */
    public suspend fun getBoxModel(
        args: GetBoxModelParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetBoxModelReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getBoxModel", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns boxes for the given node.
     *
     * @param nodeId Identifier of the node.
     * @param backendNodeId Identifier of the backend node.
     * @param objectId JavaScript object id of the node wrapper.
     */
    public suspend fun getBoxModel(
        nodeId: Int? = null,
        backendNodeId: Int? = null,
        objectId: String? = null,
    ): GetBoxModelReturn {
        val parameter = GetBoxModelParameter(nodeId = nodeId, backendNodeId = backendNodeId, objectId = objectId)
        return getBoxModel(parameter)
    }

    /**
     * Returns quads that describe node position on the page. This method
     * might return multiple quads for inline nodes.
     */
    public suspend fun getContentQuads(
        args: GetContentQuadsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetContentQuadsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getContentQuads", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns quads that describe node position on the page. This method
     * might return multiple quads for inline nodes.
     *
     * @param nodeId Identifier of the node.
     * @param backendNodeId Identifier of the backend node.
     * @param objectId JavaScript object id of the node wrapper.
     */
    public suspend fun getContentQuads(
        nodeId: Int? = null,
        backendNodeId: Int? = null,
        objectId: String? = null,
    ): GetContentQuadsReturn {
        val parameter = GetContentQuadsParameter(nodeId = nodeId, backendNodeId = backendNodeId, objectId = objectId)
        return getContentQuads(parameter)
    }

    /**
     * Returns the root DOM node (and optionally the subtree) to the caller.
     * Implicitly enables the DOM domain events for the current target.
     */
    public suspend fun getDocument(
        args: GetDocumentParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetDocumentReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getDocument", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the root DOM node (and optionally the subtree) to the caller.
     * Implicitly enables the DOM domain events for the current target.
     *
     * @param depth The maximum depth at which children should be retrieved, defaults to 1. Use -1 for the
     * entire subtree or provide an integer larger than 0.
     * @param pierce Whether or not iframes and shadow roots should be traversed when returning the subtree
     * (default is false).
     */
    public suspend fun getDocument(depth: Int? = null, pierce: Boolean? = null): GetDocumentReturn {
        val parameter = GetDocumentParameter(depth = depth, pierce = pierce)
        return getDocument(parameter)
    }

    /**
     * Returns the root DOM node (and optionally the subtree) to the caller.
     * Deprecated, as it is not designed to work well with the rest of the DOM agent.
     * Use DOMSnapshot.captureSnapshot instead.
     */
    @Deprecated(message = "")
    public suspend fun getFlattenedDocument(
        args: GetFlattenedDocumentParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetFlattenedDocumentReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getFlattenedDocument", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the root DOM node (and optionally the subtree) to the caller.
     * Deprecated, as it is not designed to work well with the rest of the DOM agent.
     * Use DOMSnapshot.captureSnapshot instead.
     *
     * @param depth The maximum depth at which children should be retrieved, defaults to 1. Use -1 for the
     * entire subtree or provide an integer larger than 0.
     * @param pierce Whether or not iframes and shadow roots should be traversed when returning the subtree
     * (default is false).
     */
    @Deprecated(message = "")
    public suspend fun getFlattenedDocument(depth: Int? = null, pierce: Boolean? = null): GetFlattenedDocumentReturn {
        val parameter = GetFlattenedDocumentParameter(depth = depth, pierce = pierce)
        return getFlattenedDocument(parameter)
    }

    /**
     * Finds nodes with a given computed style in a subtree.
     */
    public suspend fun getNodesForSubtreeByStyle(
        args: GetNodesForSubtreeByStyleParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetNodesForSubtreeByStyleReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getNodesForSubtreeByStyle", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Finds nodes with a given computed style in a subtree.
     *
     * @param nodeId Node ID pointing to the root of a subtree.
     * @param computedStyles The style to filter nodes by (includes nodes if any of properties matches).
     * @param pierce Whether or not iframes and shadow roots in the same target should be traversed when returning the
     * results (default is false).
     */
    public suspend fun getNodesForSubtreeByStyle(
        nodeId: Int,
        computedStyles: List<CSSComputedStyleProperty>,
        pierce: Boolean? = null,
    ): GetNodesForSubtreeByStyleReturn {
        val parameter =
            GetNodesForSubtreeByStyleParameter(nodeId = nodeId, computedStyles = computedStyles, pierce = pierce)
        return getNodesForSubtreeByStyle(parameter)
    }

    /**
     * Returns node id at given location. Depending on whether DOM domain is enabled, nodeId is
     * either returned or not.
     */
    public suspend fun getNodeForLocation(
        args: GetNodeForLocationParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetNodeForLocationReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getNodeForLocation", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns node id at given location. Depending on whether DOM domain is enabled, nodeId is
     * either returned or not.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param includeUserAgentShadowDOM False to skip to the nearest non-UA shadow root ancestor (default: false).
     * @param ignorePointerEventsNone Whether to ignore pointer-events: none on elements and hit test them.
     */
    public suspend fun getNodeForLocation(
        x: Int,
        y: Int,
        includeUserAgentShadowDOM: Boolean? = null,
        ignorePointerEventsNone: Boolean? = null,
    ): GetNodeForLocationReturn {
        val parameter = GetNodeForLocationParameter(
            x = x,
            y = y,
            includeUserAgentShadowDOM = includeUserAgentShadowDOM,
            ignorePointerEventsNone = ignorePointerEventsNone
        )
        return getNodeForLocation(parameter)
    }

    /**
     * Returns node's HTML markup.
     */
    public suspend fun getOuterHTML(
        args: GetOuterHTMLParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetOuterHTMLReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getOuterHTML", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns node's HTML markup.
     *
     * @param nodeId Identifier of the node.
     * @param backendNodeId Identifier of the backend node.
     * @param objectId JavaScript object id of the node wrapper.
     */
    public suspend fun getOuterHTML(
        nodeId: Int? = null,
        backendNodeId: Int? = null,
        objectId: String? = null,
    ): GetOuterHTMLReturn {
        val parameter = GetOuterHTMLParameter(nodeId = nodeId, backendNodeId = backendNodeId, objectId = objectId)
        return getOuterHTML(parameter)
    }

    /**
     * Returns the id of the nearest ancestor that is a relayout boundary.
     */
    public suspend fun getRelayoutBoundary(
        args: GetRelayoutBoundaryParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetRelayoutBoundaryReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getRelayoutBoundary", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the id of the nearest ancestor that is a relayout boundary.
     *
     * @param nodeId Id of the node.
     */
    public suspend fun getRelayoutBoundary(nodeId: Int): GetRelayoutBoundaryReturn {
        val parameter = GetRelayoutBoundaryParameter(nodeId = nodeId)
        return getRelayoutBoundary(parameter)
    }

    /**
     * Returns search results from given `fromIndex` to given `toIndex` from the search with the given
     * identifier.
     */
    public suspend fun getSearchResults(
        args: GetSearchResultsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetSearchResultsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getSearchResults", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns search results from given `fromIndex` to given `toIndex` from the search with the given
     * identifier.
     *
     * @param searchId Unique search session identifier.
     * @param fromIndex Start index of the search result to be returned.
     * @param toIndex End index of the search result to be returned.
     */
    public suspend fun getSearchResults(
        searchId: String,
        fromIndex: Int,
        toIndex: Int,
    ): GetSearchResultsReturn {
        val parameter = GetSearchResultsParameter(searchId = searchId, fromIndex = fromIndex, toIndex = toIndex)
        return getSearchResults(parameter)
    }

    /**
     * Hides any highlight.
     */
    public suspend fun hideHighlight(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("DOM.hideHighlight", parameter, mode)
    }

    /**
     * Highlights DOM node.
     */
    public suspend fun highlightNode(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("DOM.highlightNode", parameter, mode)
    }

    /**
     * Highlights given rectangle.
     */
    public suspend fun highlightRect(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("DOM.highlightRect", parameter, mode)
    }

    /**
     * Marks last undoable state.
     */
    public suspend fun markUndoableState(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("DOM.markUndoableState", parameter, mode)
    }

    /**
     * Moves node into the new container, places it before the given anchor.
     */
    public suspend fun moveTo(args: MoveToParameter, mode: CommandMode = CommandMode.DEFAULT): MoveToReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.moveTo", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Moves node into the new container, places it before the given anchor.
     *
     * @param nodeId Id of the node to move.
     * @param targetNodeId Id of the element to drop the moved node into.
     * @param insertBeforeNodeId Drop node before this one (if absent, the moved node becomes the last child of
     * `targetNodeId`).
     */
    public suspend fun moveTo(
        nodeId: Int,
        targetNodeId: Int,
        insertBeforeNodeId: Int? = null,
    ): MoveToReturn {
        val parameter =
            MoveToParameter(nodeId = nodeId, targetNodeId = targetNodeId, insertBeforeNodeId = insertBeforeNodeId)
        return moveTo(parameter)
    }

    /**
     * Searches for a given string in the DOM tree. Use `getSearchResults` to access search results or
     * `cancelSearch` to end this search session.
     */
    public suspend fun performSearch(
        args: PerformSearchParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): PerformSearchReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.performSearch", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Searches for a given string in the DOM tree. Use `getSearchResults` to access search results or
     * `cancelSearch` to end this search session.
     *
     * @param query Plain text or query selector or XPath search query.
     * @param includeUserAgentShadowDOM True to search in user agent shadow DOM.
     */
    public suspend fun performSearch(query: String, includeUserAgentShadowDOM: Boolean? = null): PerformSearchReturn {
        val parameter = PerformSearchParameter(query = query, includeUserAgentShadowDOM = includeUserAgentShadowDOM)
        return performSearch(parameter)
    }

    /**
     * Requests that the node is sent to the caller given its path. // FIXME, use XPath
     */
    public suspend fun pushNodeByPathToFrontend(
        args: PushNodeByPathToFrontendParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): PushNodeByPathToFrontendReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.pushNodeByPathToFrontend", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Requests that the node is sent to the caller given its path. // FIXME, use XPath
     *
     * @param path Path to node in the proprietary format.
     */
    public suspend fun pushNodeByPathToFrontend(path: String): PushNodeByPathToFrontendReturn {
        val parameter = PushNodeByPathToFrontendParameter(path = path)
        return pushNodeByPathToFrontend(parameter)
    }

    /**
     * Requests that a batch of nodes is sent to the caller given their backend node ids.
     */
    public suspend fun pushNodesByBackendIdsToFrontend(
        args: PushNodesByBackendIdsToFrontendParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): PushNodesByBackendIdsToFrontendReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.pushNodesByBackendIdsToFrontend", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Requests that a batch of nodes is sent to the caller given their backend node ids.
     *
     * @param backendNodeIds The array of backend node ids.
     */
    public suspend fun pushNodesByBackendIdsToFrontend(backendNodeIds: List<Int>): PushNodesByBackendIdsToFrontendReturn {
        val parameter = PushNodesByBackendIdsToFrontendParameter(backendNodeIds = backendNodeIds)
        return pushNodesByBackendIdsToFrontend(parameter)
    }

    /**
     * Executes `querySelector` on a given node.
     */
    public suspend fun querySelector(
        args: QuerySelectorParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): QuerySelectorReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.querySelector", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Executes `querySelector` on a given node.
     *
     * @param nodeId Id of the node to query upon.
     * @param selector Selector string.
     */
    public suspend fun querySelector(nodeId: Int, selector: String): QuerySelectorReturn {
        val parameter = QuerySelectorParameter(nodeId = nodeId, selector = selector)
        return querySelector(parameter)
    }

    /**
     * Executes `querySelectorAll` on a given node.
     */
    public suspend fun querySelectorAll(
        args: QuerySelectorAllParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): QuerySelectorAllReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.querySelectorAll", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Executes `querySelectorAll` on a given node.
     *
     * @param nodeId Id of the node to query upon.
     * @param selector Selector string.
     */
    public suspend fun querySelectorAll(nodeId: Int, selector: String): QuerySelectorAllReturn {
        val parameter = QuerySelectorAllParameter(nodeId = nodeId, selector = selector)
        return querySelectorAll(parameter)
    }

    /**
     * Returns NodeIds of current top layer elements.
     * Top layer is rendered closest to the user within a viewport, therefore its elements always
     * appear on top of all other content.
     */
    public suspend fun getTopLayerElements(mode: CommandMode = CommandMode.DEFAULT): GetTopLayerElementsReturn {
        val parameter = null
        val result = cdp.callCommand("DOM.getTopLayerElements", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the NodeId of the matched element according to certain relations.
     */
    public suspend fun getElementByRelation(
        args: GetElementByRelationParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetElementByRelationReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getElementByRelation", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the NodeId of the matched element according to certain relations.
     *
     * @param nodeId Id of the node from which to query the relation.
     * @param relation Type of relation to get.
     */
    public suspend fun getElementByRelation(nodeId: Int, relation: String): GetElementByRelationReturn {
        val parameter = GetElementByRelationParameter(nodeId = nodeId, relation = relation)
        return getElementByRelation(parameter)
    }

    /**
     * Re-does the last undone action.
     */
    public suspend fun redo(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("DOM.redo", parameter, mode)
    }

    /**
     * Removes attribute with given name from an element with given id.
     */
    public suspend fun removeAttribute(args: RemoveAttributeParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.removeAttribute", parameter, mode)
    }

    /**
     * Removes attribute with given name from an element with given id.
     *
     * @param nodeId Id of the element to remove attribute from.
     * @param name Name of the attribute to remove.
     */
    public suspend fun removeAttribute(nodeId: Int, name: String) {
        val parameter = RemoveAttributeParameter(nodeId = nodeId, name = name)
        removeAttribute(parameter)
    }

    /**
     * Removes node with given id.
     */
    public suspend fun removeNode(args: RemoveNodeParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.removeNode", parameter, mode)
    }

    /**
     * Removes node with given id.
     *
     * @param nodeId Id of the node to remove.
     */
    public suspend fun removeNode(nodeId: Int) {
        val parameter = RemoveNodeParameter(nodeId = nodeId)
        removeNode(parameter)
    }

    /**
     * Requests that children of the node with given id are returned to the caller in form of
     * `setChildNodes` events where not only immediate children are retrieved, but all children down to
     * the specified depth.
     */
    public suspend fun requestChildNodes(args: RequestChildNodesParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.requestChildNodes", parameter, mode)
    }

    /**
     * Requests that children of the node with given id are returned to the caller in form of
     * `setChildNodes` events where not only immediate children are retrieved, but all children down to
     * the specified depth.
     *
     * @param nodeId Id of the node to get children for.
     * @param depth The maximum depth at which children should be retrieved, defaults to 1. Use -1 for the
     * entire subtree or provide an integer larger than 0.
     * @param pierce Whether or not iframes and shadow roots should be traversed when returning the sub-tree
     * (default is false).
     */
    public suspend fun requestChildNodes(
        nodeId: Int,
        depth: Int? = null,
        pierce: Boolean? = null,
    ) {
        val parameter = RequestChildNodesParameter(nodeId = nodeId, depth = depth, pierce = pierce)
        requestChildNodes(parameter)
    }

    /**
     * Requests that the node is sent to the caller given the JavaScript node object reference. All
     * nodes that form the path from the node to the root are also sent to the client as a series of
     * `setChildNodes` notifications.
     */
    public suspend fun requestNode(
        args: RequestNodeParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): RequestNodeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.requestNode", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Requests that the node is sent to the caller given the JavaScript node object reference. All
     * nodes that form the path from the node to the root are also sent to the client as a series of
     * `setChildNodes` notifications.
     *
     * @param objectId JavaScript object id to convert into node.
     */
    public suspend fun requestNode(objectId: String): RequestNodeReturn {
        val parameter = RequestNodeParameter(objectId = objectId)
        return requestNode(parameter)
    }

    /**
     * Resolves the JavaScript node object for a given NodeId or BackendNodeId.
     */
    public suspend fun resolveNode(
        args: ResolveNodeParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): ResolveNodeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.resolveNode", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Resolves the JavaScript node object for a given NodeId or BackendNodeId.
     *
     * @param nodeId Id of the node to resolve.
     * @param backendNodeId Backend identifier of the node to resolve.
     * @param objectGroup Symbolic group name that can be used to release multiple objects.
     * @param executionContextId Execution context in which to resolve the node.
     */
    public suspend fun resolveNode(
        nodeId: Int? = null,
        backendNodeId: Int? = null,
        objectGroup: String? = null,
        executionContextId: Int? = null,
    ): ResolveNodeReturn {
        val parameter = ResolveNodeParameter(
            nodeId = nodeId,
            backendNodeId = backendNodeId,
            objectGroup = objectGroup,
            executionContextId = executionContextId
        )
        return resolveNode(parameter)
    }

    /**
     * Sets attribute for an element with given id.
     */
    public suspend fun setAttributeValue(args: SetAttributeValueParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.setAttributeValue", parameter, mode)
    }

    /**
     * Sets attribute for an element with given id.
     *
     * @param nodeId Id of the element to set attribute for.
     * @param name Attribute name.
     * @param value Attribute value.
     */
    public suspend fun setAttributeValue(
        nodeId: Int,
        name: String,
        `value`: String,
    ) {
        val parameter = SetAttributeValueParameter(nodeId = nodeId, name = name, value = value)
        setAttributeValue(parameter)
    }

    /**
     * Sets attributes on element with given id. This method is useful when user edits some existing
     * attribute value and types in several attribute name/value pairs.
     */
    public suspend fun setAttributesAsText(
        args: SetAttributesAsTextParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.setAttributesAsText", parameter, mode)
    }

    /**
     * Sets attributes on element with given id. This method is useful when user edits some existing
     * attribute value and types in several attribute name/value pairs.
     *
     * @param nodeId Id of the element to set attributes for.
     * @param text Text with a number of attributes. Will parse this text using HTML parser.
     * @param name Attribute name to replace with new attributes derived from text in case text parsed
     * successfully.
     */
    public suspend fun setAttributesAsText(
        nodeId: Int,
        text: String,
        name: String? = null,
    ) {
        val parameter = SetAttributesAsTextParameter(nodeId = nodeId, text = text, name = name)
        setAttributesAsText(parameter)
    }

    /**
     * Sets files for the given file input element.
     */
    public suspend fun setFileInputFiles(args: SetFileInputFilesParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.setFileInputFiles", parameter, mode)
    }

    /**
     * Sets files for the given file input element.
     *
     * @param files Array of file paths to set.
     * @param nodeId Identifier of the node.
     * @param backendNodeId Identifier of the backend node.
     * @param objectId JavaScript object id of the node wrapper.
     */
    public suspend fun setFileInputFiles(
        files: List<String>,
        nodeId: Int? = null,
        backendNodeId: Int? = null,
        objectId: String? = null,
    ) {
        val parameter = SetFileInputFilesParameter(
            files = files,
            nodeId = nodeId,
            backendNodeId = backendNodeId,
            objectId = objectId
        )
        setFileInputFiles(parameter)
    }

    /**
     * Sets if stack traces should be captured for Nodes. See `Node.getNodeStackTraces`. Default is disabled.
     */
    public suspend fun setNodeStackTracesEnabled(
        args: SetNodeStackTracesEnabledParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.setNodeStackTracesEnabled", parameter, mode)
    }

    /**
     * Sets if stack traces should be captured for Nodes. See `Node.getNodeStackTraces`. Default is disabled.
     *
     * @param enable Enable or disable.
     */
    public suspend fun setNodeStackTracesEnabled(enable: Boolean) {
        val parameter = SetNodeStackTracesEnabledParameter(enable = enable)
        setNodeStackTracesEnabled(parameter)
    }

    /**
     * Gets stack traces associated with a Node. As of now, only provides stack trace for Node creation.
     */
    public suspend fun getNodeStackTraces(
        args: GetNodeStackTracesParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetNodeStackTracesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getNodeStackTraces", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets stack traces associated with a Node. As of now, only provides stack trace for Node creation.
     *
     * @param nodeId Id of the node to get stack traces for.
     */
    public suspend fun getNodeStackTraces(nodeId: Int): GetNodeStackTracesReturn {
        val parameter = GetNodeStackTracesParameter(nodeId = nodeId)
        return getNodeStackTraces(parameter)
    }

    /**
     * Returns file information for the given
     * File wrapper.
     */
    public suspend fun getFileInfo(
        args: GetFileInfoParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetFileInfoReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getFileInfo", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns file information for the given
     * File wrapper.
     *
     * @param objectId JavaScript object id of the node wrapper.
     */
    public suspend fun getFileInfo(objectId: String): GetFileInfoReturn {
        val parameter = GetFileInfoParameter(objectId = objectId)
        return getFileInfo(parameter)
    }

    /**
     * Returns list of detached nodes
     */
    public suspend fun getDetachedDomNodes(mode: CommandMode = CommandMode.DEFAULT): GetDetachedDomNodesReturn {
        val parameter = null
        val result = cdp.callCommand("DOM.getDetachedDomNodes", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Enables console to refer to the node with given id via $x (see Command Line API for more details
     * $x functions).
     */
    public suspend fun setInspectedNode(args: SetInspectedNodeParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.setInspectedNode", parameter, mode)
    }

    /**
     * Enables console to refer to the node with given id via $x (see Command Line API for more details
     * $x functions).
     *
     * @param nodeId DOM node id to be accessible by means of $x command line API.
     */
    public suspend fun setInspectedNode(nodeId: Int) {
        val parameter = SetInspectedNodeParameter(nodeId = nodeId)
        setInspectedNode(parameter)
    }

    /**
     * Sets node name for a node with given id.
     */
    public suspend fun setNodeName(
        args: SetNodeNameParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): SetNodeNameReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.setNodeName", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Sets node name for a node with given id.
     *
     * @param nodeId Id of the node to set name for.
     * @param name New node's name.
     */
    public suspend fun setNodeName(nodeId: Int, name: String): SetNodeNameReturn {
        val parameter = SetNodeNameParameter(nodeId = nodeId, name = name)
        return setNodeName(parameter)
    }

    /**
     * Sets node value for a node with given id.
     */
    public suspend fun setNodeValue(args: SetNodeValueParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.setNodeValue", parameter, mode)
    }

    /**
     * Sets node value for a node with given id.
     *
     * @param nodeId Id of the node to set value for.
     * @param value New node's value.
     */
    public suspend fun setNodeValue(nodeId: Int, `value`: String) {
        val parameter = SetNodeValueParameter(nodeId = nodeId, value = value)
        setNodeValue(parameter)
    }

    /**
     * Sets node HTML markup, returns new node id.
     */
    public suspend fun setOuterHTML(args: SetOuterHTMLParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOM.setOuterHTML", parameter, mode)
    }

    /**
     * Sets node HTML markup, returns new node id.
     *
     * @param nodeId Id of the node to set markup for.
     * @param outerHTML Outer HTML markup to set.
     */
    public suspend fun setOuterHTML(nodeId: Int, outerHTML: String) {
        val parameter = SetOuterHTMLParameter(nodeId = nodeId, outerHTML = outerHTML)
        setOuterHTML(parameter)
    }

    /**
     * Undoes the last performed action.
     */
    public suspend fun undo(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("DOM.undo", parameter, mode)
    }

    /**
     * Returns iframe node that owns iframe with the given domain.
     */
    public suspend fun getFrameOwner(
        args: GetFrameOwnerParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetFrameOwnerReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getFrameOwner", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns iframe node that owns iframe with the given domain.
     *
     * @param frameId No description
     */
    public suspend fun getFrameOwner(frameId: String): GetFrameOwnerReturn {
        val parameter = GetFrameOwnerParameter(frameId = frameId)
        return getFrameOwner(parameter)
    }

    /**
     * Returns the query container of the given node based on container query
     * conditions: containerName, physical and logical axes, and whether it queries
     * scroll-state. If no axes are provided and queriesScrollState is false, the
     * style container is returned, which is the direct parent or the closest
     * element with a matching container-name.
     */
    public suspend fun getContainerForNode(
        args: GetContainerForNodeParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetContainerForNodeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getContainerForNode", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the query container of the given node based on container query
     * conditions: containerName, physical and logical axes, and whether it queries
     * scroll-state. If no axes are provided and queriesScrollState is false, the
     * style container is returned, which is the direct parent or the closest
     * element with a matching container-name.
     *
     * @param nodeId No description
     * @param containerName No description
     * @param physicalAxes No description
     * @param logicalAxes No description
     * @param queriesScrollState No description
     */
    public suspend fun getContainerForNode(
        nodeId: Int,
        containerName: String? = null,
        physicalAxes: PhysicalAxes? = null,
        logicalAxes: LogicalAxes? = null,
        queriesScrollState: Boolean? = null,
    ): GetContainerForNodeReturn {
        val parameter = GetContainerForNodeParameter(
            nodeId = nodeId,
            containerName = containerName,
            physicalAxes = physicalAxes,
            logicalAxes = logicalAxes,
            queriesScrollState = queriesScrollState
        )
        return getContainerForNode(parameter)
    }

    /**
     * Returns the descendants of a container query container that have
     * container queries against this container.
     */
    public suspend fun getQueryingDescendantsForContainer(
        args: GetQueryingDescendantsForContainerParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetQueryingDescendantsForContainerReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getQueryingDescendantsForContainer", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the descendants of a container query container that have
     * container queries against this container.
     *
     * @param nodeId Id of the container node to find querying descendants from.
     */
    public suspend fun getQueryingDescendantsForContainer(nodeId: Int): GetQueryingDescendantsForContainerReturn {
        val parameter = GetQueryingDescendantsForContainerParameter(nodeId = nodeId)
        return getQueryingDescendantsForContainer(parameter)
    }

    /**
     * Returns the target anchor element of the given anchor query according to
     * https://www.w3.org/TR/css-anchor-position-1/#target.
     */
    public suspend fun getAnchorElement(
        args: GetAnchorElementParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetAnchorElementReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOM.getAnchorElement", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the target anchor element of the given anchor query according to
     * https://www.w3.org/TR/css-anchor-position-1/#target.
     *
     * @param nodeId Id of the positioned element from which to find the anchor.
     * @param anchorSpecifier An optional anchor specifier, as defined in
     * https://www.w3.org/TR/css-anchor-position-1/#anchor-specifier.
     * If not provided, it will return the implicit anchor element for
     * the given positioned element.
     */
    public suspend fun getAnchorElement(nodeId: Int, anchorSpecifier: String? = null): GetAnchorElementReturn {
        val parameter = GetAnchorElementParameter(nodeId = nodeId, anchorSpecifier = anchorSpecifier)
        return getAnchorElement(parameter)
    }

    /**
     * Backend node with a friendly name.
     */
    @Serializable
    public data class BackendNode(
        /**
         * `Node`'s nodeType.
         */
        public val nodeType: Int,
        /**
         * `Node`'s nodeName.
         */
        public val nodeName: String,
        public val backendNodeId: Int,
    )

    /**
     * Pseudo element type.
     */
    @Serializable
    public enum class PseudoType {
        @SerialName("first-line")
        FIRST_LINE,

        @SerialName("first-letter")
        FIRST_LETTER,

        @SerialName("checkmark")
        CHECKMARK,

        @SerialName("before")
        BEFORE,

        @SerialName("after")
        AFTER,

        @SerialName("picker-icon")
        PICKER_ICON,

        @SerialName("marker")
        MARKER,

        @SerialName("backdrop")
        BACKDROP,

        @SerialName("column")
        COLUMN,

        @SerialName("selection")
        SELECTION,

        @SerialName("search-text")
        SEARCH_TEXT,

        @SerialName("target-text")
        TARGET_TEXT,

        @SerialName("spelling-error")
        SPELLING_ERROR,

        @SerialName("grammar-error")
        GRAMMAR_ERROR,

        @SerialName("highlight")
        HIGHLIGHT,

        @SerialName("first-line-inherited")
        FIRST_LINE_INHERITED,

        @SerialName("scroll-marker")
        SCROLL_MARKER,

        @SerialName("scroll-marker-group")
        SCROLL_MARKER_GROUP,

        @SerialName("scroll-button")
        SCROLL_BUTTON,

        @SerialName("scrollbar")
        SCROLLBAR,

        @SerialName("scrollbar-thumb")
        SCROLLBAR_THUMB,

        @SerialName("scrollbar-button")
        SCROLLBAR_BUTTON,

        @SerialName("scrollbar-track")
        SCROLLBAR_TRACK,

        @SerialName("scrollbar-track-piece")
        SCROLLBAR_TRACK_PIECE,

        @SerialName("scrollbar-corner")
        SCROLLBAR_CORNER,

        @SerialName("resizer")
        RESIZER,

        @SerialName("input-list-button")
        INPUT_LIST_BUTTON,

        @SerialName("view-transition")
        VIEW_TRANSITION,

        @SerialName("view-transition-group")
        VIEW_TRANSITION_GROUP,

        @SerialName("view-transition-image-pair")
        VIEW_TRANSITION_IMAGE_PAIR,

        @SerialName("view-transition-group-children")
        VIEW_TRANSITION_GROUP_CHILDREN,

        @SerialName("view-transition-old")
        VIEW_TRANSITION_OLD,

        @SerialName("view-transition-new")
        VIEW_TRANSITION_NEW,

        @SerialName("placeholder")
        PLACEHOLDER,

        @SerialName("file-selector-button")
        FILE_SELECTOR_BUTTON,

        @SerialName("details-content")
        DETAILS_CONTENT,

        @SerialName("picker")
        PICKER,

        @SerialName("permission-icon")
        PERMISSION_ICON,
    }

    /**
     * Shadow root type.
     */
    @Serializable
    public enum class ShadowRootType {
        @SerialName("user-agent")
        USER_AGENT,

        @SerialName("open")
        OPEN,

        @SerialName("closed")
        CLOSED,
    }

    /**
     * Document compatibility mode.
     */
    @Serializable
    public enum class CompatibilityMode {
        @SerialName("QuirksMode")
        QUIRKSMODE,

        @SerialName("LimitedQuirksMode")
        LIMITEDQUIRKSMODE,

        @SerialName("NoQuirksMode")
        NOQUIRKSMODE,
    }

    /**
     * ContainerSelector physical axes
     */
    @Serializable
    public enum class PhysicalAxes {
        @SerialName("Horizontal")
        HORIZONTAL,

        @SerialName("Vertical")
        VERTICAL,

        @SerialName("Both")
        BOTH,
    }

    /**
     * ContainerSelector logical axes
     */
    @Serializable
    public enum class LogicalAxes {
        @SerialName("Inline")
        INLINE,

        @SerialName("Block")
        BLOCK,

        @SerialName("Both")
        BOTH,
    }

    /**
     * Physical scroll orientation
     */
    @Serializable
    public enum class ScrollOrientation {
        @SerialName("horizontal")
        HORIZONTAL,

        @SerialName("vertical")
        VERTICAL,
    }

    /**
     * DOM interaction is implemented in terms of mirror objects that represent the actual DOM nodes.
     * DOMNode is a base node mirror type.
     */
    @Serializable
    public data class Node(
        /**
         * Node identifier that is passed into the rest of the DOM messages as the `nodeId`. Backend
         * will only push node with given `id` once. It is aware of all requested nodes and will only
         * fire DOM events for nodes known to the client.
         */
        public val nodeId: Int,
        /**
         * The id of the parent node if any.
         */
        public val parentId: Int? = null,
        /**
         * The BackendNodeId for this node.
         */
        public val backendNodeId: Int,
        /**
         * `Node`'s nodeType.
         */
        public val nodeType: Int,
        /**
         * `Node`'s nodeName.
         */
        public val nodeName: String,
        /**
         * `Node`'s localName.
         */
        public val localName: String,
        /**
         * `Node`'s nodeValue.
         */
        public val nodeValue: String,
        /**
         * Child count for `Container` nodes.
         */
        public val childNodeCount: Int? = null,
        /**
         * Child nodes of this node when requested with children.
         */
        public val children: List<Node>? = null,
        /**
         * Attributes of the `Element` node in the form of flat array `[name1, value1, name2, value2]`.
         */
        public val attributes: List<String>? = null,
        /**
         * Document URL that `Document` or `FrameOwner` node points to.
         */
        public val documentURL: String? = null,
        /**
         * Base URL that `Document` or `FrameOwner` node uses for URL completion.
         */
        public val baseURL: String? = null,
        /**
         * `DocumentType`'s publicId.
         */
        public val publicId: String? = null,
        /**
         * `DocumentType`'s systemId.
         */
        public val systemId: String? = null,
        /**
         * `DocumentType`'s internalSubset.
         */
        public val internalSubset: String? = null,
        /**
         * `Document`'s XML version in case of XML documents.
         */
        public val xmlVersion: String? = null,
        /**
         * `Attr`'s name.
         */
        public val name: String? = null,
        /**
         * `Attr`'s value.
         */
        public val `value`: String? = null,
        /**
         * Pseudo element type for this node.
         */
        public val pseudoType: PseudoType? = null,
        /**
         * Pseudo element identifier for this node. Only present if there is a
         * valid pseudoType.
         */
        public val pseudoIdentifier: String? = null,
        /**
         * Shadow root type.
         */
        public val shadowRootType: ShadowRootType? = null,
        /**
         * Frame ID for frame owner elements.
         */
        public val frameId: String? = null,
        /**
         * Content document for frame owner elements.
         */
        public val contentDocument: Node? = null,
        /**
         * Shadow root list for given element host.
         */
        public val shadowRoots: List<Node>? = null,
        /**
         * Content document fragment for template elements.
         */
        public val templateContent: Node? = null,
        /**
         * Pseudo elements associated with this node.
         */
        public val pseudoElements: List<Node>? = null,
        /**
         * Deprecated, as the HTML Imports API has been removed (crbug.com/937746).
         * This property used to return the imported document for the HTMLImport links.
         * The property is always undefined now.
         */
        public val importedDocument: Node? = null,
        /**
         * Distributed nodes for given insertion point.
         */
        public val distributedNodes: List<BackendNode>? = null,
        /**
         * Whether the node is SVG.
         */
        public val isSVG: Boolean? = null,
        public val compatibilityMode: CompatibilityMode? = null,
        public val assignedSlot: BackendNode? = null,
        public val isScrollable: Boolean? = null,
    )

    /**
     * A structure to hold the top-level node of a detached tree and an array of its retained descendants.
     */
    @Serializable
    public data class DetachedElementInfo(
        public val treeNode: Node,
        public val retainedNodeIds: List<Int>,
    )

    /**
     * A structure holding an RGBA color.
     */
    @Serializable
    public data class RGBA(
        /**
         * The red component, in the [0-255] range.
         */
        public val r: Int,
        /**
         * The green component, in the [0-255] range.
         */
        public val g: Int,
        /**
         * The blue component, in the [0-255] range.
         */
        public val b: Int,
        /**
         * The alpha component, in the [0-1] range (default: 1).
         */
        public val a: Double? = null,
    )

    /**
     * Box model.
     */
    @Serializable
    public data class BoxModel(
        /**
         * Content box
         */
        public val content: List<Double>,
        /**
         * Padding box
         */
        public val padding: List<Double>,
        /**
         * Border box
         */
        public val border: List<Double>,
        /**
         * Margin box
         */
        public val margin: List<Double>,
        /**
         * Node width
         */
        public val width: Int,
        /**
         * Node height
         */
        public val height: Int,
        /**
         * Shape outside coordinates
         */
        public val shapeOutside: ShapeOutsideInfo? = null,
    )

    /**
     * CSS Shape Outside details.
     */
    @Serializable
    public data class ShapeOutsideInfo(
        /**
         * Shape bounds
         */
        public val bounds: List<Double>,
        /**
         * Shape coordinate details
         */
        public val shape: List<JsonElement>,
        /**
         * Margin shape bounds
         */
        public val marginShape: List<JsonElement>,
    )

    /**
     * Rectangle.
     */
    @Serializable
    public data class Rect(
        /**
         * X coordinate
         */
        public val x: Double,
        /**
         * Y coordinate
         */
        public val y: Double,
        /**
         * Rectangle width
         */
        public val width: Double,
        /**
         * Rectangle height
         */
        public val height: Double,
    )

    @Serializable
    public data class CSSComputedStyleProperty(
        /**
         * Computed style property name.
         */
        public val name: String,
        /**
         * Computed style property value.
         */
        public val `value`: String,
    )

    /**
     * Fired when `Element`'s attribute is modified.
     */
    @Serializable
    public data class AttributeModifiedParameter(
        /**
         * Id of the node that has changed.
         */
        public val nodeId: Int,
        /**
         * Attribute name.
         */
        public val name: String,
        /**
         * Attribute value.
         */
        public val `value`: String,
    )

    /**
     * Fired when `Element`'s attribute is removed.
     */
    @Serializable
    public data class AttributeRemovedParameter(
        /**
         * Id of the node that has changed.
         */
        public val nodeId: Int,
        /**
         * A ttribute name.
         */
        public val name: String,
    )

    /**
     * Mirrors `DOMCharacterDataModified` event.
     */
    @Serializable
    public data class CharacterDataModifiedParameter(
        /**
         * Id of the node that has changed.
         */
        public val nodeId: Int,
        /**
         * New text value.
         */
        public val characterData: String,
    )

    /**
     * Fired when `Container`'s child node count has changed.
     */
    @Serializable
    public data class ChildNodeCountUpdatedParameter(
        /**
         * Id of the node that has changed.
         */
        public val nodeId: Int,
        /**
         * New node count.
         */
        public val childNodeCount: Int,
    )

    /**
     * Mirrors `DOMNodeInserted` event.
     */
    @Serializable
    public data class ChildNodeInsertedParameter(
        /**
         * Id of the node that has changed.
         */
        public val parentNodeId: Int,
        /**
         * Id of the previous sibling.
         */
        public val previousNodeId: Int,
        /**
         * Inserted node data.
         */
        public val node: Node,
    )

    /**
     * Mirrors `DOMNodeRemoved` event.
     */
    @Serializable
    public data class ChildNodeRemovedParameter(
        /**
         * Parent id.
         */
        public val parentNodeId: Int,
        /**
         * Id of the node that has been removed.
         */
        public val nodeId: Int,
    )

    /**
     * Called when distribution is changed.
     */
    @Serializable
    public data class DistributedNodesUpdatedParameter(
        /**
         * Insertion point where distributed nodes were updated.
         */
        public val insertionPointId: Int,
        /**
         * Distributed nodes for given insertion point.
         */
        public val distributedNodes: List<BackendNode>,
    )

    /**
     * Fired when `Element`'s inline style is modified via a CSS property modification.
     */
    @Serializable
    public data class InlineStyleInvalidatedParameter(
        /**
         * Ids of the nodes for which the inline styles have been invalidated.
         */
        public val nodeIds: List<Int>,
    )

    /**
     * Called when a pseudo element is added to an element.
     */
    @Serializable
    public data class PseudoElementAddedParameter(
        /**
         * Pseudo element's parent element id.
         */
        public val parentId: Int,
        /**
         * The added pseudo element.
         */
        public val pseudoElement: Node,
    )

    /**
     * Fired when a node's scrollability state changes.
     */
    @Serializable
    public data class ScrollableFlagUpdatedParameter(
        /**
         * The id of the node.
         */
        public val nodeId: Int,
        /**
         * If the node is scrollable.
         */
        public val isScrollable: Boolean,
    )

    /**
     * Called when a pseudo element is removed from an element.
     */
    @Serializable
    public data class PseudoElementRemovedParameter(
        /**
         * Pseudo element's parent element id.
         */
        public val parentId: Int,
        /**
         * The removed pseudo element id.
         */
        public val pseudoElementId: Int,
    )

    /**
     * Fired when backend wants to provide client with the missing DOM structure. This happens upon
     * most of the calls requesting node ids.
     */
    @Serializable
    public data class SetChildNodesParameter(
        /**
         * Parent node id to populate with children.
         */
        public val parentId: Int,
        /**
         * Child nodes array.
         */
        public val nodes: List<Node>,
    )

    /**
     * Called when shadow root is popped from the element.
     */
    @Serializable
    public data class ShadowRootPoppedParameter(
        /**
         * Host element id.
         */
        public val hostId: Int,
        /**
         * Shadow root id.
         */
        public val rootId: Int,
    )

    /**
     * Called when shadow root is pushed into the element.
     */
    @Serializable
    public data class ShadowRootPushedParameter(
        /**
         * Host element id.
         */
        public val hostId: Int,
        /**
         * Shadow root.
         */
        public val root: Node,
    )

    @Serializable
    public data class CollectClassNamesFromSubtreeParameter(
        /**
         * Id of the node to collect class names.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class CollectClassNamesFromSubtreeReturn(
        /**
         * Class name list.
         */
        public val classNames: List<String>,
    )

    @Serializable
    public data class CopyToParameter(
        /**
         * Id of the node to copy.
         */
        public val nodeId: Int,
        /**
         * Id of the element to drop the copy into.
         */
        public val targetNodeId: Int,
        /**
         * Drop the copy before this node (if absent, the copy becomes the last child of
         * `targetNodeId`).
         */
        public val insertBeforeNodeId: Int? = null,
    )

    @Serializable
    public data class CopyToReturn(
        /**
         * Id of the node clone.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class DescribeNodeParameter(
        /**
         * Identifier of the node.
         */
        public val nodeId: Int? = null,
        /**
         * Identifier of the backend node.
         */
        public val backendNodeId: Int? = null,
        /**
         * JavaScript object id of the node wrapper.
         */
        public val objectId: String? = null,
        /**
         * The maximum depth at which children should be retrieved, defaults to 1. Use -1 for the
         * entire subtree or provide an integer larger than 0.
         */
        public val depth: Int? = null,
        /**
         * Whether or not iframes and shadow roots should be traversed when returning the subtree
         * (default is false).
         */
        public val pierce: Boolean? = null,
    )

    @Serializable
    public data class DescribeNodeReturn(
        /**
         * Node description.
         */
        public val node: Node,
    )

    @Serializable
    public data class ScrollIntoViewIfNeededParameter(
        /**
         * Identifier of the node.
         */
        public val nodeId: Int? = null,
        /**
         * Identifier of the backend node.
         */
        public val backendNodeId: Int? = null,
        /**
         * JavaScript object id of the node wrapper.
         */
        public val objectId: String? = null,
        /**
         * The rect to be scrolled into view, relative to the node's border box, in CSS pixels.
         * When omitted, center of the node will be used, similar to Element.scrollIntoView.
         */
        public val rect: Rect? = null,
    )

    @Serializable
    public data class DiscardSearchResultsParameter(
        /**
         * Unique search session identifier.
         */
        public val searchId: String,
    )

    @Serializable
    public data class EnableParameter(
        /**
         * Whether to include whitespaces in the children array of returned Nodes.
         */
        public val includeWhitespace: String? = null,
    )

    @Serializable
    public data class FocusParameter(
        /**
         * Identifier of the node.
         */
        public val nodeId: Int? = null,
        /**
         * Identifier of the backend node.
         */
        public val backendNodeId: Int? = null,
        /**
         * JavaScript object id of the node wrapper.
         */
        public val objectId: String? = null,
    )

    @Serializable
    public data class GetAttributesParameter(
        /**
         * Id of the node to retrieve attributes for.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class GetAttributesReturn(
        /**
         * An interleaved array of node attribute names and values.
         */
        public val attributes: List<String>,
    )

    @Serializable
    public data class GetBoxModelParameter(
        /**
         * Identifier of the node.
         */
        public val nodeId: Int? = null,
        /**
         * Identifier of the backend node.
         */
        public val backendNodeId: Int? = null,
        /**
         * JavaScript object id of the node wrapper.
         */
        public val objectId: String? = null,
    )

    @Serializable
    public data class GetBoxModelReturn(
        /**
         * Box model for the node.
         */
        public val model: BoxModel,
    )

    @Serializable
    public data class GetContentQuadsParameter(
        /**
         * Identifier of the node.
         */
        public val nodeId: Int? = null,
        /**
         * Identifier of the backend node.
         */
        public val backendNodeId: Int? = null,
        /**
         * JavaScript object id of the node wrapper.
         */
        public val objectId: String? = null,
    )

    @Serializable
    public data class GetContentQuadsReturn(
        /**
         * Quads that describe node layout relative to viewport.
         */
        public val quads: List<List<Double>>,
    )

    @Serializable
    public data class GetDocumentParameter(
        /**
         * The maximum depth at which children should be retrieved, defaults to 1. Use -1 for the
         * entire subtree or provide an integer larger than 0.
         */
        public val depth: Int? = null,
        /**
         * Whether or not iframes and shadow roots should be traversed when returning the subtree
         * (default is false).
         */
        public val pierce: Boolean? = null,
    )

    @Serializable
    public data class GetDocumentReturn(
        /**
         * Resulting node.
         */
        public val root: Node,
    )

    @Serializable
    public data class GetFlattenedDocumentParameter(
        /**
         * The maximum depth at which children should be retrieved, defaults to 1. Use -1 for the
         * entire subtree or provide an integer larger than 0.
         */
        public val depth: Int? = null,
        /**
         * Whether or not iframes and shadow roots should be traversed when returning the subtree
         * (default is false).
         */
        public val pierce: Boolean? = null,
    )

    @Serializable
    public data class GetFlattenedDocumentReturn(
        /**
         * Resulting node.
         */
        public val nodes: List<Node>,
    )

    @Serializable
    public data class GetNodesForSubtreeByStyleParameter(
        /**
         * Node ID pointing to the root of a subtree.
         */
        public val nodeId: Int,
        /**
         * The style to filter nodes by (includes nodes if any of properties matches).
         */
        public val computedStyles: List<CSSComputedStyleProperty>,
        /**
         * Whether or not iframes and shadow roots in the same target should be traversed when returning the
         * results (default is false).
         */
        public val pierce: Boolean? = null,
    )

    @Serializable
    public data class GetNodesForSubtreeByStyleReturn(
        /**
         * Resulting nodes.
         */
        public val nodeIds: List<Int>,
    )

    @Serializable
    public data class GetNodeForLocationParameter(
        /**
         * X coordinate.
         */
        public val x: Int,
        /**
         * Y coordinate.
         */
        public val y: Int,
        /**
         * False to skip to the nearest non-UA shadow root ancestor (default: false).
         */
        public val includeUserAgentShadowDOM: Boolean? = null,
        /**
         * Whether to ignore pointer-events: none on elements and hit test them.
         */
        public val ignorePointerEventsNone: Boolean? = null,
    )

    @Serializable
    public data class GetNodeForLocationReturn(
        /**
         * Resulting node.
         */
        public val backendNodeId: Int,
        /**
         * Frame this node belongs to.
         */
        public val frameId: String,
        /**
         * Id of the node at given coordinates, only when enabled and requested document.
         */
        public val nodeId: Int?,
    )

    @Serializable
    public data class GetOuterHTMLParameter(
        /**
         * Identifier of the node.
         */
        public val nodeId: Int? = null,
        /**
         * Identifier of the backend node.
         */
        public val backendNodeId: Int? = null,
        /**
         * JavaScript object id of the node wrapper.
         */
        public val objectId: String? = null,
    )

    @Serializable
    public data class GetOuterHTMLReturn(
        /**
         * Outer HTML markup.
         */
        public val outerHTML: String,
    )

    @Serializable
    public data class GetRelayoutBoundaryParameter(
        /**
         * Id of the node.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class GetRelayoutBoundaryReturn(
        /**
         * Relayout boundary node id for the given node.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class GetSearchResultsParameter(
        /**
         * Unique search session identifier.
         */
        public val searchId: String,
        /**
         * Start index of the search result to be returned.
         */
        public val fromIndex: Int,
        /**
         * End index of the search result to be returned.
         */
        public val toIndex: Int,
    )

    @Serializable
    public data class GetSearchResultsReturn(
        /**
         * Ids of the search result nodes.
         */
        public val nodeIds: List<Int>,
    )

    @Serializable
    public data class MoveToParameter(
        /**
         * Id of the node to move.
         */
        public val nodeId: Int,
        /**
         * Id of the element to drop the moved node into.
         */
        public val targetNodeId: Int,
        /**
         * Drop node before this one (if absent, the moved node becomes the last child of
         * `targetNodeId`).
         */
        public val insertBeforeNodeId: Int? = null,
    )

    @Serializable
    public data class MoveToReturn(
        /**
         * New id of the moved node.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class PerformSearchParameter(
        /**
         * Plain text or query selector or XPath search query.
         */
        public val query: String,
        /**
         * True to search in user agent shadow DOM.
         */
        public val includeUserAgentShadowDOM: Boolean? = null,
    )

    @Serializable
    public data class PerformSearchReturn(
        /**
         * Unique search session identifier.
         */
        public val searchId: String,
        /**
         * Number of search results.
         */
        public val resultCount: Int,
    )

    @Serializable
    public data class PushNodeByPathToFrontendParameter(
        /**
         * Path to node in the proprietary format.
         */
        public val path: String,
    )

    @Serializable
    public data class PushNodeByPathToFrontendReturn(
        /**
         * Id of the node for given path.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class PushNodesByBackendIdsToFrontendParameter(
        /**
         * The array of backend node ids.
         */
        public val backendNodeIds: List<Int>,
    )

    @Serializable
    public data class PushNodesByBackendIdsToFrontendReturn(
        /**
         * The array of ids of pushed nodes that correspond to the backend ids specified in
         * backendNodeIds.
         */
        public val nodeIds: List<Int>,
    )

    @Serializable
    public data class QuerySelectorParameter(
        /**
         * Id of the node to query upon.
         */
        public val nodeId: Int,
        /**
         * Selector string.
         */
        public val selector: String,
    )

    @Serializable
    public data class QuerySelectorReturn(
        /**
         * Query selector result.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class QuerySelectorAllParameter(
        /**
         * Id of the node to query upon.
         */
        public val nodeId: Int,
        /**
         * Selector string.
         */
        public val selector: String,
    )

    @Serializable
    public data class QuerySelectorAllReturn(
        /**
         * Query selector result.
         */
        public val nodeIds: List<Int>,
    )

    @Serializable
    public data class GetTopLayerElementsReturn(
        /**
         * NodeIds of top layer elements
         */
        public val nodeIds: List<Int>,
    )

    @Serializable
    public data class GetElementByRelationParameter(
        /**
         * Id of the node from which to query the relation.
         */
        public val nodeId: Int,
        /**
         * Type of relation to get.
         */
        public val relation: String,
    )

    @Serializable
    public data class GetElementByRelationReturn(
        /**
         * NodeId of the element matching the queried relation.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class RemoveAttributeParameter(
        /**
         * Id of the element to remove attribute from.
         */
        public val nodeId: Int,
        /**
         * Name of the attribute to remove.
         */
        public val name: String,
    )

    @Serializable
    public data class RemoveNodeParameter(
        /**
         * Id of the node to remove.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class RequestChildNodesParameter(
        /**
         * Id of the node to get children for.
         */
        public val nodeId: Int,
        /**
         * The maximum depth at which children should be retrieved, defaults to 1. Use -1 for the
         * entire subtree or provide an integer larger than 0.
         */
        public val depth: Int? = null,
        /**
         * Whether or not iframes and shadow roots should be traversed when returning the sub-tree
         * (default is false).
         */
        public val pierce: Boolean? = null,
    )

    @Serializable
    public data class RequestNodeParameter(
        /**
         * JavaScript object id to convert into node.
         */
        public val objectId: String,
    )

    @Serializable
    public data class RequestNodeReturn(
        /**
         * Node id for given object.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class ResolveNodeParameter(
        /**
         * Id of the node to resolve.
         */
        public val nodeId: Int? = null,
        /**
         * Backend identifier of the node to resolve.
         */
        public val backendNodeId: Int? = null,
        /**
         * Symbolic group name that can be used to release multiple objects.
         */
        public val objectGroup: String? = null,
        /**
         * Execution context in which to resolve the node.
         */
        public val executionContextId: Int? = null,
    )

    @Serializable
    public data class ResolveNodeReturn(
        /**
         * JavaScript object wrapper for given node.
         */
        public val `object`: Runtime.RemoteObject,
    )

    @Serializable
    public data class SetAttributeValueParameter(
        /**
         * Id of the element to set attribute for.
         */
        public val nodeId: Int,
        /**
         * Attribute name.
         */
        public val name: String,
        /**
         * Attribute value.
         */
        public val `value`: String,
    )

    @Serializable
    public data class SetAttributesAsTextParameter(
        /**
         * Id of the element to set attributes for.
         */
        public val nodeId: Int,
        /**
         * Text with a number of attributes. Will parse this text using HTML parser.
         */
        public val text: String,
        /**
         * Attribute name to replace with new attributes derived from text in case text parsed
         * successfully.
         */
        public val name: String? = null,
    )

    @Serializable
    public data class SetFileInputFilesParameter(
        /**
         * Array of file paths to set.
         */
        public val files: List<String>,
        /**
         * Identifier of the node.
         */
        public val nodeId: Int? = null,
        /**
         * Identifier of the backend node.
         */
        public val backendNodeId: Int? = null,
        /**
         * JavaScript object id of the node wrapper.
         */
        public val objectId: String? = null,
    )

    @Serializable
    public data class SetNodeStackTracesEnabledParameter(
        /**
         * Enable or disable.
         */
        public val enable: Boolean,
    )

    @Serializable
    public data class GetNodeStackTracesParameter(
        /**
         * Id of the node to get stack traces for.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class GetNodeStackTracesReturn(
        /**
         * Creation stack trace, if available.
         */
        public val creation: Runtime.StackTrace?,
    )

    @Serializable
    public data class GetFileInfoParameter(
        /**
         * JavaScript object id of the node wrapper.
         */
        public val objectId: String,
    )

    @Serializable
    public data class GetFileInfoReturn(
        public val path: String,
    )

    @Serializable
    public data class GetDetachedDomNodesReturn(
        /**
         * The list of detached nodes
         */
        public val detachedNodes: List<DetachedElementInfo>,
    )

    @Serializable
    public data class SetInspectedNodeParameter(
        /**
         * DOM node id to be accessible by means of $x command line API.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class SetNodeNameParameter(
        /**
         * Id of the node to set name for.
         */
        public val nodeId: Int,
        /**
         * New node's name.
         */
        public val name: String,
    )

    @Serializable
    public data class SetNodeNameReturn(
        /**
         * New node's id.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class SetNodeValueParameter(
        /**
         * Id of the node to set value for.
         */
        public val nodeId: Int,
        /**
         * New node's value.
         */
        public val `value`: String,
    )

    @Serializable
    public data class SetOuterHTMLParameter(
        /**
         * Id of the node to set markup for.
         */
        public val nodeId: Int,
        /**
         * Outer HTML markup to set.
         */
        public val outerHTML: String,
    )

    @Serializable
    public data class GetFrameOwnerParameter(
        public val frameId: String,
    )

    @Serializable
    public data class GetFrameOwnerReturn(
        /**
         * Resulting node.
         */
        public val backendNodeId: Int,
        /**
         * Id of the node at given coordinates, only when enabled and requested document.
         */
        public val nodeId: Int?,
    )

    @Serializable
    public data class GetContainerForNodeParameter(
        public val nodeId: Int,
        public val containerName: String? = null,
        public val physicalAxes: PhysicalAxes? = null,
        public val logicalAxes: LogicalAxes? = null,
        public val queriesScrollState: Boolean? = null,
    )

    @Serializable
    public data class GetContainerForNodeReturn(
        /**
         * The container node for the given node, or null if not found.
         */
        public val nodeId: Int?,
    )

    @Serializable
    public data class GetQueryingDescendantsForContainerParameter(
        /**
         * Id of the container node to find querying descendants from.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class GetQueryingDescendantsForContainerReturn(
        /**
         * Descendant nodes with container queries against the given container.
         */
        public val nodeIds: List<Int>,
    )

    @Serializable
    public data class GetAnchorElementParameter(
        /**
         * Id of the positioned element from which to find the anchor.
         */
        public val nodeId: Int,
        /**
         * An optional anchor specifier, as defined in
         * https://www.w3.org/TR/css-anchor-position-1/#anchor-specifier.
         * If not provided, it will return the implicit anchor element for
         * the given positioned element.
         */
        public val anchorSpecifier: String? = null,
    )

    @Serializable
    public data class GetAnchorElementReturn(
        /**
         * The anchor element of the given anchor query.
         */
        public val nodeId: Int,
    )
}
