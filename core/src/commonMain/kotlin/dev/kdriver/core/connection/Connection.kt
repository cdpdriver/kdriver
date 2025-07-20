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
