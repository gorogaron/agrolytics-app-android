package com.agrolytics.agrolytics_android.networking.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

data class ImageUploadResponse(var mask : String){

    fun toBitmap() : Bitmap{
        val arr = Base64.decode(mask, Base64.NO_WRAP)
        return BitmapFactory.decodeByteArray(arr, 0, arr.size)
    }

}