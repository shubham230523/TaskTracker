package com.shubham.tasktrackerapp.allupcommingtasks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.RoomViewModel
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.theme.TaskTrackerTopography
import com.shubham.tasktrackerapp.util.Screen
import java.time.LocalDate
import kotlin.math.min

/**
 * Composable to show all upcoming tasks
 */
const val TAG = "AllUpcomingTasksFragment"

@Composable
fun AllUpcomingTasksList(navController: NavController) {
    val viewModel = hiltViewModel<RoomViewModel>()
    val taskList = viewModel.getTasks().observeAsState()

    // sorting the tasks by due date first and then by start time and after that by end time
    // we are selecting all the tasks except the first 3 tasks of the current day which will be shown in
    // upcoming tasks screen
    val sortedList by remember {
        derivedStateOf {
            var count = 0
            taskList.value?.let { list ->
                list.sortedWith(compareBy<Task> { it.due_date }.thenBy { it.start_time }
                    .thenBy { it.end_time })
                    .dropWhile {
                        (it.due_date == LocalDate.now() && count != 3).apply { count++ }
                    }
            }
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (sortedList != null) {
            items(sortedList!!) { task ->
                ListTaskItem(task = task, navController)
            }
        }
    }
}

@Composable
fun ListTaskItem(task: Task, navController: NavController) {
    var showDropDownMenu by remember { mutableStateOf(false) }
    val roomViewModel = hiltViewModel<RoomViewModel>()

    Surface(
        color = Color.Transparent,
        tonalElevation = 2.dp,
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                .padding(15.dp)
        ) {
            // task title and menu option
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = task.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = TaskTrackerTopography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painterResource(id = R.drawable.menu__colon),
                        contentDescription = "task options",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                showDropDownMenu = !showDropDownMenu
                            }
                    )
                    DropdownMenu(
                        expanded = showDropDownMenu,
                        onDismissRequest = { showDropDownMenu = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                roomViewModel.deleteTask(task)
                                showDropDownMenu = false
                            },
                            enabled = true,
                            contentPadding = MenuDefaults.DropdownMenuItemContentPadding,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            Text(
                                text = "Delete",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        DropdownMenuItem(
                            onClick = {
                                showDropDownMenu = false
                                navController.navigate(
                                    Screen.EditTask.route.plus("/${task.id}"),
                                )
                            },
                            enabled = true,
                            contentPadding = MenuDefaults.DropdownMenuItemContentPadding,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Text(
                                text = "Edit",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Due date and time
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(top = 7.dp)
            ) {
                Text(
                    text = "Due date: ${task.due_date}",
                    style = TaskTrackerTopography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1
                )
                Text(
                    text = "Time: ${task.start_time} - ${task.end_time}",
                    style = TaskTrackerTopography.labelMedium,
                    modifier = Modifier.padding(start = 20.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1
                )
            }

            // task categories
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 7.dp)
            ) {
                for (i in 0 until min(task.taskTypes.size, 2)) {
                    Text(
                        text = task.taskTypes[i],
                        style = TaskTrackerTopography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    )
                }
                if (task.taskTypes.size > 2) {
                    Text(
                        text = "+${task.taskTypes.size - 2} more",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Attachments text
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 7.dp)
            ) {
                Text(
                    text = "${task.attachments.size} " + if (task.attachments.size > 1) "Attachments" else "Attachment",
                    style = TaskTrackerTopography.labelMedium,
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}