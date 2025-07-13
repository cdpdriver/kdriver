package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.pwa: PWA
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(PWA(this))

/**
 * This domain allows interacting with the browser to control PWAs.
 */
public class PWA(
    private val cdp: CDP,
) : Domain {
    /**
     * Returns the following OS state for the given manifest id.
     */
    public suspend fun getOsAppState(
        args: GetOsAppStateParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetOsAppStateReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("PWA.getOsAppState", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the following OS state for the given manifest id.
     *
     * @param manifestId The id from the webapp's manifest file, commonly it's the url of the
     * site installing the webapp. See
     * https://web.dev/learn/pwa/web-app-manifest.
     */
    public suspend fun getOsAppState(manifestId: String): GetOsAppStateReturn {
        val parameter = GetOsAppStateParameter(manifestId = manifestId)
        return getOsAppState(parameter)
    }

    /**
     * Installs the given manifest identity, optionally using the given installUrlOrBundleUrl
     *
     * IWA-specific install description:
     * manifestId corresponds to isolated-app:// + web_package::SignedWebBundleId
     *
     * File installation mode:
     * The installUrlOrBundleUrl can be either file:// or http(s):// pointing
     * to a signed web bundle (.swbn). In this case SignedWebBundleId must correspond to
     * The .swbn file's signing key.
     *
     * Dev proxy installation mode:
     * installUrlOrBundleUrl must be http(s):// that serves dev mode IWA.
     * web_package::SignedWebBundleId must be of type dev proxy.
     *
     * The advantage of dev proxy mode is that all changes to IWA
     * automatically will be reflected in the running app without
     * reinstallation.
     *
     * To generate bundle id for proxy mode:
     * 1. Generate 32 random bytes.
     * 2. Add a specific suffix 0x00 at the end.
     * 3. Encode the entire sequence using Base32 without padding.
     *
     * If Chrome is not in IWA dev
     * mode, the installation will fail, regardless of the state of the allowlist.
     */
    public suspend fun install(args: InstallParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("PWA.install", parameter, mode)
    }

    /**
     * Installs the given manifest identity, optionally using the given installUrlOrBundleUrl
     *
     * IWA-specific install description:
     * manifestId corresponds to isolated-app:// + web_package::SignedWebBundleId
     *
     * File installation mode:
     * The installUrlOrBundleUrl can be either file:// or http(s):// pointing
     * to a signed web bundle (.swbn). In this case SignedWebBundleId must correspond to
     * The .swbn file's signing key.
     *
     * Dev proxy installation mode:
     * installUrlOrBundleUrl must be http(s):// that serves dev mode IWA.
     * web_package::SignedWebBundleId must be of type dev proxy.
     *
     * The advantage of dev proxy mode is that all changes to IWA
     * automatically will be reflected in the running app without
     * reinstallation.
     *
     * To generate bundle id for proxy mode:
     * 1. Generate 32 random bytes.
     * 2. Add a specific suffix 0x00 at the end.
     * 3. Encode the entire sequence using Base32 without padding.
     *
     * If Chrome is not in IWA dev
     * mode, the installation will fail, regardless of the state of the allowlist.
     *
     * @param manifestId No description
     * @param installUrlOrBundleUrl The location of the app or bundle overriding the one derived from the
     * manifestId.
     */
    public suspend fun install(manifestId: String, installUrlOrBundleUrl: String? = null) {
        val parameter = InstallParameter(manifestId = manifestId, installUrlOrBundleUrl = installUrlOrBundleUrl)
        install(parameter)
    }

    /**
     * Uninstalls the given manifest_id and closes any opened app windows.
     */
    public suspend fun uninstall(args: UninstallParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("PWA.uninstall", parameter, mode)
    }

    /**
     * Uninstalls the given manifest_id and closes any opened app windows.
     *
     * @param manifestId No description
     */
    public suspend fun uninstall(manifestId: String) {
        val parameter = UninstallParameter(manifestId = manifestId)
        uninstall(parameter)
    }

    /**
     * Launches the installed web app, or an url in the same web app instead of the
     * default start url if it is provided. Returns a page Target.TargetID which
     * can be used to attach to via Target.attachToTarget or similar APIs.
     */
    public suspend fun launch(args: LaunchParameter, mode: CommandMode = CommandMode.DEFAULT): LaunchReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("PWA.launch", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Launches the installed web app, or an url in the same web app instead of the
     * default start url if it is provided. Returns a page Target.TargetID which
     * can be used to attach to via Target.attachToTarget or similar APIs.
     *
     * @param manifestId No description
     * @param url No description
     */
    public suspend fun launch(manifestId: String, url: String? = null): LaunchReturn {
        val parameter = LaunchParameter(manifestId = manifestId, url = url)
        return launch(parameter)
    }

    /**
     * Opens one or more local files from an installed web app identified by its
     * manifestId. The web app needs to have file handlers registered to process
     * the files. The API returns one or more page Target.TargetIDs which can be
     * used to attach to via Target.attachToTarget or similar APIs.
     * If some files in the parameters cannot be handled by the web app, they will
     * be ignored. If none of the files can be handled, this API returns an error.
     * If no files are provided as the parameter, this API also returns an error.
     *
     * According to the definition of the file handlers in the manifest file, one
     * Target.TargetID may represent a page handling one or more files. The order
     * of the returned Target.TargetIDs is not guaranteed.
     *
     * TODO(crbug.com/339454034): Check the existences of the input files.
     */
    public suspend fun launchFilesInApp(
        args: LaunchFilesInAppParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): LaunchFilesInAppReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("PWA.launchFilesInApp", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Opens one or more local files from an installed web app identified by its
     * manifestId. The web app needs to have file handlers registered to process
     * the files. The API returns one or more page Target.TargetIDs which can be
     * used to attach to via Target.attachToTarget or similar APIs.
     * If some files in the parameters cannot be handled by the web app, they will
     * be ignored. If none of the files can be handled, this API returns an error.
     * If no files are provided as the parameter, this API also returns an error.
     *
     * According to the definition of the file handlers in the manifest file, one
     * Target.TargetID may represent a page handling one or more files. The order
     * of the returned Target.TargetIDs is not guaranteed.
     *
     * TODO(crbug.com/339454034): Check the existences of the input files.
     *
     * @param manifestId No description
     * @param files No description
     */
    public suspend fun launchFilesInApp(manifestId: String, files: List<String>): LaunchFilesInAppReturn {
        val parameter = LaunchFilesInAppParameter(manifestId = manifestId, files = files)
        return launchFilesInApp(parameter)
    }

    /**
     * Opens the current page in its web app identified by the manifest id, needs
     * to be called on a page target. This function returns immediately without
     * waiting for the app to finish loading.
     */
    public suspend fun openCurrentPageInApp(
        args: OpenCurrentPageInAppParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("PWA.openCurrentPageInApp", parameter, mode)
    }

    /**
     * Opens the current page in its web app identified by the manifest id, needs
     * to be called on a page target. This function returns immediately without
     * waiting for the app to finish loading.
     *
     * @param manifestId No description
     */
    public suspend fun openCurrentPageInApp(manifestId: String) {
        val parameter = OpenCurrentPageInAppParameter(manifestId = manifestId)
        openCurrentPageInApp(parameter)
    }

    /**
     * Changes user settings of the web app identified by its manifestId. If the
     * app was not installed, this command returns an error. Unset parameters will
     * be ignored; unrecognized values will cause an error.
     *
     * Unlike the ones defined in the manifest files of the web apps, these
     * settings are provided by the browser and controlled by the users, they
     * impact the way the browser handling the web apps.
     *
     * See the comment of each parameter.
     */
    public suspend fun changeAppUserSettings(
        args: ChangeAppUserSettingsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("PWA.changeAppUserSettings", parameter, mode)
    }

    /**
     * Changes user settings of the web app identified by its manifestId. If the
     * app was not installed, this command returns an error. Unset parameters will
     * be ignored; unrecognized values will cause an error.
     *
     * Unlike the ones defined in the manifest files of the web apps, these
     * settings are provided by the browser and controlled by the users, they
     * impact the way the browser handling the web apps.
     *
     * See the comment of each parameter.
     *
     * @param manifestId No description
     * @param linkCapturing If user allows the links clicked on by the user in the app's scope, or
     * extended scope if the manifest has scope extensions and the flags
     * `DesktopPWAsLinkCapturingWithScopeExtensions` and
     * `WebAppEnableScopeExtensions` are enabled.
     *
     * Note, the API does not support resetting the linkCapturing to the
     * initial value, uninstalling and installing the web app again will reset
     * it.
     *
     * TODO(crbug.com/339453269): Setting this value on ChromeOS is not
     * supported yet.
     * @param displayMode No description
     */
    public suspend fun changeAppUserSettings(
        manifestId: String,
        linkCapturing: Boolean? = null,
        displayMode: DisplayMode? = null,
    ) {
        val parameter = ChangeAppUserSettingsParameter(
            manifestId = manifestId,
            linkCapturing = linkCapturing,
            displayMode = displayMode
        )
        changeAppUserSettings(parameter)
    }

    /**
     * The following types are the replica of
     * https://crsrc.org/c/chrome/browser/web_applications/proto/web_app_os_integration_state.proto;drc=9910d3be894c8f142c977ba1023f30a656bc13fc;l=67
     */
    @Serializable
    public data class FileHandlerAccept(
        /**
         * New name of the mimetype according to
         * https://www.iana.org/assignments/media-types/media-types.xhtml
         */
        public val mediaType: String,
        public val fileExtensions: List<String>,
    )

    @Serializable
    public data class FileHandler(
        public val action: String,
        public val accepts: List<FileHandlerAccept>,
        public val displayName: String,
    )

    /**
     * If user prefers opening the app in browser or an app window.
     */
    @Serializable
    public enum class DisplayMode {
        @SerialName("standalone")
        STANDALONE,

        @SerialName("browser")
        BROWSER,
    }

    @Serializable
    public data class GetOsAppStateParameter(
        /**
         * The id from the webapp's manifest file, commonly it's the url of the
         * site installing the webapp. See
         * https://web.dev/learn/pwa/web-app-manifest.
         */
        public val manifestId: String,
    )

    @Serializable
    public data class GetOsAppStateReturn(
        public val badgeCount: Int,
        public val fileHandlers: List<FileHandler>,
    )

    @Serializable
    public data class InstallParameter(
        public val manifestId: String,
        /**
         * The location of the app or bundle overriding the one derived from the
         * manifestId.
         */
        public val installUrlOrBundleUrl: String? = null,
    )

    @Serializable
    public data class UninstallParameter(
        public val manifestId: String,
    )

    @Serializable
    public data class LaunchParameter(
        public val manifestId: String,
        public val url: String? = null,
    )

    @Serializable
    public data class LaunchReturn(
        /**
         * ID of the tab target created as a result.
         */
        public val targetId: String,
    )

    @Serializable
    public data class LaunchFilesInAppParameter(
        public val manifestId: String,
        public val files: List<String>,
    )

    @Serializable
    public data class LaunchFilesInAppReturn(
        /**
         * IDs of the tab targets created as the result.
         */
        public val targetIds: List<String>,
    )

    @Serializable
    public data class OpenCurrentPageInAppParameter(
        public val manifestId: String,
    )

    @Serializable
    public data class ChangeAppUserSettingsParameter(
        public val manifestId: String,
        /**
         * If user allows the links clicked on by the user in the app's scope, or
         * extended scope if the manifest has scope extensions and the flags
         * `DesktopPWAsLinkCapturingWithScopeExtensions` and
         * `WebAppEnableScopeExtensions` are enabled.
         *
         * Note, the API does not support resetting the linkCapturing to the
         * initial value, uninstalling and installing the web app again will reset
         * it.
         *
         * TODO(crbug.com/339453269): Setting this value on ChromeOS is not
         * supported yet.
         */
        public val linkCapturing: Boolean? = null,
        public val displayMode: DisplayMode? = null,
    )
}
