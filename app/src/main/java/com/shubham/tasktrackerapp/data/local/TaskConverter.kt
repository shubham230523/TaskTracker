package com.shubham.tasktrackerapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.sql.Time
import java.time.LocalDate
import java.time.LocalTime

/**
 * Converters for storing mutable list as string in room table and to converting string
 * back to mutable list
 */
class TaskConverter {
    companion object {
    }

    val gson = Gson()

    @TypeConverter
    fun mutableListToString(list: MutableList<String>): String {
        val tasks = gson.toJson(list)
        return tasks
    }

    @TypeConverter
    fun listToMutableList(string: String): MutableList<String> {
        val listType = object : TypeToken<MutableList<String>>() {}.type
        return gson.fromJson(string, listType)
    }

    @TypeConverter
    fun uriHashMapToString(hashMap: HashMap<String, String>): String {
        return gson.toJson(hashMap)
    }

    @TypeConverter
    fun stringToUriHashMap(string: String): HashMap<String, String> {
        val map = object : TypeToken<HashMap<String, String>>() {}.type
        return gson.fromJson(string, map)
    }

    @TypeConverter
    fun taskToString(task: Task): String{
        return gson.toJson(task)
    }

    @TypeConverter
    fun stringToTask(string: String): Task{
        val map = object : TypeToken<Task>(){}.type
        return gson.fromJson(string , map)
    }

    @TypeConverter
    fun dateToString(date: LocalDate): String{
        var month = date.monthValue.toString()
        var dayOfMonth = date.dayOfMonth.toString()
        if(month.length == 1) month = "0$month"
        if(dayOfMonth.length == 1) dayOfMonth = "0$dayOfMonth"
        val string = "${date.year}-$month-$dayOfMonth"
        return string
    }

    @TypeConverter
    fun stringToDate(string: String): LocalDate{
        val map = object : TypeToken<LocalDate>(){}.type
        return gson.fromJson(string , map)
    }

    @TypeConverter
    fun timeToString(time: LocalTime): String{
        return gson.toJson(time)
    }

    @TypeConverter
    fun stringToTime(string: String): LocalTime{
        val map = object : TypeToken<LocalTime>(){}.type
        return gson.fromJson(string , map)
    }


}