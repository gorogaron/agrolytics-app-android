package com.agrolytics.agrolytics_android.ui.measurement.utils

import android.annotation.SuppressLint
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

class PolyCropperCanvas(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    lateinit var bitmap : Bitmap
    private lateinit var finalImgBlackBackground : Bitmap
    private lateinit var finalImgBlurredBackground : Bitmap

    private val borderPaint = Paint()
    private val polyPointPaint = Paint()
    private val polyPathPaint = Paint()
    private val polyPoints = ArrayList<Point>()
    private var selectedPointIdx : Int? = null
    private var polyFinished = false

    private var srcRect = Rect()
    private var dstRect = Rect()

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
        resetFinalImages()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (srcRect.bottom == 0) //if Rects not initialized
        {
            val wRatio = this.width.toFloat() / bitmap.width.toFloat()
            val hRatio = this.height.toFloat() / bitmap.height.toFloat()
            val scalingRatio = min(wRatio, hRatio)
            srcRect = Rect(0, 0, bitmap.width, bitmap.height)
            dstRect = Rect(0, 0, (bitmap.width * scalingRatio).toInt(), (bitmap.height * scalingRatio).toInt())
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
            val maskWidth = (dstRect.right - dstRect.left)
            val maskHeight = (dstRect.bottom - dstRect.top)
            val bitmap = createPolyMask(polyPoints, maskWidth, maskHeight, dstRect.left, dstRect.top)
            canvas.drawBitmap(bitmap, Rect(0, 0, maskWidth, maskHeight), dstRect, null)
        }

        //Drawing lines
        for ((index, _) in polyPoints.withIndex()) {
            if (index > 0){
                val x1 = polyPoints[index-1].x.toFloat()
                val y1 = polyPoints[index-1].y.toFloat()
                val x2 = polyPoints[index].x.toFloat()
                val y2 = polyPoints[index].y.toFloat()
                canvas.drawLine(x1, y1, x2, y2, polyPathPaint)
                polyPathPaint.color = parseColor("#444444")
                canvas.drawLine(x1+3, y1+3, x2+3, y2+3, polyPathPaint)
                polyPathPaint.color = Color.WHITE
            }
        }

        //Drawing points
        for (point in polyPoints) {
            polyPointPaint.style = Paint.Style.FILL
            polyPointPaint.color = parseColor("#444444")
            canvas.drawCircle(point.x.toFloat() + 3, point.y.toFloat() + 3, 20f, polyPointPaint)
            polyPointPaint.color = ContextCompat.getColor(context, R.color.yellow)
            canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 20f, polyPointPaint)
        }

        //Draw line between first and last point if polygon is finished
        if (polyFinished) {
            val x1 = polyPoints[polyPoints.size-1].x.toFloat()
            val y1 = polyPoints[polyPoints.size-1].y.toFloat()

            val x2 = polyPoints[0].x.toFloat()
            val y2 = polyPoints[0].y.toFloat()
            canvas.drawLine(x1, y1, x2, y2, polyPathPaint)
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
                selectedPointIdx = null
            }

        }
        invalidate()
        return true
    }

    //TODO: create reset button and call this
    fun reset(){
        polyFinished = false
        polyPoints.clear()
        invalidate()
    }

    private fun createPolyMask(polyPoints: ArrayList<Point>, width: Int, height: Int, xOffset: Int, yOffset: Int): Bitmap{

        val path = getTransformedPathFromPoints(polyPoints, xOffset, yOffset)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap!!.eraseColor(Color.TRANSPARENT)
        val maskCanvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.alpha = 100
        paint.style = Paint.Style.FILL

        maskCanvas.drawPath(path, paint)

        return bitmap
    }

    fun crop(): Pair<Bitmap?, Bitmap?> {
        if (polyFinished)
        {
            val transformedPolyPoints = transformPointsToOrigImg()
            val path = getTransformedPathFromPoints(transformedPolyPoints, 0, 0)
            path.fillType = Path.FillType.EVEN_ODD
            val paint = Paint()
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

            /**Draw cropped image on black image*/
            var canvas = Canvas(finalImgBlackBackground)
            canvas.drawPath(path, Paint())
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            /**Draw cropped image on blurred image*/
            // croppedImgBitmap - this will be an image containing the cropped chunk, with transparent background
            val croppedImgBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            canvas = Canvas(croppedImgBitmap)
            canvas.drawPath(path, Paint())
            canvas.drawBitmap(bitmap, 0f, 0f, paint)
            // Copy croppedImgBitmap on blurred original image
            canvas = Canvas(finalImgBlurredBackground)
            canvas.drawBitmap(croppedImgBitmap, 0f, 0f, borderPaint)
            canvas.drawBitmap(createPolyMask(transformedPolyPoints, bitmap.width, bitmap.height, 0, 0), 0f, 0f, borderPaint)

            /**Create bounding rectangle for cropping*/
            val (minX, minY, maxX, maxY) = getMinMaxCoordinatesOfPolygon(transformedPolyPoints)
            val (paddingDeltaX, paddingDeltaY) = getPaddingForAspectRatio(0.75, minX, minY, maxX, maxY)
            val boundingRect = getBoundingRectForCropping(minX, minY, maxX, maxY, paddingDeltaX, paddingDeltaY)

            /**Crop relevant parts of finalImgBlackBackground and finalImgBlurredBackground based on the
            bounding rectangle of the polygon*/
            val blackPaint = Paint()
            blackPaint.color = Color.BLACK
            blackPaint.style = Paint.Style.FILL

            //Crop from finalImgBlurredBackground
            val croppedImgBlurredBackground = Bitmap.createBitmap(maxX - minX + paddingDeltaX, maxY - minY + paddingDeltaY, Bitmap.Config.ARGB_8888)
            var croppedCanvas = Canvas(croppedImgBlurredBackground)
            croppedCanvas.drawPaint(blackPaint) //Make the bmp black. This is needed to avoid transparency when edge of an image is clipped
            croppedCanvas.drawBitmap(finalImgBlurredBackground, boundingRect, Rect(0, 0 , croppedCanvas.width, croppedCanvas.height), borderPaint)

            //Crop from finalImgBlackBackground
            val croppedImgBlackBackground = Bitmap.createBitmap(maxX - minX + paddingDeltaX, maxY - minY + paddingDeltaY, Bitmap.Config.ARGB_8888)
            croppedCanvas = Canvas(croppedImgBlackBackground)
            croppedCanvas.drawPaint(blackPaint)
            croppedCanvas.drawBitmap(finalImgBlackBackground, boundingRect, Rect(0, 0 , croppedCanvas.width, croppedCanvas.height), borderPaint)

            return Pair(croppedImgBlackBackground, croppedImgBlurredBackground)
        } else {
            return Pair(null, null)
        }

    }

    private fun getTransformedPathFromPoints(polyPoints: ArrayList<Point>, xOffset: Int, yOffset: Int): Path{
        val path = Path()
        path.fillType = Path.FillType.INVERSE_EVEN_ODD
        for ((index, point) in polyPoints.withIndex()){

            val x = (point.x - xOffset).toFloat()
            val y = (point.y - yOffset).toFloat()

            if (index == 0){
                path.moveTo(x, y)
            }
            else{
                path.lineTo(x, y)
            }
        }
        return path
    }

    private fun transformPointsToOrigImg(): ArrayList<Point>{
        val xOffset = dstRect.left
        val yOffset = dstRect.top

        val xScale = srcRect.right/(dstRect.right-dstRect.left).toFloat()
        val yScale = srcRect.bottom/(dstRect.bottom - dstRect.top).toFloat()

        val transformedPolyPoints = ArrayList<Point>()

        for (p in polyPoints){
            val x = p.x
            val y = p.y
            transformedPolyPoints.add(Point(x, y))
        }

        for (point in transformedPolyPoints) {
            point.x = ((point.x - xOffset)*xScale).toInt()
            point.y = ((point.y - yOffset)*yScale).toInt()
        }

        return transformedPolyPoints
    }

    //This function calculates the minimum and maximum coordinates of a polygon
    //to get a minimum sized bounding box.
    private fun getMinMaxCoordinatesOfPolygon(polygon: ArrayList<Point>): Array<Int> {
        var minX = this.width
        var minY = this.height
        var maxX = 0
        var maxY = 0
        for (point in polygon){
            if (point.x > maxX) maxX = point.x
            if (point.y > maxY) maxY = point.y

            if (point.x < minX) minX = point.x
            if (point.y < minY) minY = point.y
        }
        return arrayOf(minX, minY, maxX, maxY)
    }

    //This function calculates the horizontal and vertical padding for bounding box defined
    //by min/max x/y points. By adding the padding values to the bounding box left/right/top/bot
    //parameters, the bounding box will have 'aspectRatioNeeded' aspect ratio.
    private fun getPaddingForAspectRatio(aspectRatioNeeded: Double, minX:Int, minY:Int, maxX:Int, maxY:Int) : Pair<Int,Int> {
        var paddingDeltaY = 0
        var paddingDeltaX = 0
        val aspectRatio = (maxY - minY).toDouble()/(maxX - minX).toDouble()
        if (aspectRatio < aspectRatioNeeded) {
            //Add padding to top and bottom
            paddingDeltaY = (aspectRatioNeeded*(maxX - minX) - (maxY - minY)).toInt()
        } else{
            //Add padding to right and left
            paddingDeltaX = (1/aspectRatioNeeded * (maxY - minY) - (maxX - minX)).toInt()
        }
        return Pair(paddingDeltaX, paddingDeltaY)
    }

    private fun getBoundingRectForCropping(minX:Int, minY:Int, maxX:Int, maxY:Int, paddingDeltaX:Int, paddingDeltaY:Int) : Rect{
        val boundingRect = Rect(minX - paddingDeltaX / 2, minY - paddingDeltaY / 2, maxX + paddingDeltaX / 2, maxY + paddingDeltaY / 2)
        val widthOfImage = finalImgBlurredBackground.width    //width of image to be cropped from
        val heightOfImage = finalImgBlurredBackground.height    //height of image to be cropped from

        /**Shift to left is needed*/
        if (boundingRect.left < 0) {
            //If cropped part would be outside the image (left side), shift src rectangle to right to avoid having blank part on image
            val rightShift: Int = if ((boundingRect.right + abs(boundingRect.left)) < widthOfImage) {
                //If possible, shift to right to totally remove blank space
                abs(boundingRect.left)
            } else {
                //Shift right so that blank spaces on right and left will be equal
                val remainingSpaceOnRight = widthOfImage - boundingRect.right
                remainingSpaceOnRight + (abs(boundingRect.left) - remainingSpaceOnRight)/2
            }
            boundingRect.right += rightShift
            boundingRect.left += rightShift
        }

        /**Shift to right if needed*/
        if (boundingRect.right > widthOfImage) {
            //If cropped part would be outside the image (right side), shift src rectangle to right to avoid having blank part on image
            val leftShift: Int = if ((boundingRect.left - (boundingRect.right - widthOfImage)) > 0) {
                //If possible, shift to left to totally remove blank space
                boundingRect.right - widthOfImage
            } else {
                //Shift left so that blank spaces on right and left will be equal
                boundingRect.left + (boundingRect.right - widthOfImage - boundingRect.left)/2
            }
            boundingRect.left -= leftShift
            boundingRect.right -= leftShift
        }

        /**Shift to bottom if needed*/
        if (boundingRect.top < 0){
            val bottomShift : Int = if ((boundingRect.bottom + abs(boundingRect.top)) < heightOfImage){
                abs(boundingRect.top)
            }
            else {
                heightOfImage + (abs(boundingRect.top) - heightOfImage)/2
            }
            boundingRect.top += bottomShift
            boundingRect.bottom += bottomShift
        }

        /**Shift to top if needed*/
        if (boundingRect.bottom > heightOfImage){
            val topShift : Int = if (boundingRect.top - (boundingRect.bottom - heightOfImage) > 0) {
                boundingRect.bottom - heightOfImage
            }
            else {
                boundingRect.top + (boundingRect.bottom - heightOfImage - boundingRect.top)/2
            }
            boundingRect.top -= topShift
            boundingRect.bottom -= topShift
        }

        return boundingRect
    }

    fun resetFinalImages(){
        finalImgBlackBackground = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        //TODO: Find a better way to blur image
        //Create blurred image by down and upscaling image
        val scaleRatioForBlurring = 0.05f
        finalImgBlurredBackground = Bitmap.createScaledBitmap(bitmap, (bitmap.width*scaleRatioForBlurring).toInt(), (bitmap.height*scaleRatioForBlurring).toInt(), true)
        finalImgBlurredBackground = Bitmap.createScaledBitmap(finalImgBlurredBackground, bitmap.width, bitmap.height, true)
    }

}