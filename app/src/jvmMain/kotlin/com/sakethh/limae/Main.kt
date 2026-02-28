package com.sakethh.limae

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakethh.limae.di.initializeKoin
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.limaeSpecificFolder
import com.sakethh.limae.ui.Limae
import com.sakethh.limae.ui.common.KeyEventTunnel
import com.sakethh.limae.ui.theme.LimaeTheme
import com.sakethh.limae.utils.Constants
import com.sakethh.limae.utils.LimaePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import org.koin.dsl.bind
import org.koin.dsl.module
import java.awt.Dimension

suspend fun main() {
    initializeKoin {
        modules(
            module {
                single {
                    object : Platform.Actions {
                        override fun openAccessibilitySettings() = Unit

                        override suspend fun exportData(content: String) {
                            TODO()
                        }

                        override suspend fun importData(): String {
                            TODO()
                        }
                    }
                }.bind<Platform.Actions>()
            },
            module {
                single {
                    PreferenceDataStoreFactory.createWithPath(
                        produceFile = { "${limaeSpecificFolder.absolutePath}/${Constants.DATA_STORE_PREF_NAME}".toPath() },
                    )
                }.bind<DataStore<Preferences>>()
            },
            module {
                single {
                    object : Platform.Preferences {
                        val dataStore = get<DataStore<Preferences>>()

                        override suspend fun <T> writePreferenceValue(
                            preferenceKey: Platform.Preferences.Key<T>,
                            newValue: T,
                        ) {
                            dataStore.edit {
                                when (preferenceKey) {
                                    is Platform.Preferences.Key.BooleanPreferencesKey -> {
                                        it[booleanPreferencesKey(preferenceKey.key)] =
                                            newValue as Boolean
                                    }

                                    is Platform.Preferences.Key.IntPreferencesKey -> {
                                        it[intPreferencesKey(preferenceKey.key)] =
                                            newValue as Int
                                    }

                                    is Platform.Preferences.Key.StringPreferencesKey -> {
                                        it[stringPreferencesKey(preferenceKey.key)] =
                                            newValue as String
                                    }
                                }
                            }
                        }

                        override suspend fun <T> getPreferenceValue(preferenceKey: Platform.Preferences.Key<T>): T? =
                            when (preferenceKey) {
                                is Platform.Preferences.Key.BooleanPreferencesKey -> {
                                    dataStore.data.first()[
                                        booleanPreferencesKey(
                                            preferenceKey.key,
                                        ),
                                    ]
                                }

                                is Platform.Preferences.Key.IntPreferencesKey -> {
                                    dataStore.data.first()[
                                        intPreferencesKey(
                                            preferenceKey.key,
                                        ),
                                    ]
                                }

                                is Platform.Preferences.Key.StringPreferencesKey -> {
                                    dataStore.data.first()[
                                        stringPreferencesKey(
                                            preferenceKey.key,
                                        ),
                                    ]
                                }
                            } as T?

                        override suspend fun getAllPreferences(): Map<Platform.Preferences.Key<*>, *> =
                            dataStore.data
                                .first()
                                .asMap()
                                .entries
                                .mapNotNull { (key, value) ->
                                    val platformKey =
                                        when (value) {
                                            is Boolean -> {
                                                Platform.Preferences.Key.BooleanPreferencesKey(
                                                    key.name,
                                                )
                                            }

                                            is String -> {
                                                Platform.Preferences.Key.StringPreferencesKey(
                                                    key.name,
                                                )
                                            }

                                            is Int -> {
                                                Platform.Preferences.Key.IntPreferencesKey(
                                                    key.name,
                                                )
                                            }

                                            else -> {
                                                null
                                            }
                                        }

                                    if (platformKey != null) platformKey to value else null
                                }.toMap()
                    }
                }.bind<Platform.Preferences>()
            },
        )
    }
    LimaePreferences.loadAll()
    val keyEventTunnelScope = CoroutineScope(Dispatchers.Default)
    application {
        Window(
            onKeyEvent = {
                keyEventTunnelScope.launch {
                    KeyEventTunnel.send(it)
                }
                true
            },
            onCloseRequest = ::exitApplication,
            title = "Limae",
        ) {
            this.window.minimumSize = Dimension(800, 600)
            LimaeTheme {
                Limae()
            }
        }
    }
}
