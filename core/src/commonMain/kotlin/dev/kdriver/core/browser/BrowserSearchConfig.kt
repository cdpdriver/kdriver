package dev.kdriver.core.browser

import kotlinx.io.files.Path

/**
 * Browser search configuration flags
 */
data class BrowserSearchConfig(
    val pathSeparator: String,
    val searchInPath: Boolean = true,
    val searchMacosApplications: Boolean = false,
    val searchWindowsProgramFiles: Boolean = false,
    val searchLinuxCommonPaths: Boolean = false,
) {

    fun findBrowserExecutable(): Path? {
        return findChromeExecutable()
            ?: findOperaExecutable()
            ?: findBraveExecutable()
            ?: findEdgeExecutable()
    }

    fun findChromeExecutable(): Path? {
        return findBrowserExecutableCommon(
            executableNames = listOf(
                "google-chrome",
                "chromium",
                "chromium-browser",
                "chrome",
                "google-chrome-stable"
            ),
            macosAppPaths = listOf(
                "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                "/Applications/Chromium.app/Contents/MacOS/Chromium"
            ),
            linuxCommonPaths = listOf(
                "/usr/bin/google-chrome",
                "/usr/bin/chromium",
                "/usr/bin/chromium-browser",
                "/snap/bin/chromium",
                "/opt/google/chrome/chrome",
            ),
            windowsProgramFilesSuffixes = listOf(
                "Google/Chrome/Application",
                "Google/Chrome Beta/Application",
                "Google/Chrome Canary/Application",
                "Google/Chrome SxS/Application",
            ),
            windowsExecutableNames = listOf("chrome.exe"),
        )
    }

    fun findOperaExecutable(): Path? {
        return findBrowserExecutableCommon(
            executableNames = listOf("opera"),
            macosAppPaths = listOf(
                "/Applications/Opera.app/Contents/MacOS/Opera"
            ),
            linuxCommonPaths = listOf(
                "/usr/bin/opera",
                "/usr/local/bin/opera",
            ),
            windowsProgramFilesSuffixes = listOf(
                "Opera",
                "Programs/Opera"
            ),
            windowsExecutableNames = listOf("opera.exe"),
        )
    }

    fun findBraveExecutable(): Path? {
        return findBrowserExecutableCommon(
            executableNames = listOf("brave-browser", "brave"),
            macosAppPaths = listOf(
                "/Applications/Brave Browser.app/Contents/MacOS/Brave Browser"
            ),
            linuxCommonPaths = listOf(
                "/usr/bin/brave-browser",
                "/usr/bin/brave",
                "/snap/bin/brave",
            ),
            windowsProgramFilesSuffixes = listOf(
                "BraveSoftware/Brave-Browser/Application"
            ),
            windowsExecutableNames = listOf("brave.exe"),
        )
    }

    fun findEdgeExecutable(): Path? {
        return findBrowserExecutableCommon(
            executableNames = listOf(
                "microsoft-edge",
                "microsoft-edge-stable",
                "microsoft-edge-beta",
                "microsoft-edge-dev"
            ),
            macosAppPaths = listOf(
                "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge"
            ),
            linuxCommonPaths = listOf(
                "/usr/bin/microsoft-edge",
                "/usr/bin/microsoft-edge-stable",
            ),
            windowsProgramFilesSuffixes = listOf(
                "Microsoft/Edge/Application"
            ),
            windowsExecutableNames = listOf("msedge.exe"),
        )
    }

    /**
     * Common helper to search for browser executables based on platform configuration.
     *
     * @param executableNames List of executable names to search for (e.g., ["chrome", "google-chrome"])
     * @param macosAppPaths macOS .app bundle paths (only used if searchMacosApplications is true)
     * @param windowsProgramFilesSuffixes Windows Program Files subdirectories (only used if searchWindowsProgramFiles is true)
     * @param linuxCommonPaths Common Linux installation paths (only used if searchLinuxCommonPaths is true)
     * @param windowsExecutableNames Windows executable names with .exe extension
     *
     * @return The Path to the found executable, or null if not found.
     */
    private fun findBrowserExecutableCommon(
        executableNames: List<String>,
        macosAppPaths: List<String> = emptyList(),
        windowsProgramFilesSuffixes: List<String> = emptyList(),
        windowsExecutableNames: List<String> = emptyList(),
        linuxCommonPaths: List<String> = emptyList(),
    ): Path? {
        val candidates = mutableListOf<Path>()

        // macOS applications
        if (searchMacosApplications) {
            candidates.addAll(macosAppPaths.map { Path(it) })
        }

        // Windows Program Files
        if (searchWindowsProgramFiles) {
            val programFiles = listOfNotNull(
                getEnv("PROGRAMFILES"),
                getEnv("PROGRAMFILES(X86)"),
                getEnv("LOCALAPPDATA"),
                getEnv("PROGRAMW6432")
            )
            for (base in programFiles) {
                for (suffix in windowsProgramFilesSuffixes) {
                    for (exe in windowsExecutableNames) {
                        candidates.add(Path("$base/$suffix/$exe"))
                    }
                }
            }
        }

        // Linux common paths
        if (searchLinuxCommonPaths) {
            candidates.addAll(linuxCommonPaths.map { Path(it) })
        }

        // Search in PATH
        if (searchInPath) {
            val pathEnv = getEnv("PATH")
            val paths = pathEnv?.split(pathSeparator) ?: emptyList()
            for (pathDir in paths) {
                for (exe in executableNames + windowsExecutableNames) {
                    candidates.add(Path("$pathDir/$exe"))
                }
            }
        }

        // Return the shortest path that exists
        return candidates
            .filter { exists(it) }
            .minByOrNull { it.toString().length }
    }

}
