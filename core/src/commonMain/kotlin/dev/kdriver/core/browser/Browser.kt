package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Target
import dev.kdriver.cdp.domain.page
import dev.kdriver.cdp.domain.target
import dev.kdriver.core.connection.Connection
import dev.kdriver.core.tab.Tab
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.io.FileNotFoundException
import java.nio.file.Path
import java.util.logging.Logger

class Browser private constructor(
    val config: Config,
    val messageListeningScope: CoroutineScope = GlobalScope,
) {

    private val logger = Logger.getLogger(Browser::class.java.name)

    private var _process: Process? = null
    private var _processPid: Int? = null
    private var _http: HTTPApi? = null
    //private var _cookies: CookieJar? = null

    var connection: Connection? = null
        private set

    var info: ContraDict? = null
        private set

    private val _isUpdating = Mutex()

    val targets: MutableList<Connection> = mutableListOf()

    val websocketUrl: String
        get() = info?.webSocketDebuggerUrl ?: throw IllegalStateException("Browser not yet started. Call start() first")

    val mainTab: Tab?
        get() = targets.filterIsInstance<Tab>().maxByOrNull { it.type == "page" }

    val tabs: List<Tab>
        get() = targets.filterIsInstance<Tab>().filter { it.type == "page" }

    /*
    val cookies: CookieJar
        get() {
            if (_cookies == null) {
                _cookies = CookieJar(this)
            }
            return _cookies!!
        }
     */

    val stopped: Boolean
        get() = _process?.isAlive?.not() ?: true

    companion object {
        suspend fun create(
            config: Config? = null,
            userDataDir: Path? = null,
            headless: Boolean = false,
            browserExecutablePath: Path? = null,
            browserArgs: List<String>? = null,
            sandbox: Boolean = true,
            host: String? = null,
            port: Int? = null,
            messageListeningScope: CoroutineScope = GlobalScope,
            vararg kwargs: Pair<String, Any?>,
        ): Browser {
            val cfg = config ?: Config(
                userDataDir = userDataDir,
                headless = headless,
                browserExecutablePath = browserExecutablePath,
                browserArgs = browserArgs ?: emptyList(),
                sandbox = sandbox,
                host = host,
                port = port,
                // You can process kwargs here if Config supports it
            )

            val instance = Browser(cfg, messageListeningScope)
            instance.start()

            Runtime.getRuntime().addShutdownHook(Thread {
                runBlocking {
                    if (!instance.stopped) {
                        instance.stop()
                    }
                    instance.cleanupTemporaryProfile()
                }
            })

            return instance
        }
    }

    init {
        logger.fine("Session object initialized: $this")
    }

    suspend fun wait(timeSeconds: Double = 1.0): Browser {
        delay((timeSeconds * 1000).toLong())
        return this
    }

    suspend fun get(url: String = "about:blank", newTab: Boolean = false, newWindow: Boolean = false): Tab {
        val connection = connection ?: throw IllegalStateException("Browser not yet started. Call start() first")

        val future = CompletableDeferred<Target.TargetInfoChangedParameter>()

        val job = messageListeningScope.launch {
            connection.target.targetInfoChanged.collect {
                if (future.isCompleted) return@collect
                if (it.targetInfo.url != "about:blank" || (url == "about:blank" && it.targetInfo.url == "about:blank")) {
                    future.complete(it)
                }
            }
        }

        val connectionTab = if (newTab || newWindow) {
            val targetId = connection.target.createTarget(
                url = url,
                newWindow = newWindow,
                enableBeginFrameControl = true
            )
            targets.filterIsInstance<Tab>().first { it.type == "page" && it.targetId == targetId.targetId }.also {
                it.owner = this
            }
        } else {
            logger.info(targets.toString())
            targets.filterIsInstance<Tab>().first { it.type == "page" }.also {
                it.page.navigate(url)
                it.owner = this
            }
        }

        withTimeout(10_000) {
            future.await()
        }
        job.cancel()

        return connectionTab
    }

    suspend fun start(): Browser {
        if (_process != null || _processPid != null) {
            if (_process?.isAlive == false) {
                return create(config)
            }
            logger.warning("Ignored! Browser is already running.")
            return this
        }

        val connectExisting = config.host != null && config.port != null

        if (!connectExisting) {
            config.host = "127.0.0.1"
            config.port = freePort()
        }

        if (!connectExisting) {
            logger.info("BROWSER EXECUTABLE PATH: ${config.browserExecutablePath}")
            if (!config.browserExecutablePath.toFile().exists()) throw FileNotFoundException(
                """
                Could not determine browser executable.
                Make sure your browser is installed in the default location (path).
                Or specify browserExecutablePath parameter.
                """.trimIndent()
            )
        }

        // handle extensions if any
        config.extensions.takeIf { it.isNotEmpty() }?.let {
            config.addArgument("--load-extension=${it.joinToString(",")}")
        }

        val exe = config.browserExecutablePath
        val params = config().toMutableList()
        params.add("about:blank")

        logger.info("starting\n\texecutable :$exe\n\narguments:\n${params.joinToString("\n\t")}")

        if (!connectExisting) {
            _process = startProcess(exe.toAbsolutePath().toString(), params, Config.isPosix())
            _processPid = _process!!.pid().toInt()
        }

        logger.info("Browser process started with PID: $_processPid")
        _http = HTTPApi(config.host ?: "127.0.0.1", config.port ?: throw IllegalStateException("Port not set"))

        delay(config.browserConnectionTimeout)
        repeat(config.browserConnectionMaxTries) {
            if (testConnection()) return@repeat
            delay(config.browserConnectionTimeout)
        }
        logger.info("Connection to browser established")

        val info = info ?: run {
            logger.info("Browser info not initialized, reading error")
            withTimeoutOrNull(1000) {
                _process?.errorStream?.bufferedReader()?.use {
                    logger.info("Browser stderr: ${it.readText()}")
                }
            }
            stop()
            throw Exception(
                """
                Failed to connect to browser.
                Possible cause: running as root. Use no_sandbox = true in that case.
                """.trimIndent()
            )
        }

        logger.info("Connected to browser at ${info.webSocketDebuggerUrl}")
        val connection = Connection(info.webSocketDebuggerUrl) // , owner = this)
        this.connection = connection

        if (config.autoDiscoverTargets) {
            logger.info("Enabling autodiscover targets")
            messageListeningScope.launch {
                connection.target.targetInfoChanged.collect(::handleTargetUpdate)
            }
            messageListeningScope.launch {
                connection.target.targetCreated.collect(::handleTargetUpdate)
            }
            messageListeningScope.launch {
                connection.target.targetDestroyed.collect(::handleTargetUpdate)
            }
            messageListeningScope.launch {
                connection.target.targetCrashed.collect(::handleTargetUpdate)
            }

            connection.target.setDiscoverTargets(discover = true)
        }

        updateTargets()
        return this
    }

    private fun handleTargetUpdate(event: Any) {
        when (event) {
            is Target.TargetInfoChangedParameter -> {
                val targetInfo = event.targetInfo
                val currentTab = targets.first { it.targetId == targetInfo.targetId }
                val currentTarget = currentTab.targetInfo

                logger.fine("target #${targets.indexOf(currentTab)} has changed")
                /*
                if (logger.isLoggable(Level.FINE)) {
                    val changes = compareTargetInfo(currentTarget, targetInfo)
                    val changesString = changes.joinToString("\n") { (key, old, new) ->
                        "$key: $old => $new"
                    }
                    logger.fine("target #${targets.indexOf(currentTab)} has changed: \n$changesString")
                }
                */

                currentTab.targetInfo = targetInfo
            }

            is Target.TargetCreatedParameter -> {
                val targetInfo = event.targetInfo
                val wsUrl = buildString {
                    append("ws://${config.host}:${config.port}")
                    append("/devtools/${targetInfo.type ?: "page"}")
                    append("/${targetInfo.targetId}")
                }

                val newTarget = Tab(
                    wsUrl,
                    targetInfo = targetInfo,
                    owner = this
                )
                targets.add(newTarget)
                logger.fine("target #${targets.size - 1} created => $newTarget")
            }

            is Target.TargetDestroyedParameter -> {
                val currentTab = targets.first { it.targetId == event.targetId }
                logger.fine("target removed. id #${targets.indexOf(currentTab)} => $currentTab")
                targets.remove(currentTab)
            }

            is Target.TargetCrashedParameter -> {
                logger.warning("target crashed: ${event.targetId}")
            }
        }
    }

    suspend fun testConnection(): Boolean {
        val http = _http ?: throw IllegalStateException("HTTPApi not initialized")
        return try {
            info = http.get<ContraDict>("version")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            logger.fine("Could not start: ${e.message}")
            false
        }
    }

    suspend fun stop() {
        // implement stopping the browser process and cleaning up resources
        _process?.destroy()
        _process = null
        _processPid = null
        connection?.close()
        connection = null
    }

    suspend fun cleanupTemporaryProfile() {
        // implement cleanup of temporary profile files
    }

    private suspend fun getTargets(): List<Target.TargetInfo> {
        val connection = this.connection ?: error("Browser not yet started. Use browser.start() first.")
        val info = connection.cdp(isUpdate = true).target.getTargets()
        return info.targetInfos
    }

    suspend fun updateTargets() {
        getTargets().forEach { t ->
            val existingTab = this.targets.firstOrNull { it.targetInfo?.targetId == t.targetId }

            if (existingTab != null) {
                existingTab.targetInfo = t.copy() // ou update manuellement les champs si nécessaire
            } else {
                val wsUrl = "ws://${config.host}:${config.port}/devtools/page/${t.targetId}"
                val newConnection = Connection(
                    websocketUrl = wsUrl,
                    targetInfo = t,
                    //owner = this
                )
                this.targets.add(newConnection)
            }
        }

        yield() // equivalent to asyncio.sleep(0)
    }


}
