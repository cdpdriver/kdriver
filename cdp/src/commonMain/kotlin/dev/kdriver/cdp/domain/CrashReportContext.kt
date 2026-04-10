@file:Suppress("ALL")

package dev.kdriver.cdp.domain

import dev.kdriver.cdp.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * This domain exposes the current state of the CrashReportContext API.
 */
public val CDP.crashReportContext: CrashReportContext
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(CrashReportContext(this))

/**
 * This domain exposes the current state of the CrashReportContext API.
 */
public class CrashReportContext(
    private val cdp: CDP,
) : Domain {
    /**
     * Returns all entries in the CrashReportContext across all frames in the page.
     */
    public suspend fun getEntries(mode: CommandMode = CommandMode.DEFAULT): GetEntriesReturn {
        val parameter = null
        val result = cdp.callCommand("CrashReportContext.getEntries", parameter, mode)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Key-value pair in CrashReportContext.
     */
    @Serializable
    public data class CrashReportContextEntry(
        public val key: String,
        public val `value`: String,
        /**
         * The ID of the frame where the key-value pair was set.
         */
        public val frameId: String,
    )

    @Serializable
    public data class GetEntriesReturn(
        public val entries: List<CrashReportContextEntry>,
    )
}
