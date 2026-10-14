package com.example.mynotes.ui.notes

import com.example.mynotes.domain.model.NoteCategory
import com.example.mynotes.domain.model.Task

sealed interface NotesIntent {
    data class SearchQueryChanged(val query: String) : NotesIntent
    data class CategorySelected(val category: NoteCategory) : NotesIntent
    data class ToggleTask(val task: Task) : NotesIntent
    data class DeleteNote(val noteId: Long) : NotesIntent
    data object StartVoiceRecording : NotesIntent
    data object StopAndProcessVoiceRecording : NotesIntent
    data object CancelVoiceRecording : NotesIntent
    data object DismissError : NotesIntent
}
