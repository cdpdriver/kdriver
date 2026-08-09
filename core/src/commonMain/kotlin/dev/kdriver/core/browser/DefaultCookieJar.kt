package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Network
import dev.kdriver.cdp.domain.storage
import dev.kdriver.core.connection.Connection
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.json.Json

/**
 * Default [CookieJar], talking to the browser over its own connection.
 *
 * Cookies live at browser level, so every call goes through [Browser.connection] rather than through
 * one of the tabs.
 */
open class DefaultCookieJar(
    private val browser: Browser,
) : CookieJar {

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    private fun connection(): Connection = browser.connection
        ?: error("Browser not yet started. Call start() first")

    override suspend fun getAll(): List<Network.Cookie> =
        connection().storage.getCookies().cookies

    override suspend fun setAll(cookies: List<Network.CookieParam>) {
        if (cookies.isEmpty()) return
        connection().storage.setCookies(cookies)
    }

    override suspend fun save(path: Path, pattern: Regex): List<Network.Cookie> {
        val selected = getAll().filter { pattern.containsMatchIn(json.encodeToString(it)) }
        SystemFileSystem.sink(path).buffered().use { it.writeString(json.encodeToString(selected)) }
        return selected
    }

    override suspend fun load(path: Path, pattern: Regex): List<Network.Cookie> {
        val content = SystemFileSystem.source(path).buffered().use { it.readString() }
        val selected = json.decodeFromString<List<Network.Cookie>>(content)
            .filter { pattern.containsMatchIn(json.encodeToString(it)) }
        setAll(selected.map { it.toParam() })
        return selected
    }

    override suspend fun clear() {
        connection().storage.clearCookies()
    }

}

/**
 * Turns a cookie read from the browser into the shape needed to write it back.
 *
 * `size` and `session` are dropped on purpose: both are derived by the browser, and `session` is
 * simply the absence of an expiry, which [Network.CookieParam.expires] already carries.
 */
internal fun Network.Cookie.toParam(): Network.CookieParam = Network.CookieParam(
    name = name,
    value = value,
    domain = domain,
    path = path,
    secure = secure,
    httpOnly = httpOnly,
    sameSite = sameSite,
    expires = expires.takeUnless { session },
    priority = priority,
    sourceScheme = sourceScheme,
    sourcePort = sourcePort,
    partitionKey = partitionKey,
)
