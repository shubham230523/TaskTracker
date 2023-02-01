package com.shubham.tasktrackerapp

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.shubham.tasktrackerapp.data.local.Task

@Composable
fun EditTaskScreen(task : Task, navController: NavController) {
    Text(text = task.title)
}