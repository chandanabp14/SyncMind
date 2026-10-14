package com.example.mynotes.domain.model

enum class NoteCategory(val displayName: String, val emoji: String) {
    ALL("All", "📌"),
    PERSONAL("Personal", "👤"),
    WORK("Work", "💼"),
    IDEAS("Ideas", "💡"),
    ACTION_ITEMS("Tasks", "✅"),
    VOICE_MEMO("Voice Memos", "🎙️");

    companion object {
        fun fromString(value: String): NoteCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: IDEAS
        }
    }
}
