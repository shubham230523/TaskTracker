package com.shubham.tasktrackerapp.Fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shubham.tasktrackerapp.Adapter.CalenderAdapter
import com.shubham.tasktrackerapp.Adapter.ViewPagerTasksAdapter
import com.shubham.tasktrackerapp.Models.CalenderDateModel
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.db.Task
import com.shubham.tasktrackerapp.db.TaskDao
import com.shubham.tasktrackerapp.db.TaskDatabase
import kotlinx.coroutines.*
import java.util.*

class HomeFragment() : Fragment(R.layout.fragment_home) {
    companion object {
        private const val TAG = "HomeFragment"
    }

    private var tasks: List<Task>? = null
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
    private var mContext = activity?.applicationContext
    private var job: Job? = null
    var tabsList = arrayOf("Upcoming", "All Upcoming", "Missed")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvDates = view.findViewById(R.id.rv_dates)
        tvMonth = view.findViewById(R.id.tv_month)
        val viewPager = view.findViewById<ViewPager2>(R.id.tasksViewPager)
        val tabLayout = view.findViewById<TabLayout>(R.id.tasksTabLayout)
        val pagerAdapter = ViewPagerTasksAdapter(mContext!! , parentFragmentManager, lifecycle)

        viewPager.adapter = pagerAdapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabsList[position]
        }.attach()

        if (mContext != null) {
            calenderAdapter = CalenderAdapter(setUpCalender(), mContext!!)
            { position -> onListItemClick(position) }
            rvDates.apply {
                adapter = calenderAdapter
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            }
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

    // setting up the mContext when the fragment is attached to the container
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}