package com.agrolytics.agrolytics_android

import android.app.Application
import com.agrolytics.agrolytics_android.koin.appModule
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.mapbox.mapboxsdk.Mapbox
import net.danlew.android.joda.JodaTimeAndroid
import org.koin.android.ext.android.startKoin

class AgrolyticsApp: Application() {

	override fun onCreate() {
		super.onCreate()
		startKoin(this, listOf(appModule))
		JodaTimeAndroid.init(this)
		Mapbox.getInstance(
				this,
				ConfigInfo.MAP_BOX_KEY
		)
	}
}