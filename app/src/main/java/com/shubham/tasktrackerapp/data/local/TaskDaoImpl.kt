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

    override fun getTaskById(taskId: Int): LiveData<Task> {
        return taskDao.getTaskById(taskId)
    }

    override suspend fun deleteTask(task: Task) {
        return taskDao.deleteTask(task)
    }

    override suspend fun deleteTable() {
        return taskDao.deleteTable()
    }

    override suspend fun insertTaskIntoMissedTable(task: MissedTask) {
        return taskDao.insertTaskIntoMissedTable(task)
    }

    override fun getLastWeekMissedTasks(date: String): LiveData<List<MissedTask>> {
        return taskDao.getLastWeekMissedTasks(date)
    }

    override fun getLastMonthMissedTasks(date: String): LiveData<List<MissedTask>> {
        return taskDao.getLastMonthMissedTasks(date)
    }

    override suspend fun deleteMissedTasksOlderThanOneMonth(currentDate: String) {
        return taskDao.deleteMissedTasksOlderThanOneMonth(currentDate)
    }

    override  fun getLastWeekDoneTasks(date: String): LiveData<List<TaskDone>> {
        return taskDao.getLastWeekDoneTasks(date)
    }

    override  fun getLastMonthDoneTasks(date: String): LiveData<List<TaskDone>> {
        return taskDao.getLastMonthDoneTasks(date)
    }

    override fun deleteDoneTasksOlderThanOneMonth(currentDate: String) {
        return taskDao.deleteDoneTasksOlderThanOneMonth(currentDate)
    }

    override suspend fun insertTaskIntoDoneTable(task: TaskDone) {
        return taskDao.insertTaskIntoDoneTable(task)
    }
}