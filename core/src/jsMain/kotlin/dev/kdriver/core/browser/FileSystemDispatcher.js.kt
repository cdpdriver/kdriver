package dev.kdriver.core.browser

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** JS cannot launch a browser, so it never deletes a profile: there is no I/O dispatcher there anyway. */
internal actual val fileSystemDispatcher: CoroutineDispatcher = Dispatchers.Default
