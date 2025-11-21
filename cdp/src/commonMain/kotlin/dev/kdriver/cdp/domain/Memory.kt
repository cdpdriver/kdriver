@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kdriver.cdp.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.memory: Memory
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Memory(this))

public class Memory(
    private val cdp: CDP,
) : Domain {
    /**
     * Retruns current DOM object counters.
     */
    public suspend fun getDOMCounters(mode: CommandMode = CommandMode.DEFAULT): GetDOMCountersReturn {
        val parameter = null
        val result = cdp.callCommand("Memory.getDOMCounters", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Retruns DOM object counters after preparing renderer for leak detection.
     */
    public suspend fun getDOMCountersForLeakDetection(mode: CommandMode = CommandMode.DEFAULT): GetDOMCountersForLeakDetectionReturn {
        val parameter = null
        val result = cdp.callCommand("Memory.getDOMCountersForLeakDetection", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Prepares for leak detection by terminating workers, stopping spellcheckers,
     * dropping non-essential internal caches, running garbage collections, etc.
     */
    public suspend fun prepareForLeakDetection(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Memory.prepareForLeakDetection", parameter, mode)
    }

    /**
     * Simulate OomIntervention by purging V8 memory.
     */
    public suspend fun forciblyPurgeJavaScriptMemory(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Memory.forciblyPurgeJavaScriptMemory", parameter, mode)
    }

    /**
     * Enable/disable suppressing memory pressure notifications in all processes.
     */
    public suspend fun setPressureNotificationsSuppressed(
        args: SetPressureNotificationsSuppressedParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Memory.setPressureNotificationsSuppressed", parameter, mode)
    }

    /**
     * Enable/disable suppressing memory pressure notifications in all processes.
     *
     * @param suppressed If true, memory pressure notifications will be suppressed.
     */
    public suspend fun setPressureNotificationsSuppressed(suppressed: Boolean) {
        val parameter = SetPressureNotificationsSuppressedParameter(suppressed = suppressed)
        setPressureNotificationsSuppressed(parameter)
    }

    /**
     * Simulate a memory pressure notification in all processes.
     */
    public suspend fun simulatePressureNotification(
        args: SimulatePressureNotificationParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Memory.simulatePressureNotification", parameter, mode)
    }

    /**
     * Simulate a memory pressure notification in all processes.
     *
     * @param level Memory pressure level of the notification.
     */
    public suspend fun simulatePressureNotification(level: PressureLevel) {
        val parameter = SimulatePressureNotificationParameter(level = level)
        simulatePressureNotification(parameter)
    }

    /**
     * Start collecting native memory profile.
     */
    public suspend fun startSampling(args: StartSamplingParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Memory.startSampling", parameter, mode)
    }

    /**
     * Start collecting native memory profile.
     *
     * @param samplingInterval Average number of bytes between samples.
     * @param suppressRandomness Do not randomize intervals between samples.
     */
    public suspend fun startSampling(samplingInterval: Int? = null, suppressRandomness: Boolean? = null) {
        val parameter =
            StartSamplingParameter(samplingInterval = samplingInterval, suppressRandomness = suppressRandomness)
        startSampling(parameter)
    }

    /**
     * Stop collecting native memory profile.
     */
    public suspend fun stopSampling(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Memory.stopSampling", parameter, mode)
    }

    /**
     * Retrieve native memory allocations profile
     * collected since renderer process startup.
     */
    public suspend fun getAllTimeSamplingProfile(mode: CommandMode = CommandMode.DEFAULT): GetAllTimeSamplingProfileReturn {
        val parameter = null
        val result = cdp.callCommand("Memory.getAllTimeSamplingProfile", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Retrieve native memory allocations profile
     * collected since browser process startup.
     */
    public suspend fun getBrowserSamplingProfile(mode: CommandMode = CommandMode.DEFAULT): GetBrowserSamplingProfileReturn {
        val parameter = null
        val result = cdp.callCommand("Memory.getBrowserSamplingProfile", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Retrieve native memory allocations profile collected since last
     * `startSampling` call.
     */
    public suspend fun getSamplingProfile(mode: CommandMode = CommandMode.DEFAULT): GetSamplingProfileReturn {
        val parameter = null
        val result = cdp.callCommand("Memory.getSamplingProfile", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Memory pressure level.
     */
    @Serializable
    public enum class PressureLevel {
        @SerialName("moderate")
        MODERATE,

        @SerialName("critical")
        CRITICAL,
    }

    /**
     * Heap profile sample.
     */
    @Serializable
    public data class SamplingProfileNode(
        /**
         * Size of the sampled allocation.
         */
        public val size: Double,
        /**
         * Total bytes attributed to this sample.
         */
        public val total: Double,
        /**
         * Execution stack at the point of allocation.
         */
        public val stack: List<String>,
    )

    /**
     * Array of heap profile samples.
     */
    @Serializable
    public data class SamplingProfile(
        public val samples: List<SamplingProfileNode>,
        public val modules: List<Module>,
    )

    /**
     * Executable module information
     */
    @Serializable
    public data class Module(
        /**
         * Name of the module.
         */
        public val name: String,
        /**
         * UUID of the module.
         */
        public val uuid: String,
        /**
         * Base address where the module is loaded into memory. Encoded as a decimal
         * or hexadecimal (0x prefixed) string.
         */
        public val baseAddress: String,
        /**
         * Size of the module in bytes.
         */
        public val size: Double,
    )

    /**
     * DOM object counter data.
     */
    @Serializable
    public data class DOMCounter(
        /**
         * Object name. Note: object names should be presumed volatile and clients should not expect
         * the returned names to be consistent across runs.
         */
        public val name: String,
        /**
         * Object count.
         */
        public val count: Int,
    )

    @Serializable
    public data class GetDOMCountersReturn(
        public val documents: Int,
        public val nodes: Int,
        public val jsEventListeners: Int,
    )

    @Serializable
    public data class GetDOMCountersForLeakDetectionReturn(
        /**
         * DOM object counters.
         */
        public val counters: List<DOMCounter>,
    )

    @Serializable
    public data class SetPressureNotificationsSuppressedParameter(
        /**
         * If true, memory pressure notifications will be suppressed.
         */
        public val suppressed: Boolean,
    )

    @Serializable
    public data class SimulatePressureNotificationParameter(
        /**
         * Memory pressure level of the notification.
         */
        public val level: PressureLevel,
    )

    @Serializable
    public data class StartSamplingParameter(
        /**
         * Average number of bytes between samples.
         */
        public val samplingInterval: Int? = null,
        /**
         * Do not randomize intervals between samples.
         */
        public val suppressRandomness: Boolean? = null,
    )

    @Serializable
    public data class GetAllTimeSamplingProfileReturn(
        public val profile: SamplingProfile,
    )

    @Serializable
    public data class GetBrowserSamplingProfileReturn(
        public val profile: SamplingProfile,
    )

    @Serializable
    public data class GetSamplingProfileReturn(
        public val profile: SamplingProfile,
    )
}
