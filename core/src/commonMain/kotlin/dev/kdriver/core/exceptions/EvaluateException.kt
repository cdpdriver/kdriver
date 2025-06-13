package dev.kdriver.core.exceptions

import dev.kdriver.cdp.domain.Runtime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonPrimitive

/**
 * Exception thrown when an error occurs while evaluating a JavaScript expression in the browser context.
 *
 * @property error Details of the exception that occurred during evaluation.
 * @property jsError The JavaScript error message, if available.
 */
@Serializable
data class EvaluateException(
    /**
     * Details of the exception that occurred during evaluation.
     */
    val error: Runtime.ExceptionDetails,
    /**
     * The JavaScript error message, if available.
     */
    val jsError: String,
) : RuntimeException("Error evaluating expression: $error") {

    constructor(error: Runtime.ExceptionDetails) : this(
        error,
        error.exception?.value?.jsonPrimitive?.content ?: error.exception?.description ?: error.text
    )

}
