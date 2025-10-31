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

fun filterRecurseAll(node: DOM.Node, predicate: (DOM.Node) -> Boolean): List<DOM.Node> {
    val children = node.children ?: return emptyList()
    val out = mutableListOf<DOM.Node>()
    for (child in children) {
        if (predicate(child)) {
            out.add(child)
        }
        val shadowRoots = child.shadowRoots
        if (shadowRoots != null && shadowRoots.isNotEmpty()) {
            out.addAll(filterRecurseAll(shadowRoots[0], predicate))
        }
        out.addAll(filterRecurseAll(child, predicate))
    }
    return out
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

fun isZstdCompressed(data: ByteArray): Boolean {
    val header = data.take(4).map { it.toUByte().toInt() }
    return header == listOf(0x28, 0xB5, 0x2F, 0xFD)
}

fun isGzipCompressed(data: ByteArray): Boolean {
    val header = data.take(2).map { it.toUByte().toInt() }
    return header == listOf(0x1F, 0x8B)
}

/**
 * Browser search configuration flags
 */
data class BrowserSearchConfig(
    val searchInPath: Boolean = true,
    val searchMacosApplications: Boolean = false,
    val searchWindowsProgramFiles: Boolean = false,
    val searchLinuxCommonPaths: Boolean = false,
)

/**
 * Common helper to search for browser executables based on platform configuration.
 * @param config Platform-specific search configuration
 * @param pathSeparator The path separator character (":" for POSIX, ";" for Windows)
 * @param pathEnv The PATH environment variable value
 * @param executableNames List of executable names to search for (e.g., ["chrome", "google-chrome"])
 * @param macosAppPaths macOS .app bundle paths (only used if searchMacosApplications is true)
 * @param windowsProgramFilesSuffixes Windows Program Files subdirectories (only used if searchWindowsProgramFiles is true)
 * @param linuxCommonPaths Common Linux installation paths (only used if searchLinuxCommonPaths is true)
 * @param windowsExecutableNames Windows executable names with .exe extension
 * @param windowsProgramFilesGetter Callback to get Windows program files directories
 */
internal fun findBrowserExecutableCommon(
    config: BrowserSearchConfig,
    pathSeparator: String,
    pathEnv: String?,
    executableNames: List<String>,
    macosAppPaths: List<String> = emptyList(),
    windowsProgramFilesSuffixes: List<String> = emptyList(),
    windowsExecutableNames: List<String> = emptyList(),
    linuxCommonPaths: List<String> = emptyList(),
    windowsProgramFilesGetter: () -> List<String> = { emptyList() },
): Path? {
    val candidates = mutableListOf<Path>()

    // macOS applications
    if (config.searchMacosApplications) {
        candidates.addAll(macosAppPaths.map { Path(it) })
    }

    // Windows Program Files
    if (config.searchWindowsProgramFiles) {
        val programFiles = windowsProgramFilesGetter()
        for (base in programFiles) {
            for (suffix in windowsProgramFilesSuffixes) {
                for (exe in windowsExecutableNames) {
                    candidates.add(Path("$base/$suffix/$exe"))
                }
            }
        }
    }

    // Linux common paths
    if (config.searchLinuxCommonPaths) {
        candidates.addAll(linuxCommonPaths.map { Path(it) })
    }

    // Search in PATH
    if (config.searchInPath) {
        val paths = pathEnv?.split(pathSeparator) ?: emptyList()
        for (pathDir in paths) {
            for (exe in executableNames) {
                candidates.add(Path("$pathDir/$exe"))
            }
        }
    }

    // Return the shortest path that exists
    return candidates
        .filter { exists(it) }
        .minByOrNull { it.toString().length }
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
expect fun getEnv(name: String): String?
expect fun findChromeExecutable(): Path?
expect fun findOperaExecutable(): Path?
expect fun findBraveExecutable(): Path?
expect fun findEdgeExecutable(): Path?
expect fun freePort(): Int?
expect fun decompressIfNeeded(data: ByteArray): ByteArray
