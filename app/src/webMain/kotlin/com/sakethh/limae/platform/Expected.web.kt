package com.sakethh.limae.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import com.sakethh.limae.LimaeDatabase
import com.sakethh.limae.domain.LanguageToolEngine
import com.sakethh.limae.model.EngineSuggestion
import com.sakethh.limae.model.HarperEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.w3c.dom.Worker

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

actual val LimaeIODispatcher: CoroutineDispatcher = Dispatchers.Default

actual fun getSqlDriver(): SqlDriver {
    TODO("Not yet implemented")
}