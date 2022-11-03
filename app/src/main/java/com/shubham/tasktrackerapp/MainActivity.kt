package com.shubham.tasktrackerapp

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.Adapter.CalenderAdapter
import com.shubham.tasktrackerapp.Adapter.TimeLineTasksAdapter
import com.shubham.tasktrackerapp.Models.CalenderDateModel
import com.shubham.tasktrackerapp.Models.TimeLineTaskModel
import com.shubham.tasktrackerapp.db.Task
import com.shubham.tasktrackerapp.db.TaskDatabase
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val tasks = listOf(
        TimeLineTaskModel(
            "BDA Assignment 5" ,
            "18 Oct" ,
            "27 Oct",
            true,
            "7:15",
            "18:30"
        ),
        TimeLineTaskModel(
            "BDA Assignment 5" ,
            "18 Oct" ,
            "27 Oct",
            true,
            "7:20",
            "18:30"
        ),
        TimeLineTaskModel(
            "CSL Module 3 Quiz" ,
            "12 March" ,
            "12 Nov",
            false,
            "21:46",
            "19:10"
        ),
        TimeLineTaskModel(
            "AVR Practicals" ,
            "12 May" ,
            "14 Jun",
            false,
            "21:47",
            "19:50"
        ),
//        TimeLineTaskModel(
//            "Major Project" ,
//            "11 Dec" ,
//            "12 Jan",
//            true,
//            "11:20",
//            "11:35"
//        ),
//        TimeLineTaskModel(
//            "BDA Assignment 5" ,
//            "18 Oct" ,
//            "27 Oct",
//            true,
//            "11:50",
//            "12:22"
//        ),
//        TimeLineTaskModel(
//            "CSL Module 3 Quiz" ,
//            "12 March" ,
//            "12 Nov",
//            false,
//            "12:40",
//            "12:55"
//        ),
//        TimeLineTaskModel(
//            "AVR Practicals" ,
//            "12 May" ,
//            "14 Jun",
//            false,
//            "1:00",
//            "1:15"
//        ),
//        TimeLineTaskModel(
//            "Major Project" ,
//            "11 Dec" ,
//            "12 Jan",
//            true,
//            "1:25",
//            "1:50"
//        ),
//        TimeLineTaskModel(
//            "BDA Assignment 5" ,
//            "18 Oct" ,
//            "27 Oct",
//            true,
//            "2:00",
//            "2:20"
//        ),
//        TimeLineTaskModel(
//            "CSL Module 3 Quiz" ,
//            "12 March" ,
//            "12 Nov",
//            false,
//            "2:35",
//            "2:40"
//        ),
//        TimeLineTaskModel(
//            "AVR Practicals" ,
//            "12 May" ,
//            "14 Jun",
//            false,
//            "3:00",
//            "3:22"
//        ),
//        TimeLineTaskModel(
//            "Major Project" ,
//            "11 Dec" ,
//            "12 Jan",
//            true,
//            "4:00",
//            "5:00"
//        ),
    )
    private var datesList = mutableListOf<CalenderDateModel>()
    private var lastSelectedPosition = 0
    private lateinit var calenderAdapter: CalenderAdapter
    val cal = Calendar.getInstance(Locale.ENGLISH)
    var month = 0 ; var day = 0 ; var date = 0 ; var year = 0
    private lateinit var rvDates: RecyclerView
    private lateinit var tvMonth : TextView
    private val scaleFractions = mutableListOf<Float>(1F, 1F , 1F , 1F , 1F , 1F , 1F , 1F , 1F , 1F , 1F , 0.4F)
    private var  job : Job = Job()
    var optUpcom = true
    var optAllupcom = false
    var optMissed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvDates = findViewById(R.id.rv_dates)
        val rvTasks = findViewById<RecyclerView>(R.id.rv_tasks)
        tvMonth = findViewById(R.id.tv_month)

        val opt_upcoming = findViewById<TextView>(R.id.opt_upcoming_tasks)
        val opt_all_comming = findViewById<TextView>(R.id.opt_all_upcoming_tasks)
        val opt_missed_tasks = findViewById<TextView>(R.id.opt_missed)

        opt_upcoming.background = ContextCompat.getDrawable(this , R.drawable.task_opt_selected)
        opt_upcoming.setTextColor(ContextCompat.getColor(this , R.color.blue))
        opt_upcoming.setOnClickListener {
            if(optAllupcom) {
                optAllupcom = false
                opt_all_comming.setTextColor(ContextCompat.getColor(this , R.color.grey))
                opt_all_comming.background = ContextCompat.getDrawable(this , R.drawable.dates_unselected_background_stroke)
            }
            if(optMissed){
                optMissed = false
                opt_missed_tasks.setTextColor(ContextCompat.getColor(this , R.color.grey))
                opt_missed_tasks.background = ContextCompat.getDrawable(this , R.drawable.dates_unselected_background_stroke)
            }
            optUpcom = true
            opt_upcoming.setTextColor(ContextCompat.getColor(this , R.color.blue))
            opt_upcoming.background = ContextCompat.getDrawable(this , R.drawable.task_opt_selected)
        }
        opt_all_comming.setOnClickListener {
            if(optUpcom) {
                optUpcom = false
                opt_upcoming.setTextColor(ContextCompat.getColor(this , R.color.grey))
                opt_upcoming.background = ContextCompat.getDrawable(this , R.drawable.dates_unselected_background_stroke)
            }
            if(optMissed){
                optMissed = false
                opt_missed_tasks.setTextColor(ContextCompat.getColor(this , R.color.grey))
                opt_missed_tasks.background = ContextCompat.getDrawable(this , R.drawable.dates_unselected_background_stroke)
            }
            optAllupcom = true
            opt_all_comming.setTextColor(ContextCompat.getColor(this , R.color.blue))
            opt_all_comming.background = ContextCompat.getDrawable(this , R.drawable.task_opt_selected)
        }
        opt_missed_tasks.setOnClickListener {
            if(optAllupcom) {
                optAllupcom = false
                opt_all_comming.setTextColor(ContextCompat.getColor(this , R.color.grey))
                opt_all_comming.background = ContextCompat.getDrawable(this , R.drawable.dates_unselected_background_stroke)
            }
            if(optUpcom){
                optUpcom = false
                opt_upcoming.setTextColor(ContextCompat.getColor(this , R.color.grey))
                opt_upcoming.background = ContextCompat.getDrawable(this , R.drawable.dates_unselected_background_stroke)
            }
            optMissed = true
            opt_missed_tasks.setTextColor(ContextCompat.getColor(this , R.color.blue))
            opt_missed_tasks.background = ContextCompat.getDrawable(this , R.drawable.task_opt_selected)
        }

        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_WEEK)
        date = cal.get(Calendar.DATE)
        year = cal.get(Calendar.YEAR)

        tvMonth.text = "$date ${getMonth(month)} $year"

        calenderAdapter = CalenderAdapter(setUpCalender(), this){position ->  onListItemClick(position)}
        val tasksAdapter = TimeLineTasksAdapter(tasks , this)

        rvDates.apply {
            adapter = calenderAdapter
            layoutManager = LinearLayoutManager(this@MainActivity , LinearLayoutManager.HORIZONTAL , false)
        }
        val lm = LinearLayoutManager(this@MainActivity , LinearLayoutManager.VERTICAL , false)
        rvTasks.apply {
            adapter = tasksAdapter
            layoutManager = lm
        }
        lastSelectedPosition = cal.get(Calendar.DAY_OF_MONTH)-1
        rvDates.scrollToPosition(lastSelectedPosition)
        calenderAdapter.changeSelectedDate(lastSelectedPosition , lastSelectedPosition)
        tvMonth.setOnClickListener { pickDate() }
        Log.d("MainActivity" , "mainActivity")
        rvTasks.runWhenReady {
            tasksAdapter.scalingViewsTimeline(scaleFractions)
        }

        //inserting and retrieving data to room database
//        val task = Task(
//            title = "ML_Viva" ,
//            added_date = "03-Nov-22",
//            due_date = "03-Nov-22" ,
//            start_time = "9:10",
//            end_time = "9:20",
//            bg_img = "reading",
//            attachments = mutableListOf("VIVA"),
//            1
//        )
//        val db = TaskDatabase.getInstance(this)
//        GlobalScope.launch (Dispatchers.IO){
//            db.dao().updateTask(task)
//            val taskList = db.dao().getAllTasks()
//            Log.d("MainActivity" , "taskList - $taskList")
//        }
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
    fun RecyclerView.runWhenReady(action: () -> Unit) {
        val globalLayoutListener = object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                action()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }
}