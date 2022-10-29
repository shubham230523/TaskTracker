package com.shubham.tasktrackerapp

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        val title: TextView = itemView.findViewById(R.id.tv_card_title)
        val addedDate: TextView = itemView.findViewById(R.id.tv_card_task_added_date)
        val dueDate = itemView.findViewById<TextView>(R.id.tv_card_last_date)
        val timeLine: TimelineView = itemView.findViewById(R.id.timeline)
        val txtAttachments : TextView = itemView.findViewById(R.id.tv_txt_attachments)
        val startTime = itemView.findViewById<TextView>(R.id.tv_start_time)
        val attachment : TextView = itemView.findViewById(R.id.tv_attachments)
        val attachmentLine : View = itemView.findViewById(R.id.line_attachments)
        val endTime: TextView = itemView.findViewById<TextView>(R.id.tv_end_time)
        val timeLineLocalTime: TimelineView = itemView.findViewById(R.id.timeline_local_time)
        val taskCard = itemView.findViewById<MaterialCardView>(R.id.task_card)
        init {
            timeLine.initLine(viewType)
            timeLineLocalTime.initLine(viewType)
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
        if(task.title == ""){
            Log.d("TaskAdapter" , "description null")
            holder.taskCard.visibility = View.GONE
            holder.timeLine.visibility = View.GONE
            holder.timeLineLocalTime.visibility = View.VISIBLE
            holder.endTime.visibility = View.GONE
        }
        Log.d("TaskAdapter" , "description not null")
        holder.addedDate.text = task.added_date
        holder.title.text = task.title
        holder.dueDate.text = task.due_date
        if(task.attachments) {
            holder.attachment.visibility = View.VISIBLE
            holder.attachment.text = "Attachments"
            holder.attachmentLine.visibility = View.VISIBLE
        }
        holder.taskCard.setCardBackgroundColor(colors.random())
//        holder.card.setCardBackgroundColor(ContextCompat.getColor(context , color))
    }

    override fun getItemCount() = tasks.size
}