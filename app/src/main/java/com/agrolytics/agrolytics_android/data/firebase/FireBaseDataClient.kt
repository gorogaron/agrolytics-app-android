package com.agrolytics.agrolytics_android.data.firebase

import android.graphics.Bitmap
import com.agrolytics.agrolytics_android.data.firebase.model.FireBaseStorageItem
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class FireBaseDataClient: KoinComponent {

    private val sessionManager : SessionManager by inject()

    val storage = Storage()
    val fireStore = FireStore()

    suspend fun uploadProcessedImageItem(processedImageItem: ProcessedImageItem)
    : CachedImageItem {
        val fireBaseStorageItem = FireBaseStorageItem(
            forestryName = sessionManager.forestryName,
            maskedImage = processedImageItem.image,
            maskedImageThumbnail = Bitmap.createScaledBitmap(
                processedImageItem.image,
                64,
                48,
                true)
        )
        val (imageUrl, thumbUrl) = storage.uploadToFireBaseStorage(fireBaseStorageItem)
        val firestoreImageItem = FireStoreImageItem(
            timestamp = processedImageItem.timestamp,
            sessionId = processedImageItem.sessionId,
            forestryId = sessionManager.forestryId,
            leaderId = sessionManager.leaderId,
            userId = sessionManager.userId,
            userRole = sessionManager.userRole,
            imageUrl = imageUrl,
            thumbnailUrl = thumbUrl,
            woodType = processedImageItem.woodType,
            woodLength = processedImageItem.woodLength,
            woodVolume = processedImageItem.woodVolume,
            location = processedImageItem.location
        )
        val firestoreId = fireStore.uploadToFireStore(firestoreImageItem)
        val cachedImageItem = CachedImageItem(
            timestamp = firestoreImageItem.timestamp,
            sessionId = firestoreImageItem.sessionId,
            forestryId = firestoreImageItem.forestryId,
            leaderId = firestoreImageItem.leaderId,
            userId = firestoreImageItem.userId,
            userRole = firestoreImageItem.userRole,
            imageUrl = firestoreImageItem.imageUrl,
            thumbUrl = firestoreImageItem.thumbnailUrl,
            woodType = firestoreImageItem.woodType!!,
            woodLength = firestoreImageItem.woodLength,
            woodVolume = firestoreImageItem.woodVolume,
            location = firestoreImageItem.location!!,
            firestoreId = firestoreId,
            image = processedImageItem.image
        )
        return cachedImageItem
    }

    suspend fun downloadImageItems(timestamps: List<Long>)
    : List<CachedImageItem> {
        val cachedImageItems = ArrayList<CachedImageItem>()
        val (firestoreItems, firestoreIds) = fireStore.downloadFromFireStore(timestamps)
        for (i in firestoreItems.indices) {
            val imageUrl = firestoreItems[i].imageUrl
            val image = storage.downloadImage(imageUrl)
            cachedImageItems.add(
                CachedImageItem(
                    timestamp = firestoreItems[i].timestamp,
                    sessionId = firestoreItems[i].sessionId,
                    forestryId = firestoreItems[i].forestryId,
                    leaderId = firestoreItems[i].leaderId,
                    userId = firestoreItems[i].userId,
                    userRole = firestoreItems[i].userRole,
                    imageUrl = firestoreItems[i].imageUrl,
                    thumbUrl = firestoreItems[i].thumbnailUrl,
                    woodType = firestoreItems[i].woodType!!,
                    woodLength = firestoreItems[i].woodLength,
                    woodVolume = firestoreItems[i].woodVolume,
                    location = firestoreItems[i].location!!,
                    firestoreId = firestoreIds[i],
                    image = image!!
            ))
        }
        return cachedImageItems
    }
}