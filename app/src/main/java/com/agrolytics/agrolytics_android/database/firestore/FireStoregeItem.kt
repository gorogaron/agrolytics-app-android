package com.agrolytics.agrolytics_android.database.firestore

import android.graphics.Bitmap

data class FireStoregeItem(
    val forestryId: String,
    val thumbnail: Bitmap,
    val maskThumbnail: Bitmap
)
