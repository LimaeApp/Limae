package com.sakethh.limae.platform

import androidx.compose.runtime.Stable

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

        @Stable
        data class InstalledApp(
            val name: String,
            val packageName: String,
        )

        suspend fun getInstalledApps(): List<InstalledApp> = emptyList()
    }
}
