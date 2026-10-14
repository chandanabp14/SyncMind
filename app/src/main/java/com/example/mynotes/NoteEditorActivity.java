package com.example.mynotes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

public class NoteEditorActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "note_id";
    public static final String EXTRA_TITLE = "note_title";
    public static final String EXTRA_CONTENT = "note_content";
    public static final String EXTRA_CREATED = "note_created";
    public static final String EXTRA_UPDATED = "note_updated";

    private EditText titleInput;
    private EditText contentInput;
    private TextView timestampText;

    private String noteId;
    private long createdAt;
    private long updatedAt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        titleInput = findViewById(R.id.titleInput);
        contentInput = findViewById(R.id.contentInput);
        timestampText = findViewById(R.id.timestampText);

        Button saveButton = findViewById(R.id.saveNoteButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        Intent intent = getIntent();

        noteId = intent.getStringExtra(EXTRA_ID);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String content = intent.getStringExtra(EXTRA_CONTENT);

        long now = System.currentTimeMillis();
        createdAt = intent.getLongExtra(EXTRA_CREATED, now);
        updatedAt = intent.getLongExtra(EXTRA_UPDATED, now);

        if (noteId == null) {
            noteId = UUID.randomUUID().toString();
            createdAt = now;
            updatedAt = now;
        }

        titleInput.setText(title == null ? "" : title);
        contentInput.setText(content == null ? "" : content);
        updateTimestampText();

        saveButton.setOnClickListener(v -> saveNote());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void updateTimestampText() {
        DateFormat format = DateFormat.getDateTimeInstance();

        timestampText.setText(
                "Created: " + format.format(new Date(createdAt))
                        + "\nLast edited: " + format.format(new Date(updatedAt))
        );
    }

    private void saveNote() {
        String title = titleInput.getText().toString().trim();
        String content = contentInput.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Enter a title or some content.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (title.isEmpty()) {
            title = "Untitled";
        }

        updatedAt = System.currentTimeMillis();

        Intent result = new Intent();
        result.putExtra(EXTRA_ID, noteId);
        result.putExtra(EXTRA_TITLE, title);
        result.putExtra(EXTRA_CONTENT, content);
        result.putExtra(EXTRA_CREATED, createdAt);
        result.putExtra(EXTRA_UPDATED, updatedAt);

        setResult(Activity.RESULT_OK, result);
        finish();
    }
}
