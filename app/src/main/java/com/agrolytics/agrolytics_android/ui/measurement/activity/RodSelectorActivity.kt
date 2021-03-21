package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.network.AppServer
import com.agrolytics.agrolytics_android.ui.measurement.presenter.RodSelectorPresenter
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.utils.SessionManager
import kotlinx.android.synthetic.main.activity_rod_selector.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.toast
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject

class RodSelectorActivity : BaseActivity(){

	private val presenter: RodSelectorPresenter by inject()
	private val appServer: AppServer by inject()
	private val dataClient: DataClient by inject()
	private val sessionManager: SessionManager by inject()

	var unprocessedImageItem: UnprocessedImageItem? = null
	var unprocessedImageItemSaved = false
	var rodLength = 1.0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_rod_selector)

		presenter.addInjections(arrayListOf(appServer, dataClient, sessionManager))
		presenter.setActivity(this)

		intent.getStringExtra(ConfigInfo.CROPPED_RESIZED_IMG_PATH)?.let {
			croppedResizedImageBlackBg = BitmapFactory.decodeFile(it)
		}

		btn_next.setOnClickListener {
			val rodLengthPixel = rod_selector_canvas.getRodLengthPixels_640_480()
			presenter.uploadImage(rodLength, rodLengthPixel)
		}
		btn_back.setOnClickListener{onBackPressed()}
		rod_selector_canvas.setImage(croppedImageBlurredBg!!)
	}

	fun showOnlineMeasurementErrorDialog(){
		val builder = AlertDialog.Builder(this)
		builder.setTitle("Hiba")
		builder.setCancelable(false)
		val view = LayoutInflater.from(this).inflate(R.layout.dialog_online_measurement_error, null, false)
		builder.setView(view)
		val dialog = builder.create()

		/**Set text for included layout elements (buttons)*/
		setupSaveForLaterButton(view, dialog)
		setupNewImageButton(view, dialog)
		setupShowCurrentSessionButton(view, dialog)
		setupMeasureOfflineButton(view, dialog, unprocessedImageItem != null)


		dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_white_round)
		dialog.show()
	}

	private fun setupSaveForLaterButton(view : View, dialog: AlertDialog){
		view.findViewById<ConstraintLayout>(R.id.button_1).apply {
			findViewById<TextView>(R.id.buttonText).text = "Kép mentése későbbi feldolgozásra"
			if (unprocessedImageItem == null) {
				findViewById<TextView>(R.id.buttonText).setTextColor(ContextCompat.getColor(this@RodSelectorActivity, R.color.mediumGrey))
				this.isEnabled = false
			}
			else {
				setOnClickListener {
					findViewById<TextView>(R.id.buttonText).setTextColor(ContextCompat.getColor(this@RodSelectorActivity, R.color.mediumGrey))
					it.isEnabled = false
					setupMeasureOfflineButton(view, dialog, false)
					saveForLater()
				}
			}
		}
	}

	private fun setupNewImageButton(view : View, dialog: AlertDialog) {
		view.findViewById<ConstraintLayout>(R.id.button_2).apply {
			findViewById<TextView>(R.id.buttonText).text = "Új kép"
			setOnClickListener {
				MeasurementManager.addNewMeasurementForSession(this@RodSelectorActivity, MeasurementManager.currentSessionId)
				dialog.dismiss()
			}
		}
	}

	private fun setupShowCurrentSessionButton(view: View, dialog: AlertDialog) {
		view.findViewById<ConstraintLayout>(R.id.button_3).apply {
			findViewById<TextView>(R.id.buttonText).text = "Munkamenet áttekintése"
			setOnClickListener {
				MeasurementManager.showSession(this@RodSelectorActivity, MeasurementManager.currentSessionId)
				dialog.dismiss()
			}
		}
	}

	private fun setupMeasureOfflineButton(view: View, dialog: AlertDialog, clickable : Boolean) {
		view.findViewById<ConstraintLayout>(R.id.button_4).apply {
			findViewById<TextView>(R.id.buttonText).text = "Offline mérés"
			if (!clickable) {
				findViewById<TextView>(R.id.buttonText).setTextColor(ContextCompat.getColor(this@RodSelectorActivity, R.color.mediumGrey))
				this.isEnabled = false
			}
			else {
				setOnClickListener {
					measureOffline(unprocessedImageItem!!)
					dialog.dismiss()
				}
			}
		}
	}

	private fun saveForLater(){
		doAsync {
			dataClient.local.unprocessed.add(unprocessedImageItem!!)
			MeasurementManager.recentlyAddedItemTimestamps.add(unprocessedImageItem!!.timestamp)
			unprocessedImageItem = null
			unprocessedImageItemSaved = true
			uiThread { showToast("A kép mentésre került.") }
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
		super.onActivityResult(requestCode, resultCode, intent)
		if (resultCode == Activity.RESULT_OK ) {
			correspondingCropperActivity!!.finish()
			finish()
		}
		if (resultCode == Activity.RESULT_CANCELED && requestCode == ConfigInfo.SESSION) {
			//A sessionactivity-ben "vissza" gombot nyomtunk
			showOnlineMeasurementErrorDialog()
		}
	}

	private fun measureOffline(unprocessedImageItem: UnprocessedImageItem){
		showLoading()
		GlobalScope.launch {
			val processedImageItem = MeasurementManager.startOfflineMeasurement(unprocessedImageItem)
			MeasurementManager.startApproveMeasurementActivity(this@RodSelectorActivity, processedImageItem, unprocessedImageItem, "offline")
			finish()
			correspondingCropperActivity!!.finish()
			correspondingCropperActivity = null
			withContext(Dispatchers.Main) {
				hideLoading()
			}
		}
	}

	companion object {
		var correspondingCropperActivity : CropperActivity? = null
		var croppedResizedImageBlackBg: Bitmap? = null
		var croppedImageBlurredBg: Bitmap? = null
	}

}