package com.sakethh.limae.ui.screens.note

import androidx.compose.runtime.State
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
import com.sakethh.limae.ui.LimaeAction
import com.sakethh.limae.ui.common.ItemState
import com.sakethh.limae.ui.common.KeyEventTunnel
import com.sakethh.limae.utils.LimaePreferences
import com.sakethh.limae.utils.onFailure
import com.sakethh.limae.utils.onSuccess
import com.sakethh.limae.utils.runSafe
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class NoteScreenVM(
    private val suggestionsRepo: SuggestionsRepo,
    private val sourceNoteId: String?,
    private val notesRepo: NotesRepo,
    registerListeningToSuggestions: State<Boolean>,
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

    private var lastInsertedId: String? = null

    // a boolean would have been enough
    private val pauseSuggestions = Mutex(locked = false)

    var isSavingANote by mutableStateOf(false)

    private var currentActiveTextField by mutableStateOf(
        ActiveTextField.None,
    )

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

    private suspend fun applySuggestionToNote(
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
            note =
                note.run {
                    when (currentActiveTextField) {
                        ActiveTextField.Title -> {
                            copy(
                                title =
                                    title.replaceRange(
                                        startIndex = startIndex,
                                        endIndex = endIndex,
                                        replacement = replacementText,
                                    ),
                            )
                        }

                        ActiveTextField.Content -> {
                            copy(
                                content =
                                    content.replaceRange(
                                        startIndex = startIndex,
                                        endIndex = endIndex,
                                        replacement = replacementText,
                                    ),
                            )
                        }

                        else -> {
                            error("One of the text field (${ActiveTextField.entries}) is supposed to be active.")
                        }
                    }
                }
        }.onFailure(LimaeAction::reportError)
    }

    fun onAction(noteScreenAction: NoteScreenAction) {
        when (noteScreenAction) {
            is NoteScreenAction.OnContentChange -> {
                note = note.copy(content = noteScreenAction.newContent)
            }

            is NoteScreenAction.OnTitleChange -> {
                note = note.copy(title = noteScreenAction.newTitle)
            }

            is NoteScreenAction.OnSuggestionAccept -> {
                val limaeSuggestionBundle = suggestions.value.data[noteScreenAction.limaeNotesIndex]
                viewModelScope.launch {
                    applySuggestionToNote(
                        replacementText = limaeSuggestionBundle.suggestion.suggestions[noteScreenAction.suggestionNoteIndex],
                        engine = limaeSuggestionBundle.engine,
                        startIndex = limaeSuggestionBundle.suggestion.startIndex,
                        endIndex = limaeSuggestionBundle.suggestion.endIndex,
                    )
                }
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
                /*
                While this was originally planned and I have implemented it, I came
                to the conclusion that this needs more "study":
                1. Applying all suggestions is a guess game and does not imply what the
                user is actually expecting.
                2. Harper/LanguageTool might provide multiple suggestions within the
                same index boundary; now predicting what to apply is kind of simple using
                priority-based implementation to know what to swap and what not, but the
                problem is we are never sure about that priority; it doesn't really fix it
                in all cases.
                3. The best solution is LSP-like, which doesn't touch anything that is in
                those overlapping indices.

                Will skip this for now; if anyone raises the issue, then will see how
                it goes. Until then, adios.
                 * */

                return
                if (pauseSuggestions.isLocked) return

                viewModelScope.launch {
                    pauseSuggestions.withLock {
                        val rawText =
                            if (currentActiveTextField == ActiveTextField.Title) {
                                note.title
                            } else {
                                note.content
                            }
                        val seenIndexes = mutableSetOf<Int>()
                        val filteredSuggestions =
                            suggestions.value.data
                                .filter {
                                    val currentRange =
                                        it.suggestion.startIndex until it.suggestion.endIndex

                                    if (currentRange
                                            .any {
                                                it in seenIndexes
                                            }
                                    ) {
                                        return@filter false
                                    }
                                    seenIndexes.addAll(currentRange)
                                }.sortedBy {
                                    it.suggestion.startIndex
                                }
                        var currIndex = 0
                        val updatedString = StringBuilder()
                        for (suggestionBundle in filteredSuggestions) {
                            val currStartIndex = suggestionBundle.suggestion.startIndex
                            val currEndIndex = suggestionBundle.suggestion.endIndex
                            if (currIndex < currStartIndex) {
                                updatedString.append(
                                    rawText.substring(currIndex until currStartIndex),
                                )
                            }
                            updatedString.append(
                                getSuggestionValue(
                                    suggestion = suggestionBundle.suggestion.suggestions.first(),
                                    engine = suggestionBundle.engine,
                                ),
                            )
                            currIndex = currEndIndex
                        }
                        if (currIndex < rawText.length) {
                            updatedString.append(
                                rawText.substring(currIndex),
                            )
                        }
                        note =
                            if (currentActiveTextField == ActiveTextField.Title) {
                                note
                                    .copy(title = updatedString.toString())
                            } else {
                                note.copy(content = updatedString.toString())
                            }
                    }
                }
            }

            is NoteScreenAction.AddStringToDictionary -> {
                viewModelScope.launch {
                    suggestionsRepo
                        .addStringsToDictionary(listOf(noteScreenAction.string))
                        .onFailure(
                            LimaeAction::reportError,
                        )
                }
            }

            is NoteScreenAction.UpdateFocusedTextField -> {
                currentActiveTextField = noteScreenAction.field
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
                }.onFailure(LimaeAction::reportError)
        } else {
            notesRepo
                .updateANoteById(
                    id = noteId ?: lastInsertedId!!,
                    title = title,
                    content = content,
                ).onSuccess { result ->
                    val eventTimestamp = result.data
                    note = note.copy(lastModified = eventTimestamp)
                }.onFailure(LimaeAction::reportError)
        }
        onCompletion()
        isSavingANote = false
    }

    init {
        viewModelScope.launch(Dispatchers.Default) {
            if (sourceNoteId != null) {
                notesRepo
                    .getANoteById(sourceNoteId)
                    .onSuccess {
                        note = it.data
                    }.onFailure(LimaeAction::reportError)
            }
            if (LimaePreferences.autoSaveNotes) {
                snapshotFlow {
                    note
                }.drop(1).debounce(500L).collectLatest { note ->
                    saveNote(
                        noteId = sourceNoteId,
                        title = note.title,
                        content = note.content,
                        onCompletion = {},
                    )
                }
            }
        }

        viewModelScope.launch {
            snapshotFlow {
                registerListeningToSuggestions.value
            }.flatMapLatest { listenToSuggestions ->
                if (listenToSuggestions) {
                    combine(
                        snapshotFlow {
                            currentActiveTextField
                        },
                        snapshotFlow {
                            note
                        },
                    ) { currentActiveTextField, note ->
                        currentActiveTextField to note
                    }.transform { (currentActiveTextField, note) ->
                        if (!pauseSuggestions.isLocked) {
                            when (currentActiveTextField) {
                                ActiveTextField.Title -> emit(note.title)
                                else -> emit(note.content)
                            }
                        }
                    }.flatMapLatest { rawString ->
                        suggestionsRepo.getSuggestions(rawString)
                    }
                } else {
                    emptyFlow()
                }
            }.collectLatest { suggestionsResult ->
                suggestionsResult
                    .onSuccess { (suggestionBundles) ->
                        _suggestions.onSuccess(suggestionBundles)
                    }.onFailure(
                        _suggestions::onFailure,
                    ).onFailure(LimaeAction::reportError)
            }
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
