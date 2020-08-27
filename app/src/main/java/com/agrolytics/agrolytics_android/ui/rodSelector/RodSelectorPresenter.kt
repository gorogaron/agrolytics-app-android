package com.agrolytics.agrolytics_android.ui.rodSelector

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.agrolytics.agrolytics_android.base.BasePresenter
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult
import com.agrolytics.agrolytics_android.utils.BitmapUtils
import com.agrolytics.agrolytics_android.utils.Detector
import com.agrolytics.agrolytics_android.utils.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.FileInputStream
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException
import kotlin.math.pow

class RodSelectorPresenter(val context: Context) : BasePresenter<RodSelectorScreen>() {

    private var activity: RodSelectorActivity? = null
    private var path: String? = null

    fun setActivity(activity: RodSelectorActivity) {
        this.activity = activity
    }

    fun uploadImage(path: String?, bitmap: Bitmap?, rodLength: Double, rodLengthPixels: Int) {
        Toast.makeText(context, "Offline detection has started.", Toast.LENGTH_SHORT).show()
        handleNoInternet(bitmap!!, rodLength, rodLengthPixels)
    }

    fun saveLocalImageItem(rodLength: Double, rodLengthPixels: Int) {
        val imageItem = ImageItem(
            //TODO: create unique ID generation
            id = (0..10000).random().toString(),
            localPath = path ?: "",
            isPushedToServer = false,
            latitude = Util.lat ?: 0.0,
            longitude = Util.long ?: 0.0,
            length = sessionManager?.length?.toDouble() ?: 0.0,
            volume = 0.0,
            time = Util.getCurrentDateString(),
            selectionMode = "rod",
            rodLength = rodLength,
            rodLengthPixel = rodLengthPixels,
            woodType = sessionManager?.woodType
        )
        screen?.back()
    }


    private fun handleNoInternet(bitmap: Bitmap, rodLength: Double, rodLengthPixels: Int){
        startOfflineDetection(bitmap, rodLength, rodLengthPixels)
    }

    private fun startOfflineDetection(bitmap: Bitmap, rodLength: Double, rodLengthPixels: Int){
        screen?.showLoading()
        doAsync {
            try {
                var seg = Detector.segmentOffline(bitmap!!)
            } catch (e: Exception)
            {
                val a = 2
            }
            uiThread {
                //TODO: Remove mask visualization and volume counting from Detector, it's done in MeasurementResult class
                var measurementResult = MeasurementResult(BitmapUtils.bitmapToBase64(Detector.Result.mask!!)!!,bitmap, rodLength, rodLengthPixels, sessionManager!!.length, Util.getCurrentDateString(), "NoType", Util.lat!!, Util.long!!)
                screen?.successfulUpload(measurementResult, path, "offline")
                screen?.hideLoading()
            }
        }
    }

}