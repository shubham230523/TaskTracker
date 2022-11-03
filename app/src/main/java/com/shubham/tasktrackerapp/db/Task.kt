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
    @ColumnInfo(name = "bg_img")
    val bg_img : String,
    @ColumnInfo(name = "attachments")
    val attachments: MutableList<String> = mutableListOf<String>(),
    @PrimaryKey(autoGenerate = true)
    var id : Int = 0
)