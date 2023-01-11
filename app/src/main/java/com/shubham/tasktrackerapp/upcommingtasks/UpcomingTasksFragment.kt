package com.shubham.tasktrackerapp.upcommingtasks

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.MainActivity
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.dashboard.missedTasks
import com.shubham.tasktrackerapp.data.local.Task
import kotlinx.coroutines.*

/**
 * Represents upcoming task in home fragment
 */
class UpcomingTasksFragment(private val mContext: Context) :
    Fragment(R.layout.fragment_upcoming_tasks) {
    private var mTasks: List<Task>? = null

    companion object {
        private const val TAG = "UpcomingTasksFragment"
        private var mTasks: List<Task>? = null
    }

    private lateinit var rvTasks: RecyclerView
    private var job: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvTasks = view.findViewById(R.id.rv_tasks)

        job = GlobalScope.launch(Dispatchers.Main) {
            //mTasks = (context as MainActivity).getTasksList()
            if (mTasks != null && mTasks!!.size > 0) {
                val taskAdapter = TimeLineTasksAdapter(mTasks!!, mContext)
                rvTasks.apply {
                    adapter = taskAdapter
                    layoutManager =
                        LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
                }
                rvTasks.runWhenReady { taskAdapter.scalingViewsTimeline() }
            }
            delay(1000)
            job!!.cancelAndJoin()
        }
    }

    /**
     * Extension method to override the "runWhenReady" method
     */
    private fun RecyclerView.runWhenReady(action: () -> Unit) {
        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                action()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }
}


@Composable
fun UpcomingTasks() {
    val tasks = missedTasks
    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .offset(x = 60.dp)
                .size(width = 3.dp, height = 60.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary
                )
        )
        LazyColumn{
            items(tasks){
                TimeLineTaskItem()
            }
        }
    }
}

@Composable
fun TimeLineTaskItem(){
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = 10.dp, end = 10.dp)
    ) {
        val (startTime , endTime, timeLineStop , timeLine , taskCard) = createRefs()
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 200.dp)
                .background(color = MaterialTheme.colorScheme.primary)
                .constrainAs(timeLine) {
                    start.linkTo(parent.start, 50.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
        )
        Image(
            painter = painterResource(id = R.drawable.ic_task_stop),
            contentDescription = null,
            modifier = Modifier.constrainAs(timeLineStop){
                start.linkTo(timeLine.start)
                end.linkTo(timeLine.end)
                top.linkTo(timeLine.top)
                width = Dimension.value(24.dp)
                height = Dimension.value(24.dp)
            }
        )
        Text(
            text = "11:00",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.constrainAs(startTime){
                top.linkTo(timeLineStop.top)
                bottom.linkTo(timeLineStop.bottom)
                end.linkTo(timeLineStop.start , 10.dp)
            }
        )
        Text(
            text = "12:00",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.constrainAs(endTime){
                top.linkTo(startTime.bottom)
                start.linkTo(startTime.start , 10.dp)
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
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "BDA Assignment",
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
                    text = "18 Oct",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Due - 20 Oct",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Row (modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Assignment",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(6.dp)
                )
                Text(
                    text = "Coding",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(6.dp)
                )
                Text(
                    text = "+1 more",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Row(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "No Attachments",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_down_arrow),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(1.dp)
                .background(color = MaterialTheme.colorScheme.onPrimaryContainer)
                .alpha(0.4f)
            )
            Row(modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    color = MaterialTheme.colorScheme.inversePrimary,
                    RoundedCornerShape(10.dp)
                )
                .padding(6.dp)
            ) {
                ConstraintLayout(modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()) {
                    val (fileImg , filename , close) = createRefs()
                    Image(
                        painter = painterResource(id = R.drawable.pdf),
                        contentDescription = null,
                        modifier = Modifier.constrainAs(fileImg){
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.value(16.dp)
                            height = Dimension.value(16.dp)
                        }
                    )
                    Text(
                        text = "Bda_assign_1.pdf",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.constrainAs(filename){
                            start.linkTo(fileImg.end , 10.dp)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            end.linkTo(close.end , 10.dp)
                        }
                    )
                    Icon(
                        Icons.Filled.Close,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentDescription = null,
                        modifier = Modifier.constrainAs(close){
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.value(16.dp)
                            height = Dimension.value(16.dp)
                        }
                    )
                }
            }
            Row(modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .background(
                    color = MaterialTheme.colorScheme.inversePrimary,
                    RoundedCornerShape(10.dp)
                )
                .padding(6.dp)
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    val (fileImg, filename, close) = createRefs()
                    Image(
                        painter = painterResource(id = R.drawable.pdf),
                        contentDescription = null,
                        modifier = Modifier.constrainAs(fileImg) {
                            start.linkTo(parent.start)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            width = Dimension.value(16.dp)
                        }
                    )
                    Text(
                        text = "Bda_assign_3.pdf",
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


