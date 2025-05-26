package dev.kdriver.cdp

/***
 * An error returned from the browser.
 */
class CDPErrorException(
    val code: Int,
    val originalMessage: String,
    val data: String?,
) : Exception("Error while calling a command: $originalMessage${data?.let { "($it)" } ?: ""} (code: $code)")
