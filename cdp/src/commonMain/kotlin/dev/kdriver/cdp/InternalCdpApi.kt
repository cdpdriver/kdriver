package dev.kdriver.cdp

@RequiresOptIn(
    message = "This API is internal to the CDP library. We recommend using the public APIs instead.",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
annotation class InternalCdpApi
