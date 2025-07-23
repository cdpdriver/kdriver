package dev.kdriver.core.browser

import kotlinx.coroutines.CoroutineScope
import kotlinx.io.files.Path

/**
 * The entry point for creating a new Browser instance.
 *
 * This function initializes a new Browser instance with the provided configuration.
 * It sets up the necessary parameters such as user data directory, headless mode, browser executable path, and more.
 * It also handles the creation of a coroutine scope for the browser instance and sets up a shutdown hook to clean up resources when the application exits.
 *
 * @param coroutineScope The parent CoroutineScope, in which the browser will run.
 * @param userDataDir Optional path to the user data directory. If not provided, a temporary profile will be created.
 * @param headless If true, the browser will run in headless mode. Defaults to false.
 * @param userAgent Optional user agent string to use for the browser. If not provided, the default user agent will be used.
 * @param browserExecutablePath Optional path to the browser executable. If not provided, the default browser will be used.
 * @param browserArgs Optional list of additional arguments to pass to the browser executable.
 * @param sandbox If true, the browser will run in a sandboxed environment. Defaults to true.
 * @param lang The language to use for the browser.
 * @param host Optional host address for the browser connection. If not provided, defaults to "127.0.0.1".
 * @param port Optional port for the browser connection. If not provided, a free port will be assigned.
 *
 * @return A new instance of the Browser class.
 */
suspend fun createBrowser(
    coroutineScope: CoroutineScope,
    userDataDir: Path? = null,
    headless: Boolean = false,
    userAgent: String? = null,
    browserExecutablePath: Path? = null,
    browserArgs: List<String>? = null,
    sandbox: Boolean = true,
    lang: String? = null,
    host: String? = null,
    port: Int? = null,
): Browser = createBrowser(
    coroutineScope = coroutineScope,
    config = Config(
        userDataDir = userDataDir,
        headless = headless,
        userAgent = userAgent,
        browserExecutablePath = browserExecutablePath,
        browserArgs = browserArgs ?: emptyList(),
        sandbox = sandbox,
        lang = lang,
        host = host,
        port = port,
    )
)

/**
 * The entry point for creating a new Browser instance.
 *
 * This function initializes a new Browser instance with the provided configuration.
 * It sets up the necessary parameters such as user data directory, headless mode, browser executable path, and more.
 * It also handles the creation of a coroutine scope for the browser instance and sets up a shutdown hook to clean up resources when the application exits.
 *
 * @param coroutineScope The parent CoroutineScope, in which the browser will run.
 * @param config Configuration for the browser.
 *
 * @return A new instance of the Browser class.
 */
suspend fun createBrowser(
    coroutineScope: CoroutineScope,
    config: Config,
): Browser = DefaultBrowser.create(
    coroutineScope = coroutineScope,
    config = config
)

/**
 * The entry point for creating a new Browser instance using DSL configuration.
 *
 * This convenience function allows direct inline configuration using the DSL syntax.
 *
 * @param coroutineScope The parent CoroutineScope, in which the browser will run.
 * @param block The configuration block to apply to the ConfigBuilder.
 * @return A new instance of the Browser class.
 */

suspend fun createBrowser(
    coroutineScope: CoroutineScope,
    block: ConfigBuilder.() -> Unit
): Browser = createBrowser(coroutineScope, config(block))



/**
 * Creates a Config instance using Kotlin DSL syntax.
 *
 * @param block The configuration block to apply to the ConfigBuilder.
 * @return A new Config instance with the specified configuration.
 */
fun config(block: ConfigBuilder.() -> Unit): Config {
    return ConfigBuilder().apply(block).build()
}

