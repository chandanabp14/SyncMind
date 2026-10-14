package com.example.mynotes;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView detailNote = findViewById(R.id.detailNote);
        Button backButton = findViewById(R.id.backButton);

        String note = getIntent().getStringExtra("note_text");

        if (note != null && !note.isEmpty()) {
            detailNote.setText(note);
        } else {
            detailNote.setText("No note was received.");
        }
        backButton.setOnClickListener(v -> finish());
    }
}
