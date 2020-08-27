package com.agrolytics.agrolytics_android.ui.imageFinished

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.agrolytics.agrolytics_android.base.BasePresenter
import com.agrolytics.agrolytics_android.networking.model.ImageItem
import com.agrolytics.agrolytics_android.networking.model.ImageUploadResponse
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult
import com.agrolytics.agrolytics_android.ui.imageFinished.fragment.UploadFinishedFragment
import com.agrolytics.agrolytics_android.utils.BitmapUtils
import com.agrolytics.agrolytics_android.utils.Util
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult
import org.jetbrains.anko.uiThread
import org.koin.dsl.module.applicationContext
import java.io.File
import java.io.FileInputStream
import java.util.*

class UploadFinishedPresenter : BasePresenter<UploadFinishedScreen>() {

    val TAG = "UploadFinishedPresenter"

    private fun saveUploadedImageItem(
        measurementResult: MeasurementResult?,
        path: String?,
        fragment: UploadFinishedFragment,
        serverImage: String?,
        id: String?
    ) {
        val imageItem = ImageItem(
            id = id ?: (0..10000).random().toString(),
            localPath = path ?: "",
            isPushedToServer = true,
            latitude = Util.lat ?: 0.0,
            longitude = Util.long ?: 0.0,
            length = sessionManager?.length?.toDouble() ?: 0.0,
            volume = measurementResult?.getVolume(),
            time = Util.getCurrentDateString(),
            serverImage = serverImage
        )
        doAsync {
            //roomModule?.database?.imageItemDao()?.addImage(imageItem)
            uiThread { screen?.updateView(fragment) }
        }
    }

    private fun handleAsyncError(
        error: Exception,
        measurementResult: MeasurementResult?,
        path: String?,
        fragment: UploadFinishedFragment
    ) {
        saveUploadedImageItem(measurementResult, path, fragment, null, null)
        screen?.hideLoading()
        screen?.showToast("Upload failed, we saved it locally. You can try again in Images menu.")
        error.printStackTrace()
    }



}
