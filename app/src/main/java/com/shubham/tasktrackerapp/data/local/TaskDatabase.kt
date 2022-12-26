package com.shubham.tasktrackerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Task::class],
    exportSchema = false,
    version = 4
)
@TypeConverters(TaskConverter::class)
abstract class TaskDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "task_db"
    }

    abstract val taskDao : TaskDao
}