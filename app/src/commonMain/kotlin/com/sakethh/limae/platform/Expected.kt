package com.sakethh.limae.platform

import com.sakethh.limae.domain.LanguageToolEngine
import com.sakethh.limae.model.EngineSuggestion
import com.sakethh.limae.model.HarperEngine

expect object HarperEngine : HarperEngine {
    override suspend fun checkText(text: String): List<EngineSuggestion>
}

expect object LanguageToolEngine : LanguageToolEngine {
    override suspend fun checkText(text: String): List<EngineSuggestion>
}

expect val platform: Platform