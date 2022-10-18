package com.shubham.tasktrackerapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val tasks = listOf(
        TimeLineTaskModel(
            "",
            "",
            "",
        ),
        TimeLineTaskModel(
            "BDA Assignment 5" ,
            "Chapter Hadoop Assignment" ,
            "today 5pm"
        ),
        TimeLineTaskModel(
            "CSL Module 3 Quiz" ,
            "Quiz for Cyber law and Crimes" ,
            "tomorrow 12pm "
        ),
        TimeLineTaskModel(
            "AVR Practicals" ,
            "Completing all AVR practicals" ,
            "20th October 2022"
        ),
        TimeLineTaskModel(
            "Major Project" ,
            "Completing major project" ,
            "25th October 2022"
        ),
        TimeLineTaskModel(
            "BDA Assignment 5" ,
            "Chapter Hadoop Assignment" ,
            "today 5pm"
        ),
        TimeLineTaskModel(
            "CSL Module 3 Quiz" ,
            "Quiz for Cyber law and Crimes" ,
            "tomorrow 12pm "
        ),
        TimeLineTaskModel(
            "AVR Practicals" ,
            "Completing all AVR practicals" ,
            "20th October 2022"
        ),
        TimeLineTaskModel(
            "Major Project" ,
            "Completing major project" ,
            "25th October 2022"
        ),
        TimeLineTaskModel(
            "BDA Assignment 5" ,
            "Chapter Hadoop Assignment" ,
            "today 5pm"
        ),
        TimeLineTaskModel(
            "CSL Module 3 Quiz" ,
            "Quiz for Cyber law and Crimes" ,
            "tomorrow 12pm "
        ),
        TimeLineTaskModel(
            "AVR Practicals" ,
            "Completing all AVR practicals" ,
            "20th October 2022"
        ),
        TimeLineTaskModel(
            "Major Project" ,
            "Completing major project" ,
            "25th October 2022"
        ),
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rv_dates = findViewById<RecyclerView>(R.id.rv_dates)
        val rv_tasks = findViewById<RecyclerView>(R.id.rv_tasks)

        val calenderAdapter = CalenderAdapter(setUpCalender())
        val tasksAdapter = TimeLineTasksAdapter(tasks , this)

        rv_dates.apply {
            adapter = calenderAdapter
            layoutManager = LinearLayoutManager(this@MainActivity , LinearLayoutManager.HORIZONTAL , false)
        }
        rv_tasks.apply {
            adapter = tasksAdapter
            layoutManager = LinearLayoutManager(this@MainActivity , LinearLayoutManager.VERTICAL , false)
        }
    }
    private fun setUpCalender() : ArrayList<CalenderDateModel>{
        val sdf = SimpleDateFormat("MMMM yyyy" , Locale.ENGLISH)
        val cal = Calendar.getInstance(Locale.ENGLISH)
        val currentDate = Calendar.getInstance(Locale.ENGLISH)
        val dates = ArrayList<Date>()
        val calenderList = ArrayList<CalenderDateModel>()
        val month = cal.clone() as Calendar
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        dates.clear()
        month.set(Calendar.DAY_OF_MONTH , 1)
        while(dates.size < maxDaysInMonth){
            dates.add(month.time)
            calenderList.add(CalenderDateModel(month.time))
            month.add(Calendar.DAY_OF_MONTH , 1)
        }
        return calenderList;
    }
}