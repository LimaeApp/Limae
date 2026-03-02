package com.sakethh.limae.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.limae.domain.onFailure
import com.sakethh.limae.domain.repository.NotesRepo
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.platform.isReadTextFieldAccessibilityServiceRunning
import com.sakethh.limae.ui.LimaeAction
import com.sakethh.limae.ui.common.ItemState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

class HomeScreenVM(
    private val notesRepo: NotesRepo,
    private val platformActions: Platform.Actions,
) : ViewModel() {
    var searchQuery by mutableStateOf("")
        private set

    val savedNotes =
        notesRepo
            .getAllNotes()
            .flatMapLatest { notes ->
                snapshotFlow {
                    searchQuery
                }.transform {
                    if (searchQuery.isBlank()) {
                        emit(notes)
                    } else {
                        emit(
                            notes.filter { note ->
                                note.title.contains(searchQuery.trim()) ||
                                    note.content.contains(
                                        searchQuery.trim(),
                                    )
                            },
                        )
                    }
                }
            }.transform { drafts ->
                emit(
                    ItemState(
                        isError = false,
                        errorMessage = null,
                        isLoading = false,
                        data = drafts,
                    ),
                )
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

    val isReadTextFieldServiceRunning =
        isReadTextFieldAccessibilityServiceRunning
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = false,
            )

    fun performAction(homeScreenAction: HomeScreenAction) {
        when (homeScreenAction) {
            is HomeScreenAction.DeleteANote -> {
                deleteNote(
                    id = homeScreenAction.noteId,
                    onCompletion = homeScreenAction.onCompletion,
                )
            }

            is HomeScreenAction.UpdateSearchQuery -> {
                searchQuery = homeScreenAction.string
            }

            is HomeScreenAction.OpenAccessibilityServiceScreen -> {
                platformActions.openAccessibilitySettings()
            }
        }
    }

    private fun deleteNote(
        id: String,
        onCompletion: () -> Unit,
    ) {
        viewModelScope
            .launch {
                notesRepo.deleteANoteById(id).onFailure(LimaeAction::reportError)
            }.invokeOnCompletion {
                onCompletion()
            }
    }
}
