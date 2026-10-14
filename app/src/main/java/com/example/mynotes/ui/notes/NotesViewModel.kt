package com.example.mynotes.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.data.ai.LocalAiEngine
import com.example.mynotes.data.audio.AudioRecorderManager
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.model.NoteCategory
import com.example.mynotes.domain.model.Task
import com.example.mynotes.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val localAiEngine: LocalAiEngine,
    private val audioRecorderManager: AudioRecorderManager
) : ViewModel() {

    private val _state = MutableStateFlow(NotesState(isLoading = true))
    val state: StateFlow<NotesState> = _state.asStateFlow()

    private var recordingTimerJob: Job? = null

    init {
        loadNotes()
    }

    fun onIntent(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = intent.query) }
                filterNotes()
            }
            is NotesIntent.CategorySelected -> {
                _state.update { it.copy(selectedCategory = intent.category) }
                filterNotes()
            }
            is NotesIntent.ToggleTask -> toggleTask(intent.task)
            is NotesIntent.DeleteNote -> deleteNote(intent.noteId)
            is NotesIntent.StartVoiceRecording -> startVoiceRecording()
            is NotesIntent.StopAndProcessVoiceRecording -> stopAndProcessVoiceRecording()
            is NotesIntent.CancelVoiceRecording -> cancelVoiceRecording()
            is NotesIntent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadNotes() {
        viewModelScope.launch {
            repository.getAllNotes()
                .catch { error ->
                    _state.update { it.copy(isLoading = false, errorMessage = error.localizedMessage) }
                }
                .collect { notes ->
                    _state.update { currentState ->
                        currentState.copy(
                            notes = notes,
                            isLoading = false
                        )
                    }
                    filterNotes()
                }
        }
    }

    private fun filterNotes() {
        _state.update { currentState ->
            val query = currentState.searchQuery.lowercase().trim()
            val category = currentState.selectedCategory

            val filtered = currentState.notes.filter { note ->
                val matchesCategory = category == NoteCategory.ALL || note.category == category
                val matchesQuery = query.isEmpty() ||
                        note.title.lowercase().contains(query) ||
                        note.content.lowercase().contains(query) ||
                        note.aiSummary.lowercase().contains(query)
                matchesCategory && matchesQuery
            }
            currentState.copy(filteredNotes = filtered)
        }
    }

    private fun toggleTask(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    private fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    private fun startVoiceRecording() {
        val file = audioRecorderManager.startRecording()
        if (file != null) {
            _state.update { it.copy(isRecordingVoiceMemo = true, recordingDurationSeconds = 0) }
            recordingTimerJob = viewModelScope.launch {
                while (_state.value.isRecordingVoiceMemo) {
                    delay(1000)
                    _state.update { it.copy(recordingDurationSeconds = it.recordingDurationSeconds + 1) }
                }
            }
        } else {
            _state.update { it.copy(errorMessage = "Could not start audio recording.") }
        }
    }

    private fun stopAndProcessVoiceRecording() {
        recordingTimerJob?.cancel()
        val result = audioRecorderManager.stopRecording()
        _state.update { it.copy(isRecordingVoiceMemo = false, isAiProcessing = true) }

        if (result != null) {
            viewModelScope.launch {
                val simulatedTranscript = "Voice memo recorded on ${java.text.SimpleDateFormat.getDateTimeInstance().format(java.util.Date())}.\n" +
                        "Discussed project milestones for SyncMind. We need to implement offline AI summaries, extract actionable task items from raw notes, and test on-device model speed. Remember to follow up on design mockups by Friday."

                val summary = localAiEngine.summarize(simulatedTranscript)
                val tasks = localAiEngine.extractTasks(simulatedTranscript)
                val category = localAiEngine.autoCategorize("Voice Memo", simulatedTranscript)

                val voiceNote = Note(
                    title = "🎙️ Voice Memo (${result.durationSeconds}s)",
                    content = simulatedTranscript,
                    aiSummary = summary,
                    category = category,
                    audioPath = result.filePath,
                    audioDurationSeconds = result.durationSeconds,
                    isPinned = true,
                    tasks = tasks
                )

                repository.saveNote(voiceNote)
                _state.update { it.copy(isAiProcessing = false) }
            }
        } else {
            _state.update { it.copy(isAiProcessing = false, errorMessage = "Failed to save voice recording.") }
        }
    }

    private fun cancelVoiceRecording() {
        recordingTimerJob?.cancel()
        audioRecorderManager.cancelRecording()
        _state.update { it.copy(isRecordingVoiceMemo = false, recordingDurationSeconds = 0) }
    }

    override fun onCleared() {
        super.onCleared()
        localAiEngine.close()
    }
}
