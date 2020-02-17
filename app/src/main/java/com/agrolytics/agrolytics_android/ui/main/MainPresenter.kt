package com.agrolytics.agrolytics_android.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.util.Base64
import android.util.Base64OutputStream
import android.util.Log
import com.agrolytics.agrolytics_android.base.BasePresenter
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.networking.model.ResponseImageUpload
import com.agrolytics.agrolytics_android.utils.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.provider.MediaStore
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException


class MainPresenter(private val context: Context): BasePresenter<MainScreen>() {

	private var activity: MainActivity? = null
	private var path: String? = null

	fun setActivity(activity: MainActivity) {
		this.activity = activity
	}

	fun getImageUri(inImage: Bitmap): Uri {
		val bytes = ByteArrayOutputStream()
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
		val path = MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Title", null)
		return Uri.parse(path)
	}

	private fun createImageUploadRequest(path: String?, rodLength: Double?, rodLengthPixels: Int?): ImageUploadRequest {
		val imageUploadRequest = ImageUploadRequest()
		imageUploadRequest.processType = sessionManager?.mode
		imageUploadRequest.rodLength = rodLength
		imageUploadRequest.rodLengthPixel = rodLengthPixels
		imageUploadRequest.image = path
		return imageUploadRequest
	}

	fun uploadImage(path: String?, base64: String?, rodLength: Double?, rodLengthPixels: Int?) {
		this.path = path
		if (!Util.isNetworkAvailable(context)) {
			activity?.let {
				screen?.showAlertDialog("Nincs internet kapcsolat",
					"Jelenleg nincs internetkapcsolat. Szeretnéd elmenteni a képet?",
					it, true, "Mentés")
			}
		} else {
			screen?.showLoading()
			val imageUploadRequest = createImageUploadRequest(base64, rodLength, rodLengthPixels)
			val upload = appServer?.uploadImage(imageUploadRequest)
				?.subscribeOn(Schedulers.io())
				?.observeOn(AndroidSchedulers.mainThread())
				?.subscribe({ response ->
					if (response.isSuccessful) {
						response.body()?.let {
							screen?.successfulUpload(it, path)
						}
					} else {
						screen?.showToast(response.message())
					}
					screen?.hideLoading()
				}, { error ->
					screen?.hideLoading()
					error.printStackTrace()
					if (error is SocketTimeoutException) {
						activity?.let {
							screen?.showAlertDialog("Nincs internet kapcsolat",
								"Jelenleg nincs internetkapcsolat. Szeretnéd elmenteni a képet?",
								it, true, "Mentés")
						}
					} else {
						screen?.showToast("Hiba történt. Kérlek próbáld meg mégegyszer és ellenőrizd az internet kapcsolatot.")
					}
				})
			upload?.let { subscriptions?.add(it) }
		}
	}

	fun saveLocalImageItem() {
		val imageItem = ImageItem(
			id = (0..10000).random().toString(),
			localPath = path ?: "",
			isPushedToServer = false,
			latitude = Util.lat ?: 0.0,
			longitude = Util.long ?: 0.0,
			length = sessionManager?.length?.toDouble() ?: 0.0,
			volume = 0.0,
			time = Util.getCurrentDateString())
		doAsync {
			roomModule?.database?.imageItemDao()?.addImage(imageItem)
		}
	}
}