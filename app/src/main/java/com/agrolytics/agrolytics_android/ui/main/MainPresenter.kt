package com.agrolytics.agrolytics_android.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.agrolytics.agrolytics_android.base.BasePresenter
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.utils.Util
import java.io.*
import android.provider.MediaStore


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
	}
}