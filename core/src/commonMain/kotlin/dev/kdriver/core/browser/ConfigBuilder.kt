package dev.kdriver.core.browser

import dev.kdriver.core.browser.Config.Defaults
import kotlinx.io.files.Path

/**
 * Builder class for creating Config instances using Kotlin DSL syntax.
 *
 * Example usage:
 * ```
 * val config = config {
 *     headless = true
 *     userAgent = "Custom Agent"
 *     sandbox = false
 * }
 * ```
 */
class ConfigBuilder {

    var userDataDir: Path? = null
    var headless: Boolean = Defaults.HEADLESS
    var userAgent: String? = null
    var browserExecutablePath: Path? = null
    var browserArgs: List<String>? = null
    var sandbox: Boolean = Defaults.SANDBOX
    var lang: String? = null
    var host: String? = null
    var port: Int? = null
    var expert: Boolean = Defaults.EXPERT
    var browserConnectionTimeout: Long = Defaults.BROWSER_CONNECTION_TIMEOUT
    var browserConnectionMaxTries: Int = Defaults.BROWSER_CONNECTION_MAX_TRIES
    var autoDiscoverTargets: Boolean = Defaults.AUTO_DISCOVER_TARGETS

    /**
     * Builds the Config instance with the configured parameters.
     */
    fun build(): Config {
        return Config(
            userDataDir = userDataDir,
            headless = headless,
            userAgent = userAgent,
            browserExecutablePath = browserExecutablePath,
            browserArgs = browserArgs,
            sandbox = sandbox,
            lang = lang,
            host = host,
            port = port,
            expert = expert,
            browserConnectionTimeout = browserConnectionTimeout,
            browserConnectionMaxTries = browserConnectionMaxTries,
            autoDiscoverTargets = autoDiscoverTargets,
        )
    }
}
