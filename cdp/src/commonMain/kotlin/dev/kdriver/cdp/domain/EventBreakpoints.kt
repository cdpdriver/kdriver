package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.eventBreakpoints: EventBreakpoints
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(EventBreakpoints(this))

/**
 * EventBreakpoints permits setting JavaScript breakpoints on operations and events
 * occurring in native code invoked from JavaScript. Once breakpoint is hit, it is
 * reported through Debugger domain, similarly to regular breakpoints being hit.
 */
public class EventBreakpoints(
    private val cdp: CDP,
) : Domain {
    /**
     * Sets breakpoint on particular native event.
     */
    public suspend fun setInstrumentationBreakpoint(args: SetInstrumentationBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("EventBreakpoints.setInstrumentationBreakpoint", parameter)
    }

    /**
     * Sets breakpoint on particular native event.
     */
    public suspend fun setInstrumentationBreakpoint(eventName: String) {
        val parameter = SetInstrumentationBreakpointParameter(eventName = eventName)
        setInstrumentationBreakpoint(parameter)
    }

    /**
     * Removes breakpoint on particular native event.
     */
    public suspend fun removeInstrumentationBreakpoint(args: RemoveInstrumentationBreakpointParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("EventBreakpoints.removeInstrumentationBreakpoint", parameter)
    }

    /**
     * Removes breakpoint on particular native event.
     */
    public suspend fun removeInstrumentationBreakpoint(eventName: String) {
        val parameter = RemoveInstrumentationBreakpointParameter(eventName = eventName)
        removeInstrumentationBreakpoint(parameter)
    }

    /**
     * Removes all breakpoints
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("EventBreakpoints.disable", parameter)
    }

    @Serializable
    public data class SetInstrumentationBreakpointParameter(
        /**
         * Instrumentation name to stop on.
         */
        public val eventName: String,
    )

    @Serializable
    public data class RemoveInstrumentationBreakpointParameter(
        /**
         * Instrumentation name to stop on.
         */
        public val eventName: String,
    )
}
