package dev.kdriver.core.exceptions

/**
 * Thrown to a pending CDP command when the underlying WebSocket connection is closed (gracefully or
 * by a disconnect) before its response is received.
 *
 * This lets callers observe a failure instead of hanging forever on a reply that will never arrive.
 */
class ConnectionClosedException(
    message: String = "The connection to the browser was closed before a response was received",
    cause: Throwable? = null,
) : RuntimeException(message, cause)
