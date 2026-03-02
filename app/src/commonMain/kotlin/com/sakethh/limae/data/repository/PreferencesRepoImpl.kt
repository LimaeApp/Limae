package com.sakethh.limae.data.repository

import com.sakethh.limae.domain.Result
import com.sakethh.limae.domain.repository.PreferencesRepo
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.utils.runSafe

class PreferencesRepoImpl(
    private val preferences: Platform.Preferences,
) : PreferencesRepo {
    override suspend fun <T> writePreferenceValue(
        preferenceKey: Platform.Preferences.Key<T>,
        newValue: T,
    ): Result<Unit> =
        runSafe {
            preferences.writePreferenceValue(
                preferenceKey = preferenceKey,
                newValue = newValue,
            )
        }

    override suspend fun <T> getPreferenceValue(preferenceKey: Platform.Preferences.Key<T>): T? =
        preferences.getPreferenceValue(preferenceKey)

    override suspend fun getAllPreferences(): Map<Platform.Preferences.Key<*>, *> = preferences.getAllPreferences()
}
