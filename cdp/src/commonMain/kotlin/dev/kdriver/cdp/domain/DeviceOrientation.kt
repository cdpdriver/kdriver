package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlin.Double
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.deviceOrientation: DeviceOrientation
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(DeviceOrientation(this))

public class DeviceOrientation(
    private val cdp: CDP,
) : Domain {
    /**
     * Clears the overridden Device Orientation.
     */
    public suspend fun clearDeviceOrientationOverride() {
        val parameter = null
        cdp.callCommand("DeviceOrientation.clearDeviceOrientationOverride", parameter)
    }

    /**
     * Overrides the Device Orientation.
     */
    public suspend fun setDeviceOrientationOverride(args: SetDeviceOrientationOverrideParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DeviceOrientation.setDeviceOrientationOverride", parameter)
    }

    /**
     * Overrides the Device Orientation.
     *
     * @param alpha Mock alpha
     * @param beta Mock beta
     * @param gamma Mock gamma
     */
    public suspend fun setDeviceOrientationOverride(
        alpha: Double,
        beta: Double,
        gamma: Double,
    ) {
        val parameter = SetDeviceOrientationOverrideParameter(alpha = alpha, beta = beta, gamma = gamma)
        setDeviceOrientationOverride(parameter)
    }

    @Serializable
    public data class SetDeviceOrientationOverrideParameter(
        /**
         * Mock alpha
         */
        public val alpha: Double,
        /**
         * Mock beta
         */
        public val beta: Double,
        /**
         * Mock gamma
         */
        public val gamma: Double,
    )
}
