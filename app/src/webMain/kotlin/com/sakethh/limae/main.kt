package com.sakethh.limae

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.sakethh.limae.di.initializeKoin
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.ui.Limae
import com.sakethh.limae.ui.theme.LimaeTheme
import com.sakethh.limae.utils.LimaePreferences
import kotlinx.browser.localStorage
import org.koin.dsl.bind
import org.koin.dsl.module
import org.w3c.dom.get
import org.w3c.dom.set

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
    initializeKoin {
        modules(
            module {
                single {
                    object : Platform.Actions {
                        override fun openAccessibilitySettings() = Unit

                        override suspend fun exportData(content: String) = Unit

                        override suspend fun importData(): String = ""
                    }
                }.bind<Platform.Actions>()
            },
            module {
                single {
                    object : Platform.Preferences {
                        override suspend fun <T> writePreferenceValue(
                            preferenceKey: Platform.Preferences.Key<T>,
                            newValue: T,
                        ) {
                            when (preferenceKey) {
                                is Platform.Preferences.Key.BooleanPreferencesKey -> {
                                    localStorage[preferenceKey.key] =
                                        (newValue as Boolean).toString()
                                }

                                is Platform.Preferences.Key.IntPreferencesKey -> {
                                    localStorage[preferenceKey.key] = (newValue as Int).toString()
                                }

                                is Platform.Preferences.Key.StringPreferencesKey -> {
                                    localStorage[preferenceKey.key] = newValue as String
                                }
                            }
                        }

                        override suspend fun <T> getPreferenceValue(preferenceKey: Platform.Preferences.Key<T>): T? =
                            when (preferenceKey) {
                                is Platform.Preferences.Key.BooleanPreferencesKey -> {
                                    localStorage[preferenceKey.key]
                                }

                                is Platform.Preferences.Key.IntPreferencesKey -> {
                                    localStorage[preferenceKey.key]
                                }

                                is Platform.Preferences.Key.StringPreferencesKey -> {
                                    localStorage[preferenceKey.key]
                                }
                            } as T?

                        override suspend fun getAllPreferences(): Map<Platform.Preferences.Key<*>, *> =
                            LimaePreferences.Key.entries
                                .mapNotNull { prefKeyEntry ->
                                    localStorage[prefKeyEntry.name]?.let { localValue ->
                                        when (prefKeyEntry.stateType) {
                                            LimaePreferences.Primitive.Int -> {
                                                Platform.Preferences.Key.IntPreferencesKey(
                                                    prefKeyEntry.name,
                                                ) to localValue
                                            }

                                            LimaePreferences.Primitive.String -> {
                                                Platform.Preferences.Key.StringPreferencesKey(
                                                    prefKeyEntry.name,
                                                ) to localValue
                                            }

                                            LimaePreferences.Primitive.Boolean -> {
                                                Platform.Preferences.Key.BooleanPreferencesKey(
                                                    prefKeyEntry.name,
                                                ) to localValue
                                            }
                                        }
                                    }
                                }.toMap()
                    }
                }.bind<Platform.Preferences>()
            },
        )
    }
    ComposeViewport {
        LimaeTheme {
            Limae()
        }
    }
}
