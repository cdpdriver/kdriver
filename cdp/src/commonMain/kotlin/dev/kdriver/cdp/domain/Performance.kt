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

public val CDP.performance: Performance
    get() = getGeneratedDomain() ?: cacheGeneratedDomain(Performance(this))

public class Performance(
    private val cdp: CDP,
) : Domain {
    /**
     * Current values of the metrics.
     */
    public val metrics: Flow<MetricsParameter> = cdp
        .events
        .filter { it.method == "Performance.metrics" }
        .map { it.params }
        .filterNotNull()
        .map { Serialization.json.decodeFromJsonElement(it) }

    /**
     * Disable collecting and reporting metrics.
     */
    public suspend fun disable() {
        val parameter = null
        cdp.callCommand("Performance.disable", parameter)
    }

    /**
     * Enable collecting and reporting metrics.
     */
    public suspend fun enable(args: EnableParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Performance.enable", parameter)
    }

    /**
     * Enable collecting and reporting metrics.
     *
     * @param timeDomain Time domain to use for collecting and reporting duration metrics.
     */
    public suspend fun enable(timeDomain: String? = null) {
        val parameter = EnableParameter(timeDomain = timeDomain)
        enable(parameter)
    }

    /**
     * Sets time domain to use for collecting and reporting duration metrics.
     * Note that this must be called before enabling metrics collection. Calling
     * this method while metrics collection is enabled returns an error.
     */
    @Deprecated(message = "")
    public suspend fun setTimeDomain(args: SetTimeDomainParameter) {
        val parameter = Serialization.json.encodeToJsonElement(args)
        cdp.callCommand("Performance.setTimeDomain", parameter)
    }

    /**
     * Sets time domain to use for collecting and reporting duration metrics.
     * Note that this must be called before enabling metrics collection. Calling
     * this method while metrics collection is enabled returns an error.
     *
     * @param timeDomain Time domain
     */
    @Deprecated(message = "")
    public suspend fun setTimeDomain(timeDomain: String) {
        val parameter = SetTimeDomainParameter(timeDomain = timeDomain)
        setTimeDomain(parameter)
    }

    /**
     * Retrieve current values of run-time metrics.
     */
    public suspend fun getMetrics(): GetMetricsReturn {
        val parameter = null
        val result = cdp.callCommand("Performance.getMetrics", parameter)
        return result!!.let { Serialization.json.decodeFromJsonElement(it) }
    }

    /**
     * Run-time execution metric.
     */
    @Serializable
    public data class Metric(
        /**
         * Metric name.
         */
        public val name: String,
        /**
         * Metric value.
         */
        public val `value`: Double,
    )

    /**
     * Current values of the metrics.
     */
    @Serializable
    public data class MetricsParameter(
        /**
         * Current values of the metrics.
         */
        public val metrics: List<Metric>,
        /**
         * Timestamp title.
         */
        public val title: String,
    )

    @Serializable
    public data class EnableParameter(
        /**
         * Time domain to use for collecting and reporting duration metrics.
         */
        public val timeDomain: String? = null,
    )

    @Serializable
    public data class SetTimeDomainParameter(
        /**
         * Time domain
         */
        public val timeDomain: String,
    )

    @Serializable
    public data class GetMetricsReturn(
        /**
         * Current values for run-time metrics.
         */
        public val metrics: List<Metric>,
    )
}
