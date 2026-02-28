package com.sakethh.limae

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakethh.limae.di.initializeKoin
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.utils.Constants
import com.sakethh.limae.utils.LimaePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

class LimaeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        runBlocking {
            initializeKoin {
                androidContext(this@LimaeApplication)
                modules(
                    module {
                        single {
                            object : Platform.Actions {
                                override fun openAccessibilitySettings() {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                }

                                override suspend fun getInstalledApps(): List<Platform.Actions.InstalledApp> =
                                    withContext(Dispatchers.IO) {
                                        applicationContext.packageManager
                                            .getInstalledApplications(
                                                PackageManager.GET_META_DATA,
                                            ).map {
                                                async {
                                                    Platform.Actions.InstalledApp(
                                                        name =
                                                            try {
                                                                packageManager
                                                                    .getApplicationLabel(it)
                                                                    .toString()
                                                            } catch (_: Exception) {
                                                                ""
                                                            },
                                                        packageName = it.packageName.toString(),
                                                    )
                                                }
                                            }.awaitAll()
                                    }

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
                                produceFile = {
                                    androidContext()
                                        .filesDir
                                        .resolve(Constants.DATA_STORE_PREF_NAME)
                                        .absolutePath
                                        .toPath()
                                },
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
        }
    }
}
