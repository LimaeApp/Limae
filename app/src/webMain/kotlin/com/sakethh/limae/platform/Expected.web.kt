package com.sakethh.limae.platform

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
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

@JsModule("harper-binding")
external object RustWasmBridge {
    fun lint(text: String): String
}

actual object HarperEngine : HarperEngineRepo {
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

actual object LanguageToolEngine : LanguageToolEngineRepo {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> = emptyList()
}

actual val LimaeIODispatcher: CoroutineDispatcher = Dispatchers.Default


// private fun jsWorker(): Worker = js("""new URL(/* webpackIgnore: true */ "sqlite.worker.js", import.meta.url)""")
actual suspend fun platformDatabaseModule(): Module {
    val driver = DummySQlDriver /*WebWorkerDriver(jsWorker())*/
   // LimaeDatabase.Schema.create(driver).await()
    return module {
        single { LimaeDatabase.invoke(driver) }.bind<LimaeDatabase>()
    }
}


val DummySQlDriver = object : SqlDriver {
    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<R> {
        return mapper(object : SqlCursor {
            override fun next(): QueryResult<Boolean> {
                return QueryResult.Value(false)
            }

            override fun getString(index: Int): String? {
                return null
            }

            override fun getLong(index: Int): Long? {
                return null
            }

            override fun getBytes(index: Int): ByteArray? {
                return null
            }

            override fun getDouble(index: Int): Double? {
                return null
            }

            override fun getBoolean(index: Int): Boolean? {
                return null
            }
        })
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?
    ): QueryResult<Long> {
        return QueryResult.Value(-4545)
    }

    override fun newTransaction(): QueryResult<Transacter.Transaction> {
        return QueryResult.Value(object : Transacter.Transaction() {
            override val enclosingTransaction: Transacter.Transaction? = null

            override fun endTransaction(successful: Boolean): QueryResult<Unit> {
                return QueryResult.Unit
            }
        })
    }

    override fun currentTransaction(): Transacter.Transaction? {
        return null
    }

    override fun addListener(
        vararg queryKeys: String,
        listener: Query.Listener
    ) {
    }

    override fun removeListener(
        vararg queryKeys: String,
        listener: Query.Listener
    ) {
    }

    override fun notifyListeners(vararg queryKeys: String) {}

    override fun close() {}
}