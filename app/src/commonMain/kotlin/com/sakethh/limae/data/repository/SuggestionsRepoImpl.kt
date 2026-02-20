package com.sakethh.limae.data.repository

import com.sakethh.limae.domain.LanguageToolEngine
import com.sakethh.limae.domain.SuggestionEngine
import com.sakethh.limae.domain.model.LimaeSuggestion
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.domain.repository.SuggestionsRepo
import com.sakethh.limae.model.HarperEngine
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

class SuggestionsRepoImpl(
    private val harperEngine: HarperEngine,
    private val languageToolEngine: LanguageToolEngine
) : SuggestionsRepo {
    override suspend fun getSuggestions(text: String): Result<PersistentList<LimaeSuggestionBundle>> {
        return try {
            val harperSuggestions = harperEngine.checkText(text)
            val languageToolSuggestions = languageToolEngine.checkText(text)

            val limaeSuggestionBundles = mutableListOf<LimaeSuggestionBundle>()

            harperSuggestions.forEach { harperSuggestion ->
                val hSuggestions = harperSuggestion.suggestions
                val hStartIndex = harperSuggestion.startIndex
                val hEndIndex = harperSuggestion.endIndex
                val hMessage = harperSuggestion.message

                if (hSuggestions.isNotEmpty() && hStartIndex != null && hEndIndex != null && hMessage != null) {
                    limaeSuggestionBundles.add(
                        LimaeSuggestionBundle(
                            suggestion = LimaeSuggestion(
                                startIndex = hStartIndex,
                                endIndex = hEndIndex,
                                message = hMessage,
                                suggestions = hSuggestions,
                                kind = harperSuggestion.kind
                            ), engine = SuggestionEngine.Harper
                        )
                    )
                }
            }

            languageToolSuggestions.forEach { languageToolSuggestion ->
                val ltSuggestions = languageToolSuggestion.suggestions
                val ltStartIndex = languageToolSuggestion.startIndex
                val ltEndIndex = languageToolSuggestion.endIndex
                val ltMessage = languageToolSuggestion.message

                if (ltSuggestions.isNotEmpty() && ltStartIndex != null && ltEndIndex != null && ltMessage != null) {
                    limaeSuggestionBundles.add(
                        LimaeSuggestionBundle(
                            suggestion = LimaeSuggestion(
                                startIndex = ltStartIndex,
                                endIndex = ltEndIndex,
                                message = ltMessage,
                                suggestions = ltSuggestions,
                                kind = languageToolSuggestion.kind
                            ), engine = SuggestionEngine.LanguageTool
                        )
                    )
                }
            }

            Result.success(limaeSuggestionBundles.toPersistentList())
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}