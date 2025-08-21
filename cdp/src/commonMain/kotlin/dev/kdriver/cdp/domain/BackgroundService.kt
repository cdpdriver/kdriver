@file:Suppress("ALL")

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

/**
 * Defines events for background web platform features.
 */
public val CDP.backgroundService: BackgroundService
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(BackgroundService(this))

/**
 * Defines events for background web platform features.
 */
public class BackgroundService(
    private val cdp: CDP,
) : Domain {
    /**
     * Called when the recording state for the service has been updated.
     */
    public val recordingStateChanged: Flow<RecordingStateChangedParameter> = cdp
        .events
        .filter { it.method == "BackgroundService.recordingStateChanged" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Called with all existing backgroundServiceEvents when enabled, and all new
     * events afterwards if enabled and recording.
     */
    public val backgroundServiceEventReceived: Flow<BackgroundServiceEventReceivedParameter> = cdp
        .events
        .filter { it.method == "BackgroundService.backgroundServiceEventReceived" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Enables event updates for the service.
     */
    public suspend fun startObserving(args: StartObservingParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BackgroundService.startObserving", parameter, mode)
    }

    /**
     * Enables event updates for the service.
     *
     * @param service No description
     */
    public suspend fun startObserving(service: ServiceName) {
        val parameter = StartObservingParameter(service = service)
        startObserving(parameter)
    }

    /**
     * Disables event updates for the service.
     */
    public suspend fun stopObserving(args: StopObservingParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BackgroundService.stopObserving", parameter, mode)
    }

    /**
     * Disables event updates for the service.
     *
     * @param service No description
     */
    public suspend fun stopObserving(service: ServiceName) {
        val parameter = StopObservingParameter(service = service)
        stopObserving(parameter)
    }

    /**
     * Set the recording state for the service.
     */
    public suspend fun setRecording(args: SetRecordingParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BackgroundService.setRecording", parameter, mode)
    }

    /**
     * Set the recording state for the service.
     *
     * @param shouldRecord No description
     * @param service No description
     */
    public suspend fun setRecording(shouldRecord: Boolean, service: ServiceName) {
        val parameter = SetRecordingParameter(shouldRecord = shouldRecord, service = service)
        setRecording(parameter)
    }

    /**
     * Clears all stored data for the service.
     */
    public suspend fun clearEvents(args: ClearEventsParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("BackgroundService.clearEvents", parameter, mode)
    }

    /**
     * Clears all stored data for the service.
     *
     * @param service No description
     */
    public suspend fun clearEvents(service: ServiceName) {
        val parameter = ClearEventsParameter(service = service)
        clearEvents(parameter)
    }

    /**
     * The Background Service that will be associated with the commands/events.
     * Every Background Service operates independently, but they share the same
     * API.
     */
    @Serializable
    public enum class ServiceName {
        @SerialName("backgroundFetch")
        BACKGROUNDFETCH,

        @SerialName("backgroundSync")
        BACKGROUNDSYNC,

        @SerialName("pushMessaging")
        PUSHMESSAGING,

        @SerialName("notifications")
        NOTIFICATIONS,

        @SerialName("paymentHandler")
        PAYMENTHANDLER,

        @SerialName("periodicBackgroundSync")
        PERIODICBACKGROUNDSYNC,
    }

    /**
     * A key-value pair for additional event information to pass along.
     */
    @Serializable
    public data class EventMetadata(
        public val key: String,
        public val `value`: String,
    )

    @Serializable
    public data class BackgroundServiceEvent(
        /**
         * Timestamp of the event (in seconds).
         */
        public val timestamp: Double,
        /**
         * The origin this event belongs to.
         */
        public val origin: String,
        /**
         * The Service Worker ID that initiated the event.
         */
        public val serviceWorkerRegistrationId: String,
        /**
         * The Background Service this event belongs to.
         */
        public val service: ServiceName,
        /**
         * A description of the event.
         */
        public val eventName: String,
        /**
         * An identifier that groups related events together.
         */
        public val instanceId: String,
        /**
         * A list of event-specific information.
         */
        public val eventMetadata: List<EventMetadata>,
        /**
         * Storage key this event belongs to.
         */
        public val storageKey: String,
    )

    /**
     * Called when the recording state for the service has been updated.
     */
    @Serializable
    public data class RecordingStateChangedParameter(
        public val isRecording: Boolean,
        public val service: ServiceName,
    )

    /**
     * Called with all existing backgroundServiceEvents when enabled, and all new
     * events afterwards if enabled and recording.
     */
    @Serializable
    public data class BackgroundServiceEventReceivedParameter(
        public val backgroundServiceEvent: BackgroundServiceEvent,
    )

    @Serializable
    public data class StartObservingParameter(
        public val service: ServiceName,
    )

    @Serializable
    public data class StopObservingParameter(
        public val service: ServiceName,
    )

    @Serializable
    public data class SetRecordingParameter(
        public val shouldRecord: Boolean,
        public val service: ServiceName,
    )

    @Serializable
    public data class ClearEventsParameter(
        public val service: ServiceName,
    )
}
