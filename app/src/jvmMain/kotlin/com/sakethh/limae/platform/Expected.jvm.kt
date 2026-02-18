package com.sakethh.limae.platform

import com.sakethh.limae.HarperJVMEngine
import com.sakethh.limae.domain.LanguageToolEngine
import com.sakethh.limae.model.EngineSuggestion
import com.sakethh.limae.model.HarperEngine
import com.sakethh.limae.model.LintKind
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