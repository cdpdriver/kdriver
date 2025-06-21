package dev.kdriver.core.dom

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.domain.*
import dev.kdriver.core.exceptions.EvaluateException
import dev.kdriver.core.tab.Tab
import dev.kdriver.core.utils.filterRecurse
import io.ktor.util.logging.*
import kotlinx.io.files.Path
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * Represents a DOM element in the browser.
 *
 * This class provides methods to interact with the DOM element, such as clicking, sending keys, and applying JavaScript functions.
 * It also provides properties to access the element's tag name, text content, and position in the viewport.
 */
data class Element(
    val node: DOM.Node,
    val tab: Tab,
    val tree: DOM.Node? = null,
) {

    private val logger = KtorSimpleLogger("Element")

    private var remoteObject: Runtime.RemoteObject? = null

    /**
     * The name of the tag of the element, in lowercase.
     *
     * For example, if the element is a `<div>`, this will return "div".
     */
    val tag: String
        get() = node.nodeName.lowercase()

    /**
     * The text content of the element, which is the concatenation of all text nodes
     * within the element, excluding any HTML tags.
     */
    val text: String
        get() = filterRecurse(
            node,
            predicate = { it.nodeType == 3 },
            getChildren = { it.children },
            getShadowRoots = { it.shadowRoots }
        )?.nodeValue ?: ""


    /**
     * The internal node ID of the element in the DOM tree.
     */
    val backendNodeId: Int
        get() = node.backendNodeId

    val objectId: String?
        get() = remoteObject?.objectId

    val parentId: Int?
        get() = node.parentId

    val parent: Element?
        get() {
            val tree = this.tree ?: throw RuntimeException("could not get parent since the element has no tree set")
            val parentNode = filterRecurse(
                tree,
                predicate = { node -> node.nodeId == this.parentId },
                getChildren = { node.children },
                getShadowRoots = { it.shadowRoots }
            ) ?: return null
            return Element(parentNode, tab = this.tab, tree = this.tree)
        }

    suspend fun updateRemoteObject(): Runtime.RemoteObject? {
        remoteObject = tab.dom.resolveNode(backendNodeId = backendNodeId).`object`
        return remoteObject
    }

    suspend fun update() {
        // TODO
    }

    private suspend fun flash(durationSeconds: Double) {
        // TODO: Do we really need this?
        // displays for a short time a red dot on the element (only if the element itself is visible)
    }

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
    operator fun get(name: String): String? {
        val keyIndex = node.attributes?.indexOfFirst { it == name }?.takeIf { it != -1 } ?: return null
        return node.attributes?.get(keyIndex + 1)
    }

    /**
     * Clicks the element, simulating a user click.
     *
     * This method resolves the element's remote object and calls the `click` function on it.
     * It also flashes the element for a short duration to indicate the click action.
     *
     * @throws IllegalStateException if the remote object cannot be resolved.
     */
    suspend fun click() {
        updateRemoteObject()
        val objectId = remoteObject?.objectId ?: throw IllegalStateException("Could not resolve object id for $this")

        val arguments = listOf(Runtime.CallArgument(objectId = objectId))

        flash(0.25)
        tab.runtime.callFunctionOn(
            functionDeclaration = "(el) => el.click()",
            objectId = objectId,
            arguments = arguments,
            awaitPromise = true,
            userGesture = true,
            returnByValue = true
        )
    }

    /**
     * Moves the mouse to the center of the element and simulates a mouse click.
     *
     * This method retrieves the position of the element, moves the mouse to that position,
     * and dispatches mouse events to simulate a click.
     */
    suspend fun mouseMove() {
        val position = getPosition()
        if (position == null) {
            logger.warn("Could not find location for $this, not moving mouse")
            return
        }
        val (x, y) = position.center
        logger.debug("Mouse move to location $x, $y where $this is located")

        tab.input.dispatchMouseEvent(
            type = "mouseMoved",
            x = x,
            y = y
        )
        tab.sleep(50)
        tab.input.dispatchMouseEvent(
            type = "mouseReleased",
            x = x,
            y = y
        )
    }

    /**
     * Focuses the element, making it the active element in the document.
     *
     * This method applies a JavaScript function to focus the element.
     * It is useful for input fields or elements that require user interaction.
     */
    suspend fun focus() {
        apply<Unit>("(elem) => elem.focus()")
    }

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
    suspend fun sendKeys(text: String) {
        focus()
        for (char in text) tab.input.dispatchKeyEvent(
            type = "char",
            text = char.toString()
        )
    }

    /**
     * Inserts text into the element, simulating user input.
     *
     * This method focuses the element and uses the input API to insert the provided text.
     * It is useful for filling out input fields or text areas. It acts like a paste operation.
     *
     * @param text The text to insert into the element.
     */
    suspend fun insertText(text: String) {
        focus()
        tab.input.insertText(text)
    }

    /**
     * Sends a list of file paths to the element, simulating a file input.
     *
     * This method sets the files for the element's file input using the provided paths.
     *
     * @param paths A list of file paths to send to the element.
     */
    suspend fun sendFile(paths: List<Path>) {
        tab.dom.setFileInputFiles(
            files = paths.map { it.toString() },
            backendNodeId = backendNodeId,
            objectId = objectId,
        )
    }

    /**
     * Clears the input of the element by setting its value to an empty string.
     *
     * This method applies a JavaScript function to the element to clear its value.
     * It is useful for input fields or text areas that need to be reset.
     */
    suspend fun clearInput() {
        apply<Unit>("function (element) { element.value = \"\" }")
    }

    /**
     * Clears the input of the element by simulating a series of delete key presses.
     *
     * This method applies a JavaScript function that simulates pressing the delete key
     * repeatedly until the input is empty. It is useful for clearing input fields or text areas
     * when [clearInput] does not work (for example, when custom input handling is implemented on the page).
     */
    suspend fun clearInputByDeleting() {
        apply<Unit>(
            jsFunction = """
                async function clearByDeleting(n, d = 50) {
                    n.focus();
                    n.setSelectionRange(0, 0);
                    while (n.value.length > 0) {
                        n.dispatchEvent(
                            new KeyboardEvent("keydown", {
                                key: "Delete",
                                code: "Delete",
                                keyCode: 46,
                                which: 46,
                                bubbles: !0,
                                cancelable: !0,
                            })
                        );
                        n.value = n.value.slice(1);
                        await new Promise((r) => setTimeout(r, d));
                    }
                    n.dispatchEvent(new Event("input", { bubbles: !0 }));
                }
            """.trimIndent(),
            awaitPromise = true
        )
    }

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
    suspend inline fun <reified T> apply(
        jsFunction: String,
        awaitPromise: Boolean = false,
    ): T? {
        val remoteObject = updateRemoteObject()
        val result = tab.runtime.callFunctionOn(
            functionDeclaration = jsFunction,
            objectId = remoteObject?.objectId,
            arguments = listOf(
                Runtime.CallArgument(objectId = remoteObject?.objectId)
            ),
            returnByValue = true,
            userGesture = true,
            awaitPromise = awaitPromise,
        )
        result.exceptionDetails?.let { throw EvaluateException(it) }
        return result.result.value?.let {
            Serialization.json.decodeFromJsonElement<T>(it)
        }
    }

    /**
     * Retrieves the position of the element in the viewport.
     *
     * This method calculates the position of the element based on its content quads.
     * If `abs` is true, it returns the absolute position relative to the document.
     *
     * @param abs If true, returns the absolute position; otherwise, returns the relative position.
     * @return The position of the element, or null if it could not be determined.
     */
    suspend fun getPosition(abs: Boolean = false): Position? {
        updateRemoteObject()

        return try {
            val quads = tab.dom.getContentQuads(objectId = remoteObject!!.objectId).quads
            if (quads.isEmpty()) {
                throw Exception("could not find position for $this")
            }
            val pos = Position(quads[0])
            if (abs) {
                val scrollY = tab.evaluate<Double>("window.scrollY") ?: 0.0
                val scrollX = tab.evaluate<Double>("window.scrollX") ?: 0.0
                val absX = pos.left + scrollX + pos.width / 2
                val absY = pos.top + scrollY + pos.height / 2
                pos.absX = absX
                pos.absY = absY
            }
            pos
        } catch (e: IndexOutOfBoundsException) {
            logger.debug("no content quads for $this. mostly caused by element which is not 'in plain sight'")
            null
        }
    }

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
    override fun toString(): String {
        val tagName = node.nodeName.lowercase()
        var content = ""

        // Collect all text from this leaf
        val childNodeCount = node.childNodeCount ?: 0
        if (childNodeCount > 0) {
            val children = node.children
            if (childNodeCount == 1 && children != null && children.isNotEmpty()) {
                content += children[0].toString()
            } else if (childNodeCount > 1 && children != null) {
                for (child in children) {
                    content += child.toString()
                }
            }
        }

        if (node.nodeType == 3) { // text node
            content += node.nodeValue
            return content
        }

        val attrs = node.attributes?.chunked(2)?.joinToString(" ") {
            val key = it[0]
            val value = it.getOrNull(1) ?: ""
            """$key="$value""""
        } ?: ""

        return if (attrs.isNotBlank()) "<$tagName $attrs>$content</$tagName>"
        else "<$tagName>$content</$tagName>"
    }

}
