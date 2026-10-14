package com.example.mynotes.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynotes.data.ai.LocalAiEngine
import com.example.mynotes.data.audio.AudioPlayerManager
import com.example.mynotes.domain.model.Note
import com.example.mynotes.domain.model.Task
import com.example.mynotes.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val localAiEngine: LocalAiEngine,
    private val audioPlayerManager: AudioPlayerManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(NoteEditorState())
    val state: StateFlow<NoteEditorState> = _state.asStateFlow()

    private val noteId: Long = savedStateHandle.get<Long>("noteId") ?: 0L

    init {
        if (noteId != 0L) {
            loadNote(noteId)
        }
    }

    fun onIntent(intent: NoteEditorIntent) {
        when (intent) {
            is NoteEditorIntent.TitleChanged -> _state.update { it.copy(title = intent.title) }
            is NoteEditorIntent.ContentChanged -> _state.update { it.copy(content = intent.content) }
            is NoteEditorIntent.CategorySelected -> _state.update { it.copy(category = intent.category) }
            is NoteEditorIntent.NewTaskTitleChanged -> _state.update { it.copy(newTaskTitle = intent.taskTitle) }
            is NoteEditorIntent.ToggleTask -> toggleTask(intent.task)
            is NoteEditorIntent.DeleteTask -> deleteTask(intent.task)
            is NoteEditorIntent.AddTask -> addTask()
            is NoteEditorIntent.TogglePinned -> _state.update { it.copy(isPinned = !it.isPinned) }
            is NoteEditorIntent.GenerateAiSummary -> generateAiSummary()
            is NoteEditorIntent.ExtractAiTasks -> extractAiTasks()
            is NoteEditorIntent.ToggleAudioPlay -> toggleAudioPlay()
            is NoteEditorIntent.SaveNote -> saveNote()
        }
    }

    private fun loadNote(id: Long) {
        viewModelScope.launch {
            val note = repository.getNoteById(id)
            if (note != null) {
                _state.update {
                    it.copy(
                        noteId = note.id,
                        title = note.title,
                        content = note.content,
                        aiSummary = note.aiSummary,
                        category = note.category,
                        isPinned = note.isPinned,
                        audioPath = note.audioPath,
                        audioDurationSeconds = note.audioDurationSeconds,
                        tasks = note.tasks
                    )
                }
            }
        }
    }

    private fun generateAiSummary() {
        val currentContent = _state.value.content
        if (currentContent.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isAiGenerating = true) }
            val summary = localAiEngine.summarize(currentContent)
            val category = localAiEngine.autoCategorize(_state.value.title, currentContent)
            _state.update {
                it.copy(
                    aiSummary = summary,
                    category = category,
                    isAiGenerating = false
                )
            }
        }
    }

    private fun extractAiTasks() {
        val currentContent = _state.value.content
        if (currentContent.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isAiGenerating = true) }
            val extracted = localAiEngine.extractTasks(currentContent, noteId = _state.value.noteId)
            val currentTasks = _state.value.tasks.toMutableList()

            for (task in extracted) {
                if (currentTasks.none { it.title.equals(task.title, ignoreCase = true) }) {
                    currentTasks.add(task)
                }
            }

            _state.update {
                it.copy(
                    tasks = currentTasks,
                    isAiGenerating = false
                )
            }
        }
    }

    private fun addTask() {
        val taskTitle = _state.value.newTaskTitle.trim()
        if (taskTitle.isBlank()) return

        val newTask = Task(
            noteId = _state.value.noteId,
            title = taskTitle,
            isCompleted = false
        )
        _state.update {
            it.copy(
                tasks = it.tasks + newTask,
                newTaskTitle = ""
            )
        }
    }

    private fun toggleTask(task: Task) {
        val updatedTasks = _state.value.tasks.map {
            if (it == task) it.copy(isCompleted = !it.isCompleted) else it
        }
        _state.update { it.copy(tasks = updatedTasks) }
    }

    private fun deleteTask(task: Task) {
        val updatedTasks = _state.value.tasks.filterNot { it == task }
        _state.update { it.copy(tasks = updatedTasks) }
    }

    private fun toggleAudioPlay() {
        val audioPath = _state.value.audioPath ?: return
        if (audioPlayerManager.isPlaying) {
            audioPlayerManager.stopAudio()
            _state.update { it.copy(isPlayingAudio = false) }
        } else {
            audioPlayerManager.playAudio(audioPath) {
                _state.update { it.copy(isPlayingAudio = false) }
            }
            _state.update { it.copy(isPlayingAudio = true) }
        }
    }

    private fun saveNote() {
        val currentState = _state.value
        val title = currentState.title.ifBlank { "Untitled Note" }

        viewModelScope.launch {
            val note = Note(
                id = currentState.noteId,
                title = title,
                content = currentState.content,
                aiSummary = currentState.aiSummary,
                category = currentState.category,
                isPinned = currentState.isPinned,
                audioPath = currentState.audioPath,
                audioDurationSeconds = currentState.audioDurationSeconds,
                tasks = currentState.tasks,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveNote(note)
            _state.update { it.copy(isSaved = true) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.stopAudio()
    }
}
