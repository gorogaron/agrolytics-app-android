package com.agrolytics.agrolytics_android.ui.main

import com.agrolytics.agrolytics_android.base.BaseScreen
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload

interface MainScreen: BaseScreen {
	fun successfulUpload(imageUpload: ResponseImageUpload, path: String?)
	fun locationUpdated()
}