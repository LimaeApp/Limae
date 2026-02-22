package com.sakethh.limae.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.sakethh.limae.Dictionary
import com.sakethh.limae.DictionaryQueries
import com.sakethh.limae.domain.HarperEngineRepo
import com.sakethh.limae.domain.LanguageToolEngineRepo
import com.sakethh.limae.domain.LimaeDispatchers
import com.sakethh.limae.domain.Result
import com.sakethh.limae.domain.SuggestionEngine
import com.sakethh.limae.domain.model.LimaeSuggestion
import com.sakethh.limae.domain.model.LimaeSuggestionBundle
import com.sakethh.limae.domain.repository.SuggestionsRepo
import com.sakethh.limae.utils.getRandomUUIDv7
import com.sakethh.limae.utils.runSafe
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SuggestionsRepoImpl(
    private val harperEngineRepo: HarperEngineRepo,
    private val languageToolEngineRepo: LanguageToolEngineRepo,
    private val dictionaryQueries: DictionaryQueries,
    private val limaeDispatchers: LimaeDispatchers,
) : SuggestionsRepo {
    private val dictionaryStringsLookup by
        lazy {
            dictionaryQueries
                .getAllStrings()
                .executeAsList()
                .map {
                    it.string
                }.toHashSet()
        }

    override suspend fun getSuggestions(text: String): Result<PersistentList<LimaeSuggestionBundle>> =
        runSafe {
            val harperSuggestions = harperEngineRepo.checkText(text)
            val languageToolSuggestions = languageToolEngineRepo.checkText(text)

            val limaeSuggestionBundles = mutableListOf<LimaeSuggestionBundle>()

            harperSuggestions.forEach { harperSuggestion ->
                val hSuggestions = harperSuggestion.suggestions
                val hStartIndex = harperSuggestion.startIndex
                val hEndIndex = harperSuggestion.endIndex
                val hMessage = harperSuggestion.message

                if (hSuggestions.isNotEmpty() && hStartIndex != null && hEndIndex != null && hMessage != null &&
                    !dictionaryStringsLookup.contains(
                        text.substring(startIndex = hStartIndex, endIndex = hEndIndex),
                    )
                ) {
                    limaeSuggestionBundles.add(
                        LimaeSuggestionBundle(
                            suggestion =
                                LimaeSuggestion(
                                    refId = getRandomUUIDv7(),
                                    errorSequence =
                                        text.substring(
                                            startIndex = hStartIndex,
                                            endIndex = hEndIndex,
                                        ),
                                    startIndex = hStartIndex,
                                    endIndex = hEndIndex,
                                    message = hMessage,
                                    suggestions = hSuggestions,
                                    kind = harperSuggestion.kind,
                                ),
                            engine = SuggestionEngine.Harper,
                        ),
                    )
                }
            }

            languageToolSuggestions.forEach { languageToolSuggestion ->
                val ltSuggestions = languageToolSuggestion.suggestions
                val ltStartIndex = languageToolSuggestion.startIndex
                val ltEndIndex = languageToolSuggestion.endIndex
                val ltMessage = languageToolSuggestion.message

                if (ltSuggestions.isNotEmpty() && ltStartIndex != null && ltEndIndex != null && ltMessage != null &&
                    !dictionaryStringsLookup.contains(
                        text.substring(startIndex = ltStartIndex, endIndex = ltEndIndex),
                    )
                ) {
                    limaeSuggestionBundles.add(
                        LimaeSuggestionBundle(
                            suggestion =
                                LimaeSuggestion(
                                    refId = getRandomUUIDv7(),
                                    errorSequence =
                                        text.substring(
                                            startIndex = ltStartIndex,
                                            endIndex = ltEndIndex,
                                        ),
                                    startIndex = ltStartIndex,
                                    endIndex = ltEndIndex,
                                    message = ltMessage,
                                    suggestions = ltSuggestions,
                                    kind = languageToolSuggestion.kind,
                                ),
                            engine = SuggestionEngine.LanguageTool,
                        ),
                    )
                }
            }
            limaeSuggestionBundles.toPersistentList()
        }

    override suspend fun addStringToDictionary(string: String): Result<Unit> =
        runSafe {
            withContext(limaeDispatchers.IO) {
                dictionaryQueries.addStringToDictionary(
                    string = string,
                    id = getRandomUUIDv7(),
                )
                dictionaryStringsLookup.add(string)
            }
        }

    override suspend fun deleteAnItemFromDictionary(dictionary: Dictionary): Result<Unit> =
        runSafe {
            withContext(limaeDispatchers.IO) {
                dictionaryQueries.deleteAString(
                    id = dictionary.id,
                )
                dictionaryStringsLookup.remove(dictionary.string)
            }
        }

    override fun getAllStringsFromDictionary(): Flow<List<Dictionary>> =
        dictionaryQueries.getAllStrings().asFlow().mapToList(limaeDispatchers.IO)
}
