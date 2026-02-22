package com.sakethh.limae.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.sakethh.limae.domain.repository.PreferencesRepo
import com.sakethh.limae.utils.LimaePreferences
import kotlinx.coroutines.flow.first

class PreferencesRepoImpl(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepo {
    override suspend fun <T> writePreferenceValue(
        preferenceKey: Preferences.Key<T>,
        newValue: T,
    ) {
        dataStore.edit {
            it[preferenceKey] = newValue
        }
    }

    override suspend fun getAllPreferences(): Preferences = dataStore.data.first()

    override suspend fun <T> getPreferenceValue(preferenceKey: Preferences.Key<T>): T? = dataStore.data.first()[preferenceKey]
}
