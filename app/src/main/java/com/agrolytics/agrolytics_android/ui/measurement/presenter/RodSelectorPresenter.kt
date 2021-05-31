package com.agrolytics.agrolytics_android.ui.measurement.presenter

import android.content.Context
import android.content.DialogInterface.OnCancelListener
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.network.model.ImageUploadResponse
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.activity.RodSelectorActivity
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.agrolytics.agrolytics_android.utils.MeasurementUtils
import com.agrolytics.agrolytics_android.utils.Util
import com.agrolytics.agrolytics_android.utils.Util.Companion.round
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.awaitResponse


class RodSelectorPresenter(val context: Context) : BasePresenter<RodSelectorActivity>() {

    val TAG = "RodSelectorPresenter"
    private lateinit var activity: RodSelectorActivity
    var apiCallCanceled = false

    fun setActivity(activity: RodSelectorActivity) {
        this.activity = activity
    }

    fun uploadImage(rodLength: Double, rodLengthPixels: Double) {
        Log.d(TAG, "Processing has been triggered.")
        if (Util.lat == null || Util.lat == null) {
            Log.d(TAG, "GPS coordinates are nulls")
            Util.lat = 0.0
            Util.long = 0.0
        }

        activity.unprocessedImageItem = createUnprocessedImageItem(rodLength, rodLengthPixels)
        val uploadJob = startUploadJob()

        val loadingBarCancelListener = OnCancelListener{
            apiCallCanceled = true
            uploadJob.cancel()
        }
        activity.showLoading(true, loadingBarCancelListener)

    }

    private fun startUploadJob() = GlobalScope.launch(Dispatchers.Main) {
        try {
            val response : Response<ImageUploadResponse>
            withContext(Dispatchers.IO) {
                val apiCall = MeasurementManager.startOnlineMeasurement(RodSelectorActivity.croppedResizedImageBlackBg!!)

                //TODO: okHttp timeout nem mindig működik, ezért a 'withTimeout'. (https://github.com/square/okhttp/issues/4455)
                withTimeout(65000) {
                    response = apiCall.awaitResponse()
                }
            }
            processResponse(response)
        }
        catch (e : Exception){
            activity.hideLoading()

            Log.d(TAG, "Exception during API call: $e")
            if (apiCallCanceled) {
                apiCallCanceled = false
            }
            else {
                activity.showOnlineMeasurementErrorDialog()
            }
        }
    }

    private fun processResponse(response : Response<ImageUploadResponse>) {
        if (response.isSuccessful) {
            Log.d(TAG, "Response is successful")
            val maskImg = response.body()!!.toBitmap()
            val resizedImageToDraw = Bitmap.createScaledBitmap(RodSelectorActivity.croppedImageBlurredBg!!, 640, 480, true)
            val (maskedImg, numOfWoodPixels) = ImageUtils.drawMaskOnInputImage(resizedImageToDraw, maskImg)
            val processedImageItem = ProcessedImageItem(activity.unprocessedImageItem!!, numOfWoodPixels, maskedImg)

            activity.hideLoading()
            MeasurementManager.startApproveMeasurementActivity(activity, processedImageItem, activity.unprocessedImageItem!!,"online")
            activity.finish()
            RodSelectorActivity.correspondingCropperActivity!!.finish()
            RodSelectorActivity.correspondingCropperActivity = null

        } else {
            Log.d(TAG, "Response is not successful")
            Log.d(TAG, "Response: ${response.body().toString()}")

            activity.hideLoading()
            activity.showOnlineMeasurementErrorDialog()
        }
    }

    private fun createUnprocessedImageItem(rodLength: Double, rodLengthPixels: Double) : UnprocessedImageItem {
        return UnprocessedImageItem(
            sessionId = MeasurementManager.currentSessionId,
            image = Bitmap.createScaledBitmap(RodSelectorActivity.croppedImageBlurredBg!!, 640, 480, true),
            woodType = sessionManager?.woodType!!,
            woodLength = sessionManager?.woodLength!!.toDouble(),
            location = GeoPoint(Util.lat!!, Util.long!!),
            rodLength = rodLength,
            rodLengthPixel = rodLengthPixels.round(2),
            timestamp = sessionManager?.measurementStartTimestamp!!
        )
    }

}