package com.agrolytics.agrolytics_android.ui.measurement.presenter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.data.database.tables.CachedImageItem
import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.activity.RodSelectorActivity
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.agrolytics.agrolytics_android.ui.measurement.utils.Detector
import com.agrolytics.agrolytics_android.utils.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception
import java.net.SocketTimeoutException

class RodSelectorPresenter(val context: Context) : BasePresenter<RodSelectorActivity>() {

    private var activity: RodSelectorActivity? = null
    private var path: String? = null

    fun setActivity(activity: RodSelectorActivity) {
        this.activity = activity
    }

    fun uploadImage(path: String?, bitmap: Bitmap?, rodLength: Double, rodLengthPixels: Int) {
        this.path = path
        if (!Util.isNetworkAvailable()) {
            handleNoInternet(bitmap!!, rodLength, rodLengthPixels)
        } else {
            activity?.showLoading()
            GlobalScope.launch(Dispatchers.Main) {
                activity?.showLoading()
                try{
                    val response = MeasurementManager.startOnlineMeasurement(bitmap!!)
                    if (response.isSuccessful) {
                        if (Util.lat == null || Util.long == null) {
                            Util.lat = 0.0
                            Util.long = 0.0
                        }
                        val measurementResult = MeasurementResult(
                            response.body()?.mask,
                            bitmap!!,
                            rodLength,
                            rodLengthPixels,
                            sessionManager!!.woodLength,
                            Util.getCurrentDateString(),
                            sessionManager!!.woodType,
                            Util.lat!!,
                            Util.long!!
                        )
                        activity?.successfulUpload(measurementResult, path, "online")
                    } else {
                        activity?.let {
                            activity?.showAlertDialog(
                                "Hiba",
                                "Gond van a szerverünkkel. Próbáld újra, vagy jelezd felénk a hibát.",
                                it, true, "Mentés"
                            )
                        }
                    }
                }
                catch (e : Exception){
                    if (e is SocketTimeoutException) {
                        activity?.let {
                            activity?.showAlertDialog(
                                "Nincs internet kapcsolat",
                                "A kapcsolat időtúllépés miatt megszakadt. Szeretnéd elmenteni a képet?",
                                it, true, "Mentés"
                            )
                        }
                    }
                    else{
                        activity?.let {
                            activity?.showAlertDialog(
                                "Hiba",
                                "Váratlan hiba történt. Mentsd le a képet későbbre, vagy próbáld újra.",
                                it, true, "Mentés"
                            )
                        }
                    }
                }
                activity?.hideLoading()
            }
        }
    }

    fun saveLocalImageItem(rodLength: Double, rodLengthPixels: Int) {
//        val imageItem = CachedImageItem(
//            //TODO: create unique ID generation and sessionID
//            id = (0..10000).random().toLong(),
//            sessionId = "",
//            localPath = path ?: "",
//            lat = Util.lat ?: 0.0,
//            lon = Util.long ?: 0.0,
//            woodLength = sessionManager?.woodLength?.toDouble()!!,
//            woodVolume = 0.0,
//            timestamp = Util.getCurrentDateString().toLong(),
//            woodType = sessionManager?.woodType!!
//        )
        doAsync {
//            dataClient?.local?.addImage(imageItem)
            activity?.back()
        }
    }

    private fun createImageUploadRequest(bitmap: Bitmap?, rodLength: Double, rodLengthPixels: Int): ImageUploadRequest {
        //val file = File(path)
        val imageUploadRequest = ImageUploadRequest()
        bitmap?.let { imageUploadRequest.image = ImageUtils.bitmapToBase64(bitmap) }
        return imageUploadRequest
    }

    private fun handleNoInternet(bitmap: Bitmap, rodLength: Double, rodLengthPixels: Int){
        activity?.let{
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Nincs internetkapcsolat")
            builder.setMessage("Nem megfelelő az internetkapcsolat. A képet lementheti későbbi feldolgozásra, vagy használhatja az offline " +
                    "mérést. Ekkor a kép nem kerül mentésre, és az eredmény pontatlanabb.")
            builder.setNeutralButton("Kilépés"){_,_ -> }
            builder.setPositiveButton("Mentés"){_,_ ->  it.positiveButtonClicked()}
            builder.setNegativeButton("Offline mérés"){_,_ -> startOfflineDetection(bitmap, rodLength, rodLengthPixels)}
            builder.setCancelable(true)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun startOfflineDetection(bitmap: Bitmap, rodLength: Double, rodLengthPixels: Int){
        activity?.showLoading()
        doAsync {
            var seg = Detector.segmentOffline(bitmap!!)
            uiThread {
                //TODO: Remove mask visualization and volume counting from Detector, it's done in MeasurementResult class
                //TODO: Add proper wood type
                //TODO: When GPS and innternet is turned off, Util.lat and Util.lon are null
                //TODO: Refactor MeasurementResult class
                var measurementResult = MeasurementResult(ImageUtils.bitmapToBase64(
                    Detector.Result.mask!!)!!,bitmap, rodLength, rodLengthPixels, sessionManager!!.woodLength, Util.getCurrentDateString(), "NoType", Util.lat!!, Util.long!!)
                activity?.successfulUpload(measurementResult, path, "offline")
                activity?.hideLoading()
            }
        }
    }

}