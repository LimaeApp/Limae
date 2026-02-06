package com.sakethh.limae.ui.screens.note

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.limae.domain.repository.SuggestionCheckRepo
import com.sakethh.limae.model.LimaeNote
import com.sakethh.limae.ui.common.ItemState
import com.sakethh.limae.utils.onFailure
import com.sakethh.limae.utils.onLoading
import com.sakethh.limae.utils.onSuccess
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class NoteScreenVM(
    private val suggestionCheckRepo: SuggestionCheckRepo,
    title: String,
    content: String
) : ViewModel() {
    var noteTitle by mutableStateOf(title)
        private set
    var noteContent by mutableStateOf(content)
        private set

    private var grammarCheckJob: Job? = null
    private val _suggestions = MutableStateFlow(
        ItemState<PersistentList<LimaeNote>>(
            isError = false,
            errorMessage = null,
            isLoading = false,
            data = persistentListOf()
        )
    )
    val suggestions = _suggestions.asStateFlow()

    fun onAction(noteScreenAction: NoteScreenAction) {
        when (noteScreenAction) {
            is NoteScreenAction.OnContentChange -> noteContent = noteScreenAction.newContent
            is NoteScreenAction.OnTitleChange -> noteTitle = noteScreenAction.newTitle
        }
    }

    init {
        grammarCheckJob?.cancel()
        grammarCheckJob = viewModelScope.launch {
            launch {
                snapshotFlow {
                    noteTitle
                }.debounce(150).collectLatest {
                    _suggestions.onLoading()
                    suggestionCheckRepo.viaHarper(it)
                        .onSuccess(_suggestions::onSuccess).onFailure(
                            _suggestions::onFailure
                        )
                }
            }
            launch {
                snapshotFlow {
                    noteContent
                }.debounce(150).collectLatest {
                    _suggestions.onLoading()
                    suggestionCheckRepo.viaHarper(it)
                        .onSuccess(_suggestions::onSuccess).onFailure(
                            _suggestions::onFailure
                        )
                }
            }
        }
    }
}