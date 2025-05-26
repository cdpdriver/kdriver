package dev.kdriver.cdp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A kind of data frame comes from CDP.
 * It should be [Response] or [Event].
 */
@Serializable(with = MessageSerializer::class)
internal sealed class Message {

    /**
     * The response of a command.
     *
     * See also: [Request]
     */
    @Serializable
    class Response(
        val id: Int,
        val result: JsonElement? = null,
        val error: ResponseError? = null,
    ) : Message() {

        /**
         * Representation of the error returns from the broswe.
         */
        @Serializable
        class ResponseError(
            val code: Int,
            val message: String,
            val data: String?,
        ) {
            /**
             * Throw this error as [CDPErrorException].
             */
            fun throwAsException() {
                throw CDPErrorException(code, message, data)
            }
        }
    }

    /**
     * Events emitted by the browser.
     */
    @Serializable
    class Event(
        val method: String,
        val params: JsonElement?,
    ) : Message()

}
