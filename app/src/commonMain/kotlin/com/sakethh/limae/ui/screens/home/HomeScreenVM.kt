package com.sakethh.limae.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.limae.domain.repository.NotesRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeScreenVM(private val notesRepo: NotesRepo) : ViewModel() {
    val savedNotes = notesRepo.getAllNotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList()
    )

    fun performAction(homeScreenAction: HomeScreenAction) {
        when (homeScreenAction) {
            is HomeScreenAction.DeleteANote -> deleteNote(
                id = homeScreenAction.noteId,
                onCompletion = homeScreenAction.onCompletion
            )
        }
    }

    private fun deleteNote(id: String, onCompletion: () -> Unit) {
        viewModelScope.launch {
            notesRepo.deleteANoteById(id)
        }.invokeOnCompletion {
            onCompletion()
        }
    }

}