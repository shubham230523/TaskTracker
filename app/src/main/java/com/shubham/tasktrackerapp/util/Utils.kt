package com.shubham.tasktrackerapp.util

import android.graphics.Color
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

val newTaskColors = arrayOf(
    Color.parseColor("#FFEBEE"), // light red
    Color.parseColor("#F3E5F5"), // light purple
    Color.parseColor("#E0F7FA"), // light blue
    Color.parseColor("#FFF3E0"), // light orange
    Color.parseColor("#EFEBE9"), // light grey
)

var taskCategories = mutableListOf(
    "Assignment", "Project", "Coding",
    "Classes", "Hobby", "Meeting", "Playing", "Hangout", "Food", "Television",
    "Exercise", "Remainder", "Other"
)

@Composable
fun ColorScheme.isLight() = this.background.luminance() > 0.5

val xAxisMonthly = listOf("00", "05", "10", "15", "20", "25", "30")
val xAxisWeekly = listOf("Sun" , "Mon" , "Tue" , "Wed" , "Thu" , "Fri" , "Sat")

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