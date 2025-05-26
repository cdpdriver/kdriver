package dev.kdriver.core.tab

import dev.kdriver.cdp.domain.Runtime
import kotlinx.serialization.Serializable

@Serializable
data class EvaluateException(
    val error: Runtime.ExceptionDetails,
) : RuntimeException(
    "Error evaluating expression: $error"
)
