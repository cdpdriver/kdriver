package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.tethering: Tethering
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Tethering(this))

/**
 * The Tethering domain defines methods and events for browser port binding.
 */
public class Tethering(
    private val cdp: CDP,
) : Domain {
    /**
     * Informs that port was successfully bound and got a specified connection id.
     */
    public val accepted: Flow<AcceptedParameter> = cdp
        .events
        .filter { it.method == "Tethering.accepted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Request browser port binding.
     */
    public suspend fun bind(args: BindParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Tethering.bind", parameter, mode)
    }

    /**
     * Request browser port binding.
     *
     * @param port Port number to bind.
     */
    public suspend fun bind(port: Int) {
        val parameter = BindParameter(port = port)
        bind(parameter)
    }

    /**
     * Request browser port unbinding.
     */
    public suspend fun unbind(args: UnbindParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Tethering.unbind", parameter, mode)
    }

    /**
     * Request browser port unbinding.
     *
     * @param port Port number to unbind.
     */
    public suspend fun unbind(port: Int) {
        val parameter = UnbindParameter(port = port)
        unbind(parameter)
    }

    /**
     * Informs that port was successfully bound and got a specified connection id.
     */
    @Serializable
    public data class AcceptedParameter(
        /**
         * Port number that was successfully bound.
         */
        public val port: Int,
        /**
         * Connection id to be used.
         */
        public val connectionId: String,
    )

    @Serializable
    public data class BindParameter(
        /**
         * Port number to bind.
         */
        public val port: Int,
    )

    @Serializable
    public data class UnbindParameter(
        /**
         * Port number to unbind.
         */
        public val port: Int,
    )
}
