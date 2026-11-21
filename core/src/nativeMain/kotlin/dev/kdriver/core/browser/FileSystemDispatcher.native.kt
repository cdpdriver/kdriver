package dev.kdriver.core.browser

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual val fileSystemDispatcher: CoroutineDispatcher = Dispatchers.IO
