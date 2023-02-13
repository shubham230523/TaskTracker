package com.shubham.tasktrackerapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Task::class , MissedTask::class , TaskDone::class],
    exportSchema = false,
    version = 6
)
@TypeConverters(TaskConverter::class)
abstract class TaskDatabase : RoomDatabase() {
    companion object {
        const val DATABASE_NAME = "task_db"
    }

    abstract val taskDao : TaskDao
}