package com.sakethh.limae.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LimaeSchema(
    val schemaVersion: Long = 1,
    val dictionary: List<String>,
    val drafts: List<Draft>,
) {
    @Serializable
    data class Draft(
        val title: String,
        val content: String,
        val lastModified: Long,
    )
}
