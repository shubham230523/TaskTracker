package com.shubham.tasktrackerapp.newtask

import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.RoomViewModel
import com.shubham.tasktrackerapp.data.local.*
import com.shubham.tasktrackerapp.theme.RobotoFontFamily
import com.shubham.tasktrackerapp.theme.TaskTrackerTheme
import com.shubham.tasktrackerapp.theme.TaskTrackerTopography
import com.shubham.tasktrackerapp.util.taskCategories
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.min

private const val TAG = "NewTaskFragment"

/**
 * Composable to create new task
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewTask(navController: NavController) {
    val bgColor = MaterialTheme.colorScheme.onBackground.copy(0.4f)
    val applyBgColor = remember { mutableStateOf(false) }
    val showPopUpMenu = remember { mutableStateOf(false)}
    val x1PopUpMenu = remember { mutableStateOf(0f)}
    val x2PopUpMenu = remember { mutableStateOf(0f)}
    val y1PopUpMenu = remember { mutableStateOf(0f)}
    val y2PopUpMenu = remember { mutableStateOf(0f)}
    val roomViewModel = hiltViewModel<RoomViewModel>()
    val taskTypeList = remember{ mutableStateListOf<String>() }
    val uriList = remember { mutableStateListOf<String>() }
    val nameList = remember { mutableStateListOf<String>() }
    var boxX1Coordinate by remember {mutableStateOf(0f)}
    var boxX2Coordinate by remember {mutableStateOf(0f)}
    var boxY1Coordinate by remember {mutableStateOf(0f)}
    var boxY2Coordinate by remember {mutableStateOf(0f)}

    val mContext = LocalContext.current

    BoxWithConstraints(modifier = Modifier
        .fillMaxSize()
        .background(
            color = if (applyBgColor.value) {
                bgColor
            } else {
                Transparent
            }
        )
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                if (showPopUpMenu.value) {
                    if (it.x < x1PopUpMenu.value || it.x > x2PopUpMenu.value || it.y < y1PopUpMenu.value || it.y > y2PopUpMenu.value) {
                        showPopUpMenu.value = false
                        applyBgColor.value = false
                    }
                }
            })
        }
        .onGloballyPositioned { coordinates ->
            Log.d(
                TAG,
                "width is ${coordinates.size.width} and height is ${coordinates.size.height}"
            )
//            boxX1Coordinate = coordinates.size.width/2-100 * 1f
//            boxX2Coordinate = coordinates.size.width/2 + 100 * 1f
//            boxY1Coordinate = coordinates.size.height/2-200 * 1f
//            boxY2Coordinate = coordinates.size.height/2+200 * 1f
            boxX1Coordinate = coordinates.size.width / 4 * 1f
            boxX2Coordinate = boxX1Coordinate + coordinates.size.width / 2 * 1f
            boxY1Coordinate = 0f
            boxY2Coordinate = coordinates.size.height * 1f
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomStart)
                .background(
                    color = if (applyBgColor.value) {
                        bgColor
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp)
                )
                .padding(top = 20.dp, start = 15.dp, end = 15.dp, bottom = 15.dp),
        ) {
            var taskTitle by rememberSaveable { mutableStateOf("") }
            var dueDate by remember { mutableStateOf(LocalDate.now()) }
            var startTime by remember { mutableStateOf(LocalTime.now()) }
            var endTime by remember { mutableStateOf(LocalTime.now()) }
            val dateDialogState = rememberMaterialDialogState()
            val timeDialogState = rememberMaterialDialogState()
            val endTimeDialogState = rememberMaterialDialogState()
            val hmAttachments = hashMapOf<String , String>()
            val coroutineScope = rememberCoroutineScope()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Create a new task",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector  = Icons.Filled.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
            }
            OutlinedTextField(
                value = taskTitle,
                textStyle = MaterialTheme.typography.bodyMedium,
                onValueChange = {
                    taskTitle = it
                },
                label = { Text(
                    "Title",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                ) },
                placeholder = {
                    Text(
                        text = "Task title ",
                        modifier = Modifier.alpha(0.4f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp)
                    .height(60.dp),

                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    cursorColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    placeholderColor = MaterialTheme.colorScheme.onSurface,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
            )
            MaterialDialog(
                backgroundColor = MaterialTheme.colorScheme.surface,
                dialogState = dateDialogState,
                buttons = {
                    positiveButton(
                        text = "OK",
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = FontStyle.Normal,
                            fontFamily = RobotoFontFamily,
                            letterSpacing = 2.sp
                        ),
                    )
                    negativeButton(
                        "Cancel",
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = FontStyle.Normal,
                            fontFamily = RobotoFontFamily,
                            letterSpacing = 2.sp
                        ),
                    )
                },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
            ) {
                datepicker(
                    initialDate = LocalDate.now(),
                    title = "Pick a date",
                    colors = DatePickerDefaults.colors(
                        headerBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        headerTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        dateActiveBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                        dateActiveTextColor = MaterialTheme.colorScheme.onSurface,
                        calendarHeaderTextColor = MaterialTheme.colorScheme.onSurface,
                        dateInactiveTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    allowedDateValidator = {
                        it > LocalDate.now()
                    },
                ) { date ->
                    dueDate = date
                }
            }
            MaterialDialog(
                backgroundColor = MaterialTheme.colorScheme.surface,
                dialogState = timeDialogState,
                buttons = {
                    positiveButton(
                        text = "Ok",
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = FontStyle.Normal,
                            fontFamily = RobotoFontFamily,
                            letterSpacing = 2.sp
                        )
                    )
                    negativeButton(
                        "Cancel",
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = FontStyle.Normal,
                            fontFamily = RobotoFontFamily,
                            letterSpacing = 2.sp
                        )
                    )
                },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),
            ) {
                timepicker(
                    initialTime = LocalTime.now(),
                    title = "Select a time",
                    is24HourClock = false,
                    colors = TimePickerDefaults.colors(
                        activeBackgroundColor = MaterialTheme.colorScheme.primary,
                        inactiveBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        activeTextColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectorColor = MaterialTheme.colorScheme.secondary,
                        selectorTextColor = MaterialTheme.colorScheme.onSecondary,
                        headerTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    timeRange = if(dueDate.equals(LocalDate.now())) {
                        LocalTime.now()..LocalTime.of(23 , 29)
                    }else {
                        LocalTime.MIDNIGHT .. LocalTime.of(23 , 29)
                    }
                ){
                    startTime = it
                }
            }
            MaterialDialog(
                backgroundColor = MaterialTheme.colorScheme.surface,
                dialogState = endTimeDialogState,
                buttons = {
                    positiveButton(
                        text = "Ok",
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = FontStyle.Normal,
                            fontFamily = RobotoFontFamily,
                            letterSpacing = 2.sp
                        ),
                    )
                    negativeButton(
                        "Cancel",
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = FontStyle.Normal,
                            fontFamily = RobotoFontFamily,
                            letterSpacing = 2.sp
                        ),
                    )
                },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                ),

            ) {
                timepicker(
                    initialTime = LocalTime.now(),
                    title = "Select a time",
                    is24HourClock = false,
                    colors = TimePickerDefaults.colors(
                        activeBackgroundColor = MaterialTheme.colorScheme.primary,
                        inactiveBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        activeTextColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectorColor = MaterialTheme.colorScheme.secondary,
                        selectorTextColor = MaterialTheme.colorScheme.onSecondary,
                        headerTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    timeRange = startTime.plusMinutes(30) .. LocalTime.of(23 , 59)
                ){
                    endTime = it
                }
            }
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 20.dp)
            ){
                Text(
                    text = "Due date",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(80.dp)
                )
                Image(
                    painterResource(id = R.drawable.ic_calendar),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp, end = 9.dp)
                        .size(18.dp)
                        .alpha(0.4f)
                        .clickable { dateDialogState.show() },
                )
                Text(
                    text = dueDate.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { dateDialogState.show() }
                )
            }
            Row (modifier = Modifier.padding(top = 10.dp)){
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(80.dp)
                )
                Image(
                    painterResource(id = R.drawable.ic_clock),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp, end = 7.dp)
                        .size(20.dp)
                        .alpha(0.4f)
                        .clickable { timeDialogState.show() }
                )
                var startTimeMin = startTime.minute.toString()
                var startTimeHour = startTime.hour.toString()
                if(startTimeMin.length == 1) startTimeMin = "0$startTimeMin"
                if(startTimeHour.length == 1) startTimeHour = "0$startTimeHour"
                Text(
                    text = "$startTimeHour:$startTimeMin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable {
                        timeDialogState.show()
                    }
                )
            }
            Row (modifier = Modifier.padding(top = 10.dp)){
                Text(
                    text = "End",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(80.dp)
                )
                Image(
                    painterResource(id = R.drawable.ic_clock),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp, end = 7.dp)
                        .size(20.dp)
                        .alpha(0.4f)
                )
                var endTimeMin = endTime.minute.toString()
                var endTimeHour = endTime.hour.toString()
                if(endTimeMin.length == 1) endTimeMin = "0$endTimeMin"
                if(endTimeHour.length == 1) endTimeHour = "0$endTimeHour"
                Text(
                    text = "$endTimeHour:$endTimeMin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { endTimeDialogState.show() }
                )
            }
            Row (modifier = Modifier.padding(top = 20.dp)){
                Text(
                    text = "Task Type",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Image(
                    painterResource(id = R.drawable.ic_add_task),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .size(24.dp)
                        .clickable {
                            Log.d("NewTaskFragment", "new task .click called")
                            showPopUpMenu.value = true
                        }
                )
            }
            Column(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .height(105.dp)
                    .background(
                        color = if (applyBgColor.value) {
                            bgColor
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(10.dp),
                    )
                    .padding(15.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(taskTypeList.size == 0){
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 12.5.dp)
                            .size(50.dp)
                            .alpha(0.2f)
                    )
                }
                else {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        for(i in 0 until min(3 , taskTypeList.size)){
                            var showDropDownMenu by remember{ mutableStateOf(false)}
                            Box(contentAlignment = Alignment.Center){
                                Text(
                                    text = taskTypeList[i],
                                    style = TaskTrackerTopography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                        .combinedClickable(
                                            onLongClick = { showDropDownMenu = true },
                                            onClick = { /*....*/ })
                                )
                                DropdownMenu(
                                    expanded = showDropDownMenu,
                                    onDismissRequest = { showDropDownMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            taskTypeList.removeAt(i)
                                            showDropDownMenu = false
                                        },
                                        enabled = true,
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
                    if(taskTypeList.size == 4){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            var showDropDownMenu by remember{ mutableStateOf(false)}
                            Box(contentAlignment = Alignment.Center){
                                Text(
                                    text = taskTypeList[3],
                                    style = TaskTrackerTopography.labelMedium,
                                    color = MaterialTheme.colorScheme.onTertiary,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .background(
                                            MaterialTheme.colorScheme.tertiary,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                        .combinedClickable(
                                            onLongClick = { showDropDownMenu = true },
                                            onClick = { /*....*/ })
                                )
                                DropdownMenu(
                                    expanded = showDropDownMenu,
                                    onDismissRequest = { showDropDownMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        onClick = {
                                            taskTypeList.removeAt(3)
                                            showDropDownMenu = false
                                        },
                                        enabled = true,
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
            val fileLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { result ->
                if(result!=null) {
                    val cursor = mContext.contentResolver.query(result, null, null, null, null)
                    val indexedName = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    val filename = cursor.getString(indexedName)
                    cursor.close()
                    uriList.add(result.toString())
                    nameList.add(filename.toString())
                }
            }
            Row (modifier = Modifier.padding(top = 20.dp)){
                Text(
                    text = "No Attachments",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Image(
                    painterResource(id = R.drawable.ic_add_task),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .size(24.dp)
                        .clickable {
                            if (uriList.size < 2) {
                                fileLauncher.launch("*/*")
                            } else {
                                Toast
                                    .makeText(
                                        mContext,
                                        "You can select max 2 attachments",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        }
                )
            }
            Column(
                modifier = if(uriList.size == 0) {
                    Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            color = if (applyBgColor.value) {
                                bgColor
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            0.5.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            RoundedCornerShape(10.dp),
                        )
                }else {
                    Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .height(100.dp)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if(uriList.size == 0){
                    Image(
                        painter = painterResource(R.drawable.arrow_circle_up),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 25.dp)
                            .size(50.dp)
                            .alpha(0.2f)
                    )
                }
                else {
                    for(i in 0 until nameList.size){
                        Log.d(TAG , "nameList size is ${nameList.size}")
                        val name = nameList[i]
                        val uri = uriList[i]
                        ConstraintLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (applyBgColor.value) {
                                        bgColor
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.2f
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            val (fileType , fileName , close) = createRefs()
                            Image(
                                painterResource(id = R.drawable.pdf),
                                contentDescription = null,
                                modifier = Modifier
                                    .constrainAs(fileType) {
                                        top.linkTo(parent.top, 10.dp)
                                        bottom.linkTo(parent.bottom, 10.dp)
                                        start.linkTo(parent.start, 10.dp)
                                    }
                                    .width(16.dp)
                            )
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines  = 1,
                                color =  MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .constrainAs(fileName){
                                        top.linkTo(fileType.top)
                                        bottom.linkTo(fileType.bottom)
                                        start.linkTo(fileType.end , 7.dp)
                                        end.linkTo(close.start , 7.dp)
                                        width = Dimension.fillToConstraints
                                    }
                            )
                            Icon(
                                Icons.Filled.Close,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                contentDescription = null,
                                modifier = Modifier
                                    .clickable {
                                        uriList.remove(uri)
                                        nameList.remove(name)
                                    }
                                    .constrainAs(close) {
                                        top.linkTo(fileName.top)
                                        bottom.linkTo(fileName.bottom)
                                        end.linkTo(parent.end, 10.dp)
                                        width = Dimension.value(16.dp)
                                    }
                            )
                        }
                        Spacer(
                            modifier = if (i == 1) {
                                Modifier.height(0.dp)
                            }else {
                                Modifier.height(15.dp)
                            }
                        )
                    }
                }
            }
            Button(
                onClick = {
                    if(taskTitle == ""){
                        Toast.makeText(mContext , "Please enter a title" , Toast.LENGTH_SHORT).show()
                    }
                    else if(taskTypeList.size == 0) {
                        Toast.makeText(mContext , "Please select one task category", Toast.LENGTH_SHORT).show()
                    }
                    else if(startTime.hour == endTime.hour && startTime.minute == endTime.minute){
                        Toast.makeText(mContext , "Task start and end time are same" , Toast.LENGTH_SHORT).show()
                    }
                    else if(startTime.isAfter(endTime)){
                        Toast.makeText(mContext , "Time interval is incorrect" , Toast.LENGTH_SHORT).show()
                    }
                    else {
                        for(i in 0 until uriList.size){
                            hmAttachments.put(uriList[i] , nameList[i])
                        }
                        val task = Task(
                            title = taskTitle,
                            added_date = LocalDate.now(),
                            due_date = dueDate,
                            start_time = startTime,
                            end_time = endTime,
                            taskTypes = taskTypeList as MutableList<String>,
                            attachments = hmAttachments
                        )
                        coroutineScope.launch {
                            roomViewModel.insertIntoDatabase(task)
                        }
                        navController.popBackStack()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if(applyBgColor.value){
                        bgColor
                    }else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Text(
                    text = "Create task",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        if(showPopUpMenu.value){
            TaskTypePopUpMenu(
                modifier = Modifier
                    .align(Alignment.Center)
                    .onGloballyPositioned { layoutCoordinates ->
                        x1PopUpMenu.value = layoutCoordinates.positionInWindow().x
                        y1PopUpMenu.value = layoutCoordinates.positionInWindow().y
                        x2PopUpMenu.value = x1PopUpMenu.value + layoutCoordinates.size.width
                        y2PopUpMenu.value =
                            y1PopUpMenu.value + layoutCoordinates.size.height
                    },
                taskTypeList.size
            ) {
                it.forEach {type ->
                    taskTypeList.add(type)
                }
                applyBgColor.value = false
                showPopUpMenu.value = false
            }
            applyBgColor.value = true
        }
    }
}

@Composable
fun TaskTypePopUpMenu(modifier: (Modifier), tasksSelected : Int , onClick: (List<String>) -> Unit) {
    val state = MutableTransitionState(false).apply {
        targetState = true
    }
    val taskTypeList = mutableListOf<String>()
    AnimatedVisibility(
        modifier = modifier,
        visibleState = state,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = modifier
                .width(300.dp)
                .height(400.dp)
                .background(color = MaterialTheme.colorScheme.surfaceVariant)
                .padding(15.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val selectedTaskCount = remember { mutableStateOf(tasksSelected)}
            val showMaxCountWarning by remember {
                derivedStateOf {
                    selectedTaskCount.value == 4
                }
            }
            Text(
                text = "${selectedTaskCount.value} task type selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if(showMaxCountWarning){
                Text(
                    text = "Max limit is 4",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp)
                    .height(300.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
            ) {
                for(i in 0 until 13){
                    val checkedState = remember{ mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(
                            checked = checkedState.value,
                            onCheckedChange = {
                                if(!it){
                                    checkedState.value = false
                                    selectedTaskCount.value--
                                    taskTypeList.remove(taskCategories[i])
                                }
                                if(it && selectedTaskCount.value < 4){
                                    checkedState.value = true
                                    selectedTaskCount.value++
                                    taskTypeList.add(taskCategories[i])
                                }
                            }
                        )
                        Text(
                            text = taskCategories[i],
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }
            Button(
                onClick = { onClick(taskTypeList) },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "Select",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

class RectangularShape(val x1: Float , val y1: Float , val x2: Float, val y2: Float): Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        Log.d(TAG , "x1 - $x1 y1 - $y1 x2 - $x2 y2 - $y2")
         return Outline.Generic(
             Path().apply{
                 moveTo(x1 , y1)
                 lineTo(x2 , y1)
                 lineTo(x2 , y2)
                 lineTo(x1 , y2)
                 close()
             }
         )
    }

}