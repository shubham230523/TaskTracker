package com.shubham.tasktrackerapp.upcommingtasks

import android.util.Log
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
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.shubham.tasktrackerapp.allupcommingtasks.AllUpcomingTasksViewModel
import com.shubham.tasktrackerapp.data.local.Task
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.min

/**
 * Represents upcoming task in home fragment
 */
const val TAG = "UpcomingTasksFragment"

@Composable
fun UpcomingTasks(navController: NavController) {
    val notificationId = 0
    val mContext = LocalContext.current
    val viewModel = hiltViewModel<AllUpcomingTasksViewModel>()
    val taskList = viewModel.getTasksFromDatabase().observeAsState()
    Log.d(TAG , "taskList size - ${taskList.value?.size}")
    val sortedList by remember {
        derivedStateOf {
            taskList.value?.filter { it.due_date.equals(LocalDate.now()) }?.sortedWith(compareBy<Task> { it.due_date }.thenBy { it.start_time }
                .thenBy { it.end_time })
                ?.subList(0, min(2 , taskList.value!!.size))
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
                    Log.d(TAG , "increment value - $increment")
                    while (currTimeSeconds < task1StartTimeSeconds) {
                        scaleBox += increment
                        currTimeSeconds++
                        delay(1000)
                    }
                }
                changeTask1StopSign = true
                startScalingTiemLineTask1 = true
            })
            LazyColumn {
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
                        sign
                    ) {
                        if (index == 0) {
                            changeTask2StopSign = true
                            startScalingTiemLineTask2 = true
                        } else if (index == 1) {
                            changeTask3StopSign = true
                        }
                    }
                }
            }
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
        ){
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_menu),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${task.added_date.dayOfMonth} ${task.added_date.month}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Due - ${task.due_date.dayOfMonth} ${task.due_date.monthValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
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
            val uriList = mutableListOf<String>()
            val nameList = mutableListOf<String>()
            task.attachments.forEach { uri, name ->
                uriList.add(uri)
                nameList.add(name)
            }
            if (showAttachments) {
                for (i in 0 until task.attachments.size) {
                    Row(
                        modifier =
                        Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
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
                            )
                    ) {
                        ConstraintLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            val (fileImg, filename, close) = createRefs()
                            val array = nameList[i].split(".")
                            Image(
                                painter = painterResource(id = getFileType(extension = array[array.size - 1])),
                                contentDescription = null,
                                modifier = Modifier.constrainAs(fileImg) {
                                    start.linkTo(parent.start)
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    width = Dimension.value(16.dp)
                                    height = Dimension.value(16.dp)
                                }
                            )
                            Text(
                                text = nameList[i],
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.constrainAs(filename) {
                                    start.linkTo(fileImg.end, 10.dp)
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    end.linkTo(close.end, 10.dp)
                                }
                            )
                            Icon(
                                Icons.Filled.Close,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                contentDescription = null,
                                modifier = Modifier.constrainAs(close) {
                                    end.linkTo(parent.end)
                                    top.linkTo(parent.top)
                                    bottom.linkTo(parent.bottom)
                                    width = Dimension.value(16.dp)
                                    height = Dimension.value(16.dp)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    if (startScaling && nextTaskStartTime != null) {
        LaunchedEffect(key1 = Unit, block = {
            var currentTimeSeconds =
                LocalTime.now().hour * 60 * 60 + LocalTime.now().minute * 60 + LocalTime.now().second
            val nextTaskStartTimeSeconds =
                nextTaskStartTime.hour * 60 * 60 + nextTaskStartTime.minute * 60
            Log.d(
                TAG,
                "currentTimeSeconds - $currentTimeSeconds nextTaskStartTimeSeconds - $nextTaskStartTimeSeconds"
            )
            Log.d(TAG, "task title - ${task.title}")
            Log.d(TAG, "current time - ${LocalTime.now()} nextTaskStarTime - $nextTaskStartTime")
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
    Log.d(TAG, "Show notification called title - $title")
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