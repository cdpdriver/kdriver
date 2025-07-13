package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement

public val CDP.inspector: Inspector
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Inspector(this))

public class Inspector(
    private val cdp: CDP,
) : Domain {
    /**
     * Fired when remote debugging connection is about to be terminated. Contains detach reason.
     */
    public val detached: Flow<DetachedParameter> = cdp
        .events
        .filter { it.method == "Inspector.detached" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when debugging target has crashed
     */
    public val targetCrashed: Flow<Unit> = cdp
        .events
        .filter { it.method == "Inspector.targetCrashed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when debugging target has reloaded after crash
     */
    public val targetReloadedAfterCrash: Flow<Unit> = cdp
        .events
        .filter { it.method == "Inspector.targetReloadedAfterCrash" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Disables inspector domain notifications.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Inspector.disable", parameter, mode)
    }

    /**
     * Enables inspector domain notifications.
     */
    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Inspector.enable", parameter, mode)
    }

    /**
     * Fired when remote debugging connection is about to be terminated. Contains detach reason.
     */
    @Serializable
    public data class DetachedParameter(
        /**
         * The reason why connection has been terminated.
         */
        public val reason: String,
    )
}
