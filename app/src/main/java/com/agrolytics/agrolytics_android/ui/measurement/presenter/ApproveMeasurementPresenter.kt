package com.agrolytics.agrolytics_android.ui.measurement.presenter

import android.graphics.Bitmap
import com.agrolytics.agrolytics_android.data.database.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.firestore.*
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.ui.measurement.activity.ApproveMeasurementActivity
import com.agrolytics.agrolytics_android.utils.Util
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.time.LocalDate

class ApproveMeasurementPresenter : BasePresenter<ApproveMeasurementActivity>() {

    val TAG = "UploadFinishedPresenter"

    private fun saveUploadedImageItem(processedImageItem: ProcessedImageItem) {
//        val imageItem = CachedImageItem(
//            id = Integer.parseInt(id).toLong(),
//            session_id = "",
//            localPath = path ?: "",
//            isPushedToServer = true,
//            latitude = Util.lat ?: 0.0,
//            longitude = Util.long ?: 0.0,
//            length = sessionManager?.woodLength?.toDouble() ?: 0.0,
//            volume = measurementResult?.getVolume(),
//            time = Util.getCurrentDateString(),
//            imageUrl = serverImage!!
//        )
        doAsync {
//            dataClient?.local?.addImage(imageItem)
            uiThread {
                //screen?.updateView(fragment)
            }
        }
    }

    suspend fun uploadMeasurementToFirebase(processedImageItem: ProcessedImageItem, processMethod: String) {

        if (processMethod == "online") { //Only uploade image to firebase in case of online processing

            val thumbnailBitmap =
                Bitmap.createScaledBitmap(processedImageItem.image, 64, 48, true)

            val storageItem = FireBaseStorageItem(
                forestryName = sessionManager?.forestryName!!,
                maskedImageThumbnail = thumbnailBitmap,
                maskedImage = processedImageItem.image
            )

            val uriPairs = dataClient?.fireStore?.uploadToFireBaseStorage(storageItem)

            val fireStoreItem = FireStoreImageItem(
                forestryId = sessionManager?.forestryId!!,
                leaderId = sessionManager?.leaderId!!,
                userId = sessionManager?.userId!!,
                userRole = sessionManager?.userRole!!,
                imageUrl = uriPairs?.first?.first!!,
                imageRef = uriPairs.first.second,
                thumbnailUrl = uriPairs.second.first,
                thumbnailRef = uriPairs.second.second,
                woodType = sessionManager?.woodType,
                woodLength = sessionManager?.woodLength?.toDouble()!!,
                woodVolume = processedImageItem.woodVolume,
                locLat = Util.lat,
                locLon = Util.long,
                timestamp = LocalDate.now().toString()
            )

            val firestoreId = dataClient?.fireStore?.uploadToFireStore(fireStoreItem)

        }
    }

}
