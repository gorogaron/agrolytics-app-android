package com.agrolytics.agrolytics_android.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    // convert from bitmap to byte array
    fun getBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        //return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        return stream.toByteArray()
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

    /**
     * Rárajzolja a maszkot a képre, és megszámolja a fát tartalmazó pixeleket
     * @param inputImg Bemeneti fotó, maszk nélkül
     * @param mask Képhez készült maszk
     * @return Maszkolt kép és fát tartalmazó pixelekszáma
     */
    fun drawMaskOnInputImage(inputImg : Bitmap, mask : Bitmap) : Pair<Bitmap, Int>{
        var numOfWoodPixels = 0
        val visualizedMask = inputImg.copy(Bitmap.Config.ARGB_8888 , true)
        val intValues = IntArray(inputImg.width * inputImg.height)
        inputImg.getPixels(intValues, 0, inputImg.width, 0, 0, inputImg.width, inputImg.height)
        for (i in 0 until inputImg.width){
            for (j in 0 until inputImg.height) {
                val pixelValue = intValues[j * inputImg.width + i]
                val R = pixelValue and 0xff0000 shr 16
                val G = pixelValue and 0x00ff00 shr 8
                val B = pixelValue and 0x0000ff shr 0
                if (Color.red(mask.getPixel(i, j)) > 0){
                    numOfWoodPixels = numOfWoodPixels + 1
                    val alpha = 0.5f
                    val newR = alpha * 255 + (1-alpha) * R
                    val newG = alpha * 0 + (1-alpha) * G
                    val newB = alpha * 0 + (1-alpha) * B
                    visualizedMask.setPixel(i, j, Color.rgb(newR.toInt(), newG.toInt(), newB.toInt()))
                }
            }
        }
        return Pair(visualizedMask, numOfWoodPixels)
    }
}