package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.os.Bundle
import android.view.View
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.android.ext.android.inject
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.ui.measurement.presenter.ApproveMeasurementPresenter
import kotlinx.android.synthetic.main.activity_upload_finished.*
import org.jetbrains.anko.doAsync


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
		tv_result.text = processedImageItem.woodVolume.toString()
		doAsync {
			dataClient.local.processed.addProcessedImageItem(processedImageItem)
		}
	}

	private fun newImage() {
		finish()
		MeasurementManager.hookImage(this, MeasurementManager.sessionImagePickerID)
	}

	companion object Result {
	    lateinit var processedImageItem : ProcessedImageItem
		lateinit var method : String
	}
}
