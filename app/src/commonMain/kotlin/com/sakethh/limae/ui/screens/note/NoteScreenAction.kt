package com.sakethh.limae.ui.screens.note

sealed interface NoteScreenAction {
    data class OnTitleChange(
        val newTitle: String,
    ) : NoteScreenAction

    data class OnContentChange(
        val newContent: String,
    ) : NoteScreenAction

    data class OnSuggestionAccept(
        val limaeNotesIndex: LimaeNotesIndex,
        val suggestionNoteIndex: SuggestionNoteIndex,
    ) : NoteScreenAction

    data object AcceptAllSuggestions : NoteScreenAction

    data class SaveNote(
        val noteId: String?,
        val title: String,
        val content: String,
        val onCompletion: () -> Unit,
    ) : NoteScreenAction

    data class AddStringToDictionary(
        val string: String,
    ) : NoteScreenAction
}
