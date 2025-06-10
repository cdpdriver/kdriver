package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Target
import dev.kdriver.cdp.domain.page
import dev.kdriver.cdp.domain.target
import dev.kdriver.core.connection.Connection
import dev.kdriver.core.tab.Tab
import dev.kdriver.core.utils.Process
import dev.kdriver.core.utils.exists
import dev.kdriver.core.utils.freePort
import dev.kdriver.core.utils.startProcess
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.io.files.FileNotFoundException
import kotlinx.io.files.Path

class Browser private constructor(
    val coroutineScope: CoroutineScope,
    val config: Config = Config(),
) {

    private val logger = KtorSimpleLogger("Browser")

    private var process: Process? = null
    private var http: HTTPApi? = null
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
        get() = process?.isAlive()?.not() ?: true

    companion object {
        suspend fun create(
            coroutineScope: CoroutineScope,
            config: Config? = null,
            userDataDir: Path? = null,
            headless: Boolean = false,
            browserExecutablePath: Path? = null,
            browserArgs: List<String>? = null,
            sandbox: Boolean = true,
            host: String? = null,
            port: Int? = null,
        ): Browser {
            val browserScope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())

            val cfg = config ?: Config(
                userDataDir = userDataDir,
                headless = headless,
                browserExecutablePath = browserExecutablePath,
                browserArgs = browserArgs ?: emptyList(),
                sandbox = sandbox,
                host = host,
                port = port,
            )

            val instance = Browser(browserScope, cfg)
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

    suspend fun wait(timeSeconds: Double = 1.0): Browser {
        delay((timeSeconds * 1000).toLong())
        return this
    }

    suspend fun get(url: String = "about:blank", newTab: Boolean = false, newWindow: Boolean = false): Tab {
        val connection = connection ?: throw IllegalStateException("Browser not yet started. Call start() first")

        val future = CompletableDeferred<Target.TargetInfoChangedParameter>()

        val job = coroutineScope.launch {
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
            targets.filterIsInstance<Tab>().first { it.type == "page" }.also {
                it.page.navigate(url)
                it.owner = this
            }
        }

        // Wait for the target info to be updated (but with a timeout so we don't block indefinitely nor crash)
        withTimeoutOrNull(10_000) {
            future.await()
        }
        job.cancel()

        return connectionTab
    }

    suspend fun start(): Browser {
        process?.let {
            if (process?.isAlive() == false) return create(coroutineScope, config)
            logger.warn("Ignored! Browser is already running.")
            return this
        }

        val connectExisting = config.host != null && config.port != null

        if (!connectExisting) {
            config.host = "127.0.0.1"
            config.port = freePort()
        }

        // handle extensions if any
        config.extensions.takeIf { it.isNotEmpty() }?.let {
            config.addArgument("--load-extension=${it.joinToString(",")}")
        }

        if (!connectExisting) {
            val exe = config.browserExecutablePath ?: throw IllegalStateException(
                """
                    Browser executable path is not set and findChromeExecutable is not supported on this platform.
                    Please specify browserExecutablePath parameter.
                    """.trimIndent()
            )

            logger.info("BROWSER EXECUTABLE PATH: $exe")
            if (!exists(exe)) throw FileNotFoundException(
                """
                Could not determine browser executable.
                Make sure your browser is installed in the default location (path).
                Or specify browserExecutablePath parameter.
                """.trimIndent()
            )

            val params = config().toMutableList()
            params.add("about:blank")

            logger.info("starting\n\texecutable :$exe\n\narguments:\n${params.joinToString("\n\t")}")

            process = startProcess(exe, params)
        }

        logger.info("Browser process started with PID: ${process?.pid()}")
        http = HTTPApi(config.host ?: "127.0.0.1", config.port ?: throw IllegalStateException("Port not set"))

        delay(config.browserConnectionTimeout)
        repeat(config.browserConnectionMaxTries) {
            if (testConnection()) return@repeat
            delay(config.browserConnectionTimeout)
        }
        logger.info("Connection to browser established")

        val info = info ?: run {
            logger.info("Browser info not initialized, reading error")
            /*
            // This seems to block indefinitely on CI, so inspection is required
            withTimeoutOrNull(1000) {
                _process?.errorStream?.bufferedReader()?.use {
                    logger.info("Browser stderr: ${it.readText()}")
                }
            }
             */
            stop()
            throw Exception(
                """
                Failed to connect to browser.
                Possible cause: running as root. Use no_sandbox = true in that case.
                """.trimIndent()
            )
        }

        logger.info("Connected to browser at ${info.webSocketDebuggerUrl}")
        val connection = Connection(
            websocketUrl = info.webSocketDebuggerUrl,
            messageListeningScope = coroutineScope,
            eventsBufferSize = config.eventsBufferSize,
            owner = this
        )
        this.connection = connection

        if (config.autoDiscoverTargets) {
            logger.info("Enabling autodiscover targets")
            coroutineScope.launch {
                connection.target.targetInfoChanged.collect(::handleTargetUpdate)
            }
            coroutineScope.launch {
                connection.target.targetCreated.collect(::handleTargetUpdate)
            }
            coroutineScope.launch {
                connection.target.targetDestroyed.collect(::handleTargetUpdate)
            }
            coroutineScope.launch {
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

                logger.debug("target #${targets.indexOf(currentTab)} has changed")
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
                    messageListeningScope = coroutineScope,
                    eventsBufferSize = config.eventsBufferSize,
                    targetInfo = targetInfo,
                    owner = this
                )
                targets.add(newTarget)
                logger.debug("target #{} created => {}", targets.size - 1, newTarget)
            }

            is Target.TargetDestroyedParameter -> {
                val currentTab = targets.first { it.targetId == event.targetId }
                logger.debug("target removed. id #{} => {}", targets.indexOf(currentTab), currentTab)
                targets.remove(currentTab)
            }

            is Target.TargetCrashedParameter -> {
                logger.warn("target crashed: ${event.targetId}")
            }
        }
    }

    suspend fun testConnection(): Boolean {
        val http = http ?: throw IllegalStateException("HTTPApi not initialized")
        return try {
            info = http.get<ContraDict>("version")
            true
        } catch (e: Exception) {
            logger.debug("Could not start: ${e.message}")
            false
        }
    }

    suspend fun stop() {
        // implement stopping the browser process and cleaning up resources
        logger.info("Stopping browser process with PID: ${process?.pid()}")
        process?.destroy()
        process = null
        connection?.close()
        connection = null
        coroutineScope.cancel()
        logger.info("Browser process stopped")
    }

    suspend fun cleanupTemporaryProfile() {
        // implement cleanup of temporary profile files
    }

    private suspend fun getTargets(): List<Target.TargetInfo> {
        val connection = this.connection ?: error("Browser not yet started. Use browser.start() first.")
        val info = connection.target.getTargets()
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
                    messageListeningScope = coroutineScope,
                    eventsBufferSize = config.eventsBufferSize,
                    targetInfo = t,
                    owner = this
                )
                this.targets.add(newConnection)
            }
        }

        yield() // equivalent to asyncio.sleep(0)
    }


}
