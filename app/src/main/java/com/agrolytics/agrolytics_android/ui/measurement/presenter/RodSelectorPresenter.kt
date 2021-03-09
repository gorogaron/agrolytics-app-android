package com.agrolytics.agrolytics_android.ui.measurement.presenter

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.activity.RodSelectorActivity
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.agrolytics.agrolytics_android.utils.MeasurementUtils
import com.agrolytics.agrolytics_android.utils.Util
import com.agrolytics.agrolytics_android.utils.Util.Companion.round
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class RodSelectorPresenter(val context: Context) : BasePresenter<RodSelectorActivity>() {

    private lateinit var activity: RodSelectorActivity

    fun setActivity(activity: RodSelectorActivity) {
        this.activity = activity
    }

    fun uploadImage(bitmap: Bitmap, rodLength: Double, rodLengthPixels: Double) {
        if (Util.lat == null || Util.lat == null) {
            Util.lat = 0.0
            Util.long = 0.0
        }
        val unprocessedImageItem = UnprocessedImageItem(
            sessionId = MeasurementManager.currentSessionId,
            image = bitmap,
            woodType = sessionManager?.woodType!!,
            woodLength = sessionManager?.woodLength!!.toDouble(),
            location = GeoPoint(Util.lat!!, Util.long!!),
            rodLength = rodLength,
            rodLengthPixel = rodLengthPixels.round(2),
            timestamp = sessionManager?.measurementStartTimestamp!!
        )

        if (activity.isInternetAvailable) {
            activity.showOnlineMeasurementErrorDialog(unprocessedImageItem)
        }
        else {
            activity.showLoading()
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    val response = MeasurementManager.startOnlineMeasurement(bitmap)
                    if (response.isSuccessful) {
                        val maskImg = response.body()!!.toBitmap()
                        val (maskedImg, numOfWoodPixels) = ImageUtils.drawMaskOnInputImage(bitmap, maskImg)
                        val volume = MeasurementUtils.calculateWoodVolume(
                            numOfWoodPixels,
                            unprocessedImageItem.rodLength,
                            unprocessedImageItem.rodLengthPixel,
                            unprocessedImageItem.woodLength
                        ).round(2)

                        val processedImageItem = unprocessedImageItem.toProcessedImageItem(maskedImg, volume)
                        activity.hideLoading()
                        MeasurementManager.startApproveMeasurementActivity(activity, processedImageItem, "online")
                        activity.finish()
                        RodSelectorActivity.correspondingCropperActivity!!.finish()
                        RodSelectorActivity.correspondingCropperActivity = null

                    } else {
                        activity.hideLoading()
                        activity.showOnlineMeasurementErrorDialog(unprocessedImageItem)
                    }
                }
                catch (e : Exception){
                    activity.hideLoading()
                    activity.showOnlineMeasurementErrorDialog(unprocessedImageItem)
                }
            }
        }
    }


    private fun startOfflineDetection(unprocessedImageItem: UnprocessedImageItem){
        //TODO: segmentor = ImageSegmentation()
        //      val (mask, numOfPixels) = segmentor.segment(inputImg)
    }

}