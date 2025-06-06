package dev.kdriver.core.browser

import dev.kdriver.core.utils.findChromeExecutable
import dev.kdriver.core.utils.isPosix
import dev.kdriver.core.utils.isRoot
import dev.kdriver.core.utils.tempProfileDir
import kotlinx.io.files.Path
import org.slf4j.LoggerFactory

class Config(
    userDataDir: Path? = null,
    val headless: Boolean = false,
    browserExecutablePath: Path? = null,
    browserArgs: List<String>? = null,
    sandbox: Boolean = true,
    val lang: String = "en-US",
    var host: String? = null,
    var port: Int? = null,
    val expert: Boolean = false,
    val browserConnectionTimeout: Long = 500,
    val browserConnectionMaxTries: Int = 60,
    val eventsBufferSize: Int = 64,
    val autoDiscoverTargets: Boolean = true,
) {

    private val logger = LoggerFactory.getLogger("Config")

    private var _userDataDir: Path? = null
    private var _customDataDir: Boolean = false

    private val _browserArgs: MutableList<String> = browserArgs?.toMutableList() ?: mutableListOf()
    internal val _extensions: MutableList<Path> = mutableListOf()

    val browserExecutablePath: Path? = browserExecutablePath ?: findChromeExecutable()

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
        if (headless) {
            args.add("--headless=new")
        }
        if (!sandbox) {
            args.add("--no-sandbox")
        }
        host?.let { args.add("--remote-debugging-host=$it") }
        port?.let { args.add("--remote-debugging-port=$it") }
        return args
    }

    fun addArgument(arg: String) {
        val forbiddenArgs = listOf("headless", "data-dir", "data_dir", "no-sandbox", "no_sandbox", "lang")
        if (forbiddenArgs.any { arg.contains(it, ignoreCase = true) }) {
            throw IllegalArgumentException("\"$arg\" not allowed. Please use one of the attributes of the Config object to set it")
        }
        _browserArgs.add(arg)
    }

}
