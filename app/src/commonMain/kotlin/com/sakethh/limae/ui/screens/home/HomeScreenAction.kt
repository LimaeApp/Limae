package com.sakethh.limae.ui.screens.home

sealed interface HomeScreenAction {
    data class DeleteANote(val noteId: String, val onCompletion: () -> Unit) : HomeScreenAction
}