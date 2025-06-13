package dev.kdriver.core.exceptions

/**
 * Exception thrown when the connection to the browser fails.
 *
 * This can happen for various reasons, such as running the browser in a restricted environment,
 * or a timeout while trying to connect.
 */
class FailedToConnectToBrowserException : Exception(
    """
    Failed to connect to browser.
    Possible cause: running as root. Use no_sandbox = true in that case.
    """.trimIndent()
)
