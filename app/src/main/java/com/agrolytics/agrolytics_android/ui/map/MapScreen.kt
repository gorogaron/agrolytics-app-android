package com.agrolytics.agrolytics_android.ui.map

import com.agrolytics.agrolytics_android.ui.base.BaseScreen
import com.agrolytics.agrolytics_android.data.database.tables.CachedImageItem

interface MapScreen: BaseScreen {
	fun loadImages(images: ArrayList<CachedImageItem>, isOnline: Boolean)
	fun showDetails(imageItem: CachedImageItem)
}