package dev.kdriver.core.browser

import dev.kdriver.core.connection.Connection
import dev.kdriver.core.tab.Tab

/**
 * Represents a browser instance that can be controlled programmatically.
 *
 * This class provides methods to start the browser, navigate to URLs, manage tabs,
 * and handle browser events.
 *
 * You can create a new instance of this class using the [createBrowser] method:
 * ```kotlin
 * fun main() = runBlocking {
 *     val browser = createBrowser(this)
 *     // Use the browser instance to do things...
 *     browser.stop()
 * }
 * ```
 */
interface Browser {

    /**
     * The configuration for the browser instance.
     */
    val config: Config

    /**
     * The connection to the browser's WebSocket debugger.
     *
     * This connection is established when the browser is started and is used to communicate with the browser.
     * It allows sending commands and receiving events from the browser.
     */
    val connection: Connection?

    val info: ContraDict?

    /**
     * A list of targets currently open in the browser.
     *
     * If you want to tabs, consider using [tabs] property instead.
     */
    val targets: MutableList<Connection>

    /**
     * The WebSocket URL for the browser's debugger.
     *
     * This URL is used to connect to the browser's debugging protocol.
     * It is available after the browser has been started and the connection has been established.
     */
    val websocketUrl: String

    /**
     * The main tab of the browser.
     */
    val mainTab: Tab?

    /**
     * A list of all tabs in the browser.
     */
    val tabs: List<Tab>

    //val cookies: CookieJar

    /**
     * Checks if the browser process has stopped.
     */
    val stopped: Boolean

    /**
     * Waits for the specified time in seconds.
     *
     * This function suspends the coroutine for the given number of seconds.
     * It can be used to introduce delays in the execution flow, similar to `delay()`.
     *
     * @param timeout The number of milliseconds to wait. Defaults to 1 second.
     *
     * @return The current Browser instance for chaining.
     */
    suspend fun wait(timeout: Long = 1000): Browser

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
    suspend fun get(url: String = "about:blank", newTab: Boolean = false, newWindow: Boolean = false): Tab

    /**
     * Launches the actual browser
     */
    suspend fun start(): Browser

    /**
     * Tests the connection to the browser by sending a request to the "version" endpoint.
     *
     * This method checks if the browser is running and responds to requests.
     * If the connection is successful, it updates the `info` property with the version information.
     *
     * @return True if the connection was successful, false otherwise.
     */
    suspend fun testConnection(): Boolean

    /**
     * Stops the browser process and cleans up resources.
     *
     * This method will close the connection, cancel the coroutine scope, and destroy the process.
     * It should be called when the browser is no longer needed to free up resources.
     */
    suspend fun stop()

    suspend fun cleanupTemporaryProfile()

    /**
     * Updates the list of targets in the browser.
     *
     * This method retrieves the current targets from the browser and updates the internal list of targets.
     * It adds new targets if they do not already exist in the list, or updates existing targets with new information.
     *
     * @throws IllegalStateException if the browser has not been started yet.
     */
    suspend fun updateTargets()

}
