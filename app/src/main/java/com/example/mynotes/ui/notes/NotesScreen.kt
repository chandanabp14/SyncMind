package com.example.mynotes.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mynotes.domain.model.NoteCategory
import com.example.mynotes.ui.components.CategoryChip
import com.example.mynotes.ui.components.NoteCard
import com.example.mynotes.ui.components.VoiceMemoRecorderBottomSheet
import com.example.mynotes.ui.theme.AiBadgeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    state: NotesState,
    onIntent: (NotesIntent) -> Unit,
    onNoteClick: (Long) -> Unit,
    onCreateNoteClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SyncMind",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "On-Device AI",
                            tint = AiBadgeColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { onIntent(NotesIntent.StartVoiceRecording) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Memo")
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExtendedFloatingActionButton(
                    onClick = onCreateNoteClick,
                    icon = { Icon(Icons.Default.Add, contentDescription = "New Note") },
                    text = { Text("New Note") },
                    containerColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onIntent(NotesIntent.SearchQueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search notes or AI summaries...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
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
                        isSelected = state.selectedCategory == category,
                        onSelected = { onIntent(NotesIntent.CategorySelected(category)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading || state.isAiProcessing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.isAiProcessing) "✨ Processing Voice Memo with Local AI..." else "Loading notes...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else if (state.filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No notes found. Tap '+' to create one or record a voice memo!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(state.filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) }
                        )
                    }
                }
            }
        }

        if (state.isRecordingVoiceMemo) {
            VoiceMemoRecorderBottomSheet(
                isRecording = true,
                recordingDurationSeconds = state.recordingDurationSeconds,
                onStartRecording = { onIntent(NotesIntent.StartVoiceRecording) },
                onStopAndProcessRecording = { onIntent(NotesIntent.StopAndProcessVoiceRecording) },
                onCancelRecording = { onIntent(NotesIntent.CancelVoiceRecording) },
                onDismiss = { onIntent(NotesIntent.CancelVoiceRecording) }
            )
        }
    }
}
