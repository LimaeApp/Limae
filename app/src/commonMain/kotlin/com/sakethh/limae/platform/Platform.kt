package com.sakethh.limae.platform

import androidx.compose.runtime.Stable
import com.sakethh.limae.domain.ExportType
import com.sakethh.limae.domain.Result

interface Platform {
    val version: Int?
    val type: Type

    enum class Type {
        AndroidMobile,
        AndroidTablet,
        Desktop,
        Web,
    }

    /**
     Instead of using expect/actual, i'm implementing these stuff purely via DI,
     just because I can.

     Although event-driven implementation(s) can be done with kotlin flows and expect/actual (i did that in Linkora),
     i want to do it like _this_ with Limae.
     */
    interface Actions {
        fun openAccessibilitySettings()

        suspend fun pickADirectory(): String?

        @Stable
        data class InstalledApp(
            val name: String,
            val packageName: String,
        )

        suspend fun getInstalledApps(): List<InstalledApp> = emptyList()

        suspend fun exportData(
            exportType: ExportType,
            dirPath: String,
            content: String,
        ): Result<Unit>

        suspend fun importData(): Result<String?>
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
