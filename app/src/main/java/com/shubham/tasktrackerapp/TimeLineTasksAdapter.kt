package com.shubham.tasktrackerapp

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.*
import java.util.*

class TimeLineTasksAdapter(private val tasks : List<TimeLineTaskModel> , val context : Context) :
RecyclerView.Adapter<TimeLineTasksAdapter.TimeLineTaskViewHolder>(){
    private val TAG  = "TimeLineTasksAdapter"
    private lateinit var mLayoutInflater: LayoutInflater
    val colors = arrayOf(
        Color.parseColor("#03A9F4"),
        Color.parseColor("#E64A19"),
        Color.parseColor("#D81B60"),
        Color.parseColor("#9C27B0"),
        Color.parseColor("#FBC02D"),
    )
    private var cal = Calendar.getInstance(Locale.ENGLISH)
    var firstItem = true
    private val viewHolderList = mutableListOf<TimeLineTaskViewHolder>()
    var job: Job = Job()

    inner class TimeLineTaskViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView){
        val title: TextView = itemView.findViewById(R.id.tv_card_title)
        val addedDate: TextView = itemView.findViewById(R.id.tv_card_task_added_date)
        val dueDate = itemView.findViewById<TextView>(R.id.tv_card_last_date)
        val viewTimeline: View = itemView.findViewById(R.id.view_timeline)
        val txtAttachments : TextView = itemView.findViewById(R.id.tv_txt_attachments)
        val startTime = itemView.findViewById<TextView>(R.id.tv_start_time)
        val attachment : TextView = itemView.findViewById(R.id.tv_attachments)
        val attachmentLine : View = itemView.findViewById(R.id.line_attachments)
        val endTime: TextView = itemView.findViewById<TextView>(R.id.tv_end_time)
        val taskCard = itemView.findViewById<MaterialCardView>(R.id.task_card)
        val ivTaskStop = itemView.findViewById<ImageView>(R.id.iv_task_stop)
        val viewTimeLineCurrent = itemView.findViewById<View>(R.id.view_timeline_current)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineTaskViewHolder {
        if(!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        val view = mLayoutInflater.inflate(R.layout.item_timelineview , parent , false);
        val viewholder = TimeLineTaskViewHolder(view , viewType)
        viewHolderList.add(viewholder)
        return viewholder
    }

    override fun onBindViewHolder(holder: TimeLineTaskViewHolder, position: Int) {
        val task = tasks[position]
        if(firstItem){
            firstItem = false
            holder.viewTimeLineCurrent.visibility = View.VISIBLE
            holder.taskCard.visibility = View.GONE
            holder.ivTaskStop.visibility = View.GONE
            holder.startTime.visibility = View.GONE
            holder.endTime.visibility = View.GONE
        }
        holder.addedDate.text = task.added_date
        holder.title.text = task.title
        holder.startTime.text = task.startTime
        holder.endTime.text = task.endTime
        holder.dueDate.text = task.due_date
        if(task.attachments) {
            holder.attachment.visibility = View.VISIBLE
            holder.attachment.text = "Attachments"
            holder.attachmentLine.visibility = View.VISIBLE
        }
        holder.taskCard.setCardBackgroundColor(colors.random())
    }

    override fun getItemCount() = tasks.size

    fun scalingViewsTimeline(scaleFractions : List<Float>){
        Log.d("viewHolderList" , "viewHolderList Size ${viewHolderList.size}")
        val task1TimeArr = viewHolderList[1].startTime.text.toString().split(":").toTypedArray()
        val task2TimeArr = viewHolderList[2].startTime.text.toString().split(":").toTypedArray()
        val task3TimeArr = viewHolderList[3].startTime.text.toString().split(":").toTypedArray()

        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMin = cal.get(Calendar.MINUTE)
        val currentTime = (currentHour*60 + currentMin)*60*1000

        var task1Time = (task1TimeArr[0].toInt()*60 + task1TimeArr[1].toInt())*60*1000 * 1L
        var task2Time = (task2TimeArr[0].toInt()*60 + task2TimeArr[1].toInt())*60*1000 * 1L
        var task3Time = (task3TimeArr[0].toInt()*60 + task3TimeArr[1].toInt())*60*1000 * 1L
//        Log.d("time" , "$currentTime $time1 $time2 $time3")
        Log.d("task2Time" , "min - ${task2TimeArr[0].toInt()*60}  ${task2TimeArr[1].toInt()}")

        task1Time = currentTime * 1L
        task2Time = task1Time * 1L
        task3Time = task2Time * 1L
        Log.d(TAG , "curr - $currentTime , task1Time - $task1Time")
        var upperBound = 0L
        if(task1Time > currentTime) upperBound = task1Time
        else if(task2Time > currentTime) upperBound = task2Time
        else if(task3Time > currentTime) upperBound = task3Time

        GlobalScope.launch (Dispatchers.Main){
            if(upperBound == task1Time){
                for(i in 0..2){
                    var incr = 0L
                    if(i == 0) incr = task1Time - currentTime
                    else if(i == 1) incr = task2Time - task1Time
                    else incr = task3Time - task2Time
                    val del = incr / 100
                    Log.d(TAG , "incr(upper bound task1)  - $incr")
                    for(j in 1..100) {
                        scale(viewHolderList[i] , j , 100 , 1)
                        delay(del-1)
                    }
                    viewHolderList[i+1].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                }
            }
            else if(upperBound == task2Time){
                scale(viewHolderList[0] , 1 , 1 , 1000)
                delay(1000)
                viewHolderList[1].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                for(i in 1..2){
                    var incr = 0L
                    Log.d(TAG , "i(upperbound - task2Time) = $i" )
                    if(i == 1) incr = task2Time - task1Time
                    else incr = task3Time - task2Time
                    val del = incr / 100
                    for(j in 1..100) {
                        scale(viewHolderList[i] , j , 100 , 1)
                        delay(del-1)
                    }
                    viewHolderList[i+1].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                }
            }
            else if(upperBound == task3Time){
                scale(viewHolderList[0] , 1 , 1 , 1000)
                delay(1000)
                viewHolderList[1].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                scale(viewHolderList[1] , 1 , 1 , 1000)
                delay(1000)
                viewHolderList[2].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                val incr = task3Time - task2Time
                val del = incr / 100
                Log.d(TAG , "incr(upper bound task3) - $incr del - $del")
                for(j in 1..100){
                    scale(viewHolderList[2] , j , 100 , 1)
                    delay(del-1)
                }
                viewHolderList[3].ivTaskStop.setImageResource(R.drawable.ic_task_done)

            }
            else {
                scale(viewHolderList[0] , 1 , 1 , 1000)
                delay(1000)
                viewHolderList[1].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                scale(viewHolderList[1] , 1 , 1 , 1000)
                delay(1000)
                viewHolderList[2].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                scale(viewHolderList[2] , 1 , 1 , 1000)
                delay(1000)
                viewHolderList[3].ivTaskStop.setImageResource(R.drawable.ic_task_done)
            }
        }
    }

    fun scale(vh: TimeLineTasksAdapter.TimeLineTaskViewHolder, j : Int , div: Int , dur : Long){
        vh.viewTimeline.pivotY = 0F
        vh.viewTimeline.animate().duration = dur
        vh.viewTimeline.animate().scaleY(j.toFloat()/ div)
        vh.viewTimeline.animate().start()
    }
}