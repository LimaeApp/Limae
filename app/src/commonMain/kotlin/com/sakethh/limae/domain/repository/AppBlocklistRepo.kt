package com.sakethh.limae.domain.repository

import com.sakethh.limae.AppBlocklist
import com.sakethh.limae.domain.Result
import kotlinx.coroutines.flow.Flow

interface AppBlocklistRepo {
    suspend fun blockAnApp(packageName: String): Result<Unit>

    suspend fun unblockAnApp(packageName: String): Result<Unit>

    fun getAllBlockedApps(): Flow<List<AppBlocklist>>
}
