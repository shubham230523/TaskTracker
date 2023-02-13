package com.shubham.tasktrackerapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.tasktrackerapp.data.local.MissedTask
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.data.local.TaskDaoImpl
import com.shubham.tasktrackerapp.data.local.TaskDone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val taskDaoImpl: TaskDaoImpl
) : ViewModel() {

    private lateinit var mTasks: LiveData<List<Task>>

    fun getTasks() = mTasks

    // for all tasks from tb_task table
    private fun getTasksFromDatabase() {
        viewModelScope.launch {
            mTasks = taskDaoImpl.getAllTasks()
        }
    }

    // to insert task into tb_task table
    fun insertIntoDatabase(task: Task) {
        viewModelScope.launch {
            taskDaoImpl.insertTask(task)
        }
    }

    // to update task in tb_task table
    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDaoImpl.updateTask(task)
        }
    }

    // to get task by id from tb_task table
    fun getTaskById(taskId: Int): LiveData<Task> {
        return taskDaoImpl.getTaskById(taskId)
    }

    // to delete task from tb_task table
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDaoImpl.deleteTask(task)
        }
    }

    // to insert missed task into table  tbTasksMissed
    fun insertMissedTask(task: MissedTask) {
        viewModelScope.launch {
            taskDaoImpl.insertTaskIntoMissedTable(task)
        }
    }

    fun getLastWeekMissedTasks(date: String) = taskDaoImpl.getLastWeekMissedTasks(date)

    fun getLastMonthMissedTasks(date: String): List<MissedTask>? {
        var list: List<MissedTask>? = listOf()
        viewModelScope.launch {
            list = taskDaoImpl.getLastWeekMissedTasks(date).value
        }
        return list
    }

    // to insert task in table tbTasksDone
    fun insertTaskDone(task: TaskDone) {
        viewModelScope.launch {
            taskDaoImpl.insertTaskIntoDoneTable(task)
        }
    }

    fun getLastWeekDoneTask(date: String): List<TaskDone>? {
        var list: List<TaskDone>? = null
        viewModelScope.launch {
            list = taskDaoImpl.getLastWeekDoneTasks(date).value
        }
        return list
    }

    fun getLastMonthDoneTask(date: String): List<TaskDone>? {
        var list: List<TaskDone>? = null
        viewModelScope.launch {
            list = taskDaoImpl.getLastMonthDoneTasks(date).value
        }
        return list
    }

    init {
        getTasksFromDatabase()
    }
}