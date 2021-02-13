package com.agrolytics.agrolytics_android.ui.measurement.presenter

import android.graphics.Bitmap
import android.util.Log
import com.agrolytics.agrolytics_android.database.firestore.*
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.database.local.ImageItem
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult
import com.agrolytics.agrolytics_android.ui.imageFinished.fragment.UploadFinishedFragment
import com.agrolytics.agrolytics_android.ui.measurement.activity.ApproveMeasurementActivity
import com.agrolytics.agrolytics_android.utils.ImageUtils
import com.agrolytics.agrolytics_android.utils.Util
import com.google.firebase.storage.StorageMetadata
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.sql.Timestamp
import java.time.Instant.now
import java.time.LocalDate
import java.time.LocalDateTime.now

class ApproveMeasurementPresenter : BasePresenter<ApproveMeasurementActivity>() {

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
            session_id = "",
            localPath = path ?: "",
            isPushedToServer = true,
            latitude = Util.lat ?: 0.0,
            longitude = Util.long ?: 0.0,
            length = sessionManager?.woodLength?.toDouble() ?: 0.0,
            volume = measurementResult?.getVolume(),
            time = Util.getCurrentDateString(),
            serverImage = serverImage
        )
        doAsync {
            roomModule?.database?.imageItemDao()?.addImage(imageItem)
            uiThread { screen?.updateView(fragment) }
        }
    }

    suspend fun uploadImageToStorage(
        measurementResult: MeasurementResult,
        path: String?,
        fragment: UploadFinishedFragment,
        processMethod: String) {

        if (processMethod == "online") { //Only uploade image to firebase in case of online processing

            val thumbnailBitmap =
                Bitmap.createScaledBitmap(measurementResult.getMaskedInput(), 64, 48, true)
            val storageItem = FireBaseStorageItem(
                forestryName = sessionManager?.forestryName!!,
                maskedImageThumbnail = thumbnailBitmap,
                maskedImage = measurementResult.getMaskedInput()
            )

            val uriPairs = dataClient?.fireStore?.uploadToFireBaseStorage(storageItem)

            val fireStoreItem = FireStoreItem(
                forestryId = sessionManager?.forestryID!!,
                leaderId = sessionManager?.leaderID!!,
                userId = sessionManager?.userID!!,
                userRole = sessionManager?.userRole!!,
                imageUrl = uriPairs?.first?.first!!,
                imageRef = uriPairs.first.second,
                thumbnailUrl = uriPairs.second.first,
                thumbnailRef = uriPairs.second.second,
                woodType = sessionManager?.woodType,
                woodLength = sessionManager?.woodLength?.toDouble()!!,
                woodVolume = measurementResult.getVolume(),
                locLat = Util.lat,
                locLon = Util.long,
                timestamp = LocalDate.now().toString()
            )

            dataClient?.fireStore?.uploadToFireStore(fireStoreItem)
        }
        else {
            screen?.updateView(fragment)
        }
    }

    fun deleteImageFromLocalDatabase(id: String?) {
        id?.let {
            doAsync {
                val imageItem = roomModule?.database?.imageItemDao()?.getImageById(id)
                imageItem?.let { roomModule?.database?.imageItemDao()?.deleteImage(imageItem) }
            }
        }
    }

}
