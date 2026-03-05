package com.sakethh.limae.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.sakethh.limae.domain.SuggestionEngine
import com.sakethh.limae.domain.repository.PreferencesRepo
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.platform
import com.sakethh.limae.ui.LimaeAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KMutableProperty0

object LimaePreferences : KoinComponent {
    var useDarkTheme by mutableStateOf(
        !(Platform.onAndroid),
    )

    var useSystemTheme by mutableStateOf(Platform.onAndroid)

    var useAmoledTheme by mutableStateOf(false)

    var useDynamicTheming by mutableStateOf(
        Platform.onAndroid && platform.version?.run {
            this >= 31
        } != null,
    )

    var autoSaveNotes by mutableStateOf(true)
    var useAutoExports by mutableStateOf(false)

    var accessibilityIconSize by mutableIntStateOf(45)
    var exportDirPath by mutableStateOf("")

    var optedEngines =
        mutableStateListOf<SuggestionEngine>().also {
            if (platform.type == Platform.Type.Web || Platform.onAndroid) {
                it.add(SuggestionEngine.Harper)
            } else {
                it.addAll(SuggestionEngine.entries)
            }
        }
        private set

    private val preferencesRepo by inject<PreferencesRepo>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        coroutineScope.launch {
            optedEngines.clear()

            var optedEngines =
                preferencesRepo
                    .getPreferenceValue(
                        preferenceKey =
                            Platform.Preferences.Key.StringPreferencesKey(
                                key = Key.OPTED_ENGINES.name,
                            ),
                    )?.run {
                        LimaeJson.decodeFromString<List<String>>(this)
                    }?.map {
                        SuggestionEngine.valueOf(it)
                    }

            if (optedEngines == null || LimaePreferences.optedEngines.isEmpty()) {
                val enginesList = buildList {
                    add(SuggestionEngine.Harper)
                    if (Platform.onDesktop) {
                        add(SuggestionEngine.LanguageTool)
                    }
                }
                LimaePreferences.optedEngines.addAll(enginesList)
                optedEngines = enginesList
                updateOptedInEnginesOnDisk()
            }

            LimaePreferences.optedEngines.addAll(optedEngines)

            launch {

            }
        }
    }

    private suspend fun updateOptedInEnginesOnDisk() {
        preferencesRepo.writePreferenceValue(
            preferenceKey =
                Platform.Preferences.Key.StringPreferencesKey(
                    key = Key.OPTED_ENGINES.name,
                ),
            newValue =
                LimaeJson.encodeToString(
                    optedEngines.map {
                        it.name
                    },
                ),
        )
    }

    private var optedEngineJob: Job? = null

    fun toggleOptedEngine(suggestionEngine: SuggestionEngine) {
        var showMinOptInSnackbar: Boolean
        optedEngineJob?.cancel()

        if (!optedEngines.contains(suggestionEngine)) {
            optedEngines.add(suggestionEngine)
            showMinOptInSnackbar = false
        } else {
            if (optedEngines.size > 1) {
                optedEngines.remove(suggestionEngine)
                showMinOptInSnackbar = false
            } else {
                showMinOptInSnackbar = true
            }
        }

        optedEngineJob = coroutineScope.launch {
            if (showMinOptInSnackbar) {
                LimaeAction.reportMessage("You must opt in at least one engine.")
            }
            updateOptedInEnginesOnDisk()
        }
    }

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
        val state: KMutableProperty0<*>?,
        val stateType: Primitive?,
    ) {
        USE_DARK_THEME(::useDarkTheme, Primitive.Boolean),
        USE_SYSTEM_THEME(
            ::useSystemTheme,
            Primitive.Boolean,
        ),
        USE_AMOLED_THEME(
            ::useAmoledTheme,
            Primitive.Boolean,
        ),
        USE_DYNAMIC_THEME(::useDynamicTheming, Primitive.Boolean),
        AUTO_SAVE_NOTE(
            ::autoSaveNotes,
            Primitive.Boolean,
        ),
        USE_SNAPSHOTS(
            ::useAutoExports,
            Primitive.Boolean,
        ),
        ACCESSIBILITY_ICON_SIZE(
            ::accessibilityIconSize,
            Primitive.Int,
        ),
        EXPORT_DIR_PATH(::exportDirPath, Primitive.String),
        BLOCK_ENABLE_ACCESSIBILITY_POPUP(
            null,
            null,
        ),
        SHOW_ONBOARDING(null, null),
        OPTED_ENGINES(null, null),
    }

    private var loadedPrefs = false

    suspend fun loadAll() {
        if (loadedPrefs) return

        val preferences = preferencesRepo.getAllPreferences()
        Key.entries.forEach { preference ->
            if (preference.stateType == null || preference.state == null) return@forEach

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
