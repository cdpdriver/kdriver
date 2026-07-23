package dev.kdriver.core.connection

import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.CommandMode
import dev.kdriver.cdp.InternalCdpApi
import dev.kdriver.core.browser.BrowserTarget
import kotlinx.serialization.json.JsonElement

/**
 * Interface representing a connection to a browser target using the Chrome DevTools Protocol (CDP).
 */
interface Connection : BrowserTarget, CDP {

    /**
     * Internal method to call a CDP command.
     *
     * This should not be called directly, but rather through typed methods (like `cdp.network.enable()`).
     */
    @InternalCdpApi
    override suspend fun callCommand(method: String, parameter: JsonElement?, mode: CommandMode): JsonElement?

    /**
     * Closes the websocket connection. Should not be called manually by users.
     */
    @InternalCdpApi
    suspend fun close()

    /**
     * Registers a [restore] action to be re-run automatically after the connection transparently
     * reconnects.
     *
     * A reconnect opens a fresh CDP session that has lost all session-scoped state (enabled domains,
     * overrides such as the user agent, scripts injected with `addScriptToEvaluateOnNewDocument`, …).
     * Features that establish such state register a restorer here — keyed by [key] so it can be
     * replaced or removed — and remove it with [unregisterReconnectRestore] when they tear that state
     * down. Each restorer runs at most once per reconnect, before the command that triggered the
     * restore proceeds; a concurrent command may still race ahead of an in-progress restore. A
     * restorer that throws is logged and skipped rather than failing the command.
     *
     * A restorer must only re-issue CDP commands; it must not itself call
     * [registerReconnectRestore]/[unregisterReconnectRestore].
     */
    suspend fun registerReconnectRestore(key: Any, restore: suspend () -> Unit)

    /**
     * Removes a restorer previously registered with [registerReconnectRestore]. No-op if [key] is
     * not registered.
     */
    suspend fun unregisterReconnectRestore(key: Any)

    /**
     * Updates the target information by fetching it from the CDP.
     *
     * This is useful to refresh the target info after some operations that might change it.
     */
    suspend fun updateTarget()

    /**
     * Waits until the event listener reports idle (no new events received in a certain timespan).
     * When \`t\` is provided, ensures waiting for \`t\` milliseconds, no matter what.
     *
     * @param t Time in milliseconds to wait, or null to wait until idle.
     */
    suspend fun wait(t: Long? = null)

    /**
     * Suspends the coroutine for a specified time in milliseconds.
     *
     * This is a convenience method to ensure that the target information is updated before sleeping.
     *
     * @param t Time in milliseconds to sleep.
     */
    suspend fun sleep(t: Long)

}
