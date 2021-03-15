package com.agrolytics.agrolytics_android.data.local.tables

import android.graphics.Bitmap
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.google.firebase.firestore.GeoPoint

interface BaseImageItem {
    var timestamp: Long
    var sessionId: Long
    var image: Bitmap?      //CachedImageItem esetén ez lehet null
    var woodType: String
    var woodLength: Double
    var location : GeoPoint

    fun getItemType() : ConfigInfo.IMAGE_ITEM_TYPE
}