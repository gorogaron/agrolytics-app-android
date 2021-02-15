package com.agrolytics.agrolytics_android.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

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

    fun createTempFileFromBitmap(inImage: Bitmap): Uri? {
        var tempDir = Environment.getExternalStorageDirectory()
        tempDir = File(tempDir.getAbsolutePath() + "/.temp/")
        tempDir.mkdir()
        val tempFile = File.createTempFile("cropped_img", ".jpg", tempDir)
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val bitmapData = bytes.toByteArray()

        //write the bytes in file
        val fos = FileOutputStream(tempFile)
        fos.write(bitmapData)
        fos.flush()
        fos.close()
        return Uri.fromFile(tempFile)
    }
}