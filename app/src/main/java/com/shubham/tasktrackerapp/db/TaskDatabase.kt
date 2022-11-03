package com.shubham.tasktrackerapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Task::class] ,
    exportSchema = false,
    version = 1
)
abstract class TaskDatabase : RoomDatabase() {
    companion object{
        const val DATABASE_NAME = "task_db"
        // For Singleton instantiation
        @Volatile private var instance: TaskDatabase? = null

        fun getInstance(context: Context): TaskDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        private fun buildDatabase(context: Context) : TaskDatabase{
            return Room.databaseBuilder(context , TaskDatabase::class.java , DATABASE_NAME)
                .build()
        }
    }
    abstract fun dao() : TaskDao
}