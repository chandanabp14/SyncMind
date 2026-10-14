package com.example.mynotes.domain.model

data class Task(
    val id: Long = 0,
    val noteId: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH
}
