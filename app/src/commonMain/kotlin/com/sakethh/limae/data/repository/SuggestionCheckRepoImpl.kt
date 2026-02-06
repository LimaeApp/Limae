package com.sakethh.limae.data.repository

import com.sakethh.limae.domain.repository.SuggestionCheckRepo
import com.sakethh.limae.model.HarperEngine
import com.sakethh.limae.model.LimaeSuggestionNote
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

class SuggestionCheckRepoImpl(private val harperEngine: HarperEngine) : SuggestionCheckRepo {
    override suspend fun viaHarper(text: String): Result<PersistentList<LimaeSuggestionNote>> {
        return try {
            val limaeErrors = harperEngine.checkText(text)
            Result.success(limaeErrors.filter {
                it.suggestions.isNotEmpty()
            }.toPersistentList())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}