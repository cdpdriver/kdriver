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

public val CDP.browser: Browser
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Browser(this))

/**
 * The Browser domain defines methods and events for browser managing.
 */
public class Browser(
    private val cdp: CDP,
) : Domain {
    public val downloadWillBegin: Flow<DownloadWillBeginParameter> = cdp
        .events
        .filter {
            it.method == "Browser.downloadWillBegin"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    public val downloadProgress: Flow<DownloadProgressParameter> = cdp
        .events
        .filter {
            it.method == "Browser.downloadProgress"
        }
        .map {
            it.params
        }
        .filterNotNull()
        .map {
            Serialization.json.decodeFromJsonElement(it)
        }

    /**
     * Set permission settings for given origin.
     */
    public suspend fun setPermission(args: SetPermissionParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Browser.setPermission", parameter)
    }

    /**
     * Set permission settings for given origin.
     */
    public suspend fun setPermission(
        permission: PermissionDescriptor,
        setting: PermissionSetting,
        origin: String? = null,
        browserContextId: String? = null,
    ) {
        val parameter = SetPermissionParameter(
            permission = permission,
            setting = setting,
            origin = origin,
            browserContextId = browserContextId
        )
        setPermission(parameter)
    }

    /**
     * Grant specific permissions to the given origin and reject all others.
     */
    public suspend fun grantPermissions(args: GrantPermissionsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Browser.grantPermissions", parameter)
    }

    /**
     * Grant specific permissions to the given origin and reject all others.
     */
    public suspend fun grantPermissions(
        permissions: List<PermissionType>,
        origin: String? = null,
        browserContextId: String? = null,
    ) {
        val parameter =
            GrantPermissionsParameter(permissions = permissions, origin = origin, browserContextId = browserContextId)
        grantPermissions(parameter)
    }

    /**
     * Reset all permission management for all origins.
     */
    public suspend fun resetPermissions(args: ResetPermissionsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Browser.resetPermissions", parameter)
    }

    /**
     * Reset all permission management for all origins.
     */
    public suspend fun resetPermissions(browserContextId: String? = null) {
        val parameter = ResetPermissionsParameter(browserContextId = browserContextId)
        resetPermissions(parameter)
    }

    /**
     * Set the behavior when downloading a file.
     */
    public suspend fun setDownloadBehavior(args: SetDownloadBehaviorParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Browser.setDownloadBehavior", parameter)
    }

    /**
     * Set the behavior when downloading a file.
     */
    public suspend fun setDownloadBehavior(
        behavior: String,
        browserContextId: String? = null,
        downloadPath: String? = null,
        eventsEnabled: Boolean? = null,
    ) {
        val parameter = SetDownloadBehaviorParameter(
            behavior = behavior,
            browserContextId = browserContextId,
            downloadPath = downloadPath,
            eventsEnabled = eventsEnabled
        )
        setDownloadBehavior(parameter)
    }

    /**
     * Cancel a download if in progress
     */
    public suspend fun cancelDownload(args: CancelDownloadParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Browser.cancelDownload", parameter)
    }

    /**
     * Cancel a download if in progress
     */
    public suspend fun cancelDownload(guid: String, browserContextId: String? = null) {
        val parameter = CancelDownloadParameter(guid = guid, browserContextId = browserContextId)
        cancelDownload(parameter)
    }

    /**
     * Close browser gracefully.
     */
    public suspend fun close() {
        val parameter = null
        cdp.callCommand("Browser.close", parameter)
    }

    /**
     * Crashes browser on the main thread.
     */
    public suspend fun crash() {
        val parameter = null
        cdp.callCommand("Browser.crash", parameter)
    }

    /**
     * Crashes GPU process.
     */
    public suspend fun crashGpuProcess() {
        val parameter = null
        cdp.callCommand("Browser.crashGpuProcess", parameter)
    }

    /**
     * Returns version information.
     */
    public suspend fun getVersion(): GetVersionReturn {
        val parameter = null
        val result = cdp.callCommand("Browser.getVersion", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the command line switches for the browser process if, and only if
     * --enable-automation is on the commandline.
     */
    public suspend fun getBrowserCommandLine(): GetBrowserCommandLineReturn {
        val parameter = null
        val result = cdp.callCommand("Browser.getBrowserCommandLine", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Get Chrome histograms.
     */
    public suspend fun getHistograms(args: GetHistogramsParameter): GetHistogramsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Browser.getHistograms", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Get Chrome histograms.
     */
    public suspend fun getHistograms(query: String? = null, delta: Boolean? = null): GetHistogramsReturn {
        val parameter = GetHistogramsParameter(query = query, delta = delta)
        return getHistograms(parameter)
    }

    /**
     * Get a Chrome histogram by name.
     */
    public suspend fun getHistogram(args: GetHistogramParameter): GetHistogramReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Browser.getHistogram", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Get a Chrome histogram by name.
     */
    public suspend fun getHistogram(name: String, delta: Boolean? = null): GetHistogramReturn {
        val parameter = GetHistogramParameter(name = name, delta = delta)
        return getHistogram(parameter)
    }

    /**
     * Get position and size of the browser window.
     */
    public suspend fun getWindowBounds(args: GetWindowBoundsParameter): GetWindowBoundsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Browser.getWindowBounds", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Get position and size of the browser window.
     */
    public suspend fun getWindowBounds(windowId: Int): GetWindowBoundsReturn {
        val parameter = GetWindowBoundsParameter(windowId = windowId)
        return getWindowBounds(parameter)
    }

    /**
     * Get the browser window that contains the devtools target.
     */
    public suspend fun getWindowForTarget(args: GetWindowForTargetParameter): GetWindowForTargetReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Browser.getWindowForTarget", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Get the browser window that contains the devtools target.
     */
    public suspend fun getWindowForTarget(targetId: String? = null): GetWindowForTargetReturn {
        val parameter = GetWindowForTargetParameter(targetId = targetId)
        return getWindowForTarget(parameter)
    }

    /**
     * Set position and/or size of the browser window.
     */
    public suspend fun setWindowBounds(args: SetWindowBoundsParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Browser.setWindowBounds", parameter)
    }

    /**
     * Set position and/or size of the browser window.
     */
    public suspend fun setWindowBounds(windowId: Int, bounds: Bounds) {
        val parameter = SetWindowBoundsParameter(windowId = windowId, bounds = bounds)
        setWindowBounds(parameter)
    }

    /**
     * Set dock tile details, platform-specific.
     */
    public suspend fun setDockTile(args: SetDockTileParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Browser.setDockTile", parameter)
    }

    /**
     * Set dock tile details, platform-specific.
     */
    public suspend fun setDockTile(badgeLabel: String? = null, image: String? = null) {
        val parameter = SetDockTileParameter(badgeLabel = badgeLabel, image = image)
        setDockTile(parameter)
    }

    /**
     * Invoke custom browser commands used by telemetry.
     */
    public suspend fun executeBrowserCommand(args: ExecuteBrowserCommandParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Browser.executeBrowserCommand", parameter)
    }

    /**
     * Invoke custom browser commands used by telemetry.
     */
    public suspend fun executeBrowserCommand(commandId: BrowserCommandId) {
        val parameter = ExecuteBrowserCommandParameter(commandId = commandId)
        executeBrowserCommand(parameter)
    }

    /**
     * Allows a site to use privacy sandbox features that require enrollment
     * without the site actually being enrolled. Only supported on page targets.
     */
    public suspend fun addPrivacySandboxEnrollmentOverride(args: AddPrivacySandboxEnrollmentOverrideParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Browser.addPrivacySandboxEnrollmentOverride", parameter)
    }

    /**
     * Allows a site to use privacy sandbox features that require enrollment
     * without the site actually being enrolled. Only supported on page targets.
     */
    public suspend fun addPrivacySandboxEnrollmentOverride(url: String) {
        val parameter = AddPrivacySandboxEnrollmentOverrideParameter(url = url)
        addPrivacySandboxEnrollmentOverride(parameter)
    }

    /**
     * The state of the browser window.
     */
    @Serializable
    public enum class WindowState {
        @SerialName("normal")
        NORMAL,

        @SerialName("minimized")
        MINIMIZED,

        @SerialName("maximized")
        MAXIMIZED,

        @SerialName("fullscreen")
        FULLSCREEN,
    }

    /**
     * Browser window bounds information
     */
    @Serializable
    public data class Bounds(
        /**
         * The offset from the left edge of the screen to the window in pixels.
         */
        public val left: Int? = null,
        /**
         * The offset from the top edge of the screen to the window in pixels.
         */
        public val top: Int? = null,
        /**
         * The window width in pixels.
         */
        public val width: Int? = null,
        /**
         * The window height in pixels.
         */
        public val height: Int? = null,
        /**
         * The window state. Default to normal.
         */
        public val windowState: WindowState? = null,
    )

    @Serializable
    public enum class PermissionType {
        @SerialName("accessibilityEvents")
        ACCESSIBILITYEVENTS,

        @SerialName("audioCapture")
        AUDIOCAPTURE,

        @SerialName("backgroundSync")
        BACKGROUNDSYNC,

        @SerialName("backgroundFetch")
        BACKGROUNDFETCH,

        @SerialName("capturedSurfaceControl")
        CAPTUREDSURFACECONTROL,

        @SerialName("clipboardReadWrite")
        CLIPBOARDREADWRITE,

        @SerialName("clipboardSanitizedWrite")
        CLIPBOARDSANITIZEDWRITE,

        @SerialName("displayCapture")
        DISPLAYCAPTURE,

        @SerialName("durableStorage")
        DURABLESTORAGE,

        @SerialName("flash")
        FLASH,

        @SerialName("geolocation")
        GEOLOCATION,

        @SerialName("idleDetection")
        IDLEDETECTION,

        @SerialName("localFonts")
        LOCALFONTS,

        @SerialName("midi")
        MIDI,

        @SerialName("midiSysex")
        MIDISYSEX,

        @SerialName("nfc")
        NFC,

        @SerialName("notifications")
        NOTIFICATIONS,

        @SerialName("paymentHandler")
        PAYMENTHANDLER,

        @SerialName("periodicBackgroundSync")
        PERIODICBACKGROUNDSYNC,

        @SerialName("protectedMediaIdentifier")
        PROTECTEDMEDIAIDENTIFIER,

        @SerialName("sensors")
        SENSORS,

        @SerialName("storageAccess")
        STORAGEACCESS,

        @SerialName("topLevelStorageAccess")
        TOPLEVELSTORAGEACCESS,

        @SerialName("videoCapture")
        VIDEOCAPTURE,

        @SerialName("videoCapturePanTiltZoom")
        VIDEOCAPTUREPANTILTZOOM,

        @SerialName("wakeLockScreen")
        WAKELOCKSCREEN,

        @SerialName("wakeLockSystem")
        WAKELOCKSYSTEM,

        @SerialName("windowManagement")
        WINDOWMANAGEMENT,
    }

    @Serializable
    public enum class PermissionSetting {
        @SerialName("granted")
        GRANTED,

        @SerialName("denied")
        DENIED,

        @SerialName("prompt")
        PROMPT,
    }

    /**
     * Definition of PermissionDescriptor defined in the Permissions API:
     * https://w3c.github.io/permissions/#dom-permissiondescriptor.
     */
    @Serializable
    public data class PermissionDescriptor(
        /**
         * Name of permission.
         * See https://cs.chromium.org/chromium/src/third_party/blink/renderer/modules/permissions/permission_descriptor.idl for valid permission names.
         */
        public val name: String,
        /**
         * For "midi" permission, may also specify sysex control.
         */
        public val sysex: Boolean? = null,
        /**
         * For "push" permission, may specify userVisibleOnly.
         * Note that userVisibleOnly = true is the only currently supported type.
         */
        public val userVisibleOnly: Boolean? = null,
        /**
         * For "clipboard" permission, may specify allowWithoutSanitization.
         */
        public val allowWithoutSanitization: Boolean? = null,
        /**
         * For "camera" permission, may specify panTiltZoom.
         */
        public val panTiltZoom: Boolean? = null,
    )

    /**
     * Browser command ids used by executeBrowserCommand.
     */
    @Serializable
    public enum class BrowserCommandId {
        @SerialName("openTabSearch")
        OPENTABSEARCH,

        @SerialName("closeTabSearch")
        CLOSETABSEARCH,
    }

    /**
     * Chrome histogram bucket.
     */
    @Serializable
    public data class Bucket(
        /**
         * Minimum value (inclusive).
         */
        public val low: Int,
        /**
         * Maximum value (exclusive).
         */
        public val high: Int,
        /**
         * Number of samples.
         */
        public val count: Int,
    )

    /**
     * Chrome histogram.
     */
    @Serializable
    public data class Histogram(
        /**
         * Name.
         */
        public val name: String,
        /**
         * Sum of sample values.
         */
        public val sum: Int,
        /**
         * Total number of samples.
         */
        public val count: Int,
        /**
         * Buckets.
         */
        public val buckets: List<Bucket>,
    )

    /**
     * Fired when page is about to start a download.
     */
    @Serializable
    public data class DownloadWillBeginParameter(
        /**
         * Id of the frame that caused the download to begin.
         */
        public val frameId: String,
        /**
         * Global unique identifier of the download.
         */
        public val guid: String,
        /**
         * URL of the resource being downloaded.
         */
        public val url: String,
        /**
         * Suggested file name of the resource (the actual name of the file saved on disk may differ).
         */
        public val suggestedFilename: String,
    )

    /**
     * Fired when download makes progress. Last call has |done| == true.
     */
    @Serializable
    public data class DownloadProgressParameter(
        /**
         * Global unique identifier of the download.
         */
        public val guid: String,
        /**
         * Total expected bytes to download.
         */
        public val totalBytes: Double,
        /**
         * Total bytes received.
         */
        public val receivedBytes: Double,
        /**
         * Download status.
         */
        public val state: String,
    )

    @Serializable
    public data class SetPermissionParameter(
        /**
         * Descriptor of permission to override.
         */
        public val permission: PermissionDescriptor,
        /**
         * Setting of the permission.
         */
        public val setting: PermissionSetting,
        /**
         * Origin the permission applies to, all origins if not specified.
         */
        public val origin: String? = null,
        /**
         * Context to override. When omitted, default browser context is used.
         */
        public val browserContextId: String? = null,
    )

    @Serializable
    public data class GrantPermissionsParameter(
        public val permissions: List<PermissionType>,
        /**
         * Origin the permission applies to, all origins if not specified.
         */
        public val origin: String? = null,
        /**
         * BrowserContext to override permissions. When omitted, default browser context is used.
         */
        public val browserContextId: String? = null,
    )

    @Serializable
    public data class ResetPermissionsParameter(
        /**
         * BrowserContext to reset permissions. When omitted, default browser context is used.
         */
        public val browserContextId: String? = null,
    )

    @Serializable
    public data class SetDownloadBehaviorParameter(
        /**
         * Whether to allow all or deny all download requests, or use default Chrome behavior if
         * available (otherwise deny). |allowAndName| allows download and names files according to
         * their dowmload guids.
         */
        public val behavior: String,
        /**
         * BrowserContext to set download behavior. When omitted, default browser context is used.
         */
        public val browserContextId: String? = null,
        /**
         * The default path to save downloaded files to. This is required if behavior is set to 'allow'
         * or 'allowAndName'.
         */
        public val downloadPath: String? = null,
        /**
         * Whether to emit download events (defaults to false).
         */
        public val eventsEnabled: Boolean? = null,
    )

    @Serializable
    public data class CancelDownloadParameter(
        /**
         * Global unique identifier of the download.
         */
        public val guid: String,
        /**
         * BrowserContext to perform the action in. When omitted, default browser context is used.
         */
        public val browserContextId: String? = null,
    )

    @Serializable
    public data class GetVersionReturn(
        /**
         * Protocol version.
         */
        public val protocolVersion: String,
        /**
         * Product name.
         */
        public val product: String,
        /**
         * Product revision.
         */
        public val revision: String,
        /**
         * User-Agent.
         */
        public val userAgent: String,
        /**
         * V8 version.
         */
        public val jsVersion: String,
    )

    @Serializable
    public data class GetBrowserCommandLineReturn(
        /**
         * Commandline parameters
         */
        public val arguments: List<String>,
    )

    @Serializable
    public data class GetHistogramsParameter(
        /**
         * Requested substring in name. Only histograms which have query as a
         * substring in their name are extracted. An empty or absent query returns
         * all histograms.
         */
        public val query: String? = null,
        /**
         * If true, retrieve delta since last delta call.
         */
        public val delta: Boolean? = null,
    )

    @Serializable
    public data class GetHistogramsReturn(
        /**
         * Histograms.
         */
        public val histograms: List<Histogram>,
    )

    @Serializable
    public data class GetHistogramParameter(
        /**
         * Requested histogram name.
         */
        public val name: String,
        /**
         * If true, retrieve delta since last delta call.
         */
        public val delta: Boolean? = null,
    )

    @Serializable
    public data class GetHistogramReturn(
        /**
         * Histogram.
         */
        public val histogram: Histogram,
    )

    @Serializable
    public data class GetWindowBoundsParameter(
        /**
         * Browser window id.
         */
        public val windowId: Int,
    )

    @Serializable
    public data class GetWindowBoundsReturn(
        /**
         * Bounds information of the window. When window state is 'minimized', the restored window
         * position and size are returned.
         */
        public val bounds: Bounds,
    )

    @Serializable
    public data class GetWindowForTargetParameter(
        /**
         * Devtools agent host id. If called as a part of the session, associated targetId is used.
         */
        public val targetId: String? = null,
    )

    @Serializable
    public data class GetWindowForTargetReturn(
        /**
         * Browser window id.
         */
        public val windowId: Int,
        /**
         * Bounds information of the window. When window state is 'minimized', the restored window
         * position and size are returned.
         */
        public val bounds: Bounds,
    )

    @Serializable
    public data class SetWindowBoundsParameter(
        /**
         * Browser window id.
         */
        public val windowId: Int,
        /**
         * New window bounds. The 'minimized', 'maximized' and 'fullscreen' states cannot be combined
         * with 'left', 'top', 'width' or 'height'. Leaves unspecified fields unchanged.
         */
        public val bounds: Bounds,
    )

    @Serializable
    public data class SetDockTileParameter(
        public val badgeLabel: String? = null,
        /**
         * Png encoded image. (Encoded as a base64 string when passed over JSON)
         */
        public val image: String? = null,
    )

    @Serializable
    public data class ExecuteBrowserCommandParameter(
        public val commandId: BrowserCommandId,
    )

    @Serializable
    public data class AddPrivacySandboxEnrollmentOverrideParameter(
        public val url: String,
    )
}
