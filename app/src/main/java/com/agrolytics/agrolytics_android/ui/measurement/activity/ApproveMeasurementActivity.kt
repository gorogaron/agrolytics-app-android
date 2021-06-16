package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.agrolytics.agrolytics_android.utils.Util
import kotlinx.android.synthetic.main.activity_upload_finished.*
import kotlinx.android.synthetic.main.dialog_manual_adaption.*
import kotlinx.android.synthetic.main.layout_approve_after_confirmation.*
import kotlinx.android.synthetic.main.layout_approve_confirm.*
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

		/**Új kép, munkamenet áttekintése, mentés későbbre gombok*/
		btn_new.setOnClickListener { newImage() }
		btn_show_session.setOnClickListener { MeasurementManager.showSession(this, MeasurementManager.currentSessionId) }
		btn_save_for_later.setOnClickListener { saveImageForLater() }
		btn_modify.setOnClickListener { showManualAdaptionDialog() }

		image.setImageBitmap(processedImageItem.image)
	}

	private fun saveImageForLater() {
		doAsync {
			dataClient.local.unprocessed.add(unprocessedImageItem)
			MeasurementManager.recentlyAddedItemTimestamps.add(unprocessedImageItem.timestamp)
			uiThread {
				btn_save_for_later.visibility = View.GONE
				btn_save_for_later.isClickable = false
				toast(getString(R.string.image_saved))
			}
		}
	}

	private fun onDeclineClicked() {
		showAfterConfirmation()
		tv_result.text = getString(R.string.inaccurate_measurement)

		//Ezek az utólagos feldolgozás miatt kellenek
		doAsync { dataClient.local.unprocessed.delete(unprocessedImageItem) }
		MeasurementManager.recentlyAddedItemTimestamps.remove(unprocessedImageItem.timestamp)

		btn_modify.visibility = View.GONE
		when (method){
			"online" -> {
				btn_save_for_later.visibility = View.GONE
			}
			"offline" -> {
				btn_save_for_later.visibility = View.VISIBLE
			}
		}
	}

	private fun onAcceptClicked() {
		showAfterConfirmation()
		tv_result.text = Util.cubicMeter(this, processedImageItem.woodVolume)
		btn_save_for_later.visibility = View.GONE
		btn_modify.visibility = View.VISIBLE

		doAsync {
			dataClient.local.processed.add(processedImageItem)
			MeasurementManager.recentlyAddedItemTimestamps.add(processedImageItem.timestamp)

			//Ezek csak az utólagos feldolgozás miatt kellenek
			if (dataClient.local.unprocessed.get(unprocessedImageItem.timestamp).isNotEmpty()) {
				dataClient.local.unprocessed.delete(unprocessedImageItem)
				MeasurementManager.recentlyAddedItemTimestamps.remove(unprocessedImageItem.timestamp)
			}
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

	private fun showAfterConfirmation() {
		approve_confirm.animate().alpha(0f).duration = 300

		after_confirmation.alpha = 0f
		after_confirmation.visibility = View.VISIBLE
		after_confirmation.animate().alpha(1f).duration = 300
	}

	private fun showManualAdaptionDialog() {
		val view = LayoutInflater.from(this).inflate(R.layout.dialog_manual_adaption, null, false)
		val volumeToAddEditText = view.findViewById<EditText>(R.id.volume_to_add_edit_text)
		val justificationEditText = view.findViewById<EditText>(R.id.justification_edit_text)

		val dialog = AlertDialog.Builder(this)
			.setCancelable(true)
			.setView(view)
			.setPositiveButton(getString(R.string.ok)) { _, _ ->
				val volumeToAdd = volumeToAddEditText.text.toString().toDouble()
				val justification = justificationEditText.text.toString()
			}
			.create()
			.show()
	}

	companion object InputParameters {
	    lateinit var processedImageItem : ProcessedImageItem
		lateinit var unprocessedImageItem : UnprocessedImageItem
		lateinit var method : String
	}
}