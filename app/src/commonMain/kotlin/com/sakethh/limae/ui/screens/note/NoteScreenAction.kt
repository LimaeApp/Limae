package com.sakethh.limae.ui.screens.note

sealed interface NoteScreenAction {
    data class OnTitleChange(val newTitle: String): NoteScreenAction
    data class OnContentChange(val newContent: String): NoteScreenAction
}