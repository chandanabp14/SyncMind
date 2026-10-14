package com.example.mynotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.mynotes.data.local.dao.NoteDao
import com.example.mynotes.data.local.dao.TaskDao
import com.example.mynotes.data.local.entity.NoteEntity
import com.example.mynotes.data.local.entity.TaskEntity

@Database(
    entities = [NoteEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SyncMindDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
}
