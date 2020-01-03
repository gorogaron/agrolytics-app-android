package com.agrolytics.agrolytics_android.ui.map

import com.agrolytics.agrolytics_android.base.BaseScreen
import com.agrolytics.agrolytics_android.networking.model.ImageItem

interface MapScreen: BaseScreen {
	fun loadImages(images: ArrayList<ImageItem>, isOnline: Boolean)
	fun showDetails(imageItem: ImageItem)
}