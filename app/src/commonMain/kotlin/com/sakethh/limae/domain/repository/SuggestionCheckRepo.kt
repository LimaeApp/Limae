package com.sakethh.limae.domain.repository

import com.sakethh.limae.model.LimaeNote
import kotlinx.collections.immutable.PersistentList

interface SuggestionCheckRepo {
    suspend fun viaHarper(text: String): Result<PersistentList<LimaeNote>>
}