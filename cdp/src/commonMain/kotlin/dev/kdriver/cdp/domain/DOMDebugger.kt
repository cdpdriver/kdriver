package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlin.Boolean
import kotlin.Deprecated
import kotlin.Int
import kotlin.String
import kotlin.collections.List
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.dOMDebugger: DOMDebugger
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(DOMDebugger(this))

/**
 * DOM debugging allows setting breakpoints on particular DOM operations and events. JavaScript
 * execution will stop on these operations as if there was a regular breakpoint set.
 */
public class DOMDebugger(
    private val cdp: CDP,
) : Domain {
    /**
     * Returns event listeners of the given object.
     */
    public suspend fun getEventListeners(args: GetEventListenersParameter): GetEventListenersReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("DOMDebugger.getEventListeners", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns event listeners of the given object.
     *
     * @param objectId Identifier of the object to return listeners for.
     * @param depth The maximum depth at which Node children should be retrieved, defaults to 1. Use -1 for the
     * entire subtree or provide an integer larger than 0.
     * @param pierce Whether or not iframes and shadow roots should be traversed when returning the subtree
     * (default is false). Reports listeners for all contexts if pierce is enabled.
     */
    public suspend fun getEventListeners(
        objectId: String,
        depth: Int? = null,
        pierce: Boolean? = null,
    ): GetEventListenersReturn {
        val parameter = GetEventListenersParameter(objectId = objectId, depth = depth, pierce = pierce)
        return getEventListeners(parameter)
    }

    /**
     * Removes DOM breakpoint that was set using `setDOMBreakpoint`.
     */
    public suspend fun removeDOMBreakpoint(args: RemoveDOMBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMDebugger.removeDOMBreakpoint", parameter)
    }

    /**
     * Removes DOM breakpoint that was set using `setDOMBreakpoint`.
     *
     * @param nodeId Identifier of the node to remove breakpoint from.
     * @param type Type of the breakpoint to remove.
     */
    public suspend fun removeDOMBreakpoint(nodeId: Int, type: DOMBreakpointType) {
        val parameter = RemoveDOMBreakpointParameter(nodeId = nodeId, type = type)
        removeDOMBreakpoint(parameter)
    }

    /**
     * Removes breakpoint on particular DOM event.
     */
    public suspend fun removeEventListenerBreakpoint(args: RemoveEventListenerBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMDebugger.removeEventListenerBreakpoint", parameter)
    }

    /**
     * Removes breakpoint on particular DOM event.
     *
     * @param eventName Event name.
     * @param targetName EventTarget interface name.
     */
    public suspend fun removeEventListenerBreakpoint(eventName: String, targetName: String? = null) {
        val parameter = RemoveEventListenerBreakpointParameter(eventName = eventName, targetName = targetName)
        removeEventListenerBreakpoint(parameter)
    }

    /**
     * Removes breakpoint on particular native event.
     */
    @Deprecated(message = "")
    public suspend fun removeInstrumentationBreakpoint(args: RemoveInstrumentationBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMDebugger.removeInstrumentationBreakpoint", parameter)
    }

    /**
     * Removes breakpoint on particular native event.
     *
     * @param eventName Instrumentation name to stop on.
     */
    @Deprecated(message = "")
    public suspend fun removeInstrumentationBreakpoint(eventName: String) {
        val parameter = RemoveInstrumentationBreakpointParameter(eventName = eventName)
        removeInstrumentationBreakpoint(parameter)
    }

    /**
     * Removes breakpoint from XMLHttpRequest.
     */
    public suspend fun removeXHRBreakpoint(args: RemoveXHRBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMDebugger.removeXHRBreakpoint", parameter)
    }

    /**
     * Removes breakpoint from XMLHttpRequest.
     *
     * @param url Resource URL substring.
     */
    public suspend fun removeXHRBreakpoint(url: String) {
        val parameter = RemoveXHRBreakpointParameter(url = url)
        removeXHRBreakpoint(parameter)
    }

    /**
     * Sets breakpoint on particular CSP violations.
     */
    public suspend fun setBreakOnCSPViolation(args: SetBreakOnCSPViolationParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMDebugger.setBreakOnCSPViolation", parameter)
    }

    /**
     * Sets breakpoint on particular CSP violations.
     *
     * @param violationTypes CSP Violations to stop upon.
     */
    public suspend fun setBreakOnCSPViolation(violationTypes: List<CSPViolationType>) {
        val parameter = SetBreakOnCSPViolationParameter(violationTypes = violationTypes)
        setBreakOnCSPViolation(parameter)
    }

    /**
     * Sets breakpoint on particular operation with DOM.
     */
    public suspend fun setDOMBreakpoint(args: SetDOMBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMDebugger.setDOMBreakpoint", parameter)
    }

    /**
     * Sets breakpoint on particular operation with DOM.
     *
     * @param nodeId Identifier of the node to set breakpoint on.
     * @param type Type of the operation to stop upon.
     */
    public suspend fun setDOMBreakpoint(nodeId: Int, type: DOMBreakpointType) {
        val parameter = SetDOMBreakpointParameter(nodeId = nodeId, type = type)
        setDOMBreakpoint(parameter)
    }

    /**
     * Sets breakpoint on particular DOM event.
     */
    public suspend fun setEventListenerBreakpoint(args: SetEventListenerBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMDebugger.setEventListenerBreakpoint", parameter)
    }

    /**
     * Sets breakpoint on particular DOM event.
     *
     * @param eventName DOM Event name to stop on (any DOM event will do).
     * @param targetName EventTarget interface name to stop on. If equal to `"*"` or not provided, will stop on any
     * EventTarget.
     */
    public suspend fun setEventListenerBreakpoint(eventName: String, targetName: String? = null) {
        val parameter = SetEventListenerBreakpointParameter(eventName = eventName, targetName = targetName)
        setEventListenerBreakpoint(parameter)
    }

    /**
     * Sets breakpoint on particular native event.
     */
    @Deprecated(message = "")
    public suspend fun setInstrumentationBreakpoint(args: SetInstrumentationBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMDebugger.setInstrumentationBreakpoint", parameter)
    }

    /**
     * Sets breakpoint on particular native event.
     *
     * @param eventName Instrumentation name to stop on.
     */
    @Deprecated(message = "")
    public suspend fun setInstrumentationBreakpoint(eventName: String) {
        val parameter = SetInstrumentationBreakpointParameter(eventName = eventName)
        setInstrumentationBreakpoint(parameter)
    }

    /**
     * Sets breakpoint on XMLHttpRequest.
     */
    public suspend fun setXHRBreakpoint(args: SetXHRBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DOMDebugger.setXHRBreakpoint", parameter)
    }

    /**
     * Sets breakpoint on XMLHttpRequest.
     *
     * @param url Resource URL substring. All XHRs having this substring in the URL will get stopped upon.
     */
    public suspend fun setXHRBreakpoint(url: String) {
        val parameter = SetXHRBreakpointParameter(url = url)
        setXHRBreakpoint(parameter)
    }

    /**
     * DOM breakpoint type.
     */
    @Serializable
    public enum class DOMBreakpointType {
        @SerialName("subtree-modified")
        SUBTREE_MODIFIED,
        @SerialName("attribute-modified")
        ATTRIBUTE_MODIFIED,
        @SerialName("node-removed")
        NODE_REMOVED,
    }

    /**
     * CSP Violation type.
     */
    @Serializable
    public enum class CSPViolationType {
        @SerialName("trustedtype-sink-violation")
        TRUSTEDTYPE_SINK_VIOLATION,
        @SerialName("trustedtype-policy-violation")
        TRUSTEDTYPE_POLICY_VIOLATION,
    }

    /**
     * Object event listener.
     */
    @Serializable
    public data class EventListener(
        /**
         * `EventListener`'s type.
         */
        public val type: String,
        /**
         * `EventListener`'s useCapture.
         */
        public val useCapture: Boolean,
        /**
         * `EventListener`'s passive flag.
         */
        public val passive: Boolean,
        /**
         * `EventListener`'s once flag.
         */
        public val once: Boolean,
        /**
         * Script id of the handler code.
         */
        public val scriptId: String,
        /**
         * Line number in the script (0-based).
         */
        public val lineNumber: Int,
        /**
         * Column number in the script (0-based).
         */
        public val columnNumber: Int,
        /**
         * Event handler function value.
         */
        public val handler: Runtime.RemoteObject? = null,
        /**
         * Event original handler function value.
         */
        public val originalHandler: Runtime.RemoteObject? = null,
        /**
         * Node the listener is added to (if any).
         */
        public val backendNodeId: Int? = null,
    )

    @Serializable
    public data class GetEventListenersParameter(
        /**
         * Identifier of the object to return listeners for.
         */
        public val objectId: String,
        /**
         * The maximum depth at which Node children should be retrieved, defaults to 1. Use -1 for the
         * entire subtree or provide an integer larger than 0.
         */
        public val depth: Int? = null,
        /**
         * Whether or not iframes and shadow roots should be traversed when returning the subtree
         * (default is false). Reports listeners for all contexts if pierce is enabled.
         */
        public val pierce: Boolean? = null,
    )

    @Serializable
    public data class GetEventListenersReturn(
        /**
         * Array of relevant listeners.
         */
        public val listeners: List<EventListener>,
    )

    @Serializable
    public data class RemoveDOMBreakpointParameter(
        /**
         * Identifier of the node to remove breakpoint from.
         */
        public val nodeId: Int,
        /**
         * Type of the breakpoint to remove.
         */
        public val type: DOMBreakpointType,
    )

    @Serializable
    public data class RemoveEventListenerBreakpointParameter(
        /**
         * Event name.
         */
        public val eventName: String,
        /**
         * EventTarget interface name.
         */
        public val targetName: String? = null,
    )

    @Serializable
    public data class RemoveInstrumentationBreakpointParameter(
        /**
         * Instrumentation name to stop on.
         */
        public val eventName: String,
    )

    @Serializable
    public data class RemoveXHRBreakpointParameter(
        /**
         * Resource URL substring.
         */
        public val url: String,
    )

    @Serializable
    public data class SetBreakOnCSPViolationParameter(
        /**
         * CSP Violations to stop upon.
         */
        public val violationTypes: List<CSPViolationType>,
    )

    @Serializable
    public data class SetDOMBreakpointParameter(
        /**
         * Identifier of the node to set breakpoint on.
         */
        public val nodeId: Int,
        /**
         * Type of the operation to stop upon.
         */
        public val type: DOMBreakpointType,
    )

    @Serializable
    public data class SetEventListenerBreakpointParameter(
        /**
         * DOM Event name to stop on (any DOM event will do).
         */
        public val eventName: String,
        /**
         * EventTarget interface name to stop on. If equal to `"*"` or not provided, will stop on any
         * EventTarget.
         */
        public val targetName: String? = null,
    )

    @Serializable
    public data class SetInstrumentationBreakpointParameter(
        /**
         * Instrumentation name to stop on.
         */
        public val eventName: String,
    )

    @Serializable
    public data class SetXHRBreakpointParameter(
        /**
         * Resource URL substring. All XHRs having this substring in the URL will get stopped upon.
         */
        public val url: String,
    )
}
