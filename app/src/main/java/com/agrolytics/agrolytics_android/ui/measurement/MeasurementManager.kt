package com.agrolytics.agrolytics_android.ui.measurement

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult
import com.agrolytics.agrolytics_android.ui.measurement.activity.CropperActivity
import com.agrolytics.agrolytics_android.ui.measurement.activity.RodSelectorActivity
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageObtainer
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.agrolytics.agrolytics_android.utils.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.SocketTimeoutException



object MeasurementManager : KoinComponent{

    private val appServer: AppServer by inject()

    enum class ImagePickerID {
        ID_CAMERA, ID_BROWSER
    }

    fun hookImage(callingActivity: Activity, imagePickerID : ImagePickerID){
        ImageObtainer.setActivity(callingActivity)
        if (imagePickerID == ImagePickerID.ID_CAMERA){
            ImageObtainer.openCamera()
        }
        else if (imagePickerID == ImagePickerID.ID_BROWSER){
            ImageObtainer.openGallery()
        }
    }

    fun startCropperActivity(callingActivity: Activity, imgUri: Uri){
        val intent = Intent(callingActivity, CropperActivity::class.java)
        intent.putExtra("IMAGE", imgUri)
        callingActivity.startActivity(intent)
    }

    fun startRodSelectorActivity(callingActivity: Activity, croppedImgBitmap: Bitmap) {
        val cropImgUri = ImageUtils.createTempFileFromBitmap(Bitmap.createScaledBitmap(croppedImgBitmap, 640, 480, true))
        //TODO: Handle if temporary file could not have been created
        val intent = Intent(callingActivity, RodSelectorActivity::class.java)
        intent.putExtra(ConfigInfo.CROPPED_RESIZED_IMG_PATH, cropImgUri!!.path)
        RodSelectorActivity.bitmap = croppedImgBitmap
        callingActivity.startActivity(intent)
    }

    /*
    fun startOnlineMeasurement(iRequest : ImageUploadRequest) {
        val upload = appServer.uploadImage(iRequest)
            .subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({ response ->
                if (response.isSuccessful) {
                    response.body()?.let {
                        //TODO: Handle if GPS in not available
                        if (Util.lat == null || Util.long == null){
                            Util.lat = 0.0
                            Util.long = 0.0
                        }
                        val measurementResult = MeasurementResult(it.mask!!, bitmap!!, rodLength, rodLengthPixels, sessionManager!!.woodLength, Util.getCurrentDateString(), sessionManager!!.woodType, Util.lat!!, Util.long!!)
                        screen?.successfulUpload(measurementResult, path, "online")
                    }
                } else {
                    screen?.showToast(response.message()) //Internal server error (i.e. HTTP500)
                }
                screen?.hideLoading()
            }, { error ->
                Log.d("Sending", "Could not send image: ${error}")
                screen?.hideLoading()
                error.printStackTrace()
                if (error is SocketTimeoutException) {
                    activity?.let {
                        screen?.showAlertDialog(
                            "Nincs internet kapcsolat",
                            "A kapcsolat időtúllépés miatt megszakadt. Szeretnéd elmenteni a képet?",
                            it, true, "Mentés"
                        )
                    }
                } else {
                    screen?.showToast("Hiba történt. Kérlek próbáld meg mégegyszer és ellenőrizd az internet kapcsolatot.")
                }
            })
        upload?.let { subscriptions?.add(it) }
    }

    fun handleOnlineMeasurementError(){

    }*/
}