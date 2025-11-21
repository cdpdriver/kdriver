@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kdriver.cdp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * This domain allows configuring virtual authenticators to test the WebAuthn
 * API.
 */
public val CDP.webAuthn: WebAuthn
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(WebAuthn(this))

/**
 * This domain allows configuring virtual authenticators to test the WebAuthn
 * API.
 */
public class WebAuthn(
    private val cdp: CDP,
) : Domain {
    /**
     * Triggered when a credential is added to an authenticator.
     */
    public val credentialAdded: Flow<CredentialAddedParameter> = cdp
        .events
        .filter { it.method == "WebAuthn.credentialAdded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Triggered when a credential is deleted, e.g. through
     * PublicKeyCredential.signalUnknownCredential().
     */
    public val credentialDeleted: Flow<CredentialDeletedParameter> = cdp
        .events
        .filter { it.method == "WebAuthn.credentialDeleted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Triggered when a credential is updated, e.g. through
     * PublicKeyCredential.signalCurrentUserDetails().
     */
    public val credentialUpdated: Flow<CredentialUpdatedParameter> = cdp
        .events
        .filter { it.method == "WebAuthn.credentialUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Triggered when a credential is used in a webauthn assertion.
     */
    public val credentialAsserted: Flow<CredentialAssertedParameter> = cdp
        .events
        .filter { it.method == "WebAuthn.credentialAsserted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Enable the WebAuthn domain and start intercepting credential storage and
     * retrieval with a virtual authenticator.
     */
    public suspend fun enable(args: EnableParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("WebAuthn.enable", parameter, mode)
    }

    /**
     * Enable the WebAuthn domain and start intercepting credential storage and
     * retrieval with a virtual authenticator.
     *
     * @param enableUI Whether to enable the WebAuthn user interface. Enabling the UI is
     * recommended for debugging and demo purposes, as it is closer to the real
     * experience. Disabling the UI is recommended for automated testing.
     * Supported at the embedder's discretion if UI is available.
     * Defaults to false.
     */
    public suspend fun enable(enableUI: Boolean? = null) {
        val parameter = EnableParameter(enableUI = enableUI)
        enable(parameter)
    }

    /**
     * Disable the WebAuthn domain.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("WebAuthn.disable", parameter, mode)
    }

    /**
     * Creates and adds a virtual authenticator.
     */
    public suspend fun addVirtualAuthenticator(
        args: AddVirtualAuthenticatorParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): AddVirtualAuthenticatorReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("WebAuthn.addVirtualAuthenticator", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Creates and adds a virtual authenticator.
     *
     * @param options No description
     */
    public suspend fun addVirtualAuthenticator(options: VirtualAuthenticatorOptions): AddVirtualAuthenticatorReturn {
        val parameter = AddVirtualAuthenticatorParameter(options = options)
        return addVirtualAuthenticator(parameter)
    }

    /**
     * Resets parameters isBogusSignature, isBadUV, isBadUP to false if they are not present.
     */
    public suspend fun setResponseOverrideBits(
        args: SetResponseOverrideBitsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("WebAuthn.setResponseOverrideBits", parameter, mode)
    }

    /**
     * Resets parameters isBogusSignature, isBadUV, isBadUP to false if they are not present.
     *
     * @param authenticatorId No description
     * @param isBogusSignature If isBogusSignature is set, overrides the signature in the authenticator response to be zero.
     * Defaults to false.
     * @param isBadUV If isBadUV is set, overrides the UV bit in the flags in the authenticator response to
     * be zero. Defaults to false.
     * @param isBadUP If isBadUP is set, overrides the UP bit in the flags in the authenticator response to
     * be zero. Defaults to false.
     */
    public suspend fun setResponseOverrideBits(
        authenticatorId: String,
        isBogusSignature: Boolean? = null,
        isBadUV: Boolean? = null,
        isBadUP: Boolean? = null,
    ) {
        val parameter = SetResponseOverrideBitsParameter(
            authenticatorId = authenticatorId,
            isBogusSignature = isBogusSignature,
            isBadUV = isBadUV,
            isBadUP = isBadUP
        )
        setResponseOverrideBits(parameter)
    }

    /**
     * Removes the given authenticator.
     */
    public suspend fun removeVirtualAuthenticator(
        args: RemoveVirtualAuthenticatorParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("WebAuthn.removeVirtualAuthenticator", parameter, mode)
    }

    /**
     * Removes the given authenticator.
     *
     * @param authenticatorId No description
     */
    public suspend fun removeVirtualAuthenticator(authenticatorId: String) {
        val parameter = RemoveVirtualAuthenticatorParameter(authenticatorId = authenticatorId)
        removeVirtualAuthenticator(parameter)
    }

    /**
     * Adds the credential to the specified authenticator.
     */
    public suspend fun addCredential(args: AddCredentialParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("WebAuthn.addCredential", parameter, mode)
    }

    /**
     * Adds the credential to the specified authenticator.
     *
     * @param authenticatorId No description
     * @param credential No description
     */
    public suspend fun addCredential(authenticatorId: String, credential: Credential) {
        val parameter = AddCredentialParameter(authenticatorId = authenticatorId, credential = credential)
        addCredential(parameter)
    }

    /**
     * Returns a single credential stored in the given virtual authenticator that
     * matches the credential ID.
     */
    public suspend fun getCredential(
        args: GetCredentialParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetCredentialReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("WebAuthn.getCredential", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns a single credential stored in the given virtual authenticator that
     * matches the credential ID.
     *
     * @param authenticatorId No description
     * @param credentialId No description
     */
    public suspend fun getCredential(authenticatorId: String, credentialId: String): GetCredentialReturn {
        val parameter = GetCredentialParameter(authenticatorId = authenticatorId, credentialId = credentialId)
        return getCredential(parameter)
    }

    /**
     * Returns all the credentials stored in the given virtual authenticator.
     */
    public suspend fun getCredentials(
        args: GetCredentialsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetCredentialsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("WebAuthn.getCredentials", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns all the credentials stored in the given virtual authenticator.
     *
     * @param authenticatorId No description
     */
    public suspend fun getCredentials(authenticatorId: String): GetCredentialsReturn {
        val parameter = GetCredentialsParameter(authenticatorId = authenticatorId)
        return getCredentials(parameter)
    }

    /**
     * Removes a credential from the authenticator.
     */
    public suspend fun removeCredential(args: RemoveCredentialParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("WebAuthn.removeCredential", parameter, mode)
    }

    /**
     * Removes a credential from the authenticator.
     *
     * @param authenticatorId No description
     * @param credentialId No description
     */
    public suspend fun removeCredential(authenticatorId: String, credentialId: String) {
        val parameter = RemoveCredentialParameter(authenticatorId = authenticatorId, credentialId = credentialId)
        removeCredential(parameter)
    }

    /**
     * Clears all the credentials from the specified device.
     */
    public suspend fun clearCredentials(args: ClearCredentialsParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("WebAuthn.clearCredentials", parameter, mode)
    }

    /**
     * Clears all the credentials from the specified device.
     *
     * @param authenticatorId No description
     */
    public suspend fun clearCredentials(authenticatorId: String) {
        val parameter = ClearCredentialsParameter(authenticatorId = authenticatorId)
        clearCredentials(parameter)
    }

    /**
     * Sets whether User Verification succeeds or fails for an authenticator.
     * The default is true.
     */
    public suspend fun setUserVerified(args: SetUserVerifiedParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("WebAuthn.setUserVerified", parameter, mode)
    }

    /**
     * Sets whether User Verification succeeds or fails for an authenticator.
     * The default is true.
     *
     * @param authenticatorId No description
     * @param isUserVerified No description
     */
    public suspend fun setUserVerified(authenticatorId: String, isUserVerified: Boolean) {
        val parameter = SetUserVerifiedParameter(authenticatorId = authenticatorId, isUserVerified = isUserVerified)
        setUserVerified(parameter)
    }

    /**
     * Sets whether tests of user presence will succeed immediately (if true) or fail to resolve (if false) for an authenticator.
     * The default is true.
     */
    public suspend fun setAutomaticPresenceSimulation(
        args: SetAutomaticPresenceSimulationParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("WebAuthn.setAutomaticPresenceSimulation", parameter, mode)
    }

    /**
     * Sets whether tests of user presence will succeed immediately (if true) or fail to resolve (if false) for an authenticator.
     * The default is true.
     *
     * @param authenticatorId No description
     * @param enabled No description
     */
    public suspend fun setAutomaticPresenceSimulation(authenticatorId: String, enabled: Boolean) {
        val parameter = SetAutomaticPresenceSimulationParameter(authenticatorId = authenticatorId, enabled = enabled)
        setAutomaticPresenceSimulation(parameter)
    }

    /**
     * Allows setting credential properties.
     * https://w3c.github.io/webauthn/#sctn-automation-set-credential-properties
     */
    public suspend fun setCredentialProperties(
        args: SetCredentialPropertiesParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("WebAuthn.setCredentialProperties", parameter, mode)
    }

    /**
     * Allows setting credential properties.
     * https://w3c.github.io/webauthn/#sctn-automation-set-credential-properties
     *
     * @param authenticatorId No description
     * @param credentialId No description
     * @param backupEligibility No description
     * @param backupState No description
     */
    public suspend fun setCredentialProperties(
        authenticatorId: String,
        credentialId: String,
        backupEligibility: Boolean? = null,
        backupState: Boolean? = null,
    ) {
        val parameter = SetCredentialPropertiesParameter(
            authenticatorId = authenticatorId,
            credentialId = credentialId,
            backupEligibility = backupEligibility,
            backupState = backupState
        )
        setCredentialProperties(parameter)
    }

    @Serializable
    public enum class AuthenticatorProtocol {
        @SerialName("u2f")
        U2F,

        @SerialName("ctap2")
        CTAP2,
    }

    @Serializable
    public enum class Ctap2Version {
        @SerialName("ctap2_0")
        CTAP2_0,

        @SerialName("ctap2_1")
        CTAP2_1,
    }

    @Serializable
    public enum class AuthenticatorTransport {
        @SerialName("usb")
        USB,

        @SerialName("nfc")
        NFC,

        @SerialName("ble")
        BLE,

        @SerialName("cable")
        CABLE,

        @SerialName("internal")
        INTERNAL,
    }

    @Serializable
    public data class VirtualAuthenticatorOptions(
        public val protocol: AuthenticatorProtocol,
        /**
         * Defaults to ctap2_0. Ignored if |protocol| == u2f.
         */
        public val ctap2Version: Ctap2Version? = null,
        public val transport: AuthenticatorTransport,
        /**
         * Defaults to false.
         */
        public val hasResidentKey: Boolean? = null,
        /**
         * Defaults to false.
         */
        public val hasUserVerification: Boolean? = null,
        /**
         * If set to true, the authenticator will support the largeBlob extension.
         * https://w3c.github.io/webauthn#largeBlob
         * Defaults to false.
         */
        public val hasLargeBlob: Boolean? = null,
        /**
         * If set to true, the authenticator will support the credBlob extension.
         * https://fidoalliance.org/specs/fido-v2.1-rd-20201208/fido-client-to-authenticator-protocol-v2.1-rd-20201208.html#sctn-credBlob-extension
         * Defaults to false.
         */
        public val hasCredBlob: Boolean? = null,
        /**
         * If set to true, the authenticator will support the minPinLength extension.
         * https://fidoalliance.org/specs/fido-v2.1-ps-20210615/fido-client-to-authenticator-protocol-v2.1-ps-20210615.html#sctn-minpinlength-extension
         * Defaults to false.
         */
        public val hasMinPinLength: Boolean? = null,
        /**
         * If set to true, the authenticator will support the prf extension.
         * https://w3c.github.io/webauthn/#prf-extension
         * Defaults to false.
         */
        public val hasPrf: Boolean? = null,
        /**
         * If set to true, tests of user presence will succeed immediately.
         * Otherwise, they will not be resolved. Defaults to true.
         */
        public val automaticPresenceSimulation: Boolean? = null,
        /**
         * Sets whether User Verification succeeds or fails for an authenticator.
         * Defaults to false.
         */
        public val isUserVerified: Boolean? = null,
        /**
         * Credentials created by this authenticator will have the backup
         * eligibility (BE) flag set to this value. Defaults to false.
         * https://w3c.github.io/webauthn/#sctn-credential-backup
         */
        public val defaultBackupEligibility: Boolean? = null,
        /**
         * Credentials created by this authenticator will have the backup state
         * (BS) flag set to this value. Defaults to false.
         * https://w3c.github.io/webauthn/#sctn-credential-backup
         */
        public val defaultBackupState: Boolean? = null,
    )

    @Serializable
    public data class Credential(
        public val credentialId: String,
        public val isResidentCredential: Boolean,
        /**
         * Relying Party ID the credential is scoped to. Must be set when adding a
         * credential.
         */
        public val rpId: String? = null,
        /**
         * The ECDSA P-256 private key in PKCS#8 format. (Encoded as a base64 string when passed over JSON)
         */
        public val privateKey: String,
        /**
         * An opaque byte sequence with a maximum size of 64 bytes mapping the
         * credential to a specific user. (Encoded as a base64 string when passed over JSON)
         */
        public val userHandle: String? = null,
        /**
         * Signature counter. This is incremented by one for each successful
         * assertion.
         * See https://w3c.github.io/webauthn/#signature-counter
         */
        public val signCount: Int,
        /**
         * The large blob associated with the credential.
         * See https://w3c.github.io/webauthn/#sctn-large-blob-extension (Encoded as a base64 string when passed over JSON)
         */
        public val largeBlob: String? = null,
        /**
         * Assertions returned by this credential will have the backup eligibility
         * (BE) flag set to this value. Defaults to the authenticator's
         * defaultBackupEligibility value.
         */
        public val backupEligibility: Boolean? = null,
        /**
         * Assertions returned by this credential will have the backup state (BS)
         * flag set to this value. Defaults to the authenticator's
         * defaultBackupState value.
         */
        public val backupState: Boolean? = null,
        /**
         * The credential's user.name property. Equivalent to empty if not set.
         * https://w3c.github.io/webauthn/#dom-publickeycredentialentity-name
         */
        public val userName: String? = null,
        /**
         * The credential's user.displayName property. Equivalent to empty if
         * not set.
         * https://w3c.github.io/webauthn/#dom-publickeycredentialuserentity-displayname
         */
        public val userDisplayName: String? = null,
    )

    /**
     * Triggered when a credential is added to an authenticator.
     */
    @Serializable
    public data class CredentialAddedParameter(
        public val authenticatorId: String,
        public val credential: Credential,
    )

    /**
     * Triggered when a credential is deleted, e.g. through
     * PublicKeyCredential.signalUnknownCredential().
     */
    @Serializable
    public data class CredentialDeletedParameter(
        public val authenticatorId: String,
        public val credentialId: String,
    )

    /**
     * Triggered when a credential is updated, e.g. through
     * PublicKeyCredential.signalCurrentUserDetails().
     */
    @Serializable
    public data class CredentialUpdatedParameter(
        public val authenticatorId: String,
        public val credential: Credential,
    )

    /**
     * Triggered when a credential is used in a webauthn assertion.
     */
    @Serializable
    public data class CredentialAssertedParameter(
        public val authenticatorId: String,
        public val credential: Credential,
    )

    @Serializable
    public data class EnableParameter(
        /**
         * Whether to enable the WebAuthn user interface. Enabling the UI is
         * recommended for debugging and demo purposes, as it is closer to the real
         * experience. Disabling the UI is recommended for automated testing.
         * Supported at the embedder's discretion if UI is available.
         * Defaults to false.
         */
        public val enableUI: Boolean? = null,
    )

    @Serializable
    public data class AddVirtualAuthenticatorParameter(
        public val options: VirtualAuthenticatorOptions,
    )

    @Serializable
    public data class AddVirtualAuthenticatorReturn(
        public val authenticatorId: String,
    )

    @Serializable
    public data class SetResponseOverrideBitsParameter(
        public val authenticatorId: String,
        /**
         * If isBogusSignature is set, overrides the signature in the authenticator response to be zero.
         * Defaults to false.
         */
        public val isBogusSignature: Boolean? = null,
        /**
         * If isBadUV is set, overrides the UV bit in the flags in the authenticator response to
         * be zero. Defaults to false.
         */
        public val isBadUV: Boolean? = null,
        /**
         * If isBadUP is set, overrides the UP bit in the flags in the authenticator response to
         * be zero. Defaults to false.
         */
        public val isBadUP: Boolean? = null,
    )

    @Serializable
    public data class RemoveVirtualAuthenticatorParameter(
        public val authenticatorId: String,
    )

    @Serializable
    public data class AddCredentialParameter(
        public val authenticatorId: String,
        public val credential: Credential,
    )

    @Serializable
    public data class GetCredentialParameter(
        public val authenticatorId: String,
        public val credentialId: String,
    )

    @Serializable
    public data class GetCredentialReturn(
        public val credential: Credential,
    )

    @Serializable
    public data class GetCredentialsParameter(
        public val authenticatorId: String,
    )

    @Serializable
    public data class GetCredentialsReturn(
        public val credentials: List<Credential>,
    )

    @Serializable
    public data class RemoveCredentialParameter(
        public val authenticatorId: String,
        public val credentialId: String,
    )

    @Serializable
    public data class ClearCredentialsParameter(
        public val authenticatorId: String,
    )

    @Serializable
    public data class SetUserVerifiedParameter(
        public val authenticatorId: String,
        public val isUserVerified: Boolean,
    )

    @Serializable
    public data class SetAutomaticPresenceSimulationParameter(
        public val authenticatorId: String,
        public val enabled: Boolean,
    )

    @Serializable
    public data class SetCredentialPropertiesParameter(
        public val authenticatorId: String,
        public val credentialId: String,
        public val backupEligibility: Boolean? = null,
        public val backupState: Boolean? = null,
    )
}
