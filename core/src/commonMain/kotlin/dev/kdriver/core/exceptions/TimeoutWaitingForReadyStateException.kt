package dev.kdriver.core.exceptions

import dev.kdriver.core.tab.ReadyState
import kotlinx.serialization.Serializable

/**
 * Exception thrown when a timeout occurs while waiting for a specific ready state in the browser tab.
 *
 * @property until The expected ready state to wait for.
 * @property timeoutSeconds The maximum time in seconds to wait for the ready state.
 * @property lastKnownReadyState The last known ready state before the timeout occurred, if available.
 */
@Serializable
data class TimeoutWaitingForReadyStateException(
    /**
     * The expected ready state to wait for.
     */
    val until: ReadyState,
    /**
     * The maximum time in seconds to wait for the ready state.
     */
    val timeoutSeconds: Int,
    /**
     * The last known ready state before the timeout occurred, if available.
     */
    val lastKnownReadyState: ReadyState?,
) : IllegalStateException(
    "Timeout waiting for readyState == $until"
)
