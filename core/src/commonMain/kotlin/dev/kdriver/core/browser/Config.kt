package dev.kdriver.core.browser

import dev.kdriver.core.exceptions.NoBrowserExecutablePathException
import io.ktor.util.logging.*
import kotlinx.io.files.Path

class Config(
    userDataDir: Path? = null,
    val headless: Boolean = Defaults.HEADLESS,
    val userAgent: String? = null,
    browserExecutablePath: Path? = null,
    browserArgs: List<String>? = null,
    sandbox: Boolean = Defaults.SANDBOX,
    val lang: String? = null,
    var host: String? = null,
    var port: Int? = null,
    val expert: Boolean = Defaults.EXPERT,
    val browserConnectionTimeout: Long = Defaults.BROWSER_CONNECTION_TIMEOUT,
    val browserConnectionMaxTries: Int = Defaults.BROWSER_CONNECTION_MAX_TRIES,
    val commandTimeout: Long = Defaults.COMMAND_TIMEOUT,
    /**
     * How long, in milliseconds, the connection must receive nothing before it counts as idle.
     */
    val timeBeforeConsideredIdle: Long = Defaults.TIME_BEFORE_CONSIDERED_IDLE,
    val autoDiscoverTargets: Boolean = Defaults.AUTO_DISCOVER_TARGETS,
    val debugStringLimit: Int = Defaults.DEBUG_STRING_LIMIT,
    /**
     * Address of an already running browser to connect to, instead of launching one — typically a
     * browser reached through a reverse proxy, possibly over TLS and under a path prefix:
     * `https://example.com/browser`.
     *
     * The DevTools HTTP endpoints are requested below it (`https://example.com/browser/json/version`)
     * and the WebSocket connections use the same address with the matching scheme (`ws` for `http`,
     * `wss` for `https`). The `webSocketDebuggerUrl` the browser advertises (which names its own,
     * internal, host and port) is rewritten onto this address, keeping only its path.
     *
     * Must be an `http://` or `https://` URL, without query, fragment or credentials (put those in
     * [connectionHeaders]). Takes precedence over [host] and [port]. When null (the
     * default), [host] and [port] are used as before.
     *
     * A browser executable is still looked up when the configuration is created; on a machine
     * without one, pass any [browserExecutablePath]: it is only used to launch a browser.
     */
    val browserUrl: String? = null,
    /**
     * Extra HTTP headers sent with every request to the browser's DevTools endpoints: the HTTP
     * `/json/…` requests and the WebSocket handshakes. Useful to authenticate against a reverse proxy
     * in front of the browser, e.g. `mapOf("Authorization" to "Bearer …")`. Empty by default.
     */
    val connectionHeaders: Map<String, String> = emptyMap(),
    /**
     * Whether a CSS selector lookup on the whole page asks the page itself instead of reading the whole document.
     *
     * Concerns [dev.kdriver.core.tab.Tab.select], [dev.kdriver.core.tab.Tab.selectAll],
     * [dev.kdriver.core.tab.Tab.querySelector] and [dev.kdriver.core.tab.Tab.querySelectorAll] called without a node.
     * Off (the default), each of these reads the whole document (`DOM.getDocument`, depth -1, piercing) whenever the
     * page changed since the last read, which is after every navigation and on any live page, then queries it by node
     * id. On a large page that is a transfer of the size of the whole DOM (often a megabyte) per lookup.
     *
     * On, the page runs the selector (`Runtime.evaluate` of `document.querySelector` / `querySelectorAll`, the
     * selector passed as a JSON string literal) and only the match is described (`DOM.describeNode`, depth -1,
     * piercing). What changes for the caller:
     *
     * - **Elements**: they carry their node and its subtree (so [dev.kdriver.core.dom.Element.text],
     *   [dev.kdriver.core.dom.Element.textAll], attributes and children read the same), but no node id
     *   (`node.nodeId` is 0) and no [dev.kdriver.core.dom.Element.tree]: [dev.kdriver.core.dom.Element.parent]
     *   throws until [dev.kdriver.core.dom.Element.update] is called, which reads the document as before. Each element
     *   keeps the remote object the page returned, so its first call (`click`, `apply`, …) needs no `DOM.resolveNode`.
     * - **Shadow DOM and iframes**: same results as off: neither `document.querySelector` nor `DOM.querySelector` on
     *   the document looks inside a shadow root or an iframe. `selectAll(includeFrames = true)` reads the document as
     *   off (searching inside the iframes needs it), for the page's own matches too.
     * - **Waiting**: `select` and `selectAll` poll and time out as before. Until something reads the document again
     *   (a lookup under an element, `find`, `update`…), the browser sends no `DOM.*` mutation events for the page, so
     *   on a busy page the connection may count as idle sooner.
     * - **Navigation**: an object or execution context dropped by a navigation during the lookup counts as no match
     *   (`select` tries again), as a node gone from a document read does off; when only some matches of
     *   `querySelectorAll` are gone, none is returned rather than part of the old page's. A command Chrome cuts with
     *   "Inspected target navigated or closed" (also sent for a closed tab) is thrown, as off.
     * - **Errors**: an invalid selector throws an [dev.kdriver.core.exceptions.EvaluateException] (the page's
     *   `SyntaxError`) instead of a [dev.kdriver.cdp.CDPException]. The page's own `document.querySelector` is used:
     *   a page that replaces it changes the result.
     * - **Round trips**: `querySelector` costs two small ones (evaluate, describe) instead of a document read and a
     *   query; `querySelectorAll` costs one when nothing matches, three otherwise (evaluate, the array's properties,
     *   then every match described in parallel). Each match is described with its subtree: many matches, or nested
     *   ones, can transfer more than the document would: leave the setting off for a selector that matches most of
     *   the page. Each element holds its remote object until the page navigates, as one used off does after its first
     *   call; the array of `querySelectorAll` is released (one more command, sent with the descriptions).
     * - **Elements passed back**: a lookup under such an element ([dev.kdriver.core.dom.Element.querySelector], or a
     *   [dev.kdriver.core.dom.NodeOrElement.WrappedElement] passed to `querySelector`) updates it from the document
     *   first.
     *
     * Lookups under a node or an element ([dev.kdriver.core.dom.Element.querySelector], `find`, `findAll`, `xpath`)
     * are unchanged and still read the document. Off by default.
     */
    val lookupsInPage: Boolean = Defaults.LOOKUPS_IN_PAGE,
) {

    private val logger = KtorSimpleLogger("Config")

    private var _userDataDir: Path? = null
    private var _customDataDir: Boolean = false

    private val _browserArgs: MutableList<String> = browserArgs?.toMutableList() ?: mutableListOf()
    internal val _extensions: MutableList<Path> = mutableListOf()

    val browserExecutablePath: Path = browserExecutablePath
        ?: defaultBrowserSearchConfig().findBrowserExecutable()
        ?: throw NoBrowserExecutablePathException()

    var sandbox: Boolean = sandbox
        private set

    init {
        browserUrl?.let(DevToolsEndpoint::validateBrowserUrl)
        if (isPosix() && isRoot() && sandbox) {
            logger.info("Detected root usage, auto disabling sandbox mode")
            this.sandbox = false
        }
        userDataDir?.let {
            this.userDataDir = it
        }
    }

    var userDataDir: Path
        get() {
            if (_userDataDir == null) {
                _userDataDir = tempProfileDir()
                _customDataDir = false
            }
            return _userDataDir!!
        }
        set(value) {
            _userDataDir = value
            _customDataDir = true
        }

    val usesCustomDataDir: Boolean
        get() = _customDataDir

    private val defaultBrowserArgs: List<String> = listOf(
        "--remote-allow-origins=*",
        "--no-first-run",
        "--no-service-autorun",
        "--no-default-browser-check",
        "--homepage=about:blank",
        "--no-pings",
        "--password-store=basic",
        "--disable-infobars",
        "--disable-breakpad",
        "--disable-component-update",
        "--disable-backgrounding-occluded-windows",
        "--disable-renderer-backgrounding",
        "--disable-background-networking",
        "--disable-dev-shm-usage",
        "--disable-features=IsolateOrigins,DisableLoadExtensionCommandLineSwitch,site-per-process",
        "--disable-session-crashed-bubble",
        "--disable-search-engine-choice-screen"
    )

    val browserArgs: List<String>
        get() = (defaultBrowserArgs + _browserArgs).distinct().sorted()

    val extensions: List<Path>
        get() = _extensions.toList()

    operator fun invoke(): List<String> {
        val args = mutableListOf<String>()
        args.addAll(defaultBrowserArgs)
        args.add("--user-data-dir=$userDataDir")
        if (expert) {
            args.addAll(listOf("--disable-web-security", "--disable-site-isolation-trials"))
        }
        args.addAll(_browserArgs.filter { it !in args })
        if (headless) args.add("--headless=new")
        if (!sandbox) args.add("--no-sandbox")
        userAgent?.let { args.add("--user-agent=$it") }
        host?.let { args.add("--remote-debugging-host=$it") }
        port?.let { args.add("--remote-debugging-port=$it") }
        return args
    }

    /**
     * Returns an independent copy of this configuration.
     *
     * [DefaultBrowser.start] resolves and stores runtime state on the config it is given — it writes
     * [host] and [port], and appends `--load-extension` / `--lang` arguments. Handing the same
     * instance to a second browser would therefore make that browser take the `connectExisting`
     * branch: it would never launch a browser process at all, and would instead try to talk to the
     * previous one's now-dead port. [DefaultBrowser] copies its config on construction so that
     * callers can safely reuse (and retry with) the same [Config] instance.
     *
     * The copy is deep where it matters: the argument and extension lists are duplicated, so
     * mutating one config never affects the other.
     */
    fun copy(): Config = Config(
        // Pass the already-resolved executable so the copy does not re-run the disk search.
        browserExecutablePath = browserExecutablePath,
        headless = headless,
        userAgent = userAgent,
        // _browserArgs, not the browserArgs getter: the getter prepends defaultBrowserArgs, which
        // would end up duplicated into the copy's own arg list.
        browserArgs = _browserArgs.toList(),
        sandbox = sandbox,
        lang = lang,
        host = host,
        port = port,
        expert = expert,
        browserConnectionTimeout = browserConnectionTimeout,
        browserConnectionMaxTries = browserConnectionMaxTries,
        commandTimeout = commandTimeout,
        timeBeforeConsideredIdle = timeBeforeConsideredIdle,
        autoDiscoverTargets = autoDiscoverTargets,
        debugStringLimit = debugStringLimit,
        browserUrl = browserUrl,
        connectionHeaders = connectionHeaders,
        lookupsInPage = lookupsInPage,
    ).also { copy ->
        // Copy the backing field, never the userDataDir getter: reading it would materialise a
        // temporary profile directory on a config that may never be started.
        copy._userDataDir = _userDataDir
        copy._customDataDir = _customDataDir
        copy._extensions.addAll(_extensions)
    }

    fun addArgument(arg: String) {
        val forbiddenArgs = listOf("headless", "data-dir", "data_dir", "no-sandbox", "no_sandbox", "lang")
        if (forbiddenArgs.any { arg.contains(it, ignoreCase = true) }) {
            throw IllegalArgumentException("\"$arg\" not allowed. Please use one of the attributes of the Config object to set it")
        }
        _browserArgs.add(arg)
    }

    object Defaults {
        const val HEADLESS: Boolean = false
        const val SANDBOX: Boolean = true
        const val EXPERT: Boolean = false
        const val BROWSER_CONNECTION_TIMEOUT: Long = 500
        const val BROWSER_CONNECTION_MAX_TRIES: Int = 60

        /**
         * Default maximum time, in milliseconds, to wait for a CDP command response before
         * throwing a [dev.kdriver.core.exceptions.CommandTimeoutException]. A value <= 0 disables
         * the timeout (wait indefinitely).
         */
        const val COMMAND_TIMEOUT: Long = 30_000

        /**
         * How long, in milliseconds, the connection must receive nothing before it counts as idle.
         */
        const val TIME_BEFORE_CONSIDERED_IDLE: Long = 100

        /**
         * Default upper bound, in milliseconds, on how long
         * [dev.kdriver.core.connection.Connection.wait] will watch for the connection to go idle.
         *
         * A page can stream events indefinitely (polling, server-sent events, ads, a refreshing
         * interstitial), in which case idleness never arrives. That is not an error — waiting is
         * best-effort — so past this bound `wait` simply stops watching and returns.
         */
        const val IDLE_WAIT_TIMEOUT: Long = 10_000
        const val AUTO_DISCOVER_TARGETS: Boolean = true
        const val DEBUG_STRING_LIMIT: Int = 128

        /** See [Config.lookupsInPage]: off by default, lookups read the whole document. */
        const val LOOKUPS_IN_PAGE: Boolean = false
    }

}
