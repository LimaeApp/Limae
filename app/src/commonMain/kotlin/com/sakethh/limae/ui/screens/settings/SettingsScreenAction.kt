package com.sakethh.limae.ui.screens.settings

import com.sakethh.limae.Dictionary

sealed interface SettingsScreenAction {
    data class DeleteItemFromDictionary(
        val dictionaryItem: Dictionary,
    ) : SettingsScreenAction

    data class AddStringsToDictionary(
        val string: String,
        val onCompletion: () -> Unit,
    ) : SettingsScreenAction

    data object DeleteAllStringsFromDictionary : SettingsScreenAction

    data object DeleteAllDrafts : SettingsScreenAction

    data class UpdateAppSearchQuery(
        val string: String,
    ) : SettingsScreenAction

    data class OnBlockAnApp(
        val packageName: String,
    ) : SettingsScreenAction
}
