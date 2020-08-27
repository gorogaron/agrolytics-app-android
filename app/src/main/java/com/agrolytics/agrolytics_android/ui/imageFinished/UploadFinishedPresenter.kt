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
import com.google.firebase.storage.StorageMetadata
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

    private fun uploadImageToFireStore(
        url: String?,
        measurementResult: MeasurementResult?,
        path: String?,
        fragment: UploadFinishedFragment,
        imageRef: String?,
        thumbnailRef: String?,
        thumbnailUrl: String?
    ) {
        val imageDocument = hashMapOf(
            "time" to measurementResult?.date,
            "lat" to measurementResult?.lat,
            "long" to measurementResult?.lon,
            "role" to sessionManager?.userRole,
            "url" to url,
            //"volume" to (imageUploadResponse?.result?.toDouble() ?: 1.0) * (sessionManager?.length?.toDouble() ?: 1.0),
            "volume" to measurementResult?.getVolume(),
            "length" to measurementResult?.getWoodLength(),
            "imageRef" to imageRef,
            "userID" to sessionManager?.userID,
            "leaderID" to sessionManager?.leaderID,
            "forestryID" to sessionManager?.forestryID,
            "thumbnailRef" to thumbnailRef,
            "thumbnailUrl" to thumbnailUrl,
            "wood_type" to measurementResult?.woodType
        )

        fireStoreDB?.db?.collection("images")
            ?.add(imageDocument)
            ?.addOnSuccessListener { imageStored ->
                fireStoreDB?.db?.collection("images")?.document(imageStored.id)?.get()
                    ?.addOnSuccessListener {
                        screen?.hideLoading()
                        screen?.showToast("Upload finished")
                        screen?.updateView(fragment)
                        saveUploadedImageItem(measurementResult, path, fragment, it["url"] as String, imageStored.id)
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                    }
                    ?.addOnFailureListener { e ->
                        saveUploadedImageItem(measurementResult, path, fragment, null,null)
                        screen?.hideLoading()
                        screen?.showToast("Upload failed, we saved it locally. You try again in Images menu.")
                        Log.w(TAG, "Error writing document", e)
                    }
            }
            ?.addOnFailureListener { e ->
                saveUploadedImageItem(measurementResult, path, fragment, null, null)
                screen?.hideLoading()
                screen?.showToast("Upload failed, we saved it locally. You try again in Images menu.")
                Log.w(TAG, "Error writing document", e)
            }

    }

}
