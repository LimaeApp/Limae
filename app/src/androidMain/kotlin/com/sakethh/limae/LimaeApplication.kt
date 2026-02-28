package com.sakethh.limae

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.documentfile.provider.DocumentFile
import com.sakethh.limae.di.initializeKoin
import com.sakethh.limae.domain.ExportType
import com.sakethh.limae.domain.Result
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.utils.Constants
import com.sakethh.limae.utils.LimaePreferences
import com.sakethh.limae.utils.runSafe
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

                                override suspend fun pickADirectory(): String? {
                                    AndroidEvent.pushEvent(AndroidEvent.PickADirectory())
                                    val (uri) =
                                        AndroidEvent.readEvents.first {
                                            it is AndroidEvent.PickedDirectory && it.id == Constants.PICK_DIR_ID
                                        } as AndroidEvent.PickedDirectory

                                    return uri?.toString()
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

                                private suspend fun cleanAutoBackups(
                                    backupLocation: String,
                                    threshold: Int = 25,
                                    onCompletion: (Int) -> Unit = {},
                                ) {
                                    try {
                                        withContext(Dispatchers.IO) {
                                            DocumentFile
                                                .fromTreeUri(
                                                    this@LimaeApplication,
                                                    backupLocation.toUri(),
                                                )?.listFiles()
                                                ?.filter {
                                                    it.name?.startsWith("LimaeBackup-") == true
                                                }?.let { snapshots ->
                                                    val snapshotsCount = snapshots.count()
                                                    if (snapshotsCount > threshold) {
                                                        snapshots
                                                            .sortedBy {
                                                                it.lastModified()
                                                            }.take(snapshotsCount - threshold)
                                                            .apply {
                                                                forEach {
                                                                    it.delete()
                                                                }
                                                                onCompletion(count())
                                                            }
                                                    } else {
                                                        onCompletion(0)
                                                    }
                                                }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                override suspend fun exportData(
                                    exportType: ExportType,
                                    dirPath: String,
                                    content: String,
                                ): Result<Unit> =
                                    runSafe {
                                        if (exportType == ExportType.Backup) {
                                            cleanAutoBackups(
                                                backupLocation = dirPath,
                                            )
                                        }

                                        val simpleDateFormat =
                                            SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
                                        val timestamp = simpleDateFormat.format(Date())
                                        val exportFileName =
                                            "${if (exportType == ExportType.Standard) "LimaeExport" else "LimaeBackup"}-$timestamp.json"
                                        val directoryUri = dirPath.toUri()
                                        val directory =
                                            DocumentFile.fromTreeUri(
                                                this@LimaeApplication,
                                                directoryUri,
                                            )
                                        val newFile =
                                            directory?.createFile(
                                                "application/json",
                                                exportFileName,
                                            )
                                        newFile?.uri?.let { fileUri ->
                                            this@LimaeApplication
                                                .contentResolver
                                                .openOutputStream(
                                                    fileUri,
                                                )?.use { outputStream ->
                                                    outputStream.write(content.toByteArray())
                                                }
                                        }
                                    }

                                override suspend fun importData(): Result<String?> =
                                    runSafe {
                                        AndroidEvent.pushEvent(AndroidEvent.PickAFile())

                                        val selectedFile =
                                            AndroidEvent.readEvents.first {
                                                it is AndroidEvent.PickedFile && it.id == Constants.IMPORT_ID
                                            } as AndroidEvent.PickedFile

                                        selectedFile.uri?.let { uri ->
                                            val importRawData = StringBuilder()
                                            val documentFile =
                                                DocumentFile
                                                    .fromSingleUri(
                                                        this@LimaeApplication,
                                                        uri,
                                                    )
                                            if (documentFile?.isFile == false) {
                                                return@let null
                                            }
                                            this@LimaeApplication
                                                .contentResolver
                                                .openInputStream(
                                                    uri,
                                                ).use { inputStream ->
                                                    inputStream
                                                        ?.bufferedReader()
                                                        ?.use { bufferedReader ->
                                                            while (bufferedReader
                                                                    .readLine()
                                                                    .also { line ->
                                                                        if (line != null) {
                                                                            importRawData.append(
                                                                                line,
                                                                            )
                                                                        }
                                                                    } != null
                                                            ) {
                                                                // no op required
                                                            }
                                                        }
                                                }
                                            importRawData.toString()
                                        }
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
