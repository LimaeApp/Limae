package com.sakethh.limae.platform

import com.sakethh.limae.domain.LanguageToolEngine
import com.sakethh.limae.model.EngineSuggestion
import com.sakethh.limae.model.HarperEngine
import kotlinx.serialization.json.Json

@JsModule("harper-binding")
external object RustWasmBridge {
    fun lint(text: String): String
}

actual object HarperEngine : HarperEngine {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> {
        return try {
            val json = RustWasmBridge.lint(text)
            Json.decodeFromString(json)
        } catch (e: Exception) {
            println("Wasm Error: ${e.message}")
            emptyList()
        }
    }
}

actual val platform: Platform = Platform.Web

actual object LanguageToolEngine : LanguageToolEngine {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> = emptyList()
}