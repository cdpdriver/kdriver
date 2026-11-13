package dev.kdriver.core.dom

import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.InternalCdpApi
import dev.kdriver.cdp.Serialization
import dev.kdriver.cdp.domain.Input
import dev.kdriver.core.connection.DefaultConnection
import dev.kdriver.core.connection.PendingCommand
import dev.kdriver.core.tab.Tab
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

/**
 * Input events of one gesture (a mouse trajectory, a press and its release, a burst of keys), written to the socket one
 * after the other without waiting for each reply, the replies read once at the end by [awaitAll].
 *
 * The browser receives the same events in the same order, paced by the caller's own delays rather than by the round
 * trip of each event: on a remote link a ten-step trajectory costs one round trip instead of ten. A reply that already
 * came back as an error stops the gesture at the next event, so a failure is still seen as soon as it is known.
 *
 * On a tab that is not a [DefaultConnection] (a custom [Tab]), each event is sent and awaited as before.
 */
@OptIn(InternalCdpApi::class)
internal class InputPipeline(private val tab: Tab) {

    private val pending = mutableListOf<PendingCommand>()

    private suspend fun send(method: String, parameter: JsonElement) {
        pending.forEach { it.throwIfFailed() }
        val connection = tab as? DefaultConnection
        if (connection == null) tab.callCommand(method, parameter, CommandMode.DEFAULT)
        else pending += connection.sendCommand(method, parameter, CommandMode.DEFAULT)
    }

    /** Sends an `Input.dispatchMouseEvent`. */
    suspend fun mouse(event: Input.DispatchMouseEventParameter) =
        send("Input.dispatchMouseEvent", Serialization.json.encodeToJsonElement(event))

    /** Sends an `Input.dispatchKeyEvent`. */
    suspend fun key(event: Input.DispatchKeyEventParameter) =
        send("Input.dispatchKeyEvent", Serialization.json.encodeToJsonElement(event))

    /** Sends an `Input.insertText`. */
    suspend fun insertText(text: String) =
        send("Input.insertText", Serialization.json.encodeToJsonElement(Input.InsertTextParameter(text = text)))

    /** Reads every reply, in the order sent; throws the first error. */
    suspend fun awaitAll() {
        val replies = pending.toList()
        pending.clear()
        replies.forEach { it.await() }
    }

}

/**
 * Runs [block] with an [InputPipeline] on this tab, then waits for all its replies.
 *
 * @return what [block] returns.
 * @throws dev.kdriver.cdp.CDPException the first error the browser answered, as an awaited call would have thrown it.
 */
internal suspend fun <T> Tab.pipelineInput(block: suspend InputPipeline.() -> T): T {
    val pipeline = InputPipeline(this)
    val result = pipeline.block()
    pipeline.awaitAll()
    return result
}
