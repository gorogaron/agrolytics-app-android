package com.agrolytics.agrolytics_android.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import com.agrolytics.agrolytics_android.base.BasePresenter
import java.io.ByteArrayOutputStream

class MainPresenter(private val context: Context): BasePresenter<MainScreen>() {

	private var activity: MainActivity? = null

	fun setActivity(activity: MainActivity) {
		this.activity = activity
	}

	fun getImageUri(inImage: Bitmap): Uri {
		val bytes = ByteArrayOutputStream()
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
		val path = MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Title", null)
		return Uri.parse(path)
	}

}