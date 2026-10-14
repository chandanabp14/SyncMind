package com.example.mynotes.ui.notes

import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.model.NoteCategory

data class NotesState(
    val notes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val selectedCategory: NoteCategory = NoteCategory.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isRecordingVoiceMemo: Boolean = false,
    val recordingDurationSeconds: Int = 0,
    val isAiProcessing: Boolean = false,
    val errorMessage: String? = null
)
