package com.shubham.tasktrackerapp.dashboard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.data.local.Task

class MissedTasksAdapter(private val context: Context, private val list: List<Task>)
    : RecyclerView.Adapter<MissedTasksAdapter.TaskViewHolder>() {
    companion object {
        private const val TAG = "MissedTasksAdapter"
    }

    private lateinit var mLayoutInflater: LayoutInflater

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvMissedTaskTitle)
        val dueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        val tags: TextView = itemView.findViewById(R.id.tvTags)
        val reason: TextView = itemView.findViewById(R.id.tvReason)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(context)
        }
        val view = mLayoutInflater.inflate(R.layout.rv_missed_task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = list[position]
        holder.title.text = task.title
        holder.dueDate.text = task.due_date
        var tags = task.taskTypes[0]
        if (task.taskTypes.size > 1) {
            tags += ", ${task.taskTypes[1]}"
            if (task.taskTypes.size > 2) {
                tags += " +${task.taskTypes.size - 2} more"
            }
        }
        Log.d(TAG, "tags - $tags")
        holder.tags.text = tags
        holder.reason.text = "Missed"
    }

    override fun getItemCount() = list.size
}