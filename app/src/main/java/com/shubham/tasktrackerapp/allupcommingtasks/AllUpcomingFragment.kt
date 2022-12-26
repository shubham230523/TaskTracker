package com.shubham.tasktrackerapp.allupcommingtasks

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.RoomViewModel
import com.shubham.tasktrackerapp.data.local.Task
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment to show all upcoming tasks
 *
 * @param mContext Context of the main activity
 */
@AndroidEntryPoint
class AllUpcomingFragment(
    private val mContext: Context,
) :
    Fragment(R.layout.fragment_all_upcoming_tasks) {
    companion object {
        private const val TAG = "AllUpcomingFragment"
    }

    private val viewModel by viewModels<RoomViewModel>()
    private val viewModelAllUpTasks by viewModels<AllUpcomingTasksViewModel>()

    private lateinit var tasks: LiveData<List<Task>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tasks = viewModel.getTasks()

        val rvAllUpTasks = view.findViewById<RecyclerView>(R.id.rv_allupcomingtasks)
        val mAdapter = AllUpcomingTasksAdapter(mContext , viewModelAllUpTasks)
        rvAllUpTasks.apply {
            adapter = mAdapter
            layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        }

        tasks.observe(viewLifecycleOwner, Observer {
            mAdapter.setData(it)
        })
    }

}