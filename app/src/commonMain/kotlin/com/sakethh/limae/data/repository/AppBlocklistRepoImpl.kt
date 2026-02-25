package com.sakethh.limae.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.sakethh.limae.AppBlocklist
import com.sakethh.limae.AppBlocklistQueries
import com.sakethh.limae.domain.LimaeDispatchers
import com.sakethh.limae.domain.repository.AppBlocklistRepo
import com.sakethh.limae.utils.getRandomUUIDv7
import com.sakethh.limae.utils.runSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppBlocklistRepoImpl(
    private val appBlocklistQueries: AppBlocklistQueries,
    private val limaeDispatchers: LimaeDispatchers,
) : AppBlocklistRepo {
    override suspend fun blockAnApp(packageName: String) {
        runSafe {
            withContext(limaeDispatchers.IO) {
                appBlocklistQueries.blockAnApp(
                    id = getRandomUUIDv7(),
                    packageName = packageName.trim(),
                )
            }
        }
    }

    override suspend fun unblockAnApp(packageName: String) {
        runSafe {
            withContext(limaeDispatchers.IO) {
                appBlocklistQueries.unblockAnApp(
                    packageName = packageName,
                )
            }
        }
    }

    override fun getAllBlockedApps(): Flow<List<AppBlocklist>> =
        appBlocklistQueries.getAllBlockedApps().asFlow().mapToList(limaeDispatchers.IO)
}
