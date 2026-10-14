package com.example.mynotes.domain.model

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val aiSummary: String = "",
    val category: NoteCategory = NoteCategory.IDEAS,
    val audioPath: String? = null,
    val audioDurationSeconds: Int = 0,
    val isPinned: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
