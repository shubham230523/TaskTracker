package com.shubham.tasktrackerapp.Models

data class TimeLineTaskModel(
    val title: String,
    val added_date : String,
    val due_date: String,
    val attachments: Boolean,
    val startTime: String,
    val endTime: String
)
