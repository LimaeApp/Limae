package com.sakethh.limae
import com.sakethh.limae.model.LimaeNote
import kotlinx.serialization.json.Json

object HarperEngine {
    init {
        System.loadLibrary("harper_binding")
    }

    private external fun lintNative(text: String): String

    fun checkText(text: String): List<LimaeNote> {
        if (text.isBlank()) return emptyList()

        val jsonResult = lintNative(text)
        return try {
            Json.decodeFromString<List<LimaeNote>>(jsonResult)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}