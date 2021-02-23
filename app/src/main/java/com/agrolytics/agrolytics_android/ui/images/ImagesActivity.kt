package com.agrolytics.agrolytics_android.ui.images

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.android.inject


class ImagesActivity: BaseActivity(), ImagesScreen {

	private val TAG = "ImagesActivity"

	private val sessionManager: SessionManager by inject()
	private val dataClient: DataClient by inject()
	private val appServer: AppServer by inject()

	private lateinit var cachedImageItems: List<CachedImageItem>
	private lateinit var localImageItems: List<BaseImageItem>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_images)

//		val viewModel = ViewModelProviders.of(this).get(ImagesViewModel::class.java)
	}
}