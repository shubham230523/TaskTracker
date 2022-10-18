package com.shubham.tasktrackerapp

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import com.google.android.material.card.MaterialCardView

class TimeLineTasksAdapter(private val tasks : List<TimeLineTaskModel> , val context : Context) :
RecyclerView.Adapter<TimeLineTasksAdapter.TimeLineTaskViewHolder>(){

    private lateinit var mLayoutInflater: LayoutInflater
    val colors = arrayOf(
        Color.parseColor("#F9EBEA"),
        Color.parseColor("#F4ECF7"),
        Color.parseColor("#EBF5FB"),
        Color.parseColor("#E8F6F3"),
        Color.parseColor("#FEF9E7"),
        Color.parseColor("#FDF2E9"),
        Color.parseColor("#F8F9F9"),
        Color.parseColor("#EBEDEF")
    )

    inner class TimeLineTaskViewHolder(itemView: View, viewType: Int) : RecyclerView.ViewHolder(itemView){
        val title = itemView.findViewById<TextView>(R.id.timeline_card_title)
        val description = itemView.findViewById<TextView>(R.id.timeline_card_description)
        val lastDate = itemView.findViewById<TextView>(R.id.timeline_card_lastdate)
        val timeLine = itemView.findViewById<TimelineView>(R.id.timeline)
        val startTime = itemView.findViewById<TextView>(R.id.tv_start_time)
        val endTime = itemView.findViewById<TextView>(R.id.tv_end_time)
        val timeLine_curr_time = itemView.findViewById<TimelineView>(R.id.timeline_current_time)
        val card = itemView.findViewById<MaterialCardView>(R.id.task_card)
        init {
            timeLine.initLine(viewType)
            timeLine_curr_time.initLine(viewType)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineTaskViewHolder {
        if(!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        val view = mLayoutInflater.inflate(R.layout.item_timelineview , parent , false);
        return TimeLineTaskViewHolder(view , viewType)
    }

    override fun onBindViewHolder(holder: TimeLineTaskViewHolder, position: Int) {
        val task = tasks[position]
        if(task.description == ""){
            Log.d("TaskAdapter" , "description null")
            holder.card.visibility = View.GONE
            holder.timeLine.visibility = View.GONE
            holder.timeLine_curr_time.visibility = View.VISIBLE
            holder.endTime.visibility = View.GONE
        }
        Log.d("TaskAdapter" , "description not null")
        holder.description.text = task.description
        holder.title.text = task.title
        holder.lastDate.text = task.lastDate
        holder.card.setCardBackgroundColor(colors.random())
//        holder.card.setCardBackgroundColor(ContextCompat.getColor(context , color))
    }

    override fun getItemCount() = tasks.size
}