package com.sakethh.limae.data.repository

import app.cash.sqldelight.async.coroutines.awaitAsList
import com.sakethh.limae.DictionaryQueries
import com.sakethh.limae.NoteQueries
import com.sakethh.limae.domain.LimaeDispatchers
import com.sakethh.limae.domain.Result
import com.sakethh.limae.domain.model.LimaeSchema
import com.sakethh.limae.domain.repository.DatabaseUtilsRepo
import com.sakethh.limae.utils.getRandomUUIDv7
import com.sakethh.limae.utils.runSafe
import kotlinx.coroutines.withContext

class DatabaseUtilsRepoImpl(
    private val limaeDispatchers: LimaeDispatchers,
    private val dictionaryQueries: DictionaryQueries,
    private val noteQueries: NoteQueries,
) : DatabaseUtilsRepo {
    override suspend fun getExportData(): Result<LimaeSchema> =
        runSafe {
            withContext(limaeDispatchers.IO) {
                LimaeSchema(
                    dictionary =
                        dictionaryQueries.getAllStrings().awaitAsList().map {
                            it.string
                        },
                    drafts =
                        noteQueries.getAllNotes().awaitAsList().map {
                            LimaeSchema.Draft(
                                title = it.title,
                                content = it.content,
                                lastModified = it.lastModified,
                            )
                        },
                )
            }
        }

    override suspend fun importData(limaeSchema: LimaeSchema): Result<Unit> =
        runSafe {
            withContext(limaeDispatchers.IO) {
                val dictStrings = limaeSchema.dictionary
                dictionaryQueries.transaction {
                    dictStrings.forEach {
                        dictionaryQueries.addStringToDictionary(
                            string = it,
                            id = getRandomUUIDv7(),
                        )
                    }
                }

                val drafts = limaeSchema.drafts
                noteQueries.transaction {
                    drafts.forEach {
                        noteQueries.insertANote(
                            title = it.title,
                            content = it.content,
                            lastModified = it.lastModified,
                            id = getRandomUUIDv7(),
                        )
                    }
                }
            }
        }
}
