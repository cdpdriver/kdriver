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
    }

}
