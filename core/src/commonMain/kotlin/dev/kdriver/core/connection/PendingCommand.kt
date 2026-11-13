package dev.kdriver.core.connection

import dev.kdriver.cdp.InternalCdpApi
import dev.kdriver.cdp.Message
import dev.kdriver.core.exceptions.CommandTimeoutException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonElement
import kotlin.time.Duration.Companion.milliseconds

/**
 * A CDP command that has been written to the socket and whose reply has not been read yet.
 *
 * Returned by [DefaultConnection.sendCommand], so that several commands can be put on the wire back to back and their
 * replies read afterwards: the batch then costs one round trip instead of one per command. [DefaultConnection.callCommand]
 * is exactly `sendCommand(...).await()`.
 */
@InternalCdpApi
class PendingCommand internal constructor(
    /** The CDP method that was sent, e.g. `Input.dispatchMouseEvent`. */
    val method: String,
    private val requestId: Long,
    private val reply: CompletableDeferred<Message.Response>,
    private val timeout: Long,
    private val release: suspend () -> Unit,
) {

    /**
     * Waits for the reply and returns its result.
     *
     * @return the `result` of the reply.
     * @throws CommandTimeoutException when no reply arrives within the connection's command timeout (a timeout <= 0
     *   waits indefinitely).
     * @throws dev.kdriver.cdp.CDPException when the browser answers with an error.
     * @throws dev.kdriver.core.exceptions.ConnectionClosedException when the connection goes away before the reply.
     */
    suspend fun await(): JsonElement? {
        try {
            // A non-null Message.Response means success; null can only come from the timeout, so
            // there's no ambiguity with a legitimate value. A timeout <= 0 waits indefinitely.
            val result =
                if (timeout > 0) withTimeoutOrNull(timeout.milliseconds) { reply.await() }
                    ?: throw CommandTimeoutException(method, requestId, timeout)
                else reply.await()
            result.error?.throwAsException(method)
            return result.result
        } finally {
            release()
        }
    }

    /**
     * Throws now, without suspending, if the reply has already arrived and is an error (or the connection failed it);
     * does nothing while the reply is still in flight or when it succeeded.
     *
     * Lets a caller that keeps sending stop at the first failure it can already see, instead of only at the end.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun throwIfFailed() {
        if (!reply.isCompleted) return
        reply.getCompletionExceptionOrNull()?.let { throw it }
        reply.getCompleted().error?.throwAsException(method)
    }

}
