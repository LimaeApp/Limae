package com.sakethh.limae.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.sakethh.limae.HarperJVMEngine
import com.sakethh.limae.LimaeDatabase
import com.sakethh.limae.domain.LanguageToolEngine
import com.sakethh.limae.model.EngineSuggestion
import com.sakethh.limae.model.HarperEngine
import com.sakethh.limae.model.LintKind
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.languagetool.JLanguageTool
import org.languagetool.language.AmericanEnglish
import org.languagetool.rules.RuleMatch

actual object LanguageToolEngine : LanguageToolEngine {
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

actual object HarperEngine : HarperEngine {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> {
        return HarperJVMEngine.checkText(text)
    }
}

actual val platform: Platform = Platform.Desktop
actual val LimaeIODispatcher: CoroutineDispatcher = Dispatchers.IO

actual fun getSqlDriver(): SqlDriver {
    // TODO: remove hardcoded path
    val driver: SqlDriver = JdbcSqliteDriver(url = "jdbc:sqlite:/home/saketh/Documents/Limae/ClientTest.db")
    runBlocking {
        LimaeDatabase.Schema.create(driver).await()
    }
    return driver
}