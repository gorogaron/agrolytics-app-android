package com.agrolytics.agrolytics_android.ui.rodSelector

import com.agrolytics.agrolytics_android.base.BaseScreen
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult

interface RodSelectorScreen: BaseScreen {
	fun successfulUpload(measurementResult: MeasurementResult, path: String?, method: String)
	fun back()
}