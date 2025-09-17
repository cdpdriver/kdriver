package dev.kdriver.core.network

/**
 * Represents a batch of request expectations, each defined by a URL pattern and its corresponding expectation.
 */
interface BatchRequestExpectation {

    /**
     * A map of URL patterns (as [Regex]) to their corresponding [RequestExpectation]s.
     */
    val expectations: Map<Regex, RequestExpectation>

    /**
     * Activate all expectations for the duration of [block]. Expectations are enabled concurrently
     * by nesting RequestExpectation.use calls, so all handlers are active while [block] executes.
     */
    suspend fun <T> use(block: suspend BatchRequestExpectation.() -> T): T

}
