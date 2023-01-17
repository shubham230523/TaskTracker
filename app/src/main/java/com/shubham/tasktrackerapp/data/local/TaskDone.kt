package com.shubham.tasktrackerapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date
import java.sql.Time
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "tbTasksDone")
data class TaskDone(
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "added_date")
    val added_date: LocalDate,
    @ColumnInfo(name = "due_date")
    val due_date: LocalDate,
    @ColumnInfo(name = "start_time")
    val start_time: LocalTime,
    @ColumnInfo(name = "end_time")
    val end_time: LocalTime,
    @ColumnInfo(name = "taskTypes")
    val taskTypes: MutableList<String> = mutableListOf(),
    @ColumnInfo(name = "attachments")
    val attachments: HashMap<String, String> = hashMapOf(),
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)