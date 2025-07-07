package dev.kdriver.cdp

val CDP.cdp: CDP
    get() = this

inline fun <reified T : Domain> CDP.getGeneratedDomain(): T? =
    if (generatedDomains.containsKey(T::class)) {
        generatedDomains[T::class] as T
    } else null

inline fun <reified T : Domain> CDP.cacheGeneratedDomain(domain: T): T {
    generatedDomains[T::class] = domain
    return domain
}
