package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.bluetoothEmulation: BluetoothEmulation
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(BluetoothEmulation(this))

/**
 * This domain allows configuring virtual Bluetooth devices to test
 * the web-bluetooth API.
 */
public class BluetoothEmulation(
    private val cdp: CDP,
) : Domain {
    /**
     * Event for when a GATT operation of |type| to the peripheral with |address|
     * happened.
     */
    public val gattOperationReceived: Flow<GattOperationReceivedParameter> = cdp
        .events
        .filter { it.method == "BluetoothEmulation.gattOperationReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Event for when a characteristic operation of |type| to the characteristic
     * respresented by |characteristicId| happened. |data| and |writeType| is
     * expected to exist when |type| is write.
     */
    public val characteristicOperationReceived: Flow<CharacteristicOperationReceivedParameter> = cdp
        .events
        .filter { it.method == "BluetoothEmulation.characteristicOperationReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Event for when a descriptor operation of |type| to the descriptor
     * respresented by |descriptorId| happened. |data| is expected to exist when
     * |type| is write.
     */
    public val descriptorOperationReceived: Flow<DescriptorOperationReceivedParameter> = cdp
        .events
        .filter { it.method == "BluetoothEmulation.descriptorOperationReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Enable the BluetoothEmulation domain.
     */
    public suspend fun enable(args: EnableParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.enable", parameter, mode)
    }

    /**
     * Enable the BluetoothEmulation domain.
     *
     * @param state State of the simulated central.
     * @param leSupported If the simulated central supports low-energy.
     */
    public suspend fun enable(state: CentralState, leSupported: Boolean) {
        val parameter = EnableParameter(state = state, leSupported = leSupported)
        enable(parameter)
    }

    /**
     * Set the state of the simulated central.
     */
    public suspend fun setSimulatedCentralState(
        args: SetSimulatedCentralStateParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.setSimulatedCentralState", parameter, mode)
    }

    /**
     * Set the state of the simulated central.
     *
     * @param state State of the simulated central.
     */
    public suspend fun setSimulatedCentralState(state: CentralState) {
        val parameter = SetSimulatedCentralStateParameter(state = state)
        setSimulatedCentralState(parameter)
    }

    /**
     * Disable the BluetoothEmulation domain.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("BluetoothEmulation.disable", parameter, mode)
    }

    /**
     * Simulates a peripheral with |address|, |name| and |knownServiceUuids|
     * that has already been connected to the system.
     */
    public suspend fun simulatePreconnectedPeripheral(
        args: SimulatePreconnectedPeripheralParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.simulatePreconnectedPeripheral", parameter, mode)
    }

    /**
     * Simulates a peripheral with |address|, |name| and |knownServiceUuids|
     * that has already been connected to the system.
     *
     * @param address No description
     * @param name No description
     * @param manufacturerData No description
     * @param knownServiceUuids No description
     */
    public suspend fun simulatePreconnectedPeripheral(
        address: String,
        name: String,
        manufacturerData: List<ManufacturerData>,
        knownServiceUuids: List<String>,
    ) {
        val parameter = SimulatePreconnectedPeripheralParameter(
            address = address,
            name = name,
            manufacturerData = manufacturerData,
            knownServiceUuids = knownServiceUuids
        )
        simulatePreconnectedPeripheral(parameter)
    }

    /**
     * Simulates an advertisement packet described in |entry| being received by
     * the central.
     */
    public suspend fun simulateAdvertisement(
        args: SimulateAdvertisementParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.simulateAdvertisement", parameter, mode)
    }

    /**
     * Simulates an advertisement packet described in |entry| being received by
     * the central.
     *
     * @param entry No description
     */
    public suspend fun simulateAdvertisement(entry: ScanEntry) {
        val parameter = SimulateAdvertisementParameter(entry = entry)
        simulateAdvertisement(parameter)
    }

    /**
     * Simulates the response code from the peripheral with |address| for a
     * GATT operation of |type|. The |code| value follows the HCI Error Codes from
     * Bluetooth Core Specification Vol 2 Part D 1.3 List Of Error Codes.
     */
    public suspend fun simulateGATTOperationResponse(
        args: SimulateGATTOperationResponseParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.simulateGATTOperationResponse", parameter, mode)
    }

    /**
     * Simulates the response code from the peripheral with |address| for a
     * GATT operation of |type|. The |code| value follows the HCI Error Codes from
     * Bluetooth Core Specification Vol 2 Part D 1.3 List Of Error Codes.
     *
     * @param address No description
     * @param type No description
     * @param code No description
     */
    public suspend fun simulateGATTOperationResponse(
        address: String,
        type: GATTOperationType,
        code: Int,
    ) {
        val parameter = SimulateGATTOperationResponseParameter(address = address, type = type, code = code)
        simulateGATTOperationResponse(parameter)
    }

    /**
     * Simulates the response from the characteristic with |characteristicId| for a
     * characteristic operation of |type|. The |code| value follows the Error
     * Codes from Bluetooth Core Specification Vol 3 Part F 3.4.1.1 Error Response.
     * The |data| is expected to exist when simulating a successful read operation
     * response.
     */
    public suspend fun simulateCharacteristicOperationResponse(
        args: SimulateCharacteristicOperationResponseParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.simulateCharacteristicOperationResponse", parameter, mode)
    }

    /**
     * Simulates the response from the characteristic with |characteristicId| for a
     * characteristic operation of |type|. The |code| value follows the Error
     * Codes from Bluetooth Core Specification Vol 3 Part F 3.4.1.1 Error Response.
     * The |data| is expected to exist when simulating a successful read operation
     * response.
     *
     * @param characteristicId No description
     * @param type No description
     * @param code No description
     * @param data No description
     */
    public suspend fun simulateCharacteristicOperationResponse(
        characteristicId: String,
        type: CharacteristicOperationType,
        code: Int,
        `data`: String? = null,
    ) {
        val parameter = SimulateCharacteristicOperationResponseParameter(
            characteristicId = characteristicId,
            type = type,
            code = code,
            data = data
        )
        simulateCharacteristicOperationResponse(parameter)
    }

    /**
     * Simulates the response from the descriptor with |descriptorId| for a
     * descriptor operation of |type|. The |code| value follows the Error
     * Codes from Bluetooth Core Specification Vol 3 Part F 3.4.1.1 Error Response.
     * The |data| is expected to exist when simulating a successful read operation
     * response.
     */
    public suspend fun simulateDescriptorOperationResponse(
        args: SimulateDescriptorOperationResponseParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.simulateDescriptorOperationResponse", parameter, mode)
    }

    /**
     * Simulates the response from the descriptor with |descriptorId| for a
     * descriptor operation of |type|. The |code| value follows the Error
     * Codes from Bluetooth Core Specification Vol 3 Part F 3.4.1.1 Error Response.
     * The |data| is expected to exist when simulating a successful read operation
     * response.
     *
     * @param descriptorId No description
     * @param type No description
     * @param code No description
     * @param data No description
     */
    public suspend fun simulateDescriptorOperationResponse(
        descriptorId: String,
        type: DescriptorOperationType,
        code: Int,
        `data`: String? = null,
    ) {
        val parameter = SimulateDescriptorOperationResponseParameter(
            descriptorId = descriptorId,
            type = type,
            code = code,
            data = data
        )
        simulateDescriptorOperationResponse(parameter)
    }

    /**
     * Adds a service with |serviceUuid| to the peripheral with |address|.
     */
    public suspend fun addService(
        args: AddServiceParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): AddServiceReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("BluetoothEmulation.addService", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Adds a service with |serviceUuid| to the peripheral with |address|.
     *
     * @param address No description
     * @param serviceUuid No description
     */
    public suspend fun addService(address: String, serviceUuid: String): AddServiceReturn {
        val parameter = AddServiceParameter(address = address, serviceUuid = serviceUuid)
        return addService(parameter)
    }

    /**
     * Removes the service respresented by |serviceId| from the simulated central.
     */
    public suspend fun removeService(args: RemoveServiceParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.removeService", parameter, mode)
    }

    /**
     * Removes the service respresented by |serviceId| from the simulated central.
     *
     * @param serviceId No description
     */
    public suspend fun removeService(serviceId: String) {
        val parameter = RemoveServiceParameter(serviceId = serviceId)
        removeService(parameter)
    }

    /**
     * Adds a characteristic with |characteristicUuid| and |properties| to the
     * service represented by |serviceId|.
     */
    public suspend fun addCharacteristic(
        args: AddCharacteristicParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): AddCharacteristicReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("BluetoothEmulation.addCharacteristic", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Adds a characteristic with |characteristicUuid| and |properties| to the
     * service represented by |serviceId|.
     *
     * @param serviceId No description
     * @param characteristicUuid No description
     * @param properties No description
     */
    public suspend fun addCharacteristic(
        serviceId: String,
        characteristicUuid: String,
        properties: CharacteristicProperties,
    ): AddCharacteristicReturn {
        val parameter = AddCharacteristicParameter(
            serviceId = serviceId,
            characteristicUuid = characteristicUuid,
            properties = properties
        )
        return addCharacteristic(parameter)
    }

    /**
     * Removes the characteristic respresented by |characteristicId| from the
     * simulated central.
     */
    public suspend fun removeCharacteristic(
        args: RemoveCharacteristicParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.removeCharacteristic", parameter, mode)
    }

    /**
     * Removes the characteristic respresented by |characteristicId| from the
     * simulated central.
     *
     * @param characteristicId No description
     */
    public suspend fun removeCharacteristic(characteristicId: String) {
        val parameter = RemoveCharacteristicParameter(characteristicId = characteristicId)
        removeCharacteristic(parameter)
    }

    /**
     * Adds a descriptor with |descriptorUuid| to the characteristic respresented
     * by |characteristicId|.
     */
    public suspend fun addDescriptor(
        args: AddDescriptorParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): AddDescriptorReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("BluetoothEmulation.addDescriptor", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Adds a descriptor with |descriptorUuid| to the characteristic respresented
     * by |characteristicId|.
     *
     * @param characteristicId No description
     * @param descriptorUuid No description
     */
    public suspend fun addDescriptor(characteristicId: String, descriptorUuid: String): AddDescriptorReturn {
        val parameter = AddDescriptorParameter(characteristicId = characteristicId, descriptorUuid = descriptorUuid)
        return addDescriptor(parameter)
    }

    /**
     * Removes the descriptor with |descriptorId| from the simulated central.
     */
    public suspend fun removeDescriptor(args: RemoveDescriptorParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.removeDescriptor", parameter, mode)
    }

    /**
     * Removes the descriptor with |descriptorId| from the simulated central.
     *
     * @param descriptorId No description
     */
    public suspend fun removeDescriptor(descriptorId: String) {
        val parameter = RemoveDescriptorParameter(descriptorId = descriptorId)
        removeDescriptor(parameter)
    }

    /**
     * Simulates a GATT disconnection from the peripheral with |address|.
     */
    public suspend fun simulateGATTDisconnection(
        args: SimulateGATTDisconnectionParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BluetoothEmulation.simulateGATTDisconnection", parameter, mode)
    }

    /**
     * Simulates a GATT disconnection from the peripheral with |address|.
     *
     * @param address No description
     */
    public suspend fun simulateGATTDisconnection(address: String) {
        val parameter = SimulateGATTDisconnectionParameter(address = address)
        simulateGATTDisconnection(parameter)
    }

    /**
     * Indicates the various states of Central.
     */
    @Serializable
    public enum class CentralState {
        @SerialName("absent")
        ABSENT,

        @SerialName("powered-off")
        POWERED_OFF,

        @SerialName("powered-on")
        POWERED_ON,
    }

    /**
     * Indicates the various types of GATT event.
     */
    @Serializable
    public enum class GATTOperationType {
        @SerialName("connection")
        CONNECTION,

        @SerialName("discovery")
        DISCOVERY,
    }

    /**
     * Indicates the various types of characteristic write.
     */
    @Serializable
    public enum class CharacteristicWriteType {
        @SerialName("write-default-deprecated")
        WRITE_DEFAULT_DEPRECATED,

        @SerialName("write-with-response")
        WRITE_WITH_RESPONSE,

        @SerialName("write-without-response")
        WRITE_WITHOUT_RESPONSE,
    }

    /**
     * Indicates the various types of characteristic operation.
     */
    @Serializable
    public enum class CharacteristicOperationType {
        @SerialName("read")
        READ,

        @SerialName("write")
        WRITE,

        @SerialName("subscribe-to-notifications")
        SUBSCRIBE_TO_NOTIFICATIONS,

        @SerialName("unsubscribe-from-notifications")
        UNSUBSCRIBE_FROM_NOTIFICATIONS,
    }

    /**
     * Indicates the various types of descriptor operation.
     */
    @Serializable
    public enum class DescriptorOperationType {
        @SerialName("read")
        READ,

        @SerialName("write")
        WRITE,
    }

    /**
     * Stores the manufacturer data
     */
    @Serializable
    public data class ManufacturerData(
        /**
         * Company identifier
         * https://bitbucket.org/bluetooth-SIG/public/src/main/assigned_numbers/company_identifiers/company_identifiers.yaml
         * https://usb.org/developers
         */
        public val key: Int,
        /**
         * Manufacturer-specific data (Encoded as a base64 string when passed over JSON)
         */
        public val `data`: String,
    )

    /**
     * Stores the byte data of the advertisement packet sent by a Bluetooth device.
     */
    @Serializable
    public data class ScanRecord(
        public val name: String? = null,
        public val uuids: List<String>? = null,
        /**
         * Stores the external appearance description of the device.
         */
        public val appearance: Int? = null,
        /**
         * Stores the transmission power of a broadcasting device.
         */
        public val txPower: Int? = null,
        /**
         * Key is the company identifier and the value is an array of bytes of
         * manufacturer specific data.
         */
        public val manufacturerData: List<ManufacturerData>? = null,
    )

    /**
     * Stores the advertisement packet information that is sent by a Bluetooth device.
     */
    @Serializable
    public data class ScanEntry(
        public val deviceAddress: String,
        public val rssi: Int,
        public val scanRecord: ScanRecord,
    )

    /**
     * Describes the properties of a characteristic. This follows Bluetooth Core
     * Specification BT 4.2 Vol 3 Part G 3.3.1. Characteristic Properties.
     */
    @Serializable
    public data class CharacteristicProperties(
        public val broadcast: Boolean? = null,
        public val read: Boolean? = null,
        public val writeWithoutResponse: Boolean? = null,
        public val write: Boolean? = null,
        public val notify: Boolean? = null,
        public val indicate: Boolean? = null,
        public val authenticatedSignedWrites: Boolean? = null,
        public val extendedProperties: Boolean? = null,
    )

    /**
     * Event for when a GATT operation of |type| to the peripheral with |address|
     * happened.
     */
    @Serializable
    public data class GattOperationReceivedParameter(
        public val address: String,
        public val type: GATTOperationType,
    )

    /**
     * Event for when a characteristic operation of |type| to the characteristic
     * respresented by |characteristicId| happened. |data| and |writeType| is
     * expected to exist when |type| is write.
     */
    @Serializable
    public data class CharacteristicOperationReceivedParameter(
        public val characteristicId: String,
        public val type: CharacteristicOperationType,
        public val `data`: String? = null,
        public val writeType: CharacteristicWriteType? = null,
    )

    /**
     * Event for when a descriptor operation of |type| to the descriptor
     * respresented by |descriptorId| happened. |data| is expected to exist when
     * |type| is write.
     */
    @Serializable
    public data class DescriptorOperationReceivedParameter(
        public val descriptorId: String,
        public val type: DescriptorOperationType,
        public val `data`: String? = null,
    )

    @Serializable
    public data class EnableParameter(
        /**
         * State of the simulated central.
         */
        public val state: CentralState,
        /**
         * If the simulated central supports low-energy.
         */
        public val leSupported: Boolean,
    )

    @Serializable
    public data class SetSimulatedCentralStateParameter(
        /**
         * State of the simulated central.
         */
        public val state: CentralState,
    )

    @Serializable
    public data class SimulatePreconnectedPeripheralParameter(
        public val address: String,
        public val name: String,
        public val manufacturerData: List<ManufacturerData>,
        public val knownServiceUuids: List<String>,
    )

    @Serializable
    public data class SimulateAdvertisementParameter(
        public val entry: ScanEntry,
    )

    @Serializable
    public data class SimulateGATTOperationResponseParameter(
        public val address: String,
        public val type: GATTOperationType,
        public val code: Int,
    )

    @Serializable
    public data class SimulateCharacteristicOperationResponseParameter(
        public val characteristicId: String,
        public val type: CharacteristicOperationType,
        public val code: Int,
        public val `data`: String? = null,
    )

    @Serializable
    public data class SimulateDescriptorOperationResponseParameter(
        public val descriptorId: String,
        public val type: DescriptorOperationType,
        public val code: Int,
        public val `data`: String? = null,
    )

    @Serializable
    public data class AddServiceParameter(
        public val address: String,
        public val serviceUuid: String,
    )

    @Serializable
    public data class AddServiceReturn(
        /**
         * An identifier that uniquely represents this service.
         */
        public val serviceId: String,
    )

    @Serializable
    public data class RemoveServiceParameter(
        public val serviceId: String,
    )

    @Serializable
    public data class AddCharacteristicParameter(
        public val serviceId: String,
        public val characteristicUuid: String,
        public val properties: CharacteristicProperties,
    )

    @Serializable
    public data class AddCharacteristicReturn(
        /**
         * An identifier that uniquely represents this characteristic.
         */
        public val characteristicId: String,
    )

    @Serializable
    public data class RemoveCharacteristicParameter(
        public val characteristicId: String,
    )

    @Serializable
    public data class AddDescriptorParameter(
        public val characteristicId: String,
        public val descriptorUuid: String,
    )

    @Serializable
    public data class AddDescriptorReturn(
        /**
         * An identifier that uniquely represents this descriptor.
         */
        public val descriptorId: String,
    )

    @Serializable
    public data class RemoveDescriptorParameter(
        public val descriptorId: String,
    )

    @Serializable
    public data class SimulateGATTDisconnectionParameter(
        public val address: String,
    )
}
