package com.sakethh.limae.domain

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class EngineSuggestion(
    val startIndex: Int? = null,
    val endIndex: Int? = null,
    val message: String? = null,
    val suggestions: List<String> = emptyList(),
    val kind: LintKind = LintKind.LimaeNull
)