package com.shubham.tasktrackerapp.allupcommingtasks

import android.util.Log
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.*
import com.shubham.tasktrackerapp.RoomViewModel
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.data.local.TaskDao
import com.shubham.tasktrackerapp.data.local.TaskDaoImpl
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

const val TAGV = "AllUpcomingTasksViewModel"

@HiltViewModel
class AllUpcomingTasksViewModel @Inject constructor(
    private val taskDaoImpl: TaskDaoImpl
): ViewModel() {

    fun getTasksFromDatabase(): LiveData<List<Task>> {
        val list = taskDaoImpl.getAllTasks()
        Log.d(TAGV , "list from getTasksFromDatabase - ${list.value}")
        sortTasksAccordingToDueDate(list)
        return list
    }

    private fun sortTasksAccordingToDueDate(taskList: LiveData<List<Task>>){
        Log.d(TAGV , "taskList is - ${taskList.value}")
        val sortedList = taskList.value?.let {
            Collections.sort(it, { t1, t2 ->
                if(t1.due_date.isBefore(t2.due_date) || t1.due_date.isEqual(t2.due_date)){
                    1
                }else 0
            })
        }
        Log.d(TAGV , "sorted tasks method is getting called")
        Log.d(TAGV , "sorted list is - $sortedList")
    }

    /**
     * Function to get short form of the month based on month number counting from 1
     *
     * @param month month number
     */
    fun getMonthShortForm(month: Int): String {
        when (month) {
            1 -> return "Jan"
            2 -> return "Feb"
            3 -> return "Mar"
            4 -> return "Apr"
            5 -> return "May"
            6 -> return "June"
            7 -> return "July"
            8 -> return "Aug"
            9 -> return "Sept"
            10 -> return "Oct"
            11 -> return "Nov"
        }
        return "Dec"
    }
}