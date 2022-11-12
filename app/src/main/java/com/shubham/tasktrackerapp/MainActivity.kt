package com.shubham.tasktrackerapp

import android.Manifest
import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.shubham.tasktrackerapp.Fragments.DashboardFragment
import com.shubham.tasktrackerapp.Fragments.HomeFragment
import com.shubham.tasktrackerapp.Fragments.TaskTypePopUpWindow
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {
    var cardView : CardView? = null
    var firstType = true
    var prevId = 0
    var firstId = 0
    var firstIdConstant = 0 // It points to first task type item and doesn't change
    var clTotalWidth = 0 // task type constraint layout width covered by elements
    var ind = 0
    var taskTypeCount = 0
    var constraintLayout : ConstraintLayout? = null
    var clWidth = 0 // task type constraint layout total width
    var clickListener : View.OnClickListener? = null
    // Map for storing constraints in the following format
    // key = View.Constraint.Start , Value = View.Constraint.End
    val startToEndIdMap = hashMapOf<Int , Int>()
    // Map for storing constraints in the following format
    // key = View.Constraint.End , Value = View.Constraint.Start
    val endToStartIdMap = hashMapOf<Int , Int>()
    var constraintSet = ConstraintSet()
    var addTask : View? = null
    var btnAddAttachments : ImageView? = null
    var attachmentView : View? = null
    var attachmentsConstraintLayout : ConstraintLayout? = null
    var prevAttachmentId = -1;
    var attachmentCount = 0
    var firstAttachmentView : View? = null
    var secondAttachmentView : View? = null
    var ivBgAddTask : ImageView? = null
    var tvBgAddTask : TextView? = null
    var ivBgNoUploads : ImageView? = null
    var tvBgNoUploads : TextView? = null
    var btnCardClose : ImageView? = null
    var btnCreateTask : MaterialButton? = null
    var fragmentContainer : FragmentContainerView? = null
    var job : Job? = null
    var tvStartTime : TextView? = null
    var tvEndTime : TextView? = null
    var ivCalender: ImageView? = null
    var ivstartClock : ImageView? = null
    var ivEndClock : ImageView? = null
    var tvDueDate : TextView? = null
    val cal = Calendar.getInstance(Locale.ENGLISH)
    val TAG = "taskTag"
    val uriList  = mutableListOf<Uri>()
    val selectedTypeList = mutableListOf<String>()
    val taskPopUpWindow = TaskTypePopUpWindow() // a popUpWindow for selecting task type

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottom_nav)
        var cardView = findViewById<CardView>(R.id.new_task_cv)
        var btnCardClose = findViewById<ImageView>(R.id.iv_close)
        fragmentContainer = findViewById(R.id.fragment_container)
        bottomNavBar.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container , HomeFragment()).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_new_task -> {
                    job = GlobalScope.launch (Dispatchers.Main){
                        ObjectAnimator.ofFloat(cardView , "translationY" , resources.getDimensionPixelOffset(R.dimen.cardViewHeight) * 1f + 70).apply {
                            duration = 1500
                            start()
                        }
                        delay(1000)
                        fragmentContainer!!.foreground = ContextCompat.getDrawable(this@MainActivity , R.drawable.bg_transparent)
                        delay(500)
                        job?.cancelAndJoin()
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.nav_dashboard -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container , DashboardFragment()).commit()
                    return@setOnItemSelectedListener true
                }
            }
            true
        }
        btnCardClose.setOnClickListener {
            job = GlobalScope.launch (Dispatchers.Main){
                fragmentContainer!!.foreground = null
                ObjectAnimator.ofFloat(cardView , "translationY" , resources.getDimensionPixelOffset(R.dimen.cardViewHeight) * -1f + 70).apply {
                    duration = 1500
                    start()
                }
                delay(500)
                delay(1000)
                job?.cancelAndJoin()
            }
        }
        btnCreateTask = findViewById(R.id.btnCreateTask)
        btnCreateTask!!.setOnClickListener {
            createTask()
            job = GlobalScope.launch (Dispatchers.Main){
                fragmentContainer!!.foreground = null
                ObjectAnimator.ofFloat(cardView , "translationY" , resources.getDimensionPixelOffset(R.dimen.cardViewHeight) * -1f + 70).apply {
                    duration = 1500
                    start()
                }
                delay(500)
                delay(1000)
                job?.cancelAndJoin()
            }
        }
        addTask = findViewById<ImageView>(R.id.iv_add_task)
        btnAddAttachments = findViewById(R.id.ivAddAttachments)
        attachmentsConstraintLayout = findViewById(R.id.clAddAttachments)
        // constraint layout for placing task type categories
        constraintLayout = findViewById(R.id.task_type_constraintLayout)
        clWidth = constraintLayout!!.width
        constraintLayout!!.post{ clWidth = constraintLayout!!.width }
        ivBgAddTask = findViewById(R.id.ivAddTaskTypeBackground)
        tvBgAddTask = findViewById(R.id.tvNoTaskTypeBackground)
        ivBgNoUploads = findViewById(R.id.ivUploadFile)
        tvBgNoUploads = findViewById(R.id.txtNoUploads)
        tvDueDate = findViewById(R.id.tv_due_date)
        ivCalender = findViewById(R.id.iv_calendar)
        tvStartTime = findViewById(R.id.tv_start_time)
        ivstartClock = findViewById(R.id.iv_clock_start)
        tvEndTime = findViewById(R.id.tv_end_time)
        ivEndClock = findViewById(R.id.iv_clock_end)
        addTask!!.setOnClickListener{ view1 ->
            Log.d(TAG , "taskCategories - ${taskPopUpWindow.taskCategories.size}")
            Log.d(TAG , "itemSelected - ${taskPopUpWindow.itemsSelected}")
            Log.d(TAG , "typeList - ${taskPopUpWindow.typeList}")

            if(taskTypeCount < 4){
                //submitting the taskCategories list to popWindow object
                taskPopUpWindow.showPopUpWindow(view1)
                taskPopUpWindow.btnSelectTasks!!.setOnClickListener{
                    val types = taskPopUpWindow.typeList
                    Log.d(TAG , "types selected - $types")
                    taskTypeCount = taskPopUpWindow.itemsSelected
                    taskPopUpWindow.popUpWindow!!.dismiss()
                    addViewToConstraintLayout(it , types)
                }
            }
            else Toast.makeText(this , "Cannot select more than 4 task type" , Toast.LENGTH_SHORT).show()
        }
        clickListener = View.OnClickListener {v->
            Log.d(TAG , "view.id = ${v.id}")
            when(v.id){
                R.id.iv_calendar, R.id.tv_due_date -> { pickDate() }
                R.id.iv_clock_start, R.id.tv_start_time ->{
                    Log.d(TAG , "select time called $tvStartTime")
                    selectTime(tvStartTime!!)
                }
                R.id.iv_clock_end, R.id.tv_end_time-> { selectTime(tvEndTime!!)}
                R.id.ivRemoveAttachment -> removeAttachment(v)
                else -> removeViewById(v)
            }
        }
        tvDueDate!!.setOnClickListener(clickListener)
        ivCalender!!.setOnClickListener(clickListener)
        tvStartTime!!.setOnClickListener(clickListener)
        ivstartClock!!.setOnClickListener(clickListener)
        tvEndTime!!.setOnClickListener(clickListener)
        ivEndClock!!.setOnClickListener(clickListener)
        //btn for adding attachments
        btnAddAttachments!!.setOnClickListener {
            if(attachmentCount == 2) {
                Toast.makeText(this , "You can upload at most 2 files" , Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(
                ActivityCompat.checkSelfPermission(
                    this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )!= PackageManager.PERMISSION_GRANTED
            ){
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
            else {
                selectFile();
            }
        }
    }
    // Function to pick a date
    private fun pickDate(){
        DatePickerDialog(
            this,
            { p0, year, month, day ->
                tvDueDate!!.text = "$day-$month-$year"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    // Function to select time
    private fun selectTime(tv: TextView){
        TimePickerDialog(this ,
            { timePickerView, hourOfDay, minute -> tv.text = "${hourOfDay}:${minute}"},
        cal.get(Calendar.HOUR_OF_DAY),
        cal.get(Calendar.MINUTE),
        false
        ).show()
    }
    // Function to selectFile from the local storage
    private fun selectFile(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        openFileResultLauncher.launch(intent)
    }
    // A launcher for checking whether storage permission is granted or not
    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted ->
        if(isGranted) {
            selectFile()
        }
        else {
            Toast.makeText(this, "Permission denied" ,Toast.LENGTH_SHORT).show()
        }
    }
    // A launcher for getting the uri from the data
    val openFileResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ){ result ->
        val data : Intent? = result?.data
        if(data!=null){
            val uri = data.data
            val path = uri?.path
            if (uri != null) {
                val filename = getNameFromUri(uri)
                uriList!!.add(uri)
                createAttachment(filename)
            }
            else Toast.makeText(this, "Some error occurred, try again" , Toast.LENGTH_SHORT).show()
        }
        else Toast.makeText(this, "Some error occurred, try again" , Toast.LENGTH_SHORT).show()
    }
    // Function to remove attachment
    private fun removeAttachment(v : View){
        val tag = v.tag
        if(tag == "FirstAttachment"){
            uriList.removeAt(0)
            Log.d(TAG , "FirstAttachmentView deleted")
            attachmentsConstraintLayout!!.removeView(firstAttachmentView)
            if(secondAttachmentView!= null) {
                constraintSet.clone(attachmentsConstraintLayout)
                constraintSet.connect(secondAttachmentView!!.id , ConstraintSet.TOP , ConstraintSet.PARENT_ID , ConstraintSet.TOP)
            }
        }
        else {
            uriList.removeAt(uriList.size-1)
            Log.d(TAG , "SecondAttachmentView deleted")
            attachmentsConstraintLayout!!.removeView(secondAttachmentView)
        }
        attachmentCount--
        if(attachmentCount == 0) {
            attachmentsConstraintLayout!!.setBackgroundResource(R.drawable.dates_unselected_background_stroke)
            ivBgNoUploads!!.visibility = View.VISIBLE
            tvBgNoUploads!!.visibility = View.VISIBLE
        }
    }
    // Function to create attachment
    private fun createAttachment(filename : String){
        attachmentsConstraintLayout!!.setBackgroundResource(0)
        ivBgNoUploads!!.visibility = View.INVISIBLE
        tvBgNoUploads!!.visibility = View.INVISIBLE
        attachmentCount++
        attachmentView = layoutInflater.inflate(R.layout.item_attachment , null)
        val id = View.generateViewId()
        val clwidth = attachmentsConstraintLayout!!.width
        attachmentView!!.id = id
        val substrings = filename.split(".")
        val fileExt = substrings[substrings.size-1]
        val ivFileType = attachmentView!!.findViewById<ImageView>(R.id.ivFileType)
        val tvFileName = attachmentView!!.findViewById<TextView>(R.id.tvAttachmentName)
        val ivRemoveTask = attachmentView!!.findViewById<ImageView>(R.id.ivRemoveAttachment)
        //setting the tags which are use to checking whether it is a first attachment or not
        //while removing the them
        if(prevAttachmentId == -1) ivRemoveTask.tag = "FirstAttachment"
        else ivRemoveTask.tag = "SecondAttachment"
        tvFileName.width = attachmentsConstraintLayout!!.width
        when(fileExt){
            "pdf" -> ivFileType.setImageResource(R.drawable.pdf)
            "png" -> ivFileType.setImageResource(R.drawable.ic_image)
            "txt" -> ivFileType.setImageResource(R.drawable.pdf)
            "jpg" -> ivFileType.setImageResource(R.drawable.ic_image)
            "docx" -> ivFileType.setImageResource(R.drawable.pdf)
            "mp4" -> ivFileType.setImageResource(R.drawable.video)
            "pptx" -> ivFileType.setImageResource(R.drawable.pdf)
            else-> ivFileType.setImageResource(R.drawable.pdf)
        }
        tvFileName.text = filename
        attachmentsConstraintLayout!!.addView(attachmentView)
        constraintSet = ConstraintSet()
        constraintSet.clone(attachmentsConstraintLayout)
        constraintSet.connect(id , ConstraintSet.END , ConstraintSet.PARENT_ID , ConstraintSet.END , 10)
        constraintSet.connect(id , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START , 10)
        if(prevAttachmentId == -1){
            firstAttachmentView = attachmentView
            constraintSet.connect(id , ConstraintSet.TOP , ConstraintSet.PARENT_ID , ConstraintSet.TOP , 15)
        }
        else {
            secondAttachmentView = attachmentView
            constraintSet.connect(id , ConstraintSet.TOP , prevAttachmentId , ConstraintSet.BOTTOM , 20)
        }
        prevAttachmentId = id
        constraintSet.applyTo(attachmentsConstraintLayout)
        //setting the onclicklistener for close button for removing the task
        ivRemoveTask.setOnClickListener(clickListener)
    }
    // Function to get file name from its uri
    private fun getNameFromUri(uri : Uri) : String{
        val cursor = this.contentResolver.query(uri , null , null , null , null)
        val indexedName = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val filename = cursor.getString(indexedName)
        cursor.close()
        return filename
    }
    // Function to add view to constraint layout
    @OptIn(DelicateCoroutinesApi::class)
    private fun addViewToConstraintLayout(view : View, types: MutableList<String>){
        constraintLayout!!.setBackgroundResource(0)
        ivBgAddTask!!.visibility = View.INVISIBLE
        tvBgAddTask!!.visibility = View.INVISIBLE
        job = GlobalScope.launch (Dispatchers.Main){
            for(i in 0 until types.size){
                selectedTypeList.add(types[i])
                val taskTypeItemView = layoutInflater.inflate(R.layout.task_type_item , null)
                val textView = taskTypeItemView.findViewById<TextView>(R.id.task_type_item_name)
                textView.text = types[i]
                //changing the background of task type item
                when(ind){
                    0 -> textView.background = ContextCompat.getDrawable(this@MainActivity , R.drawable.bg_rect_blue)
                    1 -> textView.background = ContextCompat.getDrawable(this@MainActivity , R.drawable.bg_rect_purple)
                    2 -> textView.background = ContextCompat.getDrawable(this@MainActivity , R.drawable.bg_rect_orange)
                    3 -> textView.background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_rect_green)
                }
                ind++
                if (ind == 4) ind = 0
                val id = View.generateViewId()
                taskTypeItemView.id = id
                taskTypeItemView.setOnClickListener(clickListener)
                constraintLayout!!.addView(taskTypeItemView)
                constraintSet.clone(constraintLayout)
                taskTypeItemView.post{
                    if(firstType) Log.d(TAG , "FirstType is true")
                    if(firstType){
                        firstIdConstant = id
                        firstId = id
                        prevId = id
                        firstType = false
                        constraintSet.connect(id , ConstraintSet.LEFT , ConstraintSet.PARENT_ID , ConstraintSet.LEFT)
                        constraintSet.connect(id , ConstraintSet.TOP , ConstraintSet.PARENT_ID , ConstraintSet.TOP)
                        startToEndIdMap.put(id , ConstraintSet.PARENT_ID) }
                    else {
                        // checking whether totalwidth crosses the constraintlayout or not
                        // if it crosses then we are constraining the new view to the bottom
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
                    clTotalWidth = clTotalWidth + taskTypeItemView.width
                    constraintSet.applyTo(constraintLayout)
                }
                // Delaying the coroutine before going to the next iteration so that task type
                // view is created and constrained in the constraint layout
                delay(50)
            }
            delay(200)
            job!!.cancelAndJoin()
        }
    }
    // Function to remove view by id
    private fun removeViewById(view : View){
        val typeName = findViewById<View>(view.id).findViewById<TextView>(R.id.task_type_item_name).text
        selectedTypeList.remove(typeName)
        taskPopUpWindow.taskCategories.add(typeName as String)
        taskPopUpWindow.itemsSelected--
        taskPopUpWindow.typeList.remove(typeName)
        Log.d(TAG , "taskCategories - ${taskPopUpWindow.taskCategories.size}")
        Log.d(TAG , "itemSelected - ${taskPopUpWindow.itemsSelected}")
        Log.d(TAG , "typeList - ${taskPopUpWindow.typeList}")
        constraintSet.clone(constraintLayout)
        val prevViewId = startToEndIdMap[view.id]
        val nextViewId = endToStartIdMap[view.id]
        // If the view has any view in the end of the current view
        if(nextViewId != null){
            if(view.id >= firstId){
                // means the view clicked lies in the second row of the constraint layout
                // firstId refers first view of second row
                // If the clicked view is equal to first view of second row
                if(view.id == firstId){
                    constraintSet.clear(nextViewId , ConstraintSet.BOTTOM)
                    constraintSet.connect(nextViewId , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START)
                    constraintSet.connect(nextViewId, ConstraintSet.TOP , firstIdConstant , ConstraintSet.BOTTOM)
                    startToEndIdMap[nextViewId] = ConstraintSet.PARENT_ID
                    firstId = nextViewId
                }
                // otherwise the view lies in middle
                // clicked view cannot come in the end because the nextViewId will be null
                // and that condition is handled in the other parent else block
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
            // Otherwise the clicked view lies in the first row of constraint layout
            else {
                //If the clicked view is first view of first row in constraint layout
                if(view.id == firstIdConstant){
                    constraintSet.clear(nextViewId , ConstraintSet.BOTTOM)
                    constraintSet.connect(nextViewId , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START)
                    constraintSet.connect(nextViewId, ConstraintSet.TOP , ConstraintSet.PARENT_ID , ConstraintSet.TOP)
                    startToEndIdMap[nextViewId] = ConstraintSet.PARENT_ID
                    firstIdConstant = nextViewId
                }
                // otherwise the view lies in the middle
                // clicked view cannot come in the end because the nextViewId will be null
                // and that condition is handled in the other parent else block
                else {
                    if(prevViewId != null){
                        constraintSet.connect(nextViewId , ConstraintSet.START , prevViewId , ConstraintSet.END)
                        constraintSet.connect(nextViewId , ConstraintSet.TOP , prevViewId , ConstraintSet.TOP)
                        constraintSet.connect(nextViewId , ConstraintSet.BOTTOM , prevViewId , ConstraintSet.BOTTOM)
                        startToEndIdMap[nextViewId] = prevViewId
                        endToStartIdMap[prevViewId] = nextViewId
                    }
                }
                // means that only one task type row is left in constraint layout
                if(firstId != firstIdConstant){
                    val nextToFirstId = endToStartIdMap[firstId]
                    constraintSet.connect(firstId , ConstraintSet.START ,  firstId-1, ConstraintSet.END , 20)
                    constraintSet.connect(firstId , ConstraintSet.TOP , firstId-1, ConstraintSet.TOP , -10)
                    constraintSet.connect(firstId , ConstraintSet.BOTTOM , firstId-1 , ConstraintSet.BOTTOM)
                    startToEndIdMap[firstId] = firstId-1
                    endToStartIdMap[firstId-1] = firstId
                    endToStartIdMap[firstId] = 20
                    // setting the firstId to firstId constant assuming that the no next view
                    // is present in the end
                    firstId = firstIdConstant
                    if(nextToFirstId != null){
                        constraintSet.connect(nextToFirstId , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START)
                        constraintSet.connect(nextToFirstId , ConstraintSet.TOP , firstIdConstant , ConstraintSet.BOTTOM )
                        // as the next view is not null updating the firstId to nextToFirstId
                        firstId = nextToFirstId
                        startToEndIdMap[nextToFirstId] = 0
                    }
                }
            }
        }
        // If the view doesn't has any view in the end of the current view
        // it refers to the last task type view of each row in task type constraint layout
        else {
            if(view.id != firstId){
                if(firstId != firstIdConstant){
                    if(prevViewId!=null){
                        Log.d(TAG , "view.id != firstId")
                        val nextToFirstId = endToStartIdMap[firstId]
                        constraintSet.connect(firstId , ConstraintSet.START ,  prevViewId, ConstraintSet.END , 20)
                        constraintSet.connect(firstId , ConstraintSet.TOP , prevViewId, ConstraintSet.TOP  , -10)
                        constraintSet.connect(firstId , ConstraintSet.BOTTOM , prevViewId , ConstraintSet.BOTTOM)
                        startToEndIdMap[firstId] = prevViewId
                        // setting the value to 20 because there is next view to last view of constraint layout
                        endToStartIdMap[firstId] = 20
                        endToStartIdMap[prevViewId] = firstId
                        //setting the firstId to firstIdConstant assuming that there is no next View to
                        // arrange after first view of second row in constraint layout
                        firstId = firstIdConstant
                        if(nextToFirstId != null){
                            constraintSet.connect(nextToFirstId , ConstraintSet.START , ConstraintSet.PARENT_ID , ConstraintSet.START)
                            constraintSet.connect(nextToFirstId , ConstraintSet.TOP , firstIdConstant , ConstraintSet.BOTTOM)
                            // updating the firstId as next view id is not null
                            firstId = nextToFirstId
                            startToEndIdMap[nextToFirstId] = 0
                        }
                    }
                }
            }
            else {
                Log.d(TAG , "view.id == firstId")
                // It means the clicked view is first view in second row and it doesn't
                // has a next view. So we just have to update the firstId to firstId constant
                // and then simply removing the view
                firstId = firstIdConstant
            }
        }
        constraintLayout!!.removeViewInLayout(view)
        constraintSet.applyTo(constraintLayout)
        taskTypeCount--
        if(taskTypeCount == 0){
            constraintLayout!!.setBackgroundResource(R.drawable.dates_unselected_background_stroke)
            ivBgAddTask!!.visibility = View.VISIBLE
            tvBgAddTask!!.visibility = View.VISIBLE
        }
    }
    // Function to getting the new task values
    private fun createTask(){
        val title = findViewById<EditText>(R.id.new_task_title_edtxt).text
        val dueDate = tvDueDate!!.text
        val startTime = tvStartTime!!.text
        val endTime = tvEndTime!!.text
        Log.d(TAG , "title - $title dueDate - $dueDate startTime - $startTime endTime - $endTime")
        Log.d(TAG , selectedTypeList.toString())
        Log.d(TAG , uriList.toString())
    }
}