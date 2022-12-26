package com.shubham.tasktrackerapp.upcommingtasks

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.shubham.tasktrackerapp.R
import com.shubham.tasktrackerapp.data.local.Task
import kotlinx.coroutines.*
import java.util.*

/**
 * Adapter for upcoming task recycer view on home page
 */
class TimeLineTasksAdapter(
    private val tasks: List<Task>,
    val context: Context
) : RecyclerView.Adapter<TimeLineTasksAdapter.TimeLineTaskViewHolder>() {
    companion object {
        const val TAG = "TimeLineTaskAdapter"
    }

    private lateinit var mLayoutInflater: LayoutInflater
    private var cal = Calendar.getInstance(Locale.ENGLISH)
    private var firstItem = true
    private val viewHolderList = mutableListOf<TimeLineTaskViewHolder>()
    private var job: Job = Job()
    private val constraintSet = ConstraintSet()

    private val clickListener = View.OnClickListener {
        val tag: String = it.tag as String
        val subStrings = tag.split(":")
        val position = subStrings[0].toInt()
        val check = subStrings[1]
        if (check == "0") {
            // if down arrow is currently displayed then we are changing it to up arrow and making cl
            // layout visible
            viewHolderList[position].attachControlArrow.setImageResource(R.drawable.ic_up_arrow)
            viewHolderList[position].clAttach.visibility = View.VISIBLE
//            viewHolderList[position].clAttach.animate().translationY(
//                context.resources.getDimensionPixelOffset(R.dimen.taskClAttach).toFloat()
//            )
            it.tag = "$position:1"
        } else {
            // else if up arrow is displayed then we are changing it to the down arrow and making the
            // cl layout visibility as gone
            viewHolderList[position].attachControlArrow.setImageResource(R.drawable.ic_down_arrow)
            viewHolderList[position].clAttach.visibility = View.GONE
            it.tag = "$position:0"
        }
    }
    private var popUpMenu: PopupMenu? = null

    inner class TimeLineTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_card_title)
        val addedDate: TextView = itemView.findViewById(R.id.tv_card_task_added_date)
        val dueDate = itemView.findViewById<TextView>(R.id.tv_card_last_date)
        val viewTimeline: View = itemView.findViewById(R.id.view_timeline)
        val txtAttachments: TextView = itemView.findViewById(R.id.tv_txt_attachments)
        val startTime = itemView.findViewById<TextView>(R.id.tv_start_time)
        val attachmentLine: View = itemView.findViewById(R.id.line_attachments)
        val endTime: TextView = itemView.findViewById(R.id.tv_end_time)
        val taskCard = itemView.findViewById<MaterialCardView>(R.id.task_card)
        val ivTaskStop = itemView.findViewById<ImageView>(R.id.iv_task_stop)
        val viewTimeLineCurrent = itemView.findViewById<View>(R.id.view_timeline_current)
        val clTaskType = itemView.findViewById<ConstraintLayout>(R.id.clTaskType)
        val clAttach = itemView.findViewById<ConstraintLayout>(R.id.clAttach)
        val attachControlArrow = itemView.findViewById<ImageView>(R.id.ivArrow)
        val taskOptMenu = itemView.findViewById<ImageView>(R.id.timelineCardBtnMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeLineTaskViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        val view = mLayoutInflater.inflate(R.layout.item_timelineview, parent, false);
        val viewholder = TimeLineTaskViewHolder(view)
        viewHolderList.add(viewholder)
        return viewholder
    }

    override fun onBindViewHolder(holder: TimeLineTaskViewHolder, position: Int) {
        val task = tasks[position]
        if (firstItem) {
            firstItem = false
            holder.viewTimeLineCurrent.visibility = View.VISIBLE
            holder.taskCard.visibility = View.GONE
            holder.ivTaskStop.visibility = View.GONE
            holder.startTime.visibility = View.GONE
            holder.endTime.visibility = View.GONE
        }
        holder.addedDate.text = task.added_date
        holder.title.text = task.title
        holder.startTime.text = task.start_time
        holder.endTime.text = task.end_time
        holder.dueDate.text = task.due_date
        if (task.attachments.size > 0) {
            holder.txtAttachments.text = "${task.attachments.size} Attachments"
        }
        holder.taskCard.setCardBackgroundColor(task.bgColor)
        addTagsToLinearLayout(holder, task)
        if (task.attachments.size > 0) addAttachmentsToLinearLayout(holder, task)
        holder.attachControlArrow.tag = "$position:0"
        //holder.attachControlArrow.setOnClickListener(clickListener)
        holder.taskOptMenu.setOnClickListener {
            showPopUpMenuForTask(it)
        }
    }

    override fun getItemCount(): Int {
        if (tasks.size < 4) return tasks.size
        return 4
    }

    /**
     * Function to show pop up menu for task
     *
     * @param view task item view
     */
    private fun showPopUpMenuForTask(view: View) {
        popUpMenu = PopupMenu(context, view)
        popUpMenu!!.inflate(R.menu.task_card_menu)
        popUpMenu!!.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.taskEdit -> Log.d(TAG, "taskEdit clicked ")
                R.id.deleteTask -> Log.d(TAG, "delete task called")
                R.id.taskMarkDone -> Log.d(TAG, "Mark as done called")
            }
            return@setOnMenuItemClickListener true
        }
        popUpMenu!!.show()
    }

    /**
     * Function to scale the view according the difference between two task time and
     * current time. It creates a animation of real time progress of current time
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun scalingViewsTimeline() {
        val task1TimeArr = viewHolderList[1].startTime.text.toString().split(":").toTypedArray()
        val task2TimeArr = viewHolderList[2].startTime.text.toString().split(":").toTypedArray()
        val task3TimeArr = viewHolderList[3].startTime.text.toString().split(":").toTypedArray()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMin = cal.get(Calendar.MINUTE)
        val currentSec = cal.get(Calendar.SECOND)
        val currentMilliSec = cal.get(Calendar.MILLISECOND)

        var currentTime =
            ((currentHour * 60 + currentMin) * 60 + currentSec) * 1000 + currentMilliSec
        var task1Time = (task1TimeArr[0].toInt() * 60 + task1TimeArr[1].toInt()) * 60 * 1000 * 1L
        var task2Time = (task2TimeArr[0].toInt() * 60 + task2TimeArr[1].toInt()) * 60 * 1000 * 1L
        var task3Time = (task3TimeArr[0].toInt() * 60 + task3TimeArr[1].toInt()) * 60 * 1000 * 1L


        job = GlobalScope.launch(Dispatchers.Main) {
            task1Time = currentTime * 1L
            task2Time = task1Time * 1L
            task3Time = task2Time * 1L
            var isTask1Complete = false
            var isTask2Complete = false

            for (i in 0 until 3) {
                //currentTime = ((currentHour*60 + currentMin)*60 + currentSec)*1000 + currentMilliSec
                if (i == 0) {
                    if (currentTime < task1Time) {
                        val diff = task1Time - currentTime
                        var incr = 500F / diff
                        while (currentTime < task1Time) {
                            scale(viewHolderList[0], incr, 500)
                            delay(500)
                            currentTime += 500
                            incr += 500F / diff
                        }
                        viewHolderList[1].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                        isTask1Complete = true
                    }
                } else if (i == 1) {
                    if (currentTime < task2Time) {
                        val diff = task2Time - currentTime
                        var incr = 500F / diff
                        var count = 0;
                        var i = 0;
                        var check = 0
                        while (currentTime < task2Time) {
                            if (!isTask1Complete) {
                                scale(viewHolderList[0], 1F, 1000)
                                isTask1Complete = true
                                count = 2
                            }
                            i++
                            // using the count variable to not showing scaling for 1 sec so that
                            // first item viewHolderList[0] completes its scaling
                            if (i > count) {
                                if (count == 2 && check == 0) {
                                    check = 1
                                    viewHolderList[1].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                                }
                                scale(viewHolderList[1], incr, 500)
                            }
                            delay(500)
                            currentTime += 500
                            incr += 500F / diff
                        }
                        viewHolderList[2].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                        isTask2Complete = true
                    }
                } else {
                    if (currentTime < task3Time) {
                        val diff = task3Time - currentTime
                        var incr = 500F / diff
                        var count1 = 0;
                        var count2 = 0;
                        var i = 0;
                        var j = 0;
                        var check = 0
                        // count 1 is used to determine when to start scaling timeline1
                        // count 2 is used to determine when to start scaling timeline2
                        if (!isTask2Complete) {
                            count2 = 2
                            if (!isTask1Complete) {
                                count2 = 4
                                count1 = 2
                            }
                        }
                        while (currentTime < task3Time) {
                            i++
                            j++
                            if (!isTask2Complete) {
                                if (!isTask1Complete) {
                                    scale(viewHolderList[0], 1F, 1000)
                                    isTask1Complete = true
                                }
                                if (i > count1) {
                                    if (count1 == 2) viewHolderList[1].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                                    scale(viewHolderList[1], 1F, 1000)
                                    isTask2Complete = true
                                }
                            }
                            if (j > count2) {
                                if (count2 == 4 && check == 0) {
                                    check = 1
                                    viewHolderList[2].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                                }
                                scale(viewHolderList[2], incr, 500)
                            }
                            delay(500)
                            currentTime += 500
                            incr += 500F / diff
                        }
                        viewHolderList[3].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                        job.cancelAndJoin()
                    } else {
                        if (!isTask1Complete) {
                            isTask1Complete = true
                            scale(viewHolderList[0], 1F, 500)
                            delay(500)
                            viewHolderList[1].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                        }
                        if (!isTask2Complete) {
                            isTask2Complete = true
                            scale(viewHolderList[1], 1F, 500)
                            delay(500)
                            viewHolderList[2].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                        }
                        scale(viewHolderList[2], 1F, 500)
                        delay(500)
                        viewHolderList[3].ivTaskStop.setImageResource(R.drawable.ic_task_done)
                        job.cancelAndJoin()
                    }
                }
            }
        }
    }

    /**
     * Function to scale view vertically
     */
    private fun scale(vh: TimeLineTaskViewHolder, j: Float, dur: Long) {
        vh.viewTimeline.pivotY = 0F
        vh.viewTimeline.animate().duration = dur
        vh.viewTimeline.animate().scaleY(j.toFloat())
        vh.viewTimeline.animate().start()
    }

    /**
     * Function to show tags in recycer view item
     * More specifically it add tags in linear layout created inside in the recycer view item
     *
     * @param viewHolder Recycler view item viewholder
     * @param task task to be shown
     */
    @SuppressLint("InflateParams")
    private fun addTagsToLinearLayout(viewHolder: TimeLineTaskViewHolder, task: Task) {

        val id1 = View.generateViewId()
        val id2 = View.generateViewId()
        val tg1 = TextView(context)
        val tg2 = TextView(context)
        tg1.id = id1
        tg2.id = id2
        tg1.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        tg1.setPadding(15, 10, 15, 10)
        tg1.text = task.taskTypes[0]
        tg1.textSize = 10F
        tg1.setTextColor(ContextCompat.getColor(context, R.color.white))

        tg2.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        tg2.setPadding(15, 10, 15, 10)
        tg2.text = task.taskTypes[1]
        tg2.textSize = 10F
        tg2.setTextColor(ContextCompat.getColor(context, R.color.white))

        //giving random background drawable to task tags
        val random = Random()
        when (random.nextInt(4 - 0)) {
            0 -> {
                tg1.setBackgroundResource(R.drawable.bg_rect_blue)
                tg2.setBackgroundResource(R.drawable.bg_rect_orange)
            }
            1 -> {
                tg1.setBackgroundResource(R.drawable.bg_rect_purple)
                tg2.setBackgroundResource(R.drawable.bg_rect_green)
            }
            2 -> {
                tg1.setBackgroundResource(R.drawable.bg_rect_blue)
                tg2.setBackgroundResource(R.drawable.bg_rect_purple)
            }
            else -> {
                tg1.setBackgroundResource(R.drawable.bg_rect_green)
                tg2.setBackgroundResource(R.drawable.bg_rect_orange)
            }
        }

        viewHolder.clTaskType.addView(tg1)
        viewHolder.clTaskType.addView(tg2)
        var id3 = 0

        if (task.taskTypes.size > 2) {
            id3 = View.generateViewId()
            val tvMoreTypes = TextView(context)
            tvMoreTypes.id = id3
            tvMoreTypes.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            tvMoreTypes.text =
                context.getString(R.string.timeLine_task_tags_no, task.taskTypes.size - 2)
            tvMoreTypes.textSize = 10F
            viewHolder.clTaskType.addView(tvMoreTypes)
        }

        constraintSet.clone(viewHolder.clTaskType)

        constraintSet.connect(
            id1,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraintSet.connect(id1, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(id2, ConstraintSet.START, id1, ConstraintSet.END, 15)
        constraintSet.connect(id2, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        if (task.taskTypes.size > 2) {
            constraintSet.connect(id3, ConstraintSet.START, id2, ConstraintSet.END, 15)
            constraintSet.connect(id3, ConstraintSet.TOP, id2, ConstraintSet.TOP)
            constraintSet.connect(id3, ConstraintSet.BOTTOM, id2, ConstraintSet.BOTTOM)
        }
        constraintSet.applyTo(viewHolder.clTaskType)
    }

    /**
     * Function to show attachments in recycer view item
     * More specifically it add attachments in linear layout created inside in the recycer view item
     *
     * @param viewHolder Recycler view item viewholder
     * @param task task to be shown
     */
    @SuppressLint("InflateParams")
    private fun addAttachmentsToLinearLayout(viewHolder: TimeLineTaskViewHolder, task: Task) {
        val bgDrawable: GradientDrawable =
            ContextCompat.getDrawable(context, R.drawable.dates_unselected_background_stroke)
                ?.mutate() as GradientDrawable
        bgDrawable.cornerRadius = 15F
        var uri1 = ""
        var uri2 = ""
        var fn1 = ""
        var fn2 = ""
        for ((key, value) in task.attachments) {
            if (uri1 == "") {
                uri1 = key
                fn1 = value
            } else {
                uri2 = key
                fn2 = value
            }
        }

        val cl1 = ConstraintLayout(context)
        val id1 = View.generateViewId()
        cl1.id = id1
        cl1.setPadding(20, 15, 20, 15)
        cl1.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        cl1.background = bgDrawable
        val idFt1 = View.generateViewId()
        val idFn1 = View.generateViewId()
        val ivFileType1 = ImageView(context)
        ivFileType1.id = idFt1
        val tvFileName1 = TextView(context)
        tvFileName1.id = idFn1
        tvFileName1.text = fn1
        tvFileName1.textSize = 10F
        tvFileName1.maxLines = 1
        var subStr = fn1.split(".")

        var ext = subStr[subStr.size - 1]
        when (ext) {
            "pdf" -> ivFileType1.setImageResource(R.drawable.pdf)
            "png", "jpg", "jpeg" -> ivFileType1.setImageResource(R.drawable.ic_image)
            "txt", "odt" -> ivFileType1.setImageResource(R.drawable.txt)
            "doc", "docx" -> ivFileType1.setImageResource(R.drawable.word)
            "ppt", "pptx" -> ivFileType1.setImageResource(R.drawable.powerpoint)
            "mp4" -> ivFileType1.setImageResource(R.drawable.video)
            else -> ivFileType1.setImageResource(R.drawable.unknown_file_type)
        }

        ivFileType1.layoutParams = ConstraintLayout.LayoutParams(
            36,
            36
        )
        tvFileName1.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        // adding the iv and tv created in the constraint layout created above
        cl1.addView(ivFileType1)
        cl1.addView(tvFileName1)
        constraintSet.clone(cl1)
        // constraining the fileType imageview and filename imageview in the constraint layout
        constraintSet.connect(
            idFt1,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraintSet.connect(idFt1, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(idFn1, ConstraintSet.START, idFt1, ConstraintSet.END, 10)
        constraintSet.connect(idFn1, ConstraintSet.TOP, idFt1, ConstraintSet.TOP)
        constraintSet.connect(idFn1, ConstraintSet.BOTTOM, idFt1, ConstraintSet.BOTTOM)
        constraintSet.applyTo(cl1)

        // now placing the above created constraint layout in the original constraint layout
        // and then constraining it
        viewHolder.clAttach.addView(cl1)
        val id2 = View.generateViewId()

        if (task.attachments.size > 1) {
            val cl2 = ConstraintLayout(context)
            cl2.id = id2
            cl2.setPadding(20, 15, 20, 15)
            cl2.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            cl2.background = bgDrawable
            val idFt2 = View.generateViewId()
            val idFn2 = View.generateViewId()
            val ivFileType2 = ImageView(context)
            ivFileType2.id = idFt2
            val tvFileName2 = TextView(context)
            tvFileName2.id = idFn2
            tvFileName2.text = fn2
            tvFileName2.textSize = 10F
            tvFileName2.maxLines = 1
            subStr = fn2.split(".")
            ext = subStr[subStr.size - 1]

            when (ext) {
                "pdf" -> ivFileType2.setImageResource(R.drawable.pdf)
                "png", "jpg", "jpeg" -> ivFileType2.setImageResource(R.drawable.ic_image)
                "txt", "odt" -> ivFileType2.setImageResource(R.drawable.txt)
                "doc", "docx" -> ivFileType2.setImageResource(R.drawable.word)
                "ppt", "pptx" -> ivFileType2.setImageResource(R.drawable.powerpoint)
                "mp4" -> ivFileType2.setImageResource(R.drawable.video)
                else -> ivFileType2.setImageResource(R.drawable.unknown_file_type)
            }

            ivFileType2.layoutParams = ConstraintLayout.LayoutParams(
                36,
                36
            )
            tvFileName2.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            // adding the iv and tv created in the constraint layout created above
            cl2.addView(ivFileType2)
            cl2.addView(tvFileName2)
            constraintSet.clone(cl2)

            // constraining the fileType imageview and filename imageview in the constraint layout
            constraintSet.connect(
                idFt2,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            constraintSet.connect(
                idFt2,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP
            )
            constraintSet.connect(idFn2, ConstraintSet.START, idFt2, ConstraintSet.END, 10)
            constraintSet.connect(idFn2, ConstraintSet.TOP, idFt2, ConstraintSet.TOP)
            constraintSet.connect(idFn2, ConstraintSet.BOTTOM, idFt2, ConstraintSet.BOTTOM)
            constraintSet.applyTo(cl2)
            viewHolder.clAttach.addView(cl2)
        }
        constraintSet.clone(viewHolder.clAttach)
        constraintSet.connect(
            id1,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraintSet.connect(id1, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraintSet.connect(id1, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        if (task.attachments.size > 1) {
            constraintSet.connect(
                id2,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            constraintSet.connect(
                id2,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )
            constraintSet.connect(id2, ConstraintSet.TOP, id1, ConstraintSet.BOTTOM, 10)
        }
        constraintSet.applyTo(viewHolder.clAttach)
    }
}