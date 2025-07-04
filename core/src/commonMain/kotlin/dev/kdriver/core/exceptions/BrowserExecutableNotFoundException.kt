package dev.kdriver.core.exceptions

import kotlinx.io.IOException

// See https://github.com/Kotlin/kotlinx-io/pull/459

/**
 * Exception thrown when the browser executable cannot be found.
 *
 * This exception is typically thrown when the browser is not installed in the default location or
 * when the `browserExecutablePath` parameter is not specified.
 */
class BrowserExecutableNotFoundException : /*FileNotFoundException*/ IOException(
    """
    Could not determine browser executable.
    Make sure your browser is installed in the default location (path).
    Or specify browserExecutablePath parameter.
    """.trimIndent()
)
