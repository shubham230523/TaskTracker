package com.shubham.tasktrackerapp.Fragments

import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import com.shubham.tasktrackerapp.Adapter.TimeLineTasksAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.MainActivity
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.db.Task
import kotlinx.coroutines.*

class UpcomingTasksFragment(private val mContext : Context) : Fragment(R.layout.fragment_upcoming_tasks) {
    private var mTasks : List<Task>? = null
    companion object{
        private const val TAG = "UpcomingTasksFragment"
        private var mTasks : List<Task>? = null

        // Function to get all tasks
        fun getAllTasks(tasks : List<Task>?){
            mTasks = tasks
        }
    }
    private lateinit var rvTasks : RecyclerView
    private var job: Job? = null
    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvTasks= view.findViewById(R.id.rv_tasks)
        job = GlobalScope.launch (Dispatchers.Main){
//            delay(400)
            Log.d(TAG , "upTaskFrg - ${System.currentTimeMillis()}")
            Log.d(TAG , "tasklist - ${mTasks}")
            mTasks = (context as MainActivity).getTasksList()
            Log.d(TAG , "time - ${System.currentTimeMillis()}")
            Log.d(TAG ,  "tasklist - ${mTasks} ${System.currentTimeMillis()}")
            if(mTasks!=null && mTasks!!.size > 0){
                val taskAdapter = TimeLineTasksAdapter(mTasks!! , mContext)
                rvTasks.apply {
                    adapter = taskAdapter
                    layoutManager = LinearLayoutManager(mContext , LinearLayoutManager.VERTICAL , false)
                }
                rvTasks.runWhenReady { taskAdapter.scalingViewsTimeline() }
            }
            else Log.d(TAG , "mTasks is null - ${System.currentTimeMillis()}")
            delay(1000)
            job!!.cancelAndJoin()
        }
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

}