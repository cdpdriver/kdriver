package dev.kdriver.core.browser

import kotlinx.io.files.Path

/**
 * Builder class for creating Config instances using Kotlin DSL syntax.
 * ```
 */
class ConfigBuilder {

    var userDataDir: Path? = null
    var headless: Boolean = false
    var userAgent: String? = null
    var browserExecutablePath: Path? = null
    var browserArgs: List<String>? = null
    var sandbox: Boolean = true
    var lang: String? = null
    var host: String? = null
    var port: Int? = null
    var expert: Boolean = false
    var browserConnectionTimeout: Long = 500
    var browserConnectionMaxTries: Int = 60
    var autoDiscoverTargets: Boolean = true

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