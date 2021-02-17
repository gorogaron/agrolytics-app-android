package com.agrolytics.agrolytics_android.ui.measurement.presenter

import android.graphics.Bitmap
import com.agrolytics.agrolytics_android.data.firestore.*
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.networking.model.MeasurementResult
import com.agrolytics.agrolytics_android.ui.imageFinished.fragment.UploadFinishedFragment
import com.agrolytics.agrolytics_android.ui.measurement.activity.ApproveMeasurementActivity
import com.agrolytics.agrolytics_android.utils.Util
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.time.LocalDate

class ApproveMeasurementPresenter : BasePresenter<ApproveMeasurementActivity>() {

    val TAG = "UploadFinishedPresenter"

    private fun saveUploadedImageItem(
        measurementResult: MeasurementResult?,
        path: String?,
        fragment: UploadFinishedFragment,
        serverImage: String?,
        id: String?
    ) {
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
                screen?.updateView(fragment)
            }
        }
    }

    suspend fun uploadImageToStorage(
        measurementResult: MeasurementResult,
        path: String?,
        fragment: UploadFinishedFragment,
        processMethod: String
    )
    {

        if (processMethod == "online") { //Only uploade image to firebase in case of online processing

            val thumbnailBitmap =
                Bitmap.createScaledBitmap(measurementResult.getMaskedInput(), 64, 48, true)
            val storageItem = FireBaseStorageItem(
                forestryName = sessionManager?.forestryName!!,
                maskedImageThumbnail = thumbnailBitmap,
                maskedImage = measurementResult.getMaskedInput()
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
                woodVolume = measurementResult.getVolume(),
                locLat = Util.lat,
                locLon = Util.long,
                timestamp = LocalDate.now().toString()
            )

            val firestoreId = dataClient?.fireStore?.uploadToFireStore(fireStoreItem)

//            val imageItem = ImageItem(
//                id = (0..10000).random().toString(),
//                session_id = (0..10000).random().toString(),
//                localPath = path,
//                isPushedToServer = true,
//                latitude = fireStoreItem.locLat,
//                longitude = fireStoreItem.locLon,
//                length = fireStoreItem.woodLength,
//                volume = fireStoreItem.woodVolume,
//                time = fireStoreItem.timestamp,
//                imageUrl = fireStoreItem.imageUrl,
//                imageRef = fireStoreItem.imageRef,
//                firestoreId = firestoreId,
//                userID = fireStoreItem.userId,
//                leaderID = fireStoreItem.leaderId,
//                forestryID = fireStoreItem.forestryId,
//                rodLength = measurementResult.rod_length,
//                rodLengthPixel = measurementResult.rod_length_pixel,
//                thumbnailRef = fireStoreItem.thumbnailRef,
//                thumbnailUrl = fireStoreItem.thumbnailUrl,
//                woodType = fireStoreItem.woodType
//            )
//            doAsync {
//                dataClient?.local?.addImage(imageItem)
//            }
        }
        else {
            screen?.updateView(fragment)
        }
    }

    fun deleteImageFromLocalDatabase(id: String?) {
        id?.let {
            doAsync {
                val imageItem = dataClient?.local?.getImageById(id)
                imageItem?.let {
                    dataClient?.local?.deleteImage(imageItem)
                }
            }
        }
    }

}
