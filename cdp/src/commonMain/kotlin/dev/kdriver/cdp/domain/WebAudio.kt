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

public val CDP.webAudio: WebAudio
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(WebAudio(this))

/**
 * This domain allows inspection of Web Audio API.
 * https://webaudio.github.io/web-audio-api/
 */
public class WebAudio(
    private val cdp: CDP,
) : Domain {
    public val contextCreated: Flow<ContextCreatedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.contextCreated"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val contextWillBeDestroyed: Flow<ContextWillBeDestroyedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.contextWillBeDestroyed"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val contextChanged: Flow<ContextChangedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.contextChanged"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val audioListenerCreated: Flow<AudioListenerCreatedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.audioListenerCreated"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val audioListenerWillBeDestroyed: Flow<AudioListenerWillBeDestroyedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.audioListenerWillBeDestroyed"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val audioNodeCreated: Flow<AudioNodeCreatedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.audioNodeCreated"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val audioNodeWillBeDestroyed: Flow<AudioNodeWillBeDestroyedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.audioNodeWillBeDestroyed"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val audioParamCreated: Flow<AudioParamCreatedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.audioParamCreated"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val audioParamWillBeDestroyed: Flow<AudioParamWillBeDestroyedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.audioParamWillBeDestroyed"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val nodesConnected: Flow<NodesConnectedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.nodesConnected"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val nodesDisconnected: Flow<NodesDisconnectedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.nodesDisconnected"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val nodeParamConnected: Flow<NodeParamConnectedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.nodeParamConnected"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val nodeParamDisconnected: Flow<NodeParamDisconnectedParameter> = cdp
        .events
        .filter {
            it.method == "WebAudio.nodeParamDisconnected"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    /**
     * Enables the WebAudio domain and starts sending context lifetime events.
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("WebAudio.enable", parameter)
    }

    /**
     * Disables the WebAudio domain.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("WebAudio.disable", parameter)
    }

    /**
     * Fetch the realtime data from the registered contexts.
     */
    public suspend fun getRealtimeData(args: GetRealtimeDataParameter): GetRealtimeDataReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("WebAudio.getRealtimeData", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Fetch the realtime data from the registered contexts.
     */
    public suspend fun getRealtimeData(contextId: String): GetRealtimeDataReturn {
        val parameter = GetRealtimeDataParameter(contextId = contextId)
        return getRealtimeData(parameter)
    }

    /**
     * Enum of BaseAudioContext types
     */
    @Serializable
    public enum class ContextType {
        @SerialName("realtime")
        REALTIME,

        @SerialName("offline")
        OFFLINE,
    }

    /**
     * Enum of AudioContextState from the spec
     */
    @Serializable
    public enum class ContextState {
        @SerialName("suspended")
        SUSPENDED,

        @SerialName("running")
        RUNNING,

        @SerialName("closed")
        CLOSED,
    }

    /**
     * Enum of AudioNode::ChannelCountMode from the spec
     */
    @Serializable
    public enum class ChannelCountMode {
        @SerialName("clamped-max")
        CLAMPED_MAX,

        @SerialName("explicit")
        EXPLICIT,

        @SerialName("max")
        MAX,
    }

    /**
     * Enum of AudioNode::ChannelInterpretation from the spec
     */
    @Serializable
    public enum class ChannelInterpretation {
        @SerialName("discrete")
        DISCRETE,

        @SerialName("speakers")
        SPEAKERS,
    }

    /**
     * Enum of AudioParam::AutomationRate from the spec
     */
    @Serializable
    public enum class AutomationRate {
        @SerialName("a-rate")
        A_RATE,

        @SerialName("k-rate")
        K_RATE,
    }

    /**
     * Fields in AudioContext that change in real-time.
     */
    @Serializable
    public data class ContextRealtimeData(
        /**
         * The current context time in second in BaseAudioContext.
         */
        public val currentTime: Double,
        /**
         * The time spent on rendering graph divided by render quantum duration,
         * and multiplied by 100. 100 means the audio renderer reached the full
         * capacity and glitch may occur.
         */
        public val renderCapacity: Double,
        /**
         * A running mean of callback interval.
         */
        public val callbackIntervalMean: Double,
        /**
         * A running variance of callback interval.
         */
        public val callbackIntervalVariance: Double,
    )

    /**
     * Protocol object for BaseAudioContext
     */
    @Serializable
    public data class BaseAudioContext(
        public val contextId: String,
        public val contextType: ContextType,
        public val contextState: ContextState,
        public val realtimeData: ContextRealtimeData? = null,
        /**
         * Platform-dependent callback buffer size.
         */
        public val callbackBufferSize: Double,
        /**
         * Number of output channels supported by audio hardware in use.
         */
        public val maxOutputChannelCount: Double,
        /**
         * Context sample rate.
         */
        public val sampleRate: Double,
    )

    /**
     * Protocol object for AudioListener
     */
    @Serializable
    public data class AudioListener(
        public val listenerId: String,
        public val contextId: String,
    )

    /**
     * Protocol object for AudioNode
     */
    @Serializable
    public data class AudioNode(
        public val nodeId: String,
        public val contextId: String,
        public val nodeType: String,
        public val numberOfInputs: Double,
        public val numberOfOutputs: Double,
        public val channelCount: Double,
        public val channelCountMode: ChannelCountMode,
        public val channelInterpretation: ChannelInterpretation,
    )

    /**
     * Protocol object for AudioParam
     */
    @Serializable
    public data class AudioParam(
        public val paramId: String,
        public val nodeId: String,
        public val contextId: String,
        public val paramType: String,
        public val rate: AutomationRate,
        public val defaultValue: Double,
        public val minValue: Double,
        public val maxValue: Double,
    )

    /**
     * Notifies that a new BaseAudioContext has been created.
     */
    @Serializable
    public data class ContextCreatedParameter(
        public val context: BaseAudioContext,
    )

    /**
     * Notifies that an existing BaseAudioContext will be destroyed.
     */
    @Serializable
    public data class ContextWillBeDestroyedParameter(
        public val contextId: String,
    )

    /**
     * Notifies that existing BaseAudioContext has changed some properties (id stays the same)..
     */
    @Serializable
    public data class ContextChangedParameter(
        public val context: BaseAudioContext,
    )

    /**
     * Notifies that the construction of an AudioListener has finished.
     */
    @Serializable
    public data class AudioListenerCreatedParameter(
        public val listener: AudioListener,
    )

    /**
     * Notifies that a new AudioListener has been created.
     */
    @Serializable
    public data class AudioListenerWillBeDestroyedParameter(
        public val contextId: String,
        public val listenerId: String,
    )

    /**
     * Notifies that a new AudioNode has been created.
     */
    @Serializable
    public data class AudioNodeCreatedParameter(
        public val node: AudioNode,
    )

    /**
     * Notifies that an existing AudioNode has been destroyed.
     */
    @Serializable
    public data class AudioNodeWillBeDestroyedParameter(
        public val contextId: String,
        public val nodeId: String,
    )

    /**
     * Notifies that a new AudioParam has been created.
     */
    @Serializable
    public data class AudioParamCreatedParameter(
        public val `param`: AudioParam,
    )

    /**
     * Notifies that an existing AudioParam has been destroyed.
     */
    @Serializable
    public data class AudioParamWillBeDestroyedParameter(
        public val contextId: String,
        public val nodeId: String,
        public val paramId: String,
    )

    /**
     * Notifies that two AudioNodes are connected.
     */
    @Serializable
    public data class NodesConnectedParameter(
        public val contextId: String,
        public val sourceId: String,
        public val destinationId: String,
        public val sourceOutputIndex: Double? = null,
        public val destinationInputIndex: Double? = null,
    )

    /**
     * Notifies that AudioNodes are disconnected. The destination can be null, and it means all the outgoing connections from the source are disconnected.
     */
    @Serializable
    public data class NodesDisconnectedParameter(
        public val contextId: String,
        public val sourceId: String,
        public val destinationId: String,
        public val sourceOutputIndex: Double? = null,
        public val destinationInputIndex: Double? = null,
    )

    /**
     * Notifies that an AudioNode is connected to an AudioParam.
     */
    @Serializable
    public data class NodeParamConnectedParameter(
        public val contextId: String,
        public val sourceId: String,
        public val destinationId: String,
        public val sourceOutputIndex: Double? = null,
    )

    /**
     * Notifies that an AudioNode is disconnected to an AudioParam.
     */
    @Serializable
    public data class NodeParamDisconnectedParameter(
        public val contextId: String,
        public val sourceId: String,
        public val destinationId: String,
        public val sourceOutputIndex: Double? = null,
    )

    @Serializable
    public data class GetRealtimeDataParameter(
        public val contextId: String,
    )

    @Serializable
    public data class GetRealtimeDataReturn(
        public val realtimeData: ContextRealtimeData,
    )
}
