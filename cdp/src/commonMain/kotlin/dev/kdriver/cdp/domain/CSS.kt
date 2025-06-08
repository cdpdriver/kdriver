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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.css: CSS
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(CSS(this))

/**
 * This domain exposes CSS read/write operations. All CSS objects (stylesheets, rules, and styles)
 * have an associated `id` used in subsequent operations on the related object. Each object type has
 * a specific `id` structure, and those are not interchangeable between objects of different kinds.
 * CSS objects can be loaded using the `get*ForNode()` calls (which accept a DOM node id). A client
 * can also keep track of stylesheets via the `styleSheetAdded`/`styleSheetRemoved` events and
 * subsequently load the required stylesheet contents using the `getStyleSheet[Text]()` methods.
 */
public class CSS(
    private val cdp: CDP,
) : Domain {
    /**
     * Fires whenever a web font is updated.  A non-empty font parameter indicates a successfully loaded
     * web font.
     */
    public val fontsUpdated: Flow<FontsUpdatedParameter> = cdp
        .events
        .filter { it.method == "CSS.fontsUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fires whenever a MediaQuery result changes (for example, after a browser window has been
     * resized.) The current implementation considers only viewport-dependent media features.
     */
    public val mediaQueryResultChanged: Flow<Unit> = cdp
        .events
        .filter { it.method == "CSS.mediaQueryResultChanged" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired whenever an active document stylesheet is added.
     */
    public val styleSheetAdded: Flow<StyleSheetAddedParameter> = cdp
        .events
        .filter { it.method == "CSS.styleSheetAdded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired whenever a stylesheet is changed as a result of the client operation.
     */
    public val styleSheetChanged: Flow<StyleSheetChangedParameter> = cdp
        .events
        .filter { it.method == "CSS.styleSheetChanged" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired whenever an active document stylesheet is removed.
     */
    public val styleSheetRemoved: Flow<StyleSheetRemovedParameter> = cdp
        .events
        .filter { it.method == "CSS.styleSheetRemoved" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Inserts a new rule with the given `ruleText` in a stylesheet with given `styleSheetId`, at the
     * position specified by `location`.
     */
    public suspend fun addRule(args: AddRuleParameter): AddRuleReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.addRule", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Inserts a new rule with the given `ruleText` in a stylesheet with given `styleSheetId`, at the
     * position specified by `location`.
     *
     * @param styleSheetId The css style sheet identifier where a new rule should be inserted.
     * @param ruleText The text of a new rule.
     * @param location Text position of a new rule in the target style sheet.
     * @param nodeForPropertySyntaxValidation NodeId for the DOM node in whose context custom property declarations for registered properties should be
     * validated. If omitted, declarations in the new rule text can only be validated statically, which may produce
     * incorrect results if the declaration contains a var() for example.
     */
    public suspend fun addRule(
        styleSheetId: String,
        ruleText: String,
        location: SourceRange,
        nodeForPropertySyntaxValidation: Int? = null,
    ): AddRuleReturn {
        val parameter = AddRuleParameter(
            styleSheetId = styleSheetId,
            ruleText = ruleText,
            location = location,
            nodeForPropertySyntaxValidation = nodeForPropertySyntaxValidation
        )
        return addRule(parameter)
    }

    /**
     * Returns all class names from specified stylesheet.
     */
    public suspend fun collectClassNames(args: CollectClassNamesParameter): CollectClassNamesReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.collectClassNames", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns all class names from specified stylesheet.
     *
     * @param styleSheetId No description
     */
    public suspend fun collectClassNames(styleSheetId: String): CollectClassNamesReturn {
        val parameter = CollectClassNamesParameter(styleSheetId = styleSheetId)
        return collectClassNames(parameter)
    }

    /**
     * Creates a new special "via-inspector" stylesheet in the frame with given `frameId`.
     */
    public suspend fun createStyleSheet(args: CreateStyleSheetParameter): CreateStyleSheetReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.createStyleSheet", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Creates a new special "via-inspector" stylesheet in the frame with given `frameId`.
     *
     * @param frameId Identifier of the frame where "via-inspector" stylesheet should be created.
     */
    public suspend fun createStyleSheet(frameId: String): CreateStyleSheetReturn {
        val parameter = CreateStyleSheetParameter(frameId = frameId)
        return createStyleSheet(parameter)
    }

    /**
     * Disables the CSS agent for the given page.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("CSS.disable", parameter)
    }

    /**
     * Enables the CSS agent for the given page. Clients should not assume that the CSS agent has been
     * enabled until the result of this command is received.
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("CSS.enable", parameter)
    }

    /**
     * Ensures that the given node will have specified pseudo-classes whenever its style is computed by
     * the browser.
     */
    public suspend fun forcePseudoState(args: ForcePseudoStateParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("CSS.forcePseudoState", parameter)
    }

    /**
     * Ensures that the given node will have specified pseudo-classes whenever its style is computed by
     * the browser.
     *
     * @param nodeId The element id for which to force the pseudo state.
     * @param forcedPseudoClasses Element pseudo classes to force when computing the element's style.
     */
    public suspend fun forcePseudoState(nodeId: Int, forcedPseudoClasses: List<String>) {
        val parameter = ForcePseudoStateParameter(nodeId = nodeId, forcedPseudoClasses = forcedPseudoClasses)
        forcePseudoState(parameter)
    }

    public suspend fun getBackgroundColors(args: GetBackgroundColorsParameter): GetBackgroundColorsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.getBackgroundColors", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     *
     *
     * @param nodeId Id of the node to get background colors for.
     */
    public suspend fun getBackgroundColors(nodeId: Int): GetBackgroundColorsReturn {
        val parameter = GetBackgroundColorsParameter(nodeId = nodeId)
        return getBackgroundColors(parameter)
    }

    /**
     * Returns the computed style for a DOM node identified by `nodeId`.
     */
    public suspend fun getComputedStyleForNode(args: GetComputedStyleForNodeParameter): GetComputedStyleForNodeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.getComputedStyleForNode", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the computed style for a DOM node identified by `nodeId`.
     *
     * @param nodeId No description
     */
    public suspend fun getComputedStyleForNode(nodeId: Int): GetComputedStyleForNodeReturn {
        val parameter = GetComputedStyleForNodeParameter(nodeId = nodeId)
        return getComputedStyleForNode(parameter)
    }

    /**
     * Returns the styles defined inline (explicitly in the "style" attribute and implicitly, using DOM
     * attributes) for a DOM node identified by `nodeId`.
     */
    public suspend fun getInlineStylesForNode(args: GetInlineStylesForNodeParameter): GetInlineStylesForNodeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.getInlineStylesForNode", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the styles defined inline (explicitly in the "style" attribute and implicitly, using DOM
     * attributes) for a DOM node identified by `nodeId`.
     *
     * @param nodeId No description
     */
    public suspend fun getInlineStylesForNode(nodeId: Int): GetInlineStylesForNodeReturn {
        val parameter = GetInlineStylesForNodeParameter(nodeId = nodeId)
        return getInlineStylesForNode(parameter)
    }

    /**
     * Returns requested styles for a DOM node identified by `nodeId`.
     */
    public suspend fun getMatchedStylesForNode(args: GetMatchedStylesForNodeParameter): GetMatchedStylesForNodeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.getMatchedStylesForNode", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns requested styles for a DOM node identified by `nodeId`.
     *
     * @param nodeId No description
     */
    public suspend fun getMatchedStylesForNode(nodeId: Int): GetMatchedStylesForNodeReturn {
        val parameter = GetMatchedStylesForNodeParameter(nodeId = nodeId)
        return getMatchedStylesForNode(parameter)
    }

    /**
     * Returns all media queries parsed by the rendering engine.
     */
    public suspend fun getMediaQueries(): GetMediaQueriesReturn {
        val parameter = null
        val result = cdp.callCommand("CSS.getMediaQueries", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Requests information about platform fonts which we used to render child TextNodes in the given
     * node.
     */
    public suspend fun getPlatformFontsForNode(args: GetPlatformFontsForNodeParameter): GetPlatformFontsForNodeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.getPlatformFontsForNode", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Requests information about platform fonts which we used to render child TextNodes in the given
     * node.
     *
     * @param nodeId No description
     */
    public suspend fun getPlatformFontsForNode(nodeId: Int): GetPlatformFontsForNodeReturn {
        val parameter = GetPlatformFontsForNodeParameter(nodeId = nodeId)
        return getPlatformFontsForNode(parameter)
    }

    /**
     * Returns the current textual content for a stylesheet.
     */
    public suspend fun getStyleSheetText(args: GetStyleSheetTextParameter): GetStyleSheetTextReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.getStyleSheetText", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the current textual content for a stylesheet.
     *
     * @param styleSheetId No description
     */
    public suspend fun getStyleSheetText(styleSheetId: String): GetStyleSheetTextReturn {
        val parameter = GetStyleSheetTextParameter(styleSheetId = styleSheetId)
        return getStyleSheetText(parameter)
    }

    /**
     * Returns all layers parsed by the rendering engine for the tree scope of a node.
     * Given a DOM element identified by nodeId, getLayersForNode returns the root
     * layer for the nearest ancestor document or shadow root. The layer root contains
     * the full layer tree for the tree scope and their ordering.
     */
    public suspend fun getLayersForNode(args: GetLayersForNodeParameter): GetLayersForNodeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.getLayersForNode", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns all layers parsed by the rendering engine for the tree scope of a node.
     * Given a DOM element identified by nodeId, getLayersForNode returns the root
     * layer for the nearest ancestor document or shadow root. The layer root contains
     * the full layer tree for the tree scope and their ordering.
     *
     * @param nodeId No description
     */
    public suspend fun getLayersForNode(nodeId: Int): GetLayersForNodeReturn {
        val parameter = GetLayersForNodeParameter(nodeId = nodeId)
        return getLayersForNode(parameter)
    }

    /**
     * Starts tracking the given computed styles for updates. The specified array of properties
     * replaces the one previously specified. Pass empty array to disable tracking.
     * Use takeComputedStyleUpdates to retrieve the list of nodes that had properties modified.
     * The changes to computed style properties are only tracked for nodes pushed to the front-end
     * by the DOM agent. If no changes to the tracked properties occur after the node has been pushed
     * to the front-end, no updates will be issued for the node.
     */
    public suspend fun trackComputedStyleUpdates(args: TrackComputedStyleUpdatesParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("CSS.trackComputedStyleUpdates", parameter)
    }

    /**
     * Starts tracking the given computed styles for updates. The specified array of properties
     * replaces the one previously specified. Pass empty array to disable tracking.
     * Use takeComputedStyleUpdates to retrieve the list of nodes that had properties modified.
     * The changes to computed style properties are only tracked for nodes pushed to the front-end
     * by the DOM agent. If no changes to the tracked properties occur after the node has been pushed
     * to the front-end, no updates will be issued for the node.
     *
     * @param propertiesToTrack No description
     */
    public suspend fun trackComputedStyleUpdates(propertiesToTrack: List<CSSComputedStyleProperty>) {
        val parameter = TrackComputedStyleUpdatesParameter(propertiesToTrack = propertiesToTrack)
        trackComputedStyleUpdates(parameter)
    }

    /**
     * Polls the next batch of computed style updates.
     */
    public suspend fun takeComputedStyleUpdates(): TakeComputedStyleUpdatesReturn {
        val parameter = null
        val result = cdp.callCommand("CSS.takeComputedStyleUpdates", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Find a rule with the given active property for the given node and set the new value for this
     * property
     */
    public suspend fun setEffectivePropertyValueForNode(args: SetEffectivePropertyValueForNodeParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("CSS.setEffectivePropertyValueForNode", parameter)
    }

    /**
     * Find a rule with the given active property for the given node and set the new value for this
     * property
     *
     * @param nodeId The element id for which to set property.
     * @param propertyName No description
     * @param value No description
     */
    public suspend fun setEffectivePropertyValueForNode(
        nodeId: Int,
        propertyName: String,
        `value`: String,
    ) {
        val parameter =
            SetEffectivePropertyValueForNodeParameter(nodeId = nodeId, propertyName = propertyName, value = value)
        setEffectivePropertyValueForNode(parameter)
    }

    /**
     * Modifies the property rule property name.
     */
    public suspend fun setPropertyRulePropertyName(args: SetPropertyRulePropertyNameParameter): SetPropertyRulePropertyNameReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.setPropertyRulePropertyName", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Modifies the property rule property name.
     *
     * @param styleSheetId No description
     * @param range No description
     * @param propertyName No description
     */
    public suspend fun setPropertyRulePropertyName(
        styleSheetId: String,
        range: SourceRange,
        propertyName: String,
    ): SetPropertyRulePropertyNameReturn {
        val parameter = SetPropertyRulePropertyNameParameter(
            styleSheetId = styleSheetId,
            range = range,
            propertyName = propertyName
        )
        return setPropertyRulePropertyName(parameter)
    }

    /**
     * Modifies the keyframe rule key text.
     */
    public suspend fun setKeyframeKey(args: SetKeyframeKeyParameter): SetKeyframeKeyReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.setKeyframeKey", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Modifies the keyframe rule key text.
     *
     * @param styleSheetId No description
     * @param range No description
     * @param keyText No description
     */
    public suspend fun setKeyframeKey(
        styleSheetId: String,
        range: SourceRange,
        keyText: String,
    ): SetKeyframeKeyReturn {
        val parameter = SetKeyframeKeyParameter(styleSheetId = styleSheetId, range = range, keyText = keyText)
        return setKeyframeKey(parameter)
    }

    /**
     * Modifies the rule selector.
     */
    public suspend fun setMediaText(args: SetMediaTextParameter): SetMediaTextReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.setMediaText", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Modifies the rule selector.
     *
     * @param styleSheetId No description
     * @param range No description
     * @param text No description
     */
    public suspend fun setMediaText(
        styleSheetId: String,
        range: SourceRange,
        text: String,
    ): SetMediaTextReturn {
        val parameter = SetMediaTextParameter(styleSheetId = styleSheetId, range = range, text = text)
        return setMediaText(parameter)
    }

    /**
     * Modifies the expression of a container query.
     */
    public suspend fun setContainerQueryText(args: SetContainerQueryTextParameter): SetContainerQueryTextReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.setContainerQueryText", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Modifies the expression of a container query.
     *
     * @param styleSheetId No description
     * @param range No description
     * @param text No description
     */
    public suspend fun setContainerQueryText(
        styleSheetId: String,
        range: SourceRange,
        text: String,
    ): SetContainerQueryTextReturn {
        val parameter = SetContainerQueryTextParameter(styleSheetId = styleSheetId, range = range, text = text)
        return setContainerQueryText(parameter)
    }

    /**
     * Modifies the expression of a supports at-rule.
     */
    public suspend fun setSupportsText(args: SetSupportsTextParameter): SetSupportsTextReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.setSupportsText", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Modifies the expression of a supports at-rule.
     *
     * @param styleSheetId No description
     * @param range No description
     * @param text No description
     */
    public suspend fun setSupportsText(
        styleSheetId: String,
        range: SourceRange,
        text: String,
    ): SetSupportsTextReturn {
        val parameter = SetSupportsTextParameter(styleSheetId = styleSheetId, range = range, text = text)
        return setSupportsText(parameter)
    }

    /**
     * Modifies the expression of a scope at-rule.
     */
    public suspend fun setScopeText(args: SetScopeTextParameter): SetScopeTextReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.setScopeText", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Modifies the expression of a scope at-rule.
     *
     * @param styleSheetId No description
     * @param range No description
     * @param text No description
     */
    public suspend fun setScopeText(
        styleSheetId: String,
        range: SourceRange,
        text: String,
    ): SetScopeTextReturn {
        val parameter = SetScopeTextParameter(styleSheetId = styleSheetId, range = range, text = text)
        return setScopeText(parameter)
    }

    /**
     * Modifies the rule selector.
     */
    public suspend fun setRuleSelector(args: SetRuleSelectorParameter): SetRuleSelectorReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.setRuleSelector", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Modifies the rule selector.
     *
     * @param styleSheetId No description
     * @param range No description
     * @param selector No description
     */
    public suspend fun setRuleSelector(
        styleSheetId: String,
        range: SourceRange,
        selector: String,
    ): SetRuleSelectorReturn {
        val parameter = SetRuleSelectorParameter(styleSheetId = styleSheetId, range = range, selector = selector)
        return setRuleSelector(parameter)
    }

    /**
     * Sets the new stylesheet text.
     */
    public suspend fun setStyleSheetText(args: SetStyleSheetTextParameter): SetStyleSheetTextReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.setStyleSheetText", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Sets the new stylesheet text.
     *
     * @param styleSheetId No description
     * @param text No description
     */
    public suspend fun setStyleSheetText(styleSheetId: String, text: String): SetStyleSheetTextReturn {
        val parameter = SetStyleSheetTextParameter(styleSheetId = styleSheetId, text = text)
        return setStyleSheetText(parameter)
    }

    /**
     * Applies specified style edits one after another in the given order.
     */
    public suspend fun setStyleTexts(args: SetStyleTextsParameter): SetStyleTextsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("CSS.setStyleTexts", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Applies specified style edits one after another in the given order.
     *
     * @param edits No description
     * @param nodeForPropertySyntaxValidation NodeId for the DOM node in whose context custom property declarations for registered properties should be
     * validated. If omitted, declarations in the new rule text can only be validated statically, which may produce
     * incorrect results if the declaration contains a var() for example.
     */
    public suspend fun setStyleTexts(
        edits: List<StyleDeclarationEdit>,
        nodeForPropertySyntaxValidation: Int? = null,
    ): SetStyleTextsReturn {
        val parameter =
            SetStyleTextsParameter(edits = edits, nodeForPropertySyntaxValidation = nodeForPropertySyntaxValidation)
        return setStyleTexts(parameter)
    }

    /**
     * Enables the selector recording.
     */
    public suspend fun startRuleUsageTracking() {
        val parameter = null
        cdp.callCommand("CSS.startRuleUsageTracking", parameter)
    }

    /**
     * Stop tracking rule usage and return the list of rules that were used since last call to
     * `takeCoverageDelta` (or since start of coverage instrumentation).
     */
    public suspend fun stopRuleUsageTracking(): StopRuleUsageTrackingReturn {
        val parameter = null
        val result = cdp.callCommand("CSS.stopRuleUsageTracking", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Obtain list of rules that became used since last call to this method (or since start of coverage
     * instrumentation).
     */
    public suspend fun takeCoverageDelta(): TakeCoverageDeltaReturn {
        val parameter = null
        val result = cdp.callCommand("CSS.takeCoverageDelta", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Enables/disables rendering of local CSS fonts (enabled by default).
     */
    public suspend fun setLocalFontsEnabled(args: SetLocalFontsEnabledParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("CSS.setLocalFontsEnabled", parameter)
    }

    /**
     * Enables/disables rendering of local CSS fonts (enabled by default).
     *
     * @param enabled Whether rendering of local fonts is enabled.
     */
    public suspend fun setLocalFontsEnabled(enabled: Boolean) {
        val parameter = SetLocalFontsEnabledParameter(enabled = enabled)
        setLocalFontsEnabled(parameter)
    }

    /**
     * Stylesheet type: "injected" for stylesheets injected via extension, "user-agent" for user-agent
     * stylesheets, "inspector" for stylesheets created by the inspector (i.e. those holding the "via
     * inspector" rules), "regular" for regular stylesheets.
     */
    @Serializable
    public enum class StyleSheetOrigin {
        @SerialName("injected")
        INJECTED,

        @SerialName("user-agent")
        USER_AGENT,

        @SerialName("inspector")
        INSPECTOR,

        @SerialName("regular")
        REGULAR,
    }

    /**
     * CSS rule collection for a single pseudo style.
     */
    @Serializable
    public data class PseudoElementMatches(
        /**
         * Pseudo element type.
         */
        public val pseudoType: DOM.PseudoType,
        /**
         * Pseudo element custom ident.
         */
        public val pseudoIdentifier: String? = null,
        /**
         * Matches of CSS rules applicable to the pseudo style.
         */
        public val matches: List<RuleMatch>,
    )

    /**
     * Inherited CSS rule collection from ancestor node.
     */
    @Serializable
    public data class InheritedStyleEntry(
        /**
         * The ancestor node's inline style, if any, in the style inheritance chain.
         */
        public val inlineStyle: CSSStyle? = null,
        /**
         * Matches of CSS rules matching the ancestor node in the style inheritance chain.
         */
        public val matchedCSSRules: List<RuleMatch>,
    )

    /**
     * Inherited pseudo element matches from pseudos of an ancestor node.
     */
    @Serializable
    public data class InheritedPseudoElementMatches(
        /**
         * Matches of pseudo styles from the pseudos of an ancestor node.
         */
        public val pseudoElements: List<PseudoElementMatches>,
    )

    /**
     * Match data for a CSS rule.
     */
    @Serializable
    public data class RuleMatch(
        /**
         * CSS rule in the match.
         */
        public val rule: CSSRule,
        /**
         * Matching selector indices in the rule's selectorList selectors (0-based).
         */
        public val matchingSelectors: List<Int>,
    )

    /**
     * Data for a simple selector (these are delimited by commas in a selector list).
     */
    @Serializable
    public data class Value(
        /**
         * Value text.
         */
        public val text: String,
        /**
         * Value range in the underlying resource (if available).
         */
        public val range: SourceRange? = null,
        /**
         * Specificity of the selector.
         */
        public val specificity: Specificity? = null,
    )

    /**
     * Specificity:
     * https://drafts.csswg.org/selectors/#specificity-rules
     */
    @Serializable
    public data class Specificity(
        /**
         * The a component, which represents the number of ID selectors.
         */
        public val a: Int,
        /**
         * The b component, which represents the number of class selectors, attributes selectors, and
         * pseudo-classes.
         */
        public val b: Int,
        /**
         * The c component, which represents the number of type selectors and pseudo-elements.
         */
        public val c: Int,
    )

    /**
     * Selector list data.
     */
    @Serializable
    public data class SelectorList(
        /**
         * Selectors in the list.
         */
        public val selectors: List<Value>,
        /**
         * Rule selector text.
         */
        public val text: String,
    )

    /**
     * CSS stylesheet metainformation.
     */
    @Serializable
    public data class CSSStyleSheetHeader(
        /**
         * The stylesheet identifier.
         */
        public val styleSheetId: String,
        /**
         * Owner frame identifier.
         */
        public val frameId: String,
        /**
         * Stylesheet resource URL. Empty if this is a constructed stylesheet created using
         * new CSSStyleSheet() (but non-empty if this is a constructed sylesheet imported
         * as a CSS module script).
         */
        public val sourceURL: String,
        /**
         * URL of source map associated with the stylesheet (if any).
         */
        public val sourceMapURL: String? = null,
        /**
         * Stylesheet origin.
         */
        public val origin: StyleSheetOrigin,
        /**
         * Stylesheet title.
         */
        public val title: String,
        /**
         * The backend id for the owner node of the stylesheet.
         */
        public val ownerNode: Int? = null,
        /**
         * Denotes whether the stylesheet is disabled.
         */
        public val disabled: Boolean,
        /**
         * Whether the sourceURL field value comes from the sourceURL comment.
         */
        public val hasSourceURL: Boolean? = null,
        /**
         * Whether this stylesheet is created for STYLE tag by parser. This flag is not set for
         * document.written STYLE tags.
         */
        public val isInline: Boolean,
        /**
         * Whether this stylesheet is mutable. Inline stylesheets become mutable
         * after they have been modified via CSSOM API.
         * `<link>` element's stylesheets become mutable only if DevTools modifies them.
         * Constructed stylesheets (new CSSStyleSheet()) are mutable immediately after creation.
         */
        public val isMutable: Boolean,
        /**
         * True if this stylesheet is created through new CSSStyleSheet() or imported as a
         * CSS module script.
         */
        public val isConstructed: Boolean,
        /**
         * Line offset of the stylesheet within the resource (zero based).
         */
        public val startLine: Double,
        /**
         * Column offset of the stylesheet within the resource (zero based).
         */
        public val startColumn: Double,
        /**
         * Size of the content (in characters).
         */
        public val length: Double,
        /**
         * Line offset of the end of the stylesheet within the resource (zero based).
         */
        public val endLine: Double,
        /**
         * Column offset of the end of the stylesheet within the resource (zero based).
         */
        public val endColumn: Double,
        /**
         * If the style sheet was loaded from a network resource, this indicates when the resource failed to load
         */
        public val loadingFailed: Boolean? = null,
    )

    /**
     * CSS rule representation.
     */
    @Serializable
    public data class CSSRule(
        /**
         * The css style sheet identifier (absent for user agent stylesheet and user-specified
         * stylesheet rules) this rule came from.
         */
        public val styleSheetId: String? = null,
        /**
         * Rule selector data.
         */
        public val selectorList: SelectorList,
        /**
         * Array of selectors from ancestor style rules, sorted by distance from the current rule.
         */
        public val nestingSelectors: List<String>? = null,
        /**
         * Parent stylesheet's origin.
         */
        public val origin: StyleSheetOrigin,
        /**
         * Associated style declaration.
         */
        public val style: CSSStyle,
        /**
         * Media list array (for rules involving media queries). The array enumerates media queries
         * starting with the innermost one, going outwards.
         */
        public val media: List<CSSMedia>? = null,
        /**
         * Container query list array (for rules involving container queries).
         * The array enumerates container queries starting with the innermost one, going outwards.
         */
        public val containerQueries: List<CSSContainerQuery>? = null,
        /**
         * @supports CSS at-rule array.
         * The array enumerates @supports at-rules starting with the innermost one, going outwards.
         */
        public val supports: List<CSSSupports>? = null,
        /**
         * Cascade layer array. Contains the layer hierarchy that this rule belongs to starting
         * with the innermost layer and going outwards.
         */
        public val layers: List<CSSLayer>? = null,
        /**
         * @scope CSS at-rule array.
         * The array enumerates @scope at-rules starting with the innermost one, going outwards.
         */
        public val scopes: List<CSSScope>? = null,
        /**
         * The array keeps the types of ancestor CSSRules from the innermost going outwards.
         */
        public val ruleTypes: List<CSSRuleType>? = null,
    )

    /**
     * Enum indicating the type of a CSS rule, used to represent the order of a style rule's ancestors.
     * This list only contains rule types that are collected during the ancestor rule collection.
     */
    @Serializable
    public enum class CSSRuleType {
        @SerialName("MediaRule")
        MEDIARULE,

        @SerialName("SupportsRule")
        SUPPORTSRULE,

        @SerialName("ContainerRule")
        CONTAINERRULE,

        @SerialName("LayerRule")
        LAYERRULE,

        @SerialName("ScopeRule")
        SCOPERULE,

        @SerialName("StyleRule")
        STYLERULE,
    }

    /**
     * CSS coverage information.
     */
    @Serializable
    public data class RuleUsage(
        /**
         * The css style sheet identifier (absent for user agent stylesheet and user-specified
         * stylesheet rules) this rule came from.
         */
        public val styleSheetId: String,
        /**
         * Offset of the start of the rule (including selector) from the beginning of the stylesheet.
         */
        public val startOffset: Double,
        /**
         * Offset of the end of the rule body from the beginning of the stylesheet.
         */
        public val endOffset: Double,
        /**
         * Indicates whether the rule was actually used by some element in the page.
         */
        public val used: Boolean,
    )

    /**
     * Text range within a resource. All numbers are zero-based.
     */
    @Serializable
    public data class SourceRange(
        /**
         * Start line of range.
         */
        public val startLine: Int,
        /**
         * Start column of range (inclusive).
         */
        public val startColumn: Int,
        /**
         * End line of range
         */
        public val endLine: Int,
        /**
         * End column of range (exclusive).
         */
        public val endColumn: Int,
    )

    @Serializable
    public data class ShorthandEntry(
        /**
         * Shorthand name.
         */
        public val name: String,
        /**
         * Shorthand value.
         */
        public val `value`: String,
        /**
         * Whether the property has "!important" annotation (implies `false` if absent).
         */
        public val important: Boolean? = null,
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
     * CSS style representation.
     */
    @Serializable
    public data class CSSStyle(
        /**
         * The css style sheet identifier (absent for user agent stylesheet and user-specified
         * stylesheet rules) this rule came from.
         */
        public val styleSheetId: String? = null,
        /**
         * CSS properties in the style.
         */
        public val cssProperties: List<CSSProperty>,
        /**
         * Computed values for all shorthands found in the style.
         */
        public val shorthandEntries: List<ShorthandEntry>,
        /**
         * Style declaration text (if available).
         */
        public val cssText: String? = null,
        /**
         * Style declaration range in the enclosing stylesheet (if available).
         */
        public val range: SourceRange? = null,
    )

    /**
     * CSS property declaration data.
     */
    @Serializable
    public data class CSSProperty(
        /**
         * The property name.
         */
        public val name: String,
        /**
         * The property value.
         */
        public val `value`: String,
        /**
         * Whether the property has "!important" annotation (implies `false` if absent).
         */
        public val important: Boolean? = null,
        /**
         * Whether the property is implicit (implies `false` if absent).
         */
        public val implicit: Boolean? = null,
        /**
         * The full property text as specified in the style.
         */
        public val text: String? = null,
        /**
         * Whether the property is understood by the browser (implies `true` if absent).
         */
        public val parsedOk: Boolean? = null,
        /**
         * Whether the property is disabled by the user (present for source-based properties only).
         */
        public val disabled: Boolean? = null,
        /**
         * The entire property range in the enclosing style declaration (if available).
         */
        public val range: SourceRange? = null,
        /**
         * Parsed longhand components of this property if it is a shorthand.
         * This field will be empty if the given property is not a shorthand.
         */
        public val longhandProperties: List<CSSProperty>? = null,
    )

    /**
     * CSS media rule descriptor.
     */
    @Serializable
    public data class CSSMedia(
        /**
         * Media query text.
         */
        public val text: String,
        /**
         * Source of the media query: "mediaRule" if specified by a @media rule, "importRule" if
         * specified by an @import rule, "linkedSheet" if specified by a "media" attribute in a linked
         * stylesheet's LINK tag, "inlineSheet" if specified by a "media" attribute in an inline
         * stylesheet's STYLE tag.
         */
        public val source: String,
        /**
         * URL of the document containing the media query description.
         */
        public val sourceURL: String? = null,
        /**
         * The associated rule (@media or @import) header range in the enclosing stylesheet (if
         * available).
         */
        public val range: SourceRange? = null,
        /**
         * Identifier of the stylesheet containing this object (if exists).
         */
        public val styleSheetId: String? = null,
        /**
         * Array of media queries.
         */
        public val mediaList: List<MediaQuery>? = null,
    )

    /**
     * Media query descriptor.
     */
    @Serializable
    public data class MediaQuery(
        /**
         * Array of media query expressions.
         */
        public val expressions: List<MediaQueryExpression>,
        /**
         * Whether the media query condition is satisfied.
         */
        public val active: Boolean,
    )

    /**
     * Media query expression descriptor.
     */
    @Serializable
    public data class MediaQueryExpression(
        /**
         * Media query expression value.
         */
        public val `value`: Double,
        /**
         * Media query expression units.
         */
        public val unit: String,
        /**
         * Media query expression feature.
         */
        public val feature: String,
        /**
         * The associated range of the value text in the enclosing stylesheet (if available).
         */
        public val valueRange: SourceRange? = null,
        /**
         * Computed length of media query expression (if applicable).
         */
        public val computedLength: Double? = null,
    )

    /**
     * CSS container query rule descriptor.
     */
    @Serializable
    public data class CSSContainerQuery(
        /**
         * Container query text.
         */
        public val text: String,
        /**
         * The associated rule header range in the enclosing stylesheet (if
         * available).
         */
        public val range: SourceRange? = null,
        /**
         * Identifier of the stylesheet containing this object (if exists).
         */
        public val styleSheetId: String? = null,
        /**
         * Optional name for the container.
         */
        public val name: String? = null,
        /**
         * Optional physical axes queried for the container.
         */
        public val physicalAxes: DOM.PhysicalAxes? = null,
        /**
         * Optional logical axes queried for the container.
         */
        public val logicalAxes: DOM.LogicalAxes? = null,
    )

    /**
     * CSS Supports at-rule descriptor.
     */
    @Serializable
    public data class CSSSupports(
        /**
         * Supports rule text.
         */
        public val text: String,
        /**
         * Whether the supports condition is satisfied.
         */
        public val active: Boolean,
        /**
         * The associated rule header range in the enclosing stylesheet (if
         * available).
         */
        public val range: SourceRange? = null,
        /**
         * Identifier of the stylesheet containing this object (if exists).
         */
        public val styleSheetId: String? = null,
    )

    /**
     * CSS Scope at-rule descriptor.
     */
    @Serializable
    public data class CSSScope(
        /**
         * Scope rule text.
         */
        public val text: String,
        /**
         * The associated rule header range in the enclosing stylesheet (if
         * available).
         */
        public val range: SourceRange? = null,
        /**
         * Identifier of the stylesheet containing this object (if exists).
         */
        public val styleSheetId: String? = null,
    )

    /**
     * CSS Layer at-rule descriptor.
     */
    @Serializable
    public data class CSSLayer(
        /**
         * Layer name.
         */
        public val text: String,
        /**
         * The associated rule header range in the enclosing stylesheet (if
         * available).
         */
        public val range: SourceRange? = null,
        /**
         * Identifier of the stylesheet containing this object (if exists).
         */
        public val styleSheetId: String? = null,
    )

    /**
     * CSS Layer data.
     */
    @Serializable
    public data class CSSLayerData(
        /**
         * Layer name.
         */
        public val name: String,
        /**
         * Direct sub-layers
         */
        public val subLayers: List<CSSLayerData>? = null,
        /**
         * Layer order. The order determines the order of the layer in the cascade order.
         * A higher number has higher priority in the cascade order.
         */
        public val order: Double,
    )

    /**
     * Information about amount of glyphs that were rendered with given font.
     */
    @Serializable
    public data class PlatformFontUsage(
        /**
         * Font's family name reported by platform.
         */
        public val familyName: String,
        /**
         * Font's PostScript name reported by platform.
         */
        public val postScriptName: String,
        /**
         * Indicates if the font was downloaded or resolved locally.
         */
        public val isCustomFont: Boolean,
        /**
         * Amount of glyphs that were rendered with this font.
         */
        public val glyphCount: Double,
    )

    /**
     * Information about font variation axes for variable fonts
     */
    @Serializable
    public data class FontVariationAxis(
        /**
         * The font-variation-setting tag (a.k.a. "axis tag").
         */
        public val tag: String,
        /**
         * Human-readable variation name in the default language (normally, "en").
         */
        public val name: String,
        /**
         * The minimum value (inclusive) the font supports for this tag.
         */
        public val minValue: Double,
        /**
         * The maximum value (inclusive) the font supports for this tag.
         */
        public val maxValue: Double,
        /**
         * The default value.
         */
        public val defaultValue: Double,
    )

    /**
     * Properties of a web font: https://www.w3.org/TR/2008/REC-CSS2-20080411/fonts.html#font-descriptions
     * and additional information such as platformFontFamily and fontVariationAxes.
     */
    @Serializable
    public data class FontFace(
        /**
         * The font-family.
         */
        public val fontFamily: String,
        /**
         * The font-style.
         */
        public val fontStyle: String,
        /**
         * The font-variant.
         */
        public val fontVariant: String,
        /**
         * The font-weight.
         */
        public val fontWeight: String,
        /**
         * The font-stretch.
         */
        public val fontStretch: String,
        /**
         * The font-display.
         */
        public val fontDisplay: String,
        /**
         * The unicode-range.
         */
        public val unicodeRange: String,
        /**
         * The src.
         */
        public val src: String,
        /**
         * The resolved platform font family
         */
        public val platformFontFamily: String,
        /**
         * Available variation settings (a.k.a. "axes").
         */
        public val fontVariationAxes: List<FontVariationAxis>? = null,
    )

    /**
     * CSS try rule representation.
     */
    @Serializable
    public data class CSSTryRule(
        /**
         * The css style sheet identifier (absent for user agent stylesheet and user-specified
         * stylesheet rules) this rule came from.
         */
        public val styleSheetId: String? = null,
        /**
         * Parent stylesheet's origin.
         */
        public val origin: StyleSheetOrigin,
        /**
         * Associated style declaration.
         */
        public val style: CSSStyle,
    )

    /**
     * CSS position-fallback rule representation.
     */
    @Serializable
    public data class CSSPositionFallbackRule(
        public val name: Value,
        /**
         * List of keyframes.
         */
        public val tryRules: List<CSSTryRule>,
    )

    /**
     * CSS keyframes rule representation.
     */
    @Serializable
    public data class CSSKeyframesRule(
        /**
         * Animation name.
         */
        public val animationName: Value,
        /**
         * List of keyframes.
         */
        public val keyframes: List<CSSKeyframeRule>,
    )

    /**
     * Representation of a custom property registration through CSS.registerProperty
     */
    @Serializable
    public data class CSSPropertyRegistration(
        public val propertyName: String,
        public val initialValue: Value? = null,
        public val inherits: Boolean,
        public val syntax: String,
    )

    /**
     * CSS font-palette-values rule representation.
     */
    @Serializable
    public data class CSSFontPaletteValuesRule(
        /**
         * The css style sheet identifier (absent for user agent stylesheet and user-specified
         * stylesheet rules) this rule came from.
         */
        public val styleSheetId: String? = null,
        /**
         * Parent stylesheet's origin.
         */
        public val origin: StyleSheetOrigin,
        /**
         * Associated font palette name.
         */
        public val fontPaletteName: Value,
        /**
         * Associated style declaration.
         */
        public val style: CSSStyle,
    )

    /**
     * CSS property at-rule representation.
     */
    @Serializable
    public data class CSSPropertyRule(
        /**
         * The css style sheet identifier (absent for user agent stylesheet and user-specified
         * stylesheet rules) this rule came from.
         */
        public val styleSheetId: String? = null,
        /**
         * Parent stylesheet's origin.
         */
        public val origin: StyleSheetOrigin,
        /**
         * Associated property name.
         */
        public val propertyName: Value,
        /**
         * Associated style declaration.
         */
        public val style: CSSStyle,
    )

    /**
     * CSS keyframe rule representation.
     */
    @Serializable
    public data class CSSKeyframeRule(
        /**
         * The css style sheet identifier (absent for user agent stylesheet and user-specified
         * stylesheet rules) this rule came from.
         */
        public val styleSheetId: String? = null,
        /**
         * Parent stylesheet's origin.
         */
        public val origin: StyleSheetOrigin,
        /**
         * Associated key text.
         */
        public val keyText: Value,
        /**
         * Associated style declaration.
         */
        public val style: CSSStyle,
    )

    /**
     * A descriptor of operation to mutate style declaration text.
     */
    @Serializable
    public data class StyleDeclarationEdit(
        /**
         * The css style sheet identifier.
         */
        public val styleSheetId: String,
        /**
         * The range of the style text in the enclosing stylesheet.
         */
        public val range: SourceRange,
        /**
         * New style text.
         */
        public val text: String,
    )

    /**
     * Fires whenever a web font is updated.  A non-empty font parameter indicates a successfully loaded
     * web font.
     */
    @Serializable
    public data class FontsUpdatedParameter(
        /**
         * The web font that has loaded.
         */
        public val font: FontFace? = null,
    )

    /**
     * Fired whenever an active document stylesheet is added.
     */
    @Serializable
    public data class StyleSheetAddedParameter(
        /**
         * Added stylesheet metainfo.
         */
        public val `header`: CSSStyleSheetHeader,
    )

    /**
     * Fired whenever a stylesheet is changed as a result of the client operation.
     */
    @Serializable
    public data class StyleSheetChangedParameter(
        public val styleSheetId: String,
    )

    /**
     * Fired whenever an active document stylesheet is removed.
     */
    @Serializable
    public data class StyleSheetRemovedParameter(
        /**
         * Identifier of the removed stylesheet.
         */
        public val styleSheetId: String,
    )

    @Serializable
    public data class AddRuleParameter(
        /**
         * The css style sheet identifier where a new rule should be inserted.
         */
        public val styleSheetId: String,
        /**
         * The text of a new rule.
         */
        public val ruleText: String,
        /**
         * Text position of a new rule in the target style sheet.
         */
        public val location: SourceRange,
        /**
         * NodeId for the DOM node in whose context custom property declarations for registered properties should be
         * validated. If omitted, declarations in the new rule text can only be validated statically, which may produce
         * incorrect results if the declaration contains a var() for example.
         */
        public val nodeForPropertySyntaxValidation: Int? = null,
    )

    @Serializable
    public data class AddRuleReturn(
        /**
         * The newly created rule.
         */
        public val rule: CSSRule,
    )

    @Serializable
    public data class CollectClassNamesParameter(
        public val styleSheetId: String,
    )

    @Serializable
    public data class CollectClassNamesReturn(
        /**
         * Class name list.
         */
        public val classNames: List<String>,
    )

    @Serializable
    public data class CreateStyleSheetParameter(
        /**
         * Identifier of the frame where "via-inspector" stylesheet should be created.
         */
        public val frameId: String,
    )

    @Serializable
    public data class CreateStyleSheetReturn(
        /**
         * Identifier of the created "via-inspector" stylesheet.
         */
        public val styleSheetId: String,
    )

    @Serializable
    public data class ForcePseudoStateParameter(
        /**
         * The element id for which to force the pseudo state.
         */
        public val nodeId: Int,
        /**
         * Element pseudo classes to force when computing the element's style.
         */
        public val forcedPseudoClasses: List<String>,
    )

    @Serializable
    public data class GetBackgroundColorsParameter(
        /**
         * Id of the node to get background colors for.
         */
        public val nodeId: Int,
    )

    @Serializable
    public data class GetBackgroundColorsReturn(
        /**
         * The range of background colors behind this element, if it contains any visible text. If no
         * visible text is present, this will be undefined. In the case of a flat background color,
         * this will consist of simply that color. In the case of a gradient, this will consist of each
         * of the color stops. For anything more complicated, this will be an empty array. Images will
         * be ignored (as if the image had failed to load).
         */
        public val backgroundColors: List<String>?,
        /**
         * The computed font size for this node, as a CSS computed value string (e.g. '12px').
         */
        public val computedFontSize: String?,
        /**
         * The computed font weight for this node, as a CSS computed value string (e.g. 'normal' or
         * '100').
         */
        public val computedFontWeight: String?,
    )

    @Serializable
    public data class GetComputedStyleForNodeParameter(
        public val nodeId: Int,
    )

    @Serializable
    public data class GetComputedStyleForNodeReturn(
        /**
         * Computed style for the specified DOM node.
         */
        public val computedStyle: List<CSSComputedStyleProperty>,
    )

    @Serializable
    public data class GetInlineStylesForNodeParameter(
        public val nodeId: Int,
    )

    @Serializable
    public data class GetInlineStylesForNodeReturn(
        /**
         * Inline style for the specified DOM node.
         */
        public val inlineStyle: CSSStyle?,
        /**
         * Attribute-defined element style (e.g. resulting from "width=20 height=100%").
         */
        public val attributesStyle: CSSStyle?,
    )

    @Serializable
    public data class GetMatchedStylesForNodeParameter(
        public val nodeId: Int,
    )

    @Serializable
    public data class GetMatchedStylesForNodeReturn(
        /**
         * Inline style for the specified DOM node.
         */
        public val inlineStyle: CSSStyle?,
        /**
         * Attribute-defined element style (e.g. resulting from "width=20 height=100%").
         */
        public val attributesStyle: CSSStyle?,
        /**
         * CSS rules matching this node, from all applicable stylesheets.
         */
        public val matchedCSSRules: List<RuleMatch>?,
        /**
         * Pseudo style matches for this node.
         */
        public val pseudoElements: List<PseudoElementMatches>?,
        /**
         * A chain of inherited styles (from the immediate node parent up to the DOM tree root).
         */
        public val inherited: List<InheritedStyleEntry>?,
        /**
         * A chain of inherited pseudo element styles (from the immediate node parent up to the DOM tree root).
         */
        public val inheritedPseudoElements: List<InheritedPseudoElementMatches>?,
        /**
         * A list of CSS keyframed animations matching this node.
         */
        public val cssKeyframesRules: List<CSSKeyframesRule>?,
        /**
         * A list of CSS position fallbacks matching this node.
         */
        public val cssPositionFallbackRules: List<CSSPositionFallbackRule>?,
        /**
         * A list of CSS at-property rules matching this node.
         */
        public val cssPropertyRules: List<CSSPropertyRule>?,
        /**
         * A list of CSS property registrations matching this node.
         */
        public val cssPropertyRegistrations: List<CSSPropertyRegistration>?,
        /**
         * A font-palette-values rule matching this node.
         */
        public val cssFontPaletteValuesRule: CSSFontPaletteValuesRule?,
        /**
         * Id of the first parent element that does not have display: contents.
         */
        public val parentLayoutNodeId: Int?,
    )

    @Serializable
    public data class GetMediaQueriesReturn(
        public val medias: List<CSSMedia>,
    )

    @Serializable
    public data class GetPlatformFontsForNodeParameter(
        public val nodeId: Int,
    )

    @Serializable
    public data class GetPlatformFontsForNodeReturn(
        /**
         * Usage statistics for every employed platform font.
         */
        public val fonts: List<PlatformFontUsage>,
    )

    @Serializable
    public data class GetStyleSheetTextParameter(
        public val styleSheetId: String,
    )

    @Serializable
    public data class GetStyleSheetTextReturn(
        /**
         * The stylesheet text.
         */
        public val text: String,
    )

    @Serializable
    public data class GetLayersForNodeParameter(
        public val nodeId: Int,
    )

    @Serializable
    public data class GetLayersForNodeReturn(
        public val rootLayer: CSSLayerData,
    )

    @Serializable
    public data class TrackComputedStyleUpdatesParameter(
        public val propertiesToTrack: List<CSSComputedStyleProperty>,
    )

    @Serializable
    public data class TakeComputedStyleUpdatesReturn(
        /**
         * The list of node Ids that have their tracked computed styles updated.
         */
        public val nodeIds: List<Int>,
    )

    @Serializable
    public data class SetEffectivePropertyValueForNodeParameter(
        /**
         * The element id for which to set property.
         */
        public val nodeId: Int,
        public val propertyName: String,
        public val `value`: String,
    )

    @Serializable
    public data class SetPropertyRulePropertyNameParameter(
        public val styleSheetId: String,
        public val range: SourceRange,
        public val propertyName: String,
    )

    @Serializable
    public data class SetPropertyRulePropertyNameReturn(
        /**
         * The resulting key text after modification.
         */
        public val propertyName: Value,
    )

    @Serializable
    public data class SetKeyframeKeyParameter(
        public val styleSheetId: String,
        public val range: SourceRange,
        public val keyText: String,
    )

    @Serializable
    public data class SetKeyframeKeyReturn(
        /**
         * The resulting key text after modification.
         */
        public val keyText: Value,
    )

    @Serializable
    public data class SetMediaTextParameter(
        public val styleSheetId: String,
        public val range: SourceRange,
        public val text: String,
    )

    @Serializable
    public data class SetMediaTextReturn(
        /**
         * The resulting CSS media rule after modification.
         */
        public val media: CSSMedia,
    )

    @Serializable
    public data class SetContainerQueryTextParameter(
        public val styleSheetId: String,
        public val range: SourceRange,
        public val text: String,
    )

    @Serializable
    public data class SetContainerQueryTextReturn(
        /**
         * The resulting CSS container query rule after modification.
         */
        public val containerQuery: CSSContainerQuery,
    )

    @Serializable
    public data class SetSupportsTextParameter(
        public val styleSheetId: String,
        public val range: SourceRange,
        public val text: String,
    )

    @Serializable
    public data class SetSupportsTextReturn(
        /**
         * The resulting CSS Supports rule after modification.
         */
        public val supports: CSSSupports,
    )

    @Serializable
    public data class SetScopeTextParameter(
        public val styleSheetId: String,
        public val range: SourceRange,
        public val text: String,
    )

    @Serializable
    public data class SetScopeTextReturn(
        /**
         * The resulting CSS Scope rule after modification.
         */
        public val scope: CSSScope,
    )

    @Serializable
    public data class SetRuleSelectorParameter(
        public val styleSheetId: String,
        public val range: SourceRange,
        public val selector: String,
    )

    @Serializable
    public data class SetRuleSelectorReturn(
        /**
         * The resulting selector list after modification.
         */
        public val selectorList: SelectorList,
    )

    @Serializable
    public data class SetStyleSheetTextParameter(
        public val styleSheetId: String,
        public val text: String,
    )

    @Serializable
    public data class SetStyleSheetTextReturn(
        /**
         * URL of source map associated with script (if any).
         */
        public val sourceMapURL: String?,
    )

    @Serializable
    public data class SetStyleTextsParameter(
        public val edits: List<StyleDeclarationEdit>,
        /**
         * NodeId for the DOM node in whose context custom property declarations for registered properties should be
         * validated. If omitted, declarations in the new rule text can only be validated statically, which may produce
         * incorrect results if the declaration contains a var() for example.
         */
        public val nodeForPropertySyntaxValidation: Int? = null,
    )

    @Serializable
    public data class SetStyleTextsReturn(
        /**
         * The resulting styles after modification.
         */
        public val styles: List<CSSStyle>,
    )

    @Serializable
    public data class StopRuleUsageTrackingReturn(
        public val ruleUsage: List<RuleUsage>,
    )

    @Serializable
    public data class TakeCoverageDeltaReturn(
        public val coverage: List<RuleUsage>,
        /**
         * Monotonically increasing time, in seconds.
         */
        public val timestamp: Double,
    )

    @Serializable
    public data class SetLocalFontsEnabledParameter(
        /**
         * Whether rendering of local fonts is enabled.
         */
        public val enabled: Boolean,
    )
}
