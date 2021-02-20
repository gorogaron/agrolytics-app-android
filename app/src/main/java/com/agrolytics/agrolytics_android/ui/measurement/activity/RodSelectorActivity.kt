package com.agrolytics.agrolytics_android.ui.measurement.activity

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.ui.base.BaseActivity
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.ui.measurement.presenter.RodSelectorPresenter
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.measurement.MeasurementManager
import com.agrolytics.agrolytics_android.utils.SessionManager
import kotlinx.android.synthetic.main.activity_rod_selector.*
import org.jetbrains.anko.doAsync
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
		val view = LayoutInflater.from(this).inflate(R.layout.online_measurement_error_dialog, null, false)

		/**Set text for included layout elements (buttons)*/
		view.findViewById<ConstraintLayout>(R.id.button_1).apply {
			findViewById<TextView>(R.id.buttonText).text = "Kép mentése későbbi feldolgozásra"
			setOnClickListener { saveForLater(unprocessedImageItem) }
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

	fun saveForLater(unprocessedImageItem: UnprocessedImageItem){
		doAsync {
			dataClient.local.unprocessed.addUnprocessedImageItem(unprocessedImageItem)
		}
	}

	fun newImage(){
		val imagePickerID = MeasurementManager.ImagePickerID.ID_CAMERA
		finish()
		MeasurementManager.hookImage(this, imagePickerID)
	}

	fun showCurrentSession(){
		showToast("showCurrentSession - to be implemented")
	}

	fun measureOffline(){
		showToast("measureOffline - to be implemented")
	}


	companion object {
		var bitmap: Bitmap? = null
	}

	override fun positiveButtonClicked() {

	}

	override fun negativeButtonClicked() {

	}

}