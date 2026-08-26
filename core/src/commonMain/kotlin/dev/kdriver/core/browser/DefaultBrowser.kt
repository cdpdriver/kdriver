package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Target
import dev.kdriver.cdp.domain.browser
import dev.kdriver.cdp.domain.page
import dev.kdriver.cdp.domain.target
import dev.kdriver.core.connection.Connection
import dev.kdriver.core.connection.DefaultConnection
import dev.kdriver.core.connection.OwnedConnection
import dev.kdriver.core.exceptions.BrowserExecutableNotFoundException
import dev.kdriver.core.exceptions.FailedToConnectToBrowserException
import dev.kdriver.core.tab.DefaultTab
import dev.kdriver.core.tab.Tab
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.seconds

/**
 * Default implementation of the [Browser] interface.
 */
open class DefaultBrowser(
    val coroutineScope: CoroutineScope,
    config: Config,
) : Browser {

    /**
     * This browser's own copy of the configuration it was created with.
     *
     * [start] resolves runtime state onto it — it assigns [Config.host] / [Config.port] and appends
     * launch arguments — so it must not be the caller's instance. Mutating a shared config would
     * make any later browser built from it take the `connectExisting` branch of [start]: it would
     * skip launching a browser process entirely and try to reach the previous port, which is dead.
     * Copying here makes reusing (and retrying with) the same [Config] safe by construction.
     */
    override val config: Config = config.copy()

    private val logger = KtorSimpleLogger("Browser")
    private val updateTargetInfoMutex = Mutex()

    private var process: Process? = null
    private var http: HTTPApi? = null
    private var _cookies: CookieJar? = null

    override var connection: Connection? = null

    override var info: ContraDict? = null

    /** Last error from [testConnection], surfaced if the browser never opens its debug port. */
    private var lastConnectionError: Exception? = null

    // The canonical registry: mutated only while holding [updateTargetInfoMutex]. After each
    // mutation an immutable copy is published to [targetsSnapshot] so the non-suspend getters
    // below can read a consistent view without the lock (ISSUE-5).
    private val mutableTargets = mutableListOf<Connection>()

    @Volatile
    private var targetsSnapshot: List<Connection> = emptyList()

    override val targets: List<Connection>
        get() = targetsSnapshot

    override val websocketUrl: String
        get() = info?.webSocketDebuggerUrl ?: error("Browser not yet started. Call start() first")

    override val mainTab: Tab?
        get() = targetsSnapshot.filterIsInstance<Tab>().maxByOrNull { it.type == "page" }

    override val tabs: List<Tab>
        get() = targetsSnapshot.filterIsInstance<Tab>().filter { it.type == "page" }

    override val cookies: CookieJar
        get() = _cookies ?: DefaultCookieJar(this).also { _cookies = it }

    override val pid: Long?
        get() = process?.pid()

    override val stopped: Boolean
        get() = process?.isAlive()?.not() ?: true

    companion object {

        private const val TARGET_WAIT_TIMEOUT_MS = 10_000L
        private const val TARGET_POLL_INTERVAL_MS = 50L

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

    protected open fun createConnection(websocketUrl: String, targetInfo: Target.TargetInfo? = null): Connection =
        DefaultConnection(websocketUrl, coroutineScope, targetInfo, this)

    protected open fun createTab(websocketUrl: String, targetInfo: Target.TargetInfo): Tab =
        DefaultTab(websocketUrl, coroutineScope, targetInfo, this)

    /**
     * Adds the target if unknown — page targets become [Tab]s, everything else a plain connection —
     * or updates the [Target.TargetInfo] of the existing entry. Dedupes by `targetId` so a target
     * discovered by both [updateTargets] and a `targetCreated` event can't appear twice, and so page
     * targets are consistently [Tab]s (ISSUE-6).
     *
     * Must be called while holding [updateTargetInfoMutex].
     */
    private fun upsertTarget(targetInfo: Target.TargetInfo) {
        val existing = mutableTargets.firstOrNull { it.targetId == targetInfo.targetId }
        if (existing != null) {
            existing.targetInfo = targetInfo
        } else {
            val wsUrl = "ws://${config.host}:${config.port}/devtools/${targetInfo.type}/${targetInfo.targetId}"
            val created = if (targetInfo.type == "page") createTab(wsUrl, targetInfo)
            else createConnection(wsUrl, targetInfo)
            mutableTargets.add(created)
            logger.debug("target added => $created")
        }
        publishTargetsSnapshot()
    }

    /** Publishes an immutable copy of [mutableTargets]. Must be called while holding the mutex. */
    private fun publishTargetsSnapshot() {
        targetsSnapshot = mutableTargets.toList()
    }

    private fun findPageTab(predicate: (Tab) -> Boolean): Tab? =
        targetsSnapshot.filterIsInstance<Tab>().firstOrNull { it.type == "page" && predicate(it) }

    /**
     * Waits for a page [Tab] matching [predicate] to appear in the registry, instead of assuming it
     * is already present. The matching `Tab` is only registered once the asynchronous `targetCreated`
     * event is processed, which races `createTarget()` returning (ISSUE-8).
     */
    private suspend fun awaitPageTab(timeoutMillis: Long, predicate: (Tab) -> Boolean): Tab =
        withTimeoutOrNull(timeoutMillis) {
            var found = findPageTab(predicate)
            while (found == null) {
                delay(TARGET_POLL_INTERVAL_MS)
                found = findPageTab(predicate)
            }
            found
        } ?: throw IllegalStateException("Timed out waiting for a page target after ${timeoutMillis}ms")

    /**
     * Best-effort wait, during [start], for the initial page [Tab] to be registered, so `mainTab`
     * (and tests built on `browser.mainTab`) are ready when `start()` returns instead of racing the
     * asynchronous target discovery (ISSUE-6 residual). Re-runs [updateTargets] each poll so it
     * works whether or not auto-discovery events are enabled. Does NOT throw on timeout — a browser
     * may legitimately have no page yet, in which case `mainTab` stays null as before.
     */
    internal suspend fun awaitInitialPageTab() {
        withTimeoutOrNull(TARGET_WAIT_TIMEOUT_MS) {
            while (findPageTab { true } == null) {
                delay(TARGET_POLL_INTERVAL_MS)
                updateTargets()
            }
        }
    }

    override suspend fun wait(timeout: Long): Browser {
        delay(timeout)
        return this
    }

    override suspend fun get(url: String, newTab: Boolean, newWindow: Boolean): Tab {
        val connection = connection ?: error("Browser not yet started. Call start() first")

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
            awaitPageTab(TARGET_WAIT_TIMEOUT_MS) { it.targetId == targetId.targetId }.also {
                if (it is OwnedConnection) it.owner = this
            }
        } else {
            awaitPageTab(TARGET_WAIT_TIMEOUT_MS) { true }.also {
                it.page.navigate(url)
                if (it is OwnedConnection) it.owner = this
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
            val exe = config.browserExecutablePath

            logger.info("BROWSER EXECUTABLE PATH: $exe")
            if (!exists(exe)) throw BrowserExecutableNotFoundException()

            val params = config().toMutableList()
            params.add("about:blank")

            logger.info("starting\n\texecutable :$exe\n\narguments:\n${params.joinToString("\n\t")}")

            process = startProcess(exe, params)
        }

        logger.info("Browser process started with PID: ${process?.pid()}")
        http = HTTPApi(config.host ?: "127.0.0.1", config.port ?: error("Port not set"))

        delay(config.browserConnectionTimeout)
        repeat(config.browserConnectionMaxTries) {
            if (testConnection()) return@repeat
            delay(config.browserConnectionTimeout)
        }
        logger.info("Connection to browser established")

        val info = info ?: run {
            // Say what actually failed. Without this the only visible symptom is a 30s wait and a
            // generic exception, which cannot distinguish "nothing is listening on that port" (the
            // browser never opened it) from "something answers but not what we expect" (a stale or
            // foreign process holds it) — two very different causes.
            val waitedMs = config.browserConnectionTimeout * (config.browserConnectionMaxTries + 1)
            val stderr = process?.readStderrSnapshot()
            logger.error(
                "Browser never opened its debug port on ${config.host}:${config.port} after ${waitedMs}ms " +
                    "(pid=${process?.pid()}, alive=${process?.isAlive()}). " +
                    "Last connection error: " +
                    (lastConnectionError?.let { "${it::class.simpleName}: ${it.message}" } ?: "none") +
                    ". Browser stderr: " + (stderr?.trim()?.takeIf { it.isNotEmpty() } ?: "<none>")
            )
            stop()
            throw FailedToConnectToBrowserException()
        }

        logger.info("Connected to browser at ${info.webSocketDebuggerUrl}")
        val connection = createConnection(info.webSocketDebuggerUrl)
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
            // The event collectors above survive a reconnect (they read the connection's shared flow),
            // but the new CDP session won't emit target events until discovery is re-enabled on it.
            connection.registerReconnectRestore("targetDiscovery") {
                connection.target.setDiscoverTargets(discover = true)
            }
        }

        updateTargets()
        // Ensure the initial page target is registered before returning, so mainTab is ready
        // (closes the residual ISSUE-6 startup race that flakes browser.mainTab ?: error(...)).
        awaitInitialPageTab()
        return this
    }

    private suspend fun handleTargetUpdate(event: Any) = updateTargetInfoMutex.withLock {
        when (event) {
            // Both create and info-changed go through the same dedupe-by-targetId upsert, so an
            // info-changed for an as-yet-unregistered target registers it rather than being dropped.
            is Target.TargetInfoChangedParameter -> upsertTarget(event.targetInfo)
            is Target.TargetCreatedParameter -> upsertTarget(event.targetInfo)

            is Target.TargetDestroyedParameter -> {
                val current = mutableTargets.firstOrNull { it.targetId == event.targetId } ?: run {
                    logger.warn("TargetDestroyedParameter: Target with ID ${event.targetId} not found in current targets.")
                    return@withLock
                }
                logger.debug("target removed => $current")
                mutableTargets.remove(current)
                publishTargetsSnapshot()
            }

            is Target.TargetCrashedParameter -> {
                logger.warn("target crashed: ${event.targetId}")
            }
        }
    }

    override suspend fun testConnection(): Boolean {
        val http = http ?: error("HTTPApi not initialized")
        return try {
            info = http.get<ContraDict>("version")
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            lastConnectionError = e
            logger.debug("Could not start: ${e.message}")
            false
        }
    }

    override suspend fun stop() {
        logger.info("Stopping browser process with PID: ${process?.pid()}")
        logger.debug("Closing browser gracefully...")
        withTimeoutOrNull(5.seconds) { connection?.browser?.close() }
        logger.debug("Killing browser process...")
        // Wait for it to actually be gone, don't just ask. destroy() returns immediately and does not
        // reach the browser's child processes, which keep open handles on the profile directory —
        // a browser started on the same --user-data-dir right after would hang before opening its
        // debug port, and the start would time out for no visible reason.
        process?.let {
            if (!it.destroyAndAwaitExit()) logger.warn("Browser process ${it.pid()} still alive after kill")
        }
        process = null
        logger.debug("Closing connection...")
        connection?.close()
        connection = null
        logger.debug("Closing HTTP API client...")
        http?.close()
        http = null
        logger.debug("Canceling coroutine scope...")
        coroutineScope.cancel()
        logger.info("Browser process stopped")
    }

    override suspend fun cleanupTemporaryProfile() {
        // TODO: Implement cleanup of temporary profile files (nothing on JVM but maybe for other platforms)
    }

    private suspend fun getTargets(): List<Target.TargetInfo> {
        val connection = this.connection
            ?: error("Browser not yet started. Use browser.start() first.")
        val info = connection.target.getTargets()
        return info.targetInfos
    }

    override suspend fun updateTargets() {
        // Fetch outside the lock (it's a network round-trip), then apply atomically (ISSUE-5).
        val targetInfos = getTargets()
        updateTargetInfoMutex.withLock {
            targetInfos.forEach { upsertTarget(it) }
        }

        yield() // equivalent to asyncio.sleep(0)
    }

}
