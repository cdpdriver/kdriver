package dev.kdriver.core.exceptions

import kotlinx.serialization.Serializable

/**
 * Exception thrown when a timeout occurs while waiting for a specific element in the browser context.
 *
 * @property selector The CSS selector of the element that was being waited for.
 * @property timeout The maximum time in milliseconds to wait for the element.
 */
@Serializable
data class TimeoutWaitingForElementException(
    /**
     * The CSS selector of the element that was being waited for.
     */
    val selector: String,
    /**
     * The maximum time in milliseconds to wait for the element.
     */
    val timeout: Long,
) : IllegalStateException(
    "time ran out while waiting for: $selector"
)
