package com.agrolytics.agrolytics_android.ui.rodSelector

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult
import com.agrolytics.agrolytics_android.ui.imageFinished.UploadFinishedActivity
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util
import kotlinx.android.synthetic.main.activity_rod_selector.*
import org.koin.android.ext.android.inject


class RodSelectorActivity : BaseActivity(), RodSelectorScreen, BaseActivity.OnDialogActions {

	private val presenter: RodSelectorPresenter by inject()
	private val appServer: AppServer by inject()
	private val roomModule: RoomModule by inject()
	private val sessionManager: SessionManager by inject()

	var path: String? = null
	var rodLength = 1.0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_rod_selector)

		presenter.addView(this)
		presenter.addInjections(arrayListOf(appServer, roomModule, sessionManager))
		presenter.setActivity(this)

		intent.getStringExtra(ConfigInfo.PATH)?.let {
			this.path = it
		}

		btn_next.setOnClickListener {
			val rodHeight = rod_selector_canvas.getRodLengthPixels_640_480()
			if (rodHeight != null) {
				val defaultBitmap = BitmapFactory.decodeFile(path)
				val resizedBitmap = Bitmap.createScaledBitmap(defaultBitmap, 640, 480, true)
				presenter.uploadImage(path, resizedBitmap, rodLength, rodHeight)
			}
		}
		btn_back.setOnClickListener{onBackPressed()}

		bitmap?.let {
			val originalRatioY = getOriginalRatioY()
			val originalRatioX = getOriginalRatioX()

			if (originalRatioX != null && originalRatioY != null) {
				rod_selector_canvas.setImage(bitmap!!)
			}
		}

		Util.showParameterSettingsWindow(this, sessionManager)
	}

	//TODO: Not a good function name...
	override fun successfulUpload(measurementResult: MeasurementResult, path: String?, method: String) {
			val intent = Intent(this, UploadFinishedActivity::class.java)
			val results = arrayListOf<MeasurementResult>()
			val pathList = arrayListOf<String>()
			results.add(measurementResult)
			path?.let { pathList.add(path) }
			UploadFinishedActivity.responseList = results
			intent.putStringArrayListExtra(ConfigInfo.PATH, pathList)
			intent.putExtra(ConfigInfo.METHOD, method)
			startActivity(intent)
			finish()
	}

	override fun negativeButtonClicked() { }

	override fun positiveButtonClicked() {
		val rodHeight = rod_selector_canvas.getRodLengthPixels_640_480()
		if (rodHeight != null) {
			presenter.saveLocalImageItem(rodLength, rodHeight)
		}
	}

	override fun back() {
		finish()
	}

	private fun getOriginalRatioY(): Double? {
		val croppedBitmapHeight = bitmap?.height?.toDouble() ?: 1.0
		val originalBitmapHeight =  originalBitmap?.height?.toDouble() ?: 1.0

		return croppedBitmapHeight / originalBitmapHeight
	}

	private fun getOriginalRatioX(): Double? {
		val croppedBitmapWidth = bitmap?.width?.toDouble() ?: 1.0
		val originalBitmapWidth =  originalBitmap?.width?.toDouble() ?: 1.0

		return croppedBitmapWidth / originalBitmapWidth
	}

	companion object {
		var bitmap: Bitmap? = null
		var originalBitmap: Bitmap? = null
	}

}