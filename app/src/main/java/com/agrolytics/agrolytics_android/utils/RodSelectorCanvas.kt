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


class RodSelectorCanvas(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    var bitmap : Bitmap? = null

    val linePaint = Paint()
    val circlePaint = Paint()
    var srcRect = Rect()
    var dstRect = Rect()

    init {
        linePaint.isAntiAlias = true
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeCap = Paint.Cap.ROUND
        linePaint.strokeJoin = Paint.Join.ROUND
        linePaint.color = Color.WHITE
        linePaint.alpha = 200
        linePaint.strokeWidth = 3f

        circlePaint.isAntiAlias = true
        circlePaint.style = Paint.Style.STROKE
        circlePaint.strokeCap = Paint.Cap.ROUND
        circlePaint.strokeJoin = Paint.Join.ROUND
        circlePaint.color = Color.WHITE
        circlePaint.alpha = 200
        circlePaint.strokeWidth = 3f
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
        }

        //Drawing image
        canvas.drawBitmap(bitmap, srcRect, dstRect, linePaint)
    }
}