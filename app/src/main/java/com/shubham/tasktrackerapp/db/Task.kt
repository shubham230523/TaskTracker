package com.shubham.tasktrackerapp.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_task")
data class Task(
    @ColumnInfo(name = "title")
    val title : String,
    @ColumnInfo(name = "added_date")
    val added_date : String,
    @ColumnInfo(name = "due_date")
    val due_date : String,
    @ColumnInfo(name = "start_time")
    val start_time: String,
    @ColumnInfo(name = "end_time")
    val end_time: String,
    @ColumnInfo(name = "taskTypes")
    val taskTypes: MutableList<String> = mutableListOf(),
    @ColumnInfo(name = "attachments")
    val attachments: HashMap<String , String> = hashMapOf(),
    @ColumnInfo(name = "bgColor")
    val bgColor : Int,
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
)