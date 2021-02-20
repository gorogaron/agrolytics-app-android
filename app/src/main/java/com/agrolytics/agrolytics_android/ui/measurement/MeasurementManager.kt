package com.agrolytics.agrolytics_android.ui.measurement

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.ImageUploadResponse
import com.agrolytics.agrolytics_android.ui.measurement.activity.CropperActivity
import com.agrolytics.agrolytics_android.ui.measurement.activity.RodSelectorActivity
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageObtainer
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.measurement.activity.ApproveMeasurementActivity
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Response
import java.time.LocalDateTime
import java.time.ZoneOffset

object MeasurementManager : KoinComponent{

    private val appServer: AppServer by inject()
    private val sessionManager : SessionManager by inject()
    var sessionImagePickerID : ImagePickerID = ImagePickerID.ID_CAMERA

    enum class ImagePickerID {
        ID_CAMERA, ID_BROWSER
    }

    fun hookImage(callingActivity: Activity, imagePickerID : ImagePickerID){
        val currentTimeStamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        sessionManager.measurementStartTimestamp = currentTimeStamp
        if (sessionManager.sessionId == "") sessionManager.sessionId = currentTimeStamp.toString()

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

    fun startApproveMeasurementActivity(callingActivity: Activity, processedImageItem: ProcessedImageItem, method: String) {
        ApproveMeasurementActivity.method = method
        ApproveMeasurementActivity.processedImageItem = processedImageItem
        val intent = Intent(callingActivity, ApproveMeasurementActivity::class.java)
        callingActivity.finish()
        callingActivity.startActivity(intent)
    }

    suspend fun startOnlineMeasurement(bitmap: Bitmap) : Response<ImageUploadResponse>{
        val request = createImageUploadRequest(bitmap)
        return appServer.uploadImage(request)
    }

    private fun createImageUploadRequest(bitmap: Bitmap?): ImageUploadRequest {
        val imageUploadRequest = ImageUploadRequest()
        bitmap?.let { imageUploadRequest.image = ImageUtils.bitmapToBase64(bitmap) }
        return imageUploadRequest
    }

}