package com.shubham.tasktrackerapp.Adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.shubham.tasktrackerapp.Fragments.AllUpcomingFragment
import com.shubham.tasktrackerapp.Fragments.MissedTasksFragment
import com.shubham.tasktrackerapp.Fragments.UpcomingTasksFragment

class ViewPagerTasksAdapter(private val mContext : Context, fragmentManager: FragmentManager , lifecycle: Lifecycle)
    :FragmentStateAdapter(fragmentManager, lifecycle){
    companion object{
        private const val NUM_TABS = 3
    }
    override fun getItemCount() = NUM_TABS

    override fun createFragment(position: Int): Fragment {
        when(position){
            0 -> return UpcomingTasksFragment(mContext)
            1 -> return AllUpcomingFragment()
        }
        return MissedTasksFragment()
    }
}