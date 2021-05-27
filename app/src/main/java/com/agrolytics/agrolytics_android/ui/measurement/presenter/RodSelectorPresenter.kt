package com.agrolytics.agrolytics_android.ui.measurement.presenter

import android.content.Context
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.awaitResponse


class RodSelectorPresenter(val context: Context) : BasePresenter<RodSelectorActivity>() {

    val TAG = "RodSelectorPresenter"
    private lateinit var activity: RodSelectorActivity

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

        GlobalScope.launch(Dispatchers.Main) {
            var apiCall : Call<ImageUploadResponse>? = null
            try {
                apiCall = MeasurementManager.startOnlineMeasurement(RodSelectorActivity.croppedResizedImageBlackBg!!)
                activity.showUploadProgressbar(apiCall)
                val response = apiCall.awaitResponse()
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
                    //TODO: Try catch eltávolítás, csak mecsek miatt raktam ide
                    try {
                        Log.d(TAG, "Response is not successful")
                        Log.d(TAG, "Response: ${response.body().toString()}")
                    } catch (e: java.lang.Exception){}

                    activity.hideLoading()
                    activity.showOnlineMeasurementErrorDialog()
                }
            }
            catch (e : Exception){
                if (apiCall != null && apiCall.isCanceled) {
                    //Ha ki lett nyomva a hívás a "vissza" gombbal
                        Log.d(TAG, "API call has been cancelled")
                    return@launch
                }
                else {
                    //Ha hiba történt. Pl.: token request timeout, retrofit timeout
                        try {
                          Log.d(TAG, "Exception during API call: $e")
                          Log.d(TAG, "Error message: ${e.message}")
                        } catch (e: java.lang.Exception) { }
                    activity.hideLoading()
                    activity.showOnlineMeasurementErrorDialog()
                }
            }
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