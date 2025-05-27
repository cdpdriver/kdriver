package dev.kdriver.core.browser

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.ServerSocket
import java.net.URI

fun <T> filterRecurse(
    node: T,
    predicate: (T) -> Boolean,
    getChildren: (T) -> List<T>?,
    getShadowRoots: (T) -> List<T>?,
): T? {
    val children = getChildren(node) ?: return null

    for (child in children) {
        if (predicate(child)) {
            return child
        }

        val shadowRoots = getShadowRoots(child)
        if (shadowRoots != null && shadowRoots.isNotEmpty()) {
            val shadowResult = filterRecurse(shadowRoots[0], predicate, getChildren, getShadowRoots)
            if (shadowResult != null) {
                return shadowResult
            }
        }

        val recursiveResult = filterRecurse(child, predicate, getChildren, getShadowRoots)
        if (recursiveResult != null) {
            return recursiveResult
        }
    }
    return null
}


fun parseWebSocketUrl(url: String): WebSocketInfo {
    val uri = URI(url)

    val host = uri.host
    val port = if (uri.port != -1) uri.port else when (uri.scheme) {
        "ws" -> 80
        "wss" -> 443
        else -> throw IllegalArgumentException("Unsupported scheme: ${uri.scheme}")
    }
    val path = uri.rawPath ?: "/"

    return WebSocketInfo(host, port, path)
}

fun freePort(): Int {
    ServerSocket(0, 5, InetAddress.getByName("127.0.0.1")).use { socket ->
        return socket.localPort
    }
}

suspend fun startProcess(
    exe: String,
    params: List<String>,
    isPosix: Boolean,
): Process {
    return withContext(Dispatchers.IO) {
        val command = listOf(exe) + params
        val builder = ProcessBuilder(command)
        builder.redirectInput(ProcessBuilder.Redirect.PIPE)
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE)
        builder.redirectError(ProcessBuilder.Redirect.PIPE)

        if (isPosix) {
            builder.redirectErrorStream(false)
        }

        val process = builder.start()
        process
    }
}

