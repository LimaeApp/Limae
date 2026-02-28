package com.sakethh.limae.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import com.sakethh.limae.LimaeDatabase
import com.sakethh.limae.domain.EngineSuggestion
import com.sakethh.limae.domain.HarperEngineRepo
import com.sakethh.limae.domain.LanguageToolEngineRepo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

@JsModule("harper-binding")
external object RustWasmBridge {
    fun lint(text: String): String
}

actual object HarperEngine : HarperEngineRepo {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> =
        try {
            val json = RustWasmBridge.lint(text)
            Json.decodeFromString(json)
        } catch (e: Exception) {
            println("Wasm Error: ${e.message}")
            emptyList()
        }
}

actual val platform: Platform =
    object : Platform {
        override val version: Int? = null
        override val type: Platform.Type = Platform.Type.Web
    }

actual object LanguageToolEngine : LanguageToolEngineRepo {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> = emptyList()
}

actual val LimaeIODispatcher: CoroutineDispatcher = Dispatchers.Default

// private fun jsWorker(): Worker = js("""new URL(/* webpackIgnore: true */ "sqlite.worker.js", import.meta.url)""")
actual suspend fun platformDatabaseModule(): Module {
    val driver = DummySQlDriver // WebWorkerDriver(jsWorker())
    // LimaeDatabase.Schema.create(driver).await()
    return module {
        single { LimaeDatabase.invoke(driver) }.bind<LimaeDatabase>()
    }
}

val DummySQlDriver =
    object : SqlDriver {
        override fun <R> executeQuery(
            identifier: Int?,
            sql: String,
            mapper: (SqlCursor) -> QueryResult<R>,
            parameters: Int,
            binders: (SqlPreparedStatement.() -> Unit)?,
        ): QueryResult<R> =
            mapper(
                object : SqlCursor {
                    override fun next(): QueryResult<Boolean> = QueryResult.Value(false)

                    override fun getString(index: Int): String? = null

                    override fun getLong(index: Int): Long? = null

                    override fun getBytes(index: Int): ByteArray? = null

                    override fun getDouble(index: Int): Double? = null

                    override fun getBoolean(index: Int): Boolean? = null
                },
            )

        override fun execute(
            identifier: Int?,
            sql: String,
            parameters: Int,
            binders: (SqlPreparedStatement.() -> Unit)?,
        ): QueryResult<Long> = QueryResult.Value(-4545)

        override fun newTransaction(): QueryResult<Transacter.Transaction> =
            QueryResult.Value(
                object : Transacter.Transaction() {
                    override val enclosingTransaction: Transacter.Transaction? = null

                    override fun endTransaction(successful: Boolean): QueryResult<Unit> = QueryResult.Unit
                },
            )

        override fun currentTransaction(): Transacter.Transaction? = null

        override fun addListener(
            vararg queryKeys: String,
            listener: Query.Listener,
        ) {
        }

        override fun removeListener(
            vararg queryKeys: String,
            listener: Query.Listener,
        ) {
        }

        override fun notifyListeners(vararg queryKeys: String) {}

        override fun close() {}
    }

@Composable
actual fun dynamicLightTheme(): ColorScheme = lightColorScheme()

@Composable
actual fun dynamicDarkTheme(): ColorScheme = darkColorScheme()

actual val isReadTextFieldAccessibilityServiceRunning: Flow<Boolean> = flowOf(true)
