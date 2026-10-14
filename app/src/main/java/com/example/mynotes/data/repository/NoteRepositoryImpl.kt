package com.example.mynotes.data.repository

import com.example.mynotes.data.local.dao.NoteDao
import com.example.mynotes.data.local.dao.TaskDao
import com.example.mynotes.data.local.entity.NoteEntity
import com.example.mynotes.data.local.entity.TaskEntity
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.model.Task
import com.example.mynotes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val taskDao: TaskDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { noteEntity ->
                val tasks = taskDao.getTasksForNoteSync(noteEntity.id).map { it.toDomainModel() }
                noteEntity.toDomainModel(tasks)
            }
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query).map { entities ->
            entities.map { noteEntity ->
                val tasks = taskDao.getTasksForNoteSync(noteEntity.id).map { it.toDomainModel() }
                noteEntity.toDomainModel(tasks)
            }
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        val noteEntity = noteDao.getNoteById(id) ?: return null
        val tasks = taskDao.getTasksForNoteSync(id).map { it.toDomainModel() }
        return noteEntity.toDomainModel(tasks)
    }

    override suspend fun saveNote(note: Note): Long {
        val entity = NoteEntity.fromDomainModel(note)
        val noteId = if (note.id == 0L) {
            noteDao.insertNote(entity)
        } else {
            noteDao.updateNote(entity)
            note.id
        }

        if (note.tasks.isNotEmpty()) {
            val taskEntities = note.tasks.map { TaskEntity.fromDomainModel(it.copy(noteId = noteId)) }
            taskDao.insertTasks(taskEntities)
        }
        return noteId
    }

    override suspend fun deleteNote(id: Long) {
        noteDao.deleteNoteById(id)
    }

    override suspend fun toggleTaskCompletion(task: Task) {
        val updated = task.copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(TaskEntity.fromDomainModel(updated))
    }

    override suspend fun saveTasks(tasks: List<Task>) {
        taskDao.insertTasks(tasks.map { TaskEntity.fromDomainModel(it) })
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(TaskEntity.fromDomainModel(task))
    }
}
