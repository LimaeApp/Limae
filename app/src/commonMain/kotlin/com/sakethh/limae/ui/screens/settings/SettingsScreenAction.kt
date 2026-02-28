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

    data class DeleteAllStringsFromDictionary(
        val onCompletion: () -> Unit,
    ) : SettingsScreenAction

    data class DeleteAllDrafts(
        val onCompletion: () -> Unit,
    ) : SettingsScreenAction

    data object PickADirectory : SettingsScreenAction

    data class UpdateAppSearchQuery(
        val string: String,
    ) : SettingsScreenAction

    data class OnBlockAnApp(
        val packageName: String,
    ) : SettingsScreenAction

    data class ExportData(
        val onStart: () -> Unit,
        val onCompletion: () -> Unit,
    ) : SettingsScreenAction

    data class ImportData(
        val onStart: () -> Unit,
        val onCompletion: () -> Unit,
    ) : SettingsScreenAction
}
