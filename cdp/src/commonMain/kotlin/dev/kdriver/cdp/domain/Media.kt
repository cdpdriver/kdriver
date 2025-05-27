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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

public val CDP.media: Media
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Media(this))

/**
 * This domain allows detailed inspection of media elements
 */
public class Media(
    private val cdp: CDP,
) : Domain {
    public val playerPropertiesChanged: Flow<PlayerPropertiesChangedParameter> = cdp
        .events
        .filter {
            it.method == "Media.playerPropertiesChanged"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val playerEventsAdded: Flow<PlayerEventsAddedParameter> = cdp
        .events
        .filter {
            it.method == "Media.playerEventsAdded"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val playerMessagesLogged: Flow<PlayerMessagesLoggedParameter> = cdp
        .events
        .filter {
            it.method == "Media.playerMessagesLogged"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val playerErrorsRaised: Flow<PlayerErrorsRaisedParameter> = cdp
        .events
        .filter {
            it.method == "Media.playerErrorsRaised"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val playersCreated: Flow<PlayersCreatedParameter> = cdp
        .events
        .filter {
            it.method == "Media.playersCreated"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    /**
     * Enables the Media domain
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("Media.enable", parameter)
    }

    /**
     * Disables the Media domain.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Media.disable", parameter)
    }

    /**
     * Have one type per entry in MediaLogRecord::Type
     * Corresponds to kMessage
     */
    @Serializable
    public data class PlayerMessage(
        /**
         * Keep in sync with MediaLogMessageLevel
         * We are currently keeping the message level 'error' separate from the
         * PlayerError type because right now they represent different things,
         * this one being a DVLOG(ERROR) style log message that gets printed
         * based on what log level is selected in the UI, and the other is a
         * representation of a media::PipelineStatus object. Soon however we're
         * going to be moving away from using PipelineStatus for errors and
         * introducing a new error type which should hopefully let us integrate
         * the error log level into the PlayerError type.
         */
        public val level: String,
        public val message: String,
    )

    /**
     * Corresponds to kMediaPropertyChange
     */
    @Serializable
    public data class PlayerProperty(
        public val name: String,
        public val `value`: String,
    )

    /**
     * Corresponds to kMediaEventTriggered
     */
    @Serializable
    public data class PlayerEvent(
        public val timestamp: Double,
        public val `value`: String,
    )

    /**
     * Represents logged source line numbers reported in an error.
     * NOTE: file and line are from chromium c++ implementation code, not js.
     */
    @Serializable
    public data class PlayerErrorSourceLocation(
        public val `file`: String,
        public val line: Int,
    )

    /**
     * Corresponds to kMediaError
     */
    @Serializable
    public data class PlayerError(
        public val errorType: String,
        /**
         * Code is the numeric enum entry for a specific set of error codes, such
         * as PipelineStatusCodes in media/base/pipeline_status.h
         */
        public val code: Int,
        /**
         * A trace of where this error was caused / where it passed through.
         */
        public val stack: List<PlayerErrorSourceLocation>,
        /**
         * Errors potentially have a root cause error, ie, a DecoderError might be
         * caused by an WindowsError
         */
        public val cause: List<PlayerError>,
        /**
         * Extra data attached to an error, such as an HRESULT, Video Codec, etc.
         */
        public val `data`: Map<String, JsonElement>,
    )

    /**
     * This can be called multiple times, and can be used to set / override /
     * remove player properties. A null propValue indicates removal.
     */
    @Serializable
    public data class PlayerPropertiesChangedParameter(
        public val playerId: String,
        public val properties: List<PlayerProperty>,
    )

    /**
     * Send events as a list, allowing them to be batched on the browser for less
     * congestion. If batched, events must ALWAYS be in chronological order.
     */
    @Serializable
    public data class PlayerEventsAddedParameter(
        public val playerId: String,
        public val events: List<PlayerEvent>,
    )

    /**
     * Send a list of any messages that need to be delivered.
     */
    @Serializable
    public data class PlayerMessagesLoggedParameter(
        public val playerId: String,
        public val messages: List<PlayerMessage>,
    )

    /**
     * Send a list of any errors that need to be delivered.
     */
    @Serializable
    public data class PlayerErrorsRaisedParameter(
        public val playerId: String,
        public val errors: List<PlayerError>,
    )

    /**
     * Called whenever a player is created, or when a new agent joins and receives
     * a list of active players. If an agent is restored, it will receive the full
     * list of player ids and all events again.
     */
    @Serializable
    public data class PlayersCreatedParameter(
        public val players: List<String>,
    )
}
