package com.shubham.tasktrackerapp.data.local

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class TaskDaoImpl(val taskDao: TaskDao) : TaskDao {
    override fun getAllTasks(): LiveData<List<Task>> {
        return taskDao.getAllTasks()
    }

    override suspend fun insertTask(task: Task) {
        return taskDao.insertTask(task)
    }

    override suspend fun updateTask(task: Task) {
        return taskDao.updateTask(task)
    }

    override suspend fun deleteTask(task: Task) {
        return taskDao.deleteTask(task)
    }

    override suspend fun deleteTable() {
        return taskDao.deleteTable()
    }
}