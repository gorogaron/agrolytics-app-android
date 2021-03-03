package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
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
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.koin.android.ext.android.inject

class RodSelectorActivity : BaseActivity(){

	private val presenter: RodSelectorPresenter by inject()
	private val appServer: AppServer by inject()
	private val dataClient: DataClient by inject()
	private val sessionManager: SessionManager by inject()


	var path: String? = null
	var rodLength = 1.0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_rod_selector)

		presenter.addInjections(arrayListOf(appServer, dataClient, sessionManager))
		presenter.setActivity(this)

		intent.getStringExtra(ConfigInfo.CROPPED_RESIZED_IMG_PATH)?.let {
			this.path = it
		}

		btn_next.setOnClickListener {
			val rodLengthPixel = rod_selector_canvas.getRodLengthPixels_640_480()
			val defaultBitmap = BitmapFactory.decodeFile(path)
			val resizedBitmap = Bitmap.createScaledBitmap(defaultBitmap, 640, 480, true)
			presenter.uploadImage(resizedBitmap, rodLength, rodLengthPixel)
		}
		btn_back.setOnClickListener{onBackPressed()}
		rod_selector_canvas.setImage(bitmap!!)
	}

	fun showOnlineMeasurementErrorDialog(unprocessedImageItem: UnprocessedImageItem){
		val builder = AlertDialog.Builder(this)
		builder.setTitle("Hiba")
		//builder.setCancelable(true)
		val view = LayoutInflater.from(this).inflate(R.layout.online_measurement_error_dialog, null, false)
		builder.setView(view)
		val dialog = builder.create()

		/**Set text for included layout elements (buttons)*/
		view.findViewById<ConstraintLayout>(R.id.button_1).apply {
			findViewById<TextView>(R.id.buttonText).text = "Kép mentése későbbi feldolgozásra"
			setOnClickListener {
				findViewById<TextView>(R.id.buttonText).setTextColor(ContextCompat.getColor(this@RodSelectorActivity, R.color.mediumGrey))
				saveForLater(unprocessedImageItem)
				it.isEnabled = false
			}
		}
		view.findViewById<ConstraintLayout>(R.id.button_2).apply {
			findViewById<TextView>(R.id.buttonText).text = "Új kép"
			setOnClickListener {
				newImage()
				dialog.dismiss()
			}
		}
		view.findViewById<ConstraintLayout>(R.id.button_3).apply {
			findViewById<TextView>(R.id.buttonText).text = "Munkamenet áttekintése"
			setOnClickListener {
				showCurrentSession()
			}
		}
		view.findViewById<ConstraintLayout>(R.id.button_4).apply {
			findViewById<TextView>(R.id.buttonText).text = "Offline mérés"
			setOnClickListener {
				measureOffline(unprocessedImageItem)
				dialog.dismiss()
			}
		}

		dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_white_round)
		dialog.show()
	}

	private fun saveForLater(unprocessedImageItem: UnprocessedImageItem){
		doAsync {
			dataClient.local.unprocessed.add(unprocessedImageItem)
			uiThread { showToast("A kép mentésre került.") }
		}
	}

	private fun newImage(){
		MeasurementManager.hookImage(this, MeasurementManager.sessionImagePickerID)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
		super.onActivityResult(requestCode, resultCode, intent)
		finish()
	}

	private fun showCurrentSession(){
		MeasurementManager.showSession(this, sessionManager.sessionId)
	}

	private fun measureOffline(unprocessedImageItem: UnprocessedImageItem){
		MeasurementManager.startOfflineMeasurement(unprocessedImageItem.image)
		showToast("measureOffline - to be implemented")
	}

	companion object {
		var bitmap: Bitmap? = null
	}

}