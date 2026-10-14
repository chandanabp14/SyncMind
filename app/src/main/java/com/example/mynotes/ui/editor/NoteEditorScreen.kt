package com.example.mynotes.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mynotes.domain.model.NoteCategory
import com.example.mynotes.ui.components.CategoryChip
import com.example.mynotes.ui.components.TaskItemView
import com.example.mynotes.ui.theme.AiBadgeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorState,
    onIntent: (NoteEditorIntent) -> Unit,
    onBackClick: () -> Unit
) {
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.noteId == 0L) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onIntent(NoteEditorIntent.TogglePinned) }) {
                        Icon(
                            imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (state.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { onIntent(NoteEditorIntent.SaveNote) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save Note", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { onIntent(NoteEditorIntent.TitleChanged(it)) },
                placeholder = { Text("Note Title", style = MaterialTheme.typography.titleLarge) },
                textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(NoteCategory.entries.toTypedArray()) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = state.category == category,
                        onSelected = { onIntent(NoteEditorIntent.CategorySelected(category)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onIntent(NoteEditorIntent.GenerateAiSummary) },
                    colors = ButtonDefaults.buttonColors(containerColor = AiBadgeColor),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("✨ Summarize")
                }

                OutlinedButton(
                    onClick = { onIntent(NoteEditorIntent.ExtractAiTasks) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AiBadgeColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⚡ Extract Tasks")
                }
            }

            if (state.isAiGenerating) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = AiBadgeColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Local AI Processing...", style = MaterialTheme.typography.labelLarge, color = AiBadgeColor)
                }
            }

            AnimatedVisibility(visible = state.aiSummary.isNotBlank()) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AiBadgeColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AiBadgeColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Key Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AiBadgeColor)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = state.aiSummary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            if (state.audioPath != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🎙️ Voice Memo (${state.audioDurationSeconds}s)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        IconButton(onClick = { onIntent(NoteEditorIntent.ToggleAudioPlay) }) {
                            Icon(
                                imageVector = if (state.isPlayingAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Play/Stop",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.content,
                onValueChange = { onIntent(NoteEditorIntent.ContentChanged(it)) },
                placeholder = { Text("Write note content here or paste raw thoughts...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                maxLines = 15
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Action Items / Tasks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.newTaskTitle,
                    onValueChange = { onIntent(NoteEditorIntent.NewTaskTitleChanged(it)) },
                    placeholder = { Text("Add new action item...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onIntent(NoteEditorIntent.AddTask) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            state.tasks.forEach { task ->
                TaskItemView(
                    task = task,
                    onToggle = { onIntent(NoteEditorIntent.ToggleTask(it)) },
                    onDelete = { onIntent(NoteEditorIntent.DeleteTask(it)) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
