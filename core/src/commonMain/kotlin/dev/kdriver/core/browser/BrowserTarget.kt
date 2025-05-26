package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Target

interface BrowserTarget {

    var target: Target.TargetInfo?

    val targetId: String?
        get() = target?.targetId
    val type: String?
        get() = target?.type

}
