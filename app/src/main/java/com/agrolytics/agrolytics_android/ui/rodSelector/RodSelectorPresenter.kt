package com.agrolytics.agrolytics_android.ui.rodSelector

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Base64OutputStream
import com.agrolytics.agrolytics_android.base.BasePresenter
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.ImageUploadResponse
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult
import com.agrolytics.agrolytics_android.utils.BitmapUtils
import com.agrolytics.agrolytics_android.utils.Detector
import com.agrolytics.agrolytics_android.utils.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.ByteArrayOutputStream
import java.io.File
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
        this.path = path
        if (!Util.isNetworkAvailable(context)) {
            handleNoInternet(bitmap!!, rodLength, rodLengthPixels)
        } else {
            screen?.showLoading()
            val imageUploadRequest = createImageUploadRequest(bitmap, rodLength, rodLengthPixels)
            val upload = appServer?.uploadImage(imageUploadRequest)
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe({ response ->
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val measurementResult = MeasurementResult(it.mask!!, bitmap!!, rodLength, rodLengthPixels)
                            screen?.successfulUpload(measurementResult, path, "online")
                        }
                    } else {
                        screen?.showToast(response.message()) //Internal server error (i.e. HTTP500)
                    }
                    screen?.hideLoading()
                }, { error ->
                    screen?.showToast("Hiba történt. Kérlek próbáld meg mégegyszer és ellenőrizd az internet kapcsolatot.")
                    screen?.hideLoading()
                    error.printStackTrace()
                    if (error is SocketTimeoutException) {
                        activity?.let {
                            screen?.showAlertDialog(
                                "Nincs internet kapcsolat",
                                "Jelenleg nincs internetkapcsolat. Szeretnéd elmenteni a képet?",
                                it, true, "Mentés"
                            )
                        }
                    } else {
                        screen?.showToast("Hiba történt. Kérlek próbáld meg mégegyszer és ellenőrizd az internet kapcsolatot.")
                    }
                })
            upload?.let { subscriptions?.add(it) }
        }
    }

    fun saveLocalImageItem(rodLength: Double, rodLengthPixels: Int) {
        val imageItem = ImageItem(
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
        doAsync {
            roomModule?.database?.imageItemDao()?.addImage(imageItem)
            screen?.back()
        }
    }

    private fun createImageUploadRequest(bitmap: Bitmap?, rodLength: Double, rodLengthPixels: Int): ImageUploadRequest {
        //val file = File(path)
        val imageUploadRequest = ImageUploadRequest()
        bitmap?.let { imageUploadRequest.image = BitmapUtils.bitmapToBase64(bitmap) }
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
        screen?.showLoading()
        doAsync {var seg = Detector.segmentOffline(bitmap!!)
            uiThread {
                //TODO: Remove mask visualization and volume counting from Detector, it's done in MeasurementResult class
                var measurementResult = MeasurementResult(BitmapUtils.bitmapToBase64(Detector.Result.mask!!)!!,bitmap, rodLength, rodLengthPixels)
                screen?.successfulUpload(measurementResult, path, "offline")
                screen?.hideLoading()
            }
        }
    }

}