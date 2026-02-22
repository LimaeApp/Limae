package com.sakethh.limae.domain.repository

import com.sakethh.limae.Note
import com.sakethh.limae.domain.EventTimestamp
import com.sakethh.limae.domain.InsertedRowId
import com.sakethh.limae.domain.Result
import com.sakethh.limae.domain.RowInsertionCount
import kotlinx.coroutines.flow.Flow

interface NotesRepo {
    suspend fun deleteANoteById(id: String): Result<Unit>

    /**
     * Returns the pair of `id` and `lastModified`
     * */
    suspend fun insertANote(
        title: String,
        content: String,
    ): Result<Triple<InsertedRowId, RowInsertionCount, EventTimestamp>>

    /**
     * Returns the `lastModified` of saved note
     * */
    suspend fun updateANoteById(
        id: String,
        title: String,
        content: String,
    ): Result<Long>

    suspend fun getANoteById(id: String): Result<Note>

    fun getAllNotes(): Flow<List<Note>>
}
