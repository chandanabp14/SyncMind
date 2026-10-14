package com.example.mynotes.ui.editor

import com.example.mynotes.domain.model.NoteCategory
import com.example.mynotes.domain.model.Task

data class NoteEditorState(
    val noteId: Long = 0,
    val title: String = "",
    val content: String = "",
    val aiSummary: String = "",
    val category: NoteCategory = NoteCategory.IDEAS,
    val isPinned: Boolean = false,
    val audioPath: String? = null,
    val audioDurationSeconds: Int = 0,
    val tasks: List<Task> = emptyList(),
    val newTaskTitle: String = "",
    val isAiGenerating: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)
