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

class TaskTypePopUpWindow() {
    var taskCategories = mutableListOf("Assignment", "Project", "Coding",
        "Classes", "Hobby" , "Meeting" , "Playing", "Hangout" , "Food", "Television",
        "Exercise" , "Remainder" , "Other")
    val TAG = "PopUpWindow"
    var adapter : ArrayAdapter<String>? = null
    var inflater : LayoutInflater? = null
    var itemsSelected = 0
    var tvTaskLimitError : TextView? = null
    var btnSelectTasks : Button? = null
    var searchView : SearchView? = null
    var taskCL: ConstraintLayout? = null
    var txtTasks: TextView? = null
    var firstView = true
    var clWidth  = 0
    var totalWidth = 0
    // first element id added in Constraint layout
    // if the total width so far has exceeded constraint layout width then we will position the new view
    // below the first view hence first element is stored in this variable
    var firstId = 0
    var prevId = 0 //previous view added id
    var listView : ListView? = null
    var typeList = mutableListOf<String>()
    var popUpWindow : PopupWindow? = null
    // Function to show pop window
    fun showPopUpWindow(view : View){
        inflater = LayoutInflater.from(view.context)
        val popView = inflater!!.inflate(R.layout.task_type_pop_up_window_layout , null)
        popUpWindow = PopupWindow(popView , WindowManager.LayoutParams.WRAP_CONTENT , WindowManager.LayoutParams.WRAP_CONTENT , true)
        popUpWindow!!.isOutsideTouchable = true
        popUpWindow!!.isFocusable = true
        popUpWindow!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popUpWindow!!.showAtLocation(view, Gravity.CENTER , 0 , 0)
        taskCL = popView.findViewById(R.id.popUpTaskCL)
        searchView = popView.findViewById(R.id.popUpTaskSearchView)
        txtTasks = popView.findViewById(R.id.popUptxtTaskType)
        btnSelectTasks = popView.findViewById(R.id.popBtnSelectTasks)
        tvTaskLimitError = popView.findViewById(R.id.tvTaskLimitError)
        //for measuring the width of popUpWindow
        popView.measure(MeasureSpec.makeMeasureSpec(0 , MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0 , MeasureSpec.UNSPECIFIED))
        clWidth = popView.measuredWidth - 30
        totalWidth = 0 //tatal width of elements added in constraint layout so far
        typeList.clear()
        listView = popView.findViewById(R.id.popTasksListView)
        adapter = ArrayAdapter(view.context , android.R.layout.simple_list_item_1 , taskCategories)
        listView!!.adapter = adapter
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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
        listView!!.setOnItemClickListener { adapterView, v, i, l ->

            itemsSelected++
            if(itemsSelected > 4){
                // If the task type selected is more than 4
                // then we are showing a warning
                tvTaskLimitError!!.visibility = View.VISIBLE
                return@setOnItemClickListener
            }
            addViewToConstraintLayout(view , i)
        }
    }
    // Function to task type view to constraint layout
    private fun addViewToConstraintLayout(view : View , ind : Int){
        //changing the text inside the textview according to index 'i' of taskcategories list
        val taskView = inflater!!.inflate(R.layout.task_type_item , null)
        val id = View.generateViewId()
        taskView.id = id
        val taskName = taskView.findViewById<TextView>(R.id.task_type_item_name)
        taskName.text = taskCategories[ind]
        typeList.add(taskCategories[ind])
        taskCategories.removeAt(ind)
        adapter!!.notifyDataSetChanged()
        taskName.background = ContextCompat.getDrawable(view.context , R.drawable.dates_selected_background_stroke)
        taskName.setTextColor(ContextCompat.getColor(view.context , R.color.black))
        taskCL!!.addView(taskView)
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
            txtTasks!!.text = "$itemsSelected task type selected"
            totalWidth = totalWidth  + taskView.width
        }
    }
}