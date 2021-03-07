package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.Activity
import android.content.Intent
import android.icu.util.Measure
import android.os.Bundle
import android.view.View
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.android.inject
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.types.ConfigInfo.IMAGE_BROWSE
import com.agrolytics.agrolytics_android.types.ConfigInfo.IMAGE_CAPTURE
import com.agrolytics.agrolytics_android.types.ConfigInfo.SESSION
import com.agrolytics.agrolytics_android.ui.main.MainActivity
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.presenter.ApproveMeasurementPresenter
import com.agrolytics.agrolytics_android.ui.measurement.utils.ImageObtainer
import kotlinx.android.synthetic.main.activity_upload_finished.*
import org.jetbrains.anko.doAsync
import org.koin.core.component.KoinApiExtension


class ApproveMeasurementActivity : BaseActivity() {

	private val sessionManager: SessionManager by inject()
	private val presenter: ApproveMeasurementPresenter by inject()
	private val dataClient: DataClient by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_upload_finished)

		presenter.addView(this)
		presenter.addInjections(arrayListOf(sessionManager, dataClient))

		btn_decline.setOnClickListener{ onDeclineClicked() }
		btn_accept.setOnClickListener{ onAcceptClicked() }

		btn_new.setOnClickListener { newImage() }
		btn_show_session.setOnClickListener { MeasurementManager.showSession(this, MeasurementManager.currentSessionId) }

		image.setImageBitmap(processedImageItem.image)
	}

	private fun onDeclineClicked() {
		when (method){
			"online" -> {
				//TODO: Új mérés vagy session áttekintése?
			}
			"offline" -> {
				//TODO: Szeretnéd később online megpróbálni?
			}
		}
	}

	private fun onAcceptClicked() {
		container_selection.animate().alpha(0f).duration = 300
		container_after_selection.visibility = View.VISIBLE
		container_selection.visibility = View.GONE
		tv_result.text = processedImageItem.woodVolume.toString()
		doAsync {
			dataClient.local.processed.add(processedImageItem)
			MeasurementManager.recentlyAddedItemsIds.add(processedImageItem.id)
		}
	}

	private fun newImage() {
		MeasurementManager.addNewMeasurementForSession(this, MeasurementManager.currentSessionId)
	}

	override fun onBackPressed() {
		MeasurementManager.closeMeasurementDialog(this)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
		super.onActivityResult(requestCode, resultCode, intent)
		if (resultCode == Activity.RESULT_OK ){
			when (requestCode) {
				IMAGE_CAPTURE, IMAGE_BROWSE, SESSION -> finish()
			}
		}
	}

	companion object Result {
	    lateinit var processedImageItem : ProcessedImageItem
		lateinit var method : String
	}
}
