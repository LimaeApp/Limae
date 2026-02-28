package com.sakethh.limae.domain.repository

import com.sakethh.limae.platform.Platform

interface PreferencesRepo {
    suspend fun <T> writePreferenceValue(
        preferenceKey: Platform.Preferences.Key<T>,
        newValue: T,
    )

    suspend fun <T> getPreferenceValue(preferenceKey: Platform.Preferences.Key<T>): T?

    suspend fun getAllPreferences(): Map<Platform.Preferences.Key<*>, *>
}
