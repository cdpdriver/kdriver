package dev.kdriver.core.dom

import dev.kaccelero.serializers.Serialization
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
 * @return The result of the function call, or null if the result is not serializable.
 */
suspend inline fun <reified T> Element.apply(
    jsFunction: String,
    awaitPromise: Boolean = false,
): T? {
    val raw = rawApply(jsFunction, awaitPromise) ?: return null
    return Serialization.json.decodeFromJsonElement<T>(raw)
}
