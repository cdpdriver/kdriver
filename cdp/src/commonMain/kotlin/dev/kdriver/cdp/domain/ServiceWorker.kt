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

public val CDP.serviceWorker: ServiceWorker
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(ServiceWorker(this))

public class ServiceWorker(
    private val cdp: CDP,
) : Domain {
    public val workerErrorReported: Flow<WorkerErrorReportedParameter> = cdp
        .events
        .filter { it.method == "ServiceWorker.workerErrorReported" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val workerRegistrationUpdated: Flow<WorkerRegistrationUpdatedParameter> = cdp
        .events
        .filter { it.method == "ServiceWorker.workerRegistrationUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val workerVersionUpdated: Flow<WorkerVersionUpdatedParameter> = cdp
        .events
        .filter { it.method == "ServiceWorker.workerVersionUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public suspend fun deliverPushMessage(args: DeliverPushMessageParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("ServiceWorker.deliverPushMessage", parameter, mode)
    }

    /**
     *
     *
     * @param origin No description
     * @param registrationId No description
     * @param data No description
     */
    public suspend fun deliverPushMessage(
        origin: String,
        registrationId: String,
        `data`: String,
    ) {
        val parameter = DeliverPushMessageParameter(origin = origin, registrationId = registrationId, data = data)
        deliverPushMessage(parameter)
    }

    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("ServiceWorker.disable", parameter, mode)
    }

    public suspend fun dispatchSyncEvent(args: DispatchSyncEventParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("ServiceWorker.dispatchSyncEvent", parameter, mode)
    }

    /**
     *
     *
     * @param origin No description
     * @param registrationId No description
     * @param tag No description
     * @param lastChance No description
     */
    public suspend fun dispatchSyncEvent(
        origin: String,
        registrationId: String,
        tag: String,
        lastChance: Boolean,
    ) {
        val parameter = DispatchSyncEventParameter(
            origin = origin,
            registrationId = registrationId,
            tag = tag,
            lastChance = lastChance
        )
        dispatchSyncEvent(parameter)
    }

    public suspend fun dispatchPeriodicSyncEvent(
        args: DispatchPeriodicSyncEventParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("ServiceWorker.dispatchPeriodicSyncEvent", parameter, mode)
    }

    /**
     *
     *
     * @param origin No description
     * @param registrationId No description
     * @param tag No description
     */
    public suspend fun dispatchPeriodicSyncEvent(
        origin: String,
        registrationId: String,
        tag: String,
    ) {
        val parameter = DispatchPeriodicSyncEventParameter(origin = origin, registrationId = registrationId, tag = tag)
        dispatchPeriodicSyncEvent(parameter)
    }

    public suspend fun enable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("ServiceWorker.enable", parameter, mode)
    }

    public suspend fun setForceUpdateOnPageLoad(
        args: SetForceUpdateOnPageLoadParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("ServiceWorker.setForceUpdateOnPageLoad", parameter, mode)
    }

    /**
     *
     *
     * @param forceUpdateOnPageLoad No description
     */
    public suspend fun setForceUpdateOnPageLoad(forceUpdateOnPageLoad: Boolean) {
        val parameter = SetForceUpdateOnPageLoadParameter(forceUpdateOnPageLoad = forceUpdateOnPageLoad)
        setForceUpdateOnPageLoad(parameter)
    }

    public suspend fun skipWaiting(args: SkipWaitingParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("ServiceWorker.skipWaiting", parameter, mode)
    }

    /**
     *
     *
     * @param scopeURL No description
     */
    public suspend fun skipWaiting(scopeURL: String) {
        val parameter = SkipWaitingParameter(scopeURL = scopeURL)
        skipWaiting(parameter)
    }

    public suspend fun startWorker(args: StartWorkerParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("ServiceWorker.startWorker", parameter, mode)
    }

    /**
     *
     *
     * @param scopeURL No description
     */
    public suspend fun startWorker(scopeURL: String) {
        val parameter = StartWorkerParameter(scopeURL = scopeURL)
        startWorker(parameter)
    }

    public suspend fun stopAllWorkers(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("ServiceWorker.stopAllWorkers", parameter, mode)
    }

    public suspend fun stopWorker(args: StopWorkerParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("ServiceWorker.stopWorker", parameter, mode)
    }

    /**
     *
     *
     * @param versionId No description
     */
    public suspend fun stopWorker(versionId: String) {
        val parameter = StopWorkerParameter(versionId = versionId)
        stopWorker(parameter)
    }

    public suspend fun unregister(args: UnregisterParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("ServiceWorker.unregister", parameter, mode)
    }

    /**
     *
     *
     * @param scopeURL No description
     */
    public suspend fun unregister(scopeURL: String) {
        val parameter = UnregisterParameter(scopeURL = scopeURL)
        unregister(parameter)
    }

    public suspend fun updateRegistration(args: UpdateRegistrationParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("ServiceWorker.updateRegistration", parameter, mode)
    }

    /**
     *
     *
     * @param scopeURL No description
     */
    public suspend fun updateRegistration(scopeURL: String) {
        val parameter = UpdateRegistrationParameter(scopeURL = scopeURL)
        updateRegistration(parameter)
    }

    /**
     * ServiceWorker registration.
     */
    @Serializable
    public data class ServiceWorkerRegistration(
        public val registrationId: String,
        public val scopeURL: String,
        public val isDeleted: Boolean,
    )

    @Serializable
    public enum class ServiceWorkerVersionRunningStatus {
        @SerialName("stopped")
        STOPPED,

        @SerialName("starting")
        STARTING,

        @SerialName("running")
        RUNNING,

        @SerialName("stopping")
        STOPPING,
    }

    @Serializable
    public enum class ServiceWorkerVersionStatus {
        @SerialName("new")
        NEW,

        @SerialName("installing")
        INSTALLING,

        @SerialName("installed")
        INSTALLED,

        @SerialName("activating")
        ACTIVATING,

        @SerialName("activated")
        ACTIVATED,

        @SerialName("redundant")
        REDUNDANT,
    }

    /**
     * ServiceWorker version.
     */
    @Serializable
    public data class ServiceWorkerVersion(
        public val versionId: String,
        public val registrationId: String,
        public val scriptURL: String,
        public val runningStatus: ServiceWorkerVersionRunningStatus,
        public val status: ServiceWorkerVersionStatus,
        /**
         * The Last-Modified header value of the main script.
         */
        public val scriptLastModified: Double? = null,
        /**
         * The time at which the response headers of the main script were received from the server.
         * For cached script it is the last time the cache entry was validated.
         */
        public val scriptResponseTime: Double? = null,
        public val controlledClients: List<String>? = null,
        public val targetId: String? = null,
        public val routerRules: String? = null,
    )

    /**
     * ServiceWorker error message.
     */
    @Serializable
    public data class ServiceWorkerErrorMessage(
        public val errorMessage: String,
        public val registrationId: String,
        public val versionId: String,
        public val sourceURL: String,
        public val lineNumber: Int,
        public val columnNumber: Int,
    )

    @Serializable
    public data class WorkerErrorReportedParameter(
        public val errorMessage: ServiceWorkerErrorMessage,
    )

    @Serializable
    public data class WorkerRegistrationUpdatedParameter(
        public val registrations: List<ServiceWorkerRegistration>,
    )

    @Serializable
    public data class WorkerVersionUpdatedParameter(
        public val versions: List<ServiceWorkerVersion>,
    )

    @Serializable
    public data class DeliverPushMessageParameter(
        public val origin: String,
        public val registrationId: String,
        public val `data`: String,
    )

    @Serializable
    public data class DispatchSyncEventParameter(
        public val origin: String,
        public val registrationId: String,
        public val tag: String,
        public val lastChance: Boolean,
    )

    @Serializable
    public data class DispatchPeriodicSyncEventParameter(
        public val origin: String,
        public val registrationId: String,
        public val tag: String,
    )

    @Serializable
    public data class SetForceUpdateOnPageLoadParameter(
        public val forceUpdateOnPageLoad: Boolean,
    )

    @Serializable
    public data class SkipWaitingParameter(
        public val scopeURL: String,
    )

    @Serializable
    public data class StartWorkerParameter(
        public val scopeURL: String,
    )

    @Serializable
    public data class StopWorkerParameter(
        public val versionId: String,
    )

    @Serializable
    public data class UnregisterParameter(
        public val scopeURL: String,
    )

    @Serializable
    public data class UpdateRegistrationParameter(
        public val scopeURL: String,
    )
}
