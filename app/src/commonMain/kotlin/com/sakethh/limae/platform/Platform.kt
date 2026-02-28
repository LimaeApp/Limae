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

    /** Should only be used to inject context-sensitive functionality via implementation,
     i.e., when it requires properties that aren't usually available through expect/actual implementations.
     */
    interface Actions {
        fun openAccessibilitySettings()

        @Stable
        data class InstalledApp(
            val name: String,
            val packageName: String,
        )

        suspend fun getInstalledApps(): List<InstalledApp> = emptyList()

        suspend fun exportData(content: String)

        suspend fun importData(): String
    }

    interface Preferences {
        suspend fun <T> writePreferenceValue(
            preferenceKey: Key<T>,
            newValue: T,
        )

        suspend fun <T> getPreferenceValue(preferenceKey: Key<T>): T?

        suspend fun getAllPreferences(): Map<Key<*>, *>

        sealed interface Key<T> {
            data class BooleanPreferencesKey(
                val key: String,
            ) : Key<Boolean>

            data class IntPreferencesKey(
                val key: String,
            ) : Key<Int>

            data class StringPreferencesKey(
                val key: String,
            ) : Key<String>
        }
    }
}
