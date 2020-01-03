package com.agrolytics.agrolytics_android.ui.rodSelector

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import com.agrolytics.agrolytics_android.R
import com.agrolytics.agrolytics_android.base.BaseActivity
import com.agrolytics.agrolytics_android.database.tables.RoomModule
import com.agrolytics.agrolytics_android.networking.AppServer
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload
import com.agrolytics.agrolytics_android.ui.imageFinished.UploadFinishedActivity
import com.agrolytics.agrolytics_android.utils.ConfigInfo
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util
import com.agrolytics.agrolytics_android.utils.drawView.DrawView
import kotlinx.android.synthetic.main.activity_rod_selector.*
import org.koin.android.ext.android.inject


class RodSelectorActivity : BaseActivity(), RodSelectorScreen, BaseActivity.OnDialogActions {

	private val presenter: RodSelectorPresenter by inject()
	private val appServer: AppServer by inject()
	private val roomModule: RoomModule by inject()
	private val sessionManager: SessionManager by inject()

	var drawView: DrawView? = null
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

		drawView = DrawView(this)
		drawView?.layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
		)

		btn_next.setOnClickListener {
			val rodHeight = drawView?.pixelCount
			if (rodHeight != null) {
				val defaultBitmap = BitmapFactory.decodeFile(path)
				val resizedBitmap = Bitmap.createScaledBitmap(defaultBitmap, 640, 480, true)
				presenter.uploadImage(path, resizedBitmap, rodLength, rodHeight)
			}
		}

		bitmap?.let {
			val originalRatioY = getOriginalRatioY()
			val originalRatioX = getOriginalRatioX()

			if (originalRatioX != null && originalRatioY != null) {
				drawView?.setImage(it, originalRatioX, originalRatioY)
			}
		}


		main.addView(drawView)

		createRodDialog()
	}

	override fun successfulUpload(imageUpload: ResponseImageUpload, path: String?) {
		imageUpload.image?.let {
			if (it.isNotEmpty()) {
				val intent = Intent(this, UploadFinishedActivity::class.java)
				val responses = arrayListOf<ResponseImageUpload>()
				val pathList = arrayListOf<String>()
				responses.add(imageUpload)
				path?.let { pathList.add(path) }
				UploadFinishedActivity.responseList = responses
				intent.putStringArrayListExtra(ConfigInfo.PATH, pathList)
				startActivity(intent)
				finish()
			} else {
				showToast("A szerver nem tudta feldolgozni a kérést, kérlek készíts másik képek.")
			}
		} ?: run {
			showToast("A szerver nem tudta feldolgozni a kérést, kérlek készíts másik képek.")
		}
	}

	override fun negativeButtonClicked() { }

	override fun positiveButtonClicked() {
		val rodHeight = drawView?.pixelCount
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

	private fun getScaledImageRatio(): Double? {
		val croppedBitmapHeight = bitmap?.height?.toDouble() ?: 1.0
		bitmap?.let {
			val scaledBitmap =  Bitmap.createScaledBitmap(it, 640, 480, true)
			val scaledBitmapHeight =  scaledBitmap?.height?.toDouble() ?: 1.0

			return scaledBitmapHeight / croppedBitmapHeight
		} ?: run {
			return null
		}
	}

	private fun createRodDialog() {
		val builder = AlertDialog.Builder(this)
		builder.setTitle("Hossz")
		val view = LayoutInflater.from(this).inflate(R.layout.rod_dialog, null)
		val editText = view.findViewById<EditText>(R.id.et_length_rod)
		editText.setOnEditorActionListener { textView, i, keyEvent ->
			if (i == EditorInfo.IME_ACTION_DONE) {
				drawView?.setDefaultLine()
				rodLength = editText.text.toString().toDouble()
				Util.hideKeyboard(this, editText)
				true
			} else {
				false
			}
		}
		builder.setView(view)
		builder.setPositiveButton("Ok") { dialog, which ->
			drawView?.setDefaultLine()
			if (editText.text.isNotEmpty()) {
				rodLength = editText.text.toString().toDouble()
			}
			Util.hideKeyboard(this, editText)
		}
				.setCancelable(false)
				.show()
	}

	companion object {
		var bitmap: Bitmap? = null
		var originalBitmap: Bitmap? = null
	}

}