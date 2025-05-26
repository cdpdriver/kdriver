package dev.kdriver.core.connection

import dev.kdriver.cdp.CDP
import dev.kdriver.cdp.domain.Target
import dev.kdriver.cdp.domain.target
import dev.kdriver.core.browser.BrowserTarget
import dev.kdriver.core.browser.parseWebSocketUrl
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class Connection(
    private val websocketUrl: String,
    override var target: Target.TargetInfo? = null,
) : BrowserTarget {

    private val cdpMutex = Mutex()
    private var _cdp: CDP? = null

    suspend fun cdp(isUpdate: Boolean = false): CDP {
        return _cdp ?: cdpMutex.withLock {
            _cdp ?: parseWebSocketUrl(websocketUrl).let {
                CDP.create(it.host, it.port, it.path)
            }.also {
                _cdp = it
            }
        }
    }

    fun close() {
        _cdp?.close()
        _cdp = null
    }

    suspend fun updateTarget() {
        val targetInfo = cdp(isUpdate = true).target.getTargetInfo(targetId)
        target = targetInfo.targetInfo
    }


    override fun toString(): String {
        return "Connection: ${target?.toString() ?: "no target"}"
    }

}
