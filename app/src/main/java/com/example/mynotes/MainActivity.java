package com.example.mynotes;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.RoomDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private NoteDatabase database;
    private NoteAdapter adapter;
    private Button addNoteButton;
    private TextView emptyNotesText;

    private final ExecutorService databaseExecutor =
            Executors.newSingleThreadExecutor();

    private ActivityResultLauncher<Intent> editorLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = NoteDatabase.getInstance(getApplicationContext());

        RecyclerView notesList = findViewById(R.id.notesList);
        addNoteButton = findViewById(R.id.addNoteButton);
        emptyNotesText = findViewById(R.id.emptyNotesText);

        adapter = new NoteAdapter(new NoteAdapter.OnNoteActionListener() {
            @Override
            public void onOpen(Note note) {
                openEditor(note);
            }

            @Override
            public void onDelete(Note note) {
                confirmDelete(note);
            }
        });

        notesList.setLayoutManager(new LinearLayoutManager(this));
        notesList.setAdapter(adapter);

        editorLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK
                            && result.getData() != null) {
                        saveEditorResult(result.getData());
                    }
                }
        );

        addNoteButton.setOnClickListener(v -> openEditor(null));

        addNoteButton.setEnabled(false);
        migrateOldNotesAndLoad();
    }

    private void openEditor(Note note) {
        Intent intent = new Intent(this, NoteEditorActivity.class);

        if (note != null) {
            intent.putExtra(NoteEditorActivity.EXTRA_ID, note.getId());
            intent.putExtra(NoteEditorActivity.EXTRA_TITLE, note.getTitle());
            intent.putExtra(NoteEditorActivity.EXTRA_CONTENT, note.getContent());
            intent.putExtra(NoteEditorActivity.EXTRA_CREATED, note.getCreatedAt());
            intent.putExtra(NoteEditorActivity.EXTRA_UPDATED, note.getUpdatedAt());
        }

        editorLauncher.launch(intent);
    }

    private void saveEditorResult(Intent data) {
        String id = data.getStringExtra(NoteEditorActivity.EXTRA_ID);
        String title = data.getStringExtra(NoteEditorActivity.EXTRA_TITLE);
        String content = data.getStringExtra(NoteEditorActivity.EXTRA_CONTENT);
        long createdAt = data.getLongExtra(
                NoteEditorActivity.EXTRA_CREATED, System.currentTimeMillis());
        long updatedAt = data.getLongExtra(
                NoteEditorActivity.EXTRA_UPDATED, System.currentTimeMillis());

        if (id == null || title == null || content == null) {
            Toast.makeText(this, "Could not save note.", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note(id, title, content, createdAt, updatedAt);

        databaseExecutor.execute(() -> {
            Note existing = database.noteDao().getNoteById(id);

            if (existing == null) {
                database.noteDao().insert(note);
            } else {
                database.noteDao().update(note);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                loadNotes();
            });
        });
    }

    private void confirmDelete(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete note?")
                .setMessage("Delete \"" + note.getTitle() + "\"?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseExecutor.execute(() -> {
                        database.noteDao().delete(note);
                        runOnUiThread(this::loadNotes);
                    });
                })
                .show();
    }

    private void migrateOldNotesAndLoad() {
        databaseExecutor.execute(() -> {
            SharedPreferences preferences =
                    getSharedPreferences("MyNotesStorage", MODE_PRIVATE);

            boolean migrationDone =
                    preferences.getBoolean("room_migration_done", false);

            if (!migrationDone) {
                String saved = preferences.getString("notes", "[]");

                try {
                    JSONArray array = new JSONArray(saved);

                    for (int i = 0; i < array.length(); i++) {
                        String oldText = array.optString(i, "");

                        if (oldText.trim().isEmpty()) {
                            continue;
                        }

                        long now = System.currentTimeMillis();
                        String title = getTitleFromOldText(oldText);

                        Note migratedNote = new Note(
                                UUID.randomUUID().toString(),
                                title,
                                oldText,
                                now,
                                now
                        );

                        database.noteDao().insert(migratedNote);
                    }

                    preferences.edit()
                            .putBoolean("room_migration_done", true)
                            .apply();

                } catch (JSONException e) {
                    runOnUiThread(() -> Toast.makeText(
                            this,
                            "Couldn't migrate old notes. Your old data was kept.",
                            Toast.LENGTH_LONG
                    ).show());
                }
            }

            List<Note> notes = database.noteDao().getAllNotes();

            runOnUiThread(() -> {
                adapter.setNotes(notes);
                emptyNotesText.setVisibility(
                        notes.isEmpty() ? View.VISIBLE : View.GONE);
                addNoteButton.setEnabled(true);
            });
        });
    }

    private String getTitleFromOldText(String text) {
        String firstLine = text.split("\\n", 2)[0].trim();

        if (firstLine.isEmpty()) {
            return "Untitled";
        }

        if (firstLine.length() > 50) {
            return firstLine.substring(0, 50);
        }

        return firstLine;
    }

    private void loadNotes() {
        databaseExecutor.execute(() -> {
            List<Note> notes = database.noteDao().getAllNotes();

            runOnUiThread(() -> {
                adapter.setNotes(notes);
                emptyNotesText.setVisibility(
                        notes.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Executor is intentionally left alive across a configuration change.
        // For this small app, the process will clean it up when the app exits.
    }
}
