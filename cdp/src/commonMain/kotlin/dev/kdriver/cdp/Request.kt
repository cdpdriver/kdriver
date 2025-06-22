package dev.kdriver.cdp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Representation of the request of CDP command.
 *
 * See also: [Message.Response]
 */
@Serializable
class Request(
    val id: Long,
    val method: String,
    val params: JsonElement?,
)
