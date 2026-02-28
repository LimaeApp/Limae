package com.sakethh.limae.domain.repository

import com.sakethh.limae.domain.Result
import com.sakethh.limae.domain.model.LimaeSchema

interface DatabaseUtilsRepo {
    suspend fun getExportData(): Result<LimaeSchema>

    suspend fun importData(rawData: String): Result<Unit>
}
