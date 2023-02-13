package com.shubham.tasktrackerapp

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MenuDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shubham.tasktrackerapp.data.local.Task
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
import kotlin.math.max
import kotlin.math.min

const val TAG = "EditTaskScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditTaskScreen(taskId : String, navController: NavController) {
    val roomViewModel = hiltViewModel<RoomViewModel>()
    val task = roomViewModel.getTaskById(taskId.toInt()).observeAsState()
    val mContext = LocalContext.current
    val fileCache = remember {
        FileCache(mContext)
    }

    if(task.value!=null){
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
                taskTitle.equals("")
            }
        }

        val date = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        // launched effect because we don't want to recalculate hour, min otherwise they will not be
        // updated when selected
        LaunchedEffect(key1 = Unit, block = {
            if(date.equals(LocalDate.now())){
                hour = LocalTime.now().hour.toString()
                minutes = LocalTime.now().minute.toString()
                if(LocalTime.now().minute + 30 <60){
                    hourEndTime = hour
                    minutesEndTime = (minutes.toInt()+30).toString()
                }
                else {
                    hourEndTime = (hour.toInt() + 1).toString()
                    minutesEndTime = (30 - (60 - minutes.toInt())).toString()
                }
            }
        })

        Column(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            // headline and save button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Edit Task Details",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Button(
                    onClick = {
                        val startTime = LocalTime.of(hour.toInt(), minutes.toInt())
                        val endTime = LocalTime.of(hourEndTime.toInt(), minutesEndTime.toInt())

                        if(showTitleError){
                            Toast.makeText(mContext, "Title required" , Toast.LENGTH_SHORT).show()
                        }
                        else if(taskCategoryListSelected.size == 0){
                            Toast.makeText(mContext, "Select atleast one task type", Toast.LENGTH_SHORT).show()
                        }
                        else if(startTime.equals(endTime) && startTime.isAfter(endTime)){
                            Toast.makeText(mContext , "Invalid time interval", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            val hashMap = hashMapOf<String , String>()
                            attachments.forEach {
                                hashMap[it.first] = it.second
                            }
                            task.value!!.title = taskTitle
                            task.value!!.due_date = LocalDate.of(year.toInt(), month.toInt(), day.toInt())
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
            if(showTitleError){
                Text(
                    text = "* Title is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Due date and text field
            Row(
                modifier = Modifier.padding(top = 15.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showYearDropDown by remember{ mutableStateOf(false)}
                var showMonthDropDown by remember{ mutableStateOf(false)}
                var showDayDropDown by remember{ mutableStateOf(false)}
                Text(
                    text = "Due date",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = year,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(contentAlignment = Alignment.Center){
                    Icon(
                        imageVector = if(showYearDropDown){
                            Icons.Filled.KeyboardArrowUp
                        }else {
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
                            .align(Alignment.BottomCenter),

                    ) {
                        for(i in LocalDate.now().year until LocalDate.now().year+25){
                            DropdownMenuItem(
                                text = {
                                   Text(
                                       text = i.toString(),
                                       style = MaterialTheme.typography.bodySmall
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
                Text(
                    text = month,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(contentAlignment = Alignment.Center){
                    Icon(
                        imageVector = if(showMonthDropDown){
                            Icons.Filled.KeyboardArrowUp
                        }else {
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
                            .align(Alignment.BottomCenter),

                        ) {
                        var m = LocalDate.now().monthValue
                        if(!year.toInt().equals(LocalDate.now().year)){
                            m = 1
                        }
                        for(i in m until 13){
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall
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
                Text(
                    text = day,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(contentAlignment = Alignment.Center){
                    Icon(
                        imageVector = if(showDayDropDown){
                            Icons.Filled.KeyboardArrowUp
                        }else {
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
                            .align(Alignment.BottomCenter),

                        ) {
                        var d = LocalDate.now().dayOfMonth
                        if(!month.toInt().equals(LocalDate.now().monthValue)){
                            d = 1
                        }
                        for(i in d until LocalDate.of(year.toInt() , month.toInt(), 1 ).lengthOfMonth() + 1){
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall
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

            // Start time text and text field
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
                Text(
                    text = hour,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(contentAlignment = Alignment.Center){
                    Icon(
                        imageVector = if(showStartTimeHourDropDown){
                            Icons.Filled.KeyboardArrowUp
                        }else {
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
                            .align(Alignment.BottomCenter),

                        ) {
                        var h = 0
                        if(date.equals(LocalDate.now())) {
                            h = LocalTime.now().hour
                        }else h = 0
                        for(i in h until 24){
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                onClick = {
                                    hour = i.toString()
                                    if(i > hourEndTime.toInt()) {
                                        if(minutes.toInt() + 30 < 60){
                                            hourEndTime = i.toString()
                                            minutesEndTime = (minutes.toInt() + 30).toString()
                                        }else {
                                            if(i+1 < 24){
                                                hourEndTime = (i+1).toString()
                                            }else hourEndTime = 0.toString()
                                            minutesEndTime = (30 - (60 - minutes.toInt())).toString()
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
                Text(
                    text = minutes,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(contentAlignment = Alignment.Center){
                    Icon(
                        imageVector = if(showStartTimeMinDropDown){
                            Icons.Filled.KeyboardArrowUp
                        }else {
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
                            .align(Alignment.BottomCenter),

                        ) {
                        val m: Int
                        m = if(date.equals(LocalDate.now()) && hour.toInt() == LocalTime.now().hour){
                            LocalTime.now().minute
                        }else 0
                        for(i in m until 60){
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                onClick = {
                                    minutes = i.toString()
                                    if(i + 30 < 60 && hour.equals(hourEndTime)){
                                        minutesEndTime = (i+30).toString()
                                    }else if(hour.equals(hourEndTime)){
                                        hourEndTime = (hour.toInt() + 1).toString()
                                        minutesEndTime = (30 - (60 - i)).toString()
                                    }
                                    else if(hourEndTime.toInt() == hour.toInt() + 1 && i+30 >=60){
                                        if(minutesEndTime.toInt() < 30 - (60 - i)){
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

            // End time text and text field
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
                Text(
                    text = hourEndTime,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(contentAlignment = Alignment.Center){
                    Icon(
                        imageVector = if(showEndTimeHourDropDown){
                            Icons.Filled.KeyboardArrowUp
                        }else {
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
                            .align(Alignment.BottomCenter),

                        ) {
                        var h = hour.toInt()
                        if(minutes.toInt() + 30 >=60) h++
                        for(i in h until 24){
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall
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
                Text(
                    text = minutesEndTime,
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(7.dp))
                        .padding(6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(contentAlignment = Alignment.Center){
                    Icon(
                        imageVector = if(showEndTimeMinDropDown){
                            Icons.Filled.KeyboardArrowUp
                        }else {
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
                            .align(Alignment.BottomCenter),

                        ) {
                        val m : Int = if(hour.equals(hourEndTime)){
                            minutes.toInt()+30
                        }else {
                            30 - (60 - minutes.toInt())
                        }
                        for(i in max(m, 0) until 60){
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = i.toString(),
                                        style = MaterialTheme.typography.bodySmall
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
            ){
                var showCategoryDropDownMenu by remember{
                    mutableStateOf(false)
                }

                Text(
                    text = "Task types",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                BoxWithConstraints{
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
                    ) {
                        taskCategories.forEach { type ->
                            val tagEnabled = !taskCategoryListSelected.contains(type)
                            DropdownMenuItem(
                                onClick = {
                                    selectedType = type
                                    taskCategoryListSelected.add(type)
                                    showCategoryDropDownMenu = false
                                },
                            enabled = tagEnabled) {
                                Text(
                                    text = type,
                                    style = if(tagEnabled){
                                        MaterialTheme.typography.labelMedium
                                    }else {
                                        MaterialTheme.typography.bodySmall
                                    },
                                    color = if(tagEnabled){
                                        MaterialTheme.colorScheme.onSurface
                                    }else {
                                        MaterialTheme.colorScheme.onSurface.copy(0.4f)
                                    }
                                )
                            }
                        }
                    }
                }
                if(taskCategoryListSelected.size > 0){
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
            ){
                for(i in 0 until taskCategoryListSelected.size){
                    var showTaskDeleteOpt by remember{ mutableStateOf(false)}
                    val type = taskCategoryListSelected[i]
                    Box(contentAlignment = Alignment.Center){
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
                            onDismissRequest = { showTaskDeleteOpt = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                taskCategoryListSelected.remove(type)
                                showTaskDeleteOpt = false
                            }) {
                                Text(
                                    text = "Delete",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }

            // launcher for getting the file uri and storing the file in the cache directory
            val fileLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { result ->
                if(result!=null) {
                    val cursor = mContext.contentResolver.query(result, null, null, null, null)
                    val indexedName = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val filename = cursor.getString(indexedName)
                    cursor.close()
                    fileCache.cacheThis(listOf(result))
                    attachments.add(Pair(result.toString(), filename))
                    Toast.makeText(mContext, "attach size - ${attachments.size}", Toast.LENGTH_SHORT).show()
                }
            }

            // Attachments text and add button
            Row(
                modifier = Modifier
                    .padding(top = 15.dp)
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
            ){
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
fun FilePreview(attachments: MutableList<Pair<String, String>> , onClick: (pair: Pair<String, String>) -> Unit) {
    val mContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mutex = remember{ Mutex() }
    val roomViewModel = hiltViewModel<RoomViewModel>()

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
                val file = remember(uri){ File(mContext.cacheDir, name) }
                val input = remember(file, uri){ mContext.contentResolver.openFileDescriptor(file.toUri(), "r") }
                if(input!=null){
                    val renderer by produceState<PdfRenderer?>(null , input){
                        coroutineScope.launch(Dispatchers.IO) {
                            value = PdfRenderer(input)
                        }
                        awaitDispose {
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
                    ){
                        val width = with(LocalDensity.current){ maxWidth.toPx()}.toInt()
                        val height = (width * 0.6f).toInt()
                        val bitmap = remember {
                            Bitmap.createBitmap(width , height , Bitmap.Config.ARGB_8888)
                        }
                        DisposableEffect(key1 = renderer){
                            val job = coroutineScope.launch(Dispatchers.IO) {
                                mutex.withLock {
                                    if(!coroutineContext.isActive) return@launch
                                    try{
                                        renderer?.let {
                                            it.openPage(0).use { page ->
                                                page.render(
                                                    bitmap,
                                                    null,
                                                    null,
                                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                                )
                                            }
                                        }
                                    }catch (e: Exception){
                                        return@launch
                                    }
                                }
                            }
                            onDispose {
                                job.cancel()
                            }
                        }
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            alignment = Alignment.Center
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onBackground.copy(0.4f),
                                    RoundedCornerShape(bottomStart = 7.dp, bottomEnd = 7.dp)
                                )
                                .align(Alignment.BottomCenter)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ){
                            var showDropDownMenu by remember {
                                mutableStateOf(false)
                            }
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.background,
                            )
                            Box(contentAlignment = Alignment.Center){
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
                                    onDismissRequest = { showDropDownMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            onClick(it)
                                            //Toast.makeText(mContext, "attach file prev size - ${attachments.size}", Toast.LENGTH_SHORT).show()
                                            showDropDownMenu = false
                                        },
                                        contentPadding = MenuDefaults.DropdownMenuItemContentPadding,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        Text(
                                            text = "Delete",
                                            color = MaterialTheme.colorScheme.onBackground,
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
