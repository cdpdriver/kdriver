package dev.kdriver.core.browser

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*

/**
 * A minimal reverse proxy in front of a real browser's DevTools endpoint, for integration tests.
 *
 * It serves the endpoint under a path [prefix], requires a header ([requiredHeader]) on every request
 * — the HTTP `/json/…` calls and the WebSocket handshakes alike — and forwards the rest to the
 * browser with the prefix stripped and the `Host` set to the browser's own address. The browser
 * therefore advertises its internal address in `webSocketDebuggerUrl`, as it would behind a real
 * reverse proxy, and a client that does not rewrite that address bypasses the proxy.
 *
 * Plain HTTP requests are forwarded with `Connection: close`, so each connection carries a single
 * request; WebSocket upgrades are piped as raw bytes once the handshake is forwarded.
 */
class PrefixProxy(
    private val upstreamHost: String,
    private val upstreamPort: Int,
    private val prefix: String,
    private val requiredHeader: Pair<String, String>,
    private val scope: CoroutineScope,
) {

    private val server = ServerSocket(0)
    private val sockets = Collections.synchronizedList(mutableListOf<Socket>())

    /** Paths (as received, prefix included) of the WebSocket handshakes forwarded to the browser. */
    val webSocketPaths: MutableList<String> = Collections.synchronizedList(mutableListOf())

    /** Paths of the requests refused for a wrong prefix or a missing header. */
    val refusedPaths: MutableList<String> = Collections.synchronizedList(mutableListOf())

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
                sockets.add(client)
                scope.launch(Dispatchers.IO) { handle(client) }
            }
        }
    }

    private fun handle(client: Socket) {
        val head = readHead(client.getInputStream())
        if (head == null) client.close() else handleRequest(client, head)
    }

    private fun handleRequest(client: Socket, head: String) {
        val lines = head.split("\r\n")
        val (method, path, version) = lines.first().split(" ", limit = 3)
        val headers = lines.drop(1).map { it.substringBefore(":").trim() to it.substringAfter(":").trim() }

        val authorized = headers.any { (name, value) ->
            name.equals(requiredHeader.first, ignoreCase = true) && value == requiredHeader.second
        }
        if (!path.startsWith("$prefix/") || !authorized) {
            refusedPaths.add(path)
            val status = if (authorized) "404 Not Found" else "401 Unauthorized"
            client.getOutputStream()
                .write("HTTP/1.1 $status\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray())
            client.close()
            return
        }

        val upgrade = headers.any { (name, value) ->
            name.equals("Upgrade", ignoreCase = true) && value.equals("websocket", ignoreCase = true)
        }
        if (upgrade) webSocketPaths.add(path)

        val forwardedHeaders = headers
            .filterNot { (name, _) -> name.equals("Host", ignoreCase = true) }
            .filterNot { (name, _) -> !upgrade && name.equals("Connection", ignoreCase = true) }
            .map { (name, value) -> "$name: $value" }
        val forwarded = buildList {
            add("$method ${path.removePrefix(prefix)} $version")
            add("Host: $upstreamHost:$upstreamPort")
            addAll(forwardedHeaders)
            if (!upgrade) add("Connection: close")
        }.joinToString("\r\n", postfix = "\r\n\r\n")

        val upstream = try {
            Socket(upstreamHost, upstreamPort)
        } catch (_: Exception) {
            client.close()
            return
        }
        sockets.add(upstream)
        upstream.getOutputStream().apply {
            write(forwarded.toByteArray())
            flush()
        }
        pump(client, upstream)
        pump(upstream, client)
    }

    /** Reads the request head, up to and excluding the blank line; null if the stream ends first. */
    private fun readHead(input: InputStream): String? {
        val head = StringBuilder()
        while (!head.endsWith("\r\n\r\n")) {
            val b = input.read()
            if (b < 0) return null
            head.append(b.toChar())
        }
        return head.removeSuffix("\r\n\r\n").toString()
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
                    output.write(buffer, 0, read)
                    output.flush()
                }
            } catch (_: Exception) {
                // Socket closed: just end this pump.
            } finally {
                runCatching { from.close() }
                runCatching { to.close() }
            }
        }
    }

    fun stop() {
        synchronized(sockets) {
            sockets.forEach { runCatching { it.close() } }
            sockets.clear()
        }
        runCatching { server.close() }
    }

}
