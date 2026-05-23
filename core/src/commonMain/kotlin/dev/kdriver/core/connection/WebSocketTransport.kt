package dev.kdriver.core.connection

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the raw WebSocket connection used to talk to the browser.
 *
 * Extracting this lets [DefaultConnection]'s message plumbing (request/response correlation,
 * event dispatch) be exercised without a real browser, by injecting a fake transport in tests.
 */
interface WebSocketTransport {

    /**
     * Whether the underlying connection is currently open.
     */
    val isActive: Boolean

    /**
     * Opens the connection. Must be called before [send] or [incoming]. No-op if already open.
     */
    suspend fun connect()

    /**
     * Sends a raw text payload to the browser.
     */
    suspend fun send(message: String)

    /**
     * Cold stream of raw text payloads received from the browser. Collecting it starts consuming
     * frames; the flow completes when the connection is closed.
     */
    fun incoming(): Flow<String>

    /**
     * Closes the connection.
     */
    suspend fun close()

}
