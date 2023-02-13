package com.shubham.tasktrackerapp

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.pager.*
import com.shubham.tasktrackerapp.allupcommingtasks.AllUpcomingTasksList
import com.shubham.tasktrackerapp.selecteddate.CalenderDateModel
import com.shubham.tasktrackerapp.selecteddate.CalenderViewModel
import com.shubham.tasktrackerapp.upcommingtasks.UpcomingTasks
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

var tabsList = listOf("Upcoming", "All Upcoming")

@Composable
fun HomeScreen(navController: NavController) {

    var lastSelectedPosition = 0
    val calenderViewModel: CalenderViewModel = hiltViewModel()
    val datesList = remember { calenderViewModel.getDatesList() }
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }
    val formattedDate by remember {
        derivedStateOf {
            DateTimeFormatter.ofPattern("dd MM yyyy").format(pickedDate)
        }
    }
    val dateDialogState = rememberMaterialDialogState()
    val datesListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = formattedDate,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(top = 5.dp, bottom = 10.dp)
                .clickable {
                    dateDialogState.show()
                }
        )
        DatesList(datesList, datesListState)
        lastSelectedPosition = calenderViewModel.dayOfMonth - 1
        LaunchedEffect(key1 = Unit, block = {
            //TODO current date is not recomposing to show green color after changing the 'selected' boolean flag
            //TODO Also changing the color of lazy row item after clicking on it is not implemented
            datesList[lastSelectedPosition].selected = true
            datesListState.animateScrollToItem(lastSelectedPosition)
        })
        HorizontalPagerScreen(navController)
    }
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton(
                text = "Ok",
                textStyle = MaterialTheme.typography.bodyLarge
            )
            negativeButton(
                "Cancel",
                textStyle = MaterialTheme.typography.bodyLarge
            )
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        datepicker(
            initialDate = LocalDate.now(),
            title = "Pick a date",
            colors = DatePickerDefaults.colors(
                headerBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                headerTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                dateActiveBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                dateActiveTextColor = MaterialTheme.colorScheme.onSurface,
                calendarHeaderTextColor = MaterialTheme.colorScheme.onBackground
            ),
            allowedDateValidator = {
                it > LocalDate.now()
            },
        ) { date ->
            pickedDate = date
            coroutineScope.launch {
                //TODO previous selected date is not recomposing or changing the color
                datesList[lastSelectedPosition].selected = false
                lastSelectedPosition = date.dayOfMonth - 1
                datesList[lastSelectedPosition].selected = true
                datesListState.animateScrollToItem(lastSelectedPosition)
            }
        }
    }
}

@Composable
fun DatesList(dates: MutableList<CalenderDateModel>, datesListState: LazyListState) {
    val coroutineScope = rememberCoroutineScope()
    LazyRow(
        state = datesListState,
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        itemsIndexed(dates) { index: Int, item: CalenderDateModel ->
            DateItem(item) {
                coroutineScope.launch {
                    datesListState.animateScrollToItem(index)
                }
            }
        }
    }
}

@Composable
fun DateItem(date: CalenderDateModel, onClick: () -> Unit) {
    Surface(
        color = Color.Transparent,
        tonalElevation = 5.dp,
        modifier = Modifier
            .padding(start = 5.dp, top = 5.dp, end = 5.dp)
            .height(60.dp)
            .width(50.dp)
            .clickable { onClick() }
    ) {
        var bgColor = MaterialTheme.colorScheme.surface
        var txtColor = MaterialTheme.colorScheme.onSurface

        if (date.selected) {
            bgColor = MaterialTheme.colorScheme.primary
            txtColor = MaterialTheme.colorScheme.onPrimary
        }
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor, RoundedCornerShape(4.dp))
                .border(
                    width = 0.5.dp,
                    color = txtColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(4.dp)
        ) {
            Text(
                text = date.day,
                color = txtColor,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
            Text(
                text = date.date,
                color = txtColor,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerScreen(navController: NavController) {
    val roomViewModel: RoomViewModel = hiltViewModel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, top = 10.dp, end = 5.dp)
    ) {
        val pagerState = rememberPagerState()
        val coroutineScope = rememberCoroutineScope()
        val tasks by roomViewModel.getTasks().observeAsState()
//
//    val task1 = MissedTask(
//        "MissedTask1" ,
//        LocalDate.of(2023 , 1 , 25),
//        LocalDate.of(2023 , 1 , 26),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task2 = MissedTask(
//        "MissedTask2" ,
//        LocalDate.of(2023 , 1 , 24),
//        LocalDate.of(2023 , 1 , 25),
//        LocalTime.of(5 , 0),
//        LocalTime.of(6 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task3 = MissedTask(
//        "MissedTask3" ,
//        LocalDate.of(2023 , 1 , 23),
//        LocalDate.of(2023 , 1 , 24),
//        LocalTime.of(5 , 0),
//        LocalTime.of(15 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task4 = MissedTask(
//        "MissedTask4" ,
//        LocalDate.of(2023 , 1 , 22),
//        LocalDate.of(2023 , 1 , 23),
//        LocalTime.of(6 , 0),
//        LocalTime.of(7 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task5 = MissedTask(
//        "MissedTask5" ,
//        LocalDate.of(2023 , 1 , 21),
//        LocalDate.of(2023 , 1 , 22),
//        LocalTime.of(8 , 0),
//        LocalTime.of(8 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task6 = MissedTask(
//        "MissedTask6" ,
//        LocalDate.of(2023 , 1 , 20),
//        LocalDate.of(2023 , 1 , 21),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task7 = MissedTask(
//        "MissedTask7" ,
//        LocalDate.of(2023 , 1 , 19),
//        LocalDate.of(2023 , 1 , 20),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task8 = MissedTask(
//        "MissedTask8" ,
//        LocalDate.of(2023 , 1 , 18),
//        LocalDate.of(2023 , 1 , 19),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task9 = MissedTask(
//        "MissedTask9" ,
//        LocalDate.of(2023 , 1 , 17),
//        LocalDate.of(2023 , 1 , 18),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task10 = MissedTask(
//        "MissedTask10" ,
//        LocalDate.of(2023 , 1 , 16),
//        LocalDate.of(2023 , 1 , 17),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task11 = MissedTask(
//        "MissedTask11" ,
//        LocalDate.of(2023 , 1 , 15),
//        LocalDate.of(2023 , 1 , 16),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task12 = MissedTask(
//        "MissedTask12" ,
//        LocalDate.of(2023 , 1 , 14),
//        LocalDate.of(2023 , 1 , 15),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task13 = MissedTask(
//        "MissedTask13" ,
//        LocalDate.of(2023 , 1 , 13),
//        LocalDate.of(2023 , 1 , 14),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task14 = MissedTask(
//        "MissedTask14" ,
//        LocalDate.of(2023 , 1 , 12),
//        LocalDate.of(2023 , 1 , 13),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task15 = MissedTask(
//        "MissedTask15" ,
//        LocalDate.of(2023 , 1 , 11),
//        LocalDate.of(2023 , 1 , 12),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task16 = MissedTask(
//        "MissedTask16" ,
//        LocalDate.of(2023 , 1 , 10),
//        LocalDate.of(2023 , 1 , 11),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task17 = MissedTask(
//        "MissedTask17" ,
//        LocalDate.of(2023 , 1 , 9),
//        LocalDate.of(2023 , 1 , 10),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task18 = MissedTask(
//        "MissedTask18" ,
//        LocalDate.of(2023 , 1 , 8),
//        LocalDate.of(2023 , 1 , 9),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task19 = MissedTask(
//        "MissedTask19" ,
//        LocalDate.of(2023 , 1 , 7),
//        LocalDate.of(2023 , 1 , 8),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task20 = MissedTask(
//        "MissedTask20" ,
//        LocalDate.of(2023 , 1 , 6),
//        LocalDate.of(2023 , 1 , 7),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task21 = MissedTask(
//        "MissedTask21" ,
//        LocalDate.of(2023 , 1 , 5),
//        LocalDate.of(2023 , 1 , 6),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task22 = MissedTask(
//        "MissedTask22" ,
//        LocalDate.of(2023 , 1 , 4),
//        LocalDate.of(2023 , 1 , 5),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task23 = MissedTask(
//        "MissedTask23" ,
//        LocalDate.of(2023 , 1 , 3),
//        LocalDate.of(2023 , 1 , 4),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task24 = MissedTask(
//        "MissedTask24" ,
//        LocalDate.of(2023 , 1 , 2),
//        LocalDate.of(2023 , 1 , 3),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task25 = MissedTask(
//        "MissedTask25" ,
//        LocalDate.of(2023 , 1 , 1),
//        LocalDate.of(2023 , 1 , 2),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task26 = MissedTask(
//        "MissedTask26" ,
//        LocalDate.of(2022 , 12 , 31),
//        LocalDate.of(2023 , 1 , 1),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task27 = MissedTask(
//        "MissedTask27" ,
//        LocalDate.of(2022 , 12 , 30),
//        LocalDate.of(2022 , 12 , 31),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task28 = MissedTask(
//        "MissedTask28" ,
//        LocalDate.of(2022 , 12 , 29),
//        LocalDate.of(2022 , 12 , 30),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task29 = MissedTask(
//        "MissedTask29" ,
//        LocalDate.of(2022 , 12 , 28),
//        LocalDate.of(2022 , 12 , 29),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task30 = MissedTask(
//        "MissedTask30" ,
//        LocalDate.of(2022 , 12 , 27),
//        LocalDate.of(2022 , 12 , 28),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//    val task31 = MissedTask(
//        "MissedTask31" ,
//        LocalDate.of(2022 , 12 , 26),
//        LocalDate.of(2022 , 12 , 27),
//        LocalTime.of(5 , 0),
//        LocalTime.of(5 , 30),
//        mutableListOf("Assignment" , "Coding"),
//        hashMapOf(Pair("uri" , "ML_assign_1.pdf")),
//        "Playing"
//    )
//
//    roomViewModel.insertMissedTask(task1)
//    roomViewModel.insertMissedTask(task2)
//    roomViewModel.insertMissedTask(task3)
//    roomViewModel.insertMissedTask(task4)
//    roomViewModel.insertMissedTask(task5)
//    roomViewModel.insertMissedTask(task6)
//    roomViewModel.insertMissedTask(task7)
//    roomViewModel.insertMissedTask(task8)
//    roomViewModel.insertMissedTask(task9)
//    roomViewModel.insertMissedTask(task10)
//    roomViewModel.insertMissedTask(task11)
//    roomViewModel.insertMissedTask(task12)
//    roomViewModel.insertMissedTask(task13)
//    roomViewModel.insertMissedTask(task14)
//    roomViewModel.insertMissedTask(task15)
//    roomViewModel.insertMissedTask(task16)
//    roomViewModel.insertMissedTask(task17)
//    roomViewModel.insertMissedTask(task18)
//    roomViewModel.insertMissedTask(task19)
//    roomViewModel.insertMissedTask(task20)
//    roomViewModel.insertMissedTask(task21)
//    roomViewModel.insertMissedTask(task22)
//    roomViewModel.insertMissedTask(task23)
//    roomViewModel.insertMissedTask(task24)
//    roomViewModel.insertMissedTask(task25)
//    roomViewModel.insertMissedTask(task26)
//    roomViewModel.insertMissedTask(task27)
//    roomViewModel.insertMissedTask(task28)
//    roomViewModel.insertMissedTask(task29)
//    roomViewModel.insertMissedTask(task30)
//    roomViewModel.insertMissedTask(task31)

        HorizontalTabs(items = tabsList, pagerState = pagerState, coroutineScope)

        HorizontalPager(
            count = tabsList.size,
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { currentPage ->
            if (currentPage == 0) {
                UpcomingTasks(navController)
            } else {
                AllUpcomingTasksList(navController)
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalTabs(
    items: List<String>,
    pagerState: PagerState,
    scope: CoroutineScope
) {
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier
                    .pagerTabIndicatorOffset(pagerState, tabPositions)
                    .background(color = MaterialTheme.colorScheme.onSurface),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        backgroundColor = Color.Transparent
    ) {
        items.forEachIndexed { index, item ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(page = index)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(10.dp)
            ) {
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}