package com.sakethh.limae.domain

import com.sakethh.limae.LimaeDatabase

interface PlatformUtils {
    suspend fun getLocalDatabase(): LimaeDatabase
}