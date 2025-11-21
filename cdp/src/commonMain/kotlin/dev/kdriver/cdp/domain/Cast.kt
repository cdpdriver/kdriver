@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kdriver.cdp.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * A domain for interacting with Cast, Presentation API, and Remote Playback API
 * functionalities.
 */
public val CDP.cast: Cast
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Cast(this))

/**
 * A domain for interacting with Cast, Presentation API, and Remote Playback API
 * functionalities.
 */
public class Cast(
    private val cdp: CDP,
) : Domain {
    /**
     * This is fired whenever the list of available sinks changes. A sink is a
     * device or a software surface that you can cast to.
     */
    public val sinksUpdated: Flow<SinksUpdatedParameter> = cdp
        .events
        .filter { it.method == "Cast.sinksUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * This is fired whenever the outstanding issue/error message changes.
     * |issueMessage| is empty if there is no issue.
     */
    public val issueUpdated: Flow<IssueUpdatedParameter> = cdp
        .events
        .filter { it.method == "Cast.issueUpdated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Starts observing for sinks that can be used for tab mirroring, and if set,
     * sinks compatible with |presentationUrl| as well. When sinks are found, a
     * |sinksUpdated| event is fired.
     * Also starts observing for issue messages. When an issue is added or removed,
     * an |issueUpdated| event is fired.
     */
    public suspend fun enable(args: EnableParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Cast.enable", parameter, mode)
    }

    /**
     * Starts observing for sinks that can be used for tab mirroring, and if set,
     * sinks compatible with |presentationUrl| as well. When sinks are found, a
     * |sinksUpdated| event is fired.
     * Also starts observing for issue messages. When an issue is added or removed,
     * an |issueUpdated| event is fired.
     *
     * @param presentationUrl No description
     */
    public suspend fun enable(presentationUrl: String? = null) {
        val parameter = EnableParameter(presentationUrl = presentationUrl)
        enable(parameter)
    }

    /**
     * Stops observing for sinks and issues.
     */
    public suspend fun disable(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Cast.disable", parameter, mode)
    }

    /**
     * Sets a sink to be used when the web page requests the browser to choose a
     * sink via Presentation API, Remote Playback API, or Cast SDK.
     */
    public suspend fun setSinkToUse(args: SetSinkToUseParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Cast.setSinkToUse", parameter, mode)
    }

    /**
     * Sets a sink to be used when the web page requests the browser to choose a
     * sink via Presentation API, Remote Playback API, or Cast SDK.
     *
     * @param sinkName No description
     */
    public suspend fun setSinkToUse(sinkName: String) {
        val parameter = SetSinkToUseParameter(sinkName = sinkName)
        setSinkToUse(parameter)
    }

    /**
     * Starts mirroring the desktop to the sink.
     */
    public suspend fun startDesktopMirroring(
        args: StartDesktopMirroringParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Cast.startDesktopMirroring", parameter, mode)
    }

    /**
     * Starts mirroring the desktop to the sink.
     *
     * @param sinkName No description
     */
    public suspend fun startDesktopMirroring(sinkName: String) {
        val parameter = StartDesktopMirroringParameter(sinkName = sinkName)
        startDesktopMirroring(parameter)
    }

    /**
     * Starts mirroring the tab to the sink.
     */
    public suspend fun startTabMirroring(args: StartTabMirroringParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Cast.startTabMirroring", parameter, mode)
    }

    /**
     * Starts mirroring the tab to the sink.
     *
     * @param sinkName No description
     */
    public suspend fun startTabMirroring(sinkName: String) {
        val parameter = StartTabMirroringParameter(sinkName = sinkName)
        startTabMirroring(parameter)
    }

    /**
     * Stops the active Cast session on the sink.
     */
    public suspend fun stopCasting(args: StopCastingParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Cast.stopCasting", parameter, mode)
    }

    /**
     * Stops the active Cast session on the sink.
     *
     * @param sinkName No description
     */
    public suspend fun stopCasting(sinkName: String) {
        val parameter = StopCastingParameter(sinkName = sinkName)
        stopCasting(parameter)
    }

    @Serializable
    public data class Sink(
        public val name: String,
        public val id: String,
        /**
         * Text describing the current session. Present only if there is an active
         * session on the sink.
         */
        public val session: String? = null,
    )

    /**
     * This is fired whenever the list of available sinks changes. A sink is a
     * device or a software surface that you can cast to.
     */
    @Serializable
    public data class SinksUpdatedParameter(
        public val sinks: List<Sink>,
    )

    /**
     * This is fired whenever the outstanding issue/error message changes.
     * |issueMessage| is empty if there is no issue.
     */
    @Serializable
    public data class IssueUpdatedParameter(
        public val issueMessage: String,
    )

    @Serializable
    public data class EnableParameter(
        public val presentationUrl: String? = null,
    )

    @Serializable
    public data class SetSinkToUseParameter(
        public val sinkName: String,
    )

    @Serializable
    public data class StartDesktopMirroringParameter(
        public val sinkName: String,
    )

    @Serializable
    public data class StartTabMirroringParameter(
        public val sinkName: String,
    )

    @Serializable
    public data class StopCastingParameter(
        public val sinkName: String,
    )
}
