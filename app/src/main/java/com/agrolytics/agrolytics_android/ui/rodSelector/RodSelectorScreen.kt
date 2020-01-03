package com.agrolytics.agrolytics_android.ui.rodSelector

import com.agrolytics.agrolytics_android.base.BaseScreen
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload

interface RodSelectorScreen: BaseScreen {
	fun successfulUpload(imageUpload: ResponseImageUpload, path: String?)
	fun back()
}