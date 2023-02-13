package com.shubham.tasktrackerapp.allupcommingtasks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.util.TaskDiffCallBack


class AllUpcomingTasksAdapter(
    private val context: Context,
    private val viewModel: AllUpcomingTasksViewModel
) : RecyclerView.Adapter<AllUpcomingTasksAdapter.TaskViewHolder>() {

    companion object {
        private const val TAG = "AllUpcomingTasksAdapter"
    }

    private val tasks: ArrayList<Task> = arrayListOf()

    private lateinit var mLayoutInflater: LayoutInflater

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val duration = itemView.findViewById<TextView>(R.id.tvDur)
        val title: TextView = itemView.findViewById(R.id.taskTitle)
        val tags = itemView.findViewById<TextView>(R.id.tvTaskTags)
        val attachments: TextView = itemView.findViewById(R.id.taskAttachments)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(context)
        }
        val view = mLayoutInflater.inflate(R.layout.rv_allupcoming_task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
//        val date = task.due_date.split("-")
        val date = "25-11-2023".split("-")
        holder.date.text =
            context.getString(
                R.string.upcom_task_date, date[0] ,
                viewModel.getMonthShortForm(date[1].toInt()))
        holder.duration.text = context.getString(R.string.upcom_dur, task.start_time, task.end_time)
        holder.title.text = task.title
        var string = ""
        for (i in task.taskTypes) {
            string = string + i + ", "
        }
        holder.tags.text = string
        if (task.attachments.size > 1) {
            holder.attachments.text = context.getString(R.string.upcom_attachments , task.attachments.size , "Attachments")
        }
        else {
            holder.attachments.text = context.getString(R.string.upcom_attachments , task.attachments.size , "Attachment")
        }
    }

    override fun getItemCount() = tasks.size

    fun setData(newList : List<Task>){
        val diffCallBack = TaskDiffCallBack(tasks , newList as ArrayList<Task>)
        val diffTask = DiffUtil.calculateDiff(diffCallBack)
        tasks.clear()
        tasks.addAll(newList)
        diffTask.dispatchUpdatesTo(this)
    }
}