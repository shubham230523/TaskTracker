package com.shubham.tasktrackerapp.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {
    companion object{
        val gson = Gson()
        @TypeConverter
        fun mutableListToString(list : MutableList<String>) : String {
            val tasks = gson.toJson(list)
            return tasks
        }

        @TypeConverter
        fun listToMutableList(string : String) : MutableList<String>{
            val listType = object  : TypeToken<MutableList<String>>() {}.type
            return gson.fromJson(string , listType)
        }
    }
}