package com.example.mynotes.data.ai

import android.content.Context
import com.example.mynotes.domain.model.NoteCategory
import com.example.mynotes.domain.model.Task
import com.example.mynotes.domain.model.TaskPriority
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalAiEngine @Inject constructor(
    private val context: Context
) {
    private var generativeModel: GenerativeModel? = null

    init {
        try {
            generativeModel = Generation.getClient()
        } catch (_: Exception) {
            // Fallback to offline rule-based / NLP engine
        }
    }

    suspend fun summarize(text: String): String = withContext(Dispatchers.Default) {
        if (text.isBlank()) return@withContext "Empty note."

        // Attempt ML Kit GenAI Prompt API first if initialized
        generativeModel?.let { model ->
            try {
                val prompt = "Summarize the following text concisely in 2-3 bullet points:\n\n$text"
                val response = model.generateContent(prompt)
                val summaryText = response.candidates.firstOrNull()?.text?.toString()
                if (!summaryText.isNullOrBlank()) {
                    return@withContext summaryText.trim()
                }
            } catch (_: Exception) {
                // Fallback to intelligent local NLP engine
            }
        }

        // Local Intelligent Summarizer
        val sentences = text.lines()
            .flatMap { it.split(Regex("(?<=[.!?])\\s+")) }
            .map { it.trim() }
            .filter { it.length > 5 }

        if (sentences.isEmpty()) return@withContext text.take(100)

        val bulletPoints = mutableListOf<String>()
        val keyKeywords = listOf("key", "important", "must", "summary", "need", "plan", "review", "agenda", "decision", "goal")

        for (sentence in sentences) {
            if (keyKeywords.any { sentence.lowercase().contains(it) } || sentence.startsWith("-") || sentence.startsWith("*")) {
                val cleaned = sentence.removePrefix("-").removePrefix("*").trim()
                if (cleaned.isNotBlank()) {
                    bulletPoints.add("• $cleaned")
                }
            }
            if (bulletPoints.size >= 3) break
        }

        if (bulletPoints.isEmpty()) {
            sentences.take(3).forEach { bulletPoints.add("• $it") }
        }

        return@withContext bulletPoints.joinToString("\n")
    }

    suspend fun extractTasks(text: String, noteId: Long = 0): List<Task> = withContext(Dispatchers.Default) {
        if (text.isBlank()) return@withContext emptyList()

        val tasks = mutableListOf<Task>()
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }

        for (line in lines) {
            val lowerLine = line.lowercase()
            val isExplicitTask = lowerLine.contains("todo") ||
                    lowerLine.contains("[ ]") ||
                    lowerLine.startsWith("need to") ||
                    lowerLine.startsWith("must") ||
                    lowerLine.startsWith("call ") ||
                    lowerLine.startsWith("email ") ||
                    lowerLine.startsWith("buy ") ||
                    lowerLine.startsWith("schedule ")

            if (isExplicitTask) {
                val cleanTitle = line
                    .removePrefix("- [ ]")
                    .removePrefix("* [ ]")
                    .removePrefix("-")
                    .removePrefix("*")
                    .replace(Regex("(?i)^TODO:?\\s*"), "")
                    .replace(Regex("(?i)^TASK:?\\s*"), "")
                    .trim()

                if (cleanTitle.length > 2) {
                    val priority = when {
                        lowerLine.contains("urgent") || lowerLine.contains("asap") || lowerLine.contains("important") -> TaskPriority.HIGH
                        lowerLine.contains("maybe") || lowerLine.contains("later") -> TaskPriority.LOW
                        else -> TaskPriority.MEDIUM
                    }

                    tasks.add(
                        Task(
                            noteId = noteId,
                            title = cleanTitle,
                            isCompleted = false,
                            priority = priority
                        )
                    )
                }
            }
        }

        tasks.distinctBy { it.title.lowercase() }
    }

    suspend fun autoCategorize(title: String, content: String): NoteCategory = withContext(Dispatchers.Default) {
        val combined = "$title $content".lowercase()

        when {
            combined.contains("work") || combined.contains("project") || combined.contains("client") || combined.contains("meeting") || combined.contains("deadline") || combined.contains("email") -> NoteCategory.WORK
            combined.contains("buy") || combined.contains("grocery") || combined.contains("home") || combined.contains("family") || combined.contains("doctor") -> NoteCategory.PERSONAL
            combined.contains("todo") || combined.contains("task") || combined.contains("checklist") || combined.contains("action item") -> NoteCategory.ACTION_ITEMS
            combined.contains("voice") || combined.contains("transcript") || combined.contains("audio") || combined.contains("memo") -> NoteCategory.VOICE_MEMO
            else -> NoteCategory.IDEAS
        }
    }

    fun close() {
        try {
            generativeModel?.close()
        } catch (_: Exception) {}
    }
}
