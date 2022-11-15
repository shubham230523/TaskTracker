package com.shubham.tasktrackerapp

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.shubham.tasktrackerapp.Fragments.DashboardFragment
import com.shubham.tasktrackerapp.Fragments.HomeFragment
import com.shubham.tasktrackerapp.db.Task
import com.shubham.tasktrackerapp.db.TaskDatabase
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() , FileInterface{
    companion object {
        const val TAG = "taskTag"
    }

    // activity main
    private var cardView: CardView? = null
    private var clickListener: View.OnClickListener? = null
    private var longClickListener : View.OnLongClickListener? = null
    private var attachmentView: View? = null
    private var addTask: View? = null
    private var fragmentContainer: FragmentContainerView? = null
    private var btnAddAttachments: ImageView? = null
    private var ivCalender: ImageView? = null
    private var ivStartClock: ImageView? = null
    private var ivEndClock: ImageView? = null
    private var tvStartTime: TextView? = null
    private var tvEndTime: TextView? = null
    private var tvDueDate: TextView? = null
    private var tvBgLLAttachments: TextView? = null
    private var title: TextView? = null
    private var tvBgLLTasks: TextView? = null
    private var tvLongPressDelete: TextView? = null
    private var btnCreateTask: MaterialButton? = null
    private var linearLayoutTasks: LinearLayout? = null
    private var linearLayoutAttachments: LinearLayout? = null
    private var tasksSV: HorizontalScrollView? = null
    private var job: Job? = null
    private var taskTypeCount = 0
    private var attachmentCount = 0
    private var firstAttachmentId = 0
    private var secondAttachmentId = 0
    private var fstAttach = "FirstAttachment"
    private var sndAttach = "SecondAttachment"
    private val cal = Calendar.getInstance(Locale.ENGLISH)
    private val uriList = mutableListOf<String>()
    private val allTypesSelectedList = mutableListOf<String>()
    private val colors = arrayOf(
        Color.parseColor("#FFEBEE"), // light red
        Color.parseColor("#F3E5F5"), // light purple
        Color.parseColor("#E0F7FA"), // light blue
        Color.parseColor("#FFF3E0"), // light orange
        Color.parseColor("#EFEBE9"), // light grey
    )
    val hashMap = hashMapOf<String , String>()

    //popUpWindow
    private var taskCategories = mutableListOf(
        "Assignment", "Project", "Coding",
        "Classes", "Hobby", "Meeting", "Playing", "Hangout", "Food", "Television",
        "Exercise", "Remainder", "Other"
    )
    private var btnSelectTasks: Button? = null
    private var txtTasks: TextView? = null
    private var cbSelectedTypesList = mutableListOf<String>()
    private var popUpWindow: PopupWindow? = null
    private var cbAssignment: CheckBox? = null
    private var cbProject: CheckBox? = null
    private var cbCoding: CheckBox? = null
    private var cbClasses: CheckBox? = null
    private var cbHobby: CheckBox? = null
    private var cbMeeting: CheckBox? = null
    private var cbPlaying: CheckBox? = null
    private var cbHangout: CheckBox? = null
    private var cbFood: CheckBox? = null
    private var cbTelevision: CheckBox? = null
    private var cbExercise: CheckBox? = null
    private var cbRemainder: CheckBox? = null
    private var cbOther: CheckBox? = null
    private var popUpMenu: PopupMenu? = null

    @SuppressLint("ObjectAnimatorBinding")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // activity main high level components
        val bottomNavBar = findViewById<BottomNavigationView>(R.id.bottom_nav)
        cardView = findViewById(R.id.new_task_cv)
        val btnCardClose = findViewById<ImageView>(R.id.iv_close)
        fragmentContainer = findViewById(R.id.fragment_container)
        // Card view components
        btnCreateTask = findViewById(R.id.btnCreateTask)
        addTask = findViewById<ImageView>(R.id.iv_add_task)
        btnAddAttachments = findViewById(R.id.ivAddAttachments)
        linearLayoutTasks = findViewById(R.id.taskTypeLL)
        linearLayoutAttachments = findViewById(R.id.attachmentLL)
        tvBgLLTasks = findViewById(R.id.tvNoTaskTypeBackground)
        tvBgLLAttachments = findViewById(R.id.txtNoUploads)
        tvDueDate = findViewById(R.id.tv_due_date)
        ivCalender = findViewById(R.id.iv_calendar)
        ivStartClock = findViewById(R.id.iv_clock_start)
        ivEndClock = findViewById(R.id.iv_clock_end)
        tvStartTime = findViewById(R.id.tv_start_time)
        tvEndTime = findViewById(R.id.tv_end_time)
        tvLongPressDelete = findViewById(R.id.txtLongPressDelete)
        title = findViewById<EditText>(R.id.new_task_title_edtxt)
        tasksSV = findViewById(R.id.taskTypeSV)
        // Setting onclick listeners
        clickListener = View.OnClickListener { v ->
            Log.d(TAG, "clickListener - ${v.id}")
            when (v.id) {
                R.id.iv_calendar, R.id.tv_due_date -> {
                    pickDate()
                }
                R.id.iv_clock_start, R.id.tv_start_time -> {
                    selectTime(tvStartTime!!)
                }
                R.id.iv_clock_end, R.id.tv_end_time -> {
                    selectTime(tvEndTime!!)
                }
                R.id.ivRemoveAttachment ->{ removeAttachmentFromLinearLayout(v) }
            }
        }
        longClickListener = View.OnLongClickListener {
            showPopUpMenuForDeletion(it)
            return@OnLongClickListener true
        }
        tvDueDate!!.setOnClickListener(clickListener)
        ivCalender!!.setOnClickListener(clickListener)
        tvStartTime!!.setOnClickListener(clickListener)
        ivStartClock!!.setOnClickListener(clickListener)
        tvEndTime!!.setOnClickListener(clickListener)
        ivEndClock!!.setOnClickListener(clickListener)
        //setting the home fragment as the first fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment()).commit()
        // click listeners
        bottomNavBar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment()).commit()
                    return@setOnItemSelectedListener true
                }
                R.id.nav_new_task -> {
                    job = GlobalScope.launch(Dispatchers.Main) {
                        ObjectAnimator.ofFloat(
                            cardView,
                            "translationY",
                            resources.getDimensionPixelOffset(R.dimen.cardViewHeight) * 1f + 70
                        ).apply {
                            duration = 1500
                            start()
                        }
                        delay(1000)
                        fragmentContainer!!.foreground =
                            ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_transparent)
                        delay(500)
                        allTypesSelectedList.clear()
                        uriList.clear()
                        hashMap.clear()
                        job?.cancelAndJoin()
                    }
                    return@setOnItemSelectedListener true
                }
                R.id.nav_dashboard -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, DashboardFragment()).commit()
                    return@setOnItemSelectedListener true
                }
            }
            true
        }
        btnCardClose.setOnClickListener { clearAndHideCard() }
        btnCreateTask!!.setOnClickListener { createTask() }
        addTask!!.setOnClickListener { view1 ->
            if (taskTypeCount < 4) {
                cardView!!.foreground = ContextCompat.getDrawable(this, R.drawable.bg_transparent)
                showPopUpWindow(view1)
                popUpWindow!!.setOnDismissListener {
                    cardView!!.foreground = null
                }
                btnSelectTasks!!.setOnClickListener {
                    popUpWindow!!.dismiss()
                    addTaskToLinearLayout()
                }
            } else Toast.makeText(this, "Max task type limit is 4", Toast.LENGTH_SHORT)
                .show()
        }
        btnAddAttachments!!.setOnClickListener {
            if (attachmentCount == 2) {
                Toast.makeText(this, "You can upload at most 2 files", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            } else {
                selectFile()
            }
        }
    }

    // Function to add task type in the linear layout
    @SuppressLint("InflateParams")
    private fun addTaskToLinearLayout() {
        tasksSV!!.setBackgroundResource(0)
        tvBgLLTasks!!.visibility = View.GONE
        tvLongPressDelete!!.visibility = View.VISIBLE
        for (i in 0 until cbSelectedTypesList.size) {
            allTypesSelectedList.add(cbSelectedTypesList[i])
            val taskTypeItemView = layoutInflater.inflate(R.layout.task_type_item, null)
            taskTypeItemView.setOnLongClickListener(longClickListener)
            val tv = taskTypeItemView.findViewById<TextView>(R.id.task_type_item_name)
            val id = View.generateViewId()
            taskTypeItemView.id = id
            tv.text = cbSelectedTypesList[i]
            when (id % 4) {
                0 -> taskTypeItemView.findViewById<TextView>(R.id.task_type_item_name)
                    .setBackgroundResource(R.drawable.bg_rect_green)
                1 -> taskTypeItemView.findViewById<TextView>(R.id.task_type_item_name)
                    .setBackgroundResource(R.drawable.bg_rect_orange)
                2 -> taskTypeItemView.findViewById<TextView>(R.id.task_type_item_name)
                    .setBackgroundResource(R.drawable.bg_rect_purple)
                3 -> taskTypeItemView.findViewById<TextView>(R.id.task_type_item_name)
                    .setBackgroundResource(R.drawable.bg_rect_blue)
            }
            linearLayoutTasks!!.addView(taskTypeItemView)
        }
    }

    // Function to pick a date
    private fun pickDate() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                tvDueDate!!.text = getString(R.string.selected_date , day , month+1 , year)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Function to select time
    private fun selectTime(tv: TextView) {
        TimePickerDialog(
            this,
            { _, hourOfDay, minute -> tv.text = getString(R.string.selected_time , hourOfDay , minute) },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    // Function to selectFile from the local storage
    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        openFileResultLauncher.launch(intent)
    }

    // A launcher for checking whether storage permission is granted or not
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            selectFile()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    // A launcher for getting the uri from the data
    private val openFileResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val data: Intent? = result?.data
        if (data != null) {
            val uri = data.data
            if (uri != null) {
                val filename = getNameFromUri(uri)
                hashMap[uri.toString()] = filename
                uriList.add(uri.toString())
                addAttachmentToLinearLayout(filename)
            } else Toast.makeText(this, "Some error occurred, try again", Toast.LENGTH_SHORT).show()
        } else Toast.makeText(this, "Some error occurred, try again", Toast.LENGTH_SHORT).show()
    }

    // Function to remove attachment
    private fun removeAttachmentFromLinearLayout(v: View) {
        if (v.tag == fstAttach) {
            val attachment = linearLayoutAttachments!!.findViewById<View>(firstAttachmentId)
            val fileName = attachment.findViewById<TextView>(R.id.tvAttachmentName).text
            uriList.remove(fileName as String?)
            var k = ""
            for((key , value) in hashMap){
                if(value == fileName){
                    k = key
                }
            }
            hashMap.remove(k)
            linearLayoutAttachments!!.removeView(attachment)
        } else {
            val attachment = linearLayoutAttachments!!.findViewById<View>(secondAttachmentId)
            val fileName = attachment
                .findViewById<TextView>(R.id.tvAttachmentName).text
            uriList.remove(fileName as String?)
            var k = ""
            for((key , value) in hashMap){
                if(value == fileName){
                    k = key
                }
            }
            hashMap.remove(k)
            linearLayoutAttachments!!.removeView(attachment)
        }
        attachmentCount--
        if(attachmentCount == 0) {
            linearLayoutAttachments!!.setBackgroundResource(R.drawable.dates_unselected_background_stroke)
            tvBgLLAttachments!!.visibility = View.VISIBLE
        }
    }

    // Function to create attachment
    @SuppressLint("InflateParams")
    private fun addAttachmentToLinearLayout(filename: String) {
        linearLayoutAttachments!!.setBackgroundResource(0)
        tvBgLLAttachments!!.visibility = View.GONE
        attachmentCount++
        attachmentView = layoutInflater.inflate(R.layout.item_attachment, null)
        val id = View.generateViewId()
        attachmentView!!.id = id
        val substrings = filename.split(".")
        val fileExt = substrings[substrings.size - 1]
        val ivFileType = attachmentView!!.findViewById<ImageView>(R.id.ivFileType)
        val tvFileName = attachmentView!!.findViewById<TextView>(R.id.tvAttachmentName)
        val ivRemoveTask = attachmentView!!.findViewById<ImageView>(R.id.ivRemoveAttachment)
        //setting the tags which are use to checking whether it is a first attachment or not
        //while removing the them
        if (attachmentCount == 1) {
            ivRemoveTask.tag = fstAttach
            firstAttachmentId = id
        } else {
            ivRemoveTask.tag = sndAttach
            secondAttachmentId = id
        }
        tvFileName.width = linearLayoutAttachments!!.width
        when (fileExt) {
            "pdf" -> ivFileType.setImageResource(R.drawable.pdf)
            "png" -> ivFileType.setImageResource(R.drawable.ic_image)
            "txt" -> ivFileType.setImageResource(R.drawable.pdf)
            "jpg" -> ivFileType.setImageResource(R.drawable.ic_image)
            "docx" -> ivFileType.setImageResource(R.drawable.pdf)
            "mp4" -> ivFileType.setImageResource(R.drawable.video)
            "pptx" -> ivFileType.setImageResource(R.drawable.pdf)
            else -> ivFileType.setImageResource(R.drawable.pdf)
        }
        tvFileName.text = filename
        //setting the on click listener for close button for removing the task
        ivRemoveTask.setOnClickListener(clickListener)
        linearLayoutAttachments!!.addView(attachmentView)
    }

    // Function to get file name from its uri
    fun getNameFromUri(uri: Uri): String {
        val cursor = this.contentResolver.query(uri, null, null, null, null)
        val indexedName = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val filename = cursor.getString(indexedName)
        cursor.close()
        return filename
    }

    // Function to remove view by id
    private fun removeTaskFromLinearLayout(view: View) {
        val typeName =
            findViewById<View>(view.id).findViewById<TextView>(R.id.task_type_item_name).text
        allTypesSelectedList.remove(typeName)
        taskCategories.add(typeName as String)
        linearLayoutTasks!!.removeView(view)
        taskTypeCount--
        if (taskTypeCount == 0) {
            tasksSV!!.setBackgroundResource(R.drawable.dates_unselected_background_stroke)
            tvBgLLTasks!!.visibility = View.VISIBLE
            tvLongPressDelete!!.visibility = View.INVISIBLE
        }
    }

    // Function to show pop up menu on task item long press
    private fun showPopUpMenuForDeletion(view : View){
        popUpMenu = PopupMenu(this , view)
        popUpMenu!!.inflate(R.menu.popup_menu)
        popUpMenu!!.setOnMenuItemClickListener {
            removeTaskFromLinearLayout(view)
            return@setOnMenuItemClickListener true
        }
        popUpMenu!!.show()
    }

    // Function to getting the new task values and inserting into the database
    @OptIn(DelicateCoroutinesApi::class)
    private fun createTask() {
        val title = title!!.text.toString()
        val dueDate = tvDueDate!!.text.toString()
        val startTime = tvStartTime!!.text.toString()
        val endTime = tvEndTime!!.text.toString()
        val todayDate = "${cal.get(Calendar.DATE)}-${cal.get(Calendar.MONTH)+1}-${cal.get(Calendar.YEAR)}"
        val task = Task(
            title = title,
            added_date = todayDate,
            due_date = dueDate,
            start_time = startTime,
            end_time = endTime,
            taskTypes = allTypesSelectedList,
            attachments = hashMap,
            bgColor = colors.random()
        )
        val taskDao = TaskDatabase.getInstance(this).dao()
        job = GlobalScope.launch {
            taskDao.insertTask(task)
            delay(200)
            job!!.cancelAndJoin()
        }
        clearAndHideCard()
    }

    @SuppressLint("ObjectAnimatorBinding")
    @OptIn(DelicateCoroutinesApi::class)
    private fun clearAndHideCard() {
        title!!.text = ""
        tvDueDate!!.text = getString(R.string.no_date)
        tvStartTime!!.text = getString(R.string.no_time)
        tvEndTime!!.text = getString(R.string.no_time)
        for (i in allTypesSelectedList) {
            taskCategories.add(i)
        }
        //removing all child views of task linearlayout except background text which present at index 0
        for (i in 0 until taskTypeCount) {
            linearLayoutTasks!!.removeViewAt(1)
        }
        tvBgLLTasks!!.visibility = View.VISIBLE
        tasksSV!!.setBackgroundResource(R.drawable.dates_unselected_background_stroke)
        //removing all child views of attachment linearlayout except background text which present at index 0
        for (i in 0 until attachmentCount) {
            linearLayoutAttachments!!.removeViewAt(1)
        }
        tvBgLLAttachments!!.visibility = View.VISIBLE
        linearLayoutAttachments!!.setBackgroundResource(R.drawable.dates_unselected_background_stroke)
        taskTypeCount = 0
        attachmentCount = 0
        job = GlobalScope.launch(Dispatchers.Main) {
            fragmentContainer!!.foreground = null
            ObjectAnimator.ofFloat(
                cardView,
                "translationY",
                resources.getDimensionPixelOffset(R.dimen.cardViewHeight) * -1f + 70
            ).apply {
                duration = 1500
                start()
            }
            delay(500)
            delay(1000)
            job?.cancelAndJoin()
        }
    }

    // Function to show pop window
    @SuppressLint("InflateParams")
    fun showPopUpWindow(view: View) {
        //pop up window
        val popView = layoutInflater.inflate(R.layout.task_type_pop_up_window_layout, null)
        popUpWindow = PopupWindow(
            popView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popUpWindow!!.isOutsideTouchable = true
        popUpWindow!!.isFocusable = true
        popUpWindow!!.showAtLocation(view, Gravity.CENTER, 0, 0)
        txtTasks = popView.findViewById(R.id.popUptxtTaskType)
        // pop up checkboxes
        cbAssignment = popView.findViewById(R.id.cbAssignment)
        cbProject = popView.findViewById(R.id.cbProject)
        cbCoding = popView.findViewById(R.id.cbCoding)
        cbClasses = popView.findViewById(R.id.cbClasses)
        cbHobby = popView.findViewById(R.id.cbHobby)
        cbMeeting = popView.findViewById(R.id.cbMeeting)
        cbPlaying = popView.findViewById(R.id.cbPlaying)
        cbHangout = popView.findViewById(R.id.cbHangout)
        cbFood = popView.findViewById(R.id.cbFood)
        cbTelevision = popView.findViewById(R.id.cbTV)
        cbExercise = popView.findViewById(R.id.cbExercise)
        cbRemainder = popView.findViewById(R.id.cbRemainder)
        cbOther = popView.findViewById(R.id.cbOther)
        //onCheckedChange listener for checkboxes
        cbAssignment!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbProject!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbCoding!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbClasses!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbHobby!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbMeeting!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbPlaying!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbHangout!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbFood!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbTelevision!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbExercise!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbRemainder!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbOther!!.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        // making the checkboxes visible which are present in typeCategories list
        for (i in taskCategories) {
            popView!!.findViewWithTag<CheckBox>(i).visibility = View.VISIBLE
        }
        //button for selecting tasks
        btnSelectTasks = popView.findViewById(R.id.popBtnSelectTasks)
        cbSelectedTypesList.clear()
    }

    // Function to control the max limit of check boxes clicked
    //and add the types in cbSelectedTypes list
    private fun onCheckBoxSelected(compoundButton: CompoundButton, b: Boolean, view: View) {
        if (taskTypeCount == 4 && b) {
            compoundButton.isChecked = false
            Toast.makeText(view.context, "Limit reached!!", Toast.LENGTH_SHORT).show()
        } else if (b) {
            taskTypeCount++
            val name = compoundButton.text.toString()
            taskCategories.remove(name)
            cbSelectedTypesList.add(name)
        } else {
            taskTypeCount--
            val name = compoundButton.text.toString()
            taskCategories.add(name)
            cbSelectedTypesList.remove(name)
        }
    }

    override fun getFileNameFromUri(uri : Uri): String {
        val cursor = this.contentResolver.query(uri, null, null, null, null)
        val indexedName = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val filename = cursor.getString(indexedName)
        cursor.close()
        return filename
    }
}