package com.sakethh.limae.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LimaeError(
    val startIndex: Int,
    val endIndex: Int,
    val message: String,
    val suggestions: List<String>,
    val kind: String
)