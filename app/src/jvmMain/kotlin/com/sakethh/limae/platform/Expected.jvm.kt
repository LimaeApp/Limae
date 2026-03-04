package com.sakethh.limae.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.sakethh.limae.HarperJVMEngine
import com.sakethh.limae.LimaeDatabase
import com.sakethh.limae.domain.EngineSuggestion
import com.sakethh.limae.domain.HarperEngineRepo
import com.sakethh.limae.domain.LanguageToolEngineRepo
import com.sakethh.limae.domain.LintKind
import com.sakethh.limae.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.languagetool.JLanguageTool
import org.languagetool.language.AmericanEnglish
import org.languagetool.rules.RuleMatch
import java.io.File

actual object LanguageToolEngine : LanguageToolEngineRepo {
    private val languageTool = JLanguageTool(AmericanEnglish.getInstance())

    actual override suspend fun checkText(text: String): List<EngineSuggestion> =
        languageTool.check(text).map {
            EngineSuggestion(
                startIndex = it.fromPos,
                endIndex = it.toPos,
                message = it.shortMessage,
                suggestions = it.suggestedReplacements,
                kind =
                    when (it.type) {
                        RuleMatch.Type.UnknownWord -> LintKind.Spelling
                        RuleMatch.Type.Hint -> LintKind.Enhancement
                        RuleMatch.Type.Other -> LintKind.Miscellaneous
                    },
            )
        }
}

actual object HarperEngine : HarperEngineRepo {
    actual override suspend fun checkText(text: String): List<EngineSuggestion> = HarperJVMEngine.checkText(text)
}

actual val platform: Platform =
    object : Platform {
        override val version: Int? = null
        override val type: Platform.Type = Platform.Type.Desktop
    }

actual val LimaeIODispatcher: CoroutineDispatcher = Dispatchers.IO

val limaeSpecificFolder =
    System.getProperty("user.home").run {
        val appDataDir = File(this, ".limae")
        if (appDataDir.exists().not()) {
            appDataDir.mkdirs()
        }
        appDataDir
    }

actual suspend fun platformDatabaseModule(): Module =
    module {
        single {
            val driver: SqlDriver =
                JdbcSqliteDriver(url = "jdbc:sqlite:${limaeSpecificFolder.absolutePath}/${Constants.DATABASE_NAME}")
            runBlocking {
                LimaeDatabase.Schema.create(driver).await()
            }
            LimaeDatabase.invoke(driver)
        }.bind<LimaeDatabase>()
    }

@Composable
actual fun dynamicLightTheme(): ColorScheme = lightColorScheme()

@Composable
actual fun dynamicDarkTheme(): ColorScheme = darkColorScheme()

actual val isReadTextFieldAccessibilityServiceRunning: StateFlow<Boolean> = MutableStateFlow(true)

actual inline fun runBlockingNonWeb(crossinline block: suspend () -> Unit) {
    runBlocking {
        block()
    }
}
