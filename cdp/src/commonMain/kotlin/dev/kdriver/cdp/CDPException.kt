package dev.kdriver.cdp

/**
 * Exception thrown when the browser responds with an error to a CDP command.
 *
 * Wraps the [Message.Response.ResponseError] fields into a typed exception
 * so that client code can catch and handle failures programmatically.
 */
class CDPException(
    /**
     * The CDP command method that triggered the error.
     *
     * Example: `"Runtime.evaluate"`.
     */
    val method: String,
    /**
     * The numeric error code returned by the browser.
     *
     * Codes are typically JSON-RPC standard values, e.g.:
     * - `-32601`: Method not found
     * - `-32602`: Invalid params
     * - `-32000`: Generic command execution error
     */
    val code: Int,
    /**
     * The original error message from the browser.
     *
     * Example: `"No node with given id found"`.
     */
    val originalMessage: String,
    /**
     * Optional extra data returned by the browser for more context.
     *
     * - May be `null` if no additional data is provided.
     * - Sometimes contains structured JSON (stack trace, exception details).
     */
    val data: String?,
) : Exception(
    "Error while calling command $method: $originalMessage (code: $code)"
)
