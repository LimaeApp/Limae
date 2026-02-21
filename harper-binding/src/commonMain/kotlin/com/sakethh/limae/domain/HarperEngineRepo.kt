package com.sakethh.limae.domain

interface HarperEngineRepo {
   suspend fun checkText(text: String): List<EngineSuggestion>
}