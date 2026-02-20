package com.sakethh.limae.platform

import app.cash.sqldelight.db.SqlDriver
import com.sakethh.limae.LimaeDatabase
import com.sakethh.limae.domain.LanguageToolEngine
import com.sakethh.limae.domain.LimaeDispatchers
import com.sakethh.limae.model.EngineSuggestion
import com.sakethh.limae.model.HarperEngine
import kotlinx.coroutines.CoroutineDispatcher

expect object HarperEngine : HarperEngine {
    override suspend fun checkText(text: String): List<EngineSuggestion>
}

expect object LanguageToolEngine : LanguageToolEngine {
    override suspend fun checkText(text: String): List<EngineSuggestion>
}

expect val platform: Platform

expect val LimaeIODispatcher: CoroutineDispatcher


// TODO: replace all the below stuff with DI
expect fun getSqlDriver(): SqlDriver

val localDatabase = LimaeDatabase.invoke(getSqlDriver())

val LimaeDispatchers = object : LimaeDispatchers {
    override val IO: CoroutineDispatcher = LimaeIODispatcher
}