package com.sakethh.limae.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.sakethh.limae.Note
import com.sakethh.limae.NoteQueries
import com.sakethh.limae.domain.LimaeDispatchers
import com.sakethh.limae.domain.Result
import com.sakethh.limae.domain.repository.NotesRepo
import com.sakethh.limae.utils.getEpochSecond
import com.sakethh.limae.utils.getRandomUUIDv7
import com.sakethh.limae.utils.runSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class NotesRepoImpl(
    private val noteQueries: NoteQueries,
    private val limaeDispatchers: LimaeDispatchers
) : NotesRepo {

    override suspend fun deleteANoteById(id: String): Result<Unit> {
        return runSafe {
            withContext(limaeDispatchers.IO) {
                noteQueries.deleteANoteById(id)
            }
        }
    }

    override suspend fun insertANote(
        title: String,
        content: String
    ): Result<String> {
        return runSafe {
            val noteId = getRandomUUIDv7()
            withContext(limaeDispatchers.IO) {
                noteQueries.insertANote(
                    id = noteId,
                    title = title,
                    content = content,
                    lastModified = getEpochSecond()
                )
            }.await()
            noteId
        }
    }

    override suspend fun updateANoteById(
        id: String,
        title: String,
        content: String
    ): Result<Unit> {
        return runSafe {
            withContext(limaeDispatchers.IO) {
                noteQueries.updateANoteById(
                    title = title,
                    content = content,
                    lastModified = getEpochSecond(),
                    id = id
                )
            }
        }
    }

    override suspend fun getANoteById(id: String): Result<Note> {
        return runSafe {
            withContext(limaeDispatchers.IO) {
                noteQueries.getANoteById(
                    id = id
                ).executeAsOne()
            }
        }
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return noteQueries.getAllNotes().asFlow().mapToList(limaeDispatchers.IO)
    }
}