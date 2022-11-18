package com.shubham.tasktrackerapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.Nullable

class CustomView : View {
    private var paint: Paint? = null
    private var xCoord = 0f
    private var yCoord = 0f
    private val path: Path = Path()
    fun setXCoord(xCoord: Float) {
        Log.d("CustomView" , "xCoord - $xCoord")
        this.xCoord = xCoord
    }

    fun setYCoord(yCoord: Float) {
        Log.d("CustomView" , "yCoord - $yCoord")
        this.yCoord = yCoord
        path.lineTo(xCoord, yCoord)
        invalidate()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {
        paint = Paint()
        paint!!.setAntiAlias(true)
        paint!!.setDither(true)
        paint!!.setColor(Color.BLACK)
        paint!!.setStyle(Paint.Style.STROKE)
        paint!!.setStrokeJoin(Paint.Join.ROUND)
        paint!!.setStrokeCap(Paint.Cap.ROUND)
        paint!!.setStrokeWidth(20f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, paint!!)
    }
}