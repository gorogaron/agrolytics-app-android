package com.agrolytics.agrolytics_android.ui.main

import android.content.Context
import com.agrolytics.agrolytics_android.ui.base.BasePresenter


class MainPresenter(private val context: Context): BasePresenter<MainScreen>() {

	private var activity: MainActivity? = null

	fun setActivity(activity: MainActivity) {
		this.activity = activity
	}

}