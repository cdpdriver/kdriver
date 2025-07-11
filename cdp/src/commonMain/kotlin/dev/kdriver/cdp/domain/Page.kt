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

public val CDP.page: Page
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Page(this))

/**
 * Actions and events related to the inspected page belong to the page domain.
 */
public class Page(
    private val cdp: CDP,
) : Domain {
    public val domContentEventFired: Flow<DomContentEventFiredParameter> = cdp
        .events
        .filter { it.method == "Page.domContentEventFired" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Emitted only when `page.interceptFileChooser` is enabled.
     */
    public val fileChooserOpened: Flow<FileChooserOpenedParameter> = cdp
        .events
        .filter { it.method == "Page.fileChooserOpened" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when frame has been attached to its parent.
     */
    public val frameAttached: Flow<FrameAttachedParameter> = cdp
        .events
        .filter { it.method == "Page.frameAttached" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when frame no longer has a scheduled navigation.
     */
    public val frameClearedScheduledNavigation: Flow<FrameClearedScheduledNavigationParameter> = cdp
        .events
        .filter { it.method == "Page.frameClearedScheduledNavigation" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when frame has been detached from its parent.
     */
    public val frameDetached: Flow<FrameDetachedParameter> = cdp
        .events
        .filter { it.method == "Page.frameDetached" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired before frame subtree is detached. Emitted before any frame of the
     * subtree is actually detached.
     */
    public val frameSubtreeWillBeDetached: Flow<FrameSubtreeWillBeDetachedParameter> = cdp
        .events
        .filter { it.method == "Page.frameSubtreeWillBeDetached" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired once navigation of the frame has completed. Frame is now associated with the new loader.
     */
    public val frameNavigated: Flow<FrameNavigatedParameter> = cdp
        .events
        .filter { it.method == "Page.frameNavigated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when opening document to write to.
     */
    public val documentOpened: Flow<DocumentOpenedParameter> = cdp
        .events
        .filter { it.method == "Page.documentOpened" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val frameResized: Flow<Unit> = cdp
        .events
        .filter { it.method == "Page.frameResized" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when a navigation starts. This event is fired for both
     * renderer-initiated and browser-initiated navigations. For renderer-initiated
     * navigations, the event is fired after `frameRequestedNavigation`.
     * Navigation may still be cancelled after the event is issued. Multiple events
     * can be fired for a single navigation, for example, when a same-document
     * navigation becomes a cross-document navigation (such as in the case of a
     * frameset).
     */
    public val frameStartedNavigating: Flow<FrameStartedNavigatingParameter> = cdp
        .events
        .filter { it.method == "Page.frameStartedNavigating" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when a renderer-initiated navigation is requested.
     * Navigation may still be cancelled after the event is issued.
     */
    public val frameRequestedNavigation: Flow<FrameRequestedNavigationParameter> = cdp
        .events
        .filter { it.method == "Page.frameRequestedNavigation" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when frame schedules a potential navigation.
     */
    public val frameScheduledNavigation: Flow<FrameScheduledNavigationParameter> = cdp
        .events
        .filter { it.method == "Page.frameScheduledNavigation" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when frame has started loading.
     */
    public val frameStartedLoading: Flow<FrameStartedLoadingParameter> = cdp
        .events
        .filter { it.method == "Page.frameStartedLoading" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when frame has stopped loading.
     */
    public val frameStoppedLoading: Flow<FrameStoppedLoadingParameter> = cdp
        .events
        .filter { it.method == "Page.frameStoppedLoading" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when page is about to start a download.
     * Deprecated. Use Browser.downloadWillBegin instead.
     */
    public val downloadWillBegin: Flow<DownloadWillBeginParameter> = cdp
        .events
        .filter { it.method == "Page.downloadWillBegin" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when download makes progress. Last call has |done| == true.
     * Deprecated. Use Browser.downloadProgress instead.
     */
    public val downloadProgress: Flow<DownloadProgressParameter> = cdp
        .events
        .filter { it.method == "Page.downloadProgress" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when interstitial page was hidden
     */
    public val interstitialHidden: Flow<Unit> = cdp
        .events
        .filter { it.method == "Page.interstitialHidden" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when interstitial page was shown
     */
    public val interstitialShown: Flow<Unit> = cdp
        .events
        .filter { it.method == "Page.interstitialShown" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when a JavaScript initiated dialog (alert, confirm, prompt, or onbeforeunload) has been
     * closed.
     */
    public val javascriptDialogClosed: Flow<JavascriptDialogClosedParameter> = cdp
        .events
        .filter { it.method == "Page.javascriptDialogClosed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when a JavaScript initiated dialog (alert, confirm, prompt, or onbeforeunload) is about to
     * open.
     */
    public val javascriptDialogOpening: Flow<JavascriptDialogOpeningParameter> = cdp
        .events
        .filter { it.method == "Page.javascriptDialogOpening" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired for lifecycle events (navigation, load, paint, etc) in the current
     * target (including local frames).
     */
    public val lifecycleEvent: Flow<LifecycleEventParameter> = cdp
        .events
        .filter { it.method == "Page.lifecycleEvent" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired for failed bfcache history navigations if BackForwardCache feature is enabled. Do
     * not assume any ordering with the Page.frameNavigated event. This event is fired only for
     * main-frame history navigation where the document changes (non-same-document navigations),
     * when bfcache navigation fails.
     */
    public val backForwardCacheNotUsed: Flow<BackForwardCacheNotUsedParameter> = cdp
        .events
        .filter { it.method == "Page.backForwardCacheNotUsed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    public val loadEventFired: Flow<LoadEventFiredParameter> = cdp
        .events
        .filter { it.method == "Page.loadEventFired" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when same-document navigation happens, e.g. due to history API usage or anchor navigation.
     */
    public val navigatedWithinDocument: Flow<NavigatedWithinDocumentParameter> = cdp
        .events
        .filter { it.method == "Page.navigatedWithinDocument" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Compressed image data requested by the `startScreencast`.
     */
    public val screencastFrame: Flow<ScreencastFrameParameter> = cdp
        .events
        .filter { it.method == "Page.screencastFrame" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when the page with currently enabled screencast was shown or hidden `.
     */
    public val screencastVisibilityChanged: Flow<ScreencastVisibilityChangedParameter> = cdp
        .events
        .filter { it.method == "Page.screencastVisibilityChanged" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Fired when a new window is going to be opened, via window.open(), link click, form submission,
     * etc.
     */
    public val windowOpen: Flow<WindowOpenParameter> = cdp
        .events
        .filter { it.method == "Page.windowOpen" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Issued for every compilation cache generated. Is only available
     * if Page.setGenerateCompilationCache is enabled.
     */
    public val compilationCacheProduced: Flow<CompilationCacheProducedParameter> = cdp
        .events
        .filter { it.method == "Page.compilationCacheProduced" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Deprecated, please use addScriptToEvaluateOnNewDocument instead.
     */
    @Deprecated(message = "")
    public suspend fun addScriptToEvaluateOnLoad(args: AddScriptToEvaluateOnLoadParameter): AddScriptToEvaluateOnLoadReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.addScriptToEvaluateOnLoad", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Deprecated, please use addScriptToEvaluateOnNewDocument instead.
     *
     * @param scriptSource No description
     */
    @Deprecated(message = "")
    public suspend fun addScriptToEvaluateOnLoad(scriptSource: String): AddScriptToEvaluateOnLoadReturn {
        val parameter = AddScriptToEvaluateOnLoadParameter(scriptSource = scriptSource)
        return addScriptToEvaluateOnLoad(parameter)
    }

    /**
     * Evaluates given script in every frame upon creation (before loading frame's scripts).
     */
    public suspend fun addScriptToEvaluateOnNewDocument(args: AddScriptToEvaluateOnNewDocumentParameter): AddScriptToEvaluateOnNewDocumentReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.addScriptToEvaluateOnNewDocument", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Evaluates given script in every frame upon creation (before loading frame's scripts).
     *
     * @param source No description
     * @param worldName If specified, creates an isolated world with the given name and evaluates given script in it.
     * This world name will be used as the ExecutionContextDescription::name when the corresponding
     * event is emitted.
     * @param includeCommandLineAPI Specifies whether command line API should be available to the script, defaults
     * to false.
     * @param runImmediately If true, runs the script immediately on existing execution contexts or worlds.
     * Default: false.
     */
    public suspend fun addScriptToEvaluateOnNewDocument(
        source: String,
        worldName: String? = null,
        includeCommandLineAPI: Boolean? = null,
        runImmediately: Boolean? = null,
    ): AddScriptToEvaluateOnNewDocumentReturn {
        val parameter = AddScriptToEvaluateOnNewDocumentParameter(
            source = source,
            worldName = worldName,
            includeCommandLineAPI = includeCommandLineAPI,
            runImmediately = runImmediately
        )
        return addScriptToEvaluateOnNewDocument(parameter)
    }

    /**
     * Brings page to front (activates tab).
     */
    public suspend fun bringToFront() {
        val parameter = null
        cdp.callCommand("Page.bringToFront", parameter)
    }

    /**
     * Capture page screenshot.
     */
    public suspend fun captureScreenshot(args: CaptureScreenshotParameter): CaptureScreenshotReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.captureScreenshot", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Capture page screenshot.
     *
     * @param format Image compression format (defaults to png).
     * @param quality Compression quality from range [0..100] (jpeg only).
     * @param clip Capture the screenshot of a given region only.
     * @param fromSurface Capture the screenshot from the surface, rather than the view. Defaults to true.
     * @param captureBeyondViewport Capture the screenshot beyond the viewport. Defaults to false.
     * @param optimizeForSpeed Optimize image encoding for speed, not for resulting size (defaults to false)
     */
    public suspend fun captureScreenshot(
        format: String? = null,
        quality: Int? = null,
        clip: Viewport? = null,
        fromSurface: Boolean? = null,
        captureBeyondViewport: Boolean? = null,
        optimizeForSpeed: Boolean? = null,
    ): CaptureScreenshotReturn {
        val parameter = CaptureScreenshotParameter(
            format = format,
            quality = quality,
            clip = clip,
            fromSurface = fromSurface,
            captureBeyondViewport = captureBeyondViewport,
            optimizeForSpeed = optimizeForSpeed
        )
        return captureScreenshot(parameter)
    }

    /**
     * Returns a snapshot of the page as a string. For MHTML format, the serialization includes
     * iframes, shadow DOM, external resources, and element-inline styles.
     */
    public suspend fun captureSnapshot(args: CaptureSnapshotParameter): CaptureSnapshotReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.captureSnapshot", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns a snapshot of the page as a string. For MHTML format, the serialization includes
     * iframes, shadow DOM, external resources, and element-inline styles.
     *
     * @param format Format (defaults to mhtml).
     */
    public suspend fun captureSnapshot(format: String? = null): CaptureSnapshotReturn {
        val parameter = CaptureSnapshotParameter(format = format)
        return captureSnapshot(parameter)
    }

    /**
     * Clears the overridden device metrics.
     */
    @Deprecated(message = "")
    public suspend fun clearDeviceMetricsOverride() {
        val parameter = null
        cdp.callCommand("Page.clearDeviceMetricsOverride", parameter)
    }

    /**
     * Clears the overridden Device Orientation.
     */
    @Deprecated(message = "")
    public suspend fun clearDeviceOrientationOverride() {
        val parameter = null
        cdp.callCommand("Page.clearDeviceOrientationOverride", parameter)
    }

    /**
     * Clears the overridden Geolocation Position and Error.
     */
    @Deprecated(message = "")
    public suspend fun clearGeolocationOverride() {
        val parameter = null
        cdp.callCommand("Page.clearGeolocationOverride", parameter)
    }

    /**
     * Creates an isolated world for the given frame.
     */
    public suspend fun createIsolatedWorld(args: CreateIsolatedWorldParameter): CreateIsolatedWorldReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.createIsolatedWorld", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Creates an isolated world for the given frame.
     *
     * @param frameId Id of the frame in which the isolated world should be created.
     * @param worldName An optional name which is reported in the Execution Context.
     * @param grantUniveralAccess Whether or not universal access should be granted to the isolated world. This is a powerful
     * option, use with caution.
     */
    public suspend fun createIsolatedWorld(
        frameId: String,
        worldName: String? = null,
        grantUniveralAccess: Boolean? = null,
    ): CreateIsolatedWorldReturn {
        val parameter = CreateIsolatedWorldParameter(
            frameId = frameId,
            worldName = worldName,
            grantUniveralAccess = grantUniveralAccess
        )
        return createIsolatedWorld(parameter)
    }

    /**
     * Deletes browser cookie with given name, domain and path.
     */
    @Deprecated(message = "")
    public suspend fun deleteCookie(args: DeleteCookieParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.deleteCookie", parameter)
    }

    /**
     * Deletes browser cookie with given name, domain and path.
     *
     * @param cookieName Name of the cookie to remove.
     * @param url URL to match cooke domain and path.
     */
    @Deprecated(message = "")
    public suspend fun deleteCookie(cookieName: String, url: String) {
        val parameter = DeleteCookieParameter(cookieName = cookieName, url = url)
        deleteCookie(parameter)
    }

    /**
     * Disables page domain notifications.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Page.disable", parameter)
    }

    /**
     * Enables page domain notifications.
     */
    public suspend fun enable(args: EnableParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.enable", parameter)
    }

    /**
     * Enables page domain notifications.
     *
     * @param enableFileChooserOpenedEvent If true, the `Page.fileChooserOpened` event will be emitted regardless of the state set by
     * `Page.setInterceptFileChooserDialog` command (default: false).
     */
    public suspend fun enable(enableFileChooserOpenedEvent: Boolean? = null) {
        val parameter = EnableParameter(enableFileChooserOpenedEvent = enableFileChooserOpenedEvent)
        enable(parameter)
    }

    /**
     * Gets the processed manifest for this current document.
     *   This API always waits for the manifest to be loaded.
     *   If manifestId is provided, and it does not match the manifest of the
     *     current document, this API errors out.
     *   If there is not a loaded page, this API errors out immediately.
     */
    public suspend fun getAppManifest(args: GetAppManifestParameter): GetAppManifestReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.getAppManifest", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Gets the processed manifest for this current document.
     *   This API always waits for the manifest to be loaded.
     *   If manifestId is provided, and it does not match the manifest of the
     *     current document, this API errors out.
     *   If there is not a loaded page, this API errors out immediately.
     *
     * @param manifestId No description
     */
    public suspend fun getAppManifest(manifestId: String? = null): GetAppManifestReturn {
        val parameter = GetAppManifestParameter(manifestId = manifestId)
        return getAppManifest(parameter)
    }

    public suspend fun getInstallabilityErrors(): GetInstallabilityErrorsReturn {
        val parameter = null
        val result = cdp.callCommand("Page.getInstallabilityErrors", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Deprecated because it's not guaranteed that the returned icon is in fact the one used for PWA installation.
     */
    @Deprecated(message = "")
    public suspend fun getManifestIcons(): GetManifestIconsReturn {
        val parameter = null
        val result = cdp.callCommand("Page.getManifestIcons", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns the unique (PWA) app id.
     * Only returns values if the feature flag 'WebAppEnableManifestId' is enabled
     */
    public suspend fun getAppId(): GetAppIdReturn {
        val parameter = null
        val result = cdp.callCommand("Page.getAppId", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    public suspend fun getAdScriptAncestry(args: GetAdScriptAncestryParameter): GetAdScriptAncestryReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.getAdScriptAncestry", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     *
     *
     * @param frameId No description
     */
    public suspend fun getAdScriptAncestry(frameId: String): GetAdScriptAncestryReturn {
        val parameter = GetAdScriptAncestryParameter(frameId = frameId)
        return getAdScriptAncestry(parameter)
    }

    /**
     * Returns present frame tree structure.
     */
    public suspend fun getFrameTree(): GetFrameTreeReturn {
        val parameter = null
        val result = cdp.callCommand("Page.getFrameTree", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns metrics relating to the layouting of the page, such as viewport bounds/scale.
     */
    public suspend fun getLayoutMetrics(): GetLayoutMetricsReturn {
        val parameter = null
        val result = cdp.callCommand("Page.getLayoutMetrics", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns navigation history for the current page.
     */
    public suspend fun getNavigationHistory(): GetNavigationHistoryReturn {
        val parameter = null
        val result = cdp.callCommand("Page.getNavigationHistory", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Resets navigation history for the current page.
     */
    public suspend fun resetNavigationHistory() {
        val parameter = null
        cdp.callCommand("Page.resetNavigationHistory", parameter)
    }

    /**
     * Returns content of the given resource.
     */
    public suspend fun getResourceContent(args: GetResourceContentParameter): GetResourceContentReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.getResourceContent", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns content of the given resource.
     *
     * @param frameId Frame id to get resource for.
     * @param url URL of the resource to get content for.
     */
    public suspend fun getResourceContent(frameId: String, url: String): GetResourceContentReturn {
        val parameter = GetResourceContentParameter(frameId = frameId, url = url)
        return getResourceContent(parameter)
    }

    /**
     * Returns present frame / resource tree structure.
     */
    public suspend fun getResourceTree(): GetResourceTreeReturn {
        val parameter = null
        val result = cdp.callCommand("Page.getResourceTree", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Accepts or dismisses a JavaScript initiated dialog (alert, confirm, prompt, or onbeforeunload).
     */
    public suspend fun handleJavaScriptDialog(args: HandleJavaScriptDialogParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.handleJavaScriptDialog", parameter)
    }

    /**
     * Accepts or dismisses a JavaScript initiated dialog (alert, confirm, prompt, or onbeforeunload).
     *
     * @param accept Whether to accept or dismiss the dialog.
     * @param promptText The text to enter into the dialog prompt before accepting. Used only if this is a prompt
     * dialog.
     */
    public suspend fun handleJavaScriptDialog(accept: Boolean, promptText: String? = null) {
        val parameter = HandleJavaScriptDialogParameter(accept = accept, promptText = promptText)
        handleJavaScriptDialog(parameter)
    }

    /**
     * Navigates current page to the given URL.
     */
    public suspend fun navigate(args: NavigateParameter): NavigateReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.navigate", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Navigates current page to the given URL.
     *
     * @param url URL to navigate the page to.
     * @param referrer Referrer URL.
     * @param transitionType Intended transition type.
     * @param frameId Frame id to navigate, if not specified navigates the top frame.
     * @param referrerPolicy Referrer-policy used for the navigation.
     */
    public suspend fun navigate(
        url: String,
        referrer: String? = null,
        transitionType: TransitionType? = null,
        frameId: String? = null,
        referrerPolicy: ReferrerPolicy? = null,
    ): NavigateReturn {
        val parameter = NavigateParameter(
            url = url,
            referrer = referrer,
            transitionType = transitionType,
            frameId = frameId,
            referrerPolicy = referrerPolicy
        )
        return navigate(parameter)
    }

    /**
     * Navigates current page to the given history entry.
     */
    public suspend fun navigateToHistoryEntry(args: NavigateToHistoryEntryParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.navigateToHistoryEntry", parameter)
    }

    /**
     * Navigates current page to the given history entry.
     *
     * @param entryId Unique id of the entry to navigate to.
     */
    public suspend fun navigateToHistoryEntry(entryId: Int) {
        val parameter = NavigateToHistoryEntryParameter(entryId = entryId)
        navigateToHistoryEntry(parameter)
    }

    /**
     * Print page as PDF.
     */
    public suspend fun printToPDF(args: PrintToPDFParameter): PrintToPDFReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.printToPDF", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Print page as PDF.
     *
     * @param landscape Paper orientation. Defaults to false.
     * @param displayHeaderFooter Display header and footer. Defaults to false.
     * @param printBackground Print background graphics. Defaults to false.
     * @param scale Scale of the webpage rendering. Defaults to 1.
     * @param paperWidth Paper width in inches. Defaults to 8.5 inches.
     * @param paperHeight Paper height in inches. Defaults to 11 inches.
     * @param marginTop Top margin in inches. Defaults to 1cm (~0.4 inches).
     * @param marginBottom Bottom margin in inches. Defaults to 1cm (~0.4 inches).
     * @param marginLeft Left margin in inches. Defaults to 1cm (~0.4 inches).
     * @param marginRight Right margin in inches. Defaults to 1cm (~0.4 inches).
     * @param pageRanges Paper ranges to print, one based, e.g., '1-5, 8, 11-13'. Pages are
     * printed in the document order, not in the order specified, and no
     * more than once.
     * Defaults to empty string, which implies the entire document is printed.
     * The page numbers are quietly capped to actual page count of the
     * document, and ranges beyond the end of the document are ignored.
     * If this results in no pages to print, an error is reported.
     * It is an error to specify a range with start greater than end.
     * @param headerTemplate HTML template for the print header. Should be valid HTML markup with following
     * classes used to inject printing values into them:
     * - `date`: formatted print date
     * - `title`: document title
     * - `url`: document location
     * - `pageNumber`: current page number
     * - `totalPages`: total pages in the document
     *
     * For example, `<span class=title></span>` would generate span containing the title.
     * @param footerTemplate HTML template for the print footer. Should use the same format as the `headerTemplate`.
     * @param preferCSSPageSize Whether or not to prefer page size as defined by css. Defaults to false,
     * in which case the content will be scaled to fit the paper size.
     * @param transferMode return as stream
     * @param generateTaggedPDF Whether or not to generate tagged (accessible) PDF. Defaults to embedder choice.
     * @param generateDocumentOutline Whether or not to embed the document outline into the PDF.
     */
    public suspend fun printToPDF(
        landscape: Boolean? = null,
        displayHeaderFooter: Boolean? = null,
        printBackground: Boolean? = null,
        scale: Double? = null,
        paperWidth: Double? = null,
        paperHeight: Double? = null,
        marginTop: Double? = null,
        marginBottom: Double? = null,
        marginLeft: Double? = null,
        marginRight: Double? = null,
        pageRanges: String? = null,
        headerTemplate: String? = null,
        footerTemplate: String? = null,
        preferCSSPageSize: Boolean? = null,
        transferMode: String? = null,
        generateTaggedPDF: Boolean? = null,
        generateDocumentOutline: Boolean? = null,
    ): PrintToPDFReturn {
        val parameter = PrintToPDFParameter(
            landscape = landscape,
            displayHeaderFooter = displayHeaderFooter,
            printBackground = printBackground,
            scale = scale,
            paperWidth = paperWidth,
            paperHeight = paperHeight,
            marginTop = marginTop,
            marginBottom = marginBottom,
            marginLeft = marginLeft,
            marginRight = marginRight,
            pageRanges = pageRanges,
            headerTemplate = headerTemplate,
            footerTemplate = footerTemplate,
            preferCSSPageSize = preferCSSPageSize,
            transferMode = transferMode,
            generateTaggedPDF = generateTaggedPDF,
            generateDocumentOutline = generateDocumentOutline
        )
        return printToPDF(parameter)
    }

    /**
     * Reloads given page optionally ignoring the cache.
     */
    public suspend fun reload(args: ReloadParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.reload", parameter)
    }

    /**
     * Reloads given page optionally ignoring the cache.
     *
     * @param ignoreCache If true, browser cache is ignored (as if the user pressed Shift+refresh).
     * @param scriptToEvaluateOnLoad If set, the script will be injected into all frames of the inspected page after reload.
     * Argument will be ignored if reloading dataURL origin.
     * @param loaderId If set, an error will be thrown if the target page's main frame's
     * loader id does not match the provided id. This prevents accidentally
     * reloading an unintended target in case there's a racing navigation.
     */
    public suspend fun reload(
        ignoreCache: Boolean? = null,
        scriptToEvaluateOnLoad: String? = null,
        loaderId: String? = null,
    ) {
        val parameter = ReloadParameter(
            ignoreCache = ignoreCache,
            scriptToEvaluateOnLoad = scriptToEvaluateOnLoad,
            loaderId = loaderId
        )
        reload(parameter)
    }

    /**
     * Deprecated, please use removeScriptToEvaluateOnNewDocument instead.
     */
    @Deprecated(message = "")
    public suspend fun removeScriptToEvaluateOnLoad(args: RemoveScriptToEvaluateOnLoadParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.removeScriptToEvaluateOnLoad", parameter)
    }

    /**
     * Deprecated, please use removeScriptToEvaluateOnNewDocument instead.
     *
     * @param identifier No description
     */
    @Deprecated(message = "")
    public suspend fun removeScriptToEvaluateOnLoad(identifier: String) {
        val parameter = RemoveScriptToEvaluateOnLoadParameter(identifier = identifier)
        removeScriptToEvaluateOnLoad(parameter)
    }

    /**
     * Removes given script from the list.
     */
    public suspend fun removeScriptToEvaluateOnNewDocument(args: RemoveScriptToEvaluateOnNewDocumentParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.removeScriptToEvaluateOnNewDocument", parameter)
    }

    /**
     * Removes given script from the list.
     *
     * @param identifier No description
     */
    public suspend fun removeScriptToEvaluateOnNewDocument(identifier: String) {
        val parameter = RemoveScriptToEvaluateOnNewDocumentParameter(identifier = identifier)
        removeScriptToEvaluateOnNewDocument(parameter)
    }

    /**
     * Acknowledges that a screencast frame has been received by the frontend.
     */
    public suspend fun screencastFrameAck(args: ScreencastFrameAckParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.screencastFrameAck", parameter)
    }

    /**
     * Acknowledges that a screencast frame has been received by the frontend.
     *
     * @param sessionId Frame number.
     */
    public suspend fun screencastFrameAck(sessionId: Int) {
        val parameter = ScreencastFrameAckParameter(sessionId = sessionId)
        screencastFrameAck(parameter)
    }

    /**
     * Searches for given string in resource content.
     */
    public suspend fun searchInResource(args: SearchInResourceParameter): SearchInResourceReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.searchInResource", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Searches for given string in resource content.
     *
     * @param frameId Frame id for resource to search in.
     * @param url URL of the resource to search in.
     * @param query String to search for.
     * @param caseSensitive If true, search is case sensitive.
     * @param isRegex If true, treats string parameter as regex.
     */
    public suspend fun searchInResource(
        frameId: String,
        url: String,
        query: String,
        caseSensitive: Boolean? = null,
        isRegex: Boolean? = null,
    ): SearchInResourceReturn {
        val parameter = SearchInResourceParameter(
            frameId = frameId,
            url = url,
            query = query,
            caseSensitive = caseSensitive,
            isRegex = isRegex
        )
        return searchInResource(parameter)
    }

    /**
     * Enable Chrome's experimental ad filter on all sites.
     */
    public suspend fun setAdBlockingEnabled(args: SetAdBlockingEnabledParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setAdBlockingEnabled", parameter)
    }

    /**
     * Enable Chrome's experimental ad filter on all sites.
     *
     * @param enabled Whether to block ads.
     */
    public suspend fun setAdBlockingEnabled(enabled: Boolean) {
        val parameter = SetAdBlockingEnabledParameter(enabled = enabled)
        setAdBlockingEnabled(parameter)
    }

    /**
     * Enable page Content Security Policy by-passing.
     */
    public suspend fun setBypassCSP(args: SetBypassCSPParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setBypassCSP", parameter)
    }

    /**
     * Enable page Content Security Policy by-passing.
     *
     * @param enabled Whether to bypass page CSP.
     */
    public suspend fun setBypassCSP(enabled: Boolean) {
        val parameter = SetBypassCSPParameter(enabled = enabled)
        setBypassCSP(parameter)
    }

    /**
     * Get Permissions Policy state on given frame.
     */
    public suspend fun getPermissionsPolicyState(args: GetPermissionsPolicyStateParameter): GetPermissionsPolicyStateReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.getPermissionsPolicyState", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Get Permissions Policy state on given frame.
     *
     * @param frameId No description
     */
    public suspend fun getPermissionsPolicyState(frameId: String): GetPermissionsPolicyStateReturn {
        val parameter = GetPermissionsPolicyStateParameter(frameId = frameId)
        return getPermissionsPolicyState(parameter)
    }

    /**
     * Get Origin Trials on given frame.
     */
    public suspend fun getOriginTrials(args: GetOriginTrialsParameter): GetOriginTrialsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Page.getOriginTrials", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Get Origin Trials on given frame.
     *
     * @param frameId No description
     */
    public suspend fun getOriginTrials(frameId: String): GetOriginTrialsReturn {
        val parameter = GetOriginTrialsParameter(frameId = frameId)
        return getOriginTrials(parameter)
    }

    /**
     * Overrides the values of device screen dimensions (window.screen.width, window.screen.height,
     * window.innerWidth, window.innerHeight, and "device-width"/"device-height"-related CSS media
     * query results).
     */
    @Deprecated(message = "")
    public suspend fun setDeviceMetricsOverride(args: SetDeviceMetricsOverrideParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setDeviceMetricsOverride", parameter)
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
     * @param viewport The viewport dimensions and scale. If not set, the override is cleared.
     */
    @Deprecated(message = "")
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
        screenOrientation: Emulation.ScreenOrientation? = null,
        viewport: Viewport? = null,
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
            viewport = viewport
        )
        setDeviceMetricsOverride(parameter)
    }

    /**
     * Overrides the Device Orientation.
     */
    @Deprecated(message = "")
    public suspend fun setDeviceOrientationOverride(args: SetDeviceOrientationOverrideParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setDeviceOrientationOverride", parameter)
    }

    /**
     * Overrides the Device Orientation.
     *
     * @param alpha Mock alpha
     * @param beta Mock beta
     * @param gamma Mock gamma
     */
    @Deprecated(message = "")
    public suspend fun setDeviceOrientationOverride(
        alpha: Double,
        beta: Double,
        gamma: Double,
    ) {
        val parameter = SetDeviceOrientationOverrideParameter(alpha = alpha, beta = beta, gamma = gamma)
        setDeviceOrientationOverride(parameter)
    }

    /**
     * Set generic font families.
     */
    public suspend fun setFontFamilies(args: SetFontFamiliesParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setFontFamilies", parameter)
    }

    /**
     * Set generic font families.
     *
     * @param fontFamilies Specifies font families to set. If a font family is not specified, it won't be changed.
     * @param forScripts Specifies font families to set for individual scripts.
     */
    public suspend fun setFontFamilies(fontFamilies: FontFamilies, forScripts: List<ScriptFontFamilies>? = null) {
        val parameter = SetFontFamiliesParameter(fontFamilies = fontFamilies, forScripts = forScripts)
        setFontFamilies(parameter)
    }

    /**
     * Set default font sizes.
     */
    public suspend fun setFontSizes(args: SetFontSizesParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setFontSizes", parameter)
    }

    /**
     * Set default font sizes.
     *
     * @param fontSizes Specifies font sizes to set. If a font size is not specified, it won't be changed.
     */
    public suspend fun setFontSizes(fontSizes: FontSizes) {
        val parameter = SetFontSizesParameter(fontSizes = fontSizes)
        setFontSizes(parameter)
    }

    /**
     * Sets given markup as the document's HTML.
     */
    public suspend fun setDocumentContent(args: SetDocumentContentParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setDocumentContent", parameter)
    }

    /**
     * Sets given markup as the document's HTML.
     *
     * @param frameId Frame id to set HTML for.
     * @param html HTML content to set.
     */
    public suspend fun setDocumentContent(frameId: String, html: String) {
        val parameter = SetDocumentContentParameter(frameId = frameId, html = html)
        setDocumentContent(parameter)
    }

    /**
     * Set the behavior when downloading a file.
     */
    @Deprecated(message = "")
    public suspend fun setDownloadBehavior(args: SetDownloadBehaviorParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setDownloadBehavior", parameter)
    }

    /**
     * Set the behavior when downloading a file.
     *
     * @param behavior Whether to allow all or deny all download requests, or use default Chrome behavior if
     * available (otherwise deny).
     * @param downloadPath The default path to save downloaded files to. This is required if behavior is set to 'allow'
     */
    @Deprecated(message = "")
    public suspend fun setDownloadBehavior(behavior: String, downloadPath: String? = null) {
        val parameter = SetDownloadBehaviorParameter(behavior = behavior, downloadPath = downloadPath)
        setDownloadBehavior(parameter)
    }

    /**
     * Overrides the Geolocation Position or Error. Omitting any of the parameters emulates position
     * unavailable.
     */
    @Deprecated(message = "")
    public suspend fun setGeolocationOverride(args: SetGeolocationOverrideParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setGeolocationOverride", parameter)
    }

    /**
     * Overrides the Geolocation Position or Error. Omitting any of the parameters emulates position
     * unavailable.
     *
     * @param latitude Mock latitude
     * @param longitude Mock longitude
     * @param accuracy Mock accuracy
     */
    @Deprecated(message = "")
    public suspend fun setGeolocationOverride(
        latitude: Double? = null,
        longitude: Double? = null,
        accuracy: Double? = null,
    ) {
        val parameter = SetGeolocationOverrideParameter(latitude = latitude, longitude = longitude, accuracy = accuracy)
        setGeolocationOverride(parameter)
    }

    /**
     * Controls whether page will emit lifecycle events.
     */
    public suspend fun setLifecycleEventsEnabled(args: SetLifecycleEventsEnabledParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setLifecycleEventsEnabled", parameter)
    }

    /**
     * Controls whether page will emit lifecycle events.
     *
     * @param enabled If true, starts emitting lifecycle events.
     */
    public suspend fun setLifecycleEventsEnabled(enabled: Boolean) {
        val parameter = SetLifecycleEventsEnabledParameter(enabled = enabled)
        setLifecycleEventsEnabled(parameter)
    }

    /**
     * Toggles mouse event-based touch event emulation.
     */
    @Deprecated(message = "")
    public suspend fun setTouchEmulationEnabled(args: SetTouchEmulationEnabledParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setTouchEmulationEnabled", parameter)
    }

    /**
     * Toggles mouse event-based touch event emulation.
     *
     * @param enabled Whether the touch event emulation should be enabled.
     * @param configuration Touch/gesture events configuration. Default: current platform.
     */
    @Deprecated(message = "")
    public suspend fun setTouchEmulationEnabled(enabled: Boolean, configuration: String? = null) {
        val parameter = SetTouchEmulationEnabledParameter(enabled = enabled, configuration = configuration)
        setTouchEmulationEnabled(parameter)
    }

    /**
     * Starts sending each frame using the `screencastFrame` event.
     */
    public suspend fun startScreencast(args: StartScreencastParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.startScreencast", parameter)
    }

    /**
     * Starts sending each frame using the `screencastFrame` event.
     *
     * @param format Image compression format.
     * @param quality Compression quality from range [0..100].
     * @param maxWidth Maximum screenshot width.
     * @param maxHeight Maximum screenshot height.
     * @param everyNthFrame Send every n-th frame.
     */
    public suspend fun startScreencast(
        format: String? = null,
        quality: Int? = null,
        maxWidth: Int? = null,
        maxHeight: Int? = null,
        everyNthFrame: Int? = null,
    ) {
        val parameter = StartScreencastParameter(
            format = format,
            quality = quality,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            everyNthFrame = everyNthFrame
        )
        startScreencast(parameter)
    }

    /**
     * Force the page stop all navigations and pending resource fetches.
     */
    public suspend fun stopLoading() {
        val parameter = null
        cdp.callCommand("Page.stopLoading", parameter)
    }

    /**
     * Crashes renderer on the IO thread, generates minidumps.
     */
    public suspend fun crash() {
        val parameter = null
        cdp.callCommand("Page.crash", parameter)
    }

    /**
     * Tries to close page, running its beforeunload hooks, if any.
     */
    public suspend fun close() {
        val parameter = null
        cdp.callCommand("Page.close", parameter)
    }

    /**
     * Tries to update the web lifecycle state of the page.
     * It will transition the page to the given state according to:
     * https://github.com/WICG/web-lifecycle/
     */
    public suspend fun setWebLifecycleState(args: SetWebLifecycleStateParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setWebLifecycleState", parameter)
    }

    /**
     * Tries to update the web lifecycle state of the page.
     * It will transition the page to the given state according to:
     * https://github.com/WICG/web-lifecycle/
     *
     * @param state Target lifecycle state
     */
    public suspend fun setWebLifecycleState(state: String) {
        val parameter = SetWebLifecycleStateParameter(state = state)
        setWebLifecycleState(parameter)
    }

    /**
     * Stops sending each frame in the `screencastFrame`.
     */
    public suspend fun stopScreencast() {
        val parameter = null
        cdp.callCommand("Page.stopScreencast", parameter)
    }

    /**
     * Requests backend to produce compilation cache for the specified scripts.
     * `scripts` are appended to the list of scripts for which the cache
     * would be produced. The list may be reset during page navigation.
     * When script with a matching URL is encountered, the cache is optionally
     * produced upon backend discretion, based on internal heuristics.
     * See also: `Page.compilationCacheProduced`.
     */
    public suspend fun produceCompilationCache(args: ProduceCompilationCacheParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.produceCompilationCache", parameter)
    }

    /**
     * Requests backend to produce compilation cache for the specified scripts.
     * `scripts` are appended to the list of scripts for which the cache
     * would be produced. The list may be reset during page navigation.
     * When script with a matching URL is encountered, the cache is optionally
     * produced upon backend discretion, based on internal heuristics.
     * See also: `Page.compilationCacheProduced`.
     *
     * @param scripts No description
     */
    public suspend fun produceCompilationCache(scripts: List<CompilationCacheParams>) {
        val parameter = ProduceCompilationCacheParameter(scripts = scripts)
        produceCompilationCache(parameter)
    }

    /**
     * Seeds compilation cache for given url. Compilation cache does not survive
     * cross-process navigation.
     */
    public suspend fun addCompilationCache(args: AddCompilationCacheParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.addCompilationCache", parameter)
    }

    /**
     * Seeds compilation cache for given url. Compilation cache does not survive
     * cross-process navigation.
     *
     * @param url No description
     * @param data Base64-encoded data (Encoded as a base64 string when passed over JSON)
     */
    public suspend fun addCompilationCache(url: String, `data`: String) {
        val parameter = AddCompilationCacheParameter(url = url, data = data)
        addCompilationCache(parameter)
    }

    /**
     * Clears seeded compilation cache.
     */
    public suspend fun clearCompilationCache() {
        val parameter = null
        cdp.callCommand("Page.clearCompilationCache", parameter)
    }

    /**
     * Sets the Secure Payment Confirmation transaction mode.
     * https://w3c.github.io/secure-payment-confirmation/#sctn-automation-set-spc-transaction-mode
     */
    public suspend fun setSPCTransactionMode(args: SetSPCTransactionModeParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setSPCTransactionMode", parameter)
    }

    /**
     * Sets the Secure Payment Confirmation transaction mode.
     * https://w3c.github.io/secure-payment-confirmation/#sctn-automation-set-spc-transaction-mode
     *
     * @param mode No description
     */
    public suspend fun setSPCTransactionMode(mode: String) {
        val parameter = SetSPCTransactionModeParameter(mode = mode)
        setSPCTransactionMode(parameter)
    }

    /**
     * Extensions for Custom Handlers API:
     * https://html.spec.whatwg.org/multipage/system-state.html#rph-automation
     */
    public suspend fun setRPHRegistrationMode(args: SetRPHRegistrationModeParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setRPHRegistrationMode", parameter)
    }

    /**
     * Extensions for Custom Handlers API:
     * https://html.spec.whatwg.org/multipage/system-state.html#rph-automation
     *
     * @param mode No description
     */
    public suspend fun setRPHRegistrationMode(mode: String) {
        val parameter = SetRPHRegistrationModeParameter(mode = mode)
        setRPHRegistrationMode(parameter)
    }

    /**
     * Generates a report for testing.
     */
    public suspend fun generateTestReport(args: GenerateTestReportParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.generateTestReport", parameter)
    }

    /**
     * Generates a report for testing.
     *
     * @param message Message to be displayed in the report.
     * @param group Specifies the endpoint group to deliver the report to.
     */
    public suspend fun generateTestReport(message: String, group: String? = null) {
        val parameter = GenerateTestReportParameter(message = message, group = group)
        generateTestReport(parameter)
    }

    /**
     * Pauses page execution. Can be resumed using generic Runtime.runIfWaitingForDebugger.
     */
    public suspend fun waitForDebugger() {
        val parameter = null
        cdp.callCommand("Page.waitForDebugger", parameter)
    }

    /**
     * Intercept file chooser requests and transfer control to protocol clients.
     * When file chooser interception is enabled, native file chooser dialog is not shown.
     * Instead, a protocol event `Page.fileChooserOpened` is emitted.
     */
    public suspend fun setInterceptFileChooserDialog(args: SetInterceptFileChooserDialogParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setInterceptFileChooserDialog", parameter)
    }

    /**
     * Intercept file chooser requests and transfer control to protocol clients.
     * When file chooser interception is enabled, native file chooser dialog is not shown.
     * Instead, a protocol event `Page.fileChooserOpened` is emitted.
     *
     * @param enabled No description
     * @param cancel If true, cancels the dialog by emitting relevant events (if any)
     * in addition to not showing it if the interception is enabled
     * (default: false).
     */
    public suspend fun setInterceptFileChooserDialog(enabled: Boolean, cancel: Boolean? = null) {
        val parameter = SetInterceptFileChooserDialogParameter(enabled = enabled, cancel = cancel)
        setInterceptFileChooserDialog(parameter)
    }

    /**
     * Enable/disable prerendering manually.
     *
     * This command is a short-term solution for https://crbug.com/1440085.
     * See https://docs.google.com/document/d/12HVmFxYj5Jc-eJr5OmWsa2bqTJsbgGLKI6ZIyx0_wpA
     * for more details.
     *
     * TODO(https://crbug.com/1440085): Remove this once Puppeteer supports tab targets.
     */
    public suspend fun setPrerenderingAllowed(args: SetPrerenderingAllowedParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Page.setPrerenderingAllowed", parameter)
    }

    /**
     * Enable/disable prerendering manually.
     *
     * This command is a short-term solution for https://crbug.com/1440085.
     * See https://docs.google.com/document/d/12HVmFxYj5Jc-eJr5OmWsa2bqTJsbgGLKI6ZIyx0_wpA
     * for more details.
     *
     * TODO(https://crbug.com/1440085): Remove this once Puppeteer supports tab targets.
     *
     * @param isAllowed No description
     */
    public suspend fun setPrerenderingAllowed(isAllowed: Boolean) {
        val parameter = SetPrerenderingAllowedParameter(isAllowed = isAllowed)
        setPrerenderingAllowed(parameter)
    }

    /**
     * Indicates whether a frame has been identified as an ad.
     */
    @Serializable
    public enum class AdFrameType {
        @SerialName("none")
        NONE,

        @SerialName("child")
        CHILD,

        @SerialName("root")
        ROOT,
    }

    @Serializable
    public enum class AdFrameExplanation {
        @SerialName("ParentIsAd")
        PARENTISAD,

        @SerialName("CreatedByAdScript")
        CREATEDBYADSCRIPT,

        @SerialName("MatchedBlockingRule")
        MATCHEDBLOCKINGRULE,
    }

    /**
     * Indicates whether a frame has been identified as an ad and why.
     */
    @Serializable
    public data class AdFrameStatus(
        public val adFrameType: AdFrameType,
        public val explanations: List<AdFrameExplanation>? = null,
    )

    /**
     * Identifies the script which caused a script or frame to be labelled as an
     * ad.
     */
    @Serializable
    public data class AdScriptId(
        /**
         * Script Id of the script which caused a script or frame to be labelled as
         * an ad.
         */
        public val scriptId: String,
        /**
         * Id of scriptId's debugger.
         */
        public val debuggerId: String,
    )

    /**
     * Encapsulates the script ancestry and the root script filterlist rule that
     * caused the frame to be labelled as an ad. Only created when `ancestryChain`
     * is not empty.
     */
    @Serializable
    public data class AdScriptAncestry(
        /**
         * A chain of `AdScriptId`s representing the ancestry of an ad script that
         * led to the creation of a frame. The chain is ordered from the script
         * itself (lower level) up to its root ancestor that was flagged by
         * filterlist.
         */
        public val ancestryChain: List<AdScriptId>,
        /**
         * The filterlist rule that caused the root (last) script in
         * `ancestryChain` to be ad-tagged. Only populated if the rule is
         * available.
         */
        public val rootScriptFilterlistRule: String? = null,
    )

    /**
     * Indicates whether the frame is a secure context and why it is the case.
     */
    @Serializable
    public enum class SecureContextType {
        @SerialName("Secure")
        SECURE,

        @SerialName("SecureLocalhost")
        SECURELOCALHOST,

        @SerialName("InsecureScheme")
        INSECURESCHEME,

        @SerialName("InsecureAncestor")
        INSECUREANCESTOR,
    }

    /**
     * Indicates whether the frame is cross-origin isolated and why it is the case.
     */
    @Serializable
    public enum class CrossOriginIsolatedContextType {
        @SerialName("Isolated")
        ISOLATED,

        @SerialName("NotIsolated")
        NOTISOLATED,

        @SerialName("NotIsolatedFeatureDisabled")
        NOTISOLATEDFEATUREDISABLED,
    }

    @Serializable
    public enum class GatedAPIFeatures {
        @SerialName("SharedArrayBuffers")
        SHAREDARRAYBUFFERS,

        @SerialName("SharedArrayBuffersTransferAllowed")
        SHAREDARRAYBUFFERSTRANSFERALLOWED,

        @SerialName("PerformanceMeasureMemory")
        PERFORMANCEMEASUREMEMORY,

        @SerialName("PerformanceProfile")
        PERFORMANCEPROFILE,
    }

    /**
     * All Permissions Policy features. This enum should match the one defined
     * in services/network/public/cpp/permissions_policy/permissions_policy_features.json5.
     * LINT.IfChange(PermissionsPolicyFeature)
     */
    @Serializable
    public enum class PermissionsPolicyFeature {
        @SerialName("accelerometer")
        ACCELEROMETER,

        @SerialName("all-screens-capture")
        ALL_SCREENS_CAPTURE,

        @SerialName("ambient-light-sensor")
        AMBIENT_LIGHT_SENSOR,

        @SerialName("attribution-reporting")
        ATTRIBUTION_REPORTING,

        @SerialName("autoplay")
        AUTOPLAY,

        @SerialName("bluetooth")
        BLUETOOTH,

        @SerialName("browsing-topics")
        BROWSING_TOPICS,

        @SerialName("camera")
        CAMERA,

        @SerialName("captured-surface-control")
        CAPTURED_SURFACE_CONTROL,

        @SerialName("ch-dpr")
        CH_DPR,

        @SerialName("ch-device-memory")
        CH_DEVICE_MEMORY,

        @SerialName("ch-downlink")
        CH_DOWNLINK,

        @SerialName("ch-ect")
        CH_ECT,

        @SerialName("ch-prefers-color-scheme")
        CH_PREFERS_COLOR_SCHEME,

        @SerialName("ch-prefers-reduced-motion")
        CH_PREFERS_REDUCED_MOTION,

        @SerialName("ch-prefers-reduced-transparency")
        CH_PREFERS_REDUCED_TRANSPARENCY,

        @SerialName("ch-rtt")
        CH_RTT,

        @SerialName("ch-save-data")
        CH_SAVE_DATA,

        @SerialName("ch-ua")
        CH_UA,

        @SerialName("ch-ua-arch")
        CH_UA_ARCH,

        @SerialName("ch-ua-bitness")
        CH_UA_BITNESS,

        @SerialName("ch-ua-high-entropy-values")
        CH_UA_HIGH_ENTROPY_VALUES,

        @SerialName("ch-ua-platform")
        CH_UA_PLATFORM,

        @SerialName("ch-ua-model")
        CH_UA_MODEL,

        @SerialName("ch-ua-mobile")
        CH_UA_MOBILE,

        @SerialName("ch-ua-form-factors")
        CH_UA_FORM_FACTORS,

        @SerialName("ch-ua-full-version")
        CH_UA_FULL_VERSION,

        @SerialName("ch-ua-full-version-list")
        CH_UA_FULL_VERSION_LIST,

        @SerialName("ch-ua-platform-version")
        CH_UA_PLATFORM_VERSION,

        @SerialName("ch-ua-wow64")
        CH_UA_WOW64,

        @SerialName("ch-viewport-height")
        CH_VIEWPORT_HEIGHT,

        @SerialName("ch-viewport-width")
        CH_VIEWPORT_WIDTH,

        @SerialName("ch-width")
        CH_WIDTH,

        @SerialName("clipboard-read")
        CLIPBOARD_READ,

        @SerialName("clipboard-write")
        CLIPBOARD_WRITE,

        @SerialName("compute-pressure")
        COMPUTE_PRESSURE,

        @SerialName("controlled-frame")
        CONTROLLED_FRAME,

        @SerialName("cross-origin-isolated")
        CROSS_ORIGIN_ISOLATED,

        @SerialName("deferred-fetch")
        DEFERRED_FETCH,

        @SerialName("deferred-fetch-minimal")
        DEFERRED_FETCH_MINIMAL,

        @SerialName("device-attributes")
        DEVICE_ATTRIBUTES,

        @SerialName("digital-credentials-get")
        DIGITAL_CREDENTIALS_GET,

        @SerialName("direct-sockets")
        DIRECT_SOCKETS,

        @SerialName("direct-sockets-private")
        DIRECT_SOCKETS_PRIVATE,

        @SerialName("display-capture")
        DISPLAY_CAPTURE,

        @SerialName("document-domain")
        DOCUMENT_DOMAIN,

        @SerialName("encrypted-media")
        ENCRYPTED_MEDIA,

        @SerialName("execution-while-out-of-viewport")
        EXECUTION_WHILE_OUT_OF_VIEWPORT,

        @SerialName("execution-while-not-rendered")
        EXECUTION_WHILE_NOT_RENDERED,

        @SerialName("fenced-unpartitioned-storage-read")
        FENCED_UNPARTITIONED_STORAGE_READ,

        @SerialName("focus-without-user-activation")
        FOCUS_WITHOUT_USER_ACTIVATION,

        @SerialName("fullscreen")
        FULLSCREEN,

        @SerialName("frobulate")
        FROBULATE,

        @SerialName("gamepad")
        GAMEPAD,

        @SerialName("geolocation")
        GEOLOCATION,

        @SerialName("gyroscope")
        GYROSCOPE,

        @SerialName("hid")
        HID,

        @SerialName("identity-credentials-get")
        IDENTITY_CREDENTIALS_GET,

        @SerialName("idle-detection")
        IDLE_DETECTION,

        @SerialName("interest-cohort")
        INTEREST_COHORT,

        @SerialName("join-ad-interest-group")
        JOIN_AD_INTEREST_GROUP,

        @SerialName("keyboard-map")
        KEYBOARD_MAP,

        @SerialName("language-detector")
        LANGUAGE_DETECTOR,

        @SerialName("language-model")
        LANGUAGE_MODEL,

        @SerialName("local-fonts")
        LOCAL_FONTS,

        @SerialName("local-network-access")
        LOCAL_NETWORK_ACCESS,

        @SerialName("magnetometer")
        MAGNETOMETER,

        @SerialName("media-playback-while-not-visible")
        MEDIA_PLAYBACK_WHILE_NOT_VISIBLE,

        @SerialName("microphone")
        MICROPHONE,

        @SerialName("midi")
        MIDI,

        @SerialName("on-device-speech-recognition")
        ON_DEVICE_SPEECH_RECOGNITION,

        @SerialName("otp-credentials")
        OTP_CREDENTIALS,

        @SerialName("payment")
        PAYMENT,

        @SerialName("picture-in-picture")
        PICTURE_IN_PICTURE,

        @SerialName("popins")
        POPINS,

        @SerialName("private-aggregation")
        PRIVATE_AGGREGATION,

        @SerialName("private-state-token-issuance")
        PRIVATE_STATE_TOKEN_ISSUANCE,

        @SerialName("private-state-token-redemption")
        PRIVATE_STATE_TOKEN_REDEMPTION,

        @SerialName("publickey-credentials-create")
        PUBLICKEY_CREDENTIALS_CREATE,

        @SerialName("publickey-credentials-get")
        PUBLICKEY_CREDENTIALS_GET,

        @SerialName("record-ad-auction-events")
        RECORD_AD_AUCTION_EVENTS,

        @SerialName("rewriter")
        REWRITER,

        @SerialName("run-ad-auction")
        RUN_AD_AUCTION,

        @SerialName("screen-wake-lock")
        SCREEN_WAKE_LOCK,

        @SerialName("serial")
        SERIAL,

        @SerialName("shared-autofill")
        SHARED_AUTOFILL,

        @SerialName("shared-storage")
        SHARED_STORAGE,

        @SerialName("shared-storage-select-url")
        SHARED_STORAGE_SELECT_URL,

        @SerialName("smart-card")
        SMART_CARD,

        @SerialName("speaker-selection")
        SPEAKER_SELECTION,

        @SerialName("storage-access")
        STORAGE_ACCESS,

        @SerialName("sub-apps")
        SUB_APPS,

        @SerialName("summarizer")
        SUMMARIZER,

        @SerialName("sync-xhr")
        SYNC_XHR,

        @SerialName("translator")
        TRANSLATOR,

        @SerialName("unload")
        UNLOAD,

        @SerialName("usb")
        USB,

        @SerialName("usb-unrestricted")
        USB_UNRESTRICTED,

        @SerialName("vertical-scroll")
        VERTICAL_SCROLL,

        @SerialName("web-app-installation")
        WEB_APP_INSTALLATION,

        @SerialName("web-printing")
        WEB_PRINTING,

        @SerialName("web-share")
        WEB_SHARE,

        @SerialName("window-management")
        WINDOW_MANAGEMENT,

        @SerialName("writer")
        WRITER,

        @SerialName("xr-spatial-tracking")
        XR_SPATIAL_TRACKING,
    }

    /**
     * Reason for a permissions policy feature to be disabled.
     */
    @Serializable
    public enum class PermissionsPolicyBlockReason {
        @SerialName("Header")
        HEADER,

        @SerialName("IframeAttribute")
        IFRAMEATTRIBUTE,

        @SerialName("InFencedFrameTree")
        INFENCEDFRAMETREE,

        @SerialName("InIsolatedApp")
        INISOLATEDAPP,
    }

    @Serializable
    public data class PermissionsPolicyBlockLocator(
        public val frameId: String,
        public val blockReason: PermissionsPolicyBlockReason,
    )

    @Serializable
    public data class PermissionsPolicyFeatureState(
        public val feature: PermissionsPolicyFeature,
        public val allowed: Boolean,
        public val locator: PermissionsPolicyBlockLocator? = null,
    )

    /**
     * Origin Trial(https://www.chromium.org/blink/origin-trials) support.
     * Status for an Origin Trial token.
     */
    @Serializable
    public enum class OriginTrialTokenStatus {
        @SerialName("Success")
        SUCCESS,

        @SerialName("NotSupported")
        NOTSUPPORTED,

        @SerialName("Insecure")
        INSECURE,

        @SerialName("Expired")
        EXPIRED,

        @SerialName("WrongOrigin")
        WRONGORIGIN,

        @SerialName("InvalidSignature")
        INVALIDSIGNATURE,

        @SerialName("Malformed")
        MALFORMED,

        @SerialName("WrongVersion")
        WRONGVERSION,

        @SerialName("FeatureDisabled")
        FEATUREDISABLED,

        @SerialName("TokenDisabled")
        TOKENDISABLED,

        @SerialName("FeatureDisabledForUser")
        FEATUREDISABLEDFORUSER,

        @SerialName("UnknownTrial")
        UNKNOWNTRIAL,
    }

    /**
     * Status for an Origin Trial.
     */
    @Serializable
    public enum class OriginTrialStatus {
        @SerialName("Enabled")
        ENABLED,

        @SerialName("ValidTokenNotProvided")
        VALIDTOKENNOTPROVIDED,

        @SerialName("OSNotSupported")
        OSNOTSUPPORTED,

        @SerialName("TrialNotAllowed")
        TRIALNOTALLOWED,
    }

    @Serializable
    public enum class OriginTrialUsageRestriction {
        @SerialName("None")
        NONE,

        @SerialName("Subset")
        SUBSET,
    }

    @Serializable
    public data class OriginTrialToken(
        public val origin: String,
        public val matchSubDomains: Boolean,
        public val trialName: String,
        public val expiryTime: Double,
        public val isThirdParty: Boolean,
        public val usageRestriction: OriginTrialUsageRestriction,
    )

    @Serializable
    public data class OriginTrialTokenWithStatus(
        public val rawTokenText: String,
        /**
         * `parsedToken` is present only when the token is extractable and
         * parsable.
         */
        public val parsedToken: OriginTrialToken? = null,
        public val status: OriginTrialTokenStatus,
    )

    @Serializable
    public data class OriginTrial(
        public val trialName: String,
        public val status: OriginTrialStatus,
        public val tokensWithStatus: List<OriginTrialTokenWithStatus>,
    )

    /**
     * Additional information about the frame document's security origin.
     */
    @Serializable
    public data class SecurityOriginDetails(
        /**
         * Indicates whether the frame document's security origin is one
         * of the local hostnames (e.g. "localhost") or IP addresses (IPv4
         * 127.0.0.0/8 or IPv6 ::1).
         */
        public val isLocalhost: Boolean,
    )

    /**
     * Information about the Frame on the page.
     */
    @Serializable
    public data class Frame(
        /**
         * Frame unique identifier.
         */
        public val id: String,
        /**
         * Parent frame identifier.
         */
        public val parentId: String? = null,
        /**
         * Identifier of the loader associated with this frame.
         */
        public val loaderId: String,
        /**
         * Frame's name as specified in the tag.
         */
        public val name: String? = null,
        /**
         * Frame document's URL without fragment.
         */
        public val url: String,
        /**
         * Frame document's URL fragment including the '#'.
         */
        public val urlFragment: String? = null,
        /**
         * Frame document's registered domain, taking the public suffixes list into account.
         * Extracted from the Frame's url.
         * Example URLs: http://www.google.com/file.html -> "google.com"
         *               http://a.b.co.uk/file.html      -> "b.co.uk"
         */
        public val domainAndRegistry: String,
        /**
         * Frame document's security origin.
         */
        public val securityOrigin: String,
        /**
         * Additional details about the frame document's security origin.
         */
        public val securityOriginDetails: SecurityOriginDetails? = null,
        /**
         * Frame document's mimeType as determined by the browser.
         */
        public val mimeType: String,
        /**
         * If the frame failed to load, this contains the URL that could not be loaded. Note that unlike url above, this URL may contain a fragment.
         */
        public val unreachableUrl: String? = null,
        /**
         * Indicates whether this frame was tagged as an ad and why.
         */
        public val adFrameStatus: AdFrameStatus? = null,
        /**
         * Indicates whether the main document is a secure context and explains why that is the case.
         */
        public val secureContextType: SecureContextType,
        /**
         * Indicates whether this is a cross origin isolated context.
         */
        public val crossOriginIsolatedContextType: CrossOriginIsolatedContextType,
        /**
         * Indicated which gated APIs / features are available.
         */
        public val gatedAPIFeatures: List<GatedAPIFeatures>,
    )

    /**
     * Information about the Resource on the page.
     */
    @Serializable
    public data class FrameResource(
        /**
         * Resource URL.
         */
        public val url: String,
        /**
         * Type of this resource.
         */
        public val type: Network.ResourceType,
        /**
         * Resource mimeType as determined by the browser.
         */
        public val mimeType: String,
        /**
         * last-modified timestamp as reported by server.
         */
        public val lastModified: Double? = null,
        /**
         * Resource content size.
         */
        public val contentSize: Double? = null,
        /**
         * True if the resource failed to load.
         */
        public val failed: Boolean? = null,
        /**
         * True if the resource was canceled during loading.
         */
        public val canceled: Boolean? = null,
    )

    /**
     * Information about the Frame hierarchy along with their cached resources.
     */
    @Serializable
    public data class FrameResourceTree(
        /**
         * Frame information for this tree item.
         */
        public val frame: Frame,
        /**
         * Child frames.
         */
        public val childFrames: List<FrameResourceTree>? = null,
        /**
         * Information about frame resources.
         */
        public val resources: List<FrameResource>,
    )

    /**
     * Information about the Frame hierarchy.
     */
    @Serializable
    public data class FrameTree(
        /**
         * Frame information for this tree item.
         */
        public val frame: Frame,
        /**
         * Child frames.
         */
        public val childFrames: List<FrameTree>? = null,
    )

    /**
     * Transition type.
     */
    @Serializable
    public enum class TransitionType {
        @SerialName("link")
        LINK,

        @SerialName("typed")
        TYPED,

        @SerialName("address_bar")
        ADDRESS_BAR,

        @SerialName("auto_bookmark")
        AUTO_BOOKMARK,

        @SerialName("auto_subframe")
        AUTO_SUBFRAME,

        @SerialName("manual_subframe")
        MANUAL_SUBFRAME,

        @SerialName("generated")
        GENERATED,

        @SerialName("auto_toplevel")
        AUTO_TOPLEVEL,

        @SerialName("form_submit")
        FORM_SUBMIT,

        @SerialName("reload")
        RELOAD,

        @SerialName("keyword")
        KEYWORD,

        @SerialName("keyword_generated")
        KEYWORD_GENERATED,

        @SerialName("other")
        OTHER,
    }

    /**
     * Navigation history entry.
     */
    @Serializable
    public data class NavigationEntry(
        /**
         * Unique id of the navigation history entry.
         */
        public val id: Int,
        /**
         * URL of the navigation history entry.
         */
        public val url: String,
        /**
         * URL that the user typed in the url bar.
         */
        public val userTypedURL: String,
        /**
         * Title of the navigation history entry.
         */
        public val title: String,
        /**
         * Transition type.
         */
        public val transitionType: TransitionType,
    )

    /**
     * Screencast frame metadata.
     */
    @Serializable
    public data class ScreencastFrameMetadata(
        /**
         * Top offset in DIP.
         */
        public val offsetTop: Double,
        /**
         * Page scale factor.
         */
        public val pageScaleFactor: Double,
        /**
         * Device screen width in DIP.
         */
        public val deviceWidth: Double,
        /**
         * Device screen height in DIP.
         */
        public val deviceHeight: Double,
        /**
         * Position of horizontal scroll in CSS pixels.
         */
        public val scrollOffsetX: Double,
        /**
         * Position of vertical scroll in CSS pixels.
         */
        public val scrollOffsetY: Double,
        /**
         * Frame swap timestamp.
         */
        public val timestamp: Double? = null,
    )

    /**
     * Javascript dialog type.
     */
    @Serializable
    public enum class DialogType {
        @SerialName("alert")
        ALERT,

        @SerialName("confirm")
        CONFIRM,

        @SerialName("prompt")
        PROMPT,

        @SerialName("beforeunload")
        BEFOREUNLOAD,
    }

    /**
     * Error while paring app manifest.
     */
    @Serializable
    public data class AppManifestError(
        /**
         * Error message.
         */
        public val message: String,
        /**
         * If critical, this is a non-recoverable parse error.
         */
        public val critical: Int,
        /**
         * Error line.
         */
        public val line: Int,
        /**
         * Error column.
         */
        public val column: Int,
    )

    /**
     * Parsed app manifest properties.
     */
    @Serializable
    public data class AppManifestParsedProperties(
        /**
         * Computed scope value
         */
        public val scope: String,
    )

    /**
     * Layout viewport position and dimensions.
     */
    @Serializable
    public data class LayoutViewport(
        /**
         * Horizontal offset relative to the document (CSS pixels).
         */
        public val pageX: Int,
        /**
         * Vertical offset relative to the document (CSS pixels).
         */
        public val pageY: Int,
        /**
         * Width (CSS pixels), excludes scrollbar if present.
         */
        public val clientWidth: Int,
        /**
         * Height (CSS pixels), excludes scrollbar if present.
         */
        public val clientHeight: Int,
    )

    /**
     * Visual viewport position, dimensions, and scale.
     */
    @Serializable
    public data class VisualViewport(
        /**
         * Horizontal offset relative to the layout viewport (CSS pixels).
         */
        public val offsetX: Double,
        /**
         * Vertical offset relative to the layout viewport (CSS pixels).
         */
        public val offsetY: Double,
        /**
         * Horizontal offset relative to the document (CSS pixels).
         */
        public val pageX: Double,
        /**
         * Vertical offset relative to the document (CSS pixels).
         */
        public val pageY: Double,
        /**
         * Width (CSS pixels), excludes scrollbar if present.
         */
        public val clientWidth: Double,
        /**
         * Height (CSS pixels), excludes scrollbar if present.
         */
        public val clientHeight: Double,
        /**
         * Scale relative to the ideal viewport (size at width=device-width).
         */
        public val scale: Double,
        /**
         * Page zoom factor (CSS to device independent pixels ratio).
         */
        public val zoom: Double? = null,
    )

    /**
     * Viewport for capturing screenshot.
     */
    @Serializable
    public data class Viewport(
        /**
         * X offset in device independent pixels (dip).
         */
        public val x: Double,
        /**
         * Y offset in device independent pixels (dip).
         */
        public val y: Double,
        /**
         * Rectangle width in device independent pixels (dip).
         */
        public val width: Double,
        /**
         * Rectangle height in device independent pixels (dip).
         */
        public val height: Double,
        /**
         * Page scale factor.
         */
        public val scale: Double,
    )

    /**
     * Generic font families collection.
     */
    @Serializable
    public data class FontFamilies(
        /**
         * The standard font-family.
         */
        public val standard: String? = null,
        /**
         * The fixed font-family.
         */
        public val fixed: String? = null,
        /**
         * The serif font-family.
         */
        public val serif: String? = null,
        /**
         * The sansSerif font-family.
         */
        public val sansSerif: String? = null,
        /**
         * The cursive font-family.
         */
        public val cursive: String? = null,
        /**
         * The fantasy font-family.
         */
        public val fantasy: String? = null,
        /**
         * The math font-family.
         */
        public val math: String? = null,
    )

    /**
     * Font families collection for a script.
     */
    @Serializable
    public data class ScriptFontFamilies(
        /**
         * Name of the script which these font families are defined for.
         */
        public val script: String,
        /**
         * Generic font families collection for the script.
         */
        public val fontFamilies: FontFamilies,
    )

    /**
     * Default font sizes.
     */
    @Serializable
    public data class FontSizes(
        /**
         * Default standard font size.
         */
        public val standard: Int? = null,
        /**
         * Default fixed font size.
         */
        public val fixed: Int? = null,
    )

    @Serializable
    public enum class ClientNavigationReason {
        @SerialName("anchorClick")
        ANCHORCLICK,

        @SerialName("formSubmissionGet")
        FORMSUBMISSIONGET,

        @SerialName("formSubmissionPost")
        FORMSUBMISSIONPOST,

        @SerialName("httpHeaderRefresh")
        HTTPHEADERREFRESH,

        @SerialName("initialFrameNavigation")
        INITIALFRAMENAVIGATION,

        @SerialName("metaTagRefresh")
        METATAGREFRESH,

        @SerialName("other")
        OTHER,

        @SerialName("pageBlockInterstitial")
        PAGEBLOCKINTERSTITIAL,

        @SerialName("reload")
        RELOAD,

        @SerialName("scriptInitiated")
        SCRIPTINITIATED,
    }

    @Serializable
    public enum class ClientNavigationDisposition {
        @SerialName("currentTab")
        CURRENTTAB,

        @SerialName("newTab")
        NEWTAB,

        @SerialName("newWindow")
        NEWWINDOW,

        @SerialName("download")
        DOWNLOAD,
    }

    @Serializable
    public data class InstallabilityErrorArgument(
        /**
         * Argument name (e.g. name:'minimum-icon-size-in-pixels').
         */
        public val name: String,
        /**
         * Argument value (e.g. value:'64').
         */
        public val `value`: String,
    )

    /**
     * The installability error
     */
    @Serializable
    public data class InstallabilityError(
        /**
         * The error id (e.g. 'manifest-missing-suitable-icon').
         */
        public val errorId: String,
        /**
         * The list of error arguments (e.g. {name:'minimum-icon-size-in-pixels', value:'64'}).
         */
        public val errorArguments: List<InstallabilityErrorArgument>,
    )

    /**
     * The referring-policy used for the navigation.
     */
    @Serializable
    public enum class ReferrerPolicy {
        @SerialName("noReferrer")
        NOREFERRER,

        @SerialName("noReferrerWhenDowngrade")
        NOREFERRERWHENDOWNGRADE,

        @SerialName("origin")
        ORIGIN,

        @SerialName("originWhenCrossOrigin")
        ORIGINWHENCROSSORIGIN,

        @SerialName("sameOrigin")
        SAMEORIGIN,

        @SerialName("strictOrigin")
        STRICTORIGIN,

        @SerialName("strictOriginWhenCrossOrigin")
        STRICTORIGINWHENCROSSORIGIN,

        @SerialName("unsafeUrl")
        UNSAFEURL,
    }

    /**
     * Per-script compilation cache parameters for `Page.produceCompilationCache`
     */
    @Serializable
    public data class CompilationCacheParams(
        /**
         * The URL of the script to produce a compilation cache entry for.
         */
        public val url: String,
        /**
         * A hint to the backend whether eager compilation is recommended.
         * (the actual compilation mode used is upon backend discretion).
         */
        public val eager: Boolean? = null,
    )

    @Serializable
    public data class FileFilter(
        public val name: String? = null,
        public val accepts: List<String>? = null,
    )

    @Serializable
    public data class FileHandler(
        public val action: String,
        public val name: String,
        public val icons: List<ImageResource>? = null,
        /**
         * Mimic a map, name is the key, accepts is the value.
         */
        public val accepts: List<FileFilter>? = null,
        /**
         * Won't repeat the enums, using string for easy comparison. Same as the
         * other enums below.
         */
        public val launchType: String,
    )

    /**
     * The image definition used in both icon and screenshot.
     */
    @Serializable
    public data class ImageResource(
        /**
         * The src field in the definition, but changing to url in favor of
         * consistency.
         */
        public val url: String,
        public val sizes: String? = null,
        public val type: String? = null,
    )

    @Serializable
    public data class LaunchHandler(
        public val clientMode: String,
    )

    @Serializable
    public data class ProtocolHandler(
        public val protocol: String,
        public val url: String,
    )

    @Serializable
    public data class RelatedApplication(
        public val id: String? = null,
        public val url: String,
    )

    @Serializable
    public data class ScopeExtension(
        /**
         * Instead of using tuple, this field always returns the serialized string
         * for easy understanding and comparison.
         */
        public val origin: String,
        public val hasOriginWildcard: Boolean,
    )

    @Serializable
    public data class Screenshot(
        public val image: ImageResource,
        public val formFactor: String,
        public val label: String? = null,
    )

    @Serializable
    public data class ShareTarget(
        public val action: String,
        public val method: String,
        public val enctype: String,
        /**
         * Embed the ShareTargetParams
         */
        public val title: String? = null,
        public val text: String? = null,
        public val url: String? = null,
        public val files: List<FileFilter>? = null,
    )

    @Serializable
    public data class Shortcut(
        public val name: String,
        public val url: String,
    )

    @Serializable
    public data class WebAppManifest(
        public val backgroundColor: String? = null,
        /**
         * The extra description provided by the manifest.
         */
        public val description: String? = null,
        public val dir: String? = null,
        public val display: String? = null,
        /**
         * The overrided display mode controlled by the user.
         */
        public val displayOverrides: List<String>? = null,
        /**
         * The handlers to open files.
         */
        public val fileHandlers: List<FileHandler>? = null,
        public val icons: List<ImageResource>? = null,
        public val id: String? = null,
        public val lang: String? = null,
        /**
         * TODO(crbug.com/1231886): This field is non-standard and part of a Chrome
         * experiment. See:
         * https://github.com/WICG/web-app-launch/blob/main/launch_handler.md
         */
        public val launchHandler: LaunchHandler? = null,
        public val name: String? = null,
        public val orientation: String? = null,
        public val preferRelatedApplications: Boolean? = null,
        /**
         * The handlers to open protocols.
         */
        public val protocolHandlers: List<ProtocolHandler>? = null,
        public val relatedApplications: List<RelatedApplication>? = null,
        public val scope: String? = null,
        /**
         * Non-standard, see
         * https://github.com/WICG/manifest-incubations/blob/gh-pages/scope_extensions-explainer.md
         */
        public val scopeExtensions: List<ScopeExtension>? = null,
        /**
         * The screenshots used by chromium.
         */
        public val screenshots: List<Screenshot>? = null,
        public val shareTarget: ShareTarget? = null,
        public val shortName: String? = null,
        public val shortcuts: List<Shortcut>? = null,
        public val startUrl: String? = null,
        public val themeColor: String? = null,
    )

    /**
     * The type of a frameNavigated event.
     */
    @Serializable
    public enum class NavigationType {
        @SerialName("Navigation")
        NAVIGATION,

        @SerialName("BackForwardCacheRestore")
        BACKFORWARDCACHERESTORE,
    }

    /**
     * List of not restored reasons for back-forward cache.
     */
    @Serializable
    public enum class BackForwardCacheNotRestoredReason {
        @SerialName("NotPrimaryMainFrame")
        NOTPRIMARYMAINFRAME,

        @SerialName("BackForwardCacheDisabled")
        BACKFORWARDCACHEDISABLED,

        @SerialName("RelatedActiveContentsExist")
        RELATEDACTIVECONTENTSEXIST,

        @SerialName("HTTPStatusNotOK")
        HTTPSTATUSNOTOK,

        @SerialName("SchemeNotHTTPOrHTTPS")
        SCHEMENOTHTTPORHTTPS,

        @SerialName("Loading")
        LOADING,

        @SerialName("WasGrantedMediaAccess")
        WASGRANTEDMEDIAACCESS,

        @SerialName("DisableForRenderFrameHostCalled")
        DISABLEFORRENDERFRAMEHOSTCALLED,

        @SerialName("DomainNotAllowed")
        DOMAINNOTALLOWED,

        @SerialName("HTTPMethodNotGET")
        HTTPMETHODNOTGET,

        @SerialName("SubframeIsNavigating")
        SUBFRAMEISNAVIGATING,

        @SerialName("Timeout")
        TIMEOUT,

        @SerialName("CacheLimit")
        CACHELIMIT,

        @SerialName("JavaScriptExecution")
        JAVASCRIPTEXECUTION,

        @SerialName("RendererProcessKilled")
        RENDERERPROCESSKILLED,

        @SerialName("RendererProcessCrashed")
        RENDERERPROCESSCRASHED,

        @SerialName("SchedulerTrackedFeatureUsed")
        SCHEDULERTRACKEDFEATUREUSED,

        @SerialName("ConflictingBrowsingInstance")
        CONFLICTINGBROWSINGINSTANCE,

        @SerialName("CacheFlushed")
        CACHEFLUSHED,

        @SerialName("ServiceWorkerVersionActivation")
        SERVICEWORKERVERSIONACTIVATION,

        @SerialName("SessionRestored")
        SESSIONRESTORED,

        @SerialName("ServiceWorkerPostMessage")
        SERVICEWORKERPOSTMESSAGE,

        @SerialName("EnteredBackForwardCacheBeforeServiceWorkerHostAdded")
        ENTEREDBACKFORWARDCACHEBEFORESERVICEWORKERHOSTADDED,

        @SerialName("RenderFrameHostReused_SameSite")
        RENDERFRAMEHOSTREUSED_SAMESITE,

        @SerialName("RenderFrameHostReused_CrossSite")
        RENDERFRAMEHOSTREUSED_CROSSSITE,

        @SerialName("ServiceWorkerClaim")
        SERVICEWORKERCLAIM,

        @SerialName("IgnoreEventAndEvict")
        IGNOREEVENTANDEVICT,

        @SerialName("HaveInnerContents")
        HAVEINNERCONTENTS,

        @SerialName("TimeoutPuttingInCache")
        TIMEOUTPUTTINGINCACHE,

        @SerialName("BackForwardCacheDisabledByLowMemory")
        BACKFORWARDCACHEDISABLEDBYLOWMEMORY,

        @SerialName("BackForwardCacheDisabledByCommandLine")
        BACKFORWARDCACHEDISABLEDBYCOMMANDLINE,

        @SerialName("NetworkRequestDatapipeDrainedAsBytesConsumer")
        NETWORKREQUESTDATAPIPEDRAINEDASBYTESCONSUMER,

        @SerialName("NetworkRequestRedirected")
        NETWORKREQUESTREDIRECTED,

        @SerialName("NetworkRequestTimeout")
        NETWORKREQUESTTIMEOUT,

        @SerialName("NetworkExceedsBufferLimit")
        NETWORKEXCEEDSBUFFERLIMIT,

        @SerialName("NavigationCancelledWhileRestoring")
        NAVIGATIONCANCELLEDWHILERESTORING,

        @SerialName("NotMostRecentNavigationEntry")
        NOTMOSTRECENTNAVIGATIONENTRY,

        @SerialName("BackForwardCacheDisabledForPrerender")
        BACKFORWARDCACHEDISABLEDFORPRERENDER,

        @SerialName("UserAgentOverrideDiffers")
        USERAGENTOVERRIDEDIFFERS,

        @SerialName("ForegroundCacheLimit")
        FOREGROUNDCACHELIMIT,

        @SerialName("BrowsingInstanceNotSwapped")
        BROWSINGINSTANCENOTSWAPPED,

        @SerialName("BackForwardCacheDisabledForDelegate")
        BACKFORWARDCACHEDISABLEDFORDELEGATE,

        @SerialName("UnloadHandlerExistsInMainFrame")
        UNLOADHANDLEREXISTSINMAINFRAME,

        @SerialName("UnloadHandlerExistsInSubFrame")
        UNLOADHANDLEREXISTSINSUBFRAME,

        @SerialName("ServiceWorkerUnregistration")
        SERVICEWORKERUNREGISTRATION,

        @SerialName("CacheControlNoStore")
        CACHECONTROLNOSTORE,

        @SerialName("CacheControlNoStoreCookieModified")
        CACHECONTROLNOSTORECOOKIEMODIFIED,

        @SerialName("CacheControlNoStoreHTTPOnlyCookieModified")
        CACHECONTROLNOSTOREHTTPONLYCOOKIEMODIFIED,

        @SerialName("NoResponseHead")
        NORESPONSEHEAD,

        @SerialName("Unknown")
        UNKNOWN,

        @SerialName("ActivationNavigationsDisallowedForBug1234857")
        ACTIVATIONNAVIGATIONSDISALLOWEDFORBUG1234857,

        @SerialName("ErrorDocument")
        ERRORDOCUMENT,

        @SerialName("FencedFramesEmbedder")
        FENCEDFRAMESEMBEDDER,

        @SerialName("CookieDisabled")
        COOKIEDISABLED,

        @SerialName("HTTPAuthRequired")
        HTTPAUTHREQUIRED,

        @SerialName("CookieFlushed")
        COOKIEFLUSHED,

        @SerialName("BroadcastChannelOnMessage")
        BROADCASTCHANNELONMESSAGE,

        @SerialName("WebViewSettingsChanged")
        WEBVIEWSETTINGSCHANGED,

        @SerialName("WebViewJavaScriptObjectChanged")
        WEBVIEWJAVASCRIPTOBJECTCHANGED,

        @SerialName("WebViewMessageListenerInjected")
        WEBVIEWMESSAGELISTENERINJECTED,

        @SerialName("WebViewSafeBrowsingAllowlistChanged")
        WEBVIEWSAFEBROWSINGALLOWLISTCHANGED,

        @SerialName("WebViewDocumentStartJavascriptChanged")
        WEBVIEWDOCUMENTSTARTJAVASCRIPTCHANGED,

        @SerialName("WebSocket")
        WEBSOCKET,

        @SerialName("WebTransport")
        WEBTRANSPORT,

        @SerialName("WebRTC")
        WEBRTC,

        @SerialName("MainResourceHasCacheControlNoStore")
        MAINRESOURCEHASCACHECONTROLNOSTORE,

        @SerialName("MainResourceHasCacheControlNoCache")
        MAINRESOURCEHASCACHECONTROLNOCACHE,

        @SerialName("SubresourceHasCacheControlNoStore")
        SUBRESOURCEHASCACHECONTROLNOSTORE,

        @SerialName("SubresourceHasCacheControlNoCache")
        SUBRESOURCEHASCACHECONTROLNOCACHE,

        @SerialName("ContainsPlugins")
        CONTAINSPLUGINS,

        @SerialName("DocumentLoaded")
        DOCUMENTLOADED,

        @SerialName("OutstandingNetworkRequestOthers")
        OUTSTANDINGNETWORKREQUESTOTHERS,

        @SerialName("RequestedMIDIPermission")
        REQUESTEDMIDIPERMISSION,

        @SerialName("RequestedAudioCapturePermission")
        REQUESTEDAUDIOCAPTUREPERMISSION,

        @SerialName("RequestedVideoCapturePermission")
        REQUESTEDVIDEOCAPTUREPERMISSION,

        @SerialName("RequestedBackForwardCacheBlockedSensors")
        REQUESTEDBACKFORWARDCACHEBLOCKEDSENSORS,

        @SerialName("RequestedBackgroundWorkPermission")
        REQUESTEDBACKGROUNDWORKPERMISSION,

        @SerialName("BroadcastChannel")
        BROADCASTCHANNEL,

        @SerialName("WebXR")
        WEBXR,

        @SerialName("SharedWorker")
        SHAREDWORKER,

        @SerialName("SharedWorkerMessage")
        SHAREDWORKERMESSAGE,

        @SerialName("WebLocks")
        WEBLOCKS,

        @SerialName("WebHID")
        WEBHID,

        @SerialName("WebShare")
        WEBSHARE,

        @SerialName("RequestedStorageAccessGrant")
        REQUESTEDSTORAGEACCESSGRANT,

        @SerialName("WebNfc")
        WEBNFC,

        @SerialName("OutstandingNetworkRequestFetch")
        OUTSTANDINGNETWORKREQUESTFETCH,

        @SerialName("OutstandingNetworkRequestXHR")
        OUTSTANDINGNETWORKREQUESTXHR,

        @SerialName("AppBanner")
        APPBANNER,

        @SerialName("Printing")
        PRINTING,

        @SerialName("WebDatabase")
        WEBDATABASE,

        @SerialName("PictureInPicture")
        PICTUREINPICTURE,

        @SerialName("SpeechRecognizer")
        SPEECHRECOGNIZER,

        @SerialName("IdleManager")
        IDLEMANAGER,

        @SerialName("PaymentManager")
        PAYMENTMANAGER,

        @SerialName("SpeechSynthesis")
        SPEECHSYNTHESIS,

        @SerialName("KeyboardLock")
        KEYBOARDLOCK,

        @SerialName("WebOTPService")
        WEBOTPSERVICE,

        @SerialName("OutstandingNetworkRequestDirectSocket")
        OUTSTANDINGNETWORKREQUESTDIRECTSOCKET,

        @SerialName("InjectedJavascript")
        INJECTEDJAVASCRIPT,

        @SerialName("InjectedStyleSheet")
        INJECTEDSTYLESHEET,

        @SerialName("KeepaliveRequest")
        KEEPALIVEREQUEST,

        @SerialName("IndexedDBEvent")
        INDEXEDDBEVENT,

        @SerialName("Dummy")
        DUMMY,

        @SerialName("JsNetworkRequestReceivedCacheControlNoStoreResource")
        JSNETWORKREQUESTRECEIVEDCACHECONTROLNOSTORERESOURCE,

        @SerialName("WebRTCSticky")
        WEBRTCSTICKY,

        @SerialName("WebTransportSticky")
        WEBTRANSPORTSTICKY,

        @SerialName("WebSocketSticky")
        WEBSOCKETSTICKY,

        @SerialName("SmartCard")
        SMARTCARD,

        @SerialName("LiveMediaStreamTrack")
        LIVEMEDIASTREAMTRACK,

        @SerialName("UnloadHandler")
        UNLOADHANDLER,

        @SerialName("ParserAborted")
        PARSERABORTED,

        @SerialName("ContentSecurityHandler")
        CONTENTSECURITYHANDLER,

        @SerialName("ContentWebAuthenticationAPI")
        CONTENTWEBAUTHENTICATIONAPI,

        @SerialName("ContentFileChooser")
        CONTENTFILECHOOSER,

        @SerialName("ContentSerial")
        CONTENTSERIAL,

        @SerialName("ContentFileSystemAccess")
        CONTENTFILESYSTEMACCESS,

        @SerialName("ContentMediaDevicesDispatcherHost")
        CONTENTMEDIADEVICESDISPATCHERHOST,

        @SerialName("ContentWebBluetooth")
        CONTENTWEBBLUETOOTH,

        @SerialName("ContentWebUSB")
        CONTENTWEBUSB,

        @SerialName("ContentMediaSessionService")
        CONTENTMEDIASESSIONSERVICE,

        @SerialName("ContentScreenReader")
        CONTENTSCREENREADER,

        @SerialName("ContentDiscarded")
        CONTENTDISCARDED,

        @SerialName("EmbedderPopupBlockerTabHelper")
        EMBEDDERPOPUPBLOCKERTABHELPER,

        @SerialName("EmbedderSafeBrowsingTriggeredPopupBlocker")
        EMBEDDERSAFEBROWSINGTRIGGEREDPOPUPBLOCKER,

        @SerialName("EmbedderSafeBrowsingThreatDetails")
        EMBEDDERSAFEBROWSINGTHREATDETAILS,

        @SerialName("EmbedderAppBannerManager")
        EMBEDDERAPPBANNERMANAGER,

        @SerialName("EmbedderDomDistillerViewerSource")
        EMBEDDERDOMDISTILLERVIEWERSOURCE,

        @SerialName("EmbedderDomDistillerSelfDeletingRequestDelegate")
        EMBEDDERDOMDISTILLERSELFDELETINGREQUESTDELEGATE,

        @SerialName("EmbedderOomInterventionTabHelper")
        EMBEDDEROOMINTERVENTIONTABHELPER,

        @SerialName("EmbedderOfflinePage")
        EMBEDDEROFFLINEPAGE,

        @SerialName("EmbedderChromePasswordManagerClientBindCredentialManager")
        EMBEDDERCHROMEPASSWORDMANAGERCLIENTBINDCREDENTIALMANAGER,

        @SerialName("EmbedderPermissionRequestManager")
        EMBEDDERPERMISSIONREQUESTMANAGER,

        @SerialName("EmbedderModalDialog")
        EMBEDDERMODALDIALOG,

        @SerialName("EmbedderExtensions")
        EMBEDDEREXTENSIONS,

        @SerialName("EmbedderExtensionMessaging")
        EMBEDDEREXTENSIONMESSAGING,

        @SerialName("EmbedderExtensionMessagingForOpenPort")
        EMBEDDEREXTENSIONMESSAGINGFOROPENPORT,

        @SerialName("EmbedderExtensionSentMessageToCachedFrame")
        EMBEDDEREXTENSIONSENTMESSAGETOCACHEDFRAME,

        @SerialName("RequestedByWebViewClient")
        REQUESTEDBYWEBVIEWCLIENT,

        @SerialName("PostMessageByWebViewClient")
        POSTMESSAGEBYWEBVIEWCLIENT,

        @SerialName("CacheControlNoStoreDeviceBoundSessionTerminated")
        CACHECONTROLNOSTOREDEVICEBOUNDSESSIONTERMINATED,

        @SerialName("CacheLimitPrunedOnModerateMemoryPressure")
        CACHELIMITPRUNEDONMODERATEMEMORYPRESSURE,

        @SerialName("CacheLimitPrunedOnCriticalMemoryPressure")
        CACHELIMITPRUNEDONCRITICALMEMORYPRESSURE,
    }

    /**
     * Types of not restored reasons for back-forward cache.
     */
    @Serializable
    public enum class BackForwardCacheNotRestoredReasonType {
        @SerialName("SupportPending")
        SUPPORTPENDING,

        @SerialName("PageSupportNeeded")
        PAGESUPPORTNEEDED,

        @SerialName("Circumstantial")
        CIRCUMSTANTIAL,
    }

    @Serializable
    public data class BackForwardCacheBlockingDetails(
        /**
         * Url of the file where blockage happened. Optional because of tests.
         */
        public val url: String? = null,
        /**
         * Function name where blockage happened. Optional because of anonymous functions and tests.
         */
        public val function: String? = null,
        /**
         * Line number in the script (0-based).
         */
        public val lineNumber: Int,
        /**
         * Column number in the script (0-based).
         */
        public val columnNumber: Int,
    )

    @Serializable
    public data class BackForwardCacheNotRestoredExplanation(
        /**
         * Type of the reason
         */
        public val type: BackForwardCacheNotRestoredReasonType,
        /**
         * Not restored reason
         */
        public val reason: BackForwardCacheNotRestoredReason,
        /**
         * Context associated with the reason. The meaning of this context is
         * dependent on the reason:
         * - EmbedderExtensionSentMessageToCachedFrame: the extension ID.
         */
        public val context: String? = null,
        public val details: List<BackForwardCacheBlockingDetails>? = null,
    )

    @Serializable
    public data class BackForwardCacheNotRestoredExplanationTree(
        /**
         * URL of each frame
         */
        public val url: String,
        /**
         * Not restored reasons of each frame
         */
        public val explanations: List<BackForwardCacheNotRestoredExplanation>,
        /**
         * Array of children frame
         */
        public val children: List<BackForwardCacheNotRestoredExplanationTree>,
    )

    @Serializable
    public data class DomContentEventFiredParameter(
        public val timestamp: Double,
    )

    /**
     * Emitted only when `page.interceptFileChooser` is enabled.
     */
    @Serializable
    public data class FileChooserOpenedParameter(
        /**
         * Id of the frame containing input node.
         */
        public val frameId: String,
        /**
         * Input mode.
         */
        public val mode: String,
        /**
         * Input node id. Only present for file choosers opened via an `<input type="file">` element.
         */
        public val backendNodeId: Int? = null,
    )

    /**
     * Fired when frame has been attached to its parent.
     */
    @Serializable
    public data class FrameAttachedParameter(
        /**
         * Id of the frame that has been attached.
         */
        public val frameId: String,
        /**
         * Parent frame identifier.
         */
        public val parentFrameId: String,
        /**
         * JavaScript stack trace of when frame was attached, only set if frame initiated from script.
         */
        public val stack: Runtime.StackTrace? = null,
    )

    /**
     * Fired when frame no longer has a scheduled navigation.
     */
    @Serializable
    public data class FrameClearedScheduledNavigationParameter(
        /**
         * Id of the frame that has cleared its scheduled navigation.
         */
        public val frameId: String,
    )

    /**
     * Fired when frame has been detached from its parent.
     */
    @Serializable
    public data class FrameDetachedParameter(
        /**
         * Id of the frame that has been detached.
         */
        public val frameId: String,
        public val reason: String,
    )

    /**
     * Fired before frame subtree is detached. Emitted before any frame of the
     * subtree is actually detached.
     */
    @Serializable
    public data class FrameSubtreeWillBeDetachedParameter(
        /**
         * Id of the frame that is the root of the subtree that will be detached.
         */
        public val frameId: String,
    )

    /**
     * Fired once navigation of the frame has completed. Frame is now associated with the new loader.
     */
    @Serializable
    public data class FrameNavigatedParameter(
        /**
         * Frame object.
         */
        public val frame: Frame,
        public val type: NavigationType,
    )

    /**
     * Fired when opening document to write to.
     */
    @Serializable
    public data class DocumentOpenedParameter(
        /**
         * Frame object.
         */
        public val frame: Frame,
    )

    /**
     * Fired when a navigation starts. This event is fired for both
     * renderer-initiated and browser-initiated navigations. For renderer-initiated
     * navigations, the event is fired after `frameRequestedNavigation`.
     * Navigation may still be cancelled after the event is issued. Multiple events
     * can be fired for a single navigation, for example, when a same-document
     * navigation becomes a cross-document navigation (such as in the case of a
     * frameset).
     */
    @Serializable
    public data class FrameStartedNavigatingParameter(
        /**
         * ID of the frame that is being navigated.
         */
        public val frameId: String,
        /**
         * The URL the navigation started with. The final URL can be different.
         */
        public val url: String,
        /**
         * Loader identifier. Even though it is present in case of same-document
         * navigation, the previously committed loaderId would not change unless
         * the navigation changes from a same-document to a cross-document
         * navigation.
         */
        public val loaderId: String,
        public val navigationType: String,
    )

    /**
     * Fired when a renderer-initiated navigation is requested.
     * Navigation may still be cancelled after the event is issued.
     */
    @Serializable
    public data class FrameRequestedNavigationParameter(
        /**
         * Id of the frame that is being navigated.
         */
        public val frameId: String,
        /**
         * The reason for the navigation.
         */
        public val reason: ClientNavigationReason,
        /**
         * The destination URL for the requested navigation.
         */
        public val url: String,
        /**
         * The disposition for the navigation.
         */
        public val disposition: ClientNavigationDisposition,
    )

    /**
     * Fired when frame schedules a potential navigation.
     */
    @Serializable
    public data class FrameScheduledNavigationParameter(
        /**
         * Id of the frame that has scheduled a navigation.
         */
        public val frameId: String,
        /**
         * Delay (in seconds) until the navigation is scheduled to begin. The navigation is not
         * guaranteed to start.
         */
        public val delay: Double,
        /**
         * The reason for the navigation.
         */
        public val reason: ClientNavigationReason,
        /**
         * The destination URL for the scheduled navigation.
         */
        public val url: String,
    )

    /**
     * Fired when frame has started loading.
     */
    @Serializable
    public data class FrameStartedLoadingParameter(
        /**
         * Id of the frame that has started loading.
         */
        public val frameId: String,
    )

    /**
     * Fired when frame has stopped loading.
     */
    @Serializable
    public data class FrameStoppedLoadingParameter(
        /**
         * Id of the frame that has stopped loading.
         */
        public val frameId: String,
    )

    /**
     * Fired when page is about to start a download.
     * Deprecated. Use Browser.downloadWillBegin instead.
     */
    @Serializable
    public data class DownloadWillBeginParameter(
        /**
         * Id of the frame that caused download to begin.
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
     * Deprecated. Use Browser.downloadProgress instead.
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

    /**
     * Fired when a JavaScript initiated dialog (alert, confirm, prompt, or onbeforeunload) has been
     * closed.
     */
    @Serializable
    public data class JavascriptDialogClosedParameter(
        /**
         * Frame id.
         */
        public val frameId: String,
        /**
         * Whether dialog was confirmed.
         */
        public val result: Boolean,
        /**
         * User input in case of prompt.
         */
        public val userInput: String,
    )

    /**
     * Fired when a JavaScript initiated dialog (alert, confirm, prompt, or onbeforeunload) is about to
     * open.
     */
    @Serializable
    public data class JavascriptDialogOpeningParameter(
        /**
         * Frame url.
         */
        public val url: String,
        /**
         * Frame id.
         */
        public val frameId: String,
        /**
         * Message that will be displayed by the dialog.
         */
        public val message: String,
        /**
         * Dialog type.
         */
        public val type: DialogType,
        /**
         * True iff browser is capable showing or acting on the given dialog. When browser has no
         * dialog handler for given target, calling alert while Page domain is engaged will stall
         * the page execution. Execution can be resumed via calling Page.handleJavaScriptDialog.
         */
        public val hasBrowserHandler: Boolean,
        /**
         * Default dialog prompt.
         */
        public val defaultPrompt: String? = null,
    )

    /**
     * Fired for lifecycle events (navigation, load, paint, etc) in the current
     * target (including local frames).
     */
    @Serializable
    public data class LifecycleEventParameter(
        /**
         * Id of the frame.
         */
        public val frameId: String,
        /**
         * Loader identifier. Empty string if the request is fetched from worker.
         */
        public val loaderId: String,
        public val name: String,
        public val timestamp: Double,
    )

    /**
     * Fired for failed bfcache history navigations if BackForwardCache feature is enabled. Do
     * not assume any ordering with the Page.frameNavigated event. This event is fired only for
     * main-frame history navigation where the document changes (non-same-document navigations),
     * when bfcache navigation fails.
     */
    @Serializable
    public data class BackForwardCacheNotUsedParameter(
        /**
         * The loader id for the associated navigation.
         */
        public val loaderId: String,
        /**
         * The frame id of the associated frame.
         */
        public val frameId: String,
        /**
         * Array of reasons why the page could not be cached. This must not be empty.
         */
        public val notRestoredExplanations: List<BackForwardCacheNotRestoredExplanation>,
        /**
         * Tree structure of reasons why the page could not be cached for each frame.
         */
        public val notRestoredExplanationsTree: BackForwardCacheNotRestoredExplanationTree? = null,
    )

    @Serializable
    public data class LoadEventFiredParameter(
        public val timestamp: Double,
    )

    /**
     * Fired when same-document navigation happens, e.g. due to history API usage or anchor navigation.
     */
    @Serializable
    public data class NavigatedWithinDocumentParameter(
        /**
         * Id of the frame.
         */
        public val frameId: String,
        /**
         * Frame's new url.
         */
        public val url: String,
        /**
         * Navigation type
         */
        public val navigationType: String,
    )

    /**
     * Compressed image data requested by the `startScreencast`.
     */
    @Serializable
    public data class ScreencastFrameParameter(
        /**
         * Base64-encoded compressed image. (Encoded as a base64 string when passed over JSON)
         */
        public val `data`: String,
        /**
         * Screencast frame metadata.
         */
        public val metadata: ScreencastFrameMetadata,
        /**
         * Frame number.
         */
        public val sessionId: Int,
    )

    /**
     * Fired when the page with currently enabled screencast was shown or hidden `.
     */
    @Serializable
    public data class ScreencastVisibilityChangedParameter(
        /**
         * True if the page is visible.
         */
        public val visible: Boolean,
    )

    /**
     * Fired when a new window is going to be opened, via window.open(), link click, form submission,
     * etc.
     */
    @Serializable
    public data class WindowOpenParameter(
        /**
         * The URL for the new window.
         */
        public val url: String,
        /**
         * Window name.
         */
        public val windowName: String,
        /**
         * An array of enabled window features.
         */
        public val windowFeatures: List<String>,
        /**
         * Whether or not it was triggered by user gesture.
         */
        public val userGesture: Boolean,
    )

    /**
     * Issued for every compilation cache generated. Is only available
     * if Page.setGenerateCompilationCache is enabled.
     */
    @Serializable
    public data class CompilationCacheProducedParameter(
        public val url: String,
        /**
         * Base64-encoded data (Encoded as a base64 string when passed over JSON)
         */
        public val `data`: String,
    )

    @Serializable
    public data class AddScriptToEvaluateOnLoadParameter(
        public val scriptSource: String,
    )

    @Serializable
    public data class AddScriptToEvaluateOnLoadReturn(
        /**
         * Identifier of the added script.
         */
        public val identifier: String,
    )

    @Serializable
    public data class AddScriptToEvaluateOnNewDocumentParameter(
        public val source: String,
        /**
         * If specified, creates an isolated world with the given name and evaluates given script in it.
         * This world name will be used as the ExecutionContextDescription::name when the corresponding
         * event is emitted.
         */
        public val worldName: String? = null,
        /**
         * Specifies whether command line API should be available to the script, defaults
         * to false.
         */
        public val includeCommandLineAPI: Boolean? = null,
        /**
         * If true, runs the script immediately on existing execution contexts or worlds.
         * Default: false.
         */
        public val runImmediately: Boolean? = null,
    )

    @Serializable
    public data class AddScriptToEvaluateOnNewDocumentReturn(
        /**
         * Identifier of the added script.
         */
        public val identifier: String,
    )

    @Serializable
    public data class CaptureScreenshotParameter(
        /**
         * Image compression format (defaults to png).
         */
        public val format: String? = null,
        /**
         * Compression quality from range [0..100] (jpeg only).
         */
        public val quality: Int? = null,
        /**
         * Capture the screenshot of a given region only.
         */
        public val clip: Viewport? = null,
        /**
         * Capture the screenshot from the surface, rather than the view. Defaults to true.
         */
        public val fromSurface: Boolean? = null,
        /**
         * Capture the screenshot beyond the viewport. Defaults to false.
         */
        public val captureBeyondViewport: Boolean? = null,
        /**
         * Optimize image encoding for speed, not for resulting size (defaults to false)
         */
        public val optimizeForSpeed: Boolean? = null,
    )

    @Serializable
    public data class CaptureScreenshotReturn(
        /**
         * Base64-encoded image data. (Encoded as a base64 string when passed over JSON)
         */
        public val `data`: String,
    )

    @Serializable
    public data class CaptureSnapshotParameter(
        /**
         * Format (defaults to mhtml).
         */
        public val format: String? = null,
    )

    @Serializable
    public data class CaptureSnapshotReturn(
        /**
         * Serialized page data.
         */
        public val `data`: String,
    )

    @Serializable
    public data class CreateIsolatedWorldParameter(
        /**
         * Id of the frame in which the isolated world should be created.
         */
        public val frameId: String,
        /**
         * An optional name which is reported in the Execution Context.
         */
        public val worldName: String? = null,
        /**
         * Whether or not universal access should be granted to the isolated world. This is a powerful
         * option, use with caution.
         */
        public val grantUniveralAccess: Boolean? = null,
    )

    @Serializable
    public data class CreateIsolatedWorldReturn(
        /**
         * Execution context of the isolated world.
         */
        public val executionContextId: Int,
    )

    @Serializable
    public data class DeleteCookieParameter(
        /**
         * Name of the cookie to remove.
         */
        public val cookieName: String,
        /**
         * URL to match cooke domain and path.
         */
        public val url: String,
    )

    @Serializable
    public data class EnableParameter(
        /**
         * If true, the `Page.fileChooserOpened` event will be emitted regardless of the state set by
         * `Page.setInterceptFileChooserDialog` command (default: false).
         */
        public val enableFileChooserOpenedEvent: Boolean? = null,
    )

    @Serializable
    public data class GetAppManifestParameter(
        public val manifestId: String? = null,
    )

    @Serializable
    public data class GetAppManifestReturn(
        /**
         * Manifest location.
         */
        public val url: String,
        public val errors: List<AppManifestError>,
        /**
         * Manifest content.
         */
        public val `data`: String?,
        /**
         * Parsed manifest properties. Deprecated, use manifest instead.
         */
        public val parsed: AppManifestParsedProperties?,
        public val manifest: WebAppManifest,
    )

    @Serializable
    public data class GetInstallabilityErrorsReturn(
        public val installabilityErrors: List<InstallabilityError>,
    )

    @Serializable
    public data class GetManifestIconsReturn(
        public val primaryIcon: String?,
    )

    @Serializable
    public data class GetAppIdReturn(
        /**
         * App id, either from manifest's id attribute or computed from start_url
         */
        public val appId: String?,
        /**
         * Recommendation for manifest's id attribute to match current id computed from start_url
         */
        public val recommendedId: String?,
    )

    @Serializable
    public data class GetAdScriptAncestryParameter(
        public val frameId: String,
    )

    @Serializable
    public data class GetAdScriptAncestryReturn(
        /**
         * The ancestry chain of ad script identifiers leading to this frame's
         * creation, along with the root script's filterlist rule. The ancestry
         * chain is ordered from the most immediate script (in the frame creation
         * stack) to more distant ancestors (that created the immediately preceding
         * script). Only sent if frame is labelled as an ad and ids are available.
         */
        public val adScriptAncestry: AdScriptAncestry?,
    )

    @Serializable
    public data class GetFrameTreeReturn(
        /**
         * Present frame tree structure.
         */
        public val frameTree: FrameTree,
    )

    @Serializable
    public data class GetLayoutMetricsReturn(
        /**
         * Deprecated metrics relating to the layout viewport. Is in device pixels. Use `cssLayoutViewport` instead.
         */
        public val layoutViewport: LayoutViewport,
        /**
         * Deprecated metrics relating to the visual viewport. Is in device pixels. Use `cssVisualViewport` instead.
         */
        public val visualViewport: VisualViewport,
        /**
         * Deprecated size of scrollable area. Is in DP. Use `cssContentSize` instead.
         */
        public val contentSize: DOM.Rect,
        /**
         * Metrics relating to the layout viewport in CSS pixels.
         */
        public val cssLayoutViewport: LayoutViewport,
        /**
         * Metrics relating to the visual viewport in CSS pixels.
         */
        public val cssVisualViewport: VisualViewport,
        /**
         * Size of scrollable area in CSS pixels.
         */
        public val cssContentSize: DOM.Rect,
    )

    @Serializable
    public data class GetNavigationHistoryReturn(
        /**
         * Index of the current navigation history entry.
         */
        public val currentIndex: Int,
        /**
         * Array of navigation history entries.
         */
        public val entries: List<NavigationEntry>,
    )

    @Serializable
    public data class GetResourceContentParameter(
        /**
         * Frame id to get resource for.
         */
        public val frameId: String,
        /**
         * URL of the resource to get content for.
         */
        public val url: String,
    )

    @Serializable
    public data class GetResourceContentReturn(
        /**
         * Resource content.
         */
        public val content: String,
        /**
         * True, if content was served as base64.
         */
        public val base64Encoded: Boolean,
    )

    @Serializable
    public data class GetResourceTreeReturn(
        /**
         * Present frame / resource tree structure.
         */
        public val frameTree: FrameResourceTree,
    )

    @Serializable
    public data class HandleJavaScriptDialogParameter(
        /**
         * Whether to accept or dismiss the dialog.
         */
        public val accept: Boolean,
        /**
         * The text to enter into the dialog prompt before accepting. Used only if this is a prompt
         * dialog.
         */
        public val promptText: String? = null,
    )

    @Serializable
    public data class NavigateParameter(
        /**
         * URL to navigate the page to.
         */
        public val url: String,
        /**
         * Referrer URL.
         */
        public val referrer: String? = null,
        /**
         * Intended transition type.
         */
        public val transitionType: TransitionType? = null,
        /**
         * Frame id to navigate, if not specified navigates the top frame.
         */
        public val frameId: String? = null,
        /**
         * Referrer-policy used for the navigation.
         */
        public val referrerPolicy: ReferrerPolicy? = null,
    )

    @Serializable
    public data class NavigateReturn(
        /**
         * Frame id that has navigated (or failed to navigate)
         */
        public val frameId: String,
        /**
         * Loader identifier. This is omitted in case of same-document navigation,
         * as the previously committed loaderId would not change.
         */
        public val loaderId: String?,
        /**
         * User friendly error message, present if and only if navigation has failed.
         */
        public val errorText: String?,
    )

    @Serializable
    public data class NavigateToHistoryEntryParameter(
        /**
         * Unique id of the entry to navigate to.
         */
        public val entryId: Int,
    )

    @Serializable
    public data class PrintToPDFParameter(
        /**
         * Paper orientation. Defaults to false.
         */
        public val landscape: Boolean? = null,
        /**
         * Display header and footer. Defaults to false.
         */
        public val displayHeaderFooter: Boolean? = null,
        /**
         * Print background graphics. Defaults to false.
         */
        public val printBackground: Boolean? = null,
        /**
         * Scale of the webpage rendering. Defaults to 1.
         */
        public val scale: Double? = null,
        /**
         * Paper width in inches. Defaults to 8.5 inches.
         */
        public val paperWidth: Double? = null,
        /**
         * Paper height in inches. Defaults to 11 inches.
         */
        public val paperHeight: Double? = null,
        /**
         * Top margin in inches. Defaults to 1cm (~0.4 inches).
         */
        public val marginTop: Double? = null,
        /**
         * Bottom margin in inches. Defaults to 1cm (~0.4 inches).
         */
        public val marginBottom: Double? = null,
        /**
         * Left margin in inches. Defaults to 1cm (~0.4 inches).
         */
        public val marginLeft: Double? = null,
        /**
         * Right margin in inches. Defaults to 1cm (~0.4 inches).
         */
        public val marginRight: Double? = null,
        /**
         * Paper ranges to print, one based, e.g., '1-5, 8, 11-13'. Pages are
         * printed in the document order, not in the order specified, and no
         * more than once.
         * Defaults to empty string, which implies the entire document is printed.
         * The page numbers are quietly capped to actual page count of the
         * document, and ranges beyond the end of the document are ignored.
         * If this results in no pages to print, an error is reported.
         * It is an error to specify a range with start greater than end.
         */
        public val pageRanges: String? = null,
        /**
         * HTML template for the print header. Should be valid HTML markup with following
         * classes used to inject printing values into them:
         * - `date`: formatted print date
         * - `title`: document title
         * - `url`: document location
         * - `pageNumber`: current page number
         * - `totalPages`: total pages in the document
         *
         * For example, `<span class=title></span>` would generate span containing the title.
         */
        public val headerTemplate: String? = null,
        /**
         * HTML template for the print footer. Should use the same format as the `headerTemplate`.
         */
        public val footerTemplate: String? = null,
        /**
         * Whether or not to prefer page size as defined by css. Defaults to false,
         * in which case the content will be scaled to fit the paper size.
         */
        public val preferCSSPageSize: Boolean? = null,
        /**
         * return as stream
         */
        public val transferMode: String? = null,
        /**
         * Whether or not to generate tagged (accessible) PDF. Defaults to embedder choice.
         */
        public val generateTaggedPDF: Boolean? = null,
        /**
         * Whether or not to embed the document outline into the PDF.
         */
        public val generateDocumentOutline: Boolean? = null,
    )

    @Serializable
    public data class PrintToPDFReturn(
        /**
         * Base64-encoded pdf data. Empty if |returnAsStream| is specified. (Encoded as a base64 string when passed over JSON)
         */
        public val `data`: String,
        /**
         * A handle of the stream that holds resulting PDF data.
         */
        public val stream: String?,
    )

    @Serializable
    public data class ReloadParameter(
        /**
         * If true, browser cache is ignored (as if the user pressed Shift+refresh).
         */
        public val ignoreCache: Boolean? = null,
        /**
         * If set, the script will be injected into all frames of the inspected page after reload.
         * Argument will be ignored if reloading dataURL origin.
         */
        public val scriptToEvaluateOnLoad: String? = null,
        /**
         * If set, an error will be thrown if the target page's main frame's
         * loader id does not match the provided id. This prevents accidentally
         * reloading an unintended target in case there's a racing navigation.
         */
        public val loaderId: String? = null,
    )

    @Serializable
    public data class RemoveScriptToEvaluateOnLoadParameter(
        public val identifier: String,
    )

    @Serializable
    public data class RemoveScriptToEvaluateOnNewDocumentParameter(
        public val identifier: String,
    )

    @Serializable
    public data class ScreencastFrameAckParameter(
        /**
         * Frame number.
         */
        public val sessionId: Int,
    )

    @Serializable
    public data class SearchInResourceParameter(
        /**
         * Frame id for resource to search in.
         */
        public val frameId: String,
        /**
         * URL of the resource to search in.
         */
        public val url: String,
        /**
         * String to search for.
         */
        public val query: String,
        /**
         * If true, search is case sensitive.
         */
        public val caseSensitive: Boolean? = null,
        /**
         * If true, treats string parameter as regex.
         */
        public val isRegex: Boolean? = null,
    )

    @Serializable
    public data class SearchInResourceReturn(
        /**
         * List of search matches.
         */
        public val result: List<Debugger.SearchMatch>,
    )

    @Serializable
    public data class SetAdBlockingEnabledParameter(
        /**
         * Whether to block ads.
         */
        public val enabled: Boolean,
    )

    @Serializable
    public data class SetBypassCSPParameter(
        /**
         * Whether to bypass page CSP.
         */
        public val enabled: Boolean,
    )

    @Serializable
    public data class GetPermissionsPolicyStateParameter(
        public val frameId: String,
    )

    @Serializable
    public data class GetPermissionsPolicyStateReturn(
        public val states: List<PermissionsPolicyFeatureState>,
    )

    @Serializable
    public data class GetOriginTrialsParameter(
        public val frameId: String,
    )

    @Serializable
    public data class GetOriginTrialsReturn(
        public val originTrials: List<OriginTrial>,
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
        public val screenOrientation: Emulation.ScreenOrientation? = null,
        /**
         * The viewport dimensions and scale. If not set, the override is cleared.
         */
        public val viewport: Viewport? = null,
    )

    @Serializable
    public data class SetDeviceOrientationOverrideParameter(
        /**
         * Mock alpha
         */
        public val alpha: Double,
        /**
         * Mock beta
         */
        public val beta: Double,
        /**
         * Mock gamma
         */
        public val gamma: Double,
    )

    @Serializable
    public data class SetFontFamiliesParameter(
        /**
         * Specifies font families to set. If a font family is not specified, it won't be changed.
         */
        public val fontFamilies: FontFamilies,
        /**
         * Specifies font families to set for individual scripts.
         */
        public val forScripts: List<ScriptFontFamilies>? = null,
    )

    @Serializable
    public data class SetFontSizesParameter(
        /**
         * Specifies font sizes to set. If a font size is not specified, it won't be changed.
         */
        public val fontSizes: FontSizes,
    )

    @Serializable
    public data class SetDocumentContentParameter(
        /**
         * Frame id to set HTML for.
         */
        public val frameId: String,
        /**
         * HTML content to set.
         */
        public val html: String,
    )

    @Serializable
    public data class SetDownloadBehaviorParameter(
        /**
         * Whether to allow all or deny all download requests, or use default Chrome behavior if
         * available (otherwise deny).
         */
        public val behavior: String,
        /**
         * The default path to save downloaded files to. This is required if behavior is set to 'allow'
         */
        public val downloadPath: String? = null,
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
    )

    @Serializable
    public data class SetLifecycleEventsEnabledParameter(
        /**
         * If true, starts emitting lifecycle events.
         */
        public val enabled: Boolean,
    )

    @Serializable
    public data class SetTouchEmulationEnabledParameter(
        /**
         * Whether the touch event emulation should be enabled.
         */
        public val enabled: Boolean,
        /**
         * Touch/gesture events configuration. Default: current platform.
         */
        public val configuration: String? = null,
    )

    @Serializable
    public data class StartScreencastParameter(
        /**
         * Image compression format.
         */
        public val format: String? = null,
        /**
         * Compression quality from range [0..100].
         */
        public val quality: Int? = null,
        /**
         * Maximum screenshot width.
         */
        public val maxWidth: Int? = null,
        /**
         * Maximum screenshot height.
         */
        public val maxHeight: Int? = null,
        /**
         * Send every n-th frame.
         */
        public val everyNthFrame: Int? = null,
    )

    @Serializable
    public data class SetWebLifecycleStateParameter(
        /**
         * Target lifecycle state
         */
        public val state: String,
    )

    @Serializable
    public data class ProduceCompilationCacheParameter(
        public val scripts: List<CompilationCacheParams>,
    )

    @Serializable
    public data class AddCompilationCacheParameter(
        public val url: String,
        /**
         * Base64-encoded data (Encoded as a base64 string when passed over JSON)
         */
        public val `data`: String,
    )

    @Serializable
    public data class SetSPCTransactionModeParameter(
        public val mode: String,
    )

    @Serializable
    public data class SetRPHRegistrationModeParameter(
        public val mode: String,
    )

    @Serializable
    public data class GenerateTestReportParameter(
        /**
         * Message to be displayed in the report.
         */
        public val message: String,
        /**
         * Specifies the endpoint group to deliver the report to.
         */
        public val group: String? = null,
    )

    @Serializable
    public data class SetInterceptFileChooserDialogParameter(
        public val enabled: Boolean,
        /**
         * If true, cancels the dialog by emitting relevant events (if any)
         * in addition to not showing it if the interception is enabled
         * (default: false).
         */
        public val cancel: Boolean? = null,
    )

    @Serializable
    public data class SetPrerenderingAllowedParameter(
        public val isAllowed: Boolean,
    )
}
