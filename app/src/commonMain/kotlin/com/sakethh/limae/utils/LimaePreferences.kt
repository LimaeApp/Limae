package com.sakethh.limae.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sakethh.limae.domain.repository.PreferencesRepo
import com.sakethh.limae.platform.Platform
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KMutableProperty0

object LimaePreferences : KoinComponent {
    var useDarkTheme by mutableStateOf(true)
    var useSystemTheme by mutableStateOf(false)
    var useAmoledTheme by mutableStateOf(false)
    var useDynamicTheming by mutableStateOf(false)

    var autoSaveNotes by mutableStateOf(true)
    var useAutoExports by mutableStateOf(false)

    var accessibilityIconSize by mutableIntStateOf(45)

    var exportDirPath by mutableStateOf("")

    enum class Primitive {
        Int,
        String,
        Boolean,
    }

    /*
       compose does track this generally, but misses on startup (on Default dispatcher):
       (Key.USE_DARK_THEME.state as KMutableProperty0<Any?>).set(false)
     */
    enum class Key(
        val state: KMutableProperty0<*>,
        val stateType: Primitive,
    ) {
        USE_DARK_THEME(::useDarkTheme, Primitive.Boolean),
        USE_SYSTEM_THEME(::useSystemTheme, Primitive.Boolean),
        USE_AMOLED_THEME(::useAmoledTheme, Primitive.Boolean),
        USE_DYNAMIC_THEME(::useDynamicTheming, Primitive.Boolean),
        AUTO_SAVE_NOTE(::autoSaveNotes, Primitive.Boolean),
        USE_SNAPSHOTS(::useAutoExports, Primitive.Boolean),
        ACCESSIBILITY_ICON_SIZE(::accessibilityIconSize, Primitive.Int),
        EXPORT_DIR_PATH(::exportDirPath, Primitive.String),
    }

    private val preferencesRepo by inject<PreferencesRepo>()
    private var loadedPrefs = false

    suspend fun loadAll() {
        if (loadedPrefs) return

        val preferences = preferencesRepo.getAllPreferences()
        Key.entries.forEach { preference ->
            val preferenceKey =
                when (preference.stateType) {
                    Primitive.Int -> Platform.Preferences.Key.IntPreferencesKey(preference.name)
                    Primitive.String -> Platform.Preferences.Key.StringPreferencesKey(preference.name)
                    Primitive.Boolean -> Platform.Preferences.Key.BooleanPreferencesKey(preference.name)
                }

            val persistedValue = preferences[preferenceKey] ?: return@forEach

            (preference.state as KMutableProperty0<Any?>).set(persistedValue)
        }
        loadedPrefs = true
    }
}
