package com.sakethh.limae

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
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
import com.sakethh.limae.domain.ExportType
import com.sakethh.limae.domain.Result
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.limaeSpecificFolder
import com.sakethh.limae.ui.Limae
import com.sakethh.limae.ui.common.KeyEventTunnel
import com.sakethh.limae.ui.common.showHandOnHover
import com.sakethh.limae.ui.theme.LimaeTheme
import com.sakethh.limae.utils.Constants
import com.sakethh.limae.utils.LimaePreferences
import com.sakethh.limae.utils.runSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import org.koin.dsl.bind
import org.koin.dsl.module
import java.awt.Dimension
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

suspend fun main() {
    var showImportDialog by mutableStateOf(false)
    val fileImportLocation = MutableSharedFlow<String?>()
    val coroutineScope = CoroutineScope(Dispatchers.Default)
    var importLocationJob: Job? = null

    initializeKoin {
        modules(
            module {
                single {
                    object : Platform.Actions {
                        override fun openAccessibilitySettings() = Unit

                        override suspend fun pickADirectory(): String? = null

                        suspend fun cleanupAutoBackups(
                            backupLocation: String,
                            threshold: Int = 25,
                        ): Result<Unit?> =
                            runSafe {
                                withContext(Dispatchers.IO) {
                                    File(backupLocation)
                                        .listFiles {
                                            it.nameWithoutExtension.startsWith("LimaeBackup-")
                                        }?.let { snapshots ->
                                            val backupsCount = snapshots.count()
                                            if (backupsCount > threshold) {
                                                snapshots.sortBy {
                                                    it.lastModified()
                                                }
                                                snapshots.take(backupsCount - threshold).apply {
                                                    forEach {
                                                        it.delete()
                                                    }
                                                }
                                            }
                                        }
                                }
                            }

                        override suspend fun exportData(
                            exportType: ExportType,
                            dirPath: String,
                            content: String,
                        ): Result<Unit> =
                            runSafe {
                                val exportsFolder =
                                    File(
                                        System.getProperty("user.home"),
                                        "/Documents/Limae/${if (exportType == ExportType.Standard) "Exports" else "Backups"}",
                                    )

                                if (!exportsFolder.exists()) {
                                    exportsFolder.mkdirs()
                                }

                                val simpleDateFormat =
                                    SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
                                val timestamp = simpleDateFormat.format(Date())
                                val exportFileName =
                                    "${if (exportType == ExportType.Standard) "LimaeExport" else "LimaeBackup"}-$timestamp.json"

                                val exportFilePath =
                                    Paths.get(exportsFolder.absolutePath, exportFileName)
                                withContext(Dispatchers.IO) {
                                    if (exportType == ExportType.Backup) {
                                        cleanupAutoBackups(exportsFolder.absolutePath)
                                    }

                                    Files.write(exportFilePath, content.toByteArray())
                                }
                            }

                        override suspend fun importData(): Result<String?> =
                            runSafe {
                                val importContent: StringBuilder = StringBuilder()
                                try {
                                    showImportDialog = true
                                    val fileLocation =
                                        fileImportLocation.first()
                                            ?: throw Throwable("Invalid file path")
                                    println("File location received is $fileLocation")
                                    File(fileLocation).bufferedReader().use {
                                        while (it.readLine().also { currLine ->
                                                if (currLine != null) {
                                                    importContent.append(currLine)
                                                }
                                            } != null
                                        ) {
                                            // we got nothing to do here
                                        }
                                    }
                                } catch (e: Exception) {
                                    throw e // rethrow as `runSafe` needs to propagate it
                                } finally {
                                    showImportDialog = false
                                }
                                importContent.toString().ifBlank { null }
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
                if (showImportDialog) {
                    var importFilePath by rememberSaveable {
                        mutableStateOf("")
                    }
                    AlertDialog(
                        onDismissRequest = {
                            showImportDialog = false
                        },
                        confirmButton = {
                            Button(
                                modifier = Modifier.showHandOnHover().fillMaxWidth(),
                                onClick = {
                                    importLocationJob?.cancel()
                                    importLocationJob =
                                        coroutineScope.launch {
                                            fileImportLocation.emit(
                                                importFilePath.ifBlank {
                                                    null
                                                },
                                            )
                                        }
                                },
                            ) {
                                Text(
                                    text = "Import",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        },
                        dismissButton = {
                            OutlinedButton(
                                modifier = Modifier.showHandOnHover().fillMaxWidth(),
                                onClick = {
                                    showImportDialog = false
                                },
                            ) {
                                Text(
                                    text = "Cancel",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        },
                        text = {
                            TextField(
                                textStyle = MaterialTheme.typography.titleSmall,
                                label = {
                                    Text(
                                        text = "Import file path",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                },
                                value = importFilePath,
                                onValueChange = {
                                    importFilePath = it
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        title = {
                            Text(
                                text = "Absolute Location of the JSON File",
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 24.sp,
                            )
                        },
                    )
                }
            }
        }
    }
}
