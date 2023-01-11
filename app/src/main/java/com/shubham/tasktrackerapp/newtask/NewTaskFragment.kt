package com.shubham.tasktrackerapp.newtask

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.RoundedCorner
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.shubham.tasktrackerapp.MainActivity
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.RoomViewModel
import com.shubham.tasktrackerapp.data.local.Task
import com.shubham.tasktrackerapp.data.local.TaskDaoImpl
import com.shubham.tasktrackerapp.data.local.TaskDatabase
import com.shubham.tasktrackerapp.theme.TaskTrackerTheme
import com.shubham.tasktrackerapp.theme.TaskTrackerTopography
import com.shubham.tasktrackerapp.util.newTaskColors
import com.shubham.tasktrackerapp.util.taskCategories
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

/**
 * Fragment to create new task
 *
 * @param mContext Main activity context
 */
@AndroidEntryPoint
class NewTaskFragment @Inject constructor(
    private val mContext: Context,
) : Fragment(R.layout.fragment_new_task) {
    companion object {
        const val TAG = "NewTask"
    }

    private val viewModel by viewModels<RoomViewModel>()

    // card view components
    private lateinit var cardView: CardView
    private lateinit var attachmentView: View
    private lateinit var addTask: View
    private lateinit var btnAddAttachments: ImageView
    private lateinit var ivCalender: ImageView
    private lateinit var ivStartClock: ImageView
    private lateinit var ivEndClock: ImageView
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var tvDueDate: TextView
    private lateinit var tvBgLLAttachments: TextView
    private lateinit var title: TextView
    private lateinit var tvBgLLTasks: TextView
    private lateinit var tvLongPressDelete: TextView
    private lateinit var btnCreateTask: MaterialButton
    private lateinit var linearLayoutTasks: LinearLayout
    private lateinit var linearLayoutAttachments: LinearLayout
    private lateinit var tasksSV: HorizontalScrollView

    private var taskTypeCount = 0
    private var attachmentCount = 0
    private var firstAttachmentId = 0
    private var secondAttachmentId = 0
    private var fstAttach = "FirstAttachment"
    private var sndAttach = "SecondAttachment"
    private val cal = Calendar.getInstance(Locale.ENGLISH)
    private val uriList = mutableListOf<String>()
    private val allTypesSelectedList = mutableListOf<String>()
    private lateinit var clickListener: View.OnClickListener
    private lateinit var longClickListener: View.OnLongClickListener

    val hashMap = hashMapOf<String, String>()

    // pop up window components
    private lateinit var btnSelectTasks: Button
    private lateinit var txtTasks: TextView
    private lateinit var popUpWindow: PopupWindow
    private lateinit var cbAssignment: CheckBox
    private lateinit var cbProject: CheckBox
    private lateinit var cbCoding: CheckBox
    private lateinit var cbClasses: CheckBox
    private lateinit var cbHobby: CheckBox
    private lateinit var cbMeeting: CheckBox
    private lateinit var cbPlaying: CheckBox
    private lateinit var cbHangout: CheckBox
    private lateinit var cbFood: CheckBox
    private lateinit var cbTelevision: CheckBox
    private lateinit var cbExercise: CheckBox
    private lateinit var cbRemainder: CheckBox
    private lateinit var cbOther: CheckBox
    private lateinit var popUpMenu: PopupMenu

    private var cbSelectedTypesList = mutableListOf<String>()
    private var job: Job? = null
    private lateinit var fragmentView: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentView = view

        //card components
        cardView = view.findViewById(R.id.new_task_cv)
        btnCreateTask = view.findViewById(R.id.btnCreateTask)
        addTask = view.findViewById<ImageView>(R.id.iv_add_task)
        btnAddAttachments = view.findViewById(R.id.ivAddAttachments)
        linearLayoutTasks = view.findViewById(R.id.taskTypeLL)
        linearLayoutAttachments = view.findViewById(R.id.attachmentLL)
        tvBgLLTasks = view.findViewById(R.id.tvNoTaskTypeBackground)
        tvBgLLAttachments = view.findViewById(R.id.txtNoUploads)
        tvDueDate = view.findViewById(R.id.tv_due_date)
        ivCalender = view.findViewById(R.id.iv_calendar)
        ivStartClock = view.findViewById(R.id.iv_clock_start)
        ivEndClock = view.findViewById(R.id.iv_clock_end)
        tvStartTime = view.findViewById(R.id.tv_start_time)
        tvEndTime = view.findViewById(R.id.tv_end_time)
        tvLongPressDelete = view.findViewById(R.id.txtLongPressDelete)
        tasksSV = view.findViewById(R.id.taskTypeSV)
        title = view.findViewById<EditText>(R.id.new_task_title_edtxt)

        // Setting onclick listeners
        clickListener = View.OnClickListener { v ->
            when (v.id) {
                R.id.iv_calendar, R.id.tv_due_date -> {
                    pickDate()
                }
                R.id.iv_clock_start, R.id.tv_start_time -> {
                    selectTime(tvStartTime)
                }
                R.id.iv_clock_end, R.id.tv_end_time -> {
                    selectTime(tvEndTime)
                }
                R.id.ivRemoveAttachment -> {
                    removeAttachmentFromLinearLayout(v)
                }
            }
        }

        longClickListener = View.OnLongClickListener {
            showPopUpMenuForDeletion(it)
            return@OnLongClickListener true
        }

        tvDueDate.setOnClickListener(clickListener)
        ivCalender.setOnClickListener(clickListener)
        tvStartTime.setOnClickListener(clickListener)
        ivStartClock.setOnClickListener(clickListener)
        tvEndTime.setOnClickListener(clickListener)
        ivEndClock.setOnClickListener(clickListener)

        val btnCardClose = view.findViewById<ImageView>(R.id.iv_close)

        btnCardClose.setOnClickListener {
            (mContext as MainActivity).onBackPressed()
        }

        btnCreateTask.setOnClickListener { createTask() }

        addTask.setOnClickListener { view1 ->
            if (taskTypeCount < 4) {
                cardView.foreground = ContextCompat.getDrawable(mContext, R.drawable.bg_transparent)
                showPopUpWindow(view1)
                popUpWindow.setOnDismissListener {
                    cardView.foreground = null
                }
                btnSelectTasks.setOnClickListener {
                    popUpWindow.dismiss()
                    addTaskToLinearLayout()
                }
            } else Toast.makeText(mContext, "Max task type limit is 4", Toast.LENGTH_SHORT)
                .show()
        }

        btnAddAttachments.setOnClickListener {
            if (attachmentCount == 2) {
                Toast.makeText(mContext, "You can upload at most 2 files", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (
                ActivityCompat.checkSelfPermission(
                    mContext,
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

    /**
     * Function to create task type item and show in linear layout of tasks type selected
     */
    @SuppressLint("InflateParams")
    private fun addTaskToLinearLayout() {

        tasksSV.setBackgroundResource(0)
        tvBgLLTasks.visibility = View.GONE
        tvLongPressDelete.visibility = View.VISIBLE

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

            linearLayoutTasks.addView(taskTypeItemView)
        }
    }

    private fun pickDate() {
        DatePickerDialog(
            mContext,
            { _, year, month, day ->
                tvDueDate.text = getString(R.string.selected_date, day, month + 1, year)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun selectTime(tv: TextView) {
        TimePickerDialog(
            mContext,
            { _, hourOfDay, minute ->
                tv.text = getString(R.string.selected_time, hourOfDay, minute)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    /**
     * Function to selectFile from the local storage
     */
    private fun selectFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "*/*"
        openFileResultLauncher.launch(intent)
    }

    /**
     * A launcher for checking whether storage permission is granted or not
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            selectFile()
        } else {
            Toast.makeText(mContext, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * A launcher for getting the uri from the data
     */
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
            } else {
                Toast.makeText(mContext, "Some error occurred, try again", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(mContext, "Some error occurred, try again", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Function to remove attachment
     *
     * @param v view to be removed from linear layout
     */
    private fun removeAttachmentFromLinearLayout(v: View) {
        lateinit var attachment: View
        if (v.tag == fstAttach) {
            attachment = linearLayoutAttachments.findViewById<View>(firstAttachmentId)
        } else {
            attachment = linearLayoutAttachments.findViewById<View>(secondAttachmentId)
        }
        val fileName = attachment.findViewById<TextView>(R.id.tvAttachmentName).text
        uriList.remove(fileName as String?)
        var k = ""
        for ((key, value) in hashMap) {
            if (value == fileName) {
                k = key
            }
        }
        hashMap.remove(k)
        linearLayoutAttachments.removeView(attachment)
        attachmentCount--
        if (attachmentCount == 0) {
            linearLayoutAttachments.setBackgroundResource(R.drawable.dates_unselected_background_stroke)
            tvBgLLAttachments.visibility = View.VISIBLE
        }
    }

    /**
     * Function to create attachment
     *
     * @param filename name of the file to be added as attachment
     */
    @SuppressLint("InflateParams")
    private fun addAttachmentToLinearLayout(filename: String) {

        linearLayoutAttachments.setBackgroundResource(0)
        tvBgLLAttachments.visibility = View.GONE
        attachmentCount++
        attachmentView = layoutInflater.inflate(R.layout.item_attachment, null)
        val id = View.generateViewId()
        attachmentView.id = id
        val substrings = filename.split(".")
        val fileExt = substrings[substrings.size - 1]
        val ivFileType = attachmentView.findViewById<ImageView>(R.id.ivFileType)
        val tvFileName = attachmentView.findViewById<TextView>(R.id.tvAttachmentName)
        val ivRemoveTask = attachmentView.findViewById<ImageView>(R.id.ivRemoveAttachment)

        //setting the tags which are use to checking whether it is a first attachment or not
        //while removing the them
        if (attachmentCount == 1) {
            ivRemoveTask.tag = fstAttach
            firstAttachmentId = id
        } else {
            ivRemoveTask.tag = sndAttach
            secondAttachmentId = id
        }
        tvFileName.width = linearLayoutAttachments.width

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
        linearLayoutAttachments.addView(attachmentView)
    }

    fun getNameFromUri(uri: Uri): String {
        val cursor = mContext.contentResolver.query(uri, null, null, null, null)
        val indexedName = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        val filename = cursor.getString(indexedName)
        cursor.close()
        return filename
    }

    /**
     * Function to remove task type item by id
     *
     * @param v view to be removed from linear layout
     */
    private fun removeTaskFromLinearLayout(view: View) {
        val typeName =
            fragmentView.findViewById<View>(view.id)
                .findViewById<TextView>(R.id.task_type_item_name).text
        allTypesSelectedList.remove(typeName)
        taskCategories.add(typeName as String)
        linearLayoutTasks.removeView(view)
        taskTypeCount--
        if (taskTypeCount == 0) {
            tasksSV.setBackgroundResource(R.drawable.dates_unselected_background_stroke)
            tvBgLLTasks.visibility = View.VISIBLE
            tvLongPressDelete.visibility = View.INVISIBLE
        }
    }

    /**
     * Function to shows pop up menu on task item long press
     *
     * @param view View on which long press is performed
     */
    private fun showPopUpMenuForDeletion(view: View) {
        popUpMenu = PopupMenu(mContext, view)
        popUpMenu.inflate(R.menu.popup_menu)
        popUpMenu.setOnMenuItemClickListener {
            removeTaskFromLinearLayout(view)
            return@setOnMenuItemClickListener true
        }
        popUpMenu.show()
    }

    /**
     * Function to create task based on value provided
     */

    private fun createTask() {

        val title = title.text.toString()
        val dueDate = tvDueDate.text.toString()
        val startTime = tvStartTime.text.toString()
        val endTime = tvEndTime.text.toString()
        val todayDate =
            "${cal.get(Calendar.DATE)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.YEAR)}"
        val task = Task(
            title = title,
            added_date = todayDate,
            due_date = dueDate,
            start_time = startTime,
            end_time = endTime,
            taskTypes = allTypesSelectedList,
            attachments = hashMap,
            bgColor = newTaskColors.random()
        )
        viewModel.insertIntoDatabase(task)
        (mContext as MainActivity).onBackPressed()
    }

    /**
     * Function to show pop window while selecting task types
     *
     * @param view add task type icon's view needed to show pop up menu
     */
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
        popUpWindow.isOutsideTouchable = true
        popUpWindow.isFocusable = true
        popUpWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
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
        cbAssignment.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbProject.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbCoding.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbClasses.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbHobby.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbMeeting.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbPlaying.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbHangout.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbFood.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbTelevision.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbExercise.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbRemainder.setOnCheckedChangeListener { compoundButton, b ->
            onCheckBoxSelected(
                compoundButton,
                b,
                view
            )
        }
        cbOther.setOnCheckedChangeListener { compoundButton, b ->
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

    /**
     * Function to control the max limit of check boxes clicked
     * And also for adding the selected task types in cbSelectedTypes list
     *
     * @param compoundButton a button that represent checkbox
     * @param b Boolean which indicates active state of checkbox i.e on/off
     * @param view View of checkbox
     */
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
}

@Composable
fun NewTask() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(630.dp),
            color = Transparent,
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp)
                    )
                    .padding(15.dp),
            ) {
                var taskTitle by rememberSaveable { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Create a new task",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector  = Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = {
                        taskTitle = it
                    },
                    label = { Text(
                        "Title",
                        style = MaterialTheme.typography.bodyMedium,
                    ) },
                    placeholder = {
                        Text(
                            text = "Task title ",
                            modifier = Modifier.alpha(0.4f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(60.dp),

                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                        placeholderColor = MaterialTheme.colorScheme.onSurface,
                    )
                )
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 18.dp)
                ){
                    Text(
                        text = "Due date",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(80.dp)
                    )
                    Image(
                        painterResource(id = R.drawable.ic_calendar),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 20.dp, end = 9.dp)
                            .size(18.dp)
                            .alpha(0.4f)
                    )
                    Text(
                        text = "dd-mm-yy",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row (modifier = Modifier.padding(top = 10.dp)){
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(80.dp)
                    )
                    Image(
                        painterResource(id = R.drawable.ic_clock),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 20.dp, end = 7.dp)
                            .size(20.dp)
                            .alpha(0.4f)
                    )
                    Text(
                        text = "00:00",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row (modifier = Modifier.padding(top = 10.dp)){
                    Text(
                        text = "End",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(80.dp)
                    )
                    Image(
                        painterResource(id = R.drawable.ic_clock),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 20.dp, end = 7.dp)
                            .size(20.dp)
                            .alpha(0.4f)
                    )
                    Text(
                        text = "00:00",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row (modifier = Modifier.padding(top = 18.dp)){
                    Text(
                        text = "Task Type",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Image(
                        painterResource(id = R.drawable.ic_add_task),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .size(24.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(
                            1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(10.dp),
                        )
                        .background(
                            color = MaterialTheme.colorScheme.background,
                        )
                        .padding(15.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Row (
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Text(
                            text = "Assignment",
                            style = TaskTrackerTopography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        )
                        Text(
                            text = "Project",
                            style = TaskTrackerTopography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondary,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        )
                        Text(
                            text = "Assignment",
                            style = TaskTrackerTopography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    MaterialTheme.colorScheme.tertiary,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Text(
                            text = "Hangout",
                            style = TaskTrackerTopography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    MaterialTheme.colorScheme.tertiary,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                }
                Row (modifier = Modifier.padding(top = 18.dp)){
                    Text(
                        text = "No Attachments",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Image(
                        painterResource(id = R.drawable.ic_add_task),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 10.dp, end = 10.dp)
                            .size(24.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .height(115.dp)
                        .border(
                            1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(10.dp),
                        )
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        )
                        .padding(15.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    for(i in 0 until 2){
                        ConstraintLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(10.dp),
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.background,
                                )
                        ) {
                            val (fileType , fileName , close) = createRefs()
                            Image(
                                painterResource(id = R.drawable.pdf),
                                contentDescription = null,
                                modifier = Modifier
                                    .constrainAs(fileType) {
                                        top.linkTo(parent.top, 10.dp)
                                        bottom.linkTo(parent.bottom, 10.dp)
                                        start.linkTo(parent.start, 10.dp)
                                    }
                                    .width(16.dp)
                            )
                            Text(
                                text = "BDA_Assignmentgowjsnfioweal;sfjioweisfjocknsdiolfjzxciodsjfkcsdjfjcops",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines  = 1,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .constrainAs(fileName){
                                        top.linkTo(fileType.top)
                                        bottom.linkTo(fileType.bottom)
                                        start.linkTo(fileType.end , 5.dp)
                                        end.linkTo(close.start , 5.dp)
                                        width = Dimension.fillToConstraints
                                    }
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = null,
                                modifier = Modifier.constrainAs(close){
                                    top.linkTo(fileName.top)
                                    bottom.linkTo(fileName.bottom)
                                    end.linkTo(parent.end , 10.dp)
                                    width = Dimension.value(16.dp)
                                }
                            )
                        }
                        Spacer(
                            modifier = if (i == 1) {
                                Modifier.height(10.dp)
                            }else {
                                Modifier.height(10.dp)
                            }
                        )
                    }
                }
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .padding(top = 20.dp, start = 10.dp, end = 10.dp)
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Text(
                        text = "Create task",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun NewTaskPreview(){
    NewTask()
}

@Preview
@Composable
fun NewTaskDarkPreview() {
    TaskTrackerTheme(isDarkTheme = true) {
        NewTask()
    }
}