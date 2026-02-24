package com.sakethh.limae.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakethh.limae.domain.repository.PreferencesRepo
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KMutableProperty0

object LimaePreferences : KoinComponent {
    var useDarkTheme by mutableStateOf(true)
    var useSystemTheme by mutableStateOf(false)
    var useAmoledTheme by mutableStateOf(false)
    var useDynamicTheming by mutableStateOf(false)

    var autoSaveNotes by mutableStateOf(true)

    enum class Primitives {
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
        val stateType: Primitives,
    ) {
        USE_DARK_THEME(::useDarkTheme, Primitives.Boolean),
        USE_SYSTEM_THEME(::useSystemTheme, Primitives.Boolean),
        USE_AMOLED_THEME(::useAmoledTheme, Primitives.Boolean),
        USE_DYNAMIC_THEME(::useDynamicTheming, Primitives.Boolean),
        AUTO_SAVE_NOTE(::autoSaveNotes, Primitives.Boolean),
    }

    private val preferencesRepo by inject<PreferencesRepo>()
    private var loadedPrefs = false

    suspend fun loadAll() {
        if (loadedPrefs) return

        val preferences = preferencesRepo.getAllPreferences()
        Key.entries.forEach { preference ->
            val preferenceKey =
                when (preference.stateType) {
                    Primitives.Int -> intPreferencesKey(preference.name)
                    Primitives.String -> stringPreferencesKey(preference.name)
                    Primitives.Boolean -> booleanPreferencesKey(preference.name)
                }

            val persistedValue = preferences[preferenceKey] ?: return@forEach

            (preference.state as KMutableProperty0<Any?>).set(persistedValue)
        }
        loadedPrefs = true
    }
}
