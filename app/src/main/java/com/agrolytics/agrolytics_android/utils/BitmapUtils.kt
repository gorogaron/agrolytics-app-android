package com.agrolytics.agrolytics_android.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object BitmapUtils {

    // convert from bitmap to byte array
    fun getBytes(bitmap: Bitmap): ByteArray? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 1, stream)
        //return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        return stream.toByteArray()
    }

    // convert from byte array to bitmap
    fun getImage(image: String?): Bitmap {
        val arr = Base64.decode(image, Base64.NO_WRAP)
        return BitmapFactory.decodeByteArray(arr, 0, arr.size)
    }

    fun bitmapToBase64(bitmap: Bitmap): String? {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 1, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}