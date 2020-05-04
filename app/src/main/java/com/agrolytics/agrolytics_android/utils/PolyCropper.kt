//package com.example.polycrop //TODO: FIX PACKAGE NAME

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import kotlin.math.min
import com.agrolytics.agrolytics_android.R



class PolyCropper(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    val bitmap : Bitmap
    val borderPaint = Paint()
    val polyPointPaint = Paint()
    val polyPathPaint = Paint()
    val polyPoints = ArrayList<Point>()
    val polyPath = Path()
    var selectedPointIdx : Int? = null
    var polyFinished = false

    var srcRect = Rect()
    var dstRect = Rect()

    var done = false
    var finalImg : Bitmap

    init {

        bitmap = BitmapFactory.decodeResource(resources, R.drawable.dummy)
        finalImg = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888) //TODO : REMOVE!!!
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
        polyPathPaint.setShadowLayer(1f,2f,2f,Color.BLACK)
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!done) {
            val wRatio = this.width.toFloat() / bitmap.width.toFloat()
            val hRatio = this.height.toFloat() / bitmap.height.toFloat()
            val scalingRatio = min(wRatio, hRatio)


            srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            dstRect = Rect(
                0,
                0,
                (bitmap.width * scalingRatio).toInt(),
                (bitmap.height * scalingRatio).toInt()
            )
            val horizontalOffset = (this.width - dstRect.right) / 2
            val verticalOffset = (this.height - dstRect.bottom) / 2
            dstRect.left = dstRect.left + horizontalOffset
            dstRect.right = dstRect.right + horizontalOffset
            dstRect.top = dstRect.top + verticalOffset
            dstRect.bottom = dstRect.bottom + verticalOffset
            //Drawing image
            canvas.drawBitmap(bitmap, srcRect, dstRect, borderPaint)

            if (polyFinished) {
                var maskWidth = (dstRect.right - dstRect.left)
                var maskHeight = (dstRect.bottom - dstRect.top)
                var bitmap =
                    createPolyMask(polyPoints, maskWidth, maskHeight, dstRect.left, dstRect.top)
                canvas.drawBitmap(bitmap, Rect(0, 0, maskWidth, maskHeight), dstRect, null)
            }

            polyPath.reset()
            for ((index, point) in polyPoints.withIndex()) {
                if (index == 0) {
                    polyPath.moveTo(point.x.toFloat(), point.y.toFloat())
                } else {
                    polyPath.lineTo(point.x.toFloat(), point.y.toFloat())
                }
            }

            if (polyFinished) {
                polyPath.lineTo(polyPoints[0].x.toFloat(), polyPoints[0].y.toFloat())
            }

            canvas.drawPath(polyPath, polyPathPaint)

            for ((index, point) in polyPoints.withIndex()) {
                polyPointPaint.style = Paint.Style.FILL
                if (index == selectedPointIdx) {
                    polyPointPaint.color = Color.WHITE
                } else {
                    polyPointPaint.color = Color.WHITE
                }
                polyPointPaint.setShadowLayer(1f, 2f, 2f, Color.BLACK)
                //polyPointPaint.alpha = 175
                canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 20f, polyPointPaint)
            }
        }
        else{
            val wRatio = this.width.toFloat() / bitmap.width.toFloat()
            val hRatio = this.height.toFloat() / bitmap.height.toFloat()
            val scalingRatio = min(wRatio, hRatio)


            srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            dstRect = Rect(
                0,
                0,
                (bitmap.width * scalingRatio).toInt(),
                (bitmap.height * scalingRatio).toInt()
            )
            val horizontalOffset = (this.width - dstRect.right) / 2
            val verticalOffset = (this.height - dstRect.bottom) / 2
            dstRect.left = dstRect.left + horizontalOffset
            dstRect.right = dstRect.right + horizontalOffset
            dstRect.top = dstRect.top + verticalOffset
            dstRect.bottom = dstRect.bottom + verticalOffset
            //Drawing image
            canvas.drawBitmap(finalImg, srcRect, dstRect, borderPaint)
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
                if (selectedPointIdx == 0) {
                    polyFinished = true
                }
                if (addNewPoint){
                    polyPoints.add(Point(touchX!!.toInt(), touchY!!.toInt()))
                    selectedPointIdx = polyPoints.size - 1
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (selectedPointIdx != null){
                    polyPoints[selectedPointIdx!!].x = touchX!!.toInt()
                    polyPoints[selectedPointIdx!!].y = touchY!!.toInt()
                }
                System.out.println("Moving event : x = " + touchX + " y = " + touchY)
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
        polyPath.reset()
        polyPoints.clear()
        invalidate()
    }

    fun createPolyMask(polyPoints: ArrayList<Point>, width: Int, height: Int, xOffset: Int, yOffset: Int): Bitmap{

        var path = getTransformedPathFromPoints(polyPoints, xOffset, yOffset)
        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
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


        var croppedImg = Bitmap.createBitmap(maxX - minX, maxY - minY, Bitmap.Config.ARGB_8888) //TODO : REMOVE!!!
        var blackPaint = Paint()
        blackPaint.setColor(Color.BLACK)
        blackPaint.style = Paint.Style.FILL
        val croppedCanvas = Canvas(croppedImg)
        croppedCanvas.drawPaint(blackPaint)
        var boundingRect = Rect(minX, minY, maxX, maxY)
        croppedCanvas.drawBitmap(finalImg, boundingRect, Rect(0, 0,croppedCanvas.width, croppedCanvas.height), blackPaint)

        done = true
        invalidate()
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

        var transformedPolyPoints = polyPoints

        for (point in transformedPolyPoints) {
            point.x = ((point.x - xOffset)*xScale).toInt()
            point.y = ((point.y - yOffset)*yScale).toInt()
        }

        return transformedPolyPoints
    }
}