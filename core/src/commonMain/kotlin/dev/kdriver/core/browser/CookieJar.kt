package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Network
import kotlinx.io.files.Path

/**
 * Gives access to the cookies of a [Browser], across every tab and window it owns.
 *
 * Cookies are handled at browser level rather than per tab, so reading or writing them through any
 * tab of the same browser yields the same jar.
 */
interface CookieJar {

    companion object {
        /**
         * Where [save] and [load] read and write when no path is given.
         */
        const val DEFAULT_SESSION_FILE: String = ".session.dat"
    }

    /**
     * Returns every cookie currently held by the browser.
     *
     * @throws IllegalStateException if the browser has not been started yet.
     */
    suspend fun getAll(): List<Network.Cookie>

    /**
     * Adds the given cookies to the browser, replacing any that already exist with the same name,
     * domain and path. Cookies absent from [cookies] are left untouched, use [clear] to drop them.
     *
     * @throws IllegalStateException if the browser has not been started yet.
     */
    suspend fun setAll(cookies: List<Network.CookieParam>)

    /**
     * Writes the cookies to [path] as JSON, so a later [load] can restore the session.
     *
     * @param path Where to write. Defaults to [DEFAULT_SESSION_FILE] in the working directory.
     * @param pattern Only cookies whose serialized form matches are saved. Defaults to all of them.
     *                For instance `Regex("(cf|\\.com|nowsecure)")` keeps the cookies carrying `cf`,
     *                `.com` or `nowsecure` in any of their fields.
     *
     * @return The cookies that were written, so the caller can tell what the pattern selected.
     *
     * @throws IllegalStateException if the browser has not been started yet.
     */
    suspend fun save(path: Path = Path(DEFAULT_SESSION_FILE), pattern: Regex = Regex(".*")): List<Network.Cookie>

    /**
     * Restores cookies previously written by [save] and hands them to the browser.
     *
     * @param path Where to read from. Defaults to [DEFAULT_SESSION_FILE] in the working directory.
     * @param pattern Only cookies whose serialized form matches are loaded. Defaults to all of them.
     *
     * @return The cookies that were restored.
     *
     * @throws IllegalStateException if the browser has not been started yet.
     */
    suspend fun load(path: Path = Path(DEFAULT_SESSION_FILE), pattern: Regex = Regex(".*")): List<Network.Cookie>

    /**
     * Removes every cookie from the browser, for all of its tabs and windows.
     *
     * @throws IllegalStateException if the browser has not been started yet.
     */
    suspend fun clear()

}
