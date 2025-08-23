package dev.kdriver.cdp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Base type for any message exchanged with the Chrome DevTools Protocol (CDP).
 *
 * A [Message] can be either:
 * - a [Response]: the result of a previously sent command,
 * - an [Event]: a notification emitted asynchronously by the browser.
 *
 * See: https://chromedevtools.github.io/devtools-protocol/
 */
@Serializable(with = MessageSerializer::class)
sealed class Message {

    /**
     * Represents the response of a CDP command.
     *
     * A [Response] always corresponds to a previously sent [Request],
     * matched by its [id].
     */
    @Serializable
    class Response(
        /**
         * The unique identifier of the request.
         *
         * This is the same `id` that was sent with the original [Request],
         * allowing the client to match a response to its command.
         */
        val id: Long,
        /**
         * The result of the command execution, if successful.
         *
         * This field is a raw JSON element and its structure depends on
         * the specific CDP domain and command invoked.
         *
         * - Present if the command succeeded.
         * - Absent (null) if an [error] is returned instead.
         */
        val result: JsonElement? = null,
        /**
         * The error returned by the browser, if the command failed.
         *
         * - Present only if the command could not be executed successfully.
         * - Contains details such as error [code], [message], and optional [data].
         */
        val error: ResponseError? = null,
    ) : Message() {

        /**
         * Representation of an error returned by the browser
         * in response to a command.
         *
         * See also: https://chromedevtools.github.io/devtools-protocol/tot/Debugger/#type-Error
         */
        @Serializable
        class ResponseError(
            /**
             * The numeric error code, as defined by the protocol.
             *
             * Example: `-32000` is often used for generic command failures.
             */
            val code: Int,
            /**
             * A human-readable description of the error.
             *
             * Example: `"No node with given id found"`.
             */
            val message: String,
            /**
             * Optional additional data providing more context about the error.
             *
             * Its format depends on the specific CDP domain.
             * May contain raw JSON serialized as string.
             */
            val data: String? = null,
        ) {

            /**
             * Throws this error as a [CDPException], preserving
             * the original [code], [message], and [data].
             *
             * @param method The command method name that triggered the error.
             */
            fun throwAsException(method: String) {
                throw CDPException(method, code, message, data)
            }

        }
    }

    /**
     * Represents an event emitted asynchronously by the browser.
     *
     * Unlike [Response], an [Event] is not tied to a specific request.
     * It is pushed from the browser to notify the client of changes,
     * such as `"Network.requestWillBeSent"` or `"Debugger.paused"`.
     */
    @Serializable
    class Event(
        /**
         * The event method name.
         *
         * Example: `"Network.requestWillBeSent"`.
         * Identifies the CDP domain and event type.
         */
        val method: String,
        /**
         * The event parameters, if any.
         *
         * This is a raw JSON element whose structure depends on the event type.
         * Some events may not include parameters (null).
         */
        val params: JsonElement? = null,
    ) : Message()

}
