package dev.kdriver.core.dom

import dev.kdriver.cdp.domain.DOM
import dev.kdriver.cdp.domain.Runtime
import dev.kdriver.core.tab.Tab
import kotlinx.io.files.Path
import kotlinx.serialization.json.JsonElement

/**
 * Represents a DOM element in the browser.
 *
 * This class provides methods to interact with the DOM element, such as clicking, sending keys, and applying JavaScript functions.
 * It also provides properties to access the element's tag name, text content, and position in the viewport.
 */
interface Element {

    /**
     * The underlying DOM node representing this element.
     */
    val node: DOM.Node

    /**
     * The DOM tree in which this element resides.
     */
    val tree: DOM.Node?

    /**
     * The name of the tag of the element, in lowercase.
     *
     * For example, if the element is a `<div>`, this will return "div".
     */
    val tag: String

    /**
     * The text content of the element, which is the concatenation of all text nodes
     * within the element, excluding any HTML tags.
     */
    val text: String

    /**
     * Gets the text contents of this element, and it's children in a concatenated string
     * NOTE: this includes text in the form of script content, as those are also just 'text nodes'
     */
    val textAll: String

    /**
     * The internal node ID of the element in the DOM tree.
     */
    val backendNodeId: Int

    val nodeType: Int

    val objectId: String?

    val parentId: Int?

    val parent: Element?

    /**
     * Returns the elements' children. Those children also have a children property
     * so you can browse through the entire tree as well.
     */
    val children: List<Element>

    /**
     * Returns a list of attributes of the element.
     *
     * Each attribute is represented by its name, and the value can be accessed using the `get` operator.
     *
     * For example, if an element has attributes `class="my-class"` and `id="my-id"`, the list will contain:
     * ```kotlin
     * val attrs = element.attrs // ["class", "id"]
     * val classValue = element["class"] // "my-class"
     * val idValue = element["id"] // "my-id"
     * ```
     */
    val attrs: List<String>

    suspend fun updateRemoteObject(): Runtime.RemoteObject?

    /**
     * Updates the element to retrieve more properties, such as enabling the `children` and `parent` attributes.
     *
     * Also resolves the JavaScript object which is stored in [remoteObject].
     *
     * Usually, elements are obtained via [Tab.querySelectorAll] or [Tab.findElementsByText], and those elements are already updated.
     * The reason for a separate call instead of doing it at initialization is because retrieving many elements can be expensive.
     * Therefore, it is not advised to call this method on a large number of elements at the same time.
     */
    suspend fun update(nodeOverride: DOM.Node? = null): Element

    /**
     * Returns the value of the attribute with the given name, or null if it does not exist.
     *
     * For example, if the element has an attribute `class="my-class"`, you can retrieve it with:
     * ```kotlin
     * val classValue = element["class"]
     * ```
     *
     * @param name The name of the attribute to retrieve.
     * @return The value of the attribute, or null if it does not exist.
     */
    operator fun get(name: String): String?

    /**
     * Clicks the element, simulating a user click.
     *
     * This method resolves the element's remote object and calls the `click` function on it.
     * It also flashes the element for a short duration to indicate the click action.
     *
     * @throws IllegalStateException if the remote object cannot be resolved.
     */
    suspend fun click()

    /**
     * Moves the mouse to the center of the element and simulates a mouse click.
     *
     * This method retrieves the position of the element, moves the mouse to that position,
     * and dispatches mouse events to simulate a click.
     */
    suspend fun mouseMove()

    /**
     * Focuses the element, making it the active element in the document.
     *
     * This method applies a JavaScript function to focus the element.
     * It is useful for input fields or elements that require user interaction.
     */
    suspend fun focus()

    /**
     * Sends a sequence of keys to the element, simulating user input.
     *
     * This method focuses the element and dispatches key events for each character in the provided text.
     *
     * If the text contains special characters or needs to be handled differently,
     * prefer calling `sendKeysWithSpecialChars(text: String)` instead.
     *
     * @param text The text to send to the element.
     */
    suspend fun sendKeys(text: String)

    /**
     * Inserts text into the element, simulating user input.
     *
     * This method focuses the element and uses the input API to insert the provided text.
     * It is useful for filling out input fields or text areas. It acts like a paste operation.
     *
     * @param text The text to insert into the element.
     */
    suspend fun insertText(text: String)

    /**
     * Sends a list of file paths to the element, simulating a file input.
     *
     * This method sets the files for the element's file input using the provided paths.
     *
     * @param paths A list of file paths to send to the element.
     */
    suspend fun sendFile(paths: List<Path>)

    /**
     * Retrieves the value of the element, typically used for input fields.
     *
     * This method applies a JavaScript function to get the `value` property of the element.
     *
     * @return The value of the element, or null if it does not have a value.
     */
    suspend fun getInputValue(): String?

    /**
     * Clears the input of the element by setting its value to an empty string.
     *
     * This method applies a JavaScript function to the element to clear its value.
     * It is useful for input fields or text areas that need to be reset.
     */
    suspend fun clearInput()

    /**
     * Clears the input of the element by simulating a series of delete key presses.
     *
     * This method applies a JavaScript function that simulates pressing the delete key
     * repeatedly until the input is empty. It is useful for clearing input fields or text areas
     * when [clearInput] does not work (for example, when custom input handling is implemented on the page).
     */
    suspend fun clearInputByDeleting()

    /**
     * Applies a JavaScript function to the element and returns the result. The given js_function string should accept the js element as parameter,
     * and can be a arrow function, or function declaration.
     *
     * Examples of valid JavaScript functions:
     * - `(elem) => { elem.value = "blabla"; console.log(elem); alert(JSON.stringify(elem)); }`
     * - `elem => elem.play()`
     * - `function myFunction(elem) { alert(elem) }`
     *
     * @param jsFunction The JavaScript function to apply to the element.
     * @param awaitPromise If true, waits for any promises to resolve before returning the result.
     *
     * @return The result of the function call, or null if the result is not serializable.
     */
    suspend fun rawApply(
        jsFunction: String,
        awaitPromise: Boolean = false,
    ): JsonElement?

    /**
     * Finds all descendant elements matching the given CSS selector, similar to JavaScript's querySelectorAll().
     *
     * @param selector The CSS selector to match.
     * @return A list of matching [Element]s.
     */
    suspend fun querySelectorAll(selector: String): List<Element>

    /**
     * Finds the first descendant element matching the given CSS selector, similar to JavaScript's querySelector().
     *
     * @param selector The CSS selector to match.
     * @return The first matching [Element], or null if none found.
     */
    suspend fun querySelector(selector: String): Element?

    /**
     * Retrieves the position of the element in the viewport.
     *
     * This method calculates the position of the element based on its content quads.
     * If `abs` is true, it returns the absolute position relative to the document.
     *
     * @param abs If true, returns the absolute position; otherwise, returns the relative position.
     * @return The position of the element, or null if it could not be determined.
     */
    suspend fun getPosition(abs: Boolean = false): Position?

    /**
     * Returns a string representation of the element, including its tag name, attributes, and content.
     *
     * The string representation is formatted as an HTML-like tag, with attributes in the format `key="value"`.
     * For example, an element with tag name `div`, class `my-class`, and content `Hello World` would be represented as:
     *
     * ```html
     * <div class="my-class">Hello World</div>
     * ```
     */
    override fun toString(): String

}
