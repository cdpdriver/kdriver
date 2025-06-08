package dev.kdriver.cdp.domain

import dev.kaccelero.serializers.Serialization
import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.Domain
import dev.kdriver.cdp.cacheGeneratedDomain
import dev.kdriver.cdp.getGeneratedDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

public val CDP.log: Log
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Log(this))

/**
 * Provides access to log entries.
 */
public class Log(
    private val cdp: CDP,
) : Domain {
    /**
     * Issued when new message was logged.
     */
    public val entryAdded: Flow<EntryAddedParameter> = cdp
        .events
        .filter { it.method == "Log.entryAdded" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Clears the log.
     */
    public suspend fun clear() {
        val parameter = null
        cdp.callCommand("Log.clear", parameter)
    }

    /**
     * Disables log domain, prevents further log entries from being reported to the client.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Log.disable", parameter)
    }

    /**
     * Enables log domain, sends the entries collected so far to the client by means of the
     * `entryAdded` notification.
     */
    public suspend fun enable() {
        val parameter = null
        cdp.callCommand("Log.enable", parameter)
    }

    /**
     * start violation reporting.
     */
    public suspend fun startViolationsReport(args: StartViolationsReportParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Log.startViolationsReport", parameter)
    }

    /**
     * start violation reporting.
     */
    public suspend fun startViolationsReport(config: List<ViolationSetting>) {
        val parameter = StartViolationsReportParameter(config = config)
        startViolationsReport(parameter)
    }

    /**
     * Stop violation reporting.
     */
    public suspend fun stopViolationsReport() {
        val parameter = null
        cdp.callCommand("Log.stopViolationsReport", parameter)
    }

    /**
     * Log entry.
     */
    @Serializable
    public data class LogEntry(
        /**
         * Log entry source.
         */
        public val source: String,
        /**
         * Log entry severity.
         */
        public val level: String,
        /**
         * Logged text.
         */
        public val text: String,
        public val category: String? = null,
        /**
         * Timestamp when this entry was added.
         */
        public val timestamp: Double,
        /**
         * URL of the resource if known.
         */
        public val url: String? = null,
        /**
         * Line number in the resource.
         */
        public val lineNumber: Int? = null,
        /**
         * JavaScript stack trace.
         */
        public val stackTrace: Runtime.StackTrace? = null,
        /**
         * Identifier of the network request associated with this entry.
         */
        public val networkRequestId: String? = null,
        /**
         * Identifier of the worker associated with this entry.
         */
        public val workerId: String? = null,
        /**
         * Call arguments.
         */
        public val args: List<Runtime.RemoteObject>? = null,
    )

    /**
     * Violation configuration setting.
     */
    @Serializable
    public data class ViolationSetting(
        /**
         * Violation type.
         */
        public val name: String,
        /**
         * Time threshold to trigger upon.
         */
        public val threshold: Double,
    )

    /**
     * Issued when new message was logged.
     */
    @Serializable
    public data class EntryAddedParameter(
        /**
         * The entry.
         */
        public val entry: LogEntry,
    )

    @Serializable
    public data class StartViolationsReportParameter(
        /**
         * Configuration for violations.
         */
        public val config: List<ViolationSetting>,
    )
}
