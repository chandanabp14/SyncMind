package com.example.mynotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.model.NoteCategory

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val aiSummary: String = "",
    val category: String = NoteCategory.IDEAS.name,
    val audioPath: String? = null,
    val audioDurationSeconds: Int = 0,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(tasks: List<com.example.mynotes.domain.model.Task> = emptyList()): Note {
        return Note(
            id = id,
            title = title,
            content = content,
            aiSummary = aiSummary,
            category = NoteCategory.fromString(category),
            audioPath = audioPath,
            audioDurationSeconds = audioDurationSeconds,
            isPinned = isPinned,
            tasks = tasks,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomainModel(note: Note): NoteEntity {
            return NoteEntity(
                id = note.id,
                title = note.title,
                content = note.content,
                aiSummary = note.aiSummary,
                category = note.category.name,
                audioPath = note.audioPath,
                audioDurationSeconds = note.audioDurationSeconds,
                isPinned = note.isPinned,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt
            )
        }
    }
}
