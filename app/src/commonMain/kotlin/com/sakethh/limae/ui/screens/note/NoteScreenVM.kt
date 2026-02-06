package com.sakethh.limae.ui.screens.note

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.limae.domain.repository.SuggestionCheckRepo
import com.sakethh.limae.model.LimaeSuggestionNote
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
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class NoteScreenVM(
    private val suggestionCheckRepo: SuggestionCheckRepo,
    title: String,
    content: String,
    registerListeningToSuggestions: Boolean
) : ViewModel() {
    var noteTitle by mutableStateOf(title)
        private set
    var noteContent by mutableStateOf(content)
        private set

    private var grammarCheckJob: Job? = null
    private val _suggestions = MutableStateFlow(
        ItemState<PersistentList<LimaeSuggestionNote>>(
            isError = false,
            errorMessage = null,
            isLoading = false,
            data = persistentListOf()
        )
    )
    val suggestions = _suggestions.asStateFlow()

    private val isEmittedFromTitle = AtomicBoolean(true)

    fun onAction(noteScreenAction: NoteScreenAction) {
        when (noteScreenAction) {
            is NoteScreenAction.OnContentChange -> noteContent = noteScreenAction.newContent
            is NoteScreenAction.OnTitleChange -> noteTitle = noteScreenAction.newTitle
            is NoteScreenAction.OnSuggestionAccept -> {
                val limaeSuggestion = suggestions.value.data[noteScreenAction.limaeNotesIndex]
                val replacementText = limaeSuggestion
                    .suggestions[noteScreenAction.suggestionNoteIndex].substringAfter(
                    "“"
                ).substringBeforeLast("”")
                if (isEmittedFromTitle.load()) {
                    noteTitle = noteTitle.replaceRange(
                        startIndex = limaeSuggestion.startIndex,
                        endIndex = limaeSuggestion.endIndex,
                        replacement = replacementText
                    )
                } else {
                    noteContent = noteContent.replaceRange(
                        startIndex = limaeSuggestion.startIndex,
                        endIndex = limaeSuggestion.endIndex,
                        replacement = replacementText
                    )
                }
            }
        }
    }

    init {
        if (registerListeningToSuggestions) {
            grammarCheckJob = viewModelScope.launch {
                launch {
                    snapshotFlow {
                        noteTitle
                    }.debounce(150).collectLatest {
                        isEmittedFromTitle.store(true)
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
                        isEmittedFromTitle.store(false)
                        _suggestions.onLoading()
                        suggestionCheckRepo.viaHarper(it)
                            .onSuccess(_suggestions::onSuccess).onFailure(
                                _suggestions::onFailure
                            )
                    }
                }
            }
        } else {
            grammarCheckJob?.cancel() // there will be no Job when it reaches here. but aight.
        }
    }
}