package com.agrolytics.agrolytics_android.ui.measurement.presenter

import android.graphics.Bitmap
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.firebase.model.FireBaseStorageItem
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImageItem
import com.agrolytics.agrolytics_android.ui.base.BasePresenter
import com.agrolytics.agrolytics_android.ui.measurement.activity.ApproveMeasurementActivity
import com.agrolytics.agrolytics_android.utils.Util
import com.google.firebase.firestore.GeoPoint
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.time.LocalDate

class ApproveMeasurementPresenter : BasePresenter<ApproveMeasurementActivity>() {

    suspend fun uploadMeasurementToFirebase(processedImageItem: ProcessedImageItem) {

        if (ApproveMeasurementActivity.method == "online") {

            val thumbnailBitmap =
                Bitmap.createScaledBitmap(processedImageItem.image, 64, 48, true)

            val storageItem =
                FireBaseStorageItem(
                    forestryName = sessionManager?.forestryName!!,
                    maskedImageThumbnail = thumbnailBitmap,
                    maskedImage = processedImageItem.image
                )

            /*val uriPairs = dataClient!!.fireBase.storage.uploadToFireBaseStorage(storageItem)

            val fireStoreItem =
                FireStoreImageItem(
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
                    location = GeoPoint(Util.lat!!, Util.long!!),
                    timestamp = LocalDate.now().toString()
                )

            val firestoreId = dataClient!!.fireBase.fireStore.uploadToFireStore(fireStoreItem)*/

        }
    }

}
