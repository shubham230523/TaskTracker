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
import android.util.Log
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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import com.shubham.tasktrackerapp.MainActivity
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.RoomViewModel
import com.shubham.tasktrackerapp.data.local.*
import com.shubham.tasktrackerapp.theme.TaskTrackerTheme
import com.shubham.tasktrackerapp.theme.TaskTrackerTopography
import com.shubham.tasktrackerapp.util.newTaskColors
import com.shubham.tasktrackerapp.util.taskCategories
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.TimePickerColors
import com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
            added_date = LocalDate.of(2023 , 1 , 23),
            due_date = LocalDate.of(2023 , 1 , 23),
            start_time = LocalTime.now(),
            end_time = LocalTime.now(),
            taskTypes = allTypesSelectedList,
            attachments = hashMap,
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
private const val TAG = "NewTaskFragment"

@Composable
fun NewTask() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .zIndex(1f),
            color = Transparent,
        ) {
            val roomViewModel = hiltViewModel<RoomViewModel>()
//            val missedTask1 = TaskDone(
//                "TaskDone1",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 14),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask2 = TaskDone(
//                "TaskDone2",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 13),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask3 = TaskDone(
//                "TaskDone3",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 12),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask4 = TaskDone(
//                "TaskDone4",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 11),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask5 = TaskDone(
//                "TaskDone5",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 11),
//                LocalTime.of(10 , 30 , 0),
//                LocalTime.of(11 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask6 = TaskDone(
//                "TaskDone6",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 10),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask7 = TaskDone(
//                "TaskDone7",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 9),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask8 = TaskDone(
//                "TaskDone8",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 8),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask9 = TaskDone(
//                "TaskDone9",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 7),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask10 = TaskDone(
//                "TaskDone10",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 6),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask11 = TaskDone(
//                "TaskDone11",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 6),
//                LocalTime.of(11 , 30 , 0),
//                LocalTime.of(12 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask12 = TaskDone(
//                "TaskDone12",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 5),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask13 = TaskDone(
//                "TaskDone13",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 5),
//                LocalTime.of(11 , 30 , 0),
//                LocalTime.of(12 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask14 = TaskDone(
//                "TaskDone14",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 4),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask15 = TaskDone(
//                "TaskDone15",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 3),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask16 = TaskDone(
//                "TaskDone16",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 2),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask17 = TaskDone(
//                "TaskDone17",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 2),
//                LocalTime.of(10 , 30 , 0),
//                LocalTime.of(11 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask18 = TaskDone(
//                "TaskDone18",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2023 , 1 , 1),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask19 = TaskDone(
//                "TaskDone19",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 31),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf(),
//            )
//            val missedTask20 = TaskDone(
//                "TaskDone20",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 29),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask21 = TaskDone(
//                "TaskDone21",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 28),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask22 = TaskDone(
//                "TaskDone22",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 27),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask23 = TaskDone(
//                "TaskDone23",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 26),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask24 = TaskDone(
//                "TaskDone24",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 25),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask25 = TaskDone(
//                "TaskDone25",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 24),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask26 = TaskDone(
//                "TaskDone26",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 23),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask27 = TaskDone(
//                "TaskDone27",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 22),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask28 = TaskDone(
//                "TaskDone28",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 21),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask29 = TaskDone(
//                "TaskDone29",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 20),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask30 = TaskDone(
//                "TaskDone30",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 19),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask31 = TaskDone(
//                "TaskDone31",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 18),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask32 = TaskDone(
//                "TaskDone32",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 17),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask33 = TaskDone(
//                "TaskDone33",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 16),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask34 = TaskDone(
//                "TaskDone34",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 15),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask35 = TaskDone(
//                "TaskDone35",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 14),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask36 = TaskDone(
//                "TaskDone36",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 13),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask37 = TaskDone(
//                "TaskDone37",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 12),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask38 = TaskDone(
//                "TaskDone38",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 11),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask39 = TaskDone(
//                "TaskDone39",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12 , 10),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            val missedTask40 = TaskDone(
//                "TaskDone40",
//                LocalDate.of(2023 , 1 , 15),
//                LocalDate.of(2022 , 12, 9),
//                LocalTime.of(13 , 30 , 0),
//                LocalTime.of(14 , 30 , 0),
//                mutableListOf("Assignment , Coding"),
//                hashMapOf()
//            )
//            roomViewModel.insertTaskDone(missedTask1)
//            roomViewModel.insertTaskDone(missedTask2)
//            roomViewModel.insertTaskDone(missedTask3)
//            roomViewModel.insertTaskDone(missedTask4)
//            roomViewModel.insertTaskDone(missedTask5)
//            roomViewModel.insertTaskDone(missedTask6)
//            roomViewModel.insertTaskDone(missedTask7)
//            roomViewModel.insertTaskDone(missedTask8)
//            roomViewModel.insertTaskDone(missedTask9)
//            roomViewModel.insertTaskDone(missedTask10)
//            roomViewModel.insertTaskDone(missedTask11)
//            roomViewModel.insertTaskDone(missedTask12)
//            roomViewModel.insertTaskDone(missedTask13)
//            roomViewModel.insertTaskDone(missedTask14)
//            roomViewModel.insertTaskDone(missedTask15)
//            roomViewModel.insertTaskDone(missedTask16)
//            roomViewModel.insertTaskDone(missedTask17)
//            roomViewModel.insertTaskDone(missedTask18)
//            roomViewModel.insertTaskDone(missedTask19)
//            roomViewModel.insertTaskDone(missedTask20)
//            roomViewModel.insertTaskDone(missedTask21)
//            roomViewModel.insertTaskDone(missedTask22)
//            roomViewModel.insertTaskDone(missedTask23)
//            roomViewModel.insertTaskDone(missedTask24)
//            roomViewModel.insertTaskDone(missedTask25)
//            roomViewModel.insertTaskDone(missedTask26)
//            roomViewModel.insertTaskDone(missedTask27)
//            roomViewModel.insertTaskDone(missedTask28)
//            roomViewModel.insertTaskDone(missedTask29)
//            roomViewModel.insertTaskDone(missedTask30)
//            roomViewModel.insertTaskDone(missedTask31)
//            roomViewModel.insertTaskDone(missedTask32)
//            roomViewModel.insertTaskDone(missedTask33)
//            roomViewModel.insertTaskDone(missedTask34)
//            roomViewModel.insertTaskDone(missedTask35)
//            roomViewModel.insertTaskDone(missedTask36)
//            roomViewModel.insertTaskDone(missedTask37)
//            roomViewModel.insertTaskDone(missedTask38)
//            roomViewModel.insertTaskDone(missedTask39)
//            roomViewModel.insertTaskDone(missedTask40)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp)
                    )
                    .padding(top = 20.dp, start = 15.dp, end = 15.dp, bottom = 15.dp),
            ) {
                var taskTitle by rememberSaveable { mutableStateOf("") }
                var pickedDate by remember { mutableStateOf(LocalDate.now()) }
                var selectedTime by remember { mutableStateOf(LocalTime.now()) }
                var endTime by remember { mutableStateOf(LocalTime.now()) }
                val dateDialogState = rememberMaterialDialogState()
                val timeDialogState = rememberMaterialDialogState()
                val endTimeDialogState = rememberMaterialDialogState()
                var showPopUpMenu = remember { mutableStateOf(false)}

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
                    textStyle = MaterialTheme.typography.bodyMedium,
                    onValueChange = {
                        taskTitle = it
                    },
                    label = { Text(
                        "Title",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    ) },
                    placeholder = {
                        Text(
                            text = "Task title ",
                            modifier = Modifier.alpha(0.4f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp)
                        .height(60.dp),

                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                        placeholderColor = MaterialTheme.colorScheme.onSurface,
                        textColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                MaterialDialog(
                    dialogState = dateDialogState,
                    buttons = {
                        positiveButton(
                            text = "Ok",
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        negativeButton(
                            "Cancel",
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                    ),
                ) {
                    datepicker(
                        initialDate = LocalDate.now(),
                        title = "Pick a date",
                        colors = DatePickerDefaults.colors(
                            headerBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            headerTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            dateActiveBackgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
                            dateActiveTextColor = MaterialTheme.colorScheme.onSurface,
                            calendarHeaderTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        allowedDateValidator = {
                            it > LocalDate.now()
                        },
                    ) { date ->
                        pickedDate = date
                    }
                }
                MaterialDialog(
                    dialogState = timeDialogState,
                    buttons = {
                        positiveButton(
                            text = "Ok",
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        negativeButton(
                            "Cancel",
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                    ),
                ) {
                    timepicker(
                        initialTime = LocalTime.now(),
                        title = "Select a time",
                        is24HourClock = true,
                        colors = TimePickerDefaults.colors(
                            activeBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            inactiveBackgroundColor = MaterialTheme.colorScheme.primary,
                            activeTextColor = MaterialTheme.colorScheme.primary,
                            inactiveTextColor = MaterialTheme.colorScheme.onPrimary,
                            selectorColor = MaterialTheme.colorScheme.secondary,
                            selectorTextColor = MaterialTheme.colorScheme.tertiary
                        )
                    ){
                        selectedTime = it
                    }
                }
                MaterialDialog(
                    dialogState = endTimeDialogState,
                    buttons = {
                        positiveButton(
                            text = "Ok",
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        negativeButton(
                            "Cancel",
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                    ),
                ) {
                    timepicker(
                        initialTime = LocalTime.now(),
                        title = "Select a time",
                        is24HourClock = true,
                        colors = TimePickerDefaults.colors(
                            activeBackgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            inactiveBackgroundColor = MaterialTheme.colorScheme.primary,
                            activeTextColor = MaterialTheme.colorScheme.primary,
                            inactiveTextColor = MaterialTheme.colorScheme.onPrimary,
                            selectorColor = MaterialTheme.colorScheme.secondary,
                            selectorTextColor = MaterialTheme.colorScheme.tertiary
                        )
                    ){
                        endTime = it
                    }
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 20.dp)
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
                            .clickable { dateDialogState.show() },
                    )
                    Text(
                        text = pickedDate.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { dateDialogState.show() }
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
                            .clickable { timeDialogState.show() }
                    )
                    Text(
                        text = "${selectedTime.hour}:${selectedTime.minute}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { timeDialogState.show() }
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
                        text = "${endTime.hour}:${endTime.minute}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clickable { endTimeDialogState.show() }
                    )
                }
                if(showPopUpMenu.value){
                    TaskTypePopUpMenu(modifier = Modifier.zIndex(1f))
                }
                Row (modifier = Modifier.padding(top = 20.dp)){
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
                            .clickable {
                                Log.d("NewTaskFragment" , "new task .click called")
                                showPopUpMenu.value = true
                            }
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            0.5.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            RoundedCornerShape(10.dp),
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
                                .padding(end = 16.dp)
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
                                .padding(end = 16.dp)
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
                                .padding(end = 16.dp)
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
                            .padding(top = 8.dp)
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
                Row (modifier = Modifier.padding(top = 20.dp)){
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
                        .wrapContentHeight()
                ) {
                    for(i in 0 until 2){
                        ConstraintLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(16.dp)
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
                                text = "BDA_Assignment.pdf",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines  = 1,
                                color =  MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .constrainAs(fileName){
                                        top.linkTo(fileType.top)
                                        bottom.linkTo(fileType.bottom)
                                        start.linkTo(fileType.end , 5.dp)
                                        end.linkTo(close.start , 5.dp)
                                        width = Dimension.fillToConstraints
                                    }
                            )
                            Icon(
                                Icons.Filled.Close,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                Modifier.height(0.dp)
                            }else {
                                Modifier.height(15.dp)
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
                        .padding(top = 20.dp)
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

@Composable
fun TaskTypePopUpMenu(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.tertiaryContainer),
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .width(350.dp)
                .wrapContentHeight()
                .background(color = MaterialTheme.colorScheme.tertiary)
                .padding(15.dp)
        ){}
    }
}