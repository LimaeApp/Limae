package com.sakethh.limae.ui.screens.note

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.limae.Note
import com.sakethh.limae.domain.SuggestionEngine
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.domain.onFailure
import com.sakethh.limae.domain.onSuccess
import com.sakethh.limae.domain.repository.NotesRepo
import com.sakethh.limae.domain.repository.SuggestionsRepo
import com.sakethh.limae.ui.common.ItemState
import com.sakethh.limae.ui.common.KeyEventTunnel
import com.sakethh.limae.utils.onFailure
import com.sakethh.limae.utils.onLoading
import com.sakethh.limae.utils.onSuccess
import com.sakethh.limae.utils.runSafe
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class NoteScreenVM(
    private val suggestionsRepo: SuggestionsRepo,
    private val sourceNoteId: String?,
    private val notesRepo: NotesRepo,
    registerListeningToSuggestions: Boolean,
) : ViewModel() {
    var note by mutableStateOf(
        Note(
            id = "",
            title = "",
            content = "",
            lastModified = 0,
        ),
    )
        private set

    private var grammarCheckJob: Job? = null
    private val _suggestions =
        MutableStateFlow(
            ItemState<PersistentList<LimaeSuggestionBundle>>(
                isError = false,
                errorMessage = null,
                isLoading = false,
                data = persistentListOf(),
            ),
        )
    val suggestions = _suggestions.asStateFlow()

    private val isEmittedFromTitle = AtomicBoolean(true)
    private var lastInsertedId: String? = null

    // a boolean would have been enough
    private val pauseSuggestions = Mutex(locked = false)

    var isSavingANote by mutableStateOf(false)

    private fun getSuggestionValue(
        suggestion: String,
        engine: SuggestionEngine,
    ): String =
        if (engine == SuggestionEngine.Harper) {
            suggestion
                .substringAfter(
                    "“",
                ).substringBeforeLast("”")
        } else {
            suggestion
        }

    private fun applySuggestionToNote(
        replacementText: String,
        engine: SuggestionEngine,
        startIndex: Int,
        endIndex: Int,
    ) {
        val replacementText =
            replacementText.run {
                getSuggestionValue(
                    suggestion = replacementText,
                    engine = engine,
                )
            }

        runSafe {
            if (isEmittedFromTitle.load()) {
                note =
                    note.run {
                        copy(
                            title =
                                title.replaceRange(
                                    startIndex = startIndex,
                                    endIndex = endIndex,
                                    replacement = replacementText,
                                ),
                        )
                    }
            } else {
                note =
                    note.run {
                        copy(
                            content =
                                content.replaceRange(
                                    startIndex = startIndex,
                                    endIndex = endIndex,
                                    replacement = replacementText,
                                ),
                        )
                    }
            }
        }
    }

    fun onAction(noteScreenAction: NoteScreenAction) {
        when (noteScreenAction) {
            is NoteScreenAction.OnContentChange -> {
                note =
                    note.copy(content = noteScreenAction.newContent)
            }

            is NoteScreenAction.OnTitleChange -> {
                note = note.copy(title = noteScreenAction.newTitle)
            }

            is NoteScreenAction.OnSuggestionAccept -> {
                val limaeSuggestionBundle = suggestions.value.data[noteScreenAction.limaeNotesIndex]
                applySuggestionToNote(
                    replacementText = limaeSuggestionBundle.suggestion.suggestions[noteScreenAction.suggestionNoteIndex],
                    engine = limaeSuggestionBundle.engine,
                    startIndex = limaeSuggestionBundle.suggestion.startIndex,
                    endIndex = limaeSuggestionBundle.suggestion.endIndex,
                )
            }

            is NoteScreenAction.SaveNote -> {
                viewModelScope.launch {
                    saveNote(
                        noteId = noteScreenAction.noteId,
                        title = noteScreenAction.title,
                        content = noteScreenAction.content,
                        onCompletion = noteScreenAction.onCompletion,
                    )
                }
            }

            is NoteScreenAction.AcceptAllSuggestions -> {
                if (pauseSuggestions.isLocked) return

                viewModelScope.launch {
                    pauseSuggestions.withLock {
                        TODO()
                    }
                }
            }

            is NoteScreenAction.AddStringToDictionary -> {
                viewModelScope.launch {
                    suggestionsRepo.addStringsToDictionary(listOf(noteScreenAction.string))
                }
            }
        }
    }

    private suspend fun saveNote(
        noteId: String?,
        title: String,
        content: String,
        onCompletion: () -> Unit,
    ) {
        isSavingANote = true
        if (lastInsertedId == null && noteId == null) {
            notesRepo
                .insertANote(
                    title = title,
                    content = content,
                ).onSuccess { result ->
                    val (insertedRowId, rowInsertionCount, eventTimestamp) = result.data

                    if (rowInsertionCount == (0).toLong()) return@onSuccess

                    lastInsertedId = insertedRowId
                    note =
                        note.copy(
                            id = insertedRowId,
                            lastModified = eventTimestamp,
                        )
                }
        } else {
            notesRepo
                .updateANoteById(
                    id = noteId ?: lastInsertedId!!,
                    title = title,
                    content = content,
                ).onSuccess { result ->
                    val eventTimestamp = result.data
                    note = note.copy(lastModified = eventTimestamp)
                }
        }
        onCompletion()
        isSavingANote = false
    }

    init {
        viewModelScope.launch {
            if (sourceNoteId != null) {
                notesRepo.getANoteById(sourceNoteId).onSuccess {
                    note = it.data
                }
            }
        }
        if (registerListeningToSuggestions) {
            grammarCheckJob =
                viewModelScope.launch(Dispatchers.Default) {
                    launch {
                        snapshotFlow {
                            note.title
                        }.transform {
                            if (!pauseSuggestions.isLocked) {
                                emit(it)
                            }
                        }.collectLatest { rawString ->
                            isEmittedFromTitle.store(true)
                            _suggestions.onLoading()
                            suggestionsRepo
                                .getSuggestions(rawString)
                                .onSuccess { (suggestionBundles) ->
                                    _suggestions.onSuccess(suggestionBundles)
                                }.onFailure(
                                    _suggestions::onFailure,
                                )
                        }
                    }
                    launch {
                        snapshotFlow {
                            note.content
                        }.transform {
                            if (!pauseSuggestions.isLocked) {
                                emit(it)
                            }
                        }.collectLatest { rawString ->
                            isEmittedFromTitle.store(false)
                            _suggestions.onLoading()
                            suggestionsRepo
                                .getSuggestions(rawString)
                                .onSuccess { (suggestionBundles) ->
                                    _suggestions.onSuccess(suggestionBundles)
                                }.onFailure(
                                    _suggestions::onFailure,
                                )
                        }
                    }
                }
        } else {
            grammarCheckJob?.cancel() // there will be no Job when it reaches here. but aight.
        }

        viewModelScope.launch(Dispatchers.Default) {

            // generally, this should be collect instead of collectLatest
            // since we only listen to ctrl + s to update the latest state
            // and don't care about previous events, collectLatest makes more sense here
            KeyEventTunnel.readTunnel.collectLatest { keyEvent ->
                if (keyEvent.isCtrlPressed && keyEvent.key == Key.S && keyEvent.type == KeyEventType.KeyUp) {
                    println("Saving the note")
                    saveNote(
                        noteId = sourceNoteId,
                        title = note.title,
                        content = note.content,
                        onCompletion = {},
                    )
                }
            }
        }
    }
}
