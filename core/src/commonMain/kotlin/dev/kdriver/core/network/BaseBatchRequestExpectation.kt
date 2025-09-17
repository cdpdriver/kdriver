package dev.kdriver.core.network

import dev.kdriver.core.tab.Tab

/**
 * Batch expectation built on top of BaseRequestExpectation instances.
 */
class BaseBatchRequestExpectation(
    tab: Tab,
    urlPatterns: List<Regex>,
) : BatchRequestExpectation {

    override val expectations: Map<Regex, RequestExpectation> =
        urlPatterns.associateWith { pattern -> BaseRequestExpectation(tab, pattern) }

    override suspend fun <T> use(block: suspend BatchRequestExpectation.() -> T): T {
        val exps = expectations.values.toList()
        suspend fun <T> nest(index: Int, run: suspend BatchRequestExpectation.() -> T): T {
            return if (index >= exps.size) run(this) else exps[index].use { nest(index + 1, run) }
        }
        return nest(0, block)
    }

}
