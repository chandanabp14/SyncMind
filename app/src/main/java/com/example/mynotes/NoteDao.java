package com.example.mynotes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    Note getNoteById(String noteId);

    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);
}
