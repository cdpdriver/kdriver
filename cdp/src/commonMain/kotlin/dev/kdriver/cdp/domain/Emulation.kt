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
 * This domain emulates different environments for the page.
 */
public val CDP.emulation: Emulation
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Emulation(this))

/**
 * This domain emulates different environments for the page.
 */
public class Emulation(
    private val cdp: CDP,
) : Domain {
    /**
     * Notification sent after the virtual time budget for the current VirtualTimePolicy has run out.
     */
    public val virtualTimeBudgetExpired: Flow<Unit> = cdp
        .events
        .filter { it.method == "Emulation.virtualTimeBudgetExpired" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Tells whether emulation is supported.
     */
    @Deprecated(message = "")
    public suspend fun canEmulate(mode: CommandMode = CommandMode.DEFAULT): CanEmulateReturn {
        val parameter = null
        val result = cdp.callCommand("Emulation.canEmulate", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Clears the overridden device metrics.
     */
    public suspend fun clearDeviceMetricsOverride(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Emulation.clearDeviceMetricsOverride", parameter, mode)
    }

    /**
     * Clears the overridden Geolocation Position and Error.
     */
    public suspend fun clearGeolocationOverride(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Emulation.clearGeolocationOverride", parameter, mode)
    }

    /**
     * Requests that page scale factor is reset to initial values.
     */
    public suspend fun resetPageScaleFactor(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Emulation.resetPageScaleFactor", parameter, mode)
    }

    /**
     * Enables or disables simulating a focused and active page.
     */
    public suspend fun setFocusEmulationEnabled(
        args: SetFocusEmulationEnabledParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setFocusEmulationEnabled", parameter, mode)
    }

    /**
     * Enables or disables simulating a focused and active page.
     *
     * @param enabled Whether to enable to disable focus emulation.
     */
    public suspend fun setFocusEmulationEnabled(enabled: Boolean) {
        val parameter = SetFocusEmulationEnabledParameter(enabled = enabled)
        setFocusEmulationEnabled(parameter)
    }

    /**
     * Automatically render all web contents using a dark theme.
     */
    public suspend fun setAutoDarkModeOverride(
        args: SetAutoDarkModeOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setAutoDarkModeOverride", parameter, mode)
    }

    /**
     * Automatically render all web contents using a dark theme.
     *
     * @param enabled Whether to enable or disable automatic dark mode.
     * If not specified, any existing override will be cleared.
     */
    public suspend fun setAutoDarkModeOverride(enabled: Boolean? = null) {
        val parameter = SetAutoDarkModeOverrideParameter(enabled = enabled)
        setAutoDarkModeOverride(parameter)
    }

    /**
     * Enables CPU throttling to emulate slow CPUs.
     */
    public suspend fun setCPUThrottlingRate(
        args: SetCPUThrottlingRateParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setCPUThrottlingRate", parameter, mode)
    }

    /**
     * Enables CPU throttling to emulate slow CPUs.
     *
     * @param rate Throttling rate as a slowdown factor (1 is no throttle, 2 is 2x slowdown, etc).
     */
    public suspend fun setCPUThrottlingRate(rate: Double) {
        val parameter = SetCPUThrottlingRateParameter(rate = rate)
        setCPUThrottlingRate(parameter)
    }

    /**
     * Sets or clears an override of the default background color of the frame. This override is used
     * if the content does not specify one.
     */
    public suspend fun setDefaultBackgroundColorOverride(
        args: SetDefaultBackgroundColorOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setDefaultBackgroundColorOverride", parameter, mode)
    }

    /**
     * Sets or clears an override of the default background color of the frame. This override is used
     * if the content does not specify one.
     *
     * @param color RGBA of the default background color. If not specified, any existing override will be
     * cleared.
     */
    public suspend fun setDefaultBackgroundColorOverride(color: DOM.RGBA? = null) {
        val parameter = SetDefaultBackgroundColorOverrideParameter(color = color)
        setDefaultBackgroundColorOverride(parameter)
    }

    /**
     * Overrides the values for env(safe-area-inset-*) and env(safe-area-max-inset-*). Unset values will cause the
     * respective variables to be undefined, even if previously overridden.
     */
    public suspend fun setSafeAreaInsetsOverride(
        args: SetSafeAreaInsetsOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setSafeAreaInsetsOverride", parameter, mode)
    }

    /**
     * Overrides the values for env(safe-area-inset-*) and env(safe-area-max-inset-*). Unset values will cause the
     * respective variables to be undefined, even if previously overridden.
     *
     * @param insets No description
     */
    public suspend fun setSafeAreaInsetsOverride(insets: SafeAreaInsets) {
        val parameter = SetSafeAreaInsetsOverrideParameter(insets = insets)
        setSafeAreaInsetsOverride(parameter)
    }

    /**
     * Overrides the values of device screen dimensions (window.screen.width, window.screen.height,
     * window.innerWidth, window.innerHeight, and "device-width"/"device-height"-related CSS media
     * query results).
     */
    public suspend fun setDeviceMetricsOverride(
        args: SetDeviceMetricsOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setDeviceMetricsOverride", parameter, mode)
    }

    /**
     * Overrides the values of device screen dimensions (window.screen.width, window.screen.height,
     * window.innerWidth, window.innerHeight, and "device-width"/"device-height"-related CSS media
     * query results).
     *
     * @param width Overriding width value in pixels (minimum 0, maximum 10000000). 0 disables the override.
     * @param height Overriding height value in pixels (minimum 0, maximum 10000000). 0 disables the override.
     * @param deviceScaleFactor Overriding device scale factor value. 0 disables the override.
     * @param mobile Whether to emulate mobile device. This includes viewport meta tag, overlay scrollbars, text
     * autosizing and more.
     * @param scale Scale to apply to resulting view image.
     * @param screenWidth Overriding screen width value in pixels (minimum 0, maximum 10000000).
     * @param screenHeight Overriding screen height value in pixels (minimum 0, maximum 10000000).
     * @param positionX Overriding view X position on screen in pixels (minimum 0, maximum 10000000).
     * @param positionY Overriding view Y position on screen in pixels (minimum 0, maximum 10000000).
     * @param dontSetVisibleSize Do not set visible view size, rely upon explicit setVisibleSize call.
     * @param screenOrientation Screen orientation override.
     * @param viewport If set, the visible area of the page will be overridden to this viewport. This viewport
     * change is not observed by the page, e.g. viewport-relative elements do not change positions.
     * @param displayFeature If set, the display feature of a multi-segment screen. If not set, multi-segment support
     * is turned-off.
     * Deprecated, use Emulation.setDisplayFeaturesOverride.
     * @param devicePosture If set, the posture of a foldable device. If not set the posture is set
     * to continuous.
     * Deprecated, use Emulation.setDevicePostureOverride.
     */
    public suspend fun setDeviceMetricsOverride(
        width: Int,
        height: Int,
        deviceScaleFactor: Double,
        mobile: Boolean,
        scale: Double? = null,
        screenWidth: Int? = null,
        screenHeight: Int? = null,
        positionX: Int? = null,
        positionY: Int? = null,
        dontSetVisibleSize: Boolean? = null,
        screenOrientation: ScreenOrientation? = null,
        viewport: Page.Viewport? = null,
        displayFeature: DisplayFeature? = null,
        devicePosture: DevicePosture? = null,
    ) {
        val parameter = SetDeviceMetricsOverrideParameter(
            width = width,
            height = height,
            deviceScaleFactor = deviceScaleFactor,
            mobile = mobile,
            scale = scale,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            positionX = positionX,
            positionY = positionY,
            dontSetVisibleSize = dontSetVisibleSize,
            screenOrientation = screenOrientation,
            viewport = viewport,
            displayFeature = displayFeature,
            devicePosture = devicePosture
        )
        setDeviceMetricsOverride(parameter)
    }

    /**
     * Start reporting the given posture value to the Device Posture API.
     * This override can also be set in setDeviceMetricsOverride().
     */
    public suspend fun setDevicePostureOverride(
        args: SetDevicePostureOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setDevicePostureOverride", parameter, mode)
    }

    /**
     * Start reporting the given posture value to the Device Posture API.
     * This override can also be set in setDeviceMetricsOverride().
     *
     * @param posture No description
     */
    public suspend fun setDevicePostureOverride(posture: DevicePosture) {
        val parameter = SetDevicePostureOverrideParameter(posture = posture)
        setDevicePostureOverride(parameter)
    }

    /**
     * Clears a device posture override set with either setDeviceMetricsOverride()
     * or setDevicePostureOverride() and starts using posture information from the
     * platform again.
     * Does nothing if no override is set.
     */
    public suspend fun clearDevicePostureOverride(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Emulation.clearDevicePostureOverride", parameter, mode)
    }

    /**
     * Start using the given display features to pupulate the Viewport Segments API.
     * This override can also be set in setDeviceMetricsOverride().
     */
    public suspend fun setDisplayFeaturesOverride(
        args: SetDisplayFeaturesOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setDisplayFeaturesOverride", parameter, mode)
    }

    /**
     * Start using the given display features to pupulate the Viewport Segments API.
     * This override can also be set in setDeviceMetricsOverride().
     *
     * @param features No description
     */
    public suspend fun setDisplayFeaturesOverride(features: List<DisplayFeature>) {
        val parameter = SetDisplayFeaturesOverrideParameter(features = features)
        setDisplayFeaturesOverride(parameter)
    }

    /**
     * Clears the display features override set with either setDeviceMetricsOverride()
     * or setDisplayFeaturesOverride() and starts using display features from the
     * platform again.
     * Does nothing if no override is set.
     */
    public suspend fun clearDisplayFeaturesOverride(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Emulation.clearDisplayFeaturesOverride", parameter, mode)
    }

    public suspend fun setScrollbarsHidden(
        args: SetScrollbarsHiddenParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setScrollbarsHidden", parameter, mode)
    }

    /**
     *
     *
     * @param hidden Whether scrollbars should be always hidden.
     */
    public suspend fun setScrollbarsHidden(hidden: Boolean) {
        val parameter = SetScrollbarsHiddenParameter(hidden = hidden)
        setScrollbarsHidden(parameter)
    }

    public suspend fun setDocumentCookieDisabled(
        args: SetDocumentCookieDisabledParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setDocumentCookieDisabled", parameter, mode)
    }

    /**
     *
     *
     * @param disabled Whether document.coookie API should be disabled.
     */
    public suspend fun setDocumentCookieDisabled(disabled: Boolean) {
        val parameter = SetDocumentCookieDisabledParameter(disabled = disabled)
        setDocumentCookieDisabled(parameter)
    }

    public suspend fun setEmitTouchEventsForMouse(
        args: SetEmitTouchEventsForMouseParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setEmitTouchEventsForMouse", parameter, mode)
    }

    /**
     *
     *
     * @param enabled Whether touch emulation based on mouse input should be enabled.
     * @param configuration Touch/gesture events configuration. Default: current platform.
     */
    public suspend fun setEmitTouchEventsForMouse(enabled: Boolean, configuration: String? = null) {
        val parameter = SetEmitTouchEventsForMouseParameter(enabled = enabled, configuration = configuration)
        setEmitTouchEventsForMouse(parameter)
    }

    /**
     * Emulates the given media type or media feature for CSS media queries.
     */
    public suspend fun setEmulatedMedia(args: SetEmulatedMediaParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setEmulatedMedia", parameter, mode)
    }

    /**
     * Emulates the given media type or media feature for CSS media queries.
     *
     * @param media Media type to emulate. Empty string disables the override.
     * @param features Media features to emulate.
     */
    public suspend fun setEmulatedMedia(media: String? = null, features: List<MediaFeature>? = null) {
        val parameter = SetEmulatedMediaParameter(media = media, features = features)
        setEmulatedMedia(parameter)
    }

    /**
     * Emulates the given vision deficiency.
     */
    public suspend fun setEmulatedVisionDeficiency(
        args: SetEmulatedVisionDeficiencyParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setEmulatedVisionDeficiency", parameter, mode)
    }

    /**
     * Emulates the given vision deficiency.
     *
     * @param type Vision deficiency to emulate. Order: best-effort emulations come first, followed by any
     * physiologically accurate emulations for medically recognized color vision deficiencies.
     */
    public suspend fun setEmulatedVisionDeficiency(type: String) {
        val parameter = SetEmulatedVisionDeficiencyParameter(type = type)
        setEmulatedVisionDeficiency(parameter)
    }

    /**
     * Emulates the given OS text scale.
     */
    public suspend fun setEmulatedOSTextScale(
        args: SetEmulatedOSTextScaleParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setEmulatedOSTextScale", parameter, mode)
    }

    /**
     * Emulates the given OS text scale.
     *
     * @param scale No description
     */
    public suspend fun setEmulatedOSTextScale(scale: Double? = null) {
        val parameter = SetEmulatedOSTextScaleParameter(scale = scale)
        setEmulatedOSTextScale(parameter)
    }

    /**
     * Overrides the Geolocation Position or Error. Omitting latitude, longitude or
     * accuracy emulates position unavailable.
     */
    public suspend fun setGeolocationOverride(
        args: SetGeolocationOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setGeolocationOverride", parameter, mode)
    }

    /**
     * Overrides the Geolocation Position or Error. Omitting latitude, longitude or
     * accuracy emulates position unavailable.
     *
     * @param latitude Mock latitude
     * @param longitude Mock longitude
     * @param accuracy Mock accuracy
     * @param altitude Mock altitude
     * @param altitudeAccuracy Mock altitudeAccuracy
     * @param heading Mock heading
     * @param speed Mock speed
     */
    public suspend fun setGeolocationOverride(
        latitude: Double? = null,
        longitude: Double? = null,
        accuracy: Double? = null,
        altitude: Double? = null,
        altitudeAccuracy: Double? = null,
        heading: Double? = null,
        speed: Double? = null,
    ) {
        val parameter = SetGeolocationOverrideParameter(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            altitude = altitude,
            altitudeAccuracy = altitudeAccuracy,
            heading = heading,
            speed = speed
        )
        setGeolocationOverride(parameter)
    }

    public suspend fun getOverriddenSensorInformation(
        args: GetOverriddenSensorInformationParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetOverriddenSensorInformationReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Emulation.getOverriddenSensorInformation", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     *
     *
     * @param type No description
     */
    public suspend fun getOverriddenSensorInformation(type: SensorType): GetOverriddenSensorInformationReturn {
        val parameter = GetOverriddenSensorInformationParameter(type = type)
        return getOverriddenSensorInformation(parameter)
    }

    /**
     * Overrides a platform sensor of a given type. If |enabled| is true, calls to
     * Sensor.start() will use a virtual sensor as backend rather than fetching
     * data from a real hardware sensor. Otherwise, existing virtual
     * sensor-backend Sensor objects will fire an error event and new calls to
     * Sensor.start() will attempt to use a real sensor instead.
     */
    public suspend fun setSensorOverrideEnabled(
        args: SetSensorOverrideEnabledParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setSensorOverrideEnabled", parameter, mode)
    }

    /**
     * Overrides a platform sensor of a given type. If |enabled| is true, calls to
     * Sensor.start() will use a virtual sensor as backend rather than fetching
     * data from a real hardware sensor. Otherwise, existing virtual
     * sensor-backend Sensor objects will fire an error event and new calls to
     * Sensor.start() will attempt to use a real sensor instead.
     *
     * @param enabled No description
     * @param type No description
     * @param metadata No description
     */
    public suspend fun setSensorOverrideEnabled(
        enabled: Boolean,
        type: SensorType,
        metadata: SensorMetadata? = null,
    ) {
        val parameter = SetSensorOverrideEnabledParameter(enabled = enabled, type = type, metadata = metadata)
        setSensorOverrideEnabled(parameter)
    }

    /**
     * Updates the sensor readings reported by a sensor type previously overridden
     * by setSensorOverrideEnabled.
     */
    public suspend fun setSensorOverrideReadings(
        args: SetSensorOverrideReadingsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setSensorOverrideReadings", parameter, mode)
    }

    /**
     * Updates the sensor readings reported by a sensor type previously overridden
     * by setSensorOverrideEnabled.
     *
     * @param type No description
     * @param reading No description
     */
    public suspend fun setSensorOverrideReadings(type: SensorType, reading: SensorReading) {
        val parameter = SetSensorOverrideReadingsParameter(type = type, reading = reading)
        setSensorOverrideReadings(parameter)
    }

    /**
     * Overrides a pressure source of a given type, as used by the Compute
     * Pressure API, so that updates to PressureObserver.observe() are provided
     * via setPressureStateOverride instead of being retrieved from
     * platform-provided telemetry data.
     */
    public suspend fun setPressureSourceOverrideEnabled(
        args: SetPressureSourceOverrideEnabledParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setPressureSourceOverrideEnabled", parameter, mode)
    }

    /**
     * Overrides a pressure source of a given type, as used by the Compute
     * Pressure API, so that updates to PressureObserver.observe() are provided
     * via setPressureStateOverride instead of being retrieved from
     * platform-provided telemetry data.
     *
     * @param enabled No description
     * @param source No description
     * @param metadata No description
     */
    public suspend fun setPressureSourceOverrideEnabled(
        enabled: Boolean,
        source: PressureSource,
        metadata: PressureMetadata? = null,
    ) {
        val parameter =
            SetPressureSourceOverrideEnabledParameter(enabled = enabled, source = source, metadata = metadata)
        setPressureSourceOverrideEnabled(parameter)
    }

    /**
     * TODO: OBSOLETE: To remove when setPressureDataOverride is merged.
     * Provides a given pressure state that will be processed and eventually be
     * delivered to PressureObserver users. |source| must have been previously
     * overridden by setPressureSourceOverrideEnabled.
     */
    public suspend fun setPressureStateOverride(
        args: SetPressureStateOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setPressureStateOverride", parameter, mode)
    }

    /**
     * TODO: OBSOLETE: To remove when setPressureDataOverride is merged.
     * Provides a given pressure state that will be processed and eventually be
     * delivered to PressureObserver users. |source| must have been previously
     * overridden by setPressureSourceOverrideEnabled.
     *
     * @param source No description
     * @param state No description
     */
    public suspend fun setPressureStateOverride(source: PressureSource, state: PressureState) {
        val parameter = SetPressureStateOverrideParameter(source = source, state = state)
        setPressureStateOverride(parameter)
    }

    /**
     * Provides a given pressure data set that will be processed and eventually be
     * delivered to PressureObserver users. |source| must have been previously
     * overridden by setPressureSourceOverrideEnabled.
     */
    public suspend fun setPressureDataOverride(
        args: SetPressureDataOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setPressureDataOverride", parameter, mode)
    }

    /**
     * Provides a given pressure data set that will be processed and eventually be
     * delivered to PressureObserver users. |source| must have been previously
     * overridden by setPressureSourceOverrideEnabled.
     *
     * @param source No description
     * @param state No description
     * @param ownContributionEstimate No description
     */
    public suspend fun setPressureDataOverride(
        source: PressureSource,
        state: PressureState,
        ownContributionEstimate: Double? = null,
    ) {
        val parameter = SetPressureDataOverrideParameter(
            source = source,
            state = state,
            ownContributionEstimate = ownContributionEstimate
        )
        setPressureDataOverride(parameter)
    }

    /**
     * Overrides the Idle state.
     */
    public suspend fun setIdleOverride(args: SetIdleOverrideParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setIdleOverride", parameter, mode)
    }

    /**
     * Overrides the Idle state.
     *
     * @param isUserActive Mock isUserActive
     * @param isScreenUnlocked Mock isScreenUnlocked
     */
    public suspend fun setIdleOverride(isUserActive: Boolean, isScreenUnlocked: Boolean) {
        val parameter = SetIdleOverrideParameter(isUserActive = isUserActive, isScreenUnlocked = isScreenUnlocked)
        setIdleOverride(parameter)
    }

    /**
     * Clears Idle state overrides.
     */
    public suspend fun clearIdleOverride(mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = null
        cdp.callCommand("Emulation.clearIdleOverride", parameter, mode)
    }

    /**
     * Overrides value returned by the javascript navigator object.
     */
    @Deprecated(message = "")
    public suspend fun setNavigatorOverrides(
        args: SetNavigatorOverridesParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setNavigatorOverrides", parameter, mode)
    }

    /**
     * Overrides value returned by the javascript navigator object.
     *
     * @param platform The platform navigator.platform should return.
     */
    @Deprecated(message = "")
    public suspend fun setNavigatorOverrides(platform: String) {
        val parameter = SetNavigatorOverridesParameter(platform = platform)
        setNavigatorOverrides(parameter)
    }

    /**
     * Sets a specified page scale factor.
     */
    public suspend fun setPageScaleFactor(args: SetPageScaleFactorParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setPageScaleFactor", parameter, mode)
    }

    /**
     * Sets a specified page scale factor.
     *
     * @param pageScaleFactor Page scale factor.
     */
    public suspend fun setPageScaleFactor(pageScaleFactor: Double) {
        val parameter = SetPageScaleFactorParameter(pageScaleFactor = pageScaleFactor)
        setPageScaleFactor(parameter)
    }

    /**
     * Switches script execution in the page.
     */
    public suspend fun setScriptExecutionDisabled(
        args: SetScriptExecutionDisabledParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setScriptExecutionDisabled", parameter, mode)
    }

    /**
     * Switches script execution in the page.
     *
     * @param value Whether script execution should be disabled in the page.
     */
    public suspend fun setScriptExecutionDisabled(`value`: Boolean) {
        val parameter = SetScriptExecutionDisabledParameter(value = value)
        setScriptExecutionDisabled(parameter)
    }

    /**
     * Enables touch on platforms which do not support them.
     */
    public suspend fun setTouchEmulationEnabled(
        args: SetTouchEmulationEnabledParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setTouchEmulationEnabled", parameter, mode)
    }

    /**
     * Enables touch on platforms which do not support them.
     *
     * @param enabled Whether the touch event emulation should be enabled.
     * @param maxTouchPoints Maximum touch points supported. Defaults to one.
     */
    public suspend fun setTouchEmulationEnabled(enabled: Boolean, maxTouchPoints: Int? = null) {
        val parameter = SetTouchEmulationEnabledParameter(enabled = enabled, maxTouchPoints = maxTouchPoints)
        setTouchEmulationEnabled(parameter)
    }

    /**
     * Turns on virtual time for all frames (replacing real-time with a synthetic time source) and sets
     * the current virtual time policy.  Note this supersedes any previous time budget.
     */
    public suspend fun setVirtualTimePolicy(
        args: SetVirtualTimePolicyParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): SetVirtualTimePolicyReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Emulation.setVirtualTimePolicy", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Turns on virtual time for all frames (replacing real-time with a synthetic time source) and sets
     * the current virtual time policy.  Note this supersedes any previous time budget.
     *
     * @param policy No description
     * @param budget If set, after this many virtual milliseconds have elapsed virtual time will be paused and a
     * virtualTimeBudgetExpired event is sent.
     * @param maxVirtualTimeTaskStarvationCount If set this specifies the maximum number of tasks that can be run before virtual is forced
     * forwards to prevent deadlock.
     * @param initialVirtualTime If set, base::Time::Now will be overridden to initially return this value.
     */
    public suspend fun setVirtualTimePolicy(
        policy: VirtualTimePolicy,
        budget: Double? = null,
        maxVirtualTimeTaskStarvationCount: Int? = null,
        initialVirtualTime: Double? = null,
    ): SetVirtualTimePolicyReturn {
        val parameter = SetVirtualTimePolicyParameter(
            policy = policy,
            budget = budget,
            maxVirtualTimeTaskStarvationCount = maxVirtualTimeTaskStarvationCount,
            initialVirtualTime = initialVirtualTime
        )
        return setVirtualTimePolicy(parameter)
    }

    /**
     * Overrides default host system locale with the specified one.
     */
    public suspend fun setLocaleOverride(args: SetLocaleOverrideParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setLocaleOverride", parameter, mode)
    }

    /**
     * Overrides default host system locale with the specified one.
     *
     * @param locale ICU style C locale (e.g. "en_US"). If not specified or empty, disables the override and
     * restores default host system locale.
     */
    public suspend fun setLocaleOverride(locale: String? = null) {
        val parameter = SetLocaleOverrideParameter(locale = locale)
        setLocaleOverride(parameter)
    }

    /**
     * Overrides default host system timezone with the specified one.
     */
    public suspend fun setTimezoneOverride(
        args: SetTimezoneOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setTimezoneOverride", parameter, mode)
    }

    /**
     * Overrides default host system timezone with the specified one.
     *
     * @param timezoneId The timezone identifier. List of supported timezones:
     * https://source.chromium.org/chromium/chromium/deps/icu.git/+/faee8bc70570192d82d2978a71e2a615788597d1:source/data/misc/metaZones.txt
     * If empty, disables the override and restores default host system timezone.
     */
    public suspend fun setTimezoneOverride(timezoneId: String) {
        val parameter = SetTimezoneOverrideParameter(timezoneId = timezoneId)
        setTimezoneOverride(parameter)
    }

    /**
     * Resizes the frame/viewport of the page. Note that this does not affect the frame's container
     * (e.g. browser window). Can be used to produce screenshots of the specified size. Not supported
     * on Android.
     */
    @Deprecated(message = "")
    public suspend fun setVisibleSize(args: SetVisibleSizeParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setVisibleSize", parameter, mode)
    }

    /**
     * Resizes the frame/viewport of the page. Note that this does not affect the frame's container
     * (e.g. browser window). Can be used to produce screenshots of the specified size. Not supported
     * on Android.
     *
     * @param width Frame width (DIP).
     * @param height Frame height (DIP).
     */
    @Deprecated(message = "")
    public suspend fun setVisibleSize(width: Int, height: Int) {
        val parameter = SetVisibleSizeParameter(width = width, height = height)
        setVisibleSize(parameter)
    }

    public suspend fun setDisabledImageTypes(
        args: SetDisabledImageTypesParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setDisabledImageTypes", parameter, mode)
    }

    /**
     *
     *
     * @param imageTypes Image types to disable.
     */
    public suspend fun setDisabledImageTypes(imageTypes: List<DisabledImageType>) {
        val parameter = SetDisabledImageTypesParameter(imageTypes = imageTypes)
        setDisabledImageTypes(parameter)
    }

    /**
     * Override the value of navigator.connection.saveData
     */
    public suspend fun setDataSaverOverride(
        args: SetDataSaverOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setDataSaverOverride", parameter, mode)
    }

    /**
     * Override the value of navigator.connection.saveData
     *
     * @param dataSaverEnabled Override value. Omitting the parameter disables the override.
     */
    public suspend fun setDataSaverOverride(dataSaverEnabled: Boolean? = null) {
        val parameter = SetDataSaverOverrideParameter(dataSaverEnabled = dataSaverEnabled)
        setDataSaverOverride(parameter)
    }

    public suspend fun setHardwareConcurrencyOverride(
        args: SetHardwareConcurrencyOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setHardwareConcurrencyOverride", parameter, mode)
    }

    /**
     *
     *
     * @param hardwareConcurrency Hardware concurrency to report
     */
    public suspend fun setHardwareConcurrencyOverride(hardwareConcurrency: Int) {
        val parameter = SetHardwareConcurrencyOverrideParameter(hardwareConcurrency = hardwareConcurrency)
        setHardwareConcurrencyOverride(parameter)
    }

    /**
     * Allows overriding user agent with the given string.
     * `userAgentMetadata` must be set for Client Hint headers to be sent.
     */
    public suspend fun setUserAgentOverride(
        args: SetUserAgentOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setUserAgentOverride", parameter, mode)
    }

    /**
     * Allows overriding user agent with the given string.
     * `userAgentMetadata` must be set for Client Hint headers to be sent.
     *
     * @param userAgent User agent to use.
     * @param acceptLanguage Browser language to emulate.
     * @param platform The platform navigator.platform should return.
     * @param userAgentMetadata To be sent in Sec-CH-UA-* headers and returned in navigator.userAgentData
     */
    public suspend fun setUserAgentOverride(
        userAgent: String,
        acceptLanguage: String? = null,
        platform: String? = null,
        userAgentMetadata: UserAgentMetadata? = null,
    ) {
        val parameter = SetUserAgentOverrideParameter(
            userAgent = userAgent,
            acceptLanguage = acceptLanguage,
            platform = platform,
            userAgentMetadata = userAgentMetadata
        )
        setUserAgentOverride(parameter)
    }

    /**
     * Allows overriding the automation flag.
     */
    public suspend fun setAutomationOverride(
        args: SetAutomationOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setAutomationOverride", parameter, mode)
    }

    /**
     * Allows overriding the automation flag.
     *
     * @param enabled Whether the override should be enabled.
     */
    public suspend fun setAutomationOverride(enabled: Boolean) {
        val parameter = SetAutomationOverrideParameter(enabled = enabled)
        setAutomationOverride(parameter)
    }

    /**
     * Allows overriding the difference between the small and large viewport sizes, which determine the
     * value of the `svh` and `lvh` unit, respectively. Only supported for top-level frames.
     */
    public suspend fun setSmallViewportHeightDifferenceOverride(
        args: SetSmallViewportHeightDifferenceOverrideParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Emulation.setSmallViewportHeightDifferenceOverride", parameter, mode)
    }

    /**
     * Allows overriding the difference between the small and large viewport sizes, which determine the
     * value of the `svh` and `lvh` unit, respectively. Only supported for top-level frames.
     *
     * @param difference This will cause an element of size 100svh to be `difference` pixels smaller than an element
     * of size 100lvh.
     */
    public suspend fun setSmallViewportHeightDifferenceOverride(difference: Int) {
        val parameter = SetSmallViewportHeightDifferenceOverrideParameter(difference = difference)
        setSmallViewportHeightDifferenceOverride(parameter)
    }

    @Serializable
    public data class SafeAreaInsets(
        /**
         * Overrides safe-area-inset-top.
         */
        public val top: Int? = null,
        /**
         * Overrides safe-area-max-inset-top.
         */
        public val topMax: Int? = null,
        /**
         * Overrides safe-area-inset-left.
         */
        public val left: Int? = null,
        /**
         * Overrides safe-area-max-inset-left.
         */
        public val leftMax: Int? = null,
        /**
         * Overrides safe-area-inset-bottom.
         */
        public val bottom: Int? = null,
        /**
         * Overrides safe-area-max-inset-bottom.
         */
        public val bottomMax: Int? = null,
        /**
         * Overrides safe-area-inset-right.
         */
        public val right: Int? = null,
        /**
         * Overrides safe-area-max-inset-right.
         */
        public val rightMax: Int? = null,
    )

    /**
     * Screen orientation.
     */
    @Serializable
    public data class ScreenOrientation(
        /**
         * Orientation type.
         */
        public val type: String,
        /**
         * Orientation angle.
         */
        public val angle: Int,
    )

    @Serializable
    public data class DisplayFeature(
        /**
         * Orientation of a display feature in relation to screen
         */
        public val orientation: String,
        /**
         * The offset from the screen origin in either the x (for vertical
         * orientation) or y (for horizontal orientation) direction.
         */
        public val offset: Int,
        /**
         * A display feature may mask content such that it is not physically
         * displayed - this length along with the offset describes this area.
         * A display feature that only splits content will have a 0 mask_length.
         */
        public val maskLength: Int,
    )

    @Serializable
    public data class DevicePosture(
        /**
         * Current posture of the device
         */
        public val type: String,
    )

    @Serializable
    public data class MediaFeature(
        public val name: String,
        public val `value`: String,
    )

    /**
     * advance: If the scheduler runs out of immediate work, the virtual time base may fast forward to
     * allow the next delayed task (if any) to run; pause: The virtual time base may not advance;
     * pauseIfNetworkFetchesPending: The virtual time base may not advance if there are any pending
     * resource fetches.
     */
    @Serializable
    public enum class VirtualTimePolicy {
        @SerialName("advance")
        ADVANCE,

        @SerialName("pause")
        PAUSE,

        @SerialName("pauseIfNetworkFetchesPending")
        PAUSEIFNETWORKFETCHESPENDING,
    }

    /**
     * Used to specify User Agent Client Hints to emulate. See https://wicg.github.io/ua-client-hints
     */
    @Serializable
    public data class UserAgentBrandVersion(
        public val brand: String,
        public val version: String,
    )

    /**
     * Used to specify User Agent Client Hints to emulate. See https://wicg.github.io/ua-client-hints
     * Missing optional values will be filled in by the target with what it would normally use.
     */
    @Serializable
    public data class UserAgentMetadata(
        /**
         * Brands appearing in Sec-CH-UA.
         */
        public val brands: List<UserAgentBrandVersion>? = null,
        /**
         * Brands appearing in Sec-CH-UA-Full-Version-List.
         */
        public val fullVersionList: List<UserAgentBrandVersion>? = null,
        public val fullVersion: String? = null,
        public val platform: String,
        public val platformVersion: String,
        public val architecture: String,
        public val model: String,
        public val mobile: Boolean,
        public val bitness: String? = null,
        public val wow64: Boolean? = null,
        /**
         * Used to specify User Agent form-factor values.
         * See https://wicg.github.io/ua-client-hints/#sec-ch-ua-form-factors
         */
        public val formFactors: List<String>? = null,
    )

    /**
     * Used to specify sensor types to emulate.
     * See https://w3c.github.io/sensors/#automation for more information.
     */
    @Serializable
    public enum class SensorType {
        @SerialName("absolute-orientation")
        ABSOLUTE_ORIENTATION,

        @SerialName("accelerometer")
        ACCELEROMETER,

        @SerialName("ambient-light")
        AMBIENT_LIGHT,

        @SerialName("gravity")
        GRAVITY,

        @SerialName("gyroscope")
        GYROSCOPE,

        @SerialName("linear-acceleration")
        LINEAR_ACCELERATION,

        @SerialName("magnetometer")
        MAGNETOMETER,

        @SerialName("relative-orientation")
        RELATIVE_ORIENTATION,
    }

    @Serializable
    public data class SensorMetadata(
        public val available: Boolean? = null,
        public val minimumFrequency: Double? = null,
        public val maximumFrequency: Double? = null,
    )

    @Serializable
    public data class SensorReadingSingle(
        public val `value`: Double,
    )

    @Serializable
    public data class SensorReadingXYZ(
        public val x: Double,
        public val y: Double,
        public val z: Double,
    )

    @Serializable
    public data class SensorReadingQuaternion(
        public val x: Double,
        public val y: Double,
        public val z: Double,
        public val w: Double,
    )

    @Serializable
    public data class SensorReading(
        public val single: SensorReadingSingle? = null,
        public val xyz: SensorReadingXYZ? = null,
        public val quaternion: SensorReadingQuaternion? = null,
    )

    @Serializable
    public enum class PressureSource {
        @SerialName("cpu")
        CPU,
    }

    @Serializable
    public enum class PressureState {
        @SerialName("nominal")
        NOMINAL,

        @SerialName("fair")
        FAIR,

        @SerialName("serious")
        SERIOUS,

        @SerialName("critical")
        CRITICAL,
    }

    @Serializable
    public data class PressureMetadata(
        public val available: Boolean? = null,
    )

    /**
     * Enum of image types that can be disabled.
     */
    @Serializable
    public enum class DisabledImageType {
        @SerialName("avif")
        AVIF,

        @SerialName("webp")
        WEBP,
    }

    @Serializable
    public data class CanEmulateReturn(
        /**
         * True if emulation is supported.
         */
        public val result: Boolean,
    )

    @Serializable
    public data class SetFocusEmulationEnabledParameter(
        /**
         * Whether to enable to disable focus emulation.
         */
        public val enabled: Boolean,
    )

    @Serializable
    public data class SetAutoDarkModeOverrideParameter(
        /**
         * Whether to enable or disable automatic dark mode.
         * If not specified, any existing override will be cleared.
         */
        public val enabled: Boolean? = null,
    )

    @Serializable
    public data class SetCPUThrottlingRateParameter(
        /**
         * Throttling rate as a slowdown factor (1 is no throttle, 2 is 2x slowdown, etc).
         */
        public val rate: Double,
    )

    @Serializable
    public data class SetDefaultBackgroundColorOverrideParameter(
        /**
         * RGBA of the default background color. If not specified, any existing override will be
         * cleared.
         */
        public val color: DOM.RGBA? = null,
    )

    @Serializable
    public data class SetSafeAreaInsetsOverrideParameter(
        public val insets: SafeAreaInsets,
    )

    @Serializable
    public data class SetDeviceMetricsOverrideParameter(
        /**
         * Overriding width value in pixels (minimum 0, maximum 10000000). 0 disables the override.
         */
        public val width: Int,
        /**
         * Overriding height value in pixels (minimum 0, maximum 10000000). 0 disables the override.
         */
        public val height: Int,
        /**
         * Overriding device scale factor value. 0 disables the override.
         */
        public val deviceScaleFactor: Double,
        /**
         * Whether to emulate mobile device. This includes viewport meta tag, overlay scrollbars, text
         * autosizing and more.
         */
        public val mobile: Boolean,
        /**
         * Scale to apply to resulting view image.
         */
        public val scale: Double? = null,
        /**
         * Overriding screen width value in pixels (minimum 0, maximum 10000000).
         */
        public val screenWidth: Int? = null,
        /**
         * Overriding screen height value in pixels (minimum 0, maximum 10000000).
         */
        public val screenHeight: Int? = null,
        /**
         * Overriding view X position on screen in pixels (minimum 0, maximum 10000000).
         */
        public val positionX: Int? = null,
        /**
         * Overriding view Y position on screen in pixels (minimum 0, maximum 10000000).
         */
        public val positionY: Int? = null,
        /**
         * Do not set visible view size, rely upon explicit setVisibleSize call.
         */
        public val dontSetVisibleSize: Boolean? = null,
        /**
         * Screen orientation override.
         */
        public val screenOrientation: ScreenOrientation? = null,
        /**
         * If set, the visible area of the page will be overridden to this viewport. This viewport
         * change is not observed by the page, e.g. viewport-relative elements do not change positions.
         */
        public val viewport: Page.Viewport? = null,
        /**
         * If set, the display feature of a multi-segment screen. If not set, multi-segment support
         * is turned-off.
         * Deprecated, use Emulation.setDisplayFeaturesOverride.
         */
        public val displayFeature: DisplayFeature? = null,
        /**
         * If set, the posture of a foldable device. If not set the posture is set
         * to continuous.
         * Deprecated, use Emulation.setDevicePostureOverride.
         */
        public val devicePosture: DevicePosture? = null,
    )

    @Serializable
    public data class SetDevicePostureOverrideParameter(
        public val posture: DevicePosture,
    )

    @Serializable
    public data class SetDisplayFeaturesOverrideParameter(
        public val features: List<DisplayFeature>,
    )

    @Serializable
    public data class SetScrollbarsHiddenParameter(
        /**
         * Whether scrollbars should be always hidden.
         */
        public val hidden: Boolean,
    )

    @Serializable
    public data class SetDocumentCookieDisabledParameter(
        /**
         * Whether document.coookie API should be disabled.
         */
        public val disabled: Boolean,
    )

    @Serializable
    public data class SetEmitTouchEventsForMouseParameter(
        /**
         * Whether touch emulation based on mouse input should be enabled.
         */
        public val enabled: Boolean,
        /**
         * Touch/gesture events configuration. Default: current platform.
         */
        public val configuration: String? = null,
    )

    @Serializable
    public data class SetEmulatedMediaParameter(
        /**
         * Media type to emulate. Empty string disables the override.
         */
        public val media: String? = null,
        /**
         * Media features to emulate.
         */
        public val features: List<MediaFeature>? = null,
    )

    @Serializable
    public data class SetEmulatedVisionDeficiencyParameter(
        /**
         * Vision deficiency to emulate. Order: best-effort emulations come first, followed by any
         * physiologically accurate emulations for medically recognized color vision deficiencies.
         */
        public val type: String,
    )

    @Serializable
    public data class SetEmulatedOSTextScaleParameter(
        public val scale: Double? = null,
    )

    @Serializable
    public data class SetGeolocationOverrideParameter(
        /**
         * Mock latitude
         */
        public val latitude: Double? = null,
        /**
         * Mock longitude
         */
        public val longitude: Double? = null,
        /**
         * Mock accuracy
         */
        public val accuracy: Double? = null,
        /**
         * Mock altitude
         */
        public val altitude: Double? = null,
        /**
         * Mock altitudeAccuracy
         */
        public val altitudeAccuracy: Double? = null,
        /**
         * Mock heading
         */
        public val heading: Double? = null,
        /**
         * Mock speed
         */
        public val speed: Double? = null,
    )

    @Serializable
    public data class GetOverriddenSensorInformationParameter(
        public val type: SensorType,
    )

    @Serializable
    public data class GetOverriddenSensorInformationReturn(
        public val requestedSamplingFrequency: Double,
    )

    @Serializable
    public data class SetSensorOverrideEnabledParameter(
        public val enabled: Boolean,
        public val type: SensorType,
        public val metadata: SensorMetadata? = null,
    )

    @Serializable
    public data class SetSensorOverrideReadingsParameter(
        public val type: SensorType,
        public val reading: SensorReading,
    )

    @Serializable
    public data class SetPressureSourceOverrideEnabledParameter(
        public val enabled: Boolean,
        public val source: PressureSource,
        public val metadata: PressureMetadata? = null,
    )

    @Serializable
    public data class SetPressureStateOverrideParameter(
        public val source: PressureSource,
        public val state: PressureState,
    )

    @Serializable
    public data class SetPressureDataOverrideParameter(
        public val source: PressureSource,
        public val state: PressureState,
        public val ownContributionEstimate: Double? = null,
    )

    @Serializable
    public data class SetIdleOverrideParameter(
        /**
         * Mock isUserActive
         */
        public val isUserActive: Boolean,
        /**
         * Mock isScreenUnlocked
         */
        public val isScreenUnlocked: Boolean,
    )

    @Serializable
    public data class SetNavigatorOverridesParameter(
        /**
         * The platform navigator.platform should return.
         */
        public val platform: String,
    )

    @Serializable
    public data class SetPageScaleFactorParameter(
        /**
         * Page scale factor.
         */
        public val pageScaleFactor: Double,
    )

    @Serializable
    public data class SetScriptExecutionDisabledParameter(
        /**
         * Whether script execution should be disabled in the page.
         */
        public val `value`: Boolean,
    )

    @Serializable
    public data class SetTouchEmulationEnabledParameter(
        /**
         * Whether the touch event emulation should be enabled.
         */
        public val enabled: Boolean,
        /**
         * Maximum touch points supported. Defaults to one.
         */
        public val maxTouchPoints: Int? = null,
    )

    @Serializable
    public data class SetVirtualTimePolicyParameter(
        public val policy: VirtualTimePolicy,
        /**
         * If set, after this many virtual milliseconds have elapsed virtual time will be paused and a
         * virtualTimeBudgetExpired event is sent.
         */
        public val budget: Double? = null,
        /**
         * If set this specifies the maximum number of tasks that can be run before virtual is forced
         * forwards to prevent deadlock.
         */
        public val maxVirtualTimeTaskStarvationCount: Int? = null,
        /**
         * If set, base::Time::Now will be overridden to initially return this value.
         */
        public val initialVirtualTime: Double? = null,
    )

    @Serializable
    public data class SetVirtualTimePolicyReturn(
        /**
         * Absolute timestamp at which virtual time was first enabled (up time in milliseconds).
         */
        public val virtualTimeTicksBase: Double,
    )

    @Serializable
    public data class SetLocaleOverrideParameter(
        /**
         * ICU style C locale (e.g. "en_US"). If not specified or empty, disables the override and
         * restores default host system locale.
         */
        public val locale: String? = null,
    )

    @Serializable
    public data class SetTimezoneOverrideParameter(
        /**
         * The timezone identifier. List of supported timezones:
         * https://source.chromium.org/chromium/chromium/deps/icu.git/+/faee8bc70570192d82d2978a71e2a615788597d1:source/data/misc/metaZones.txt
         * If empty, disables the override and restores default host system timezone.
         */
        public val timezoneId: String,
    )

    @Serializable
    public data class SetVisibleSizeParameter(
        /**
         * Frame width (DIP).
         */
        public val width: Int,
        /**
         * Frame height (DIP).
         */
        public val height: Int,
    )

    @Serializable
    public data class SetDisabledImageTypesParameter(
        /**
         * Image types to disable.
         */
        public val imageTypes: List<DisabledImageType>,
    )

    @Serializable
    public data class SetDataSaverOverrideParameter(
        /**
         * Override value. Omitting the parameter disables the override.
         */
        public val dataSaverEnabled: Boolean? = null,
    )

    @Serializable
    public data class SetHardwareConcurrencyOverrideParameter(
        /**
         * Hardware concurrency to report
         */
        public val hardwareConcurrency: Int,
    )

    @Serializable
    public data class SetUserAgentOverrideParameter(
        /**
         * User agent to use.
         */
        public val userAgent: String,
        /**
         * Browser language to emulate.
         */
        public val acceptLanguage: String? = null,
        /**
         * The platform navigator.platform should return.
         */
        public val platform: String? = null,
        /**
         * To be sent in Sec-CH-UA-* headers and returned in navigator.userAgentData
         */
        public val userAgentMetadata: UserAgentMetadata? = null,
    )

    @Serializable
    public data class SetAutomationOverrideParameter(
        /**
         * Whether the override should be enabled.
         */
        public val enabled: Boolean,
    )

    @Serializable
    public data class SetSmallViewportHeightDifferenceOverrideParameter(
        /**
         * This will cause an element of size 100svh to be `difference` pixels smaller than an element
         * of size 100lvh.
         */
        public val difference: Int,
    )
}
