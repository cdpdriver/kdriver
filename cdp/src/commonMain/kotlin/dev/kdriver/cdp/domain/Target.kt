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
 * Supports additional targets discovery and allows to attach to them.
 */
public val CDP.target: Target
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Target(this))

/**
 * Supports additional targets discovery and allows to attach to them.
 */
public class Target(
    private val cdp: CDP,
) : Domain {
    /**
     * Issued when attached to target because of auto-attach or `attachToTarget` command.
     */
    public val attachedToTarget: Flow<AttachedToTargetParameter> = cdp
        .events
        .filter { it.method == "Target.attachedToTarget" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Issued when detached from target for any reason (including `detachFromTarget` command). Can be
     * issued multiple times per target if multiple sessions have been attached to it.
     */
    public val detachedFromTarget: Flow<DetachedFromTargetParameter> = cdp
        .events
        .filter { it.method == "Target.detachedFromTarget" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Notifies about a new protocol message received from the session (as reported in
     * `attachedToTarget` event).
     */
    public val receivedMessageFromTarget: Flow<ReceivedMessageFromTargetParameter> = cdp
        .events
        .filter { it.method == "Target.receivedMessageFromTarget" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Issued when a possible inspection target is created.
     */
    public val targetCreated: Flow<TargetCreatedParameter> = cdp
        .events
        .filter { it.method == "Target.targetCreated" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Issued when a target is destroyed.
     */
    public val targetDestroyed: Flow<TargetDestroyedParameter> = cdp
        .events
        .filter { it.method == "Target.targetDestroyed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Issued when a target has crashed.
     */
    public val targetCrashed: Flow<TargetCrashedParameter> = cdp
        .events
        .filter { it.method == "Target.targetCrashed" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Issued when some information about a target has changed. This only happens between
     * `targetCreated` and `targetDestroyed`.
     */
    public val targetInfoChanged: Flow<TargetInfoChangedParameter> = cdp
        .events
        .filter { it.method == "Target.targetInfoChanged" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Activates (focuses) the target.
     */
    public suspend fun activateTarget(args: ActivateTargetParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Target.activateTarget", parameter, mode)
    }

    /**
     * Activates (focuses) the target.
     *
     * @param targetId No description
     */
    public suspend fun activateTarget(targetId: String) {
        val parameter = ActivateTargetParameter(targetId = targetId)
        activateTarget(parameter)
    }

    /**
     * Attaches to the target with given id.
     */
    public suspend fun attachToTarget(
        args: AttachToTargetParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): AttachToTargetReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Target.attachToTarget", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Attaches to the target with given id.
     *
     * @param targetId No description
     * @param flatten Enables "flat" access to the session via specifying sessionId attribute in the commands.
     * We plan to make this the default, deprecate non-flattened mode,
     * and eventually retire it. See crbug.com/991325.
     */
    public suspend fun attachToTarget(targetId: String, flatten: Boolean? = null): AttachToTargetReturn {
        val parameter = AttachToTargetParameter(targetId = targetId, flatten = flatten)
        return attachToTarget(parameter)
    }

    /**
     * Attaches to the browser target, only uses flat sessionId mode.
     */
    public suspend fun attachToBrowserTarget(mode: CommandMode = CommandMode.DEFAULT): AttachToBrowserTargetReturn {
        val parameter = null
        val result = cdp.callCommand("Target.attachToBrowserTarget", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Closes the target. If the target is a page that gets closed too.
     */
    public suspend fun closeTarget(
        args: CloseTargetParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): CloseTargetReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Target.closeTarget", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Closes the target. If the target is a page that gets closed too.
     *
     * @param targetId No description
     */
    public suspend fun closeTarget(targetId: String): CloseTargetReturn {
        val parameter = CloseTargetParameter(targetId = targetId)
        return closeTarget(parameter)
    }

    /**
     * Inject object to the target's main frame that provides a communication
     * channel with browser target.
     *
     * Injected object will be available as `window[bindingName]`.
     *
     * The object has the following API:
     * - `binding.send(json)` - a method to send messages over the remote debugging protocol
     * - `binding.onmessage = json => handleMessage(json)` - a callback that will be called for the protocol notifications and command responses.
     */
    public suspend fun exposeDevToolsProtocol(
        args: ExposeDevToolsProtocolParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Target.exposeDevToolsProtocol", parameter, mode)
    }

    /**
     * Inject object to the target's main frame that provides a communication
     * channel with browser target.
     *
     * Injected object will be available as `window[bindingName]`.
     *
     * The object has the following API:
     * - `binding.send(json)` - a method to send messages over the remote debugging protocol
     * - `binding.onmessage = json => handleMessage(json)` - a callback that will be called for the protocol notifications and command responses.
     *
     * @param targetId No description
     * @param bindingName Binding name, 'cdp' if not specified.
     * @param inheritPermissions If true, inherits the current root session's permissions (default: false).
     */
    public suspend fun exposeDevToolsProtocol(
        targetId: String,
        bindingName: String? = null,
        inheritPermissions: Boolean? = null,
    ) {
        val parameter = ExposeDevToolsProtocolParameter(
            targetId = targetId,
            bindingName = bindingName,
            inheritPermissions = inheritPermissions
        )
        exposeDevToolsProtocol(parameter)
    }

    /**
     * Creates a new empty BrowserContext. Similar to an incognito profile but you can have more than
     * one.
     */
    public suspend fun createBrowserContext(
        args: CreateBrowserContextParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): CreateBrowserContextReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Target.createBrowserContext", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Creates a new empty BrowserContext. Similar to an incognito profile but you can have more than
     * one.
     *
     * @param disposeOnDetach If specified, disposes this context when debugging session disconnects.
     * @param proxyServer Proxy server, similar to the one passed to --proxy-server
     * @param proxyBypassList Proxy bypass list, similar to the one passed to --proxy-bypass-list
     * @param originsWithUniversalNetworkAccess An optional list of origins to grant unlimited cross-origin access to.
     * Parts of the URL other than those constituting origin are ignored.
     */
    public suspend fun createBrowserContext(
        disposeOnDetach: Boolean? = null,
        proxyServer: String? = null,
        proxyBypassList: String? = null,
        originsWithUniversalNetworkAccess: List<String>? = null,
    ): CreateBrowserContextReturn {
        val parameter = CreateBrowserContextParameter(
            disposeOnDetach = disposeOnDetach,
            proxyServer = proxyServer,
            proxyBypassList = proxyBypassList,
            originsWithUniversalNetworkAccess = originsWithUniversalNetworkAccess
        )
        return createBrowserContext(parameter)
    }

    /**
     * Returns all browser contexts created with `Target.createBrowserContext` method.
     */
    public suspend fun getBrowserContexts(mode: CommandMode = CommandMode.DEFAULT): GetBrowserContextsReturn {
        val parameter = null
        val result = cdp.callCommand("Target.getBrowserContexts", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Creates a new page.
     */
    public suspend fun createTarget(
        args: CreateTargetParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): CreateTargetReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Target.createTarget", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Creates a new page.
     *
     * @param url The initial URL the page will be navigated to. An empty string indicates about:blank.
     * @param left Frame left origin in DIP (requires newWindow to be true or headless shell).
     * @param top Frame top origin in DIP (requires newWindow to be true or headless shell).
     * @param width Frame width in DIP (requires newWindow to be true or headless shell).
     * @param height Frame height in DIP (requires newWindow to be true or headless shell).
     * @param windowState Frame window state (requires newWindow to be true or headless shell).
     * Default is normal.
     * @param browserContextId The browser context to create the page in.
     * @param enableBeginFrameControl Whether BeginFrames for this target will be controlled via DevTools (headless shell only,
     * not supported on MacOS yet, false by default).
     * @param newWindow Whether to create a new Window or Tab (false by default, not supported by headless shell).
     * @param background Whether to create the target in background or foreground (false by default, not supported
     * by headless shell).
     * @param forTab Whether to create the target of type "tab".
     * @param hidden Whether to create a hidden target. The hidden target is observable via protocol, but not
     * present in the tab UI strip. Cannot be created with `forTab: true`, `newWindow: true` or
     * `background: false`. The life-time of the tab is limited to the life-time of the session.
     */
    public suspend fun createTarget(
        url: String,
        left: Int? = null,
        top: Int? = null,
        width: Int? = null,
        height: Int? = null,
        windowState: WindowState? = null,
        browserContextId: String? = null,
        enableBeginFrameControl: Boolean? = null,
        newWindow: Boolean? = null,
        background: Boolean? = null,
        forTab: Boolean? = null,
        hidden: Boolean? = null,
    ): CreateTargetReturn {
        val parameter = CreateTargetParameter(
            url = url,
            left = left,
            top = top,
            width = width,
            height = height,
            windowState = windowState,
            browserContextId = browserContextId,
            enableBeginFrameControl = enableBeginFrameControl,
            newWindow = newWindow,
            background = background,
            forTab = forTab,
            hidden = hidden
        )
        return createTarget(parameter)
    }

    /**
     * Detaches session with given id.
     */
    public suspend fun detachFromTarget(args: DetachFromTargetParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Target.detachFromTarget", parameter, mode)
    }

    /**
     * Detaches session with given id.
     *
     * @param sessionId Session to detach.
     * @param targetId Deprecated.
     */
    public suspend fun detachFromTarget(sessionId: String? = null, targetId: String? = null) {
        val parameter = DetachFromTargetParameter(sessionId = sessionId, targetId = targetId)
        detachFromTarget(parameter)
    }

    /**
     * Deletes a BrowserContext. All the belonging pages will be closed without calling their
     * beforeunload hooks.
     */
    public suspend fun disposeBrowserContext(
        args: DisposeBrowserContextParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Target.disposeBrowserContext", parameter, mode)
    }

    /**
     * Deletes a BrowserContext. All the belonging pages will be closed without calling their
     * beforeunload hooks.
     *
     * @param browserContextId No description
     */
    public suspend fun disposeBrowserContext(browserContextId: String) {
        val parameter = DisposeBrowserContextParameter(browserContextId = browserContextId)
        disposeBrowserContext(parameter)
    }

    /**
     * Returns information about a target.
     */
    public suspend fun getTargetInfo(
        args: GetTargetInfoParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetTargetInfoReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Target.getTargetInfo", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Returns information about a target.
     *
     * @param targetId No description
     */
    public suspend fun getTargetInfo(targetId: String? = null): GetTargetInfoReturn {
        val parameter = GetTargetInfoParameter(targetId = targetId)
        return getTargetInfo(parameter)
    }

    /**
     * Retrieves a list of available targets.
     */
    public suspend fun getTargets(
        args: GetTargetsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): GetTargetsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Target.getTargets", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Retrieves a list of available targets.
     *
     * @param filter Only targets matching filter will be reported. If filter is not specified
     * and target discovery is currently enabled, a filter used for target discovery
     * is used for consistency.
     */
    public suspend fun getTargets(filter: List<Double>? = null): GetTargetsReturn {
        val parameter = GetTargetsParameter(filter = filter)
        return getTargets(parameter)
    }

    /**
     * Sends protocol message over session with given id.
     * Consider using flat mode instead; see commands attachToTarget, setAutoAttach,
     * and crbug.com/991325.
     */
    @Deprecated(message = "")
    public suspend fun sendMessageToTarget(
        args: SendMessageToTargetParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Target.sendMessageToTarget", parameter, mode)
    }

    /**
     * Sends protocol message over session with given id.
     * Consider using flat mode instead; see commands attachToTarget, setAutoAttach,
     * and crbug.com/991325.
     *
     * @param message No description
     * @param sessionId Identifier of the session.
     * @param targetId Deprecated.
     */
    @Deprecated(message = "")
    public suspend fun sendMessageToTarget(
        message: String,
        sessionId: String? = null,
        targetId: String? = null,
    ) {
        val parameter = SendMessageToTargetParameter(message = message, sessionId = sessionId, targetId = targetId)
        sendMessageToTarget(parameter)
    }

    /**
     * Controls whether to automatically attach to new targets which are considered
     * to be directly related to this one (for example, iframes or workers).
     * When turned on, attaches to all existing related targets as well. When turned off,
     * automatically detaches from all currently attached targets.
     * This also clears all targets added by `autoAttachRelated` from the list of targets to watch
     * for creation of related targets.
     * You might want to call this recursively for auto-attached targets to attach
     * to all available targets.
     */
    public suspend fun setAutoAttach(args: SetAutoAttachParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Target.setAutoAttach", parameter, mode)
    }

    /**
     * Controls whether to automatically attach to new targets which are considered
     * to be directly related to this one (for example, iframes or workers).
     * When turned on, attaches to all existing related targets as well. When turned off,
     * automatically detaches from all currently attached targets.
     * This also clears all targets added by `autoAttachRelated` from the list of targets to watch
     * for creation of related targets.
     * You might want to call this recursively for auto-attached targets to attach
     * to all available targets.
     *
     * @param autoAttach Whether to auto-attach to related targets.
     * @param waitForDebuggerOnStart Whether to pause new targets when attaching to them. Use `Runtime.runIfWaitingForDebugger`
     * to run paused targets.
     * @param flatten Enables "flat" access to the session via specifying sessionId attribute in the commands.
     * We plan to make this the default, deprecate non-flattened mode,
     * and eventually retire it. See crbug.com/991325.
     * @param filter Only targets matching filter will be attached.
     */
    public suspend fun setAutoAttach(
        autoAttach: Boolean,
        waitForDebuggerOnStart: Boolean,
        flatten: Boolean? = null,
        filter: List<Double>? = null,
    ) {
        val parameter = SetAutoAttachParameter(
            autoAttach = autoAttach,
            waitForDebuggerOnStart = waitForDebuggerOnStart,
            flatten = flatten,
            filter = filter
        )
        setAutoAttach(parameter)
    }

    /**
     * Adds the specified target to the list of targets that will be monitored for any related target
     * creation (such as child frames, child workers and new versions of service worker) and reported
     * through `attachedToTarget`. The specified target is also auto-attached.
     * This cancels the effect of any previous `setAutoAttach` and is also cancelled by subsequent
     * `setAutoAttach`. Only available at the Browser target.
     */
    public suspend fun autoAttachRelated(args: AutoAttachRelatedParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Target.autoAttachRelated", parameter, mode)
    }

    /**
     * Adds the specified target to the list of targets that will be monitored for any related target
     * creation (such as child frames, child workers and new versions of service worker) and reported
     * through `attachedToTarget`. The specified target is also auto-attached.
     * This cancels the effect of any previous `setAutoAttach` and is also cancelled by subsequent
     * `setAutoAttach`. Only available at the Browser target.
     *
     * @param targetId No description
     * @param waitForDebuggerOnStart Whether to pause new targets when attaching to them. Use `Runtime.runIfWaitingForDebugger`
     * to run paused targets.
     * @param filter Only targets matching filter will be attached.
     */
    public suspend fun autoAttachRelated(
        targetId: String,
        waitForDebuggerOnStart: Boolean,
        filter: List<Double>? = null,
    ) {
        val parameter = AutoAttachRelatedParameter(
            targetId = targetId,
            waitForDebuggerOnStart = waitForDebuggerOnStart,
            filter = filter
        )
        autoAttachRelated(parameter)
    }

    /**
     * Controls whether to discover available targets and notify via
     * `targetCreated/targetInfoChanged/targetDestroyed` events.
     */
    public suspend fun setDiscoverTargets(args: SetDiscoverTargetsParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Target.setDiscoverTargets", parameter, mode)
    }

    /**
     * Controls whether to discover available targets and notify via
     * `targetCreated/targetInfoChanged/targetDestroyed` events.
     *
     * @param discover Whether to discover available targets.
     * @param filter Only targets matching filter will be attached. If `discover` is false,
     * `filter` must be omitted or empty.
     */
    public suspend fun setDiscoverTargets(discover: Boolean, filter: List<Double>? = null) {
        val parameter = SetDiscoverTargetsParameter(discover = discover, filter = filter)
        setDiscoverTargets(parameter)
    }

    /**
     * Enables target discovery for the specified locations, when `setDiscoverTargets` was set to
     * `true`.
     */
    public suspend fun setRemoteLocations(args: SetRemoteLocationsParameter, mode: CommandMode = CommandMode.DEFAULT) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Target.setRemoteLocations", parameter, mode)
    }

    /**
     * Enables target discovery for the specified locations, when `setDiscoverTargets` was set to
     * `true`.
     *
     * @param locations List of remote locations.
     */
    public suspend fun setRemoteLocations(locations: List<RemoteLocation>) {
        val parameter = SetRemoteLocationsParameter(locations = locations)
        setRemoteLocations(parameter)
    }

    /**
     * Opens a DevTools window for the target.
     */
    public suspend fun openDevTools(
        args: OpenDevToolsParameter,
        mode: CommandMode = CommandMode.DEFAULT,
    ): OpenDevToolsReturn {
        val parameter = Serialization.json.encodeToJsonElement(args)
        val result = cdp.callCommand("Target.openDevTools", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Opens a DevTools window for the target.
     *
     * @param targetId This can be the page or tab target ID.
     */
    public suspend fun openDevTools(targetId: String): OpenDevToolsReturn {
        val parameter = OpenDevToolsParameter(targetId = targetId)
        return openDevTools(parameter)
    }

    @Serializable
    public data class TargetInfo(
        public val targetId: String,
        /**
         * List of types: https://source.chromium.org/chromium/chromium/src/+/main:content/browser/devtools/devtools_agent_host_impl.cc?ss=chromium&q=f:devtools%20-f:out%20%22::kTypeTab%5B%5D%22
         */
        public val type: String,
        public val title: String,
        public val url: String,
        /**
         * Whether the target has an attached client.
         */
        public val attached: Boolean,
        /**
         * Opener target Id
         */
        public val openerId: String? = null,
        /**
         * Whether the target has access to the originating window.
         */
        public val canAccessOpener: Boolean,
        /**
         * Frame id of originating window (is only set if target has an opener).
         */
        public val openerFrameId: String? = null,
        public val browserContextId: String? = null,
        /**
         * Provides additional details for specific target types. For example, for
         * the type of "page", this may be set to "prerender".
         */
        public val subtype: String? = null,
    )

    /**
     * A filter used by target query/discovery/auto-attach operations.
     */
    @Serializable
    public data class FilterEntry(
        /**
         * If set, causes exclusion of matching targets from the list.
         */
        public val exclude: Boolean? = null,
        /**
         * If not present, matches any type.
         */
        public val type: String? = null,
    )

    @Serializable
    public data class RemoteLocation(
        public val host: String,
        public val port: Int,
    )

    /**
     * The state of the target window.
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
     * Issued when attached to target because of auto-attach or `attachToTarget` command.
     */
    @Serializable
    public data class AttachedToTargetParameter(
        /**
         * Identifier assigned to the session used to send/receive messages.
         */
        public val sessionId: String,
        public val targetInfo: TargetInfo,
        public val waitingForDebugger: Boolean,
    )

    /**
     * Issued when detached from target for any reason (including `detachFromTarget` command). Can be
     * issued multiple times per target if multiple sessions have been attached to it.
     */
    @Serializable
    public data class DetachedFromTargetParameter(
        /**
         * Detached session identifier.
         */
        public val sessionId: String,
        /**
         * Deprecated.
         */
        public val targetId: String? = null,
    )

    /**
     * Notifies about a new protocol message received from the session (as reported in
     * `attachedToTarget` event).
     */
    @Serializable
    public data class ReceivedMessageFromTargetParameter(
        /**
         * Identifier of a session which sends a message.
         */
        public val sessionId: String,
        public val message: String,
        /**
         * Deprecated.
         */
        public val targetId: String? = null,
    )

    /**
     * Issued when a possible inspection target is created.
     */
    @Serializable
    public data class TargetCreatedParameter(
        public val targetInfo: TargetInfo,
    )

    /**
     * Issued when a target is destroyed.
     */
    @Serializable
    public data class TargetDestroyedParameter(
        public val targetId: String,
    )

    /**
     * Issued when a target has crashed.
     */
    @Serializable
    public data class TargetCrashedParameter(
        public val targetId: String,
        /**
         * Termination status type.
         */
        public val status: String,
        /**
         * Termination error code.
         */
        public val errorCode: Int,
    )

    /**
     * Issued when some information about a target has changed. This only happens between
     * `targetCreated` and `targetDestroyed`.
     */
    @Serializable
    public data class TargetInfoChangedParameter(
        public val targetInfo: TargetInfo,
    )

    @Serializable
    public data class ActivateTargetParameter(
        public val targetId: String,
    )

    @Serializable
    public data class AttachToTargetParameter(
        public val targetId: String,
        /**
         * Enables "flat" access to the session via specifying sessionId attribute in the commands.
         * We plan to make this the default, deprecate non-flattened mode,
         * and eventually retire it. See crbug.com/991325.
         */
        public val flatten: Boolean? = null,
    )

    @Serializable
    public data class AttachToTargetReturn(
        /**
         * Id assigned to the session.
         */
        public val sessionId: String,
    )

    @Serializable
    public data class AttachToBrowserTargetReturn(
        /**
         * Id assigned to the session.
         */
        public val sessionId: String,
    )

    @Serializable
    public data class CloseTargetParameter(
        public val targetId: String,
    )

    @Serializable
    public data class CloseTargetReturn(
        /**
         * Always set to true. If an error occurs, the response indicates protocol error.
         */
        public val success: Boolean,
    )

    @Serializable
    public data class ExposeDevToolsProtocolParameter(
        public val targetId: String,
        /**
         * Binding name, 'cdp' if not specified.
         */
        public val bindingName: String? = null,
        /**
         * If true, inherits the current root session's permissions (default: false).
         */
        public val inheritPermissions: Boolean? = null,
    )

    @Serializable
    public data class CreateBrowserContextParameter(
        /**
         * If specified, disposes this context when debugging session disconnects.
         */
        public val disposeOnDetach: Boolean? = null,
        /**
         * Proxy server, similar to the one passed to --proxy-server
         */
        public val proxyServer: String? = null,
        /**
         * Proxy bypass list, similar to the one passed to --proxy-bypass-list
         */
        public val proxyBypassList: String? = null,
        /**
         * An optional list of origins to grant unlimited cross-origin access to.
         * Parts of the URL other than those constituting origin are ignored.
         */
        public val originsWithUniversalNetworkAccess: List<String>? = null,
    )

    @Serializable
    public data class CreateBrowserContextReturn(
        /**
         * The id of the context created.
         */
        public val browserContextId: String,
    )

    @Serializable
    public data class GetBrowserContextsReturn(
        /**
         * An array of browser context ids.
         */
        public val browserContextIds: List<String>,
    )

    @Serializable
    public data class CreateTargetParameter(
        /**
         * The initial URL the page will be navigated to. An empty string indicates about:blank.
         */
        public val url: String,
        /**
         * Frame left origin in DIP (requires newWindow to be true or headless shell).
         */
        public val left: Int? = null,
        /**
         * Frame top origin in DIP (requires newWindow to be true or headless shell).
         */
        public val top: Int? = null,
        /**
         * Frame width in DIP (requires newWindow to be true or headless shell).
         */
        public val width: Int? = null,
        /**
         * Frame height in DIP (requires newWindow to be true or headless shell).
         */
        public val height: Int? = null,
        /**
         * Frame window state (requires newWindow to be true or headless shell).
         * Default is normal.
         */
        public val windowState: WindowState? = null,
        /**
         * The browser context to create the page in.
         */
        public val browserContextId: String? = null,
        /**
         * Whether BeginFrames for this target will be controlled via DevTools (headless shell only,
         * not supported on MacOS yet, false by default).
         */
        public val enableBeginFrameControl: Boolean? = null,
        /**
         * Whether to create a new Window or Tab (false by default, not supported by headless shell).
         */
        public val newWindow: Boolean? = null,
        /**
         * Whether to create the target in background or foreground (false by default, not supported
         * by headless shell).
         */
        public val background: Boolean? = null,
        /**
         * Whether to create the target of type "tab".
         */
        public val forTab: Boolean? = null,
        /**
         * Whether to create a hidden target. The hidden target is observable via protocol, but not
         * present in the tab UI strip. Cannot be created with `forTab: true`, `newWindow: true` or
         * `background: false`. The life-time of the tab is limited to the life-time of the session.
         */
        public val hidden: Boolean? = null,
    )

    @Serializable
    public data class CreateTargetReturn(
        /**
         * The id of the page opened.
         */
        public val targetId: String,
    )

    @Serializable
    public data class DetachFromTargetParameter(
        /**
         * Session to detach.
         */
        public val sessionId: String? = null,
        /**
         * Deprecated.
         */
        public val targetId: String? = null,
    )

    @Serializable
    public data class DisposeBrowserContextParameter(
        public val browserContextId: String,
    )

    @Serializable
    public data class GetTargetInfoParameter(
        public val targetId: String? = null,
    )

    @Serializable
    public data class GetTargetInfoReturn(
        public val targetInfo: TargetInfo,
    )

    @Serializable
    public data class GetTargetsParameter(
        /**
         * Only targets matching filter will be reported. If filter is not specified
         * and target discovery is currently enabled, a filter used for target discovery
         * is used for consistency.
         */
        public val filter: List<Double>? = null,
    )

    @Serializable
    public data class GetTargetsReturn(
        /**
         * The list of targets.
         */
        public val targetInfos: List<TargetInfo>,
    )

    @Serializable
    public data class SendMessageToTargetParameter(
        public val message: String,
        /**
         * Identifier of the session.
         */
        public val sessionId: String? = null,
        /**
         * Deprecated.
         */
        public val targetId: String? = null,
    )

    @Serializable
    public data class SetAutoAttachParameter(
        /**
         * Whether to auto-attach to related targets.
         */
        public val autoAttach: Boolean,
        /**
         * Whether to pause new targets when attaching to them. Use `Runtime.runIfWaitingForDebugger`
         * to run paused targets.
         */
        public val waitForDebuggerOnStart: Boolean,
        /**
         * Enables "flat" access to the session via specifying sessionId attribute in the commands.
         * We plan to make this the default, deprecate non-flattened mode,
         * and eventually retire it. See crbug.com/991325.
         */
        public val flatten: Boolean? = null,
        /**
         * Only targets matching filter will be attached.
         */
        public val filter: List<Double>? = null,
    )

    @Serializable
    public data class AutoAttachRelatedParameter(
        public val targetId: String,
        /**
         * Whether to pause new targets when attaching to them. Use `Runtime.runIfWaitingForDebugger`
         * to run paused targets.
         */
        public val waitForDebuggerOnStart: Boolean,
        /**
         * Only targets matching filter will be attached.
         */
        public val filter: List<Double>? = null,
    )

    @Serializable
    public data class SetDiscoverTargetsParameter(
        /**
         * Whether to discover available targets.
         */
        public val discover: Boolean,
        /**
         * Only targets matching filter will be attached. If `discover` is false,
         * `filter` must be omitted or empty.
         */
        public val filter: List<Double>? = null,
    )

    @Serializable
    public data class SetRemoteLocationsParameter(
        /**
         * List of remote locations.
         */
        public val locations: List<RemoteLocation>,
    )

    @Serializable
    public data class OpenDevToolsParameter(
        /**
         * This can be the page or tab target ID.
         */
        public val targetId: String,
    )

    @Serializable
    public data class OpenDevToolsReturn(
        /**
         * The targetId of DevTools page target.
         */
        public val targetId: String,
    )
}
