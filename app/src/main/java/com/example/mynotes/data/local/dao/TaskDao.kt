package com.example.mynotes.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mynotes.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE noteId = :noteId ORDER BY isCompleted ASC, id ASC")
    fun getTasksForNote(noteId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE noteId = :noteId ORDER BY isCompleted ASC, id ASC")
    suspend fun getTasksForNoteSync(noteId: Long): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity): Int

    @Delete
    suspend fun deleteTask(task: TaskEntity): Int

    @Query("DELETE FROM tasks WHERE noteId = :noteId")
    suspend fun deleteTasksForNote(noteId: Long): Int
}
