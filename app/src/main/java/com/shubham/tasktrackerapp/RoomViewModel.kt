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
): ViewModel() {

    private lateinit var mTasks: LiveData<List<Task>>

    fun getTasks() = mTasks

    private fun getTasksFromDatabase() {
        viewModelScope.launch {
            mTasks = taskDaoImpl.getAllTasks()
        }
    }

    fun insertIntoDatabase(task : Task){
        viewModelScope.launch {
            taskDaoImpl.insertTask(task)
        }
    }

    fun updateTask(task: Task){
        viewModelScope.launch {
            taskDaoImpl.updateTask(task)
        }
    }

    fun getTaskById(taskId: Int): LiveData<Task>{
        return taskDaoImpl.getTaskById(taskId)
    }

    fun deleteTask(task: Task){
        viewModelScope.launch {
            taskDaoImpl.deleteTask(task)
        }
    }

    fun insertMissedTask(task: MissedTask){
        viewModelScope.launch {
            taskDaoImpl.insertTaskIntoMissedTable(task)
        }
    }

    fun getLastWeekMissedTasks(date: String) = taskDaoImpl.getLastWeekMissedTasks(date)

    fun getLastMonthMissedTasks(date: String): List<MissedTask>?{
        var list : List<MissedTask>? = listOf()
        viewModelScope.launch {
            list = taskDaoImpl.getLastWeekMissedTasks(date).value
        }
        return list
    }

    fun insertTaskDone(task: TaskDone){
        viewModelScope.launch {
            taskDaoImpl.insertTaskIntoDoneTable(task)
        }
    }

    fun getLastWeekDoneTask(date: String) : List<TaskDone>?{
        var list : List<TaskDone>? = null
        viewModelScope.launch {
            list = taskDaoImpl.getLastWeekDoneTasks(date).value
        }
        return list
    }

    fun getLastMonthDoneTask(date: String) : List<TaskDone>?{
        var list : List<TaskDone>? = null
        viewModelScope.launch {
            list = taskDaoImpl.getLastMonthDoneTasks(date).value
        }
        return list
    }

    init {
        getTasksFromDatabase()
    }
}