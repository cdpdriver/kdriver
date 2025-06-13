package dev.kdriver.core.exceptions

/**
 * Exception thrown when the browser executable path is not set and the platform does not support
 * automatically finding the Chrome executable.
 *
 * This exception is typically thrown when the `browserExecutablePath` parameter is not specified
 * and the `findChromeExecutable` function is not supported on the current platform.
 */
class NoBrowserExecutablePathException : IllegalStateException(
    """
    Browser executable path is not set and findChromeExecutable is not supported on this platform.
    Please specify browserExecutablePath parameter.
    """.trimIndent()
)
