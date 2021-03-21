package com.agrolytics.agrolytics_android.ui.images

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.agrolytics.agrolytics_android.AgrolyticsApp
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.ui.images.recyclerview.ImagesRecyclerViewAdapter
import com.agrolytics.agrolytics_android.ui.images.recyclerview.SessionItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import kotlinx.android.synthetic.main.activity_images.*
import kotlinx.android.synthetic.main.activity_session.*
import kotlinx.android.synthetic.main.activity_session.recycler_view
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension


class ImagesActivity: BaseActivity() {

	lateinit var recyclerViewAdapter : ImagesRecyclerViewAdapter
	lateinit var recyclerViewLayoutManager : GridLayoutManager

	@KoinApiExtension
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_images)

		btn_back.setOnClickListener{ onBackPressed() }

		val viewModel = ViewModelProvider(this).get(ImagesViewModel::class.java)
		viewModel.getSessionItems()
		AgrolyticsApp.firebaseUpdates.observe(this, Observer{
			viewModel.getSessionItems()
		})
		viewModel.sessionItems.observe(this, Observer {
			if (!::recyclerViewAdapter.isInitialized) {
				recyclerViewAdapter = ImagesRecyclerViewAdapter(this, ArrayList(it))
				recyclerViewLayoutManager = GridLayoutManager(this, 2)
				recycler_view.adapter = recyclerViewAdapter
				recycler_view.layoutManager = recyclerViewLayoutManager
			}
			else {
				recyclerViewAdapter.itemList = ArrayList(it)
			}
			recyclerViewAdapter.notifyDataSetChanged()
		})
	}
}