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
        // today's date
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

        // date list in row format
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

    // dialog for selecting the tasks
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
                    // animating the scroll of row when click on a particular date
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, top = 10.dp, end = 5.dp)
    ) {
        val pagerState = rememberPagerState()
        val coroutineScope = rememberCoroutineScope()

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