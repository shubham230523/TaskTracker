package com.shubham.tasktrackerapp.Fragments

import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.shubham.tasktrackerapp.R
import kotlin.random.Random

class NewTaskFragment : Fragment(R.layout.fragment_new_task) {
    val TAG = "NewTaskFragment"
    var mContext = activity?.applicationContext
    val colors = arrayOf(
        Color.parseColor("#28B463"), // green
        Color.parseColor("#2E86C1"), // blue
        Color.parseColor("#7D3C98"), //purple
        Color.parseColor("#BA4A00"), //dark orange
    )
    var firstType = true
    var prevId = 0
    var firstId = 0
    var clTotalWidth = 0
    var ind = 0
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val addTask = view.findViewById<ImageView>(R.id.iv_add_task)
        // constraint layout for placing task type categories
        val constraintLayout = view.findViewById<ConstraintLayout>(R.id.task_type_constraintLayout)
        var clWidth = constraintLayout.width
        var addImgWidth = addTask.width

        constraintLayout.post{ clWidth = constraintLayout.width }
        addTask.post { addImgWidth  = addTask.width }

        addTask.setOnClickListener{
            val view = layoutInflater.inflate(R.layout.task_type_item , null)
            val textView = view.findViewById<TextView>(R.id.task_type_item_name)

            DrawableCompat.setTint(textView.background , colors[0])
            ind++
            if (ind == 4) ind = 0

            val id = View.generateViewId()
            view.id = id
            constraintLayout.addView(view)

            //constraint set for setting the constrains for new task type added
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)

            val taskPopUpWindow = TaskTypePopUpWindow()
            taskPopUpWindow.showPopUpWindow(it)


            view.post{
                if(firstType){
                    firstId = id
                    prevId = id
                    firstType = false
                    constraintSet.connect(id , ConstraintSet.LEFT , ConstraintSet.PARENT_ID , ConstraintSet.LEFT)
                    constraintSet.connect(id , ConstraintSet.TOP , ConstraintSet.PARENT_ID , ConstraintSet.TOP)
//                Toast.makeText(mContext , "view width - ${view.width}" , Toast.LENGTH_SHORT).show()
                }
                else {
                    if (clTotalWidth + view.width + 20 < clWidth){
                        constraintSet.connect(id , ConstraintSet.START , prevId , ConstraintSet.END , 20)
                        constraintSet.connect(id , ConstraintSet.TOP , prevId , ConstraintSet.TOP)
                        constraintSet.connect(id , ConstraintSet.BOTTOM , prevId , ConstraintSet.BOTTOM)
                        clTotalWidth += 20
                    }
                    else {
                        clTotalWidth = 0
                        constraintSet.connect(id , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START )
                        constraintSet.connect(id , ConstraintSet.TOP , firstId , ConstraintSet.BOTTOM ,20)
                        firstId = id
                    }
                    prevId = id
                }
                constraintSet.connect(R.id.iv_add_task , ConstraintSet.START , id , ConstraintSet.END , 20)
                constraintSet.connect(R.id.iv_add_task , ConstraintSet.TOP , id , ConstraintSet.TOP)
                constraintSet.connect(R.id.iv_add_task , ConstraintSet.BOTTOM , id , ConstraintSet.BOTTOM)
                constraintSet.applyTo(constraintLayout)
                clTotalWidth = clTotalWidth + addImgWidth + 20 + view.width
//            Toast.makeText(mContext , "clTotalWidth = $clTotalWidth clWidth = $clWidth", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
}