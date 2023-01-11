package com.shubham.tasktrackerapp.util

import android.graphics.Color
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance

val newTaskColors = arrayOf(
    Color.parseColor("#FFEBEE"), // light red
    Color.parseColor("#F3E5F5"), // light purple
    Color.parseColor("#E0F7FA"), // light blue
    Color.parseColor("#FFF3E0"), // light orange
    Color.parseColor("#EFEBE9"), // light grey
)

var taskCategories = mutableListOf(
    "Assignment", "Project", "Coding",
    "Classes", "Hobby", "Meeting", "Playing", "Hangout", "Food", "Television",
    "Exercise", "Remainder", "Other"
)

@Composable
fun ColorScheme.isLight() = this.background.luminance() > 0.5