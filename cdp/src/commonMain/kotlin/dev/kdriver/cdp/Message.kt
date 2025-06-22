package dev.kdriver.cdp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A kind of data frame comes from CDP.
 * It should be [Response] or [Event].
 */
@Serializable(with = MessageSerializer::class)
sealed class Message {

    /**
     * The response of a command.
     *
     * See also: [Request]
     */
    @Serializable
    class Response(
        val id: Long,
        val result: JsonElement? = null,
        val error: ResponseError? = null,
    ) : Message() {

        /**
         * Representation of the error returns from the browser.
         */
        @Serializable
        class ResponseError(
            val code: Int,
            val message: String,
            val data: String? = null,
        ) {

            /**
             * Throw this error as [CDPException].
             */
            fun throwAsException(method: String) {
                throw CDPException(method, code, message, data)
            }

        }
    }

    /**
     * Events emitted by the browser.
     */
    @Serializable
    class Event(
        val method: String,
        val params: JsonElement? = null,
    ) : Message()

}
