package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import org.koin.android.ext.android.inject
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo.IMAGE_BROWSE
import com.agrolytics.agrolytics_android.types.ConfigInfo.IMAGE_CAPTURE
import com.agrolytics.agrolytics_android.types.ConfigInfo.SESSION
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import kotlinx.android.synthetic.main.activity_upload_finished.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread


class ApproveMeasurementActivity : BaseActivity() {

	private val dataClient: DataClient by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_upload_finished)

		btn_decline.setOnClickListener{ onDeclineClicked() }
		btn_accept.setOnClickListener{ onAcceptClicked() }

		/**Új kép, munkamenet áttekintése*/
		btn_new.setOnClickListener { newImage() }
		btn_show_session.setOnClickListener { MeasurementManager.showSession(this, MeasurementManager.currentSessionId) }

		/**Új kép, munkamenet áttekintése, mentés későbbre*/
		btn_new_offline.setOnClickListener { newImage() }
		btn_show_session_offline.setOnClickListener { MeasurementManager.showSession(this, MeasurementManager.currentSessionId) }
		btn_save_for_later.setOnClickListener { doAsync {
			dataClient.local.unprocessed.add(unprocessedImageItem)
			MeasurementManager.recentlyAddedItemTimestamps.add(unprocessedImageItem.timestamp)
			uiThread {
				btn_save_for_later.visibility = View.GONE
				btn_save_for_later.isClickable = false
				toast("A kép mentésre került.")
			}
		} }

		image.setImageBitmap(processedImageItem.image)
	}

	private fun onDeclineClicked() {
		when (method){
			"online" -> {
				dataClient.local.unprocessed.delete(unprocessedImageItem) //Ez csak az utólagos feldolgozás miatt kell

				//Új mérés vagy session áttekintése?
				container_selection.animate().alpha(0f).duration = 300
				container_after_selection.visibility = View.VISIBLE
				container_selection.visibility = View.GONE
			}
			"offline" -> {
				//Szeretnéd később online megpróbálni?
				container_selection.animate().alpha(0f).duration = 300
				container_after_selection_offline_declined.visibility = View.VISIBLE
				container_selection.visibility = View.GONE
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
			dataClient.local.unprocessed.delete(unprocessedImageItem) //Ez csak az utólagos feldolgozás miatt kell
			MeasurementManager.recentlyAddedItemTimestamps.add(processedImageItem.timestamp)
		}
	}

	private fun newImage() {
		MeasurementManager.addNewMeasurementForSession(this, MeasurementManager.currentSessionId)
	}

	override fun onBackPressed() {
		MeasurementManager.showCloseMeasurementConfirmationDialog(this)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
		super.onActivityResult(requestCode, resultCode, intent)
		if (resultCode == Activity.RESULT_OK ){
			when (requestCode) {
				IMAGE_CAPTURE, IMAGE_BROWSE, SESSION -> finish()
			}
		}
	}

	companion object InputParameters {
	    lateinit var processedImageItem : ProcessedImageItem
		lateinit var unprocessedImageItem : UnprocessedImageItem
		lateinit var method : String
	}
}
