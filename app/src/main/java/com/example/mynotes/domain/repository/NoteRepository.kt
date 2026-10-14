package com.example.mynotes.domain.repository

import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note?
    suspend fun saveNote(note: Note): Long
    suspend fun deleteNote(id: Long)
    suspend fun toggleTaskCompletion(task: Task)
    suspend fun saveTasks(tasks: List<Task>)
    suspend fun deleteTask(task: Task)
}
