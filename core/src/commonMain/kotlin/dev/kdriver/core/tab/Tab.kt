package dev.kdriver.core.tab

import dev.kdriver.cdp.domain.Fetch
import dev.kdriver.cdp.domain.Input
import dev.kdriver.cdp.domain.Network
import dev.kdriver.core.connection.Connection
import dev.kdriver.core.dom.Element
import dev.kdriver.core.dom.NodeOrElement
import dev.kdriver.core.exceptions.TimeoutWaitingForElementException
import dev.kdriver.core.exceptions.TimeoutWaitingForReadyStateException
import dev.kdriver.core.network.FetchInterception
import dev.kdriver.core.network.RequestExpectation
import kotlinx.io.files.Path
import kotlinx.serialization.json.JsonElement
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Represents a browser tab, which is a connection to a specific target in the browser.
 *
 * This class provides methods to interact with the tab, such as navigating to URLs,
 * managing history, evaluating JavaScript expressions, and manipulating the DOM.
 *
 * You can create a new instance of this class using the [dev.kdriver.core.browser.Browser.get] method:
 * ```kotlin
 * fun main() = runBlocking {
 *     val browser = createBrowser(this)
 *     val tab = browser.get("https://example.com")
 *     // Use the tab instance to do things...
 *     browser.stop()
 * }
 * ```
 */
interface Tab : Connection {

    /**
     * Navigate to a specified URL in the tab.
     *
     * @param url The URL to navigate to. Defaults to "about:blank".
     * @param newTab If true, opens the URL in a new tab instead of the current one.
     * @param newWindow If true, opens the URL in a new window instead of the current one.
     *
     * @return The current tab instance after navigation.
     */
    suspend fun get(
        url: String = "about:blank",
        newTab: Boolean = false,
        newWindow: Boolean = false,
    ): Tab

    /**
     * Navigate back in the tab's history.
     */
    suspend fun back()

    /**
     * Navigate forward in the tab's history.
     */
    suspend fun forward()

    /**
     * Reload the current page in the tab.
     *
     * @param ignoreCache If true, reloads the page without using the cache.
     * @param scriptToEvaluateOnLoad Optional JavaScript to evaluate after the page loads.
     */
    suspend fun reload(
        ignoreCache: Boolean = true,
        scriptToEvaluateOnLoad: String? = null,
    )

    /**
     * Evaluates a JavaScript expression in the context of the tab.
     *
     * @param expression The JavaScript expression to evaluate.
     * @param awaitPromise If true, waits for any promises to resolve before returning the result.
     *
     * @return The result of the evaluation, deserialized to type T, or null if no result is returned.
     */
    suspend fun rawEvaluate(
        expression: String,
        awaitPromise: Boolean = false,
    ): JsonElement?

    /**
     * Sets the user agent for the tab.
     *
     * This method allows you to override the user agent string, accept language, and platform
     * for the tab's network requests. If no user agent is provided, it will read the existing user agent
     * from the navigator object.
     *
     * @param userAgent The user agent string to set. If null, the existing user agent will be used.
     * @param acceptLanguage The accept language string to set. If null, it will not override the existing value.
     * @param platform The platform string to set. If null, it will not override the existing value.
     */
    suspend fun setUserAgent(
        userAgent: String? = null,
        acceptLanguage: String? = null,
        platform: String? = null,
    )

    /**
     * Gets the current window information for the tab.
     *
     * @return A [dev.kdriver.cdp.domain.Browser.GetWindowForTargetReturn] object containing the window ID and bounds.
     */
    suspend fun getWindow(): dev.kdriver.cdp.domain.Browser.GetWindowForTargetReturn

    /**
     * Gets the content of the tab as a string.
     *
     * This method retrieves the outer HTML of the document in the tab, which includes the entire HTML structure.
     *
     * @return The outer HTML of the document as a string.
     */
    suspend fun getContent(): String

    /**
     * Activates the tab, bringing it to the foreground.
     *
     * This method uses the target's ID to activate the target in the browser.
     *
     * @throws IllegalArgumentException if the target is null.
     */
    suspend fun activate()

    /**
     * Brings the tab to the front, activating it.
     *
     * This method is a convenience method that calls [activate].
     */
    suspend fun bringToFront()

    /**
     * Maximizes the tab's window.
     *
     * This method sets the window state to "maximize", which typically maximizes the browser window.
     */
    suspend fun maximize()

    /**
     * Minimizes the tab's window.
     *
     * This method sets the window state to "minimize", which typically minimizes the browser window.
     */
    suspend fun minimize()

    /**
     * Sets the tab's window to fullscreen mode.
     *
     * This method sets the window state to "fullscreen", which typically makes the browser window occupy the entire screen.
     */
    suspend fun fullscreen()

    /**
     * Restores the tab's window to its normal state.
     *
     * This method sets the window state to "normal", which typically restores the browser window to its default size and position.
     */
    suspend fun medimize()

    /**
     * Sets the tab's window state, including position and size.
     *
     * This method allows you to set the window's position (left, top), size (width, height), and state (normal, minimized, maximized, fullscreen).
     *
     * @param left The left position of the window. Defaults to 0.
     * @param top The top position of the window. Defaults to 0.
     * @param width The width of the window. Defaults to 1280.
     * @param height The height of the window. Defaults to 720.
     * @param state The state of the window. Defaults to "normal". Valid values are "minimized", "maximized", "fullscreen", and "normal".
     */
    suspend fun setWindowState(
        left: Int = 0,
        top: Int = 0,
        width: Int = 1280,
        height: Int = 720,
        state: String = "normal",
    )

    /**
     * Scrolls the tab down by a specified amount.
     *
     * This method simulates a scroll gesture downwards by a percentage of the viewport height.
     *
     * @param amount The percentage of the viewport height to scroll down. Defaults to 25%. 25 is a quarter of page, 50 half, and 1000 is 10x the page
     * @param speed Swipe speed in pixels per second (default: 800).
     */
    suspend fun scrollDown(amount: Int = 25, speed: Int = 800)

    /**
     * Scrolls the tab up by a specified amount.
     *
     * This method simulates a scroll gesture upwards by a percentage of the viewport height.
     *
     * @param amount The percentage of the viewport height to scroll up. Defaults to 25%. 25 is a quarter of page, 50 half, and 1000 is 10x the page
     * @param speed Swipe speed in pixels per second (default: 800).
     */
    suspend fun scrollUp(amount: Int = 25, speed: Int = 800)

    /**
     * Waits for the document's ready state to reach a specified state.
     *
     * This method continuously checks the document's ready state until it matches the specified state or a timeout occurs.
     *
     * @param until The desired ready state to wait for. Can be LOADING, INTERACTIVE, or COMPLETE. Defaults to INTERACTIVE.
     *
     * @throws TimeoutWaitingForReadyStateException if the timeout is reached before the ready state matches.
     *
     * @return True if the ready state matches the specified state before the timeout, false otherwise.
     */
    suspend fun waitForReadyState(
        until: ReadyState = ReadyState.INTERACTIVE,
        timeout: Long = 10_000,
    ): Boolean

    /**
     * Finds a single element by its text content, optionally waiting for it to appear.
     *
     * @param text The text to search for. Note: script contents are also considered text.
     * @param bestMatch If true (default), returns the element with the most comparable string length.
     *                  This helps when searching for common terms (e\.g\., "login") to get the most relevant element,
     *                  such as a login button, instead of unrelated elements containing the text.
     *                  If false, returns the first match found, which is faster.
     * @param returnEnclosingElement Since the function often returns text nodes (children of elements like "span", "p", etc\.),
     *                  this flag (default: true) returns the containing element instead of the text node itself.
     *                  Set to false if you want the text node or for cases like elements with "placeholder=" property.
     *                  If the found node is not a text node but a regular element, the flag is ignored and the element is returned.
     * @param timeout The maximum time in milliseconds to wait for the element to appear before raising a timeout exception.
     *
     * @return The found [Element], or null if no matching element is found within the timeout.
     */
    suspend fun find(
        text: String,
        bestMatch: Boolean = true,
        returnEnclosingElement: Boolean = true,
        timeout: Long = 10_000,
    ): Element

    /**
     * Selects an element in the DOM using a CSS selector.
     *
     * This method waits for the element to appear in the DOM, retrying every 500 milliseconds until the timeout is reached.
     *
     * @param selector The CSS selector of the element to select.
     * @param timeout The maximum time in milliseconds to wait for the element to appear. Defaults to 10 seconds.
     *
     * @throws TimeoutWaitingForElementException if the element is not found before the timeout.
     *
     * @return The selected [Element] if found before the timeout, otherwise throws a [TimeoutWaitingForElementException].
     */
    suspend fun select(
        selector: String,
        timeout: Long = 10_000,
    ): Element

    /**
     * Finds multiple elements by their text content, optionally waiting for them to appear.
     *
     * This method searches for all elements containing the specified text. If no elements are found,
     * it waits and retries until at least one element is found or the timeout is reached.
     *
     * @param text The text to search for. Note: script contents are also considered text.
     * @param timeout The maximum time in milliseconds to wait for elements to appear before raising a timeout exception.
     *
     * @return A list of found [Element]s.
     *
     * @throws TimeoutWaitingForElementException if no elements are found within the timeout.
     */
    suspend fun findAll(
        text: String,
        timeout: Long = 10_000,
    ): List<Element>

    /**
     * Finds multiple elements by CSS selector, optionally waiting for them to appear.
     *
     * Can also be used to wait for such elements to appear. Optionally includes results from iframes.
     *
     * @param selector CSS selector, e\.g\., `a[href]`, `button[class*=close]`, `a > img[src]`
     * @param timeout Raise timeout exception when after this many milliseconds nothing is found.
     * @param includeFrames Whether to include results in iframes.
     *
     * @return List of found [Element]s.
     * @throws TimeoutWaitingForElementException if no elements are found within the timeout.
     */
    suspend fun selectAll(
        selector: String,
        timeout: Long = 10_000,
        includeFrames: Boolean = false,
    ): List<Element>

    /**
     * Finds elements by XPath string.
     *
     * If not immediately found, retries are attempted until [timeout] is reached (default 10 seconds).
     * In case nothing is found, it returns an empty list. It will not throw.
     * This timeout mechanism helps when relying on some element to appear before continuing your script.
     *
     * Example usage:
     *
     * ```kotlin
     * // Find all the inline scripts (script elements without src attribute)
     * tab.xpath("//script[not(@src)]")
     *
     * // More complex, case-insensitive text search
     * tab.xpath("//text()[ contains( translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'),'test')]")
     * ```
     *
     * @param xpath The XPath string to search for.
     * @param timeout The maximum time in milliseconds to wait for elements to appear before returning. Default is 10 seconds.
     *
     * @return List of found [Element]s, or an empty list if none found within the timeout.
     */
    suspend fun xpath(
        xpath: String,
        timeout: Long = 10_000,
    ): List<Element>

    /**
     * Equivalent of JavaScript's `document.querySelectorAll`.
     * This is considered one of the main methods to use in this package.
     *
     * It returns all matching [Element] objects.
     *
     * @param selector CSS selector. (first time? See https://www.w3schools.com/cssref/css_selectors.php)
     * @param node For internal use. The node to start the search from. Defaults to the document root.
     *
     * @return List of matching [Element]s.
     */
    suspend fun querySelectorAll(
        selector: String,
        node: NodeOrElement? = null,
    ): List<Element>

    /**
     * Finds a single element based on a CSS selector string.
     *
     * @param selector CSS selector(s).
     * @param node For internal use. The node to start the search from. Defaults to the document root.
     *
     * @return The found [Element], or null if no matching element is found.
     */
    suspend fun querySelector(
        selector: String,
        node: NodeOrElement? = null,
    ): Element?

    /**
     * Returns elements which match the given text.
     * Please note: this may (or will) also return any other element (like inline scripts),
     * which happen to contain that text.
     *
     * @param text The text to search for.
     * @param tagHint When provided, narrows down search to only elements which match given tag (e.g., a, div, script, span).
     * @return List of matching [Element]s.
     */
    suspend fun findElementsByText(
        text: String,
        tagHint: String? = null,
    ): List<Element>

    /**
     * Finds and returns the first element containing the specified [text], or the best match if [bestMatch] is true.
     *
     * @param text The text to search for within elements.
     * @param bestMatch If true, finds the closest match based on length, which is more expensive and slower.
     *                  This is useful when searching for common terms (e.g., "login") to get the most relevant element,
     *                  such as a login button, instead of unrelated elements containing the text.
     * @param returnEnclosingElement If true, returns the enclosing element of the found text node.
     *
     * @return The found [Element], or null if no matching element is found.
     */
    suspend fun findElementByText(
        text: String,
        bestMatch: Boolean = false,
        returnEnclosingElement: Boolean = true,
    ): Element?

    /**
     * Disables the DOM agent to stop receiving DOM-related events.
     *
     * This method is useful when you no longer need to interact with the DOM or want to clean up resources.
     */
    suspend fun disableDomAgent()

    /**
     * Moves the mouse cursor to the specified \[x, y\] coordinates, optionally in multiple steps and with a flash effect.
     *
     * @param x The target x coordinate.
     * @param y The target y coordinate.
     * @param steps The number of steps to move the mouse (default: 10). If less than 1, moves in a single step.
     * @param flash If true, flashes the point at each step.
     */
    suspend fun mouseMove(x: Double, y: Double, steps: Int = 10, flash: Boolean = false)

    /**
     * Performs a native mouse click at the specified \[x, y\] coordinates.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param button The mouse button (LEFT, RIGHT, MIDDLE). Default is LEFT.
     * @param buttons Which button (default 1 = left).
     * @param modifiers Bit field for pressed modifier keys. Alt=1, Ctrl=2, Meta/Command=4, Shift=8 (default: 0).
     */
    suspend fun mouseClick(
        x: Double,
        y: Double,
        button: Input.MouseButton = Input.MouseButton.LEFT,
        buttons: Int = 1,
        modifiers: Int = 0,
    )

    /**
     * Expects a request to match the given [urlPattern].
     *
     * For example, capturing the response body of a specific API call can be done like this:
     * ```kotlin
     * val responseBody = tab.expect(Regex("https://api.example.com/data")) {
     *     tab.get("https://example.com")
     *     getResponseBody<UserData>()
     * }
     * ```
     *
     * @param urlPattern The regex pattern to match the request URL.
     * @param block The block to execute during which the expectation is active.
     */
    suspend fun <T> expect(urlPattern: Regex, block: suspend RequestExpectation.() -> T): T

    /**
     * Intercepts network requests matching the given [urlPattern] and [requestStage].
     * This allows you to modify requests, responses, or block them entirely.
     *
     * Example usage:
     * ```kotlin
     * tab.intercept("https://api.example.com/data", Fetch.RequestStage.RESPONSE, Network.ResourceType.XHR) {
     *     tab.get("https://example.com")
     *
     *     // Can modify the request or response here, or intercept the body before it gets unavailable
     *     val originalResponse = getResponseBody<UserData>()
     *     continueRequest()
     * }
     */
    suspend fun <T> intercept(
        urlPattern: String,
        requestStage: Fetch.RequestStage,
        resourceType: Network.ResourceType,
        block: suspend FetchInterception.() -> T,
    ): T

    /**
     * Takes a screenshot of the page and returns the result as a base64 encoded string.
     * This is not the same as [Element.screenshotB64], which takes a screenshot of a single element only.
     *
     * @param format "jpeg" or "png" (defaults to "jpeg").
     * @param fullPage When false (default), captures the current viewport. When true, captures the entire page.
     * @return Screenshot data as a base64 encoded string.
     */
    suspend fun screenshotB64(
        format: ScreenshotFormat = ScreenshotFormat.JPEG,
        fullPage: Boolean = false,
    ): String

    /**
     * Saves a screenshot of the page.
     * This is not the same as [Element.saveScreenshot], which saves a screenshot of a single element only.
     *
     * @param filename Uses this as the save path. If "auto" or null, generates a filename based on the page URL and timestamp.
     * @param format "jpeg" or "png" (defaults to "jpeg").
     * @param fullPage When false (default), captures the current viewport. When true, captures the entire page.
     * @return The path/filename of the saved screenshot.
     */
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun saveScreenshot(
        filename: Path? = null,
        format: ScreenshotFormat = ScreenshotFormat.JPEG,
        fullPage: Boolean = false,
    ): String

    /**
     * Gets all elements of tag: link, a, img, script, meta.
     *
     * @return List of [Element]s matching the asset tags.
     */
    suspend fun getAllLinkedSources(): List<Element>

    /**
     * Convenience function, which returns all links (a, link, img, script, meta).
     *
     * @param absolute If true, tries to build all the links in absolute form instead of "as is" (often relative).
     * @return List of URLs as [String].
     */
    suspend fun getAllUrls(absolute: Boolean = true): List<String>

}
