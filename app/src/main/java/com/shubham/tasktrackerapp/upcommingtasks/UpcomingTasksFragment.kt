package com.shubham.tasktrackerapp.upcommingtasks

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.MainActivity
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.data.local.Task
import kotlinx.coroutines.*

/**
 * Represents upcoming task in home fragment
 */
class UpcomingTasksFragment(private val mContext: Context) :
    Fragment(R.layout.fragment_upcoming_tasks) {
    private var mTasks: List<Task>? = null

    companion object {
        private const val TAG = "UpcomingTasksFragment"
        private var mTasks: List<Task>? = null
    }

    private lateinit var rvTasks: RecyclerView
    private var job: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvTasks = view.findViewById(R.id.rv_tasks)

        job = GlobalScope.launch(Dispatchers.Main) {
            //mTasks = (context as MainActivity).getTasksList()
            if (mTasks != null && mTasks!!.size > 0) {
                val taskAdapter = TimeLineTasksAdapter(mTasks!!, mContext)
                rvTasks.apply {
                    adapter = taskAdapter
                    layoutManager =
                        LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
                }
                rvTasks.runWhenReady { taskAdapter.scalingViewsTimeline() }
            }
            delay(1000)
            job!!.cancelAndJoin()
        }
    }

    /**
     * Extension method to override the "runWhenReady" method
     */
    private fun RecyclerView.runWhenReady(action: () -> Unit) {
        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                action()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }
}