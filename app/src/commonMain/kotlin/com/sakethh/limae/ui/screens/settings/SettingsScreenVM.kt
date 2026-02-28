package com.sakethh.limae.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.limae.domain.onSuccess
import com.sakethh.limae.domain.repository.AppBlocklistRepo
import com.sakethh.limae.domain.repository.DatabaseUtilsRepo
import com.sakethh.limae.domain.repository.NotesRepo
import com.sakethh.limae.domain.repository.PreferencesRepo
import com.sakethh.limae.domain.repository.SuggestionsRepo
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.ui.common.ItemState
import com.sakethh.limae.utils.LimaeJson
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

class SettingsScreenVM(
    private val suggestionsRepo: SuggestionsRepo,
    private val notesRepo: NotesRepo,
    private val preferencesRepo: PreferencesRepo,
    private val platformActions: Platform.Actions,
    private val appBlocklistRepo: AppBlocklistRepo,
    private val databaseUtilsRepo: DatabaseUtilsRepo,
) : ViewModel() {
    var appSearchQuery by mutableStateOf("")
        private set

    val installedApps =
        flow {
            emit(
                ItemState(
                    isError = false,
                    errorMessage = null,
                    isLoading = true,
                    data = emptyList(),
                ),
            )
            val installedApps = platformActions.getInstalledApps()
            emit(
                ItemState(
                    isError = false,
                    errorMessage = null,
                    isLoading = false,
                    data = installedApps,
                ),
            )
        }.flatMapLatest { installedApps ->
            snapshotFlow {
                appSearchQuery
            }.debounce(150L)
                .transform { searchQuery ->
                    if (searchQuery.isBlank()) {
                        emit(installedApps)
                    } else {
                        emit(
                            installedApps.copy(
                                data =
                                    installedApps.data.filter {
                                        it.name.contains(searchQuery.trim()) ||
                                            it.packageName.contains(
                                                searchQuery.trim(),
                                            )
                                    },
                            ),
                        )
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000L),
                    initialValue =
                        ItemState(
                            isError = false,
                            errorMessage = null,
                            isLoading = true,
                            data = emptyList(),
                        ),
                )
        }

    val appBlockList =
        appBlocklistRepo
            .getAllBlockedApps()
            .transform { appBlockList ->
                emit(
                    appBlockList
                        .map { app ->
                            app.packageName
                        }.toHashSet(),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptySet(),
            )

    val dictionaryStrings =
        suggestionsRepo.getAllStringsFromDictionary().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList(),
        )

    fun performAction(settingsScreenAction: SettingsScreenAction) {
        when (settingsScreenAction) {
            is SettingsScreenAction.DeleteItemFromDictionary -> {
                viewModelScope.launch {
                    suggestionsRepo.deleteAnItemFromDictionary(
                        dictionary = settingsScreenAction.dictionaryItem,
                    )
                }
            }

            is SettingsScreenAction.AddStringsToDictionary -> {
                viewModelScope
                    .launch {
                        suggestionsRepo.addStringsToDictionary(
                            customStrings = settingsScreenAction.string.split("\n"),
                        )
                    }.invokeOnCompletion {
                        settingsScreenAction.onCompletion()
                    }
            }

            is SettingsScreenAction.DeleteAllStringsFromDictionary -> {
                viewModelScope.launch {
                    suggestionsRepo.deleteAllStringsFromDictionary()
                }
            }

            is SettingsScreenAction.DeleteAllDrafts -> {
                viewModelScope.launch {
                    notesRepo.deleteAllNotes()
                }
            }

            is SettingsScreenAction.UpdateAppSearchQuery -> {
                appSearchQuery =
                    settingsScreenAction.string
            }

            is SettingsScreenAction.OnBlockAnApp -> {
                viewModelScope.launch {
                    if (appBlockList.value.contains(settingsScreenAction.packageName)) {
                        appBlocklistRepo.unblockAnApp(settingsScreenAction.packageName)
                    } else {
                        appBlocklistRepo.blockAnApp(settingsScreenAction.packageName)
                    }
                }
            }

            is SettingsScreenAction.ExportData -> {
                viewModelScope
                    .launch {
                        databaseUtilsRepo.getExportData().onSuccess { (exportObject) ->
                            platformActions.exportData(LimaeJson.encodeToString(exportObject))
                        }
                    }.invokeOnCompletion {
                        settingsScreenAction.onCompletion()
                    }
            }

            is SettingsScreenAction.ImportData -> {
                viewModelScope
                    .launch {
                        databaseUtilsRepo.importData(LimaeJson.decodeFromString(platformActions.importData()))
                    }.invokeOnCompletion {
                        settingsScreenAction.onCompletion()
                    }
            }
        }
    }

    fun <T> updatePreference(
        preferenceKey: Platform.Preferences.Key<T>,
        newValue: T,
    ) {
        viewModelScope.launch {
            preferencesRepo.writePreferenceValue(
                preferenceKey = preferenceKey,
                newValue = newValue,
            )
        }
    }
}
