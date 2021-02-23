package com.agrolytics.agrolytics_android.ui.images

import android.os.Bundle
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.android.inject


class ImagesActivity: BaseActivity(), ImagesScreen {

	private val TAG = "ImagesActivity"

	private val presenter: ImagesPresenter by inject()
	private val sessionManager: SessionManager by inject()
	private val dataClient: DataClient by inject()
	private val appServer: AppServer by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_images)

		presenter.addView(this)
		presenter.addInjections(arrayListOf(sessionManager, appServer, dataClient))
		presenter.setActivity(this)

	}
}