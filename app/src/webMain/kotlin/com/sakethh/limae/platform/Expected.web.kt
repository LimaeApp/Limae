package com.sakethh.limae.platform

import com.sakethh.limae.model.LimaeNote
import kotlinx.serialization.json.Json

@JsModule("harper-binding")
external object RustWasmBridge {
    fun lint(text: String): String
}

actual object HarperEngine {
    actual fun checkText(text: String): List<LimaeNote> {
        return try {
            val json = RustWasmBridge.lint(text)
            Json.decodeFromString(json)
        } catch (e: Exception) {
            println("Wasm Error: ${e.message}")
            emptyList()
        }
    }
}