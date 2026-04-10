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

public val CDP.webMCP: WebMCP
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(WebMCP(this))

public class WebMCP(
    private val cdp: CDP,
) : Domain {
    /**
     * Event fired when new tools are added.
     */
    public val toolsAdded: Flow<ToolsAddedParameter> = cdp
        .events
        .filter { it.method == "WebMCP.toolsAdded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Event fired when tools are removed.
     */
    public val toolsRemoved: Flow<ToolsRemovedParameter> = cdp
        .events
        .filter { it.method == "WebMCP.toolsRemoved" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Event fired when a tool invocation starts.
     */
    public val toolInvoked: Flow<ToolInvokedParameter> = cdp
        .events
        .filter { it.method == "WebMCP.toolInvoked" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Event fired when a tool invocation completes or fails.
     */
    public val toolResponded: Flow<ToolRespondedParameter> = cdp
        .events
        .filter { it.method == "WebMCP.toolResponded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Enables the WebMCP domain, allowing events to be sent. Enabling the domain will trigger a toolsAdded event for
     * all currently registered tools.
     */
    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("WebMCP.enable", parameter, mode)
    }

    /**
     * Disables the WebMCP domain.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("WebMCP.disable", parameter, mode)
    }

    /**
     * Tool annotations
     */
    @Serializable
    public data class Annotation(
        /**
         * A hint indicating that the tool does not modify any state.
         */
        public val readOnly: Boolean? = null,
        /**
         * If the declarative tool was declared with the autosubmit attribute.
         */
        public val autosubmit: Boolean? = null,
    )

    /**
     * Represents the status of a tool invocation.
     */
    @Serializable
    public enum class InvocationStatus {
        @SerialName("Success")
        SUCCESS,

        @SerialName("Canceled")
        CANCELED,

        @SerialName("Error")
        ERROR,
    }

    /**
     * Definition of a tool that can be invoked.
     */
    @Serializable
    public data class Tool(
        /**
         * Tool name.
         */
        public val name: String,
        /**
         * Tool description.
         */
        public val description: String,
        /**
         * Schema for the tool's input parameters.
         */
        public val inputSchema: Map<String, JsonElement>? = null,
        /**
         * Optional annotations for the tool.
         */
        public val annotations: Annotation? = null,
        /**
         * Frame identifier associated with the tool registration.
         */
        public val frameId: String,
        /**
         * Optional node ID for declarative tools.
         */
        public val backendNodeId: Int? = null,
        /**
         * The stack trace at the time of the registration.
         */
        public val stackTrace: Runtime.StackTrace? = null,
    )

    /**
     * Event fired when new tools are added.
     */
    @Serializable
    public data class ToolsAddedParameter(
        /**
         * Array of tools that were added.
         */
        public val tools: List<Tool>,
    )

    /**
     * Event fired when tools are removed.
     */
    @Serializable
    public data class ToolsRemovedParameter(
        /**
         * Array of tools that were removed.
         */
        public val tools: List<Tool>,
    )

    /**
     * Event fired when a tool invocation starts.
     */
    @Serializable
    public data class ToolInvokedParameter(
        /**
         * Name of the tool to invoke.
         */
        public val toolName: String,
        /**
         * Frame id
         */
        public val frameId: String,
        /**
         * Invocation identifier.
         */
        public val invocationId: String,
        /**
         * The input parameters used for the invocation.
         */
        public val input: String,
    )

    /**
     * Event fired when a tool invocation completes or fails.
     */
    @Serializable
    public data class ToolRespondedParameter(
        /**
         * Invocation identifier.
         */
        public val invocationId: String,
        /**
         * Status of the invocation.
         */
        public val status: InvocationStatus,
        /**
         * Output or error delivered as delivered to the agent. Missing if `status` is anything other than Success.
         */
        public val output: JsonElement? = null,
        /**
         * Error text for protocol users.
         */
        public val errorText: String? = null,
        /**
         * The exception object, if the javascript tool threw an error>
         */
        public val exception: Runtime.RemoteObject? = null,
    )
}
