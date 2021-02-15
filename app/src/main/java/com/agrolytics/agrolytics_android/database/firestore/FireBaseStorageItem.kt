package com.agrolytics.agrolytics_android.database.firestore

import android.graphics.Bitmap

data class FireBaseStorageItem(
    val forestryName: String,
    val maskedImageThumbnail: Bitmap,
    val maskedImage: Bitmap
)
