package com.agrolytics.agrolytics_android.data.firebase

import android.graphics.Bitmap
import com.agrolytics.agrolytics_android.data.firebase.model.FireBaseStorageItem
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class FireBaseDataClient: KoinComponent {

    private val sessionManager : SessionManager by inject()

    val storage = Storage()
    val fireStore = FireStore()

    suspend fun uploadProcessedImageItem(processedImageItem: ProcessedImageItem)
    : CachedImageItem {
        val fireBaseStorageItem = FireBaseStorageItem(
            forestryName = sessionManager.forestryName,
            maskedImage = processedImageItem.image!!,
            maskedImageThumbnail = Bitmap.createScaledBitmap(
                processedImageItem.image,
                64,
                48,
                true),
            imageName = "${sessionManager.userId}_${processedImageItem.timestamp}"
        )

        storage.uploadToFireBaseStorage(fireBaseStorageItem)
        val firestoreImageItem = FireStoreImageItem(
            timestamp = processedImageItem.timestamp,
            sessionId = processedImageItem.sessionId,
            forestryId = sessionManager.forestryId,
            leaderId = sessionManager.leaderId,
            userId = sessionManager.userId,
            userRole = sessionManager.userRole,
            woodType = processedImageItem.woodType,
            woodLength = processedImageItem.woodLength,
            woodVolume = processedImageItem.woodVolume,
            location = processedImageItem.location,
            firestoreId = ""
        )
        firestoreImageItem.firestoreId = fireStore.uploadToFireStore(firestoreImageItem)

        val cachedImageItem = CachedImageItem(
            timestamp = firestoreImageItem.timestamp,
            sessionId = firestoreImageItem.sessionId,
            forestryId = firestoreImageItem.forestryId,
            leaderId = firestoreImageItem.leaderId,
            userId = firestoreImageItem.userId,
            userRole = firestoreImageItem.userRole,
            woodType = firestoreImageItem.woodType!!,
            woodLength = firestoreImageItem.woodLength,
            woodVolume = firestoreImageItem.woodVolume,
            location = firestoreImageItem.location!!,
            firestoreId = firestoreImageItem.firestoreId,
            image = processedImageItem.image
        )
        return cachedImageItem
    }

    suspend fun downloadImageItemsAfterTimestamp(timestamp : Long): List<CachedImageItem> {
        val cachedImageItems = ArrayList<CachedImageItem>()
        val firestoreItems = fireStore.downloadFromFireStoreAfterTimestamp(timestamp)
        val timestampsBySessionIds = firestoreItems.groupByTo(HashMap(), {it.sessionId}, {it.timestamp})
        for (firestoreItem in firestoreItems) {
            val timestampsOfSession = timestampsBySessionIds[firestoreItem.sessionId] as List<Long>
            val minTimestampOfSession = timestampsOfSession.min()
            val image: Bitmap? = if (firestoreItem.timestamp == minTimestampOfSession) {
                storage.downloadImage(sessionManager.forestryName, sessionManager.userId, firestoreItem.timestamp)
            } else {
                null
            }
            cachedImageItems.add(
                CachedImageItem(
                    timestamp = firestoreItem.timestamp,
                    sessionId = firestoreItem.sessionId,
                    forestryId = firestoreItem.forestryId,
                    leaderId = firestoreItem.leaderId,
                    userId = firestoreItem.userId,
                    userRole = firestoreItem.userRole,
                    woodType = firestoreItem.woodType!!,
                    woodLength = firestoreItem.woodLength,
                    woodVolume = firestoreItem.woodVolume,
                    location = firestoreItem.location!!,
                    image = image,
                    firestoreId = firestoreItem.firestoreId
            ))
        }
        return cachedImageItems
    }

}