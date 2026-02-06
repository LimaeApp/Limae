package com.sakethh.limae
import com.sakethh.limae.model.HarperEngine
import com.sakethh.limae.model.LimaeSuggestionNote
import kotlinx.serialization.json.Json

object HarperEngine: HarperEngine {
    init {
        System.loadLibrary("harper_binding")
    }

    private external fun lintNative(text: String): String

   override fun checkText(text: String): List<LimaeSuggestionNote> {
        if (text.isBlank()) return emptyList()

        val jsonResult = lintNative(text)
        return try {
            Json.decodeFromString<List<LimaeSuggestionNote>>(jsonResult)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}