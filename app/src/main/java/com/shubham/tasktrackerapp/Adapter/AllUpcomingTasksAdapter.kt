package com.shubham.tasktrackerapp.Adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.db.Task

class AllUpcomingTasksAdapter(
    private val tasks: List<Task>,
    private val context: Context
) : RecyclerView.Adapter<AllUpcomingTasksAdapter.TaskViewHolder>(){
    companion object{
        private const val TAG = "AllUpcomingTasksAdapter"
    }
    private lateinit var mLayoutInflater: LayoutInflater
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val duration = itemView.findViewById<TextView>(R.id.tvDur)
        val title: TextView = itemView.findViewById(R.id.taskTitle)
        val tags = itemView.findViewById<TextView>(R.id.tvTaskTags)
        val attachments: TextView = itemView.findViewById(R.id.taskAttachments)
        val optMenu : ImageView = itemView.findViewById(R.id.optMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        if(!::mLayoutInflater.isInitialized){
            mLayoutInflater = LayoutInflater.from(context)
        }
        val view = mLayoutInflater.inflate(R.layout.rv_allupcoming_task_item , parent , false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        val date = task.due_date.split("-")
        holder.date.text = "${date[0]} ${getMonthShortForm(date[1].toInt())}"
        holder.duration.text = "${task.start_time} - ${task.end_time}"
        holder.title.text = task.title
        var string = ""
        for(i in task.taskTypes){
            string = string + i + ", "
        }
        holder.tags.text = string
        if(task.attachments.size > 1) holder.attachments.text = "${task.attachments.size} Attachments"
        else holder.attachments.text = "${task.attachments.size} Attachment"
    }
    private fun getMonthShortForm(month : Int) : String{
        when(month){
            1 -> return "Jan"
            2 -> return "Feb"
            3 -> return "Mar"
            4 -> return "Apr"
            5 -> return "May"
            6 -> return "June"
            7 -> return "July"
            8 -> return "Aug"
            9 -> return "Sept"
            10 -> return "Oct"
            11 -> return "Nov"
        }
        return "Dec"
    }
    override fun getItemCount(): Int{
        Log.d(TAG , "tasks.size in adapter - ${tasks.size}")
        return tasks.size
    }
}