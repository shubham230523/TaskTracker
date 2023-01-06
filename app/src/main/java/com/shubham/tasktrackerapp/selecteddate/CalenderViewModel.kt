package com.shubham.tasktrackerapp.selecteddate

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CalenderViewModel @Inject constructor() : ViewModel() {

    var month = 0
    var day = 0
    var date = 0
    var year = 0
    var dayOfMonth = 0

    private var datesList = mutableListOf<CalenderDateModel>()
    private val cal = Calendar.getInstance(Locale.ENGLISH)

    /**
     * Function for setting up the calender i.e to get no of days in the month and
     * corresponding day and also for getting the current date and day
     */
    fun setUpCalender() {
        val dates = ArrayList<Date>()
        val month = cal.clone() as Calendar
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        dates.clear()
        datesList.clear()
        month.set(Calendar.DAY_OF_MONTH, 1)
        while (dates.size < maxDaysInMonth) {
            dates.add(month.time)
            datesList.add(
                CalenderDateModel(
                    day = getDay(month.time.day),
                    date = month.time.date.toString(),
                    false
                )
            )
            month.add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    fun getDatesList() = datesList

    /**
     * Function to get month from number
     *
     * @param month Number of month in a year
     */
    fun getMonth(month: Int): String {
        when (month) {
            0 -> return "January"
            1 -> return "February"
            2 -> return "March"
            3 -> return "April"
            4 -> return "May"
            5 -> return "June"
            6 -> return "July"
            7 -> return "August"
            8 -> return "September"
            9 -> return "October"
            10 -> return "November"
            11 -> return "December"
        }
        return ""
    }

    fun getDay(dayNo: Int): String{
        when(dayNo){
            0 -> return "Sun"
            1 -> return "Mon"
            2 -> return "Tue"
            3 -> return "Wed"
            4 -> return "Thu"
            5 -> return "Fri"
        }
        return "Sat"
    }

    init {
        setUpCalender()
        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_WEEK)
        date = cal.get(Calendar.DATE)
        year = cal.get(Calendar.YEAR)
        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    }
}