package com.sakethh.limae.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.limae.domain.ExportType
import com.sakethh.limae.domain.model.LimaeSchema
import com.sakethh.limae.domain.onFailure
import com.sakethh.limae.domain.repository.AppBlocklistRepo
import com.sakethh.limae.domain.repository.NotesRepo
import com.sakethh.limae.domain.repository.SuggestionsRepo
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.utils.LimaeJson
import com.sakethh.limae.utils.LimaePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class LimaeVM(
    private val notesRepo: NotesRepo,
    private val suggestionsRepo: SuggestionsRepo,
    private val appBlocklistRepo: AppBlocklistRepo,
    private val platformActions: Platform.Actions,
) : ViewModel() {
    val snackBarHost = SnackbarHostState()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            launch {
                LimaeAction.readEvents.collect { limaeAction ->
                    when (limaeAction) {
                        is LimaeAction.Navigate -> Unit
                        LimaeAction.NavigateBack -> Unit
                        is LimaeAction.ShowSnackbar -> snackBarHost.showSnackbar(message = limaeAction.message)
                    }
                }
            }

            launch {
                snapshotFlow {
                    LimaePreferences.useAutoExports
                }.flatMapLatest { useAutoExports ->
                    if (useAutoExports) {
                        combine(
                            notesRepo.getAllNotes(),
                            suggestionsRepo.getAllStringsFromDictionary(),
                            appBlocklistRepo.getAllBlockedApps(),
                        ) { allDrafts, dictItems, appBlocklist ->
                            Triple(allDrafts, dictItems, appBlocklist)
                        }
                    } else {
                        emptyFlow()
                    }
                }.debounce(500)
                    .collect { (allDrafts, dictItems, appBlocklist) ->
                        platformActions
                            .exportData(
                                dirPath = LimaePreferences.exportDirPath,
                                exportType = ExportType.Backup,
                                content =
                                    LimaeJson.encodeToString(
                                        LimaeSchema(
                                            dictionary =
                                                dictItems.map {
                                                    it.string
                                                },
                                            drafts =
                                                allDrafts.map {
                                                    LimaeSchema.Draft(
                                                        title = it.title,
                                                        content = it.content,
                                                        lastModified = it.lastModified,
                                                    )
                                                },
                                            appBlocklist =
                                                appBlocklist.map {
                                                    it.packageName
                                                },
                                        ),
                                    ),
                            ).onFailure(LimaeAction::reportError)
                    }
            }
        }
    }
}
