package com.shubham.tasktrackerapp.dashboard

import android.content.res.Resources.getSystem
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import app.futured.donut.DonutStrokeCap
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.RoomViewModel
import com.shubham.tasktrackerapp.theme.Red200
import com.shubham.tasktrackerapp.theme.Red600
import com.shubham.tasktrackerapp.theme.Teal200
import com.shubham.tasktrackerapp.theme.Teal600
import com.shubham.tasktrackerapp.util.isLight
import com.shubham.tasktrackerapp.util.xAxisMonthly
import com.shubham.tasktrackerapp.util.xAxisWeekly
import java.time.LocalDate

const val TAG = "DashBoardFragment"
const val PG_MONTHLY = "Monthly"
const val PG_WEEKLY = "Weekly"

@Composable
fun DashBoard() {
    val roomViewModel = hiltViewModel<RoomViewModel>()
    var currentMonth = LocalDate.now().monthValue.toString()
    var currentDay= LocalDate.now().dayOfMonth.toString()
    if(currentMonth.length == 1) currentMonth = "0$currentMonth"
    if(currentDay.length == 1) currentDay = "0$currentDay"
    var btnWeekly by remember { mutableStateOf(true) }
    var btnMonthly by remember { mutableStateOf(false) }
    var btnBeforeDeadlinePG by remember { mutableStateOf(true) }
    var btnMissedPG by remember { mutableStateOf(false) }
    var btnBeforeDeadineCategories by remember { mutableStateOf(true) }
    var btnMissedCategories by remember { mutableStateOf(false) }
    var yaxisNum = remember { mutableStateListOf<Float>() }
    val xCoordList = remember { mutableStateListOf<Float>() }
    val yCoordList = remember { mutableStateListOf<Float>() }
    var tasksMissed by remember { mutableStateOf(0) }
    var tasksAccuracy by remember { mutableStateOf(0) }
    var beforeDeadline by remember { mutableStateOf(0) }
    var beforeDeadlinePercentage by remember { mutableStateOf(0) }

    if(btnWeekly){
        tasksMissed = 2
        tasksAccuracy = 80
        beforeDeadline = 8
        beforeDeadlinePercentage = 5
    }
    else if(btnMonthly){
        tasksMissed = 5
        tasksAccuracy = 65
        beforeDeadline = 22
        beforeDeadlinePercentage = 3
    }

    val recentMissedTaskList by roomViewModel.getLastWeekMissedTasks("${LocalDate.now().year}-$currentMonth-$currentDay").observeAsState()


    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .padding(15.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Recent Missed Tasks",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 30.dp)
            )
            if(recentMissedTaskList != null){
                LazyRow {
                    items(items = recentMissedTaskList!!, itemContent = {
                        BoxWithConstraints(
                            modifier = Modifier
                                .padding(top = 10.dp, end = 10.dp)
                                .width(180.dp)
                                .height(95.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = it.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                                Text(
                                    text = it.due_date.toString() + " " + it.start_time + '-' + it.end_time,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                                var string = ""
                                for (i in it.taskTypes) {
                                    string += (i + " ")
                                }
                                Text(
                                    text = string,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                            }
                        }
                    })
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .wrapContentHeight(),
            ) {
                Text(
                    text = "Weekly",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if(btnWeekly) {
                        MaterialTheme.colorScheme.onPrimary
                    }else {
                          MaterialTheme.colorScheme.onBackground
                    },
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .background(
                            color = if(btnWeekly){
                                MaterialTheme.colorScheme.primary
                            }else {
                                  MaterialTheme.colorScheme.surface
                            },
                            RoundedCornerShape(15.dp)
                        )
                        .clickable {
                            btnWeekly = true
                            btnMonthly = false
                        }
                        .padding(8.dp)
                        .weight(1f)
                )
                Text(
                    text = "Monthly",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if(btnMonthly){
                        MaterialTheme.colorScheme.onPrimary
                    }else {
                          MaterialTheme.colorScheme.onBackground
                    },
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .background(
                            color = if(btnMonthly){
                                MaterialTheme.colorScheme.primary
                            }else {
                                MaterialTheme.colorScheme.surface
                            },
                            RoundedCornerShape(15.dp)
                        )
                        .clickable {
                            btnMonthly = true
                            btnWeekly = false
                        }
                        .padding(8.dp)
                        .weight(1f)
                )
            }
            Column(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .verticalScroll(rememberScrollState())
                    .weight(1f, fill = false)
            ) {
                Text(
                    text = "Performance Graph",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Before Deadline",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if(btnBeforeDeadlinePG){
                            MaterialTheme.colorScheme.onPrimary
                        }else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .background(
                                color = if(btnBeforeDeadlinePG){
                                    MaterialTheme.colorScheme.primary
                                }else {
                                    MaterialTheme.colorScheme.surface
                                },
                                RoundedCornerShape(15.dp)
                            )
                            .clickable {
                                btnBeforeDeadlinePG = true
                                btnMissedPG = false
                            }
                            .padding(10.dp)
                    )
                    Text(
                        text = "Missed",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if(btnMissedPG){
                            MaterialTheme.colorScheme.onPrimary
                        }else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .background(
                                color = if(btnMissedPG){
                                    MaterialTheme.colorScheme.primary
                                }else {
                                    MaterialTheme.colorScheme.surface
                                },
                                RoundedCornerShape(15.dp)
                            )
                            .clickable {
                                btnMissedPG = true
                                btnBeforeDeadlinePG = false
                            }
                            .padding(10.dp)
                    )
                }
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp)
                        .wrapContentHeight()
                ) {
                    if(btnMonthly) {
                        val list  = calculateLocationsAndDrawTheCurve(
                            listNoOfMissedTasksForMonth,
                            PG_MONTHLY
                        )
                        yaxisNum.removeRange(0 , yaxisNum.size)
                        xCoordList.removeRange(0 , xCoordList.size)
                        yCoordList.removeRange(0 , yCoordList.size)

                        yaxisNum.addAll(list[0])
                        xCoordList.addAll(list[1])
                        yCoordList.addAll(list[2])

                    }else if(btnWeekly) {
                        val list = calculateLocationsAndDrawTheCurve(
                            listNoOfMissedTasksPerWeek,
                            PG_WEEKLY
                        )

                        yaxisNum.removeRange(0 , yaxisNum.size)
                        xCoordList.removeRange(0 , xCoordList.size)
                        yCoordList.removeRange(0 , yCoordList.size)

                        yaxisNum.addAll(list[0])
                        xCoordList.addAll(list[1])
                        yCoordList.addAll(list[2])
                    }

                    val (ycol, xrow, ivgraph, revealView) = createRefs()
                    Column(
                        modifier = Modifier.constrainAs(ycol) {
                            start.linkTo(parent.start)
                            top.linkTo(ivgraph.top)
                            bottom.linkTo(ivgraph.bottom)
                            height = Dimension.fillToConstraints
                        },
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (i in yaxisNum.size - 1 downTo 0) {
                            Text(
                                text = yaxisNum[i].toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    val isLight = MaterialTheme.colorScheme.isLight()
                    Canvas(
                        modifier = Modifier
                            .constrainAs(ivgraph) {
                                start.linkTo(ycol.end, 5.dp)
                                width = Dimension.value(320.dp)
                                height = Dimension.value(266.dp)
                            }
                            .padding(
                                top = 8.dp,
                                start = if(btnMonthly){
                                    7.dp
                                }else{
                                    10.dp
                                },
                                end = if(btnMonthly){
                                    7.dp
                                }else{
                                    10.dp
                                },
                                bottom = 8.dp
                            )
                    ) {

                        val strokePath = Path().let {
                            for (i in 0 until xCoordList.size) {
                                if (i == 0) {
                                    it.moveTo(xCoordList[i], yCoordList[i])
                                } else {
                                    it.lineTo(xCoordList[i], yCoordList[i])
                                }
                            }
                            //it.lineTo(this.size.width, this.size.height)
                            it
                        }
                        val path = Path().let {
                            for (i in 0 until xCoordList.size) {
                                if (i == 0) {
                                    it.moveTo(xCoordList[i], yCoordList[i])
                                } else {
                                    it.lineTo(xCoordList[i], yCoordList[i])
                                }
                            }
                            it.lineTo(this.size.width, this.size.height)
                            it.lineTo(0f, this.size.height)
                            it.close()
                            it
                        }
                        drawPath(
                            path = strokePath,
                            style = Stroke(3f),
                            brush = if (isLight) {
                                if(btnBeforeDeadlinePG){
                                    Brush.verticalGradient(listOf(Teal600, Teal600))
                                }else {
                                    Brush.verticalGradient(listOf(Red600 , Red600))
                                }
                            } else {
                                if(btnBeforeDeadlinePG){
                                    Brush.verticalGradient(listOf(Teal200, Teal200))
                                }else {
                                    Brush.verticalGradient(listOf(Red200 , Red200))
                                }
                            },
                        )
                        drawPath(
                            path = path,
                            brush = if (isLight) {
                                if(btnBeforeDeadlinePG){
                                    Brush.verticalGradient(listOf(Teal600, Transparent))
                                }else {
                                    Brush.verticalGradient(listOf(Red600 , Transparent))
                                }
                            } else {
                                if(btnBeforeDeadlinePG){
                                    Brush.verticalGradient(listOf(Teal200, Transparent))
                                }else {
                                    Brush.verticalGradient(listOf(Red200 , Transparent))
                                }
                            },
                            style = Fill,
                        )
                    }
                    val state = remember {
                        MutableTransitionState(false).apply {
                            targetState = true
                        }
                    }
                    RevealViewAnimation(
                        modifier = Modifier
                            .constrainAs(revealView) {
                                start.linkTo(ivgraph.start, 320.dp)
                                width = Dimension.value(320.dp)
                                height = Dimension.value(266.dp)
                            }
                    ) {
                        Text(
                            text = "",
                            modifier = Modifier
                                .background(color = MaterialTheme.colorScheme.background)
                        )
                    }
                    Row(
                        modifier = Modifier.constrainAs(xrow) {
                            start.linkTo(ivgraph.start)
                            end.linkTo(ivgraph.end)
                            top.linkTo(ivgraph.bottom, 10.dp)
                            width = Dimension.fillToConstraints
                        },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val xaxis: List<String>
                        if(btnWeekly){
                            xaxis = xAxisWeekly
                        }
                        else {
                            xaxis = xAxisMonthly
                        }
                        for (element in xaxis) {
                            Text(
                                text = element,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp)
                        .wrapContentHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(15.dp)
                            )
                            .height(120.dp)
                            .padding(10.dp)
                            .weight(1f)
                    ) {
                        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                            val (tvMissed, tvAcc, ivAcc) = createRefs()
                            Text(
                                text = "Missed - $tasksMissed",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.constrainAs(tvMissed) {
                                    start.linkTo(parent.start)
                                    top.linkTo(parent.top)
                                }
                            )
                            Text(
                                text = "$tasksAccuracy% Accuracy",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.constrainAs(tvAcc) {
                                    start.linkTo(parent.start)
                                    top.linkTo(tvMissed.bottom, 10.dp)
                                }
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_accuracy),
                                contentDescription = null,
                                modifier = Modifier
                                    .constrainAs(ivAcc) {
                                        end.linkTo(parent.end)
                                        bottom.linkTo(parent.bottom)
                                    }
                                    .size(40.dp)
                                    .alpha(0.4f)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiary,
                                RoundedCornerShape(15.dp)
                            )
                            .height(120.dp)
                            .padding(10.dp)
                            .weight(1f)
                    ) {
                        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                            val (tvBeforeDeadline, tvMore, ivClock) = createRefs()
                            Text(
                                text = "Before deadline - $beforeDeadline",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.constrainAs(tvBeforeDeadline) {
                                    start.linkTo(parent.start)
                                    top.linkTo(parent.top)
                                }
                            )
                            Text(
                                text = "$beforeDeadlinePercentage% more than last week",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.constrainAs(tvMore) {
                                    start.linkTo(parent.start)
                                    top.linkTo(tvBeforeDeadline.bottom, 10.dp)
                                }
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_before_deadline),
                                contentDescription = null,
                                modifier = Modifier
                                    .constrainAs(ivClock) {
                                        end.linkTo(parent.end)
                                        bottom.linkTo(parent.bottom)
                                    }
                                    .size(40.dp)
                                    .alpha(0.4f)
                            )
                        }
                    }
                }
                Text(
                    text = "Tasks by categories",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 30.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp)
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "Before Deadline",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if(btnBeforeDeadineCategories){
                            MaterialTheme.colorScheme.onPrimary
                        }else {
                            MaterialTheme.colorScheme.onBackground
                        },
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .background(
                                color = if(btnBeforeDeadineCategories){
                                    MaterialTheme.colorScheme.primary
                                }else {
                                    MaterialTheme.colorScheme.surface
                                },
                                RoundedCornerShape(15.dp)
                            )
                            .clickable {
                                btnBeforeDeadineCategories = true
                                btnMissedCategories = false
                            }
                            .padding(10.dp)
                    )
                    Text(
                        text = "Missed",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if(btnMissedCategories){
                            MaterialTheme.colorScheme.onPrimary
                        }else {
                              MaterialTheme.colorScheme.onBackground
                        },
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .background(
                                color = if(btnMissedCategories){
                                    MaterialTheme.colorScheme.primary
                                }else {
                                    MaterialTheme.colorScheme.surface
                                },
                                RoundedCornerShape(15.dp)
                            )
                            .clickable {
                                btnMissedCategories = true
                                btnBeforeDeadineCategories = false
                            }
                            .padding(10.dp)

                    )
                }
                ConstraintLayout(
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    val (donutView, colPrim, colSecon, colTer, colRed, colonBack, leg1, leg2, leg3, leg4, leg5) = createRefs()
                    AndroidView(
                        modifier = Modifier
                            .width(150.dp)
                            .height(150.dp)
                            .constrainAs(donutView) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                            },
                        factory = { context ->
                            val donutSections = listOf(
                                DonutSection(
                                    "Assignment",
                                    ContextCompat.getColor(context, R.color.primary),
                                    0.3f
                                ),
                                DonutSection(
                                    "Classes",
                                    ContextCompat.getColor(context, R.color.tertiary),
                                    0.2f
                                ),
                                DonutSection(
                                    "Hangout",
                                    ContextCompat.getColor(context, R.color.secondary),
                                    0.2f
                                ),
                                DonutSection(
                                    "Television",
                                    ContextCompat.getColor(context, R.color.onBackground),
                                    0.2f
                                ),
                                DonutSection(
                                    "Remainder",
                                    ContextCompat.getColor(context, R.color.error),
                                    0.1f
                                ),
                            )
                            DonutProgressView(context).apply {
                                bgLineColor = ContextCompat.getColor(context, R.color.light_grey)
                                gapAngleDegrees = 270F
                                gapWidthDegrees = 20F
                                strokeWidth = 32F
                                strokeCap = DonutStrokeCap.ROUND
                                animationDurationMs = 2000
                                submitData(donutSections)
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(color = MaterialTheme.colorScheme.primary)
                            .constrainAs(colPrim) {
                                start.linkTo(donutView.end, 80.dp)
                                top.linkTo(donutView.top, 15.dp)
                            }
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(color = MaterialTheme.colorScheme.tertiary)
                            .constrainAs(colSecon) {
                                start.linkTo(donutView.end, 80.dp)
                                top.linkTo(colPrim.bottom, 10.dp)
                            }
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(color = MaterialTheme.colorScheme.secondary)
                            .constrainAs(colTer) {
                                start.linkTo(donutView.end, 80.dp)
                                top.linkTo(colSecon.bottom, 10.dp)
                            }
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(color = MaterialTheme.colorScheme.onBackground)
                            .constrainAs(colonBack) {
                                start.linkTo(donutView.end, 80.dp)
                                top.linkTo(colTer.bottom, 10.dp)
                            }
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(color = MaterialTheme.colorScheme.error)
                            .constrainAs(colRed) {
                                start.linkTo(donutView.end, 80.dp)
                                top.linkTo(colonBack.bottom, 10.dp)
                            }
                    )
                    Text(
                        text = "Assignment",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(leg1) {
                            start.linkTo(colPrim.end, 15.dp)
                            top.linkTo(colPrim.top)
                            bottom.linkTo(colPrim.bottom)
                        }
                    )
                    Text(
                        text = "Classes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(leg2) {
                            start.linkTo(colSecon.end, 15.dp)
                            top.linkTo(colSecon.top)
                            bottom.linkTo(colSecon.bottom)
                        }
                    )
                    Text(
                        text = "Hangout",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(leg3) {
                            start.linkTo(colTer.end, 15.dp)
                            top.linkTo(colTer.top)
                            bottom.linkTo(colTer.bottom)
                        }
                    )
                    Text(
                        text = "Television",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(leg4) {
                            start.linkTo(colonBack.end, 15.dp)
                            top.linkTo(colonBack.top)
                            bottom.linkTo(colonBack.bottom)
                        }
                    )
                    Text(
                        text = "Remainder",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(leg5) {
                            start.linkTo(colRed.end, 15.dp)
                            top.linkTo(colRed.top)
                            bottom.linkTo(colRed.bottom)
                        }
                    )
                }
            }
        }
    }
}

fun calculateLocationsAndDrawTheCurve(
    missedTasks: MutableList<Int>,
    tag: String
): ArrayList<ArrayList<Float>> {

    val coordinates = ArrayList<ArrayList<Float>>()

    var maxMissed = Int.MIN_VALUE
    var lowestMissed = Int.MAX_VALUE
    for (i in missedTasks) {
        maxMissed = Math.max(maxMissed, i)
        lowestMissed = Math.min(lowestMissed, i)
    }
    val yAxisGap = Math.ceil((maxMissed - lowestMissed).toDouble() / 5).toInt()
    val yAxisNoList = arrayListOf<Float>()
    var tasksMissed = lowestMissed

    //setting the values of yaxis of the graph to the appropriate range
    for (i in 0 until 6) {
        yAxisNoList.add(tasksMissed * 1f)
        tasksMissed += yAxisGap
    }
    coordinates.add(yAxisNoList)

    val widthPx = 300.toPx * 1f
    val heightPx = 250.toPx * 1f

    val xCordList = arrayListOf<Float>()
    val yCordList = arrayListOf<Float>()

    var x = if (tag == PG_MONTHLY) {
        0.toPx * 1f
    } else {
        -(50.toPx * 1f)
    }
    for (i in 0 until missedTasks.size) {
        var low = 0f
        var high = 0f
        val num = missedTasks[i] * 1f
        var y = 0F
        if (tag == PG_MONTHLY) {
            x += (10.2.toFloat().toPx * 1f)
        } else {
            x += (50.toPx * 1f)
        }
        //for loop for finding the lower bound and upperbound
        for (j in 0 until yAxisNoList.size) {
            val vBar = yAxisNoList[j]
            if (vBar >= num) {
                high = vBar
                break
            }
            if (j != 0) y += (50.toPx) * 1F
            low = vBar
        }
        if (high != num) {
            y = y + ((50.toPx / ((high - low).toInt().toPx) * ((num - low).toInt()).toPx)) * 1f
        }
        // heightPx - y because we have calculated y from bottom and we want the distance from the top
        xCordList.add(x)
        yCordList.add(heightPx - y)
    }

    coordinates.add(xCordList)
    coordinates.add(yCordList)

    return coordinates
}


val listNoOfMissedTasksForMonth = mutableListOf(
    5, 7, 15, 10, 12, 4, 18, 12, 16, 16, 19, 18, 6, 7, 9, 12, 4, 3, 6, 9,
    10, 8, 10, 18, 2, 3, 4, 6, 6, 17
)

val listNoOfMissedTasksPerWeek = mutableListOf(9, 7, 15, 10, 12, 4, 18)

val Int.toDp: Int get() = (this / getSystem().displayMetrics.density).toInt()
val Int.toPx: Int get() = (this * getSystem().displayMetrics.density).toInt()
val Float.toPx: Float get() = (this * getSystem().displayMetrics.density).toFloat()

@Composable
fun RevealViewAnimation(
    modifier: Modifier,
    composable: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    val state = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    val density = LocalDensity.current
    AnimatedVisibility(
        visibleState = state,
        enter = slideInHorizontally(
            initialOffsetX = { with(density) { -320.dp.roundToPx() } },
            animationSpec = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        ),
        exit = slideOutVertically(
            animationSpec = tween(
                durationMillis = 3000,
                easing = LinearEasing
            )
        ),
        content = composable,
        modifier = modifier
    )
}