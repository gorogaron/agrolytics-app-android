package com.agrolytics.agrolytics_android.ui.main

import com.agrolytics.agrolytics_android.base.BaseScreen
import com.agrolytics.agrolytics_android.networking.model.ImageUploadResponse

interface MainScreen: BaseScreen {
	fun locationUpdated()
}