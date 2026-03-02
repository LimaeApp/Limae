package com.sakethh.limae.data.repository

import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext

class SuggestionsRepoImpl(
    private val harperEngineRepo: HarperEngineRepo,
    private val languageToolEngineRepo: LanguageToolEngineRepo,
    private val dictionaryQueries: DictionaryQueries,
    private val limaeDispatchers: LimaeDispatchers,
) : SuggestionsRepo {
    private val dictionaryStringsLookup =
        mutableStateSetOf<String>().apply {
            addAll(
                dictionaryQueries.getAllStrings().executeAsList().map {
                    it.string
                },
            )
        }

    override suspend fun getSuggestions(text: String): Flow<Result<PersistentList<LimaeSuggestionBundle>>> =
        flow {
            val harperSuggestions = harperEngineRepo.checkText(text)
            val languageToolSuggestions = languageToolEngineRepo.checkText(text)

            val limaeSuggestionBundles = mutableListOf<LimaeSuggestionBundle>()

            harperSuggestions.forEach { harperSuggestion ->
                val hSuggestions = harperSuggestion.suggestions
                val hStartIndex = harperSuggestion.startIndex
                val hEndIndex = harperSuggestion.endIndex
                val hMessage = harperSuggestion.message

                if (hSuggestions.isNotEmpty() && hStartIndex != null && hEndIndex != null && hMessage != null) {
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

                if (ltSuggestions.isNotEmpty() && ltStartIndex != null && ltEndIndex != null && ltMessage != null) {
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
            emit(limaeSuggestionBundles)
        }.flatMapLatest { limaeSuggestionBundles ->
            snapshotFlow {
                dictionaryStringsLookup.toSet()
            }.transform { stringsInDictionary ->
                try {
                    val filteredList =
                        limaeSuggestionBundles
                            .filter {
                                !stringsInDictionary.contains(it.suggestion.errorSequence)
                            }.toPersistentList()
                    emit(Result.Success(filteredList))
                } catch (e: Exception) {
                    emit(Result.Failure(e))
                }
            }
        }.catch {
            emit(Result.Failure(it))
        }

    override suspend fun addStringsToDictionary(customStrings: List<String>): Result<Unit> =
        runSafe {
            withContext(limaeDispatchers.IO) {
                dictionaryQueries.transactionWithResult {
                    customStrings.forEach { customString ->
                        dictionaryQueries.addStringToDictionary(
                            string = customString,
                            id = getRandomUUIDv7(),
                        )
                    }
                }
                dictionaryStringsLookup.addAll(customStrings)
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

    override suspend fun deleteAllStringsFromDictionary(): Result<Unit> =
        runSafe {
            dictionaryQueries.deleteAll()
        }
}
