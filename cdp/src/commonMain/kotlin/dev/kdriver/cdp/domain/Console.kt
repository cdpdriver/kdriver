@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kdriver.cdp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * This domain is deprecated - use Runtime or Log instead.
 */
public val CDP.console: Console
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Console(this))

/**
 * This domain is deprecated - use Runtime or Log instead.
 */
public class Console(
    private val cdp: CDP,
) : Domain {
    /**
     * Issued when new console message is added.
     */
    public val messageAdded: Flow<MessageAddedParameter> = cdp
        .events
        .filter { it.method == "Console.messageAdded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Does nothing.
     */
    public suspend fun clearMessages(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Console.clearMessages", parameter, mode)
    }

    /**
     * Disables console domain, prevents further console messages from being reported to the client.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Console.disable", parameter, mode)
    }

    /**
     * Enables console domain, sends the messages collected so far to the client by means of the
     * `messageAdded` notification.
     */
    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Console.enable", parameter, mode)
    }

    /**
     * Console message.
     */
    @Serializable
    public data class ConsoleMessage(
        /**
         * Message source.
         */
        public val source: String,
        /**
         * Message severity.
         */
        public val level: String,
        /**
         * Message text.
         */
        public val text: String,
        /**
         * URL of the message origin.
         */
        public val url: String? = null,
        /**
         * Line number in the resource that generated this message (1-based).
         */
        public val line: Int? = null,
        /**
         * Column number in the resource that generated this message (1-based).
         */
        public val column: Int? = null,
    )

    /**
     * Issued when new console message is added.
     */
    @Serializable
    public data class MessageAddedParameter(
        /**
         * Console message that has been added.
         */
        public val message: ConsoleMessage,
    )
}
