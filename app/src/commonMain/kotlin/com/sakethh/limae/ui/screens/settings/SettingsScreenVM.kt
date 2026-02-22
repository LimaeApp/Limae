package com.sakethh.limae.ui.screens.settings

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.limae.domain.repository.PreferencesRepo
import com.sakethh.limae.domain.repository.SuggestionsRepo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsScreenVM(
    private val suggestionsRepo: SuggestionsRepo,
    private val preferencesRepo: PreferencesRepo,
) : ViewModel() {
    val dictionaryStrings =
        suggestionsRepo.getAllStringsFromDictionary().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList(),
        )

    fun performAction(settingsScreenAction: SettingsScreenAction) {
        when (settingsScreenAction) {
            is SettingsScreenAction.DeleteItemFromDictionary -> {
                viewModelScope.launch {
                    suggestionsRepo.deleteAnItemFromDictionary(
                        dictionary = settingsScreenAction.dictionaryItem,
                    )
                }
            }

            is SettingsScreenAction.AddAStringToDictionary -> {
                viewModelScope
                    .launch {
                        suggestionsRepo.addStringToDictionary(
                            string = settingsScreenAction.string,
                        )
                    }.invokeOnCompletion {
                        settingsScreenAction.onCompletion()
                    }
            }
        }
    }

    fun <T> updatePreference(
        preferenceKey: Preferences.Key<T>,
        newValue: T,
    ) {
        viewModelScope.launch {
            preferencesRepo.writePreferenceValue(
                preferenceKey = preferenceKey,
                newValue = newValue,
            )
        }
    }
}
