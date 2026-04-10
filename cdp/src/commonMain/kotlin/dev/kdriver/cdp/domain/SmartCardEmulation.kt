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

public val CDP.smartCardEmulation: SmartCardEmulation
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(SmartCardEmulation(this))

public class SmartCardEmulation(
    private val cdp: CDP,
) : Domain {
    /**
     * Fired when |SCardEstablishContext| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaa1b8970169fd4883a6dc4a8f43f19b67
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardestablishcontext
     */
    public val establishContextRequested: Flow<EstablishContextRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.establishContextRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardReleaseContext| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga6aabcba7744c5c9419fdd6404f73a934
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardreleasecontext
     */
    public val releaseContextRequested: Flow<ReleaseContextRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.releaseContextRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardListReaders| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga93b07815789b3cf2629d439ecf20f0d9
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardlistreadersa
     */
    public val listReadersRequested: Flow<ListReadersRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.listReadersRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardGetStatusChange| is called. Timeout is specified in milliseconds.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga33247d5d1257d59e55647c3bb717db24
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardgetstatuschangea
     */
    public val getStatusChangeRequested: Flow<GetStatusChangeRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.getStatusChangeRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardCancel| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaacbbc0c6d6c0cbbeb4f4debf6fbeeee6
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardcancel
     */
    public val cancelRequested: Flow<CancelRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.cancelRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardConnect| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga4e515829752e0a8dbc4d630696a8d6a5
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardconnecta
     */
    public val connectRequested: Flow<ConnectRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.connectRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardDisconnect| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga4be198045c73ec0deb79e66c0ca1738a
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scarddisconnect
     */
    public val disconnectRequested: Flow<DisconnectRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.disconnectRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardTransmit| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga9a2d77242a271310269065e64633ab99
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardtransmit
     */
    public val transmitRequested: Flow<TransmitRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.transmitRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardControl| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gac3454d4657110fd7f753b2d3d8f4e32f
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardcontrol
     */
    public val controlRequested: Flow<ControlRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.controlRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardGetAttrib| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaacfec51917255b7a25b94c5104961602
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardgetattrib
     */
    public val getAttribRequested: Flow<GetAttribRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.getAttribRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardSetAttrib| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga060f0038a4ddfd5dd2b8fadf3c3a2e4f
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardsetattrib
     */
    public val setAttribRequested: Flow<SetAttribRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.setAttribRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardStatus| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gae49c3c894ad7ac12a5b896bde70d0382
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardstatusa
     */
    public val statusRequested: Flow<StatusRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.statusRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardBeginTransaction| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaddb835dce01a0da1d6ca02d33ee7d861
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardbegintransaction
     */
    public val beginTransactionRequested: Flow<BeginTransactionRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.beginTransactionRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when |SCardEndTransaction| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gae8742473b404363e5c587f570d7e2f3b
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardendtransaction
     */
    public val endTransactionRequested: Flow<EndTransactionRequestedParameter> = cdp
        .events
        .filter { it.method == "SmartCardEmulation.endTransactionRequested" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Enables the |SmartCardEmulation| domain.
     */
    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("SmartCardEmulation.enable", parameter, mode)
    }

    /**
     * Disables the |SmartCardEmulation| domain.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("SmartCardEmulation.disable", parameter, mode)
    }

    /**
     * Reports the successful result of a |SCardEstablishContext| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaa1b8970169fd4883a6dc4a8f43f19b67
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardestablishcontext
     */
    public suspend fun reportEstablishContextResult(
        args: ReportEstablishContextResultParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("SmartCardEmulation.reportEstablishContextResult", parameter, mode)
    }

    /**
     * Reports the successful result of a |SCardEstablishContext| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaa1b8970169fd4883a6dc4a8f43f19b67
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardestablishcontext
     *
     * @param requestId No description
     * @param contextId No description
     */
    public suspend fun reportEstablishContextResult(requestId: String, contextId: Int) {
        val parameter = ReportEstablishContextResultParameter(requestId = requestId, contextId = contextId)
        reportEstablishContextResult(parameter)
    }

    /**
     * Reports the successful result of a |SCardReleaseContext| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga6aabcba7744c5c9419fdd6404f73a934
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardreleasecontext
     */
    public suspend fun reportReleaseContextResult(
        args: ReportReleaseContextResultParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("SmartCardEmulation.reportReleaseContextResult", parameter, mode)
    }

    /**
     * Reports the successful result of a |SCardReleaseContext| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga6aabcba7744c5c9419fdd6404f73a934
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardreleasecontext
     *
     * @param requestId No description
     */
    public suspend fun reportReleaseContextResult(requestId: String) {
        val parameter = ReportReleaseContextResultParameter(requestId = requestId)
        reportReleaseContextResult(parameter)
    }

    /**
     * Reports the successful result of a |SCardListReaders| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga93b07815789b3cf2629d439ecf20f0d9
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardlistreadersa
     */
    public suspend fun reportListReadersResult(
        args: ReportListReadersResultParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("SmartCardEmulation.reportListReadersResult", parameter, mode)
    }

    /**
     * Reports the successful result of a |SCardListReaders| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga93b07815789b3cf2629d439ecf20f0d9
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardlistreadersa
     *
     * @param requestId No description
     * @param readers No description
     */
    public suspend fun reportListReadersResult(requestId: String, readers: List<String>) {
        val parameter = ReportListReadersResultParameter(requestId = requestId, readers = readers)
        reportListReadersResult(parameter)
    }

    /**
     * Reports the successful result of a |SCardGetStatusChange| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga33247d5d1257d59e55647c3bb717db24
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardgetstatuschangea
     */
    public suspend fun reportGetStatusChangeResult(
        args: ReportGetStatusChangeResultParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("SmartCardEmulation.reportGetStatusChangeResult", parameter, mode)
    }

    /**
     * Reports the successful result of a |SCardGetStatusChange| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga33247d5d1257d59e55647c3bb717db24
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardgetstatuschangea
     *
     * @param requestId No description
     * @param readerStates No description
     */
    public suspend fun reportGetStatusChangeResult(requestId: String, readerStates: List<ReaderStateOut>) {
        val parameter = ReportGetStatusChangeResultParameter(requestId = requestId, readerStates = readerStates)
        reportGetStatusChangeResult(parameter)
    }

    /**
     * Reports the result of a |SCardBeginTransaction| call.
     * On success, this creates a new transaction object.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaddb835dce01a0da1d6ca02d33ee7d861
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardbegintransaction
     */
    public suspend fun reportBeginTransactionResult(
        args: ReportBeginTransactionResultParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("SmartCardEmulation.reportBeginTransactionResult", parameter, mode)
    }

    /**
     * Reports the result of a |SCardBeginTransaction| call.
     * On success, this creates a new transaction object.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaddb835dce01a0da1d6ca02d33ee7d861
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardbegintransaction
     *
     * @param requestId No description
     * @param handle No description
     */
    public suspend fun reportBeginTransactionResult(requestId: String, handle: Int) {
        val parameter = ReportBeginTransactionResultParameter(requestId = requestId, handle = handle)
        reportBeginTransactionResult(parameter)
    }

    /**
     * Reports the successful result of a call that returns only a result code.
     * Used for: |SCardCancel|, |SCardDisconnect|, |SCardSetAttrib|, |SCardEndTransaction|.
     *
     * This maps to:
     * 1. SCardCancel
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaacbbc0c6d6c0cbbeb4f4debf6fbeeee6
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardcancel
     *
     * 2. SCardDisconnect
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga4be198045c73ec0deb79e66c0ca1738a
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scarddisconnect
     *
     * 3. SCardSetAttrib
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga060f0038a4ddfd5dd2b8fadf3c3a2e4f
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardsetattrib
     *
     * 4. SCardEndTransaction
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gae8742473b404363e5c587f570d7e2f3b
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardendtransaction
     */
    public suspend fun reportPlainResult(args: ReportPlainResultParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("SmartCardEmulation.reportPlainResult", parameter, mode)
    }

    /**
     * Reports the successful result of a call that returns only a result code.
     * Used for: |SCardCancel|, |SCardDisconnect|, |SCardSetAttrib|, |SCardEndTransaction|.
     *
     * This maps to:
     * 1. SCardCancel
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaacbbc0c6d6c0cbbeb4f4debf6fbeeee6
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardcancel
     *
     * 2. SCardDisconnect
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga4be198045c73ec0deb79e66c0ca1738a
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scarddisconnect
     *
     * 3. SCardSetAttrib
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga060f0038a4ddfd5dd2b8fadf3c3a2e4f
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardsetattrib
     *
     * 4. SCardEndTransaction
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gae8742473b404363e5c587f570d7e2f3b
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardendtransaction
     *
     * @param requestId No description
     */
    public suspend fun reportPlainResult(requestId: String) {
        val parameter = ReportPlainResultParameter(requestId = requestId)
        reportPlainResult(parameter)
    }

    /**
     * Reports the successful result of a |SCardConnect| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga4e515829752e0a8dbc4d630696a8d6a5
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardconnecta
     */
    public suspend fun reportConnectResult(
        args: ReportConnectResultParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("SmartCardEmulation.reportConnectResult", parameter, mode)
    }

    /**
     * Reports the successful result of a |SCardConnect| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga4e515829752e0a8dbc4d630696a8d6a5
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardconnecta
     *
     * @param requestId No description
     * @param handle No description
     * @param activeProtocol No description
     */
    public suspend fun reportConnectResult(
        requestId: String,
        handle: Int,
        activeProtocol: Protocol? = null,
    ) {
        val parameter =
            ReportConnectResultParameter(requestId = requestId, handle = handle, activeProtocol = activeProtocol)
        reportConnectResult(parameter)
    }

    /**
     * Reports the successful result of a call that sends back data on success.
     * Used for |SCardTransmit|, |SCardControl|, and |SCardGetAttrib|.
     *
     * This maps to:
     * 1. SCardTransmit
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga9a2d77242a271310269065e64633ab99
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardtransmit
     *
     * 2. SCardControl
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gac3454d4657110fd7f753b2d3d8f4e32f
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardcontrol
     *
     * 3. SCardGetAttrib
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaacfec51917255b7a25b94c5104961602
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardgetattrib
     */
    public suspend fun reportDataResult(args: ReportDataResultParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("SmartCardEmulation.reportDataResult", parameter, mode)
    }

    /**
     * Reports the successful result of a call that sends back data on success.
     * Used for |SCardTransmit|, |SCardControl|, and |SCardGetAttrib|.
     *
     * This maps to:
     * 1. SCardTransmit
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga9a2d77242a271310269065e64633ab99
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardtransmit
     *
     * 2. SCardControl
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gac3454d4657110fd7f753b2d3d8f4e32f
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardcontrol
     *
     * 3. SCardGetAttrib
     *    PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaacfec51917255b7a25b94c5104961602
     *    Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardgetattrib
     *
     * @param requestId No description
     * @param data No description
     */
    public suspend fun reportDataResult(requestId: String, `data`: String) {
        val parameter = ReportDataResultParameter(requestId = requestId, data = data)
        reportDataResult(parameter)
    }

    /**
     * Reports the successful result of a |SCardStatus| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gae49c3c894ad7ac12a5b896bde70d0382
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardstatusa
     */
    public suspend fun reportStatusResult(args: ReportStatusResultParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("SmartCardEmulation.reportStatusResult", parameter, mode)
    }

    /**
     * Reports the successful result of a |SCardStatus| call.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gae49c3c894ad7ac12a5b896bde70d0382
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardstatusa
     *
     * @param requestId No description
     * @param readerName No description
     * @param state No description
     * @param atr No description
     * @param protocol No description
     */
    public suspend fun reportStatusResult(
        requestId: String,
        readerName: String,
        state: ConnectionState,
        atr: String,
        protocol: Protocol? = null,
    ) {
        val parameter = ReportStatusResultParameter(
            requestId = requestId,
            readerName = readerName,
            state = state,
            atr = atr,
            protocol = protocol
        )
        reportStatusResult(parameter)
    }

    /**
     * Reports an error result for the given request.
     */
    public suspend fun reportError(args: ReportErrorParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("SmartCardEmulation.reportError", parameter, mode)
    }

    /**
     * Reports an error result for the given request.
     *
     * @param requestId No description
     * @param resultCode No description
     */
    public suspend fun reportError(requestId: String, resultCode: ResultCode) {
        val parameter = ReportErrorParameter(requestId = requestId, resultCode = resultCode)
        reportError(parameter)
    }

    /**
     * Indicates the PC/SC error code.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__ErrorCodes.html
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/secauthn/authentication-return-values
     */
    @Serializable
    public enum class ResultCode {
        @SerialName("success")
        SUCCESS,

        @SerialName("removed-card")
        REMOVED_CARD,

        @SerialName("reset-card")
        RESET_CARD,

        @SerialName("unpowered-card")
        UNPOWERED_CARD,

        @SerialName("unresponsive-card")
        UNRESPONSIVE_CARD,

        @SerialName("unsupported-card")
        UNSUPPORTED_CARD,

        @SerialName("reader-unavailable")
        READER_UNAVAILABLE,

        @SerialName("sharing-violation")
        SHARING_VIOLATION,

        @SerialName("not-transacted")
        NOT_TRANSACTED,

        @SerialName("no-smartcard")
        NO_SMARTCARD,

        @SerialName("proto-mismatch")
        PROTO_MISMATCH,

        @SerialName("system-cancelled")
        SYSTEM_CANCELLED,

        @SerialName("not-ready")
        NOT_READY,

        @SerialName("cancelled")
        CANCELLED,

        @SerialName("insufficient-buffer")
        INSUFFICIENT_BUFFER,

        @SerialName("invalid-handle")
        INVALID_HANDLE,

        @SerialName("invalid-parameter")
        INVALID_PARAMETER,

        @SerialName("invalid-value")
        INVALID_VALUE,

        @SerialName("no-memory")
        NO_MEMORY,

        @SerialName("timeout")
        TIMEOUT,

        @SerialName("unknown-reader")
        UNKNOWN_READER,

        @SerialName("unsupported-feature")
        UNSUPPORTED_FEATURE,

        @SerialName("no-readers-available")
        NO_READERS_AVAILABLE,

        @SerialName("service-stopped")
        SERVICE_STOPPED,

        @SerialName("no-service")
        NO_SERVICE,

        @SerialName("comm-error")
        COMM_ERROR,

        @SerialName("internal-error")
        INTERNAL_ERROR,

        @SerialName("server-too-busy")
        SERVER_TOO_BUSY,

        @SerialName("unexpected")
        UNEXPECTED,

        @SerialName("shutdown")
        SHUTDOWN,

        @SerialName("unknown-card")
        UNKNOWN_CARD,

        @SerialName("unknown")
        UNKNOWN,
    }

    /**
     * Maps to the |SCARD_SHARE_*| values.
     */
    @Serializable
    public enum class ShareMode {
        @SerialName("shared")
        SHARED,

        @SerialName("exclusive")
        EXCLUSIVE,

        @SerialName("direct")
        DIRECT,
    }

    /**
     * Indicates what the reader should do with the card.
     */
    @Serializable
    public enum class Disposition {
        @SerialName("leave-card")
        LEAVE_CARD,

        @SerialName("reset-card")
        RESET_CARD,

        @SerialName("unpower-card")
        UNPOWER_CARD,

        @SerialName("eject-card")
        EJECT_CARD,
    }

    /**
     * Maps to |SCARD_*| connection state values.
     */
    @Serializable
    public enum class ConnectionState {
        @SerialName("absent")
        ABSENT,

        @SerialName("present")
        PRESENT,

        @SerialName("swallowed")
        SWALLOWED,

        @SerialName("powered")
        POWERED,

        @SerialName("negotiable")
        NEGOTIABLE,

        @SerialName("specific")
        SPECIFIC,
    }

    /**
     * Maps to the |SCARD_STATE_*| flags.
     */
    @Serializable
    public data class ReaderStateFlags(
        public val unaware: Boolean? = null,
        public val ignore: Boolean? = null,
        public val changed: Boolean? = null,
        public val unknown: Boolean? = null,
        public val unavailable: Boolean? = null,
        public val empty: Boolean? = null,
        public val present: Boolean? = null,
        public val exclusive: Boolean? = null,
        public val inuse: Boolean? = null,
        public val mute: Boolean? = null,
        public val unpowered: Boolean? = null,
    )

    /**
     * Maps to the |SCARD_PROTOCOL_*| flags.
     */
    @Serializable
    public data class ProtocolSet(
        public val t0: Boolean? = null,
        public val t1: Boolean? = null,
        public val raw: Boolean? = null,
    )

    /**
     * Maps to the |SCARD_PROTOCOL_*| values.
     */
    @Serializable
    public enum class Protocol {
        @SerialName("t0")
        T0,

        @SerialName("t1")
        T1,

        @SerialName("raw")
        RAW,
    }

    @Serializable
    public data class ReaderStateIn(
        public val reader: String,
        public val currentState: ReaderStateFlags,
        public val currentInsertionCount: Int,
    )

    @Serializable
    public data class ReaderStateOut(
        public val reader: String,
        public val eventState: ReaderStateFlags,
        public val eventCount: Int,
        public val atr: String,
    )

    /**
     * Fired when |SCardEstablishContext| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaa1b8970169fd4883a6dc4a8f43f19b67
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardestablishcontext
     */
    @Serializable
    public data class EstablishContextRequestedParameter(
        public val requestId: String,
    )

    /**
     * Fired when |SCardReleaseContext| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga6aabcba7744c5c9419fdd6404f73a934
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardreleasecontext
     */
    @Serializable
    public data class ReleaseContextRequestedParameter(
        public val requestId: String,
        public val contextId: Int,
    )

    /**
     * Fired when |SCardListReaders| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga93b07815789b3cf2629d439ecf20f0d9
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardlistreadersa
     */
    @Serializable
    public data class ListReadersRequestedParameter(
        public val requestId: String,
        public val contextId: Int,
    )

    /**
     * Fired when |SCardGetStatusChange| is called. Timeout is specified in milliseconds.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga33247d5d1257d59e55647c3bb717db24
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardgetstatuschangea
     */
    @Serializable
    public data class GetStatusChangeRequestedParameter(
        public val requestId: String,
        public val contextId: Int,
        public val readerStates: List<ReaderStateIn>,
        /**
         * in milliseconds, if absent, it means "infinite"
         */
        public val timeout: Int? = null,
    )

    /**
     * Fired when |SCardCancel| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaacbbc0c6d6c0cbbeb4f4debf6fbeeee6
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardcancel
     */
    @Serializable
    public data class CancelRequestedParameter(
        public val requestId: String,
        public val contextId: Int,
    )

    /**
     * Fired when |SCardConnect| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga4e515829752e0a8dbc4d630696a8d6a5
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardconnecta
     */
    @Serializable
    public data class ConnectRequestedParameter(
        public val requestId: String,
        public val contextId: Int,
        public val reader: String,
        public val shareMode: ShareMode,
        public val preferredProtocols: ProtocolSet,
    )

    /**
     * Fired when |SCardDisconnect| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga4be198045c73ec0deb79e66c0ca1738a
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scarddisconnect
     */
    @Serializable
    public data class DisconnectRequestedParameter(
        public val requestId: String,
        public val handle: Int,
        public val disposition: Disposition,
    )

    /**
     * Fired when |SCardTransmit| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga9a2d77242a271310269065e64633ab99
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardtransmit
     */
    @Serializable
    public data class TransmitRequestedParameter(
        public val requestId: String,
        public val handle: Int,
        public val `data`: String,
        public val protocol: Protocol? = null,
    )

    /**
     * Fired when |SCardControl| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gac3454d4657110fd7f753b2d3d8f4e32f
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardcontrol
     */
    @Serializable
    public data class ControlRequestedParameter(
        public val requestId: String,
        public val handle: Int,
        public val controlCode: Int,
        public val `data`: String,
    )

    /**
     * Fired when |SCardGetAttrib| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaacfec51917255b7a25b94c5104961602
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardgetattrib
     */
    @Serializable
    public data class GetAttribRequestedParameter(
        public val requestId: String,
        public val handle: Int,
        public val attribId: Int,
    )

    /**
     * Fired when |SCardSetAttrib| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#ga060f0038a4ddfd5dd2b8fadf3c3a2e4f
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardsetattrib
     */
    @Serializable
    public data class SetAttribRequestedParameter(
        public val requestId: String,
        public val handle: Int,
        public val attribId: Int,
        public val `data`: String,
    )

    /**
     * Fired when |SCardStatus| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gae49c3c894ad7ac12a5b896bde70d0382
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardstatusa
     */
    @Serializable
    public data class StatusRequestedParameter(
        public val requestId: String,
        public val handle: Int,
    )

    /**
     * Fired when |SCardBeginTransaction| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gaddb835dce01a0da1d6ca02d33ee7d861
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardbegintransaction
     */
    @Serializable
    public data class BeginTransactionRequestedParameter(
        public val requestId: String,
        public val handle: Int,
    )

    /**
     * Fired when |SCardEndTransaction| is called.
     *
     * This maps to:
     * PC/SC Lite: https://pcsclite.apdu.fr/api/group__API.html#gae8742473b404363e5c587f570d7e2f3b
     * Microsoft: https://learn.microsoft.com/en-us/windows/win32/api/winscard/nf-winscard-scardendtransaction
     */
    @Serializable
    public data class EndTransactionRequestedParameter(
        public val requestId: String,
        public val handle: Int,
        public val disposition: Disposition,
    )

    @Serializable
    public data class ReportEstablishContextResultParameter(
        public val requestId: String,
        public val contextId: Int,
    )

    @Serializable
    public data class ReportReleaseContextResultParameter(
        public val requestId: String,
    )

    @Serializable
    public data class ReportListReadersResultParameter(
        public val requestId: String,
        public val readers: List<String>,
    )

    @Serializable
    public data class ReportGetStatusChangeResultParameter(
        public val requestId: String,
        public val readerStates: List<ReaderStateOut>,
    )

    @Serializable
    public data class ReportBeginTransactionResultParameter(
        public val requestId: String,
        public val handle: Int,
    )

    @Serializable
    public data class ReportPlainResultParameter(
        public val requestId: String,
    )

    @Serializable
    public data class ReportConnectResultParameter(
        public val requestId: String,
        public val handle: Int,
        public val activeProtocol: Protocol? = null,
    )

    @Serializable
    public data class ReportDataResultParameter(
        public val requestId: String,
        public val `data`: String,
    )

    @Serializable
    public data class ReportStatusResultParameter(
        public val requestId: String,
        public val readerName: String,
        public val state: ConnectionState,
        public val atr: String,
        public val protocol: Protocol? = null,
    )

    @Serializable
    public data class ReportErrorParameter(
        public val requestId: String,
        public val resultCode: ResultCode,
    )
}
