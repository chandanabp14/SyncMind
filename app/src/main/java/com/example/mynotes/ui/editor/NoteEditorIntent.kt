package com.example.mynotes.ui.editor

import com.example.mynotes.domain.model.NoteCategory
import com.example.mynotes.domain.model.Task

sealed interface NoteEditorIntent {
    data class TitleChanged(val title: String) : NoteEditorIntent
    data class ContentChanged(val content: String) : NoteEditorIntent
    data class CategorySelected(val category: NoteCategory) : NoteEditorIntent
    data class NewTaskTitleChanged(val taskTitle: String) : NoteEditorIntent
    data class ToggleTask(val task: Task) : NoteEditorIntent
    data class DeleteTask(val task: Task) : NoteEditorIntent
    data object AddTask : NoteEditorIntent
    data object TogglePinned : NoteEditorIntent
    data object GenerateAiSummary : NoteEditorIntent
    data object ExtractAiTasks : NoteEditorIntent
    data object ToggleAudioPlay : NoteEditorIntent
    data object SaveNote : NoteEditorIntent
}
