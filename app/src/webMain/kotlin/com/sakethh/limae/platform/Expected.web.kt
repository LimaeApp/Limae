package com.sakethh.limae.platform

import com.sakethh.limae.model.HarperEngine
import com.sakethh.limae.model.LimaeSuggestionNote
import kotlinx.serialization.json.Json

@JsModule("harper-binding")
external object RustWasmBridge {
    fun lint(text: String): String
}

actual object HarperEngine: HarperEngine {
    actual override fun checkText(text: String): List<LimaeSuggestionNote> {
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
