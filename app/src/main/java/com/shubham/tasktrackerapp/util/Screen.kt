package com.shubham.tasktrackerapp.util

sealed class Screen(val route: String){
    object Home: Screen("home")
    object NewTask: Screen("new_task")
    object DashBoard: Screen("dashboard")
    object EditTask: Screen("editScreen")
}
