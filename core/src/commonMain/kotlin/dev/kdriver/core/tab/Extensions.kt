package dev.kdriver.core.tab

import dev.kaccelero.serializers.Serialization
import kotlinx.serialization.json.decodeFromJsonElement


/**
 * Evaluates a JavaScript expression in the context of the tab.
 *
 * @param expression The JavaScript expression to evaluate.
 * @param awaitPromise If true, waits for any promises to resolve before returning the result.
 *
 * @return The result of the evaluation, deserialized to type T, or null if no result is returned.
 */
suspend inline fun <reified T> Tab.evaluate(
    expression: String,
    awaitPromise: Boolean = false,
): T? {
    val raw = rawEvaluate(expression, awaitPromise) ?: return null
    return Serialization.json.decodeFromJsonElement<T>(raw)
}
