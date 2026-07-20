package dev.kdriver.core.connection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.Socket
import java.util.*

/**
 * A dumb TCP relay used by reconnection integration tests.
 *
 * It forwards raw bytes between a kdriver client and a real Chrome DevTools endpoint, which makes it
 * transparent for both the HTTP (`/json/version`) and WebSocket traffic. Because it sits on the TCP
 * stream it can reproduce, on demand, the two failure modes we care about:
 *  - [severAll] closes the sockets (RST/FIN) -> a clean disconnect,
 *  - [freeze]/[unfreeze] keep the sockets open but stop forwarding application bytes -> a half-open
 *    socket (the peer looks alive at the TCP level but never answers), which only WebSocket keep-alive
 *    pings can detect.
 *
 * @param upstreamHost Host of the real Chrome DevTools endpoint.
 * @param upstreamPort Port of the real Chrome DevTools endpoint.
 * @param scope Scope the relay coroutines run in.
 */
class TcpProxy(
    private val upstreamHost: String,
    private val upstreamPort: Int,
    private val scope: CoroutineScope,
) {

    private val server = ServerSocket(0)
    private val sockets = Collections.synchronizedList(mutableListOf<Socket>())

    @Volatile
    private var forwarding = true

    /** The local port clients should connect to. */
    val port: Int get() = server.localPort

    fun start() {
        scope.launch(Dispatchers.IO) {
            while (!server.isClosed) {
                val client = try {
                    server.accept()
                } catch (_: Exception) {
                    break
                }
                val upstream = try {
                    Socket(upstreamHost, upstreamPort)
                } catch (_: Exception) {
                    runCatching { client.close() }
                    continue
                }
                sockets.add(client)
                sockets.add(upstream)
                pump(client, upstream)
                pump(upstream, client)
            }
        }
    }

    private fun pump(from: Socket, to: Socket) {
        scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(16 * 1024)
            try {
                val input = from.getInputStream()
                val output = to.getOutputStream()
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    // While frozen we keep draining the socket (so TCP stays healthy and no RST is
                    // sent) but drop the bytes: the peer sees silence at the application level.
                    if (forwarding) {
                        output.write(buffer, 0, read)
                        output.flush()
                    }
                }
            } catch (_: Exception) {
                // Socket closed (severed or torn down): just end this pump.
            }
        }
    }

    /** Simulate a clean disconnect: close every live socket so both ends see the stream end. */
    fun severAll() {
        synchronized(sockets) {
            sockets.forEach { runCatching { it.close() } }
            sockets.clear()
        }
    }

    /** Simulate a half-open socket: keep connections open but stop forwarding application bytes. */
    fun freeze() {
        forwarding = false
    }

    /** Resume forwarding after a [freeze]. */
    fun unfreeze() {
        forwarding = true
    }

    fun stop() {
        severAll()
        runCatching { server.close() }
    }

}
