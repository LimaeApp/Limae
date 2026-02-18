package com.sakethh.limae.domain.model

import androidx.compose.runtime.Stable
import com.sakethh.limae.domain.SuggestionEngine

@Stable
data class LimaeSuggestionBundle(
    val suggestion: LimaeSuggestion,
    val engine: SuggestionEngine
)
