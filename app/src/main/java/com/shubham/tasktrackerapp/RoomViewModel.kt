package com.shubham.tasktrackerapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.data.local.TaskDaoImpl
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

    init {
        getTasksFromDatabase()
    }
}