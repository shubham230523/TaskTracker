package com.shubham.tasktrackerapp

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val tasks = listOf(
        TimeLineTaskModel(
            "",
            "",
            "",
            false
        ),
        TimeLineTaskModel(
            "BDA Assignment 5" ,
            "18 Oct" ,
            "27 Oct",
            true
        ),
        TimeLineTaskModel(
            "CSL Module 3 Quiz" ,
            "12 March" ,
            "12 Nov",
            false
        ),
        TimeLineTaskModel(
            "AVR Practicals" ,
            "12 May" ,
            "14 Jun",
            false
        ),
        TimeLineTaskModel(
            "Major Project" ,
            "11 Dec" ,
            "12 Jan",
            true
        ),
        TimeLineTaskModel(
            "BDA Assignment 5" ,
            "18 Oct" ,
            "27 Oct",
            true
        ),
        TimeLineTaskModel(
            "CSL Module 3 Quiz" ,
            "12 March" ,
            "12 Nov",
            false
        ),
        TimeLineTaskModel(
            "AVR Practicals" ,
            "12 May" ,
            "14 Jun",
            false
        ),
        TimeLineTaskModel(
            "Major Project" ,
            "11 Dec" ,
            "12 Jan",
            true
        ),
        TimeLineTaskModel(
            "BDA Assignment 5" ,
            "18 Oct" ,
            "27 Oct",
            true
        ),
        TimeLineTaskModel(
            "CSL Module 3 Quiz" ,
            "12 March" ,
            "12 Nov",
            false
        ),
        TimeLineTaskModel(
            "AVR Practicals" ,
            "12 May" ,
            "14 Jun",
            false
        ),
        TimeLineTaskModel(
            "Major Project" ,
            "11 Dec" ,
            "12 Jan",
            true
        ),
    )
    private var datesList = mutableListOf<CalenderDateModel>()
    private var lastSelectedPosition = 0
    private lateinit var calenderAdapter: CalenderAdapter
    val cal = Calendar.getInstance(Locale.ENGLISH)
    var month = 0 ; var day = 0 ; var date = 0 ; var year = 0
    private lateinit var rvDates: RecyclerView
    private lateinit var tvMonth : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvDates = findViewById(R.id.rv_dates)
        val rvTasks = findViewById<RecyclerView>(R.id.rv_tasks)
        tvMonth = findViewById(R.id.tv_month)
        val tvDate = findViewById<TextView>(R.id.tv_date)
        val tvDay = findViewById<TextView>(R.id.tv_day)

        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_WEEK)
        date = cal.get(Calendar.DATE)
        year = cal.get(Calendar.YEAR)

        tvDate.text = "$date ${getMonth(month)} $year"
        tvDay.text = getDay(day)
        tvMonth.text = "$date ${getMonth(month)} $year"

        calenderAdapter = CalenderAdapter(setUpCalender(), this){position ->  onListItemClick(position)}
        val tasksAdapter = TimeLineTasksAdapter(tasks , this)

        rvDates.apply {
            adapter = calenderAdapter
            layoutManager = LinearLayoutManager(this@MainActivity , LinearLayoutManager.HORIZONTAL , false)
        }
        rvTasks.apply {
            adapter = tasksAdapter
            layoutManager = LinearLayoutManager(this@MainActivity , LinearLayoutManager.VERTICAL , false)
        }

        lastSelectedPosition = cal.get(Calendar.DAY_OF_MONTH)-1
        rvDates.scrollToPosition(lastSelectedPosition)
        calenderAdapter.changeSelectedDate(lastSelectedPosition , lastSelectedPosition)

        tvMonth.setOnClickListener { pickDate() }
    }

    private fun setUpCalender() : MutableList<CalenderDateModel> {
        val dates = ArrayList<Date>()
        val month = cal.clone() as Calendar
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        dates.clear()
        datesList.clear()
        month.set(Calendar.DAY_OF_MONTH , 1)
        while(dates.size < maxDaysInMonth){
            dates.add(month.time)
            datesList.add(
                CalenderDateModel(
                    month.time.day.toString(),
                    month.time.date.toString(),
                    false
                )
            )
            month.add(Calendar.DAY_OF_MONTH , 1)
        }
        return datesList;
    }
    private fun onListItemClick(position: Int){
        Toast.makeText(this , "$lastSelectedPosition $position" , Toast.LENGTH_SHORT).show()
        calenderAdapter.changeSelectedDate(lastSelectedPosition , position)
        lastSelectedPosition = position
    }
    private fun pickDate(){
        val datePickerDialog = DatePickerDialog(
            this@MainActivity,
            object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
                    this@MainActivity.month = month
                    this@MainActivity.year = year
                    this@MainActivity.date = day
                    updateDate()
                }
            } ,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    private fun updateDate(){
        tvMonth.text = "$date ${getMonth(month)} $year"
        rvDates.scrollToPosition(date-1)
        calenderAdapter.changeSelectedDate(lastSelectedPosition , date-1)
        lastSelectedPosition = date-1
    }
    private fun getDay(day: Int) : String{
        when(day){
            1 -> return "Sunday"
            2 -> return "Monday"
            3 -> return "Tuesday"
            4 -> return "Wednesday"
            5 -> return "Thursday"
            6 -> return "Friday"
            7 -> return "Saturday"
        }
        return ""
    }
    private fun getMonth(month: Int) : String{
        when(month){
            0 -> return "January"
            1 -> return "February"
            2 -> return "March"
            3 -> return "April"
            4 -> return "May"
            5 -> return "June"
            6 -> return "July"
            7 -> return "August"
            8 -> return "September"
            9 -> return "October"
            10 -> return "November"
            11 -> return "December"
        }
        return ""
    }
}