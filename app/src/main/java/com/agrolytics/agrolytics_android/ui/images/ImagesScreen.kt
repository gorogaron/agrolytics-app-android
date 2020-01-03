package com.agrolytics.agrolytics_android.ui.images

import com.agrolytics.agrolytics_android.base.BaseScreen
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload

interface ImagesScreen: BaseScreen {
	fun loadImages(images: ArrayList<ImageItem>)
	fun deleted()
	fun uploaded()
	fun sendResponseToFragment(responseList: ArrayList<Pair<ResponseImageUpload, Pair<String, String>>>?)
}