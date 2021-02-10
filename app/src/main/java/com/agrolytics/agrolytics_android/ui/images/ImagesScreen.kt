package com.agrolytics.agrolytics_android.ui.images

import com.agrolytics.agrolytics_android.ui.base.BaseScreen
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult

interface ImagesScreen: BaseScreen {
	fun loadImages(images: ArrayList<ImageItem>)
	fun deleted()
	fun uploaded()
	fun sendResultsToFragment(resultList: ArrayList<Pair<MeasurementResult, Pair<String, String>>>?)
}