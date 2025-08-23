package dev.kdriver.cdp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Representation of a request sent to the Chrome DevTools Protocol (CDP).
 *
 * A [Request] represents a command that the client (you) sends to the browser.
 * Each request will eventually yield a matching [Message.Response] with the same [id].
 *
 * See also: [Message.Response]
 */
@Serializable
class Request(
    /**
     * The unique identifier of the request.
     *
     * - Must be unique among in-flight requests.
     * - Used by the browser to correlate this request with the corresponding [Message.Response].
     */
    val id: Long,
    /**
     * The method name of the CDP command to invoke.
     *
     * Format: `"Domain.commandName"`, e.g.:
     * - `"Page.navigate"`
     * - `"Runtime.evaluate"`
     * - `"Debugger.enable"`
     */
    val method: String,
    /**
     * Optional parameters of the command, encoded as JSON.
     *
     * - The structure depends on the command being invoked.
     * - May be `null` if the command does not require any parameters.
     *
     * Example (for `Runtime.evaluate`):
     * ```json
     * {
     *   "expression": "2 + 2",
     *   "returnByValue": true
     * }
     * ```
     */
    val params: JsonElement?,
)
