package com.shubham.tasktrackerapp.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
}