package com.shubham.tasktrackerapp.data.local

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.sql.Date
import java.sql.Time
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
@Entity(tableName = "tbTask")
data class Task(
    @ColumnInfo(name = "title")
    var title: String,
    @ColumnInfo(name = "added_date")
    var added_date: LocalDate,
    @ColumnInfo(name = "due_date")
    var due_date: LocalDate,
    @ColumnInfo(name = "start_time")
    var start_time: LocalTime,
    @ColumnInfo(name = "end_time")
    var end_time: LocalTime,
    @ColumnInfo(name = "taskTypes")
    var taskTypes: MutableList<String> = mutableListOf(),
    @ColumnInfo(name = "attachments")
    var attachments: HashMap<String, String> = hashMapOf(),
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
): Parcelable