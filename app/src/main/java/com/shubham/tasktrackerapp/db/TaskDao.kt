package com.shubham.tasktrackerapp.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Query("Select * from tb_task")
    fun getAllTasks() : List<Task>

    @Insert
    fun insertTask(task : Task)

    @Update
    fun updateTask(task : Task)

    @Delete
    fun deleteTask(task : Task)
}