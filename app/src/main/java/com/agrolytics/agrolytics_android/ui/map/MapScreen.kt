package com.agrolytics.agrolytics_android.ui.map

import com.agrolytics.agrolytics_android.ui.base.BaseScreen
import com.agrolytics.agrolytics_android.database.local.ImageItem

interface MapScreen: BaseScreen {
	fun loadImages(images: ArrayList<ImageItem>, isOnline: Boolean)
	fun showDetails(imageItem: ImageItem)
}