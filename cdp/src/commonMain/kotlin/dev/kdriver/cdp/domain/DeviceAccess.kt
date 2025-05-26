package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.deviceAccess: DeviceAccess
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(DeviceAccess(this))

public class DeviceAccess(
    private val cdp: CDP,
) : Domain {
    public val deviceRequestPrompted: Flow<DeviceRequestPromptedParameter> = cdp
        .events
        .filter {
            it.method == "DeviceAccess.deviceRequestPrompted"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    /**
     * Enable events in this domain.
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("DeviceAccess.enable", parameter)
    }

    /**
     * Disable events in this domain.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("DeviceAccess.disable", parameter)
    }

    /**
     * Select a device in response to a DeviceAccess.deviceRequestPrompted event.
     */
    public suspend fun selectPrompt(args: SelectPromptParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DeviceAccess.selectPrompt", parameter)
    }

    /**
     * Select a device in response to a DeviceAccess.deviceRequestPrompted event.
     */
    public suspend fun selectPrompt(id: String, deviceId: String) {
        val parameter = SelectPromptParameter(id = id, deviceId = deviceId)
        selectPrompt(parameter)
    }

    /**
     * Cancel a prompt in response to a DeviceAccess.deviceRequestPrompted event.
     */
    public suspend fun cancelPrompt(args: CancelPromptParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("DeviceAccess.cancelPrompt", parameter)
    }

    /**
     * Cancel a prompt in response to a DeviceAccess.deviceRequestPrompted event.
     */
    public suspend fun cancelPrompt(id: String) {
        val parameter = CancelPromptParameter(id = id)
        cancelPrompt(parameter)
    }

    /**
     * Device information displayed in a user prompt to select a device.
     */
    @Serializable
    public data class PromptDevice(
        public val id: String,
        /**
         * Display name as it appears in a device request user prompt.
         */
        public val name: String,
    )

    /**
     * A device request opened a user prompt to select a device. Respond with the
     * selectPrompt or cancelPrompt command.
     */
    @Serializable
    public data class DeviceRequestPromptedParameter(
        public val id: String,
        public val devices: List<PromptDevice>,
    )

    @Serializable
    public data class SelectPromptParameter(
        public val id: String,
        public val deviceId: String,
    )

    @Serializable
    public data class CancelPromptParameter(
        public val id: String,
    )
}
