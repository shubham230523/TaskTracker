package com.shubham.tasktrackerapp.Fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.Adapter.AllUpcomingTasksAdapter
import com.shubham.tasktrackerapp.MainActivity
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.db.Task

class AllUpcomingFragment(private val mContext : Context) : Fragment(R.layout.fragment_all_upcoming_tasks){
    companion object{
        private const val TAG = "AllUpcomingFragment"
    }
    private var mTasks : List<Task>? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvAllUpTasks = view.findViewById<RecyclerView>(R.id.rv_allupcomingtasks)
        mTasks = (mContext as MainActivity).getTasksList()
        if(mTasks?.size!! > 0){
            Log.d(TAG , "mTasks size - ${mTasks!!.size}")
            val mAdapter = AllUpcomingTasksAdapter(mTasks!!, mContext)
            rvAllUpTasks.apply {
                adapter = mAdapter
                layoutManager = LinearLayoutManager(mContext , LinearLayoutManager.VERTICAL , false)
            }
        }
        else Log.d(TAG , "mTasks is null or size is 0")
    }
}