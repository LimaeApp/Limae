package com.sakethh.limae.data.repository

import com.sakethh.limae.domain.repository.SuggestionCheckRepo
import com.sakethh.limae.model.LimaeNote
import com.sakethh.limae.platform.HarperEngine
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

class SuggestionCheckRepoImpl(private val harperEngine: HarperEngine) : SuggestionCheckRepo {
    override suspend fun viaHarper(text: String): Result<PersistentList<LimaeNote>> {
        return try {
            val limaeErrors = harperEngine.checkText(text)
            Result.success(limaeErrors.toPersistentList())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}