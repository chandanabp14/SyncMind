package com.example.mynotes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mynotes.ui.editor.NoteEditorScreen
import com.example.mynotes.ui.editor.NoteEditorViewModel
import com.example.mynotes.ui.notes.NotesScreen
import com.example.mynotes.ui.notes.NotesViewModel

@Composable
fun SyncMindNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "notes"
    ) {
        composable("notes") {
            val viewModel: NotesViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            NotesScreen(
                state = state,
                onIntent = viewModel::onIntent,
                onNoteClick = { noteId -> navController.navigate("editor/$noteId") },
                onCreateNoteClick = { navController.navigate("editor/0") }
            )
        }

        composable(
            route = "editor/{noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) {
            val viewModel: NoteEditorViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            NoteEditorScreen(
                state = state,
                onIntent = viewModel::onIntent,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
