package com.agrolytics.agrolytics_android.ui.measurement

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import com.agrolytics.agrolytics_android.ui.measurement.activity.CropperActivity
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageObtainer


object MeasurementManager {

    lateinit var callingActivity : Activity

    enum class ImagePickerID {
        ID_CAMERA, ID_BROWSER
    }

    fun hookImage(activity: Activity, imagePickerID : ImagePickerID){
        callingActivity = activity
        ImageObtainer.setActivity(callingActivity)
        if (imagePickerID == ImagePickerID.ID_CAMERA){
            ImageObtainer.openCamera()
        }
        else if (imagePickerID == ImagePickerID.ID_BROWSER){
            ImageObtainer.openGallery()
        }
    }

    fun startCropperActivity(imgUri: Uri){
        val intent = Intent(callingActivity, CropperActivity::class.java)
        intent.putExtra("IMAGE", imgUri)
        callingActivity.startActivity(intent)
    }
}