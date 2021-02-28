package com.agrolytics.agrolytics_android.ui.images

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.measurement.utils.SessionRecyclerViewAdapter
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension


class ImagesActivity: BaseActivity(), ImagesScreen {

	inner class SessionItem(var woodLength : Int, var woodType : String, var woodVolume : Double, var timestamp : Long)

	private val dataClient : DataClient by inject()
	private val sessionManager : SessionManager by inject()
	lateinit var recyclerViewAdapter : ImagesRecyclerViewAdapter
	lateinit var recyclerViewLayoutManager : GridLayoutManager

	@KoinApiExtension
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_images)



		/*val viewModel = ViewModelProvider(this).get(ImagesViewModel::class.java)
		viewModel.getUnprocessedImageItems()
		viewModel.unprocessedImageItems.observe(this, Observer {
			localImageItems = it
			Log.d("FIKA", "GECI")
		})*/
	}
}