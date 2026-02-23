package com.sakethh.limae.domain.repository

import com.sakethh.limae.Dictionary
import com.sakethh.limae.domain.Result
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow

interface SuggestionsRepo {
    suspend fun getSuggestions(text: String): Result<PersistentList<LimaeSuggestionBundle>>

    suspend fun addStringsToDictionary(customStrings: List<String>): Result<Unit>

    suspend fun deleteAnItemFromDictionary(dictionary: Dictionary): Result<Unit>

    fun getAllStringsFromDictionary(): Flow<List<Dictionary>>

    suspend fun deleteAllStringsFromDictionary(): Result<Unit>
}
