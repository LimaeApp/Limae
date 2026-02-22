package com.sakethh.limae.domain.repository

import androidx.datastore.preferences.core.Preferences

interface PreferencesRepo {
    suspend fun <T> writePreferenceValue(
        preferenceKey: Preferences.Key<T>,
        newValue: T,
    )

    suspend fun <T> getPreferenceValue(preferenceKey: Preferences.Key<T>): T?

    suspend fun getAllPreferences(): Preferences
}
