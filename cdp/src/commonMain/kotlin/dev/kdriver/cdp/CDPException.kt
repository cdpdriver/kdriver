package dev.kdriver.cdp

/***
 * An error returned from the browser.
 */
class CDPException(
    val method: String,
    val code: Int,
    val originalMessage: String,
    val data: String?,
) : Exception(
    "Error while calling a command $method: $originalMessage${data?.let { "($it)" } ?: ""} (code: $code)"
)
