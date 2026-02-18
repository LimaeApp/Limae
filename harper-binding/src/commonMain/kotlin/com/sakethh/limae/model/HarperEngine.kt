package com.sakethh.limae.model

interface HarperEngine {
   suspend fun checkText(text: String): List<EngineSuggestion>
}