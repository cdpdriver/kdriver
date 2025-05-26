package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement

public val CDP.schema: Schema
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Schema(this))

/**
 * This domain is deprecated.
 */
public class Schema(
    private val cdp: CDP,
) : Domain {
    /**
     * Returns supported domains.
     */
    public suspend fun getDomains(): GetDomainsReturn {
        val parameter = null
        val result = cdp.callCommand("Schema.getDomains", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Description of the protocol domain.
     */
    @Serializable
    public data class Domain(
        /**
         * Domain name.
         */
        public val name: String,
        /**
         * Domain version.
         */
        public val version: String,
    )

    @Serializable
    public data class GetDomainsReturn(
        /**
         * List of supported domains.
         */
        public val domains: List<Domain>,
    )
}
