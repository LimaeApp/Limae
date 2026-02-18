package com.sakethh.limae.domain.repository

import com.sakethh.limae.domain.SuggestionEngine
import com.sakethh.limae.domain.model.LimaeSuggestion
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.model.EngineSuggestion
import kotlinx.collections.immutable.PersistentList

interface SuggestionCheckRepo {
    suspend fun getSuggestions(text: String): Result<PersistentList<LimaeSuggestionBundle>>
}