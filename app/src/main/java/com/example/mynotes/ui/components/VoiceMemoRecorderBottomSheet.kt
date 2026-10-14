package com.example.mynotes.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mynotes.ui.theme.AiBadgeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceMemoRecorderBottomSheet(
    isRecording: Boolean,
    recordingDurationSeconds: Int,
    onStartRecording: () -> Unit,
    onStopAndProcessRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎙️ Voice Memo & AI Summarizer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Record raw voice notes. Local AI will automatically transcribe, summarize, and extract action items.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isRecording) {
                Text(
                    text = String.format("%02d:%02d", recordingDurationSeconds / 60, recordingDurationSeconds % 60),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Processing Ready",
                        tint = AiBadgeColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Local AI Engine listening...",
                        style = MaterialTheme.typography.labelMedium,
                        color = AiBadgeColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row {
                    OutlinedButton(onClick = onCancelRecording) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = onStopAndProcessRecording,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & AI Summarize")
                    }
                }
            } else {
                Button(
                    onClick = onStartRecording,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Start Recording")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Recording Voice Memo")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
