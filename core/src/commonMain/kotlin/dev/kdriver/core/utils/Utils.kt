package dev.kdriver.core.utils

import dev.kdriver.cdp.domain.DOM
import dev.kdriver.core.browser.WebSocketInfo
import io.ktor.http.*
import kotlinx.io.files.Path

fun filterRecurse(node: DOM.Node, predicate: (DOM.Node) -> Boolean): DOM.Node? {
    val children = node.children ?: return null
    for (child in children) {
        if (predicate(child)) return child

        val shadowRoots = child.shadowRoots
        if (shadowRoots != null && shadowRoots.isNotEmpty()) {
            val shadowResult = filterRecurse(shadowRoots[0], predicate)
            if (shadowResult != null) return shadowResult
        }

        val recursiveResult = filterRecurse(child, predicate)
        if (recursiveResult != null) return recursiveResult
    }
    return null
}

fun parseWebSocketUrl(url: String): WebSocketInfo {
    val uri = Url(url)

    val host = uri.host
    val port = if (uri.port != -1) uri.port else when (uri.protocol) {
        URLProtocol.WS -> 80
        URLProtocol.WSS -> 443
        else -> throw IllegalArgumentException("Unsupported scheme: ${uri.protocol}")
    }
    val path = uri.encodedPath

    return WebSocketInfo(host, port, path)
}

expect abstract class Process {
    fun isAlive(): Boolean
    fun pid(): Long
    abstract fun destroy()
}

expect suspend fun startProcess(exe: Path, params: List<String>): Process
expect fun addShutdownHook(hook: suspend () -> Unit)
expect fun isPosix(): Boolean
expect fun isRoot(): Boolean
expect fun tempProfileDir(): Path
expect fun exists(path: Path): Boolean
expect fun findChromeExecutable(): Path?
expect fun freePort(): Int?
