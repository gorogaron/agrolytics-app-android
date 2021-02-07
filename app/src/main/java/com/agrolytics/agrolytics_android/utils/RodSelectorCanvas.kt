package com.agrolytics.agrolytics_android.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.graphics.Bitmap
import kotlin.math.min
import android.graphics.Color.parseColor
import androidx.core.content.ContextCompat
import com.agrolytics.agrolytics_android.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

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
    var zooming = false


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
        //linePaint.color = ContextCompat.getColor(context, R.color.darkGrey)
        //canvas.drawLine(linePoints.x1+3, linePoints.y1+3, linePoints.x2+3, linePoints.y2+3, linePaint)
        //linePaint.color = ContextCompat.getColor(context, R.color.yellow)
        //canvas.drawLine(linePoints.x1, linePoints.y1, linePoints.x2, linePoints.y2, linePaint)

        //Drawing circles
        circlePaint.color = ContextCompat.getColor(context, R.color.darkGrey)
        canvas.drawCircle(linePoints.x1+3, linePoints.y1+3, 60f, circlePaint)
        canvas.drawCircle(linePoints.x2+3, linePoints.y2+3, 60f, circlePaint)
        circlePaint.color = ContextCompat.getColor(context, R.color.yellow)
        canvas.drawCircle(linePoints.x1, linePoints.y1, circleRadius, circlePaint)
        canvas.drawCircle(linePoints.x2, linePoints.y2, circleRadius, circlePaint)
        canvas.drawCircle(linePoints.x1, linePoints.y1, 8f, Paint().apply { color =  ContextCompat.getColor(context, R.color.red) })
        canvas.drawCircle(linePoints.x2, linePoints.y2, 8f, Paint().apply { color =  ContextCompat.getColor(context, R.color.red) })

        if (zooming){
            if (topSelected){
                zoom(canvas, linePoints.x1, linePoints.y1)
            }
            else if (bottomSelected){
                zoom(canvas, linePoints.x2, linePoints.y2)
            }
        }
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
                if (topSelected or bottomSelected) zooming = true
            }
            MotionEvent.ACTION_MOVE -> {
                zooming = false
                if (topSelected){
                    linePoints.x1 = min(max(touchX!!, horizontalOffset), this.width - horizontalOffset)
                    linePoints.y1 = min(max(touchY!!, verticalOffset), this.height - verticalOffset)
                    zooming = true
                }
                if (bottomSelected){
                    linePoints.x2 = min(max(touchX!!, horizontalOffset), this.width - horizontalOffset)
                    linePoints.y2 = min(max(touchY!!, verticalOffset), this.height - verticalOffset)
                    zooming = true
                }
            }
            MotionEvent.ACTION_UP -> {
                topSelected = !getTopTouched(event)
                bottomSelected = !getBottomTouched(event)
                zooming = false
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

    private fun zoom(canvas: Canvas, x: Float, y: Float) {
        val zoomMatrix = Matrix()
        val zoomPos: PointF? = null

        var fixedXPosition = 225f
        if (x < canvas.width/2) fixedXPosition = canvas.width - 225f

        val fixedYPosition = 225f

        // Frame of zoom
        val framePaint = Paint()
        framePaint.style = Paint.Style.STROKE
        framePaint.strokeWidth = 7f

        val resized = addBorder(Bitmap.createScaledBitmap(bitmap, dstRect.width(), dstRect.height(), true))
        val shader = BitmapShader(resized, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        var zoomPaint = Paint()
        zoomPaint.setShader(shader)

        zoomMatrix.reset()
        zoomMatrix.postScale(2f, 2f, x, y - dstRect.top)
        zoomMatrix.postTranslate(
            fixedXPosition-x,
            fixedYPosition-(y-dstRect.top)
        )
        zoomPaint.getShader().setLocalMatrix(zoomMatrix)

        val sizeOfMagnifier = 175
        // Zoom circle
        canvas.drawCircle(
            fixedXPosition,
            fixedYPosition,
            sizeOfMagnifier.toFloat(),
            zoomPaint
        )

        // Zoom circle frame
        framePaint.color = ContextCompat.getColor(context, R.color.red)
        canvas.drawCircle(fixedXPosition, fixedYPosition, 5f, framePaint)
        framePaint.color = parseColor("#444444")
        canvas.drawCircle(fixedXPosition+3, fixedYPosition+3, sizeOfMagnifier.toFloat(), framePaint)
        framePaint.color = ContextCompat.getColor(context, R.color.yellow)
        canvas.drawCircle(fixedXPosition, fixedYPosition, sizeOfMagnifier.toFloat(), framePaint)
    }

    private fun addBorder(bmp: Bitmap): Bitmap {
        val bmpWithBorder = Bitmap.createBitmap(bmp.width, bmp.height, bmp.config)
        val canvas = Canvas(bmpWithBorder)

        val paint = Paint()
        paint.color = parseColor("#444444")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f

        canvas.drawBitmap(bmp,Rect(0,0,canvas.width, canvas.height), Rect(0,0,canvas.width, canvas.height), paint)
        canvas.drawRect(Rect(0, 0, canvas.width-1, canvas.height-1), paint)
        return bmpWithBorder
    }

    //Returns the rod length in pixels if the image was resized
    fun getRodLengthPixels_640_480(): Int{
        var canvasImgHeight = dstRect.bottom - dstRect.top
        var canvasImgWidth = dstRect.right - dstRect.left
        var linePoints_640_480 = LinePoints()

        val wRatio = 640f/canvasImgWidth
        val hRatio = 480f/canvasImgHeight

        linePoints_640_480.x1 = linePoints.x1 * wRatio
        linePoints_640_480.x2 = linePoints.x2 * wRatio
        linePoints_640_480.y1 = linePoints.y1 * hRatio
        linePoints_640_480.y2 = linePoints.y2 * hRatio

        val dy = abs(linePoints_640_480.y2 - linePoints_640_480.y1)
        val dx = abs(linePoints_640_480.x2 - linePoints_640_480.x1)

        return sqrt(dx*dx + dy*dy).toInt()
    }
}