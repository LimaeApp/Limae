package com.sakethh.limae.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.limae.domain.repository.NotesRepo
import com.sakethh.limae.ui.common.ItemState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

class HomeScreenVM(
    private val notesRepo: NotesRepo,
) : ViewModel() {
    val savedNotes =
        notesRepo
            .getAllNotes()
            .transform { drafts ->
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

    fun performAction(homeScreenAction: HomeScreenAction) {
        when (homeScreenAction) {
            is HomeScreenAction.DeleteANote -> {
                deleteNote(
                    id = homeScreenAction.noteId,
                    onCompletion = homeScreenAction.onCompletion,
                )
            }
        }
    }

    private fun deleteNote(
        id: String,
        onCompletion: () -> Unit,
    ) {
        viewModelScope
            .launch {
                notesRepo.deleteANoteById(id)
            }.invokeOnCompletion {
                onCompletion()
            }
    }
}
