package com.example.mynotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mynotes.domain.model.Task
import com.example.mynotes.domain.model.TaskPriority

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["noteId"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: String = TaskPriority.MEDIUM.name
) {
    fun toDomainModel(): Task {
        return Task(
            id = id,
            noteId = noteId,
            title = title,
            isCompleted = isCompleted,
            priority = try { TaskPriority.valueOf(priority) } catch (_: Exception) { TaskPriority.MEDIUM }
        )
    }

    companion object {
        fun fromDomainModel(task: Task): TaskEntity {
            return TaskEntity(
                id = task.id,
                noteId = task.noteId,
                title = task.title,
                isCompleted = task.isCompleted,
                priority = task.priority.name
            )
        }
    }
}
