package com.sakethh.limae.domain.model

import androidx.compose.runtime.Stable
import com.sakethh.limae.domain.LintKind
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class LimaeSuggestion(
    val refId: String,
    val errorSequence: String,
    val startIndex: Int,
    val endIndex: Int,
    val message: String,
    val suggestions: List<String> = emptyList(),
    val kind: LintKind = LintKind.LimaeNull,
)
