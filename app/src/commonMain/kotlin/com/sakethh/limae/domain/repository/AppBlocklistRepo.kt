package com.sakethh.limae.domain.repository

import com.sakethh.limae.AppBlocklist
import kotlinx.coroutines.flow.Flow

interface AppBlocklistRepo {
    suspend fun blockAnApp(packageName: String)

    suspend fun unblockAnApp(packageName: String)

    fun getAllBlockedApps(): Flow<List<AppBlocklist>>
}
