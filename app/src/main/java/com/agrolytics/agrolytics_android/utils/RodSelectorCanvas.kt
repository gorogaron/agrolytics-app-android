package com.agrolytics.agrolytics_android.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.graphics.Bitmap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.math.min
import android.graphics.BlurMaskFilter
import android.graphics.Color.parseColor
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import kotlin.math.abs
import kotlin.math.max


class RodSelectorCanvas(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    data class LinePoints(var x1: Float = 100f, var x2: Float = 200f, var y1: Float = 100f, var y2: Float = 200f)
    var bitmap : Bitmap? = null

    var linePaint = Paint()
    var circlePaint = Paint()
    var borderPaint = Paint()
    var srcRect = Rect()
    var dstRect = Rect()
    val circleRadius = 60f

    var linePoints = LinePoints()
    var topSelected = false
    var bottomSelected = false

    init {
        circlePaint.isAntiAlias = true
        circlePaint.style = Paint.Style.STROKE
        circlePaint.color = Color.WHITE
        circlePaint.strokeWidth = 6f

        linePaint.style = Paint.Style.STROKE
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.color = Color.WHITE
        linePaint.strokeWidth = 3f
    }

    fun setImage(img: Bitmap){
        bitmap = img
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (srcRect.bottom == 0){
            val wRatio = this.width.toFloat() / bitmap!!.width.toFloat()
            val hRatio = this.height.toFloat() / bitmap!!.height.toFloat()
            val scalingRatio = min(wRatio, hRatio)
            srcRect = Rect(0, 0, bitmap!!.width, bitmap!!.height)
            dstRect = Rect(0, 0, (bitmap!!.width * scalingRatio).toInt(), (bitmap!!.height * scalingRatio).toInt())
            val horizontalOffset = (this.width - dstRect.right) / 2
            val verticalOffset = (this.height - dstRect.bottom) / 2
            dstRect.left = dstRect.left + horizontalOffset
            dstRect.right = dstRect.right + horizontalOffset
            dstRect.top = dstRect.top + verticalOffset
            dstRect.bottom = dstRect.bottom + verticalOffset

            linePoints.x1 = 200f
            linePoints.x2 = 200f
            linePoints.y1 = (dstRect.bottom - dstRect.top)  / 4f + verticalOffset
            linePoints.y2 = 3 * (dstRect.bottom - dstRect.top)  / 4f + verticalOffset
        }

        //Drawing image
        canvas.drawBitmap(bitmap, srcRect, dstRect, borderPaint)

        //Drawing line
        linePaint.color = parseColor("#444444")
        canvas.drawLine(linePoints.x1+3, linePoints.y1+3, linePoints.x2+3, linePoints.y2+3, linePaint)
        linePaint.color = Color.WHITE
        canvas.drawLine(linePoints.x1, linePoints.y1, linePoints.x2, linePoints.y2, linePaint)

        //Drawing circles
        circlePaint.color = parseColor("#444444")
        canvas.drawCircle(linePoints.x1+3, linePoints.y1+3, 60f, circlePaint)
        canvas.drawCircle(linePoints.x2+3, linePoints.y2+3, 60f, circlePaint)
        circlePaint.color = Color.WHITE
        canvas.drawCircle(linePoints.x1, linePoints.y1, circleRadius, circlePaint)
        canvas.drawCircle(linePoints.x2, linePoints.y2, circleRadius, circlePaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val touchX = event?.x
        val touchY = event?.y

        val horizontalOffset = (this.width - dstRect.right).toFloat()
        val verticalOffset = (this.height - dstRect.bottom).toFloat()

        when ((event?.action!! and MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                topSelected = getTopTouched(event)
                bottomSelected = getBottomTouched(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (topSelected){
                    linePoints.x1 = min(max(touchX!!, horizontalOffset), this.width - horizontalOffset)
                    linePoints.y1 = min(max(touchY!!, verticalOffset), this.height - verticalOffset)
                }
                if (bottomSelected){
                    linePoints.x2 = min(max(touchX!!, horizontalOffset), this.width - horizontalOffset)
                    linePoints.y2 = min(max(touchY!!, verticalOffset), this.height - verticalOffset)
                }
            }
            MotionEvent.ACTION_UP -> {
                topSelected = !getTopTouched(event)
                bottomSelected = !getBottomTouched(event)
            }

        }
        invalidate()
        return true
    }

    private fun getTopTouched(event: MotionEvent): Boolean {
        val centerX = linePoints.x1
        val centerY = linePoints.y1
        val distanceX = event.x - centerX
        val distanceY = event.y - centerY
        return distanceX * distanceX + distanceY * distanceY <= circleRadius * circleRadius
    }

    private fun getBottomTouched(event: MotionEvent): Boolean {
        val centerX = linePoints.x2
        val centerY = linePoints.y2
        val distanceX = event.x - centerX
        val distanceY = event.y - centerY
        return distanceX * distanceX + distanceY * distanceY <= circleRadius * circleRadius
    }
}