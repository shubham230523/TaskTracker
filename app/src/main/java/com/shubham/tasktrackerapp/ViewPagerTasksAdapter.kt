package com.shubham.tasktrackerapp

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.shubham.tasktrackerapp.allupcommingtasks.AllUpcomingFragment
import com.shubham.tasktrackerapp.upcommingtasks.UpcomingTasksFragment

class ViewPagerTasksAdapter(
    private val mContext: Context,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    companion object {
        private const val NUM_TABS = 2
    }

    override fun getItemCount() = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return UpcomingTasksFragment(mContext)
        }
        return AllUpcomingFragment(mContext)
    }
}