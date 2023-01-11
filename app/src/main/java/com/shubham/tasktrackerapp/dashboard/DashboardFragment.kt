package com.shubham.tasktrackerapp.dashboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Resources.getSystem
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Guideline
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import app.futured.donut.DonutStrokeCap
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.RevealViewAnimation
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.theme.*
import com.shubham.tasktrackerapp.util.isLight

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    companion object {
        private const val TAG = "DashboardFragment"
        private const val GRAPH_TYPE_WEEKLY = "Weekly"
        private const val GRAPH_TYPE_MONTHLY = "Monthly"
    }

    private lateinit var btnWeekly: TextView
    private lateinit var btnMonthly: TextView
    private lateinit var btnBeforeDeadline: TextView
    private lateinit var btnMissed: TextView
    private lateinit var graphView: ImageView
    private lateinit var revealView: View
    private lateinit var tvYaxis1: TextView
    private lateinit var tvYaxis2: TextView
    private lateinit var tvYaxis3: TextView
    private lateinit var tvYaxis4: TextView
    private lateinit var tvYaxis5: TextView
    private lateinit var tvYaxis6: TextView
    private lateinit var tvXaxis1: TextView
    private lateinit var tvXaxis2: TextView
    private lateinit var tvXaxis3: TextView
    private lateinit var tvXaxis4: TextView
    private lateinit var tvXaxis5: TextView
    private lateinit var tvXaxis6: TextView
    private lateinit var tvXaxis7: TextView
    private lateinit var glXaxis1: Guideline
    private lateinit var glXaxis2: Guideline
    private lateinit var glXaxis3: Guideline
    private lateinit var glXaxis4: Guideline
    private lateinit var glXaxis5: Guideline
    private lateinit var glXaxis6: Guideline
    private lateinit var glDate5: Guideline
    private lateinit var glDate10: Guideline
    private lateinit var glDate15: Guideline
    private lateinit var glDate20: Guideline
    private lateinit var glDate25: Guideline
    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var donutView: DonutProgressView
    private lateinit var rvMissedTasks: RecyclerView
    private var isBeforeDeadlineGraphViewSelected = true

    // paint1 for filling the green color
    private var paint1 = Paint()

    // paint2 for creating the stroke
    private var paint2 = Paint()
    private var hashMap = hashMapOf<String, String>()
    private val taskList = listOf<Task>(
        Task(
            "Title1",
            "12 Nov",
            "14 Nov",
            "9:00AM",
            "9:30AM",
            mutableListOf("Assignment, Coding, Classes, Hobby"),
            hashMap,
            R.color.red,
        ),
        Task(
            "Title1",
            "12 Nov",
            "14 Nov",
            "9:00AM",
            "9:30AM",
            mutableListOf("Assignment"),
            hashMap,
            R.color.red,
        ),
        Task(
            "Title1",
            "12 Nov",
            "14 Nov",
            "9:00AM",
            "9:30AM",
            mutableListOf("Assignment"),
            hashMap,
            R.color.red,
        ),
        Task(
            "Title1",
            "12 Nov",
            "14 Nov",
            "9:00AM",
            "9:30AM",
            mutableListOf("Assignment"),
            hashMap,
            R.color.red,
        ),
        Task(
            "Title1",
            "12 Nov",
            "14 Nov",
            "9:00AM",
            "9:30AM",
            mutableListOf("Assignment"),
            hashMap,
            R.color.red,
        ),
        Task(
            "Title1",
            "12 Nov",
            "14 Nov",
            "9:00AM",
            "9:30AM",
            mutableListOf("Assignment"),
            hashMap,
            R.color.red,
        )
    )

    init {
        paint1.style = Paint.Style.FILL
        paint1.isAntiAlias = true
        paint2.style = Paint.Style.STROKE
        paint2.strokeWidth = 1.5F
        paint2.isAntiAlias = true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        constraintLayout = view.findViewById(R.id.clSVDashboard)
        btnWeekly = view.findViewById(R.id.tvWeekly)
        btnMonthly = view.findViewById(R.id.tvMonthly)
        btnBeforeDeadline = view.findViewById(R.id.tvPCBeforeDeadline)
        btnMissed = view.findViewById(R.id.tvPCMissed)

        // graph components
        graphView = view.findViewById(R.id.ivGraph)
        tvYaxis1 = view.findViewById(R.id.tvYaxis1)
        tvYaxis2 = view.findViewById(R.id.tvYaxis2)
        tvYaxis3 = view.findViewById(R.id.tvYaxis3)
        tvYaxis4 = view.findViewById(R.id.tvYaxis4)
        tvYaxis5 = view.findViewById(R.id.tvYaxis5)
        tvYaxis6 = view.findViewById(R.id.tvYaxis6)
        tvXaxis1 = view.findViewById(R.id.tvXaxis1)
        tvXaxis2 = view.findViewById(R.id.tvXaxis2)
        tvXaxis3 = view.findViewById(R.id.tvXaxis3)
        tvXaxis4 = view.findViewById(R.id.tvXaxis4)
        tvXaxis5 = view.findViewById(R.id.tvXaxis5)
        tvXaxis6 = view.findViewById(R.id.tvXaxis6)
        tvXaxis7 = view.findViewById(R.id.tvXaxis7)
        glXaxis1 = view.findViewById(R.id.glTvXaxis1)
        glXaxis2 = view.findViewById(R.id.glTvXaxis2)
        glXaxis3 = view.findViewById(R.id.glTvXaxis3)
        glXaxis4 = view.findViewById(R.id.glTvXaxis4)
        glXaxis5 = view.findViewById(R.id.glTvXaxis5)
        glXaxis6 = view.findViewById(R.id.glTvXaxis6)
        glDate5 = view.findViewById(R.id.glDate5)
        glDate10 = view.findViewById(R.id.glDate10)
        glDate15 = view.findViewById(R.id.glDate15)
        glDate20 = view.findViewById(R.id.glDate20)
        glDate25 = view.findViewById(R.id.glDate25)
        revealView = view.findViewById(R.id.ivGraphRevealView)

        //donut view
        donutView = view.findViewById(R.id.donut_view)
        rvMissedTasks = view.findViewById(R.id.missedTasksRV)

        btnWeekly.setOnClickListener {
            btnWeekly.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dates_selected_background_stroke
            )
            btnMonthly.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dates_unselected_background_stroke
            )
            updateXmlByWeekly()
        }

        btnMonthly.setOnClickListener {
            btnMonthly.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dates_selected_background_stroke
            )
            btnWeekly.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dates_unselected_background_stroke
            )
            updateXmlByMonthly()
        }

        btnMissed.setOnClickListener {
            isBeforeDeadlineGraphViewSelected = false
            btnMissed.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dates_selected_background_stroke
            )
            btnBeforeDeadline.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dates_unselected_background_stroke
            )
            setUpGraphView(GRAPH_TYPE_WEEKLY, R.color.red)
        }

        btnBeforeDeadline.setOnClickListener {
            isBeforeDeadlineGraphViewSelected = true
            btnBeforeDeadline.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dates_selected_background_stroke
            )
            btnMissed.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.dates_unselected_background_stroke
            )
            setUpGraphView(GRAPH_TYPE_WEEKLY, R.color.green)
        }

        setUpGraphView(GRAPH_TYPE_MONTHLY, R.color.green)

        setUpDonutView()

        val missedTaskAdapter = MissedTasksAdapter(requireContext(), taskList)
        rvMissedTasks.apply {
            adapter = missedTaskAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    /**
     * Function to show the dashboard according to the monthly data when
     * btnMonthly is clicked
     */
    private fun updateXmlByMonthly() {
        if (isBeforeDeadlineGraphViewSelected) {
            setUpGraphView(GRAPH_TYPE_MONTHLY, R.color.green)
        } else {
            setUpGraphView(GRAPH_TYPE_MONTHLY, R.color.red)
        }
    }

    /**
     * Function to show the dashboard according to the weekly data when
     * btnWeekly is clicked
     */
    private fun updateXmlByWeekly() {
        if (isBeforeDeadlineGraphViewSelected) {
            setUpGraphView(GRAPH_TYPE_WEEKLY, R.color.green)
        } else {
            setUpGraphView(GRAPH_TYPE_WEEKLY, R.color.red)
        }
    }

    /**
     * Function to get locations of various textviews of graphs in window/screen
     * and draw the curve
     *
     * //TODO we have to change this to all list to include tasks done on time
     * @param missedTasks list of tasks which are missed by the user
     * @param tag Tag for which indicates weekly graph or monthly graph
     * @param color Graph color red - missed and green - done on time
     */
    private fun calculateLocationsAndDrawTheCurve(
        missedTasks: MutableList<Int>,
        tag: String,
        color: Int
    ) {

        var maxMissed = Int.MIN_VALUE
        var lowestMissed = Int.MAX_VALUE
        for (i in missedTasks) {
            maxMissed = Math.max(maxMissed, i)
            lowestMissed = Math.min(lowestMissed, i)
        }
        val yAxisGap = Math.ceil((maxMissed - lowestMissed).toDouble() / 5).toInt()
        val yAxisNoList = arrayListOf<Int>()
        var tasksMissed = lowestMissed

        //setting the values of yaxis of the graph to the appropriate range
        for (i in 0 until 6) {
            when (i) {
                0 -> tvYaxis6.text = tasksMissed.toString()
                1 -> tvYaxis5.text = tasksMissed.toString()
                2 -> tvYaxis4.text = tasksMissed.toString()
                3 -> tvYaxis3.text = tasksMissed.toString()
                4 -> tvYaxis2.text = tasksMissed.toString()
                5 -> tvYaxis1.text = tasksMissed.toString()
            }
            yAxisNoList.add(tasksMissed)
            tasksMissed += yAxisGap
        }

        val widthPx = 315
        val heightPx = 250
        //val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        //val canvas = Canvas(bitmap)
        //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        paint1.color = ContextCompat.getColor(requireContext(), color)
//        paint1.shader = LinearGradient(
//            0F,
//            0F,
//            0F,
//            heightPx * 1F,
//            ContextCompat.getColor(requireContext(), color),
//            ContextCompat.getColor(requireContext(), R.color.white),
//            Shader.TileMode.REPEAT
//        )
//        paint2.color = ContextCompat.getColor(requireContext(), color)
//        graphView.setImageBitmap(bitmap)
        val path = Path()
        val strokePath = Path()
        // this indicating the first point of the graph
        var prevX = 0F
        var prevY = heightPx * 1F
        path.moveTo(prevX, prevY)
        strokePath.moveTo(prevX, prevY)

        var x = 0F
        for (i in 0 until missedTasks.size) {
            var low = 0
            var high = 0
            val num = missedTasks[i]
            var y = 0F
            if (tag == GRAPH_TYPE_MONTHLY) {
                x += 10.5f
            } else {
                x += 45f
            }
            //for loop for finding the lower bound and upperbound
            for (j in 0 until yAxisNoList.size) {
                val vBar = yAxisNoList[j]
                if (vBar >= num) {
                    high = vBar
                    break
                }
                if (j != 0) y += 50F
                low = vBar
            }
            if (high != num) {
                y = y + ((50 / (high - low)) * (num - low))
            }
            // heightPx - y because we have calculated y from bottom and we want the distance from the top
            path.lineTo(x, heightPx - y)
            strokePath.lineTo(x, heightPx - y)
            prevX = x
            prevY = heightPx - y
        }

        path.lineTo(prevX, heightPx * 1F)
        path.lineTo(0F, heightPx * 1F)

        //for filling the path
//        canvas.drawPath(path, paint1)
//
//        // drawing the bitmap because we want antialias effect
//        canvas.drawBitmap(bitmap, widthPx * 1f, heightPx * 1f, paint1)
//        canvas.drawPath(strokePath, paint2)

        //animator for sliding the reveal view to 315 px to the right so that graph view is visible in small steps
        val animator = ObjectAnimator.ofFloat(revealView, "translationX", 315.px.toFloat())
//        animator.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: Animator?) {
//                super.onAnimationEnd(animation)
//                // making the reveal view invisible and bringing back to its original position
//                revealView.visibility = View.INVISIBLE
//                ObjectAnimator.ofFloat(revealView, "translationX", 0.px.toFloat()).setDuration(0)
//                    .start()
//            }
//
//            override fun onAnimationStart(animation: Animator?) {
//                super.onAnimationStart(animation)
//                // making the graphview visible only at the start of the animation so that it graph doesn't appear before
//                // sliding the reveal view
//                revealView.visibility = View.VISIBLE
//                graphView.visibility = View.VISIBLE
//            }
//        })
//        animator.setDuration(2000).start()
    }

    /**
     * Function to change the number of text views in x-axis to 6 from 7
     *
     * @param tag which indicates monthly or weekly report
     * @param color color for the graph red - missed tasks and green - done on time
     */
    private fun setUpGraphView(tag: String, color: Int) {

        graphView.visibility = View.INVISIBLE

        val listNoOfMissedTasksForMonth = mutableListOf(
            5, 7, 15, 10, 12, 4, 18, 12, 16, 16, 19, 18, 6, 7, 9, 12, 4, 3, 6, 9,
            10, 8, 10, 18, 2, 3, 4, 6, 6, 17
        )

        val listNoOfMissedTasksPerWeek = mutableListOf(5, 7, 15, 10, 12, 4, 18)

        //as we want only 6 text view on the xaxis in case monthly chart (it includes tvXaxis7)
        if (tag == GRAPH_TYPE_MONTHLY) tvXaxis6.visibility = View.GONE
        graphView.post {
            if (tag == GRAPH_TYPE_MONTHLY) {
                val constraintSet = ConstraintSet()
                constraintSet.clone(constraintLayout)
                //changing the constraints of Xaxis text views to new guidelines incase of monthly chart
                for (i in 0 until 6) {
                    when (i) {
                        0 -> {
                            tvXaxis1.text = "5"
                            tvXaxis1.textSize = 14f
                            constraintSet.connect(
                                R.id.tvXaxis1,
                                ConstraintSet.START,
                                R.id.glDate5,
                                ConstraintSet.START
                            )
                            constraintSet.connect(
                                R.id.tvXaxis1,
                                ConstraintSet.END,
                                R.id.glDate5,
                                ConstraintSet.END
                            )
                        }
                        1 -> {
                            tvXaxis2.text = "10"
                            tvXaxis2.textSize = 14f
                            constraintSet.connect(
                                R.id.tvXaxis2,
                                ConstraintSet.START,
                                R.id.glDate10,
                                ConstraintSet.START
                            )
                            constraintSet.connect(
                                R.id.tvXaxis2,
                                ConstraintSet.END,
                                R.id.glDate10,
                                ConstraintSet.END
                            )
                        }
                        2 -> {
                            tvXaxis3.text = "15"
                            tvXaxis3.textSize = 14f
                            constraintSet.connect(
                                R.id.tvXaxis3,
                                ConstraintSet.START,
                                R.id.glDate15,
                                ConstraintSet.START
                            )
                            constraintSet.connect(
                                R.id.tvXaxis3,
                                ConstraintSet.END,
                                R.id.glDate15,
                                ConstraintSet.END
                            )
                        }
                        3 -> {
                            tvXaxis4.text = "20"
                            constraintSet.connect(
                                R.id.tvXaxis4,
                                ConstraintSet.START,
                                R.id.glDate20,
                                ConstraintSet.START
                            )
                            constraintSet.connect(
                                R.id.tvXaxis4,
                                ConstraintSet.END,
                                R.id.glDate20,
                                ConstraintSet.END
                            )
                        }
                        4 -> {
                            tvXaxis5.text = "25"
                            constraintSet.connect(
                                R.id.tvXaxis5,
                                ConstraintSet.START,
                                R.id.glDate25,
                                ConstraintSet.START
                            )
                            constraintSet.connect(
                                R.id.tvXaxis5,
                                ConstraintSet.END,
                                R.id.glDate25,
                                ConstraintSet.END,
                            )
                        }
                        5 -> {
                            tvXaxis7.text = "30"
                        }
                    }
                    constraintSet.applyTo(constraintLayout)
                }
                calculateLocationsAndDrawTheCurve(listNoOfMissedTasksForMonth, tag, color)
            } else {
                calculateLocationsAndDrawTheCurve(listNoOfMissedTasksPerWeek, tag, color)
            }
        }
    }

    /**
     * Function to show tasks by categories
     */
    private fun setUpDonutView() {
        val section1 = DonutSection(
            name = "Assignment",
            color = ContextCompat.getColor(requireContext(), R.color.red),
            amount = 0.4f
        )
        val section2 = DonutSection(
            name = "Classes",
            color = ContextCompat.getColor(requireContext(), R.color.yellow),
            amount = 0.2f
        )
        val section3 = DonutSection(
            name = "Hangout",
            color = ContextCompat.getColor(requireContext(), R.color.orange),
            amount = 0.1f
        )
        val section4 = DonutSection(
            name = "Television",
            color = ContextCompat.getColor(requireContext(), R.color.blue),
            amount = 0.1f
        )
        val section5 = DonutSection(
            name = "Other",
            color = ContextCompat.getColor(requireContext(), R.color.purple_200),
            amount = 0.2f
        )
        donutView.cap = 1f
        donutView.animationDurationMs = 3000
        donutView.submitData(listOf(section1, section2, section3, section4, section5))
    }

    /**
     * Extension for converting int to dp and int to px
     */
    val Int.dp: Int get() = (this / getSystem().displayMetrics.density).toInt()
    val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()
}

@Composable
fun DashBoard() {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp)
                    .wrapContentHeight(),
            ) {
                Text(
                    text = "Weekly",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(15.dp)
                        )
                        .padding(8.dp)
                        .weight(1f)
                )
                Text(
                    text = "Monthly",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(15.dp)
                        )
                        .padding(8.dp)
                        .weight(1f)
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f, fill = false)
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Box (
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(15.dp)
                            )
                            .height(120.dp)
                            .padding(10.dp)
                            .weight(1f)

                    ){
                        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                            val (tvMissed , tvAcc , ivAcc) = createRefs()
                            Text(
                                text = "Missed - 3/10",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.constrainAs(tvMissed){
                                    start.linkTo(parent.start)
                                    top.linkTo(parent.top)
                                }
                            )
                            Text(
                                text = "70% Accuracy",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.constrainAs(tvAcc){
                                    start.linkTo(parent.start)
                                    top.linkTo(tvMissed.bottom , 10.dp)
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
                    Box (
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.tertiary,
                                RoundedCornerShape(15.dp)
                            )
                            .height(120.dp)
                            .padding(10.dp)
                            .weight(1f)
                    ){
                        ConstraintLayout (modifier = Modifier.fillMaxSize()){
                            val (tvBeforeDeadline , tvMore , ivClock) = createRefs()
                            Text(
                                text = "Before deadline - 2/7",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.constrainAs(tvBeforeDeadline){
                                    start.linkTo(parent.start)
                                    top.linkTo(parent.top)
                                }
                            )
                            Text(
                                text = "5% more than last week",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.constrainAs(tvMore){
                                    start.linkTo(parent.start)
                                    top.linkTo(tvBeforeDeadline.bottom , 10.dp)
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
                    text = "Performance Graph",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 15.dp)
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
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(15.dp)
                            )
                            .padding(10.dp)
                    )
                    Text(
                        text = "Missed",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(15.dp)
                            )
                            .padding(10.dp)
                    )
                }
                ConstraintLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp)
                        .wrapContentHeight()
                ) {
                    val coordinates = calculateLocationsAndDrawTheCurve(
                        listNoOfMissedTasksPerWeek,
                        "Weekly"
                    )
                    val (ycol , xrow , ivgraph , revealView) = createRefs()
                    Column(
                        modifier = Modifier.constrainAs(ycol){
                            start.linkTo(parent.start)
                            top.linkTo(ivgraph.top)
                            bottom.linkTo(ivgraph.bottom)
                            height = Dimension.fillToConstraints
                        },
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Log.d("Coord" , "${coordinates[0][0]}")
                        for(i in coordinates[0].size-1 downTo 0){
                            Text(
                                text = coordinates[0][i].toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    val isLight  = MaterialTheme.colorScheme.isLight()
                    Canvas(
                        modifier = Modifier
                            .constrainAs(ivgraph) {
                                start.linkTo(ycol.end, 5.dp)
                                width = Dimension.value(320.dp)
                                height = Dimension.value(266.dp)
                            }
                            .padding(top = 8.dp, start = 10.dp, end = 10.dp, bottom = 8.dp)
                    ){

                        val path = Path().let{
                            for(i in 0 until coordinates[1].size){
                                if(i == 0) {
                                    it.moveTo(coordinates[1][i], coordinates[2][i])
                                }
                                else {
                                    it.lineTo(coordinates[1][i], coordinates[2][i])
                                }
                                Log.d("Coord" , "safe")
                            }
                            it.lineTo(this.size.width , this.size.height)
                            it.lineTo(0f , this.size.height)
                            it.close()
                            it
                        }
                        drawPath(
                            path = path,
                            brush = if(isLight){
                                Brush.verticalGradient(listOf(Teal600 ,  Transparent))
                            }else {
                                Brush.verticalGradient(listOf(Teal200 , Transparent))
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
                            .constrainAs(revealView){
                                start.linkTo(ivgraph.start , 320.dp)
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
                        modifier = Modifier.constrainAs(xrow){
                            start.linkTo(ivgraph.start)
                            end.linkTo(ivgraph.end)
                            top.linkTo(ivgraph.bottom, 10.dp)
                            width = Dimension.fillToConstraints
                        },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val xaxis  = listOf("Sun" , "Mon" , "Tue" , "Wed" , "Thu" , "Fri" , "Sat")
                        for(element in xaxis){
                            Text(
                                text = element,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
                Text(
                    text = "Tasks by categories",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 15.dp)
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
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(15.dp)
                            )
                            .padding(10.dp)
                    )
                    Text(
                        text = "Missed",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(15.dp)
                            )
                            .padding(10.dp)
                    )
                }
                ConstraintLayout(
                    modifier = Modifier
                        .padding(top = 15.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    val (donutView, colPrim , colSecon , colTer, colRed , colonBack
                    , leg1 , leg2 ,leg3 , leg4 , leg5) = createRefs()
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
                                    ContextCompat.getColor(context, R.color.secondary),
                                    0.2f
                                ),
                                DonutSection(
                                    "Hangout",
                                    ContextCompat.getColor(context, R.color.tertiary),
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
                            DonutProgressView(context).apply{
                                bgLineColor = ContextCompat.getColor(context , R.color.light_grey)
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
                                start.linkTo(donutView.end, 30.dp)
                                top.linkTo(donutView.top, 15.dp)
                            }
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(color = MaterialTheme.colorScheme.secondary)
                            .constrainAs(colSecon) {
                                start.linkTo(donutView.end, 30.dp)
                                top.linkTo(colPrim.bottom, 10.dp)
                            }
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(color = MaterialTheme.colorScheme.tertiary)
                            .constrainAs(colTer) {
                                start.linkTo(donutView.end, 30.dp)
                                top.linkTo(colSecon.bottom, 10.dp)
                            }
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(color = MaterialTheme.colorScheme.onBackground)
                            .constrainAs(colonBack) {
                                start.linkTo(donutView.end, 30.dp)
                                top.linkTo(colTer.bottom, 10.dp)
                            }
                    )
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height(10.dp)
                            .background(color = MaterialTheme.colorScheme.error)
                            .constrainAs(colRed) {
                                start.linkTo(donutView.end, 30.dp)
                                top.linkTo(colonBack.bottom, 10.dp)
                            }
                    )
                    Text(
                        text = "Assignment",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(leg1){
                            start.linkTo(colPrim.end , 15.dp)
                            top.linkTo(colPrim.top)
                            bottom.linkTo(colPrim.bottom)
                        }
                    )
                    Text(
                        text = "Classes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(leg2){
                            start.linkTo(colSecon.end , 15.dp)
                            top.linkTo(colSecon.top)
                            bottom.linkTo(colSecon.bottom)
                        }
                    )
                    Text(
                        text = "Hangout",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(leg3){
                            start.linkTo(colTer.end , 15.dp)
                            top.linkTo(colTer.top)
                            bottom.linkTo(colTer.bottom)
                        }
                    )
                    Text(
                        text = "Television",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(leg4){
                            start.linkTo(colonBack.end , 15.dp)
                            top.linkTo(colonBack.top)
                            bottom.linkTo(colonBack.bottom)
                        }
                    )
                    Text(
                        text = "Remainder",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.constrainAs(leg5){
                            start.linkTo(colRed.end , 15.dp)
                            top.linkTo(colRed.top)
                            bottom.linkTo(colRed.bottom)
                        }
                    )
                }
                Text(
                    text = "Recent Missed Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 15.dp)
                )
                LazyRow{
                    items(items = missedTasks , itemContent = {
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
                        ){
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = it.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                                Text(
                                    text = it.due_date + " " + it.start_time + '-' + it.end_time,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                                var string = ""
                                for(i in it.taskTypes){
                                    string+=(i + " ")
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
        }
    }
}

val missedTasks = listOf<Task>(
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    ),
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    ),
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    ),
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    ),
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    ),
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    ),
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    ),
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    ),
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    ),
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    ),
    Task(
        "Hello",
        "20-20-20",
        "20-12-22",
        "7:03",
        "8:30",
        taskTypes = mutableListOf("Assignment , Television"),
        hashMapOf(),
        1,
        1
    )
)

fun calculateLocationsAndDrawTheCurve(
    missedTasks: MutableList<Int>,
    tag: String
): ArrayList<ArrayList<Float>>{

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

//    if(tag == "Monthly"){
//        coordinates.add(arrayListOf(0f , 5f , 10f , 15 , 20 , 25, 30))
//    }
//    else {
//        coordinates.add(arrayListOf("Sun" , "Mon" , "Tue" , "Wed" , "Thu" , "Fri" , "Sat"))
//    }

    val widthPx = 300.toPx * 1f
    val heightPx = 250.toPx * 1f

    val xCordList = arrayListOf<Float>()
    val yCordList = arrayListOf<Float>()

    var x = -(50.toPx * 1f)
    for (i in 0 until missedTasks.size) {
        var low = 0f
        var high = 0f
        val num = missedTasks[i] * 1f
        var y = 0F
        if (tag == "Monthly") {
            x += (10.toPx * 1f)
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
//
//    //animator for sliding the reveal view to 315 px to the right so that graph view is visible in small steps
//    val animator = ObjectAnimator.ofFloat(revealView, "translationX", 315.px.toFloat())
////        animator.addListener(object : AnimatorListenerAdapter() {
////            override fun onAnimationEnd(animation: Animator?) {
////                super.onAnimationEnd(animation)
////                // making the reveal view invisible and bringing back to its original position
////                revealView.visibility = View.INVISIBLE
////                ObjectAnimator.ofFloat(revealView, "translationX", 0.px.toFloat()).setDuration(0)
////                    .start()
////            }
////
////            override fun onAnimationStart(animation: Animator?) {
////                super.onAnimationStart(animation)
////                // making the graphview visible only at the start of the animation so that it graph doesn't appear before
////                // sliding the reveal view
////                revealView.visibility = View.VISIBLE
////                graphView.visibility = View.VISIBLE
////            }
////        })
////        animator.setDuration(2000).start()
//        }
}


val listNoOfMissedTasksForMonth = mutableListOf(
    5, 7, 15, 10, 12, 4, 18, 12, 16, 16, 19, 18, 6, 7, 9, 12, 4, 3, 6, 9,
    10, 8, 10, 18, 2, 3, 4, 6, 6, 17
)

val listNoOfMissedTasksPerWeek = mutableListOf(9, 7, 15, 10, 12, 4, 18)

val Int.toDp: Int get() = (this / getSystem().displayMetrics.density).toInt()
val Int.toPx: Int get() = (this * getSystem().displayMetrics.density).toInt()