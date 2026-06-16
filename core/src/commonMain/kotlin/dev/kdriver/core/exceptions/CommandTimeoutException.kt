package dev.kdriver.core.exceptions

import kotlinx.serialization.Serializable

/**
 * Exception thrown when a CDP command does not receive a response within the configured timeout.
 *
 * The timeout is configurable through the browser configuration
 * (`commandTimeout`, see [dev.kdriver.core.browser.Config]).
 *
 * @property method The CDP method that was being called.
 * @property requestId The id of the request that timed out, useful to correlate with sent messages.
 * @property timeout The maximum time in milliseconds that was waited for the response.
 */
@Serializable
data class CommandTimeoutException(
    /**
     * The CDP method that was being called.
     */
    val method: String,
    /**
     * The id of the request that timed out, useful to correlate with sent messages.
     */
    val requestId: Long,
    /**
     * The maximum time in milliseconds that was waited for the response.
     */
    val timeout: Long,
) : IllegalStateException(
    "time ran out while waiting for a response to command: $method (request id $requestId, timeout ${timeout}ms)"
)
