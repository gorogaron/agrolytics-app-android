package com.agrolytics.agrolytics_android.ui.main

import android.app.AlertDialog
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