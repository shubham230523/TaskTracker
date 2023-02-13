package com.shubham.tasktrackerapp.upcommingtasks

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.RoomViewModel
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.util.Screen
import com.shubham.tasktrackerapp.util.getMonthShortForm
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.min

/**
 * Represents upcoming tasks in home fragment
 */
const val TAG = "UpcomingTasksFragment"

@Composable
fun UpcomingTasks(navController: NavController) {
    val notificationId = 0
    val mContext = LocalContext.current
    val viewModel = hiltViewModel<RoomViewModel>()
    val taskList = viewModel.getTasks().observeAsState()

    // filter the list by today's date and then sorting the list by start time and end time respectively
    val sortedList by remember {
        derivedStateOf {
            var filterListSize = 0 // to get the list size after filtering it by today's date
            taskList.value?.filter {
                it.due_date.equals(LocalDate.now()).apply {
                    if (this) {
                        filterListSize++
                    }
                }
            }?.sortedWith(compareBy<Task> { it.due_date }.thenBy { it.start_time }
                .thenBy { it.end_time })
                ?.subList(0, min(filterListSize, taskList.value!!.size))
        }
    }

    if (sortedList != null && sortedList!!.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            var startScalingTiemLineTask1 by remember { mutableStateOf(false) }
            var startScalingTiemLineTask2 by remember { mutableStateOf(false) }
            val startScalingTiemLineTask3 by remember { mutableStateOf(false) }
            var changeTask1StopSign by remember { mutableStateOf(false) }
            var changeTask2StopSign by remember { mutableStateOf(false) }
            var changeTask3StopSign by remember { mutableStateOf(false) }
            var scaleBox by remember { mutableStateOf(0.dp) }

            // for scaling the box which represents the time gone from start of the day
            // with respect to first task start time
            LaunchedEffect(key1 = Unit, block = {
                var currTimeSeconds =
                    LocalTime.now().hour * 60 * 60 + LocalTime.now().minute * 60 + LocalTime.now().second
                val task1StartTimeSeconds =
                    sortedList!![0].start_time.hour * 60 * 60 + sortedList!![0].start_time.minute * 60
                if (currTimeSeconds == task1StartTimeSeconds || currTimeSeconds > task1StartTimeSeconds) {
                    scaleBox = 60.dp
                } else {
                    val diff = task1StartTimeSeconds - currTimeSeconds
                    val increment = 60.dp / diff
                    while (currTimeSeconds < task1StartTimeSeconds) {
                        scaleBox += increment
                        currTimeSeconds++
                        delay(1000)
                    }
                }
                // marking that the first task has started or reached and starting the scaling of first item
                // timeline
                changeTask1StopSign = true
                startScalingTiemLineTask1 = true
            })

            LazyColumn {
                // first item representing the duration of time occurred from start of the day with respect
                // to first task
                item {
                    Column(
                        modifier = Modifier
                            .offset(x = 60.dp)
                            .height(60.dp)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(scaleBox)
                                .background(
                                    color = MaterialTheme.colorScheme.primary
                                )
                        )
                    }
                }
                itemsIndexed(sortedList!!) { index, item ->
                    val scale: Boolean
                    val sign: Boolean
                    var nextTaskStartTime: LocalTime? = null
                    if (index == 0) {
                        scale = startScalingTiemLineTask1
                        sign = changeTask1StopSign
                    } else if (index == 1) {
                        scale = startScalingTiemLineTask2
                        sign = changeTask2StopSign
                    } else {
                        scale = startScalingTiemLineTask3
                        sign = changeTask3StopSign
                    }

                    if (index + 1 < sortedList!!.size) {
                        nextTaskStartTime = sortedList!![index + 1].start_time
                    }
                    TimeLineTaskItem(
                        task = sortedList!![index],
                        nextTaskStartTime,
                        startScaling = scale,
                        sign,
                        navController
                    ) {
                        // when first task finishes scaling we are marking that second task has reached or
                        // started and starts scaling the second task
                        if (index == 0) {
                            changeTask2StopSign = true
                            startScalingTiemLineTask2 = true
                            // when second task finishes scaling we are marking that third task has reached or started
                        } else if (index == 1) {
                            changeTask3StopSign = true
                        }
                    }
                }
            }

            // giving the notifications when the task has reached and checking whether the task start time has
            // become equal to current time to avoid giving notification on recomposing
            if (
                changeTask1StopSign &&
                sortedList!![0].start_time.hour == LocalTime.now().hour &&
                sortedList!![0].start_time.minute == LocalTime.now().minute &&
                sortedList!![0].start_time.second == LocalTime.now().second
            ) {
                ShowNotification(
                    notificationId + 1,
                    sortedList!![0].title,
                    "Here is the remainder for the task to be done, from ${sortedList!![0].start_time} to ${sortedList!![0].end_time}"
                )
            }
            if (
                changeTask2StopSign &&
                sortedList!![1].start_time.hour == LocalTime.now().hour &&
                sortedList!![1].start_time.minute == LocalTime.now().minute &&
                sortedList!![1].start_time.second == LocalTime.now().second
            ) {
                ShowNotification(
                    notificationId + 2,
                    sortedList!![1].title,
                    "Here is the remainder for the task to be done, from ${sortedList!![1].start_time} to ${sortedList!![1].end_time}"
                )
            }
            if (
                changeTask3StopSign &&
                sortedList!![2].start_time.hour == LocalTime.now().hour &&
                sortedList!![2].start_time.minute == LocalTime.now().minute &&
                sortedList!![2].start_time.second == LocalTime.now().second
            ) {
                ShowNotification(
                    notificationId + 3,
                    sortedList!![2].title,
                    "Here is the remainder of the task to be done, from ${sortedList!![2].start_time} to ${sortedList!![2].end_time}"
                )
            }
        }
    } else {
        // An empty white board show when their are no tasks for the day
        BoxWithConstraints {
            Image(
                painter = painterResource(id = R.drawable.no_tasks),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(0.5f)
                    .alpha(0.5f)
                    .align(Alignment.Center)
            )
            Text(
                text = "No tasks to show",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .alpha(0.5f)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun TimeLineTaskItem(
    task: Task,
    nextTaskStartTime: LocalTime?,
    startScaling: Boolean,
    changeStopSign: Boolean,
    navController: NavController,
    viewModel: RoomViewModel = hiltViewModel(),
    onClick: () -> Unit
) {
    var scaleBox by remember { mutableStateOf(0.dp) }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(start = 10.dp, end = 10.dp)
    ) {
        val (startTime, endTime, timeLineStop, timeLine, taskCard) = createRefs()
        // box inside box to show grey/white back ground for time line when it is scaling
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                .constrainAs(timeLine) {
                    start.linkTo(parent.start, 50.dp)
                    top.linkTo(parent.top)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, scaleBox)
                    .background(color = MaterialTheme.colorScheme.primary)
            )
        }
        Image(
            painter = painterResource(
                id = if (changeStopSign) {
                    R.drawable.ic_task_done
                } else R.drawable.ic_task_stop
            ),
            contentDescription = null,
            modifier = Modifier.constrainAs(timeLineStop) {
                start.linkTo(timeLine.start)
                end.linkTo(timeLine.end)
                top.linkTo(timeLine.top)
                width = Dimension.value(24.dp)
                height = Dimension.value(24.dp)
            }
        )
        Text(
            text = "${task.start_time.hour}:${task.start_time.minute}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.constrainAs(startTime) {
                top.linkTo(timeLineStop.top)
                bottom.linkTo(timeLineStop.bottom)
                end.linkTo(timeLineStop.start, 10.dp)
            }
        )
        Text(
            text = "${task.end_time.hour}:${task.end_time.minute}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.constrainAs(endTime) {
                top.linkTo(startTime.bottom)
                start.linkTo(startTime.start, 10.dp)
                end.linkTo(startTime.end)
            }
        )

        // task card
        Column(
            modifier = Modifier
                .constrainAs(taskCard) {
                    start.linkTo(timeLineStop.end, 5.dp)
                    end.linkTo(parent.end, 10.dp)
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(15.dp)
                )
                .padding(10.dp)
        ) {
            // title and menu option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                var showTaskOptMenu by remember { mutableStateOf(false) }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                showTaskOptMenu = !showTaskOptMenu
                            }
                    )
                    DropdownMenu(
                        expanded = showTaskOptMenu,
                        onDismissRequest = { showTaskOptMenu = false },
                        modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        DropdownMenuItem(onClick = {
                            viewModel.deleteTask(task)
                            showTaskOptMenu = false
                        }) {
                            Text(
                                text = "Delete",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        DropdownMenuItem(onClick = {
                            navController.navigate(Screen.EditTask.route.plus("/${task.id}"))
                            showTaskOptMenu = false
                        }) {
                            Text(
                                text = "Edit",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // added date and due date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${task.added_date.dayOfMonth} ${getMonthShortForm(task.added_date.monthValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Due - ${task.due_date.dayOfMonth} ${getMonthShortForm(task.due_date.monthValue)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // task categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until min(2, task.taskTypes.size)) {
                    Text(
                        text = task.taskTypes[i],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(6.dp)
                    )
                }
                if (task.taskTypes.size > 2) {
                    Text(
                        text = "+${task.taskTypes.size - 2} more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            var showAttachments by remember { mutableStateOf(false) }

            // attachments text row
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (task.attachments.size == 0) {
                        "No Attachment"
                    } else {
                        "${task.attachments.size} Attachments"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (task.attachments.size > 0) {
                    Image(
                        painter = painterResource(
                            id = if (showAttachments) {
                                R.drawable.ic_up_arrow
                            } else R.drawable.ic_down_arrow
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                if (task.attachments.size != 0) {
                                    showAttachments = !showAttachments
                                }
                            }
                    )
                }
            }


            val uriList = mutableListOf<String>()
            val nameList = mutableListOf<String>()
            task.attachments.forEach { uri, name ->
                uriList.add(uri)
                nameList.add(name)
            }

            // attachments
            if (showAttachments) {
                for (i in 0 until task.attachments.size) {
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(
                                color = MaterialTheme.colorScheme.inversePrimary,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(6.dp)
                            .animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = LinearEasing
                                )
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        val array = nameList[i].split(".")
                        Image(
                            painter = painterResource(id = getFileType(extension = array[array.size - 1])),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = nameList[i],
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                }
            }
        }
    }

    // scaling the timeline box when start scaling is true and the task is not the last task
    if (startScaling && nextTaskStartTime != null) {
        LaunchedEffect(key1 = Unit, block = {
            var currentTimeSeconds =
                LocalTime.now().hour * 60 * 60 + LocalTime.now().minute * 60 + LocalTime.now().second
            val nextTaskStartTimeSeconds =
                nextTaskStartTime.hour * 60 * 60 + nextTaskStartTime.minute * 60
            val max = 200.dp
            if (currentTimeSeconds == nextTaskStartTimeSeconds || currentTimeSeconds > nextTaskStartTimeSeconds) {
                scaleBox = max
            } else {
                val diff = nextTaskStartTimeSeconds - currentTimeSeconds
                val increment = max / diff
                while (currentTimeSeconds < nextTaskStartTimeSeconds) {
                    scaleBox += increment
                    currentTimeSeconds++
                    delay(1000)
                }
            }
            onClick()
        })
    }
}

@Composable
fun getFileType(extension: String): Int {
    when (extension) {
        "pdf" -> return R.drawable.pdf
        "png", "jpeg", "jpg" -> return R.drawable.ic_image
        "ppt", "pptx" -> return R.drawable.powerpoint
        "txt" -> return R.drawable.txt
        "mp3", "mp4" -> return R.drawable.video
        "doc", "docx" -> return R.drawable.word
    }
    return R.drawable.unknown_file_type
}

@Composable
fun ShowNotification(
    notificationId: Int,
    title: String,
    content: String,
) {
    val context = LocalContext.current
    val builder = NotificationCompat.Builder(context, stringResource(id = R.string.CHANNEL_ID))
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, builder.build())
    }
}