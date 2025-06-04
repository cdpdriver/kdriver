package dev.kdriver.core.tab

import dev.kdriver.cdp.domain.Runtime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class EvaluateException(
    val error: Runtime.ExceptionDetails,
    val jsError: String,
) : RuntimeException("Error evaluating expression: $error") {

    constructor(error: Runtime.ExceptionDetails) : this(
        error,
        error.exception?.value?.jsonPrimitive?.content ?: error.exception?.description ?: error.text
    )

}
