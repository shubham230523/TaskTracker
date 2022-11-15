package com.shubham.tasktrackerapp.Fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.DatePicker
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.Adapter.CalenderAdapter
import com.shubham.tasktrackerapp.Adapter.TimeLineTasksAdapter
import com.shubham.tasktrackerapp.Models.CalenderDateModel
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.db.Task
import com.shubham.tasktrackerapp.db.TaskDao
import com.shubham.tasktrackerapp.db.TaskDatabase
import kotlinx.coroutines.*
import java.util.*

class HomeFragment() : Fragment(R.layout.fragment_home)  {
    companion object{
        private const val TAG = "HomeFragment"
    }
    private var tasks : List<Task>? = null
    private var datesList = mutableListOf<CalenderDateModel>()
    private var lastSelectedPosition = 0
    private lateinit var calenderAdapter: CalenderAdapter
    private val cal = Calendar.getInstance(Locale.ENGLISH)
    private var month = 0
    private var day = 0
    private var date = 0
    private var year = 0
    private lateinit var rvDates: RecyclerView
    private lateinit var tvMonth: TextView
    // Booleans for indicating which option is selected
    // "up coming" , "all upcoming" , "missed"
    private var optUpcom = true
    private var optAllupcom = false
    private var optMissed = false
    private var mContext = activity?.applicationContext
    private lateinit var rvTasks : RecyclerView
    private var taskDao : TaskDao? = null
    private var job: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvDates = view.findViewById(R.id.rv_dates)
        rvTasks= view.findViewById<RecyclerView>(R.id.rv_tasks)
        tvMonth = view.findViewById(R.id.tv_month)
        val btnUpComingTasks = view.findViewById<TextView>(R.id.btnUpcommingTasks)
        val btnAllUpcomingTasks = view.findViewById<TextView>(R.id.btnAllUpcomingTasks)
        val btnMissedTasks = view.findViewById<TextView>(R.id.btnTaskMissed)
        if (mContext != null) {
            btnUpComingTasks.background = ContextCompat.getDrawable(mContext!!, R.drawable.task_opt_selected)
            btnUpComingTasks.setTextColor(ContextCompat.getColor(mContext!!, R.color.blue))
            btnUpComingTasks.setOnClickListener {
                if (optAllupcom) {
                    optAllupcom = false
                    btnAllUpcomingTasks.setTextColor(ContextCompat.getColor(mContext!!, R.color.grey))
                    btnAllUpcomingTasks.background = ContextCompat.getDrawable(
                        mContext!!,
                        R.drawable.dates_unselected_background_stroke
                    )
                }
                if (optMissed) {
                    optMissed = false
                    btnMissedTasks.setTextColor(ContextCompat.getColor(mContext!!, R.color.grey))
                    btnMissedTasks.background = ContextCompat.getDrawable(
                        mContext!!,
                        R.drawable.dates_unselected_background_stroke
                    )
                }
                optUpcom = true
                btnUpComingTasks.setTextColor(ContextCompat.getColor(mContext!!, R.color.blue))
                btnUpComingTasks.background =
                    ContextCompat.getDrawable(mContext!!, R.drawable.task_opt_selected)
            }
            btnAllUpcomingTasks.setOnClickListener {
                if (optUpcom) {
                    optUpcom = false
                    btnUpComingTasks.setTextColor(ContextCompat.getColor(mContext!!, R.color.grey))
                    btnUpComingTasks.background = ContextCompat.getDrawable(
                        mContext!!,
                        R.drawable.dates_unselected_background_stroke
                    )
                }
                if (optMissed) {
                    optMissed = false
                    btnMissedTasks.setTextColor(ContextCompat.getColor(mContext!!, R.color.grey))
                    btnMissedTasks.background = ContextCompat.getDrawable(
                        mContext!!,
                        R.drawable.dates_unselected_background_stroke
                    )
                }
                optAllupcom = true
                btnAllUpcomingTasks.setTextColor(ContextCompat.getColor(mContext!!, R.color.blue))
                btnAllUpcomingTasks.background =
                    ContextCompat.getDrawable(mContext!!, R.drawable.task_opt_selected)
            }
            btnMissedTasks.setOnClickListener {
                if (optAllupcom) {
                    optAllupcom = false
                    btnAllUpcomingTasks.setTextColor(ContextCompat.getColor(mContext!!, R.color.grey))
                    btnAllUpcomingTasks.background = ContextCompat.getDrawable(
                        mContext!!,
                        R.drawable.dates_unselected_background_stroke
                    )
                }
                if (optUpcom) {
                    optUpcom = false
                    btnUpComingTasks.setTextColor(ContextCompat.getColor(mContext!!, R.color.grey))
                    btnUpComingTasks.background = ContextCompat.getDrawable(
                        mContext!!,
                        R.drawable.dates_unselected_background_stroke
                    )
                }
                optMissed = true
                btnMissedTasks.setTextColor(ContextCompat.getColor(mContext!!, R.color.blue))
                btnMissedTasks.background =
                    ContextCompat.getDrawable(mContext!!, R.drawable.task_opt_selected)
            }
            calenderAdapter = CalenderAdapter(setUpCalender(), mContext!!)
            { position -> onListItemClick(position) }
            rvDates.apply {
                adapter = calenderAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
            taskDao = TaskDatabase.getInstance(mContext!!).dao()
            setUpRvDates()
            //Making today's date as selected
            lastSelectedPosition = cal.get(Calendar.DAY_OF_MONTH) - 1
            //Scroll the recyclerview to the selected position
            rvDates.scrollToPosition(lastSelectedPosition)
            calenderAdapter.changeSelectedDate(lastSelectedPosition, lastSelectedPosition)
            tvMonth.setOnClickListener { pickDate() }
        }
        month = cal.get(Calendar.MONTH)
        day = cal.get(Calendar.DAY_OF_WEEK)
        date = cal.get(Calendar.DATE)
        year = cal.get(Calendar.YEAR)
        tvMonth.text = "$date ${getMonth(month)} $year"
    }

    //Function to set recyclerview tasks
    @OptIn(DelicateCoroutinesApi::class)
    private fun setUpRvDates(){
        var tasksAdapter : TimeLineTasksAdapter? = null
        job = GlobalScope.launch(Dispatchers.Main) {
            tasks = taskDao!!.getAllTasks()
            delay(100)
            if(tasks!=null){
                Log.d(TAG , "tasks size is - ${tasks!!.size}")
                tasksAdapter = TimeLineTasksAdapter(tasks!! , mContext!!)
                val lm = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                rvTasks.apply {
                    adapter = tasksAdapter
                    layoutManager = lm
                }
                //calling the "scalingViewsTimeline()" method for showing the scaling of timeline
                //when the recyclerview is ready
                rvTasks.runWhenReady { tasksAdapter!!.scalingViewsTimeline() }
                delay(100)
                job!!.cancelAndJoin()
            }
            else Log.d(TAG , "tasks are null")
        }
    }

    // Function for setting up the calender and getting the current date and day
    private fun setUpCalender(): MutableList<CalenderDateModel> {
        val dates = ArrayList<Date>()
        val month = cal.clone() as Calendar
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        dates.clear()
        datesList.clear()
        month.set(Calendar.DAY_OF_MONTH, 1)
        while (dates.size < maxDaysInMonth) {
            dates.add(month.time)
            datesList.add(
                CalenderDateModel(
                    month.time.day.toString(),
                    month.time.date.toString(),
                    false
                )
            )
            month.add(Calendar.DAY_OF_MONTH, 1)
        }
        return datesList;
    }

    private fun onListItemClick(position: Int) {
        calenderAdapter.changeSelectedDate(lastSelectedPosition, position)
        lastSelectedPosition = position
    }

    // Function to pick up a date from DatePickerDialog
    private fun pickDate() {
        if (mContext != null) {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
                        this@HomeFragment.month = month
                        this@HomeFragment.year = year
                        this@HomeFragment.date = day
                        updateDate()
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    // Function to update date
    private fun updateDate() {
        tvMonth.text = "$date ${getMonth(month)} $year"
        rvDates.scrollToPosition(date - 1)
        calenderAdapter.changeSelectedDate(lastSelectedPosition, date - 1)
        lastSelectedPosition = date - 1
    }

    // Function to get day from number
    private fun getDay(day: Int): String {
        when (day) {
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

    // Function to get month from number
    private fun getMonth(month: Int): String {
        when (month) {
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

    // Extension method to override the "runWhenReady" method
    fun RecyclerView.runWhenReady(action: () -> Unit) {
        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                action()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    // setting up the mContext when the fragment is attached to the container
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}