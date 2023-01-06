package com.shubham.tasktrackerapp.allupcommingtasks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.data.local.TaskDao
import com.shubham.tasktrackerapp.data.local.TaskDaoImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllUpcomingTasksViewModel @Inject constructor(): ViewModel() {

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