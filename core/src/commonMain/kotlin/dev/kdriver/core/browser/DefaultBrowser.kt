package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Target
import dev.kdriver.cdp.domain.page
import dev.kdriver.cdp.domain.target
import dev.kdriver.core.connection.Connection
import dev.kdriver.core.connection.DefaultConnection
import dev.kdriver.core.exceptions.BrowserExecutableNotFoundException
import dev.kdriver.core.exceptions.FailedToConnectToBrowserException
import dev.kdriver.core.exceptions.NoBrowserExecutablePathException
import dev.kdriver.core.tab.DefaultTab
import dev.kdriver.core.tab.Tab
import dev.kdriver.core.utils.*
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Default implementation of the [Browser] interface.
 */
open class DefaultBrowser(
    val coroutineScope: CoroutineScope,
    override val config: Config,
) : Browser {

    private val logger = KtorSimpleLogger("Browser")
    private val updateTargetInfoMutex = Mutex()

    private var process: Process? = null
    private var http: HTTPApi? = null
    //private var _cookies: CookieJar? = null

    override var connection: Connection? = null

    override var info: ContraDict? = null

    override val targets: MutableList<Connection> = mutableListOf()

    override val websocketUrl: String
        get() = info?.webSocketDebuggerUrl ?: throw IllegalStateException("Browser not yet started. Call start() first")

    override val mainTab: Tab?
        get() = targets.filterIsInstance<Tab>().maxByOrNull { it.type == "page" }

    override val tabs: List<Tab>
        get() = targets.filterIsInstance<Tab>().filter { it.type == "page" }

    /*
    override val cookies: CookieJar
        get() {
            if (_cookies == null) {
                _cookies = CookieJar(this)
            }
            return _cookies!!
        }
     */

    override val stopped: Boolean
        get() = process?.isAlive()?.not() ?: true

    companion object {

        /**
         * The entry point for creating a new Browser instance.
         *
         * This function initializes a new Browser instance with the provided configuration.
         * It sets up the necessary parameters such as user data directory, headless mode, browser executable path, and more.
         * It also handles the creation of a coroutine scope for the browser instance and sets up a shutdown hook to clean up resources when the application exits.
         *
         * @param coroutineScope The parent CoroutineScope, in which the browser will run.
         * @param config Optional configuration for the browser. If not provided, a default configuration will be used.
         *
         * @return A new instance of the Browser class.
         */
        suspend fun create(coroutineScope: CoroutineScope, config: Config): Browser {
            val browserScope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())

            val instance = DefaultBrowser(browserScope, config)
            instance.start()

            addShutdownHook {
                if (!instance.stopped) instance.stop()
                instance.cleanupTemporaryProfile()
            }

            return instance
        }

    }

    override suspend fun wait(timeout: Long): Browser {
        delay(timeout)
        return this
    }

    override suspend fun get(url: String, newTab: Boolean, newWindow: Boolean): Tab {
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
                if (it is DefaultConnection) it.owner = this
            }
        } else {
            targets.filterIsInstance<Tab>().first { it.type == "page" }.also {
                it.page.navigate(url)
                if (it is DefaultConnection) it.owner = this
            }
        }

        // Wait for the target info to be updated (but with a timeout so we don't block indefinitely nor crash)
        withTimeoutOrNull(10_000) {
            future.await()
        }
        job.cancel()

        return connectionTab
    }

    override suspend fun start(): Browser {
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

        config.lang?.let {
            config.addArgument("--lang=$it")
        }

        if (!connectExisting) {
            val exe = config.browserExecutablePath ?: throw NoBrowserExecutablePathException()

            logger.info("BROWSER EXECUTABLE PATH: $exe")
            if (!exists(exe)) throw BrowserExecutableNotFoundException()

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
            throw FailedToConnectToBrowserException()
        }

        logger.info("Connected to browser at ${info.webSocketDebuggerUrl}")
        val connection = DefaultConnection(
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

    private suspend fun handleTargetUpdate(event: Any) = updateTargetInfoMutex.withLock {
        when (event) {
            is Target.TargetInfoChangedParameter -> {
                val targetInfo = event.targetInfo
                val currentTab = targets.firstOrNull { it.targetId == targetInfo.targetId } ?: run {
                    logger.warn("TargetInfoChangedParameter: Target with ID ${targetInfo.targetId} not found in current targets.")
                    return
                }
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
                val wsUrl = "ws://${config.host}:${config.port}/devtools/${targetInfo.type}/${targetInfo.targetId}"

                val newTarget = DefaultTab(
                    wsUrl,
                    messageListeningScope = coroutineScope,
                    eventsBufferSize = config.eventsBufferSize,
                    targetInfo = targetInfo,
                    owner = this
                )
                targets.add(newTarget)
                logger.debug("target ${targets.size - 1} created => $newTarget")
            }

            is Target.TargetDestroyedParameter -> {
                val currentTab = targets.firstOrNull { it.targetId == event.targetId } ?: run {
                    logger.warn("TargetDestroyedParameter: Target with ID ${event.targetId} not found in current targets.")
                    return
                }
                logger.debug("target removed. id ${targets.indexOf(currentTab)} => $currentTab")
                targets.remove(currentTab)
            }

            is Target.TargetCrashedParameter -> {
                logger.warn("target crashed: ${event.targetId}")
            }
        }
    }

    override suspend fun testConnection(): Boolean {
        val http = http ?: throw IllegalStateException("HTTPApi not initialized")
        return try {
            info = http.get<ContraDict>("version")
            true
        } catch (e: Exception) {
            logger.debug("Could not start: ${e.message}")
            false
        }
    }

    override suspend fun stop() {
        logger.info("Stopping browser process with PID: ${process?.pid()}")
        process?.destroy()
        process = null
        connection?.close()
        connection = null
        coroutineScope.cancel()
        logger.info("Browser process stopped")
    }

    override suspend fun cleanupTemporaryProfile() {
        // TODO: Implement cleanup of temporary profile files (nothing on JVM but maybe for other platforms)
    }

    private suspend fun getTargets(): List<Target.TargetInfo> {
        val connection = this.connection
            ?: throw IllegalStateException("Browser not yet started. Use browser.start() first.")
        val info = connection.target.getTargets()
        return info.targetInfos
    }

    override suspend fun updateTargets() {
        getTargets().forEach { t ->
            val existingTab = this.targets.firstOrNull { it.targetInfo?.targetId == t.targetId }

            if (existingTab != null) {
                existingTab.targetInfo = t.copy() // ou update manuellement les champs si nécessaire
            } else {
                val wsUrl = "ws://${config.host}:${config.port}/devtools/page/${t.targetId}"
                val newConnection = DefaultConnection(
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
