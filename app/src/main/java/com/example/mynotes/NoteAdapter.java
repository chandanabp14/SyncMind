
package com.example.mynotes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteActionListener {
        void onOpen(Note note);
        void onDelete(Note note);
    }

    private final List<Note> notes = new ArrayList<>();
    private final OnNoteActionListener listener;

    public NoteAdapter(OnNoteActionListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> newNotes) {
        notes.clear();
        notes.addAll(newNotes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);

        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NoteViewHolder holder, int position) {

        Note note = notes.get(position);

        holder.noteTitle.setText(note.getTitle());

        String updatedTime = DateFormat.getDateTimeInstance()
                .format(new Date(note.getUpdatedAt()));

        holder.noteTimestamp.setText("Last edited: " + updatedTime);

        holder.openButton.setOnClickListener(v -> listener.onOpen(note));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(note));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView noteTitle;
        TextView noteTimestamp;
        Button openButton;
        Button deleteButton;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteTimestamp = itemView.findViewById(R.id.noteTimestamp);
            openButton = itemView.findViewById(R.id.openButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
