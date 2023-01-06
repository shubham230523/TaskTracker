package com.shubham.tasktrackerapp.util

import com.shubham.tasktrackerapp.R
sealed class BottomNavItems(var title: String, var icon: Int, var route: String){
    object Home: BottomNavItems("Home", R.drawable.home, "home")
    object NewTask: BottomNavItems("New task" , R.drawable.ic_add, "new_task")
    object DashBoard: BottomNavItems("Dashboard" , R.drawable.dashboard, "dashboard")
}
