package com.sakethh.limae.platform

import com.sakethh.limae.domain.EngineSuggestion
import com.sakethh.limae.domain.HarperEngineRepo
import com.sakethh.limae.domain.LanguageToolEngineRepo
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module

expect object HarperEngine : HarperEngineRepo {
    override suspend fun checkText(text: String): List<EngineSuggestion>
}

expect object LanguageToolEngine : LanguageToolEngineRepo {
    override suspend fun checkText(text: String): List<EngineSuggestion>
}

expect val platform: Platform

expect val LimaeIODispatcher: CoroutineDispatcher


expect suspend fun platformDatabaseModule(): Module