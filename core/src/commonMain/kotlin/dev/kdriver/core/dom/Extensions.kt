package dev.kdriver.core.dom

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.domain.DOM
import dev.kdriver.cdp.domain.Input
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement

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
 * @return The result of the function call, or null if the result is not serializable or JavaScript returns null.
 */
suspend inline fun <reified T> Element.apply(
    jsFunction: String,
    awaitPromise: Boolean = false,
): T? {
    val raw = rawApply(jsFunction, awaitPromise) ?: return null
    // If JavaScript returned null, return Kotlin null for nullable types
    if (raw is JsonNull) return null
    return Serialization.json.decodeFromJsonElement<T>(raw)
}

/**
 * Recursively searches the DOM tree starting from this node, returning the first node that matches the given predicate.
 *
 * @param predicate A function that takes a DOM.Node and returns true if it matches the search criteria.
 *
 * @return The first DOM.Node that matches the predicate, or null if no matching node is found.
 */
fun DOM.Node.filterRecurse(predicate: (DOM.Node) -> Boolean): DOM.Node? {
    val children = children ?: return null
    for (child in children) {
        if (predicate(child)) return child

        val shadowRoots = child.shadowRoots
        if (shadowRoots != null && shadowRoots.isNotEmpty()) {
            val shadowResult = shadowRoots[0].filterRecurse(predicate)
            if (shadowResult != null) return shadowResult
        }

        val recursiveResult = child.filterRecurse(predicate)
        if (recursiveResult != null) return recursiveResult
    }
    return null
}

/**
 * Recursively searches the DOM tree starting from this node, returning all nodes that match the given predicate.
 *
 * @param predicate A function that takes a DOM.Node and returns true if it matches the search criteria.
 *
 * @return A list of all DOM.Nodes that match the predicate.
 */
fun DOM.Node.filterRecurseAll(predicate: (DOM.Node) -> Boolean): List<DOM.Node> {
    val children = children ?: return emptyList()
    val out = mutableListOf<DOM.Node>()
    for (child in children) {
        if (predicate(child)) {
            out.add(child)
        }
        val shadowRoots = child.shadowRoots
        if (shadowRoots != null && shadowRoots.isNotEmpty()) {
            out.addAll(shadowRoots[0].filterRecurseAll(predicate))
        }
        out.addAll(child.filterRecurseAll(predicate))
    }
    return out
}


/**
 * Converts MouseButton enum to the buttons bitmask value.
 * Left=1, Right=2, Middle=4, Back=8, Forward=16, None=0
 */
val Input.MouseButton.buttonsMask: Int
    get() = when (this) {
        Input.MouseButton.LEFT -> 1
        Input.MouseButton.RIGHT -> 2
        Input.MouseButton.MIDDLE -> 4
        Input.MouseButton.BACK -> 8
        Input.MouseButton.FORWARD -> 16
        Input.MouseButton.NONE -> 0
    }
