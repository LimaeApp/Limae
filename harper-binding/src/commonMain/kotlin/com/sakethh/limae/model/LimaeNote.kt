package com.sakethh.limae.model

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Stable
data class LimaeNote(
    val startIndex: Int = -41545,
    val endIndex: Int = -45645,
    val message: String = "",
    val suggestions: List<String> = emptyList(),
    val kind: LintKind = LintKind.LimaeNull
)