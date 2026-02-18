package com.sakethh.limae
import com.sakethh.limae.model.HarperEngine
import com.sakethh.limae.model.EngineSuggestion
import kotlinx.serialization.json.Json

object HarperJVMEngine: HarperEngine {
    init {
        System.loadLibrary("harper_binding")
    }

    private external fun lintNative(text: String): String

   override suspend fun checkText(text: String): List<EngineSuggestion> {
        if (text.isBlank()) return emptyList()

        val jsonResult = lintNative(text)
        return try {
            Json.decodeFromString<List<EngineSuggestion>>(jsonResult)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}