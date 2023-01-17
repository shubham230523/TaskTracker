package com.shubham.tasktrackerapp.util

import androidx.recyclerview.widget.DiffUtil
import com.shubham.tasktrackerapp.data.local.Task

/**
 * CallBack for calculating the difference between two lists
 */
class TaskDiffCallBack(
    private val oldList: ArrayList<Task>,
    private val newList: ArrayList<Task>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].title == newList[newItemPosition].title
                && oldList[oldItemPosition].due_date == newList[newItemPosition].due_date
                && oldList[oldItemPosition].start_time == newList[newItemPosition].start_time
                && oldList[oldItemPosition].end_time == newList[newItemPosition].end_time
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].taskTypes == newList[newItemPosition].taskTypes
                && oldList[oldItemPosition].attachments == newList[newItemPosition].attachments
    }
}