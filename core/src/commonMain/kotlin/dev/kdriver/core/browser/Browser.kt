package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Target
import dev.kdriver.cdp.domain.page
import dev.kdriver.cdp.domain.target
import dev.kdriver.core.browser.Browser.Companion.create
import dev.kdriver.core.connection.Connection
import dev.kdriver.core.exceptions.BrowserExecutableNotFoundException
import dev.kdriver.core.exceptions.FailedToConnectToBrowserException
import dev.kdriver.core.exceptions.NoBrowserExecutablePathException
import dev.kdriver.core.tab.Tab
import dev.kdriver.core.utils.*
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.files.Path

/**
 * Represents a browser instance that can be controlled programmatically.
 *
 * This class provides methods to start the browser, navigate to URLs, manage tabs,
 * and handle browser events.
 *
 * You can create a new instance of this class using the [create] method:
 * ```kotlin
 * fun main() = runBlocking {
 *     val browser = Browser.create(this)
 *     // Use the browser instance to do things...
 *     browser.stop()
 * }
 * ```
 */
class Browser private constructor(
    val coroutineScope: CoroutineScope,
    val config: Config = Config(),
) {

    private val logger = KtorSimpleLogger("Browser")
    private val updateTargetInfoMutex = Mutex()

    private var process: Process? = null
    private var http: HTTPApi? = null
    //private var _cookies: CookieJar? = null

    /**
     * The connection to the browser's WebSocket debugger.
     *
     * This connection is established when the browser is started and is used to communicate with the browser.
     * It allows sending commands and receiving events from the browser.
     */
    var connection: Connection? = null
        private set

    var info: ContraDict? = null
        private set

    /**
     * A list of targets currently open in the browser.
     *
     * If you want to tabs, consider using [tabs] property instead.
     */
    val targets: MutableList<Connection> = mutableListOf()

    /**
     * The WebSocket URL for the browser's debugger.
     *
     * This URL is used to connect to the browser's debugging protocol.
     * It is available after the browser has been started and the connection has been established.
     */
    val websocketUrl: String
        get() = info?.webSocketDebuggerUrl ?: throw IllegalStateException("Browser not yet started. Call start() first")

    /**
     * The main tab of the browser.
     */
    val mainTab: Tab?
        get() = targets.filterIsInstance<Tab>().maxByOrNull { it.type == "page" }

    /**
     * A list of all tabs in the browser.
     */
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

    /**
     * Checks if the browser process has stopped.
     */
    val stopped: Boolean
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
         * @param userDataDir Optional path to the user data directory. If not provided, a temporary profile will be created.
         * @param headless If true, the browser will run in headless mode. Defaults to false.
         * @param browserExecutablePath Optional path to the browser executable. If not provided, the default browser will be used.
         * @param browserArgs Optional list of additional arguments to pass to the browser executable.
         * @param sandbox If true, the browser will run in a sandboxed environment. Defaults to true.
         * @param lang The language to use for the browser.
         * @param host Optional host address for the browser connection. If not provided, defaults to "127.0.0.1".
         * @param port Optional port for the browser connection. If not provided, a free port will be assigned.
         *
         * @return A new instance of the Browser class.
         */
        suspend fun create(
            coroutineScope: CoroutineScope,
            config: Config? = null,
            userDataDir: Path? = null,
            headless: Boolean = false,
            browserExecutablePath: Path? = null,
            browserArgs: List<String>? = null,
            sandbox: Boolean = true,
            lang: String? = null,
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
                lang = lang,
                host = host,
                port = port,
            )

            val instance = Browser(browserScope, cfg)
            instance.start()

            addShutdownHook {
                if (!instance.stopped) instance.stop()
                instance.cleanupTemporaryProfile()
            }

            return instance
        }

    }

    /**
     * Waits for the specified time in seconds.
     *
     * This function suspends the coroutine for the given number of seconds.
     * It can be used to introduce delays in the execution flow, similar to `delay()`.
     *
     * @param timeSeconds The number of seconds to wait. Defaults to 1.0 second.
     *
     * @return The current Browser instance for chaining.
     */
    suspend fun wait(timeSeconds: Double = 1.0): Browser {
        delay((timeSeconds * 1000).toLong())
        return this
    }

    /**
     * Top level get. Uses the first tab to retrieve given url.
     *
     * Convenience function known from selenium.
     * This function handles waits/sleeps and detects when DOM events fired, so it's the safest way of navigating.
     *
     * @param url The URL to navigate to. Defaults to "about:blank".
     * @param newTab If true, opens the URL in a new tab. Defaults to false.
     * @param newWindow If true, opens the URL in a new window. Defaults to false.
     *
     * @return The Tab object representing the opened tab.
     */
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

    /**
     * Launches the actual browser
     */
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

    /**
     * This is an internal handler that updates the targets when chrome emits the corresponding event.
     *
     * It handles the following events:
     * - TargetInfoChangedParameter: Updates the target info of an existing tab.
     * - TargetCreatedParameter: Creates a new tab when a target is created.
     * - TargetDestroyedParameter: Removes a tab when a target is destroyed.
     * - TargetCrashedParameter: Logs a warning when a target crashes.
     *
     * @param event The event emitted by the Target domain.
     */
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

                val newTarget = Tab(
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

    /**
     * Tests the connection to the browser by sending a request to the "version" endpoint.
     *
     * This method checks if the browser is running and responds to requests.
     * If the connection is successful, it updates the `info` property with the version information.
     *
     * @return True if the connection was successful, false otherwise.
     */
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

    /**
     * Stops the browser process and cleans up resources.
     *
     * This method will close the connection, cancel the coroutine scope, and destroy the process.
     * It should be called when the browser is no longer needed to free up resources.
     */
    suspend fun stop() {
        logger.info("Stopping browser process with PID: ${process?.pid()}")
        process?.destroy()
        process = null
        connection?.close()
        connection = null
        coroutineScope.cancel()
        logger.info("Browser process stopped")
    }

    suspend fun cleanupTemporaryProfile() {
        // TODO: Implement cleanup of temporary profile files (nothing on JVM but maybe for other platforms)
    }

    private suspend fun getTargets(): List<Target.TargetInfo> {
        val connection = this.connection
            ?: throw IllegalStateException("Browser not yet started. Use browser.start() first.")
        val info = connection.target.getTargets()
        return info.targetInfos
    }

    /**
     * Updates the list of targets in the browser.
     *
     * This method retrieves the current targets from the browser and updates the internal list of targets.
     * It adds new targets if they do not already exist in the list, or updates existing targets with new information.
     *
     * @throws IllegalStateException if the browser has not been started yet.
     */
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
