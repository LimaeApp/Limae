package com.sakethh.limae.platform

interface Platform {
    val version: Int?
    val type: Type

    enum class Type {
        AndroidMobile,
        AndroidTablet,
        Desktop,
        Web,
    }

    interface Actions {
        fun openAccessibilitySettings()
    }
}
