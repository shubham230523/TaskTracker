package com.shubham.tasktrackerapp.allupcommingtasks

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.RoomViewModel
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.theme.TaskTrackerTheme
import com.shubham.tasktrackerapp.theme.TaskTrackerTopography
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.min

/**
 * Fragment to show all upcoming tasks
 *
 * @param mContext Context of the main activity
 */
@AndroidEntryPoint
class AllUpcomingFragment(
    private val mContext: Context,
) :
    Fragment() {
    companion object {
        private const val TAG = "AllUpcomingFragment"
    }

    private val viewModel by viewModels<RoomViewModel>()
    private val viewModelAllUpTasks by viewModels<AllUpcomingTasksViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View =
            inflater.inflate(R.layout.fragment_all_upcoming_tasks, container, false)
        rootView.findViewById<ComposeView>(R.id.allUpcomTasks_compose_view).apply {
            setContent {
                TaskTrackerTheme {
                    val tasks by viewModel.getTasks().observeAsState()
                    tasks?.let { AllUpcomingTasksList(it) }
                }
            }
        }
        return rootView
    }
}

@Composable
fun AllUpcomingTasksList(taskList: List<Task>) {
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(taskList) { task ->
            ListTaskItem(task) {
                // TODO to show pop up menu on options clicked
            }
        }
    }
}

@Composable
fun ListTaskItem(task: Task, onClick: () -> Unit) {
    Surface(
        color = Color.Transparent,
        tonalElevation = 2.dp,
        modifier = Modifier
            .padding(top = 5.dp)
            .fillMaxWidth()
            .wrapContentHeight(),

        ) {
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .padding(15.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = task.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = TaskTrackerTopography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Image(
                    painterResource(id = R.drawable.menu__colon),
                    contentDescription = "task options",
                    modifier = Modifier
                        .size(16.dp)
                        .clickable {
                            onClick()
                        }
                )
            }
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.padding(top = 7.dp)
            ) {
                Text(
                    text = task.due_date,
                    style = TaskTrackerTopography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${task.start_time}-${task.end_time}",
                    style = TaskTrackerTopography.labelMedium,
                    modifier = Modifier.padding(start = 20.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
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
                        color = if (i == 0) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSecondary
                        },
                        modifier = if (i == 0) {
                            Modifier
                                .padding(end = 8.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        } else {
                            Modifier
                                .padding(end = 8.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondary,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        }
                    )
                }
                if (task.taskTypes.size > 2) {
                    Text(
                        text = "+${task.taskTypes.size - 2} more",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}