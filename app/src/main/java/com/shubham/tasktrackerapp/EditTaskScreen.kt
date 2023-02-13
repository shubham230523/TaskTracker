package com.shubham.tasktrackerapp

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MenuDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shubham.tasktrackerapp.theme.TaskTrackerTopography
import com.shubham.tasktrackerapp.util.FileCache
import com.shubham.tasktrackerapp.util.taskCategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

const val TAG = "EditTaskScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditTaskScreen(taskId: String, navController: NavController) {

    val roomViewModel = hiltViewModel<RoomViewModel>()
    val task = roomViewModel.getTaskById(taskId.toInt()).observeAsState()
    val mContext = LocalContext.current

    // to cache and store the selected attachment or file
    val fileCache = remember {
        FileCache(mContext)
    }

    if (task.value != null) {
        val taskCategoryListSelected = remember { mutableStateListOf<String>() }
        val attachments = remember { mutableStateListOf<Pair<String, String>>() }
        LaunchedEffect(key1 = Unit, block = {
            taskCategoryListSelected.addAll(task.value!!.taskTypes)
            task.value!!.attachments.forEach { (uri, name) -> attachments.add(Pair(uri, name)) }
        })

        var taskTitle by remember { mutableStateOf(task.value!!.title) }
        var year by remember { mutableStateOf(task.value!!.due_date.year.toString()) }
        var month by remember { mutableStateOf(task.value!!.due_date.monthValue.toString()) }
        var day by remember { mutableStateOf(task.value!!.due_date.dayOfMonth.toString()) }
        var hour by remember { mutableStateOf(task.value!!.start_time.hour.toString()) }
        var minutes by remember { mutableStateOf(task.value!!.start_time.minute.toString()) }
        var hourEndTime by remember { mutableStateOf(task.value!!.end_time.hour.toString()) }
        var minutesEndTime by remember { mutableStateOf(task.value!!.end_time.minute.toString()) }

        val showTitleError by remember {
            derivedStateOf {
                taskTitle == ""
            }
        }

        val date = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        // launched effect because we don't want to recalculate hour, min otherwise they will not be
        // updated when selected
//        LaunchedEffect(key1 = Unit, block = {
//            if(date.equals(LocalDate.now())){
//                hour = LocalTime.now().hour.toString()
//                minutes = LocalTime.now().minute.toString()
//                if(LocalTime.now().minute + 30 <60){
//                    hourEndTime = hour
//                    minutesEndTime = (minutes.toInt()+30).toString()
//                }
//                else {
//                    hourEndTime = (hour.toInt() + 1).toString()
//                    minutesEndTime = (30 - (60 - minutes.toInt())).toString()
//                }
//            }
//        })

        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(15.dp)
                .fillMaxSize()
        ) {
            // headline and save button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Task Details",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Button(
                    onClick = {
                        val startTime = LocalTime.of(hour.toInt(), minutes.toInt())
                        val endTime = LocalTime.of(hourEndTime.toInt(), minutesEndTime.toInt())

                        if (showTitleError) {
                            Toast.makeText(mContext, "Title required", Toast.LENGTH_SHORT).show()
                        } else if (taskCategoryListSelected.size == 0) {
                            Toast.makeText(
                                mContext,
                                "Select at least one task type",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (startTime.equals(endTime) && startTime.isAfter(endTime)) {
                            Toast.makeText(mContext, "Invalid time interval", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            val hashMap = hashMapOf<String, String>()
                            attachments.forEach {
                                hashMap[it.first] = it.second
                            }
                            task.value!!.title = taskTitle
                            task.value!!.due_date =
                                LocalDate.of(year.toInt(), month.toInt(), day.toInt())
                            task.value!!.start_time = startTime
                            task.value!!.end_time = endTime
                            task.value!!.taskTypes = taskCategoryListSelected
                            task.value!!.attachments = hashMap
                            task.value!!.added_date = LocalDate.now()
                            roomViewModel.updateTask(task.value!!)
                        }
                        roomViewModel.updateTask(task.value!!)
                        navController.popBackStack()
                    },
                    shape = RoundedCornerShape(7.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text(
                        text = "Save",
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            // Title text and text field
            Row(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Title",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = {
                        taskTitle = it
                    },
                    placeholder = {
                        Text(
                            text = "Task title ",
                            modifier = Modifier.alpha(0.4f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    modifier = Modifier
                        .padding(start = 15.dp, end = 15.dp)
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(color = MaterialTheme.colorScheme.primaryContainer),

                    colors = TextFieldDefaults.textFieldColors(
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        placeholderColor = MaterialTheme.colorScheme.onSurface,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            if (showTitleError) {
                Text(
                    text = "* Title is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Due date and date dropdowns to select
            Row(
                modifier = Modifier.padding(top = 15.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showYearDropDown by remember { mutableStateOf(false) }
                var showMonthDropDown by remember { mutableStateOf(false) }
                var showDayDropDown by remember { mutableStateOf(false) }
                Text(
                    text = "Due date",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge
                )

                // selected year
                Text(
                    text = year,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // year icon and drop down
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (showYearDropDown) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(16.dp)
                            .clickable {
                                showYearDropDown = !showYearDropDown
                            }
                    )
                    DropdownMenu(
                        expanded = showYearDropDown,
                        onDismissRequest = {
                            showYearDropDown = false
                        },
                        modifier = Modifier
                            .height(300.dp)
                            .align(Alignment.BottomCenter)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),

                        ) {
                        for (i in LocalDate.now().year until LocalDate.now().year + 25) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    year = i.toString()
                                    showYearDropDown = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                enabled = true
                            )
                        }
                    }
                }

                // selected month
                Text(
                    text = month,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // month selector icon and drop down
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (showMonthDropDown) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(16.dp)
                            .clickable {
                                showMonthDropDown = !showMonthDropDown
                            }

                    )
                    DropdownMenu(
                        expanded = showMonthDropDown,
                        onDismissRequest = {
                            showMonthDropDown = false
                        },
                        modifier = Modifier
                            .height(300.dp)
                            .align(Alignment.BottomCenter)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),

                        ) {
                        var m = LocalDate.now().monthValue
                        // if selected year value is not equal to current year value then m will range from 1 to 12
                        // else m will have value from current month value to 12
                        if (year.toInt() != LocalDate.now().year) {
                            m = 1
                        }
                        for (i in m until 13) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    month = i.toString()
                                    showMonthDropDown = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                enabled = true
                            )
                        }
                    }
                }

                // selected day
                Text(
                    text = day,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // day selector icon and drop down
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (showDayDropDown) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(16.dp)
                            .clickable {
                                showDayDropDown = !showDayDropDown
                            }
                    )
                    DropdownMenu(
                        expanded = showDayDropDown,
                        onDismissRequest = {
                            showDayDropDown = false
                        },
                        modifier = Modifier
                            .height(300.dp)
                            .align(Alignment.BottomCenter)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),

                        ) {
                        var d = LocalDate.now().dayOfMonth
                        // if the selected month is not equal to current month then d will start from 1
                        // else it will start from current day of month
                        if (month.toInt() != LocalDate.now().monthValue) {
                            d = 1
                        }
                        // d will have values till max days in current month
                        for (i in d until LocalDate.of(year.toInt(), month.toInt(), 1)
                            .lengthOfMonth() + 1) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    day = i.toString()
                                    showDayDropDown = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                enabled = true
                            )
                        }
                    }
                }
            }

            // Start time text and time selecting drop down menus
            Row(
                modifier = Modifier.padding(top = 15.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showStartTimeHourDropDown by remember { mutableStateOf(false) }
                var showStartTimeMinDropDown by remember { mutableStateOf(false) }
                Text(
                    text = "Start time",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge
                )

                // selected start time hour
                Text(
                    text = hour,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // start time hour selector icon and drop down
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (showStartTimeHourDropDown) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(16.dp)
                            .clickable {
                                showStartTimeHourDropDown = !showStartTimeHourDropDown
                            }
                    )
                    DropdownMenu(
                        expanded = showStartTimeHourDropDown,
                        onDismissRequest = {
                            showStartTimeHourDropDown = false
                        },
                        modifier = Modifier
                            .height(300.dp)
                            .align(Alignment.BottomCenter)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),

                        ) {
                        // if selected date from above row is equal to current date then h will start from
                        // current hour of the day
                        // else it will start from 0 and range till 23 i.e 0 to 23(both inclusive)
                        val h : Int = if (date.equals(LocalDate.now())) {
                            LocalTime.now().hour
                        } else 0
                        for (i in h until 24) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    hour = i.toString()
                                    // checking if the start time hour selected is greater than end time hour
                                    if (i > hourEndTime.toInt()) {
                                        // if selected start time minutes plus 30 does not go in next hour
                                        // then we will simply make endTime hour equal to start time hour
                                        // and increase the endtime minutes by 30
                                        if (minutes.toInt() + 30 < 60) {
                                            hourEndTime = i.toString()
                                            minutesEndTime = (minutes.toInt() + 30).toString()
                                        }
                                        // if selected start time minutes plus 30 goes in next hour
                                        // then we first check that endtime hour is 23 or not. if it is
                                        // in that case we have to make endTime hour to 0 and and make the
                                        // minutes equal to the value by which it was in next hour
                                        // if the endTime hour is not 23 than just increase the endTime hour
                                        // by one and make the minutes equal to the value by which it was
                                        // going in next hour
                                        else {
                                            hourEndTime = if (i + 1 < 24) {
                                                (i + 1).toString()
                                            } else 0.toString()
                                            minutesEndTime =
                                                (30 - (60 - minutes.toInt())).toString()
                                        }
                                    }
                                    showStartTimeHourDropDown = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                enabled = true
                            )
                        }
                    }
                }

                // start time selected minutes
                Text(
                    text = minutes,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // start time minutes selector icon and drop down
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (showStartTimeMinDropDown) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(16.dp)
                            .clickable {
                                showStartTimeMinDropDown = !showStartTimeMinDropDown
                            }
                    )
                    DropdownMenu(
                        expanded = showStartTimeMinDropDown,
                        onDismissRequest = {
                            showStartTimeMinDropDown = false
                        },
                        modifier = Modifier
                            .height(300.dp)
                            .align(Alignment.BottomCenter)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),

                        ) {
                        // if selected date is equal to current date and selected start time hour is also
                        // equal to the current time hour then m value will start current minute
                        // else it will start from 0
                        val m: Int =
                            if (date.equals(LocalDate.now()) && hour.toInt() == LocalTime.now().hour) {
                                LocalTime.now().minute
                            } else 0
                        for (i in m until 60) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    minutes = i.toString()
                                    if (i + 30 < 60 && hour == hourEndTime) {
                                        minutesEndTime = (i + 30).toString()
                                    } else if (hour == hourEndTime) {
                                        hourEndTime = (hour.toInt() + 1).toString()
                                        minutesEndTime = (30 - (60 - i)).toString()
                                    } else if (hourEndTime.toInt() == hour.toInt() + 1 && i + 30 >= 60) {
                                        if (minutesEndTime.toInt() < 30 - (60 - i)) {
                                            minutesEndTime = (30 - (60 - i)).toString()
                                        }
                                    }
                                    showStartTimeMinDropDown = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                enabled = true
                            )
                        }
                    }
                }
            }

            // End time text and time selecting drop down menus
            Row(
                modifier = Modifier.padding(top = 15.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showEndTimeHourDropDown by remember { mutableStateOf(false) }
                var showEndTimeMinDropDown by remember { mutableStateOf(false) }
                Text(
                    text = "End time",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge
                )

                // end time selected hour
                Text(
                    text = hourEndTime,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // end time hour selector and drop down
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (showEndTimeHourDropDown) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(16.dp)
                            .clickable {
                                showEndTimeHourDropDown = !showEndTimeHourDropDown
                            }
                    )
                    DropdownMenu(
                        expanded = showEndTimeHourDropDown,
                        onDismissRequest = {
                            showEndTimeHourDropDown = false
                        },
                        modifier = Modifier
                            .height(300.dp)
                            .align(Alignment.BottomCenter)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),

                        ) {
                        // endTime hour will start from selected start time hour but if start time minutes
                        // plus 30 goes in next hour then endTime hour will be increase by 1
                        var h = hour.toInt()
                        if (minutes.toInt() + 30 >= 60) h++
                        for (i in h until 24) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    hourEndTime = i.toString()
                                    showEndTimeHourDropDown = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                enabled = true
                            )
                        }
                    }
                }

                // endTime selected minutes
                Text(
                    text = minutesEndTime,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // endTime selector icon and minute drop down
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (showEndTimeMinDropDown) {
                            Icons.Filled.KeyboardArrowUp
                        } else {
                            Icons.Filled.KeyboardArrowDown
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(start = 7.dp)
                            .size(16.dp)
                            .clickable {
                                showEndTimeMinDropDown = !showEndTimeMinDropDown
                            }
                    )
                    DropdownMenu(
                        expanded = showEndTimeMinDropDown,
                        onDismissRequest = {
                            showEndTimeMinDropDown = false
                        },
                        modifier = Modifier
                            .height(300.dp)
                            .align(Alignment.BottomCenter)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant),

                        ) {
                        // if endTime hour is equal to start time hour then m will be
                        var m: Int = if (hour == hourEndTime) {
                            minutes.toInt() + 30
                        } else {
                            0
                        }
                        if (m >= 60) {
                            hour = (hour.toInt() + 1).toString()
                            m -= 60
                        }
                        for (i in m until 60) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                onClick = {
                                    minutesEndTime = i.toString()
                                    showEndTimeMinDropDown = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                enabled = true
                            )
                        }
                    }
                }
            }

            // Task type text and add button
            Row(
                modifier = Modifier
                    .padding(top = 15.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showCategoryDropDownMenu by remember {
                    mutableStateOf(false)
                }

                Text(
                    text = "Task types",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // task type add icon and drop down
                BoxWithConstraints {
                    var selectedType by remember {
                        mutableStateOf("")
                    }
                    Image(
                        painter = painterResource(id = R.drawable.ic_add_task),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .clickable {
                                if (taskCategoryListSelected.size == 4) {
                                    Toast
                                        .makeText(
                                            mContext,
                                            "Max 4 task types can be selected",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                } else showCategoryDropDownMenu = !showCategoryDropDownMenu
                            }
                            .align(Alignment.TopStart)
                    )
                    DropdownMenu(
                        expanded = showCategoryDropDownMenu,
                        onDismissRequest = {
                            showCategoryDropDownMenu = false
                        },
                        modifier = Modifier
                            .height(400.dp)
                            .align(Alignment.TopEnd)
                            .background(color = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        taskCategories.forEach { type ->
                            val tagEnabled = !taskCategoryListSelected.contains(type)
                            DropdownMenuItem(
                                onClick = {
                                    selectedType = type
                                    taskCategoryListSelected.add(type)
                                    showCategoryDropDownMenu = false
                                },
                                enabled = tagEnabled,
                            ) {
                                Text(
                                    text = type,
                                    style = if (tagEnabled) {
                                        MaterialTheme.typography.labelMedium
                                    } else {
                                        MaterialTheme.typography.bodySmall
                                    },
                                    color = if (tagEnabled) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else{
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
                                    }
                                )
                            }
                        }
                    }
                }

                // showing the text only if the tasks category selected is greater than 0
                if (taskCategoryListSelected.size > 0) {
                    Text(
                        text = "(long press to delete a task type)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .alpha(0.4f)
                    )
                }
            }

            // Task types row
            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until taskCategoryListSelected.size) {
                    var showTaskDeleteOpt by remember { mutableStateOf(false) }
                    val type = taskCategoryListSelected[i]
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = type,
                            style = TaskTrackerTopography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        showTaskDeleteOpt = !showTaskDeleteOpt
                                    }
                                )
                        )
                        DropdownMenu(
                            expanded = showTaskDeleteOpt,
                            onDismissRequest = { showTaskDeleteOpt = false },
                            modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            DropdownMenuItem(onClick = {
                                taskCategoryListSelected.remove(type)
                                showTaskDeleteOpt = false
                            }) {
                                Text(
                                    text = "Delete",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // launcher for getting the file uri and storing the file in the cache directory
            val fileLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { result ->
                    if (result != null) {
                        val cursor = mContext.contentResolver.query(result, null, null, null, null)
                        val indexedName = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        cursor.moveToFirst()
                        val filename = cursor.getString(indexedName)
                        cursor.close()
                        fileCache.cacheThis(listOf(result))
                        attachments.add(Pair(result.toString(), filename))
                        Toast.makeText(
                            mContext,
                            "attach size - ${attachments.size}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            // Attachments text and add button
            Row(
                modifier = Modifier
                    .padding(top = 15.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attachments",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_add_task),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .clickable {
                            if (attachments.size == 2) {
                                Toast
                                    .makeText(
                                        mContext,
                                        "Max 2 attachments can be selected",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            } else fileLauncher.launch("*/*")
                        }
                )
            }

            // Attachments file preview
            FilePreview(attachments) { pair ->
                attachments.remove(pair)
            }
        }
    }
}

@Composable
fun FilePreview(
    attachments: MutableList<Pair<String, String>>,
    onClick: (pair: Pair<String, String>) -> Unit
) {
    val mContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mutex = remember { Mutex() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp, end = 15.dp, top = 10.dp)
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            attachments.forEach {
                val uri = it.first.toUri()
                val name = it.second
                val file = remember(uri) { File(mContext.cacheDir, name) }
                val input = remember(file, uri) {
                    mContext.contentResolver.openFileDescriptor(
                        file.toUri(),
                        "r"
                    )
                }
                if (input != null) {
                    val renderer by produceState<PdfRenderer?>(null, input) {
                        coroutineScope.launch(Dispatchers.IO) {
                            value = PdfRenderer(input)
                        }
                        awaitDispose {
                            // on dispose we are closing the renderer with lock
                            val currentRenderer = value
                            coroutineScope.launch(Dispatchers.IO) {
                                mutex.withLock {
                                    currentRenderer?.close()
                                }
                            }
                        }
                    }
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 15.dp)
                            .border(
                                1.dp,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                                RoundedCornerShape(7.dp)
                            ),
                    ) {
                        val width = with(LocalDensity.current) { maxWidth.toPx() }.toInt()
                        val height = (width * 0.6f).toInt()
                        val bitmap = remember {
                            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        }
                        DisposableEffect(key1 = renderer) {
                            val job = coroutineScope.launch(Dispatchers.IO) {
                                mutex.withLock {
                                    if (!coroutineContext.isActive) return@launch
                                    try {
                                        renderer?.let { renderer->
                                            renderer.openPage(0).use { page ->
                                                page.render(
                                                    bitmap,
                                                    null,
                                                    null,
                                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                                )
                                            }
                                        }
                                    } catch (e: Exception) {
                                        return@launch
                                    }
                                }
                            }
                            onDispose {
                                job.cancel()
                            }
                        }

                        // file preview
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(7.dp)),
                            alignment = Alignment.Center
                        )

                        // row showing filename and opt menu
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(
                                    color = Color.Black.copy(0.4f),
                                    RoundedCornerShape(bottomStart = 7.dp, bottomEnd = 7.dp)
                                )
                                .align(Alignment.BottomCenter)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            var showDropDownMenu by remember {
                                mutableStateOf(false)
                            }
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.background,
                            )
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.menu__colon),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.background,
                                    modifier = Modifier
                                        .clickable {
                                            showDropDownMenu = !showDropDownMenu
                                        }
                                        .size(16.dp)
                                )
                                DropdownMenu(
                                    expanded = showDropDownMenu,
                                    onDismissRequest = { showDropDownMenu = false },
                                    modifier = Modifier.background(color = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            onClick(it)
                                            showDropDownMenu = false
                                        },
                                        contentPadding = MenuDefaults.DropdownMenuItemContentPadding,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        Text(
                                            text = "Delete",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
