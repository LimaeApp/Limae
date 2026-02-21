package com.sakethh.limae.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.sakethh.limae.HarperJVMEngine
import com.sakethh.limae.LimaeDatabase
import com.sakethh.limae.domain.LanguageToolEngineRepo
import com.sakethh.limae.domain.EngineSuggestion
import com.sakethh.limae.domain.HarperEngineRepo
import com.sakethh.limae.domain.LintKind
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.languagetool.JLanguageTool
import org.languagetool.language.AmericanEnglish
import org.languagetool.rules.RuleMatch

actual object LanguageToolEngine : LanguageToolEngineRepo {
    private val languageTool = JLanguageTool(AmericanEnglish.getInstance())
    actual override suspend fun checkText(text: String): List<EngineSuggestion> {
        return languageTool.check(text).map {
            EngineSuggestion(
                startIndex = it.fromPos,
                endIndex = it.toPos,
                message = it.shortMessage,
                suggestions = it.suggestedReplacements,
                kind = when (it.type) {
                    RuleMatch.Type.UnknownWord -> LintKind.Spelling
                    RuleMatch.Type.Hint -> LintKind.Enhancement
                    RuleMatch.Type.Other -> LintKind.Miscellaneous
                }
            )
        }
    }
}

actual object HarperEngine : HarperEngineRepo {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> {
        return HarperJVMEngine.checkText(text)
    }
}

actual val platform: Platform = Platform.Desktop
actual val LimaeIODispatcher: CoroutineDispatcher = Dispatchers.IO

actual suspend fun platformDatabaseModule(): Module = module {
    single {
        val driver: SqlDriver =
            // TODO: remove hardcoded path
            JdbcSqliteDriver(url = "jdbc:sqlite:/home/saketh/Documents/Limae/ClientTest.db")
        runBlocking {
            LimaeDatabase.Schema.create(driver).await()
        }
        LimaeDatabase.invoke(driver)
    }.bind<LimaeDatabase>()
}