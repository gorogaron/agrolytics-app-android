package com.agrolytics.agrolytics_android.ui.images

import com.agrolytics.agrolytics_android.ui.base.BaseScreen
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem

interface ImagesScreen: BaseScreen {
	fun loadImages(images: ArrayList<CachedImageItem>)
	fun deleted()
	fun uploaded()
	}