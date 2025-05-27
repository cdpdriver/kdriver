package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Target

interface BrowserTarget {

    var targetInfo: Target.TargetInfo?

    val targetId: String?
        get() = targetInfo?.targetId
    val type: String?
        get() = targetInfo?.type

}
