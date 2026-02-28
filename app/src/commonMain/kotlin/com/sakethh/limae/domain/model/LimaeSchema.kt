package com.sakethh.limae.domain.model

import kotlinx.serialization.Serializable

typealias Package = String

@Serializable
data class LimaeSchema(
    val schemaVersion: Long = 1,
    val dictionary: List<String>,
    val drafts: List<Draft>,
    val appBlocklist: List<Package>,
) {
    @Serializable
    data class Draft(
        val title: String,
        val content: String,
        val lastModified: Long,
    )
}
