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

public val CDP.autofill: Autofill
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Autofill(this))

/**
 * Defines commands and events for Autofill.
 */
public class Autofill(
    private val cdp: CDP,
) : Domain {
    /**
     * Emitted when an address form is filled.
     */
    public val addressFormFilled: Flow<AddressFormFilledParameter> = cdp
        .events
        .filter { it.method == "Autofill.addressFormFilled" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Trigger autofill on a form identified by the fieldId.
     * If the field and related form cannot be autofilled, returns an error.
     */
    public suspend fun trigger(args: TriggerParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Autofill.trigger", parameter, mode)
    }

    /**
     * Trigger autofill on a form identified by the fieldId.
     * If the field and related form cannot be autofilled, returns an error.
     *
     * @param fieldId Identifies a field that serves as an anchor for autofill.
     * @param frameId Identifies the frame that field belongs to.
     * @param card Credit card information to fill out the form. Credit card data is not saved.
     */
    public suspend fun trigger(
        fieldId: Int,
        frameId: String? = null,
        card: CreditCard,
    ) {
        val parameter = TriggerParameter(fieldId = fieldId, frameId = frameId, card = card)
        trigger(parameter)
    }

    /**
     * Set addresses so that developers can verify their forms implementation.
     */
    public suspend fun setAddresses(args: SetAddressesParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Autofill.setAddresses", parameter, mode)
    }

    /**
     * Set addresses so that developers can verify their forms implementation.
     *
     * @param addresses No description
     */
    public suspend fun setAddresses(addresses: List<Address>) {
        val parameter = SetAddressesParameter(addresses = addresses)
        setAddresses(parameter)
    }

    /**
     * Disables autofill domain notifications.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Autofill.disable", parameter, mode)
    }

    /**
     * Enables autofill domain notifications.
     */
    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Autofill.enable", parameter, mode)
    }

    @Serializable
    public data class CreditCard(
        /**
         * 16-digit credit card number.
         */
        public val number: String,
        /**
         * Name of the credit card owner.
         */
        public val name: String,
        /**
         * 2-digit expiry month.
         */
        public val expiryMonth: String,
        /**
         * 4-digit expiry year.
         */
        public val expiryYear: String,
        /**
         * 3-digit card verification code.
         */
        public val cvc: String,
    )

    @Serializable
    public data class AddressField(
        /**
         * address field name, for example GIVEN_NAME.
         */
        public val name: String,
        /**
         * address field value, for example Jon Doe.
         */
        public val `value`: String,
    )

    /**
     * A list of address fields.
     */
    @Serializable
    public data class AddressFields(
        public val fields: List<AddressField>,
    )

    @Serializable
    public data class Address(
        /**
         * fields and values defining an address.
         */
        public val fields: List<AddressField>,
    )

    /**
     * Defines how an address can be displayed like in chrome://settings/addresses.
     * Address UI is a two dimensional array, each inner array is an "address information line", and when rendered in a UI surface should be displayed as such.
     * The following address UI for instance:
     * [[{name: "GIVE_NAME", value: "Jon"}, {name: "FAMILY_NAME", value: "Doe"}], [{name: "CITY", value: "Munich"}, {name: "ZIP", value: "81456"}]]
     * should allow the receiver to render:
     * Jon Doe
     * Munich 81456
     */
    @Serializable
    public data class AddressUI(
        /**
         * A two dimension array containing the representation of values from an address profile.
         */
        public val addressFields: List<AddressFields>,
    )

    /**
     * Specified whether a filled field was done so by using the html autocomplete attribute or autofill heuristics.
     */
    @Serializable
    public enum class FillingStrategy {
        @SerialName("autocompleteAttribute")
        AUTOCOMPLETEATTRIBUTE,

        @SerialName("autofillInferred")
        AUTOFILLINFERRED,
    }

    @Serializable
    public data class FilledField(
        /**
         * The type of the field, e.g text, password etc.
         */
        public val htmlType: String,
        /**
         * the html id
         */
        public val id: String,
        /**
         * the html name
         */
        public val name: String,
        /**
         * the field value
         */
        public val `value`: String,
        /**
         * The actual field type, e.g FAMILY_NAME
         */
        public val autofillType: String,
        /**
         * The filling strategy
         */
        public val fillingStrategy: FillingStrategy,
        /**
         * The frame the field belongs to
         */
        public val frameId: String,
        /**
         * The form field's DOM node
         */
        public val fieldId: Int,
    )

    /**
     * Emitted when an address form is filled.
     */
    @Serializable
    public data class AddressFormFilledParameter(
        /**
         * Information about the fields that were filled
         */
        public val filledFields: List<FilledField>,
        /**
         * An UI representation of the address used to fill the form.
         * Consists of a 2D array where each child represents an address/profile line.
         */
        public val addressUi: AddressUI,
    )

    @Serializable
    public data class TriggerParameter(
        /**
         * Identifies a field that serves as an anchor for autofill.
         */
        public val fieldId: Int,
        /**
         * Identifies the frame that field belongs to.
         */
        public val frameId: String? = null,
        /**
         * Credit card information to fill out the form. Credit card data is not saved.
         */
        public val card: CreditCard,
    )

    @Serializable
    public data class SetAddressesParameter(
        public val addresses: List<Address>,
    )
}
