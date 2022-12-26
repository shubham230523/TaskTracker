package com.shubham.tasktrackerapp.upcommingtasks

/**
 * Data class of tasks shown in home page
 */
data class TimeLineTask(
    val title: String,
    val added_date : String,
    val due_date: String,
    val attachments: Boolean,
    val startTime: String,
    val endTime: String
)
