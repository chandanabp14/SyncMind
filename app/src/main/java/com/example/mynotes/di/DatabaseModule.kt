package com.example.mynotes.di

import android.content.Context
import androidx.room.Room
import com.example.mynotes.data.local.SyncMindDatabase
import com.example.mynotes.data.local.dao.NoteDao
import com.example.mynotes.data.local.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): SyncMindDatabase {
        return Room.databaseBuilder(
            context,
            SyncMindDatabase::class.java,
            "syncmind.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideNoteDao(database: SyncMindDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideTaskDao(database: SyncMindDatabase): TaskDao {
        return database.taskDao()
    }
}
