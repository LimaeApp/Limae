package com.sakethh.limae.ui.screens.settings

import androidx.datastore.preferences.core.Preferences
import com.sakethh.limae.Dictionary

sealed interface SettingsScreenAction {
    data class DeleteItemFromDictionary(
        val dictionaryItem: Dictionary,
    ) : SettingsScreenAction

    data class AddAStringToDictionary(
        val string: String,
        val onCompletion: () -> Unit,
    ) : SettingsScreenAction
}
