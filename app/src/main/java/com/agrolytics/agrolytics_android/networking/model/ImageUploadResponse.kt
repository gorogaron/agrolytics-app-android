package com.agrolytics.agrolytics_android.networking.model

import android.graphics.Color

class ImageUploadResponse() {
    var mask: String? = null
    var numOfWoodPixels: Int = 0

    constructor(b64: String) : this() {
        mask = b64
        val mask_bmp = com.agrolytics.agrolytics_android.utils.ImageUtils.getImage(b64)
        for (i in 0 until mask_bmp.width){
            for (j in 0 until mask_bmp.height) {
                if (mask_bmp.getPixel(i,j) != Color.BLACK){
                    numOfWoodPixels = numOfWoodPixels + 1
                }
            }
        }
    }

}