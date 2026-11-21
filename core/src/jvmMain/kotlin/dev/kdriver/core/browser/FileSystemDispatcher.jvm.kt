package dev.kdriver.core.browser

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual val fileSystemDispatcher: CoroutineDispatcher = Dispatchers.IO
