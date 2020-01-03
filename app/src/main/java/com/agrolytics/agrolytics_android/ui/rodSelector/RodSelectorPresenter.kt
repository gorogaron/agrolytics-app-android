package com.agrolytics.agrolytics_android.ui.rodSelector

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Base64OutputStream
import com.agrolytics.agrolytics_android.base.BasePresenter
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.networking.model.ImageUploadRequest
import com.agrolytics.agrolytics_android.utils.BitmapUtils
import com.agrolytics.agrolytics_android.utils.Util
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.doAsync
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

class RodSelectorPresenter(val context: Context) : BasePresenter<RodSelectorScreen>() {

    private var activity: RodSelectorActivity? = null
    private var path: String? = null

    fun setActivity(activity: RodSelectorActivity) {
        this.activity = activity
    }

    fun uploadImage(path: String?, bitmap: Bitmap?, rodLength: Double, rodLengthPixels: Int) {
        this.path = path
        if (!Util.isNetworkAvailable(context)) {
            activity?.let {
                screen?.showAlertDialog(
                    "Nincs internet kapcsolat",
                    "Jelenleg nincs internetkapcsolat. Szeretnéd elmenteni a képet?",
                    it, true, "Mentés"
                )
            }
        } else {
            screen?.showLoading()
            val imageUploadRequest = createImageUploadRequest(bitmap, rodLength, rodLengthPixels)
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
                    screen?.showToast("Hiba történt. Kérlek próbáld meg mégegyszer és ellenőrizd az internet kapcsolatot.")
                    screen?.hideLoading()
                    error.printStackTrace()
                    if (error is SocketTimeoutException) {
                        activity?.let {
                            screen?.showAlertDialog(
                                "Nincs internet kapcsolat",
                                "Jelenleg nincs internetkapcsolat. Szeretnéd elmenteni a képet?",
                                it, true, "Mentés"
                            )
                        }
                    } else {
                        screen?.showToast("Hiba történt. Kérlek próbáld meg mégegyszer és ellenőrizd az internet kapcsolatot.")
                    }
                })
            upload?.let { subscriptions?.add(it) }
        }
    }

    fun saveLocalImageItem(rodLength: Double, rodLengthPixels: Int) {
        val imageItem = ImageItem(
            id = (0..10000).random().toString(),
            localPath = path ?: "",
            isPushedToServer = false,
            latitude = Util.lat ?: 0.0,
            longitude = Util.long ?: 0.0,
            length = sessionManager?.length?.toDouble() ?: 0.0,
            volume = 0.0,
            time = Util.getCurrentDateString(),
            selectionMode = "rod",
            rodLength = rodLength,
            rodLengthPixel = rodLengthPixels
        )
        doAsync {
            roomModule?.database?.imageItemDao()?.addImage(imageItem)
            screen?.back()
        }
    }

    private fun createImageUploadRequest(bitmap: Bitmap?, rodLength: Double, rodLengthPixels: Int): ImageUploadRequest {
        //val file = File(path)
        val imageUploadRequest = ImageUploadRequest()
        imageUploadRequest.processType = sessionManager?.mode
        imageUploadRequest.rodLength = rodLength
        imageUploadRequest.rodLengthPixel = rodLengthPixels
        bitmap?.let { imageUploadRequest.image = BitmapUtils.bitmapToBase64(bitmap) }
        return imageUploadRequest
    }

    private fun convertImageFileToBase64(file: File): String {
        return FileInputStream(file).use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                Base64OutputStream(outputStream, Base64.DEFAULT).use { base64FilterStream ->
                    inputStream.copyTo(base64FilterStream)
                    base64FilterStream.flush()
                    outputStream.toString()
                }
            }
        }
    }
}