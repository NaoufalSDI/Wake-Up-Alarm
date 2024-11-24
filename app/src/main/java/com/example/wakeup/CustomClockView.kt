package com.example.wakeup

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.Calendar

class CustomClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val hourHandPaint = Paint().apply {
        color = Color.parseColor("#8E98A1")
        strokeWidth = 12f
        isAntiAlias = true
    }

    private val minuteHandPaint = Paint().apply {
        color = Color.parseColor("#8E98A1")
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val secondHandPaint = Paint().apply {
        color = Color.parseColor("#FD251E")
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val centerPointPaint = Paint().apply {
        color = secondHandPaint.color
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (Math.min(width, height) / 2.5).toFloat()

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        // Draw hour hand
        val hourAngle = Math.toRadians((hour + minute / 60.0) * 30 - 90)
        val hourHandLength = radius * 0.43f
        canvas.drawLine(
            centerX,
            centerY,
            (centerX + Math.cos(hourAngle) * hourHandLength).toFloat(),
            (centerY + Math.sin(hourAngle) * hourHandLength).toFloat(),
            hourHandPaint
        )

        // Draw minute hand
        val minuteAngle = Math.toRadians((minute + second / 60.0) * 6 - 90)
        val minuteHandLength = radius * 0.5f
        canvas.drawLine(
            centerX,
            centerY,
            (centerX + Math.cos(minuteAngle) * minuteHandLength).toFloat(),
            (centerY + Math.sin(minuteAngle) * minuteHandLength).toFloat(),
            minuteHandPaint
        )

        // Draw second hand
        val secondAngle = Math.toRadians(second * 6.0 - 90)
        val secondHandLength = radius * 0.7f
        canvas.drawLine(
            centerX,
            centerY,
            (centerX + Math.cos(secondAngle) * secondHandLength).toFloat(),
            (centerY + Math.sin(secondAngle) * secondHandLength).toFloat(),
            secondHandPaint
        )

        canvas.drawCircle(centerX, centerY, 8f, centerPointPaint)

        // Redraw the view every second to update the hands
        postInvalidateDelayed(1000)
    }
}
