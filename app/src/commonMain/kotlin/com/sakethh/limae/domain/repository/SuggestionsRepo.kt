package com.sakethh.limae.domain.repository

import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import kotlinx.collections.immutable.PersistentList

interface SuggestionsRepo {
    suspend fun getSuggestions(text: String): Result<PersistentList<LimaeSuggestionBundle>>
}