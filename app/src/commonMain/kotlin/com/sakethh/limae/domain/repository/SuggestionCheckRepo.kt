package com.sakethh.limae.domain.repository

import com.sakethh.limae.model.LimaeSuggestionNote
import kotlinx.collections.immutable.PersistentList

interface SuggestionCheckRepo {
    suspend fun viaHarper(text: String): Result<PersistentList<LimaeSuggestionNote>>
}