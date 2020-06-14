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


class PolyCropper(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    var bitmap : Bitmap? = null
    var finalImg : Bitmap? = null

    val borderPaint = Paint()
    val polyPointPaint = Paint()
    val polyPathPaint = Paint()
    val polyPoints = ArrayList<Point>()
    var selectedPointIdx : Int? = null
    var polyFinished = false

    var srcRect = Rect()
    var dstRect = Rect()

    init {
        polyPointPaint.isAntiAlias = true
        polyPointPaint.color = Color.WHITE
        polyPointPaint.strokeWidth = 4f
        polyPointPaint.style = Paint.Style.STROKE
        polyPointPaint.strokeCap = Paint.Cap.ROUND
        polyPointPaint.strokeJoin = Paint.Join.ROUND

        polyPathPaint.isAntiAlias = true
        polyPathPaint.style = Paint.Style.STROKE
        polyPathPaint.strokeCap = Paint.Cap.ROUND
        polyPathPaint.strokeJoin = Paint.Join.ROUND
        polyPathPaint.color = Color.WHITE
        polyPathPaint.alpha = 200
        polyPathPaint.strokeWidth = 3f

        borderPaint.isAntiAlias = true
        borderPaint.color = Color.BLACK
        borderPaint.strokeWidth = 5f
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeCap = Paint.Cap.ROUND
        borderPaint.strokeJoin = Paint.Join.ROUND

    }

    fun setImageBitmap(img: Bitmap){
        bitmap = img
        finalImg = Bitmap.createBitmap(bitmap!!.width, bitmap!!.height, Bitmap.Config.ARGB_8888) //TODO : REMOVE!!!
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (srcRect.bottom == 0) //if Rects not initialized
        {
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
        }
        //Drawing image
        canvas.drawBitmap(bitmap, srcRect, dstRect, borderPaint)

        //Drawing transparent black bg around finished polygon
        if (polyFinished) {
            var maskWidth = (dstRect.right - dstRect.left)
            var maskHeight = (dstRect.bottom - dstRect.top)
            var bitmap = createPolyMask(polyPoints, maskWidth, maskHeight, dstRect.left, dstRect.top)
            canvas.drawBitmap(bitmap, Rect(0, 0, maskWidth, maskHeight), dstRect, null)
        }

        for ((index, _) in polyPoints.withIndex()) {
            if (index > 0){
                var x1 = polyPoints[index-1].x.toFloat()
                var y1 = polyPoints[index-1].y.toFloat()

                var x2 = polyPoints[index].x.toFloat()
                var y2 = polyPoints[index].y.toFloat()

                canvas.drawLine(x1, y1, x2, y2, polyPathPaint)
                polyPathPaint.color = parseColor("#444444")
                canvas.drawLine(x1+3, y1+3, x2+3, y2+3, polyPathPaint)
                polyPathPaint.color = Color.WHITE
            }
        }

        if (polyFinished) {
            var x1 = polyPoints[polyPoints.size-1].x.toFloat()
            var y1 = polyPoints[polyPoints.size-1].y.toFloat()

            var x2 = polyPoints[0].x.toFloat()
            var y2 = polyPoints[0].y.toFloat()
            canvas.drawLine(x1, y1, x2, y2, polyPathPaint)
        }

        for ((index, point) in polyPoints.withIndex()) {
            polyPointPaint.style = Paint.Style.FILL
            if (index == selectedPointIdx) {
                polyPointPaint.color = Color.WHITE
            } else {
                polyPointPaint.color = Color.WHITE
            }
            polyPointPaint.color = parseColor("#444444")
            canvas.drawCircle(point.x.toFloat() + 3, point.y.toFloat() + 3, 20f, polyPointPaint)
            polyPointPaint.color = Color.WHITE
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 20f, polyPointPaint)

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val touchX = event?.x
        val touchY = event?.y

        when ((event?.action!! and MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                var addNewPoint = true
                for ((index, point) in polyPoints.withIndex())
                {
                    val xInRange = touchX!! in (point.x.toFloat() - 40f .. point.x.toFloat() + 40f)
                    val yInRange = touchY!! in (point.y.toFloat() - 40f .. point.y.toFloat() + 40f)
                    if (xInRange and yInRange) {
                        selectedPointIdx = index
                        addNewPoint = false
                        break
                    }
                }
                if (selectedPointIdx == 0 && polyPoints.size > 1) {
                    polyFinished = true
                }
                if (addNewPoint && !polyFinished){
                    polyPoints.add(Point(touchX!!.toInt(), touchY!!.toInt()))
                    selectedPointIdx = polyPoints.size - 1
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (selectedPointIdx != null){
                    polyPoints[selectedPointIdx!!].x = touchX!!.toInt()
                    polyPoints[selectedPointIdx!!].y = touchY!!.toInt()
                }
                //System.out.println("Moving event : x = " + touchX + " y = " + touchY)
            }
            MotionEvent.ACTION_UP -> {
                System.out.println("Up event : x = " + touchX + " y = " + touchY)
                selectedPointIdx = null
            }

        }
        invalidate()
        return true
    }

    fun reset(){
        polyFinished = false
        polyPoints.clear()
        invalidate()
    }

    fun createPolyMask(polyPoints: ArrayList<Point>, width: Int, height: Int, xOffset: Int, yOffset: Int): Bitmap{

        var path = getTransformedPathFromPoints(polyPoints, xOffset, yOffset)
        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap!!.eraseColor(Color.TRANSPARENT)
        var maskCanvas = Canvas(bitmap)
        var paint = Paint()
        paint.setColor(Color.BLACK)
        paint.alpha = 100
        paint.setStyle(Paint.Style.FILL)

        maskCanvas.drawPath(path, paint)

        return bitmap
    }

    fun crop(): Bitmap{
        val transformedPolyPoints = transformPointsToOrigImg()
        var path = getTransformedPathFromPoints(transformedPolyPoints, 0, 0)
        path.fillType = Path.FillType.EVEN_ODD
        var paint = Paint()

        paint.setColor(Color.BLACK)

        val canvas = Canvas(finalImg)
        canvas.drawPath(path, paint);
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0f, 0f, paint);

        var minX = canvas.width
        var minY = canvas.height
        var maxX = 0
        var maxY = 0
        for (point in transformedPolyPoints){
            if (point.x > maxX) maxX = point.x
            if (point.y > maxY) maxY = point.y

            if (point.x < minX) minX = point.x
            if (point.y < minY) minY = point.y
        }

        var paddingDeltaY = 0
        var paddingDeltaX = 0
        val aspectRatio = (maxY - minY)/(maxX - minX)
        if (aspectRatio < 0.75) {
            //Add padding to top and bottom
            paddingDeltaY = (0.75*(maxX - minX) - (maxY - minY)).toInt()
        } else{
            //Add padding to right and left
            paddingDeltaX = (1/0.75 * (maxY - minY) - (maxX - minX)).toInt()
        }

        var croppedImg = Bitmap.createBitmap(maxX - minX + paddingDeltaX, maxY - minY + paddingDeltaY, Bitmap.Config.ARGB_8888) //TODO : REMOVE!!!
        var blackPaint = Paint()
        blackPaint.setColor(Color.BLACK)
        blackPaint.style = Paint.Style.FILL
        val croppedCanvas = Canvas(croppedImg)
        croppedCanvas.drawPaint(blackPaint)
        var boundingRect = Rect(minX, minY, maxX, maxY)
        croppedCanvas.drawBitmap(finalImg, boundingRect, Rect(paddingDeltaX/2, paddingDeltaY/2 ,croppedCanvas.width - paddingDeltaX/2, croppedCanvas.height - paddingDeltaY/2), blackPaint)

        val croppedHeight = maxY - minY
        val croppedWidth = maxX - minX
        val xScale = 640 / croppedWidth
        val yScale = 480 / croppedHeight

        return croppedImg
    }

    fun getTransformedPathFromPoints(polyPoints: ArrayList<Point>, xOffset: Int, yOffset: Int): Path{
        var path = Path()
        path.fillType = Path.FillType.INVERSE_EVEN_ODD
        for ((index, point) in polyPoints.withIndex()){

            var x = (point.x - xOffset).toFloat()
            var y = (point.y - yOffset).toFloat()

            if (index == 0){
                path.moveTo(x, y)
            }
            else{
                path.lineTo(x, y)
            }
        }
        return path
    }

    fun transformPointsToOrigImg(): ArrayList<Point>{
        val xOffset = dstRect.left
        val yOffset = dstRect.top

        val xScale = srcRect.right/(dstRect.right-dstRect.left).toFloat()
        val yScale = srcRect.bottom/(dstRect.bottom - dstRect.top).toFloat()

        var transformedPolyPoints = ArrayList<Point>()

        for (p in polyPoints){
            var x = p.x
            var y = p.y
            transformedPolyPoints.add(Point(x, y))
        }

        for (point in transformedPolyPoints) {
            point.x = ((point.x - xOffset)*xScale).toInt()
            point.y = ((point.y - yOffset)*yScale).toInt()
        }

        return transformedPolyPoints
    }

}