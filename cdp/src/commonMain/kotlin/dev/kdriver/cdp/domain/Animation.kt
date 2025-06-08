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
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.animation: Animation
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Animation(this))

public class Animation(
    private val cdp: CDP,
) : Domain {
    /**
     * Event for when an animation has been cancelled.
     */
    public val animationCanceled: Flow<AnimationCanceledParameter> = cdp
        .events
        .filter { it.method == "Animation.animationCanceled" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Event for each animation that has been created.
     */
    public val animationCreated: Flow<AnimationCreatedParameter> = cdp
        .events
        .filter { it.method == "Animation.animationCreated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Event for animation that has been started.
     */
    public val animationStarted: Flow<AnimationStartedParameter> = cdp
        .events
        .filter { it.method == "Animation.animationStarted" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Disables animation domain notifications.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Animation.disable", parameter)
    }

    /**
     * Enables animation domain notifications.
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("Animation.enable", parameter)
    }

    /**
     * Returns the current time of the an animation.
     */
    public suspend fun getCurrentTime(args: GetCurrentTimeParameter): GetCurrentTimeReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Animation.getCurrentTime", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the current time of the an animation.
     */
    public suspend fun getCurrentTime(id: String): GetCurrentTimeReturn {
        val parameter = GetCurrentTimeParameter(id = id)
        return getCurrentTime(parameter)
    }

    /**
     * Gets the playback rate of the document timeline.
     */
    public suspend fun getPlaybackRate(): GetPlaybackRateReturn {
        val parameter = null
        val result = cdp.callCommand("Animation.getPlaybackRate", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Releases a set of animations to no longer be manipulated.
     */
    public suspend fun releaseAnimations(args: ReleaseAnimationsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Animation.releaseAnimations", parameter)
    }

    /**
     * Releases a set of animations to no longer be manipulated.
     */
    public suspend fun releaseAnimations(animations: List<String>) {
        val parameter = ReleaseAnimationsParameter(animations = animations)
        releaseAnimations(parameter)
    }

    /**
     * Gets the remote object of the Animation.
     */
    public suspend fun resolveAnimation(args: ResolveAnimationParameter): ResolveAnimationReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Animation.resolveAnimation", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets the remote object of the Animation.
     */
    public suspend fun resolveAnimation(animationId: String): ResolveAnimationReturn {
        val parameter = ResolveAnimationParameter(animationId = animationId)
        return resolveAnimation(parameter)
    }

    /**
     * Seek a set of animations to a particular time within each animation.
     */
    public suspend fun seekAnimations(args: SeekAnimationsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Animation.seekAnimations", parameter)
    }

    /**
     * Seek a set of animations to a particular time within each animation.
     */
    public suspend fun seekAnimations(animations: List<String>, currentTime: Double) {
        val parameter = SeekAnimationsParameter(animations = animations, currentTime = currentTime)
        seekAnimations(parameter)
    }

    /**
     * Sets the paused state of a set of animations.
     */
    public suspend fun setPaused(args: SetPausedParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Animation.setPaused", parameter)
    }

    /**
     * Sets the paused state of a set of animations.
     */
    public suspend fun setPaused(animations: List<String>, paused: Boolean) {
        val parameter = SetPausedParameter(animations = animations, paused = paused)
        setPaused(parameter)
    }

    /**
     * Sets the playback rate of the document timeline.
     */
    public suspend fun setPlaybackRate(args: SetPlaybackRateParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Animation.setPlaybackRate", parameter)
    }

    /**
     * Sets the playback rate of the document timeline.
     */
    public suspend fun setPlaybackRate(playbackRate: Double) {
        val parameter = SetPlaybackRateParameter(playbackRate = playbackRate)
        setPlaybackRate(parameter)
    }

    /**
     * Sets the timing of an animation node.
     */
    public suspend fun setTiming(args: SetTimingParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Animation.setTiming", parameter)
    }

    /**
     * Sets the timing of an animation node.
     */
    public suspend fun setTiming(
        animationId: String,
        duration: Double,
        delay: Double,
    ) {
        val parameter = SetTimingParameter(animationId = animationId, duration = duration, delay = delay)
        setTiming(parameter)
    }

    /**
     * Animation instance.
     */
    @Serializable
    public data class Animation(
        /**
         * `Animation`'s id.
         */
        public val id: String,
        /**
         * `Animation`'s name.
         */
        public val name: String,
        /**
         * `Animation`'s internal paused state.
         */
        public val pausedState: Boolean,
        /**
         * `Animation`'s play state.
         */
        public val playState: String,
        /**
         * `Animation`'s playback rate.
         */
        public val playbackRate: Double,
        /**
         * `Animation`'s start time.
         */
        public val startTime: Double,
        /**
         * `Animation`'s current time.
         */
        public val currentTime: Double,
        /**
         * Animation type of `Animation`.
         */
        public val type: String,
        /**
         * `Animation`'s source animation node.
         */
        public val source: AnimationEffect? = null,
        /**
         * A unique ID for `Animation` representing the sources that triggered this CSS
         * animation/transition.
         */
        public val cssId: String? = null,
    )

    /**
     * AnimationEffect instance
     */
    @Serializable
    public data class AnimationEffect(
        /**
         * `AnimationEffect`'s delay.
         */
        public val delay: Double,
        /**
         * `AnimationEffect`'s end delay.
         */
        public val endDelay: Double,
        /**
         * `AnimationEffect`'s iteration start.
         */
        public val iterationStart: Double,
        /**
         * `AnimationEffect`'s iterations.
         */
        public val iterations: Double,
        /**
         * `AnimationEffect`'s iteration duration.
         */
        public val duration: Double,
        /**
         * `AnimationEffect`'s playback direction.
         */
        public val direction: String,
        /**
         * `AnimationEffect`'s fill mode.
         */
        public val fill: String,
        /**
         * `AnimationEffect`'s target node.
         */
        public val backendNodeId: Int? = null,
        /**
         * `AnimationEffect`'s keyframes.
         */
        public val keyframesRule: KeyframesRule? = null,
        /**
         * `AnimationEffect`'s timing function.
         */
        public val easing: String,
    )

    /**
     * Keyframes Rule
     */
    @Serializable
    public data class KeyframesRule(
        /**
         * CSS keyframed animation's name.
         */
        public val name: String? = null,
        /**
         * List of animation keyframes.
         */
        public val keyframes: List<KeyframeStyle>,
    )

    /**
     * Keyframe Style
     */
    @Serializable
    public data class KeyframeStyle(
        /**
         * Keyframe's time offset.
         */
        public val offset: String,
        /**
         * `AnimationEffect`'s timing function.
         */
        public val easing: String,
    )

    /**
     * Event for when an animation has been cancelled.
     */
    @Serializable
    public data class AnimationCanceledParameter(
        /**
         * Id of the animation that was cancelled.
         */
        public val id: String,
    )

    /**
     * Event for each animation that has been created.
     */
    @Serializable
    public data class AnimationCreatedParameter(
        /**
         * Id of the animation that was created.
         */
        public val id: String,
    )

    /**
     * Event for animation that has been started.
     */
    @Serializable
    public data class AnimationStartedParameter(
        /**
         * Animation that was started.
         */
        public val animation: Animation,
    )

    @Serializable
    public data class GetCurrentTimeParameter(
        /**
         * Id of animation.
         */
        public val id: String,
    )

    @Serializable
    public data class GetCurrentTimeReturn(
        /**
         * Current time of the page.
         */
        public val currentTime: Double,
    )

    @Serializable
    public data class GetPlaybackRateReturn(
        /**
         * Playback rate for animations on page.
         */
        public val playbackRate: Double,
    )

    @Serializable
    public data class ReleaseAnimationsParameter(
        /**
         * List of animation ids to seek.
         */
        public val animations: List<String>,
    )

    @Serializable
    public data class ResolveAnimationParameter(
        /**
         * Animation id.
         */
        public val animationId: String,
    )

    @Serializable
    public data class ResolveAnimationReturn(
        /**
         * Corresponding remote object.
         */
        public val remoteObject: Runtime.RemoteObject,
    )

    @Serializable
    public data class SeekAnimationsParameter(
        /**
         * List of animation ids to seek.
         */
        public val animations: List<String>,
        /**
         * Set the current time of each animation.
         */
        public val currentTime: Double,
    )

    @Serializable
    public data class SetPausedParameter(
        /**
         * Animations to set the pause state of.
         */
        public val animations: List<String>,
        /**
         * Paused state to set to.
         */
        public val paused: Boolean,
    )

    @Serializable
    public data class SetPlaybackRateParameter(
        /**
         * Playback rate for animations on page
         */
        public val playbackRate: Double,
    )

    @Serializable
    public data class SetTimingParameter(
        /**
         * Animation id.
         */
        public val animationId: String,
        /**
         * Duration of the animation.
         */
        public val duration: Double,
        /**
         * Delay of the animation.
         */
        public val delay: Double,
    )
}
