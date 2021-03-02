package com.agrolytics.agrolytics_android.ui.images

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.images.recyclerview.ImagesRecyclerViewAdapter
import com.agrolytics.agrolytics_android.ui.images.recyclerview.SessionItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import kotlinx.android.synthetic.main.activity_session.*
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension


class ImagesActivity: BaseActivity(), ImagesScreen {

	private val dataClient : DataClient by inject()
	private val sessionManager : SessionManager by inject()
	lateinit var recyclerViewAdapter : ImagesRecyclerViewAdapter
	lateinit var recyclerViewLayoutManager : GridLayoutManager

	@KoinApiExtension
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_images)

		val viewModel = ViewModelProvider(this).get(ImagesViewModel::class.java)
		viewModel.getSessionItems()
		viewModel.sessionItems.observe(this, Observer {
			recyclerViewAdapter = ImagesRecyclerViewAdapter(this, ArrayList(it))
			recyclerViewLayoutManager = GridLayoutManager(this, 2)

			recycler_view.adapter = recyclerViewAdapter
			recycler_view.layoutManager = recyclerViewLayoutManager
		})
	}
}