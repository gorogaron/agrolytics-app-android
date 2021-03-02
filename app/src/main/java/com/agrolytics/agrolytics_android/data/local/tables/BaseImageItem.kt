package com.agrolytics.agrolytics_android.data.local.tables

import com.agrolytics.agrolytics_android.types.ConfigInfo

interface BaseImageItem {
    fun getItemType() : ConfigInfo.IMAGE_ITEM_TYPE
}