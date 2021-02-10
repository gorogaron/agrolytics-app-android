package com.agrolytics.agrolytics_android.ui.measurement

import android.content.Context
import android.graphics.Bitmap

object MeasurementManager {
    enum class ImagePickerID {
        ID_CAMERA, ID_BROWSER
    }

    fun startMeasurement(context : Context, imagePickerID : ImagePickerID){
        lateinit val imageToProcess : Bitmap
        if (imagePickerID == ImagePickerID.ID_CAMERA){
            startCamera(context)
        }
        else if (imagePickerID == ImagePickerID.ID_BROWSER){
            startBrowser(context)
        }
    }
}