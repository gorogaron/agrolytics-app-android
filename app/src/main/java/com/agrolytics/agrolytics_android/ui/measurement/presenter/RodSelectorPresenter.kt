package com.agrolytics.agrolytics_android.ui.measurement.presenter

import android.content.Context
import android.graphics.Bitmap
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.data.database.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.activity.RodSelectorActivity
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.agrolytics.agrolytics_android.utils.MeasurementUtils
import com.agrolytics.agrolytics_android.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class RodSelectorPresenter(val context: Context) : BasePresenter<RodSelectorActivity>() {

    private lateinit var activity: RodSelectorActivity

    fun setActivity(activity: RodSelectorActivity) {
        this.activity = activity
    }

    fun uploadImage(bitmap: Bitmap, rodLength: Double, rodLengthPixels: Double) {
        val unprocessedImageItem = UnprocessedImageItem(
            id = sessionManager?.measurementStartTimestamp!!,
            sessionId = sessionManager?.sessionId!!,
            image = bitmap,
            woodType = sessionManager?.woodType!!,
            woodLength = sessionManager?.woodLength!!.toDouble(),
            lat = Util.lat!!,
            lon = Util.long!!,
            rodLength = rodLength,
            rodLengthPixel = rodLengthPixels,
            timestamp = sessionManager?.measurementStartTimestamp!!
        )

        if (!Util.isNetworkAvailable()) {
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
                        )

                        val processedImageItem = unprocessedImageItem.toProcessedImageItem(maskedImg, volume)
                        activity.hideLoading()
                        MeasurementManager.startApproveMeasurementActivity(activity, processedImageItem, "online")
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