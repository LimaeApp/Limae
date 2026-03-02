package com.sakethh.limae.domain.repository

import com.sakethh.limae.domain.Result
import com.sakethh.limae.platform.Platform

interface PreferencesRepo {
    suspend fun <T> writePreferenceValue(
        preferenceKey: Platform.Preferences.Key<T>,
        newValue: T,
    ): Result<Unit>

    suspend fun <T> getPreferenceValue(preferenceKey: Platform.Preferences.Key<T>): T?

    suspend fun getAllPreferences(): Map<Platform.Preferences.Key<*>, *>
}
