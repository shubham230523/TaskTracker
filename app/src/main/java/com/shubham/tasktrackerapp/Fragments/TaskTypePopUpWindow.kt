package com.shubham.tasktrackerapp.Fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.shubham.tasktrackerapp.R


class TaskTypePopUpWindow {
    val taskCategories = listOf(
        "Assignment",
        "Project",
        "Coding",
        "Classes",
        "Hobby",
        "Project",
        "Meeting",
        "Playing",
        "Hangout",
        "Food",
        "Television",
        "Exercise",
        "Remainder",
        "Other"
    )
    val TAG = "PopUpWindow"
    var adapter : ArrayAdapter<String>? = null
    var inflater : LayoutInflater? = null

    fun showPopUpWindow(view : View){
        inflater = LayoutInflater.from(view.context)
        val popView = inflater!!.inflate(R.layout.task_type_pop_up_window_layout , null)

        val popUpWindow = PopupWindow(popView , WindowManager.LayoutParams.WRAP_CONTENT , WindowManager.LayoutParams.WRAP_CONTENT , true)
        popUpWindow.isOutsideTouchable = true
        popUpWindow.isFocusable = true
        popUpWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popUpWindow.showAtLocation(view, Gravity.CENTER , 0 , 0)

        val taskCL = popView.findViewById<ConstraintLayout>(R.id.popUpTaskCL)
        val searchView = popView.findViewById<SearchView>(R.id.popUpTaskSearchView)
        val txtTasks = popView.findViewById<TextView>(R.id.popUptxtTaskType)
        val btnSelectTasks = popView.findViewById<Button>(R.id.popBtnSelectTasks)
        val tvTaskLimitError = popView.findViewById<TextView>(R.id.tvTaskLimitError)
        var firstView = true
        var itemsSelected = 0

        //for measuring the width of popUpWindow
        popView.measure(MeasureSpec.makeMeasureSpec(0 , MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0 , MeasureSpec.UNSPECIFIED))
        val clWidth = popView.measuredWidth - 30
        var totalWidth = 0 //tatal width of elements added in constraint layout so far


        // first element id added in Constraint layout
        // if the total width so far has exceeded , constraint layout width then we are position the new view
        // below the first view hence first element is stored in this variable
        var firstId = 0
        var prevId = 0 //previous view added id


        val listView = popView.findViewById<ListView>(R.id.popTasksListView)
        adapter = ArrayAdapter(view.context , android.R.layout.simple_list_item_1 , taskCategories)
        listView.adapter = adapter

        btnSelectTasks.setOnClickListener {
            Toast.makeText(view.context , "btn select clicked" , Toast.LENGTH_SHORT).show()
            popUpWindow.dismiss()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter!!.filter.filter(newText)
                return false
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if(taskCategories.contains(query)){
                    adapter!!.filter.filter(query)
                }
                else {
                    Toast.makeText(view.context , "Not found" , Toast.LENGTH_SHORT).show()
                }
                return false
            }
        })

        listView.setOnItemClickListener { adapterView, v, i, l ->
            //changing the text inside the textview according to index 'i' of taskcategories list
            itemsSelected++
            if(itemsSelected > 4){
                tvTaskLimitError.visibility = View.VISIBLE
                return@setOnItemClickListener
            }
            val taskView = inflater!!.inflate(R.layout.task_type_item , null)
            val id = View.generateViewId()
            taskView.id = id

            val taskName = taskView.findViewById<TextView>(R.id.task_type_item_name)
            taskName.text = taskCategories[i]
            taskName.background = ContextCompat.getDrawable(view.context , R.drawable.dates_selected_background_stroke)
            taskName.setTextColor(ContextCompat.getColor(view.context , R.color.black))
            taskCL.addView(taskView)

            val constraintSet = ConstraintSet()
            constraintSet.clone(taskCL)
            taskView.post{
                if(firstView){
                    firstView = false
                    firstId = id
                    prevId = id
                    constraintSet.connect(id , ConstraintSet.LEFT , ConstraintSet.PARENT_ID , ConstraintSet.LEFT)
                    constraintSet.connect(id , ConstraintSet.TOP , ConstraintSet.PARENT_ID , ConstraintSet.TOP)
                }
                else {
                    if (totalWidth + taskView.width + 20 < clWidth){
                        constraintSet.connect(id , ConstraintSet.START , prevId , ConstraintSet.END , 20)
                        constraintSet.connect(id , ConstraintSet.TOP , prevId , ConstraintSet.TOP)
                        constraintSet.connect(id , ConstraintSet.BOTTOM , prevId , ConstraintSet.BOTTOM)
                        totalWidth += 20
                    }
                    else {
                        totalWidth = 0
                        constraintSet.connect(id , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START )
                        constraintSet.connect(id , ConstraintSet.TOP , firstId , ConstraintSet.BOTTOM ,20)
                        firstId = id
                    }
                    prevId = id
                }

                constraintSet.applyTo(taskCL)
                totalWidth = totalWidth  + taskView.width
            Toast.makeText(view.context , "clTotalWidth = $totalWidth clWidth = $clWidth", Toast.LENGTH_SHORT).show()
            }
        }
    }
}