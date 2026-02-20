package com.sakethh.limae.domain.repository

import com.sakethh.limae.Note
import kotlinx.coroutines.flow.Flow
import com.sakethh.limae.domain.Result

interface NotesRepo {
    suspend fun deleteANoteById(id: String): Result<Unit>
    suspend fun insertANote(title: String, content: String): Result<String>
    suspend fun updateANoteById(id: String, title: String, content: String): Result<Unit>
    suspend fun getANoteById(id: String): Result<Note>
    fun getAllNotes(): Flow<List<Note>>
}