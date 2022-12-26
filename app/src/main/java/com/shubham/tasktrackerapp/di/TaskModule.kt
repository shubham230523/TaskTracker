package com.shubham.tasktrackerapp.di

import android.app.Application
import androidx.room.Room
import com.shubham.tasktrackerapp.data.local.TaskDao
import com.shubham.tasktrackerapp.data.local.TaskDaoImpl
import com.shubham.tasktrackerapp.data.local.TaskDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskModule {

    @Provides
    @Singleton
    fun provideTaskDatabase(app: Application) : TaskDatabase {
        return Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            TaskDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideTaskDaoImpl(db: TaskDatabase) : TaskDaoImpl{
        return TaskDaoImpl(db.taskDao)
    }

}