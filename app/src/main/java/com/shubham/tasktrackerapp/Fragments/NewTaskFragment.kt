package com.shubham.tasktrackerapp.Fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.shubham.tasktrackerapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewTaskFragment : Fragment(R.layout.fragment_new_task) {
    val TAG = "NewTaskFragment"
    var mContext = activity?.applicationContext
    val colors = arrayOf(
        Color.parseColor("#28B463"), // green
        Color.parseColor("#2E86C1"), // blue
        Color.parseColor("#7D3C98"), //purple
        Color.parseColor("#E67E22"), //dark orange
    )
    var firstType = true
    var prevId = 0
    var firstId = 0
    var firstIdConstant = 0
    var clTotalWidth = 0
    var ind = 0
    var constraintLayout : ConstraintLayout? = null
    var clWidth = 0
    var addImgWidth = 0
    var clickListener : View.OnClickListener? = null
    var taskCategories = mutableListOf<String>("Assignment", "Project", "Coding",
        "Classes", "Hobby" , "Meeting" , "Playing", "Hangout" , "Food", "Television",
    "Exercise" , "Remainder" , "Other")
    val startToEndIdMap = hashMapOf<Int , Int>()
    val endToStartIdMap = hashMapOf<Int , Int>()
    val constraintSet = ConstraintSet()
    var addTask : View? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addTask = view.findViewById<ImageView>(R.id.iv_add_task)
        // constraint layout for placing task type categories
        constraintLayout = view.findViewById<ConstraintLayout>(R.id.task_type_constraintLayout)
        clWidth = constraintLayout!!.width
        addImgWidth = addTask!!.width

        constraintLayout!!.post{ clWidth = constraintLayout!!.width }
        addTask!!.post { addImgWidth  = addTask!!.width }
        val taskPopUpWindow = TaskTypePopUpWindow()

        addTask!!.setOnClickListener{ view1 ->
            taskPopUpWindow.taskCategories = taskCategories
            taskPopUpWindow.showPopUpWindow(view1)
            taskPopUpWindow.btnSelectTasks!!.setOnClickListener {
                val types = taskPopUpWindow.typeList
                Log.d(TAG , types.toString())
                taskPopUpWindow.popUpWindow!!.dismiss()
                addViewToConstraintLayout(it , types)
                Log.d(TAG , " idmap - ${startToEndIdMap.toString()}")
            }
        }
        clickListener = View.OnClickListener {
            removeViewById(it)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    private fun addViewToConstraintLayout(view : View , types: MutableList<String>){
        GlobalScope.launch (Dispatchers.Main){
            addTask!!.visibility = View.INVISIBLE
            for(i in 0..types.size-1){
                taskCategories.remove(types[i])

                val taskTypeItemView = layoutInflater.inflate(R.layout.task_type_item , null)
                val textView = taskTypeItemView.findViewById<TextView>(R.id.task_type_item_name)
                textView.text = types[i]

                DrawableCompat.setTint(textView.background , colors[ind])
                ind++
                if (ind == 4) ind = 0
                Log.d(TAG , "type - ${types[i]} time - ${System.currentTimeMillis()}")
//            Log.d(TAG , "addViewTOCL - $type")

                val id = View.generateViewId()
                taskTypeItemView.id = id
                taskTypeItemView.setOnClickListener(clickListener)

                constraintLayout!!.addView(taskTypeItemView)

                //constraint set for setting the constrains for new task type added

                constraintSet.clone(constraintLayout)

                taskTypeItemView.post{
                    if(firstType){
                        firstIdConstant = id
                        firstId = id
                        prevId = id
                        firstType = false
                        constraintSet.connect(id , ConstraintSet.LEFT , ConstraintSet.PARENT_ID , ConstraintSet.LEFT)
                        constraintSet.connect(id , ConstraintSet.TOP , ConstraintSet.PARENT_ID , ConstraintSet.TOP)
                        startToEndIdMap.put(id , ConstraintSet.PARENT_ID)
//                Toast.makeText(mContext , "view width - ${view.width}" , Toast.LENGTH_SHORT).show()
                    }
                    else {
                        if (clTotalWidth + taskTypeItemView.width + 20 < clWidth){
                            constraintSet.connect(id , ConstraintSet.START , prevId , ConstraintSet.END , 20)
                            constraintSet.connect(id , ConstraintSet.TOP , prevId , ConstraintSet.TOP)
                            constraintSet.connect(id , ConstraintSet.BOTTOM , prevId , ConstraintSet.BOTTOM)
                            clTotalWidth += 20
                            startToEndIdMap.put(id , prevId)
                            endToStartIdMap.put(prevId , id)
                        }
                        else {
                            clTotalWidth = 0
                            constraintSet.connect(id , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START )
                            constraintSet.connect(id , ConstraintSet.TOP , firstId , ConstraintSet.BOTTOM ,20)
                            firstId = id
                            startToEndIdMap.put(id , prevId)
                        }
                        prevId = id
                    }
                    Log.d(TAG , "${types[i]} constraint done , time - ${System.currentTimeMillis()}")
                    clTotalWidth = clTotalWidth + taskTypeItemView.width
                    //Toast.makeText(mContext , "clTotalWidth = $clTotalWidth clWidth = $clWidth", Toast.LENGTH_SHORT).show()

                    if(i == types.size-1){
                        if(clTotalWidth + addImgWidth + 20 < clWidth){
                            constraintSet.connect(R.id.iv_add_task , ConstraintSet.START , id , ConstraintSet.END , 20)
                            constraintSet.connect(R.id.iv_add_task , ConstraintSet.TOP , id , ConstraintSet.TOP)
                            constraintSet.connect(R.id.iv_add_task , ConstraintSet.BOTTOM , id , ConstraintSet.BOTTOM)
                            clTotalWidth = clTotalWidth + 20 + addImgWidth
                            startToEndIdMap.put(R.id.iv_add_task , id)
                            endToStartIdMap.put(id , R.id.iv_add_task)
                        }
                        else {
                            clTotalWidth = addImgWidth
                            constraintSet.connect(R.id.iv_add_task , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START)
                            constraintSet.connect(R.id.iv_add_task , ConstraintSet.TOP , firstId , ConstraintSet.BOTTOM , 20)
                            firstId = R.id.iv_add_task
                            startToEndIdMap.put(R.id.iv_add_task , ConstraintSet.PARENT_ID)
                        }

                        //addTask!!.visibility = View.VISIBLE
                        constraintSet.applyTo(constraintLayout)
                    }
                    constraintSet.applyTo(constraintLayout)
                }
                delay(50)
            }
            addTask!!.visibility = View.VISIBLE
            Log.d(TAG , " startToEndIdmap - ${startToEndIdMap}")
            Log.d(TAG , "endToStartIdmap - ${endToStartIdMap} ")
        }
    }
    private fun removeViewById(view : View){
        constraintSet.clone(constraintLayout)
        val prevViewId = startToEndIdMap[view.id]
        val nextViewId = endToStartIdMap[view.id]
//        Toast.makeText(mContext , "f-$firstId v-${view.id} fc-$firstIdConstant p-$prevViewId n-$nextViewId" , Toast.LENGTH_SHORT).show()
        Log.d(TAG , "prevViewId - $prevViewId nextViewId - $nextViewId")
        if(nextViewId != null){
            if(view.id >= firstId){
                Log.d(TAG , "view.id greater than firstId v-${view.id} fid-${firstId}")
                if(view.id == firstId){
                    constraintSet.clear(nextViewId , ConstraintSet.BOTTOM)
                    constraintSet.connect(nextViewId , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START)
                    constraintSet.connect(nextViewId, ConstraintSet.TOP , firstIdConstant , ConstraintSet.BOTTOM)
                    startToEndIdMap[nextViewId] = ConstraintSet.PARENT_ID
                    firstId = nextViewId
                }
                else {
                    if(prevViewId != null){
                        constraintSet.connect(nextViewId , ConstraintSet.START , prevViewId , ConstraintSet.END)
                        constraintSet.connect(nextViewId , ConstraintSet.TOP , prevViewId , ConstraintSet.TOP)
                        constraintSet.connect(nextViewId , ConstraintSet.BOTTOM , prevViewId , ConstraintSet.BOTTOM)
                        startToEndIdMap[nextViewId] = prevViewId
                        endToStartIdMap[prevViewId] = nextViewId
                    }
                }
            }
            else {
                if(view.id == firstIdConstant){
                    constraintSet.clear(nextViewId , ConstraintSet.BOTTOM)
                    constraintSet.connect(nextViewId , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START)
                    constraintSet.connect(nextViewId, ConstraintSet.TOP , ConstraintSet.PARENT_ID , ConstraintSet.TOP)
                    startToEndIdMap[nextViewId] = ConstraintSet.PARENT_ID
                    firstIdConstant = nextViewId
                }
                else {
                    if(prevViewId != null){
                        constraintSet.connect(nextViewId , ConstraintSet.START , prevViewId , ConstraintSet.END)
                        constraintSet.connect(nextViewId , ConstraintSet.TOP , prevViewId , ConstraintSet.TOP)
                        constraintSet.connect(nextViewId , ConstraintSet.BOTTOM , prevViewId , ConstraintSet.BOTTOM)

                        startToEndIdMap[nextViewId] = prevViewId
                        endToStartIdMap[prevViewId] = nextViewId
                    }
                }
                if(firstId != firstIdConstant){
                    Log.d(TAG , "firstId!=firstIdConstant")
                    val nextToFirstId = endToStartIdMap[firstId]
                    constraintSet.connect(firstId , ConstraintSet.START ,  firstId-1, ConstraintSet.END)
                    constraintSet.connect(firstId , ConstraintSet.TOP , firstId-1, ConstraintSet.TOP)
                    constraintSet.connect(firstId , ConstraintSet.BOTTOM , firstId-1 , ConstraintSet.BOTTOM)

                    startToEndIdMap[firstId] = firstId-1
                    endToStartIdMap[firstId-1] = firstId
                    endToStartIdMap[firstId] = 20

                    firstId = firstIdConstant
                    if(nextToFirstId != null){
                        val name = constraintLayout!!.findViewById<View>(firstIdConstant).findViewById<TextView>(R.id.task_type_item_name).text
                        Log.d(TAG , "nextToFirstId not null - $nextToFirstId and firstIdConstant - $name")
                        constraintSet.connect(nextToFirstId , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START)
                        constraintSet.connect(nextToFirstId , ConstraintSet.TOP , firstIdConstant , ConstraintSet.BOTTOM )
                        firstId = nextToFirstId
                        startToEndIdMap[nextToFirstId] = 0
                    }
                }
            }
        }
        else {
            if(prevViewId!=null){
                val nextToFirstId = endToStartIdMap[firstId]
                constraintSet.connect(firstId , ConstraintSet.START ,  prevViewId, ConstraintSet.END)
                constraintSet.connect(firstId , ConstraintSet.TOP , prevViewId, ConstraintSet.TOP)
                constraintSet.connect(firstId , ConstraintSet.BOTTOM , prevViewId , ConstraintSet.BOTTOM)
                startToEndIdMap[firstId] = prevViewId
                endToStartIdMap[firstId] = 20
                endToStartIdMap[prevViewId] = firstId

                firstId = firstIdConstant
                if(nextToFirstId != null){
                    constraintSet.connect(nextToFirstId , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START)
                    constraintSet.connect(nextToFirstId , ConstraintSet.TOP , firstIdConstant , ConstraintSet.BOTTOM)
                    firstId = nextToFirstId
                    startToEndIdMap[nextToFirstId] = 0
                }
            }
        }
        constraintLayout!!.removeView(view)
        constraintSet.applyTo(constraintLayout)
    }
}