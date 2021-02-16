package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.database.DataClient
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult
import com.agrolytics.agrolytics_android.ui.measurement.presenter.RodSelectorPresenter
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util
import kotlinx.android.synthetic.main.activity_rod_selector.*
import org.koin.android.ext.android.inject

class RodSelectorActivity : BaseActivity(), BaseActivity.OnDialogActions {

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
			val rodHeight = rod_selector_canvas.getRodLengthPixels_640_480()
			val defaultBitmap = BitmapFactory.decodeFile(path)
			val resizedBitmap = Bitmap.createScaledBitmap(defaultBitmap, 640, 480, true)
			presenter.uploadImage(path, resizedBitmap, rodLength, rodHeight)
		}
		btn_back.setOnClickListener{onBackPressed()}
		rod_selector_canvas.setImage(bitmap!!)

		//Util.showParameterSettingsWindow(this, sessionManager)
		showOnlineMeasurementErrorDialog()
	}

	//TODO: Not a good function name...
	fun successfulUpload(measurementResult: MeasurementResult, path: String?, method: String) {
			val intent = Intent(this, ApproveMeasurementActivity::class.java)
			val results = arrayListOf<MeasurementResult>()
			val pathList = arrayListOf<String>()
			results.add(measurementResult)
			path?.let { pathList.add(path) }
			ApproveMeasurementActivity.responseList = results
			intent.putStringArrayListExtra(ConfigInfo.CROPPED_RESIZED_IMG_PATH, pathList)
			intent.putExtra(ConfigInfo.METHOD, method)
			startActivity(intent)
			finish()
	}

	fun showOnlineMeasurementErrorDialog(){
		val builder = AlertDialog.Builder(this)
		builder.setTitle("Hiba")
		val view = LayoutInflater.from(this).inflate(R.layout.online_measurement_error_dialog, null, false)

		/**Set text for included layout elements (buttons)*/
		view.findViewById<ConstraintLayout>(R.id.button_1).apply {
			findViewById<TextView>(R.id.buttonText).text = "Kép mentése későbbi feldolgozásra"
			setOnClickListener { saveForLater() }
		}
		view.findViewById<ConstraintLayout>(R.id.button_2).apply {
			findViewById<TextView>(R.id.buttonText).text = "Új kép"
			setOnClickListener { newImage() }
		}
		view.findViewById<ConstraintLayout>(R.id.button_3).apply {
			findViewById<TextView>(R.id.buttonText).text = "Munkamenet áttekintése"
			setOnClickListener { showCurrentSession() }
		}
		view.findViewById<ConstraintLayout>(R.id.button_4).apply {
			findViewById<TextView>(R.id.buttonText).text = "Offline mérés"
			setOnClickListener { measureOffline() }
		}

		builder.setView(view)
		builder.setCancelable(false)

		val dialog = builder.create()
		dialog.window!!.setBackgroundDrawableResource(R.drawable.bg_dialog)
		dialog.show()
	}

	fun saveForLater(){
		showToast("saveForLater - to be implemented")
	}

	fun newImage(){
		showToast("newImage - to be implemented")
	}

	fun showCurrentSession(){
		showToast("showCurrentSession - to be implemented")
	}

	fun measureOffline(){
		showToast("measureOffline - to be implemented")
	}

	override fun negativeButtonClicked() { }

	override fun positiveButtonClicked() {
		val rodHeight = rod_selector_canvas.getRodLengthPixels_640_480()
		presenter.saveLocalImageItem(rodLength, rodHeight)
	}

	fun back() {
		finish()
	}

	companion object {
		var bitmap: Bitmap? = null
	}

}