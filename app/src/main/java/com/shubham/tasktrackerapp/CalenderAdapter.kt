package com.shubham.tasktrackerapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalenderAdapter(val list : ArrayList<CalenderDateModel>) : RecyclerView.Adapter<CalenderAdapter.CalenderViewHolder>() {
    private val dates = ArrayList<CalenderDateModel>()
    inner class CalenderViewHolder(view : View) : RecyclerView.ViewHolder(view){
        fun bind(calenderDateModel: CalenderDateModel){
            Log.d("listCA" , list.toString())
            val txt_day = itemView.findViewById<TextView>(R.id.rv_item_day)
            val txt_date = itemView.findViewById<TextView>(R.id.rv_item_date)
            val date = calenderDateModel.date.date.toString()
            var day = calenderDateModel.date.day.toString()
            when(day){
                "0" -> day = "Mon"
                "1" -> day = "Tue"
                "2" -> day = "Wed"
                "3" -> day = "Thu"
                "4" -> day = "Fri"
                "5" -> day = "Sat"
                "6" -> day = "Sun"
            }
            txt_day.text = day
            txt_date.text = date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalenderViewHolder {
        Log.d("onCreateVH" , "yes")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_dates_item , parent , false);
        return CalenderViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalenderViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
    fun setDates(calenderList : ArrayList<CalenderDateModel>){
        dates.clear()
        dates.addAll(calenderList)
        notifyDataSetChanged()
    }
}