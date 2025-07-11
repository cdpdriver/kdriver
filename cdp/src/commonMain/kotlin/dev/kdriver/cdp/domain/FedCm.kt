package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.fedCm: FedCm
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(FedCm(this))

/**
 * This domain allows interacting with the FedCM dialog.
 */
public class FedCm(
    private val cdp: CDP,
) : Domain {
    public val dialogShown: Flow<DialogShownParameter> = cdp
        .events
        .filter { it.method == "FedCm.dialogShown" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Triggered when a dialog is closed, either by user action, JS abort,
     * or a command below.
     */
    public val dialogClosed: Flow<DialogClosedParameter> = cdp
        .events
        .filter { it.method == "FedCm.dialogClosed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public suspend fun enable(args: EnableParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("FedCm.enable", parameter)
    }

    /**
     *
     *
     * @param disableRejectionDelay Allows callers to disable the promise rejection delay that would
     * normally happen, if this is unimportant to what's being tested.
     * (step 4 of https://fedidcg.github.io/FedCM/#browser-api-rp-sign-in)
     */
    public suspend fun enable(disableRejectionDelay: Boolean? = null) {
        val parameter = EnableParameter(disableRejectionDelay = disableRejectionDelay)
        enable(parameter)
    }

    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("FedCm.disable", parameter)
    }

    public suspend fun selectAccount(args: SelectAccountParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("FedCm.selectAccount", parameter)
    }

    /**
     *
     *
     * @param dialogId No description
     * @param accountIndex No description
     */
    public suspend fun selectAccount(dialogId: String, accountIndex: Int) {
        val parameter = SelectAccountParameter(dialogId = dialogId, accountIndex = accountIndex)
        selectAccount(parameter)
    }

    public suspend fun clickDialogButton(args: ClickDialogButtonParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("FedCm.clickDialogButton", parameter)
    }

    /**
     *
     *
     * @param dialogId No description
     * @param dialogButton No description
     */
    public suspend fun clickDialogButton(dialogId: String, dialogButton: DialogButton) {
        val parameter = ClickDialogButtonParameter(dialogId = dialogId, dialogButton = dialogButton)
        clickDialogButton(parameter)
    }

    public suspend fun openUrl(args: OpenUrlParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("FedCm.openUrl", parameter)
    }

    /**
     *
     *
     * @param dialogId No description
     * @param accountIndex No description
     * @param accountUrlType No description
     */
    public suspend fun openUrl(
        dialogId: String,
        accountIndex: Int,
        accountUrlType: AccountUrlType,
    ) {
        val parameter =
            OpenUrlParameter(dialogId = dialogId, accountIndex = accountIndex, accountUrlType = accountUrlType)
        openUrl(parameter)
    }

    public suspend fun dismissDialog(args: DismissDialogParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("FedCm.dismissDialog", parameter)
    }

    /**
     *
     *
     * @param dialogId No description
     * @param triggerCooldown No description
     */
    public suspend fun dismissDialog(dialogId: String, triggerCooldown: Boolean? = null) {
        val parameter = DismissDialogParameter(dialogId = dialogId, triggerCooldown = triggerCooldown)
        dismissDialog(parameter)
    }

    /**
     * Resets the cooldown time, if any, to allow the next FedCM call to show
     * a dialog even if one was recently dismissed by the user.
     */
    public suspend fun resetCooldown() {
        val parameter = null
        cdp.callCommand("FedCm.resetCooldown", parameter)
    }

    /**
     * Whether this is a sign-up or sign-in action for this account, i.e.
     * whether this account has ever been used to sign in to this RP before.
     */
    @Serializable
    public enum class LoginState {
        @SerialName("SignIn")
        SIGNIN,

        @SerialName("SignUp")
        SIGNUP,
    }

    /**
     * The types of FedCM dialogs.
     */
    @Serializable
    public enum class DialogType {
        @SerialName("AccountChooser")
        ACCOUNTCHOOSER,

        @SerialName("AutoReauthn")
        AUTOREAUTHN,

        @SerialName("ConfirmIdpLogin")
        CONFIRMIDPLOGIN,

        @SerialName("Error")
        ERROR,
    }

    /**
     * The buttons on the FedCM dialog.
     */
    @Serializable
    public enum class DialogButton {
        @SerialName("ConfirmIdpLoginContinue")
        CONFIRMIDPLOGINCONTINUE,

        @SerialName("ErrorGotIt")
        ERRORGOTIT,

        @SerialName("ErrorMoreDetails")
        ERRORMOREDETAILS,
    }

    /**
     * The URLs that each account has
     */
    @Serializable
    public enum class AccountUrlType {
        @SerialName("TermsOfService")
        TERMSOFSERVICE,

        @SerialName("PrivacyPolicy")
        PRIVACYPOLICY,
    }

    /**
     * Corresponds to IdentityRequestAccount
     */
    @Serializable
    public data class Account(
        public val accountId: String,
        public val email: String,
        public val name: String,
        public val givenName: String,
        public val pictureUrl: String,
        public val idpConfigUrl: String,
        public val idpLoginUrl: String,
        public val loginState: LoginState,
        /**
         * These two are only set if the loginState is signUp
         */
        public val termsOfServiceUrl: String? = null,
        public val privacyPolicyUrl: String? = null,
    )

    @Serializable
    public data class DialogShownParameter(
        public val dialogId: String,
        public val dialogType: DialogType,
        public val accounts: List<Account>,
        /**
         * These exist primarily so that the caller can verify the
         * RP context was used appropriately.
         */
        public val title: String,
        public val subtitle: String? = null,
    )

    /**
     * Triggered when a dialog is closed, either by user action, JS abort,
     * or a command below.
     */
    @Serializable
    public data class DialogClosedParameter(
        public val dialogId: String,
    )

    @Serializable
    public data class EnableParameter(
        /**
         * Allows callers to disable the promise rejection delay that would
         * normally happen, if this is unimportant to what's being tested.
         * (step 4 of https://fedidcg.github.io/FedCM/#browser-api-rp-sign-in)
         */
        public val disableRejectionDelay: Boolean? = null,
    )

    @Serializable
    public data class SelectAccountParameter(
        public val dialogId: String,
        public val accountIndex: Int,
    )

    @Serializable
    public data class ClickDialogButtonParameter(
        public val dialogId: String,
        public val dialogButton: DialogButton,
    )

    @Serializable
    public data class OpenUrlParameter(
        public val dialogId: String,
        public val accountIndex: Int,
        public val accountUrlType: AccountUrlType,
    )

    @Serializable
    public data class DismissDialogParameter(
        public val dialogId: String,
        public val triggerCooldown: Boolean? = null,
    )
}
