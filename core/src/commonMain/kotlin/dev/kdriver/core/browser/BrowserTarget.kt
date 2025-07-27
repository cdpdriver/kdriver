package dev.kdriver.core.browser

import dev.kdriver.cdp.domain.Target

interface BrowserTarget {

    var targetInfo: Target.TargetInfo?

    val targetId: String?
        get() = targetInfo?.targetId

    val type: String?
        get() = targetInfo?.type

    val title: String?
        get() = targetInfo?.title

    val url: String?
        get() = targetInfo?.url

    val attached: Boolean?
        get() = targetInfo?.attached

    val openerId: String?
        get() = targetInfo?.openerId

    val canAccessOpener: Boolean?
        get() = targetInfo?.canAccessOpener

    val openerFrameId: String?
        get() = targetInfo?.openerFrameId

    val browserContextId: String?
        get() = targetInfo?.browserContextId

    val subtype: String?
        get() = targetInfo?.subtype

}
