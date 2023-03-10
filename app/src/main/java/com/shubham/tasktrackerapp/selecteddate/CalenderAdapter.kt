package com.shubham.tasktrackerapp.selecteddate

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.shubham.tasktrackerapp.R

/**
 * Adapter for showing dates recycler view on top of home fragment
 * This dates are use to update the tasks in the time line according to the date
 */
class CalenderAdapter(
    val list: MutableList<CalenderDateModel>,
    val context: Context,
    private val onItemClicked: (position: Int) -> Unit
) :
    RecyclerView.Adapter<CalenderAdapter.CalenderViewHolder>() {
    inner class CalenderViewHolder(view: View, private val onItemClicked: (position: Int) -> Unit) :
        RecyclerView.ViewHolder(view), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        fun bind(date: CalenderDateModel) {
            val txtDay = itemView.findViewById<TextView>(R.id.rv_item_day)
            val txtDate = itemView.findViewById<TextView>(R.id.rv_item_date)
            // setting day according to number
            when (date.day) {
                "0" -> txtDay.text = "Sun"
                "1" -> txtDay.text = "Mon"
                "2" -> txtDay.text = "Tue"
                "3" -> txtDay.text = "Wed"
                "4" -> txtDay.text = "Thu"
                "5" -> txtDay.text = "Fri"
                "6" -> txtDay.text = "Sat"
            }
            txtDate.text = date.date
            if (date.selected) {
                txtDay.setTextColor(ContextCompat.getColor(context, R.color.white))
                txtDate.setTextColor(ContextCompat.getColor(context, R.color.white))
                itemView.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.dates_selected_background_stroke
                )
            } else {
                txtDay.setTextColor(ContextCompat.getColor(context, R.color.grey))
                txtDate.setTextColor(ContextCompat.getColor(context, R.color.black))
                itemView.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.dates_unselected_background_stroke
                )
            }
        }

        override fun onClick(p0: View?) {
            onItemClicked(adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalenderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_dates_item, parent, false);
        return CalenderViewHolder(view, onItemClicked)
    }

    override fun onBindViewHolder(holder: CalenderViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * Function to change the selected date by changing the selected boolean variable
     */
    fun changeSelectedDate(lastSelectedPosition: Int, newSelectedPosition: Int) {
        list[lastSelectedPosition].selected = false
        list[newSelectedPosition].selected = true;
        notifyItemChanged(lastSelectedPosition)
        notifyItemChanged(newSelectedPosition)
    }
}