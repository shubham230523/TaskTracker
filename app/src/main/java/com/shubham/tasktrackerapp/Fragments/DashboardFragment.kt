package com.shubham.tasktrackerapp.Fragments

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.Resources.getSystem
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.shubham.tasktrackerapp.CustomView
import com.shubham.tasktrackerapp.R

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    companion object {
        private const val TAG = "DashboardFragment"
    }

    private lateinit var graphView: ImageView
    private lateinit var tvYaxis1: TextView
    private lateinit var tvYaxis2: TextView
    private lateinit var tvYaxis3: TextView
    private lateinit var tvYaxis4: TextView
    private lateinit var tvYaxis5: TextView
    private lateinit var tvYaxis6: TextView
    private lateinit var tvSun: TextView
    private lateinit var tvMon: TextView
    private lateinit var tvTue: TextView
    private lateinit var tvWed: TextView
    private lateinit var tvThu: TextView
    private lateinit var tvFri: TextView
    private lateinit var tvSat: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        graphView = view.findViewById(R.id.ivGraph)
        tvYaxis1 = view.findViewById(R.id.tvYaxis1)
        tvYaxis2 = view.findViewById(R.id.tvYaxis2)
        tvYaxis3 = view.findViewById(R.id.tvYaxis3)
        tvYaxis4 = view.findViewById(R.id.tvYaxis4)
        tvYaxis5 = view.findViewById(R.id.tvYaxis5)
        tvYaxis6 = view.findViewById(R.id.tvYaxis6)
        tvSun = view.findViewById(R.id.tvSun)
        tvMon = view.findViewById(R.id.tvMon)
        tvTue = view.findViewById(R.id.tvTue)
        tvWed = view.findViewById(R.id.tvWed)
        tvThu = view.findViewById(R.id.tvThu)
        tvFri = view.findViewById(R.id.tvFri)
        tvSat = view.findViewById(R.id.tvSat)
        setUpGraphView()
    }

    private fun setUpGraphView() {
        //sample data for plotting
        val listNoOfMissedTasksPerDay = arrayOf(5, 7, 15, 10, 12, 4, 18)
        var maxMissed = Int.MIN_VALUE
        var lowestMissed = Int.MAX_VALUE
        for (i in listNoOfMissedTasksPerDay) {
            maxMissed = Math.max(maxMissed, i)
            lowestMissed = Math.min(lowestMissed, i)
        }
        val yAxisGap = Math.ceil((maxMissed - lowestMissed).toDouble() / 5).toInt()
        val yAxisNoList = arrayListOf<Int>()
        graphView.post {
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
            val widthPx = graphView.width.px
            val heightPx = graphView.height.px
            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            // paint1 for filling the green color
            val paint1 = Paint()
            // paint2 for creating the stroke
            val paint2 = Paint()
            canvas.drawColor(ContextCompat.getColor(requireContext(), R.color.light_grey))
            paint1.style = Paint.Style.FILL
            paint1.color = ContextCompat.getColor(requireContext(), R.color.green)
            paint1.isAntiAlias = true
            paint1.shader = LinearGradient(
                0F,
                0F,
                0F,
                heightPx * 1F,
                ContextCompat.getColor(requireContext(), R.color.green),
                Color.WHITE,
                Shader.TileMode.MIRROR
            )
            paint2.style = Paint.Style.STROKE
            paint2.strokeWidth = 18F
            paint2.isAntiAlias = true
            paint2.color = ContextCompat.getColor(requireContext(), R.color.green)
            graphView.setImageBitmap(bitmap)
            val path = Path()
            //getting the left diff and right diff
            val locationHashMap = hashMapOf<Int, IntArray>()
            // adding the locations of day textview in hashMap
            for (i in 0 until 7) {
                val locations = intArrayOf(0, 0)
                when (i) {
                    0 -> {
                        tvSun.getLocationInWindow(locations)
                        locationHashMap[R.id.tvSun] = locations
                    }
                    1 -> {
                        tvMon.getLocationInWindow(locations)
                        locationHashMap[R.id.tvMon] = locations
                    }
                    2 -> {
                        tvTue.getLocationInWindow(locations)
                        locationHashMap[R.id.tvTue] = locations
                    }
                    3 -> {
                        tvWed.getLocationInWindow(locations)
                        locationHashMap[R.id.tvWed] = locations
                    }
                    4 -> {
                        tvThu.getLocationInWindow(locations)
                        locationHashMap[R.id.tvThu] = locations
                    }
                    5 -> {
                        tvFri.getLocationInWindow(locations)
                        locationHashMap[R.id.tvFri] = locations
                    }
                    6 -> {
                        tvSat.getLocationInWindow(locations)
                        locationHashMap[R.id.tvSat] = locations
                    }
                }
            }
            // adding the locations of yaxis bar in hashMap
            for (i in 0 until 6) {
                val locations = intArrayOf(0, 0)
                when (i) {
                    0 -> {
                        tvYaxis1.getLocationInWindow(locations)
                        val key = tvYaxis1.text.toString().toInt()
                        locationHashMap[key] = locations
                    }
                    1 -> {
                        tvYaxis2.getLocationInWindow(locations)
                        locationHashMap[tvYaxis2.text.toString().toInt()] = locations
                    }
                    2 -> {
                        tvYaxis3.getLocationInWindow(locations)
                        locationHashMap[tvYaxis3.text.toString().toInt()] = locations
                    }
                    3 -> {
                        tvYaxis4.getLocationInWindow(locations)
                        locationHashMap[tvYaxis4.text.toString().toInt()] = locations
                    }
                    4 -> {
                        tvYaxis5.getLocationInWindow(locations)
                        locationHashMap[tvYaxis5.text.toString().toInt()] = locations
                    }
                    5 -> {
                        tvYaxis6.getLocationInWindow(locations)
                        locationHashMap[tvYaxis6.text.toString().toInt()] = locations
                    }
                }
            }
            // left area space that is not included in the view
            val leftDiff =
                locationHashMap[tvYaxis1.text.toString().toInt()]!![0].px + tvYaxis1.width.px / 2
            // top area space that is not included in the view
            val topDiff =
                locationHashMap[tvYaxis1.text.toString().toInt()]!![1].px - tvYaxis1.height.px / 2
            //left and top distance of textview tvSat
            var distEndTvSat = locationHashMap[R.id.tvSat]!![0].px + tvSat.width.px / 2
            val distTopTvSat = locationHashMap[R.id.tvSat]!![1].px - tvSat.height.px / 2
            val ratioWidth = widthPx / (distEndTvSat - leftDiff).toFloat()
            val ratioHeight = heightPx / (distTopTvSat - topDiff).toFloat()
            // this indicating the first point of the graph
            var prevX = 0F
            var prevY = heightPx * 1F
            path.moveTo(prevX, prevY)
            for (i in 0 until listNoOfMissedTasksPerDay.size) {
                var low = 0
                var high = 0
                val num = listNoOfMissedTasksPerDay[i]
                var x = 0F
                var y = 0F
                //for loop for finding the lower bound and upperbound
                for (j in 0 until yAxisNoList.size) {
                    val vBar = yAxisNoList[j]
                    if (vBar >= num) {
                        high = vBar
                        break;
                    }
                    low = vBar
                }
                if (high == num) {
                    y = (locationHashMap[high]!![1].px - topDiff) * ratioHeight
                } else {
                    //calculating the difference between locations of height
                    val diff = locationHashMap[low]!![1].px - locationHashMap[high]!![1].px
                    val valueDiff = high - low
                    //increasing the y co-ordinate according to the diff and value diff
                    y =
                        (locationHashMap[high]!![1].px + (diff / valueDiff) * (high - num) - topDiff) * ratioHeight
                }
                when (i) {
                    0 -> x = (locationHashMap[R.id.tvSun]!![0].px - leftDiff) * ratioWidth
                    1 -> x = (locationHashMap[R.id.tvMon]!![0].px - leftDiff) * ratioWidth
                    2 -> x = (locationHashMap[R.id.tvTue]!![0].px - leftDiff) * ratioWidth
                    3 -> x = (locationHashMap[R.id.tvWed]!![0].px - leftDiff) * ratioWidth
                    4 -> x = (locationHashMap[R.id.tvThu]!![0].px - leftDiff) * ratioWidth
                    5 -> x = (locationHashMap[R.id.tvFri]!![0].px - leftDiff) * ratioWidth
                    6 -> x = (locationHashMap[R.id.tvSat]!![0].px - leftDiff) * ratioWidth
                }
                path.lineTo(x, y)
                //for drawing the stroke
                canvas.drawLine(prevX, prevY, x, y, paint2)
                prevX = x
                prevY = y
            }
            path.lineTo(prevX, heightPx * 1F)
            path.lineTo(0F, heightPx * 1F)
            //for filling the path
            canvas.drawPath(path, paint1)
        }
    }

    //Extension for converting int to dp and int to px
    val Int.dp: Int get() = (this / getSystem().displayMetrics.density).toInt()
    val Int.px: Int get() = (this * getSystem().displayMetrics.density).toInt()
}