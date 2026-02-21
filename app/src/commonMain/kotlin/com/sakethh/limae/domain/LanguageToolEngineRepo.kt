package com.sakethh.limae.domain

interface LanguageToolEngineRepo {
   suspend fun checkText(text: String): List<EngineSuggestion>
}