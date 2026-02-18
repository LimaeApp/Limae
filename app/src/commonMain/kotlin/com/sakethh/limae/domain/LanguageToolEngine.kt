package com.sakethh.limae.domain

import com.sakethh.limae.model.EngineSuggestion

interface LanguageToolEngine {
   suspend fun checkText(text: String): List<EngineSuggestion>
}