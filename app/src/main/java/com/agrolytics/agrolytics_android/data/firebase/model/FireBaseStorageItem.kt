package com.agrolytics.agrolytics_android.data.firebase.model

import android.graphics.Bitmap

data class FireBaseStorageItem(
    val forestryName: String,
    val maskedImageThumbnail: Bitmap,
    val maskedImage: Bitmap,
    val imageName: String
)