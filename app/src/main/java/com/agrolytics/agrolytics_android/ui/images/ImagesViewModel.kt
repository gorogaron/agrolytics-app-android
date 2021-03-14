package com.agrolytics.agrolytics_android.ui.images

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.ui.images.recyclerview.SessionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


@KoinApiExtension
class ImagesViewModel: ViewModel(), KoinComponent {

    private val dataClient: DataClient by inject()

    var sessionItems = MutableLiveData<List<SessionItem>>()

    fun getSessionItems() = viewModelScope.launch(Dispatchers.IO) {
        // Cache frissítése
        updateLocalCache()

        val sessionIds = getSessionIdList()
        val sessionItemList = ArrayList<SessionItem>()

        // Minden session-höz előállítjuk az item-eket
        for (sessionId in sessionIds) {
            // SessionItem-ek alapméretezett attribútumainak inicializálása
            var woodLength = -1.0
            var woodVolume = -1.0
            var woodType = ""
            var sessionImage = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)

            // Kigyűjtött értékek listái
            val woodLengths = ArrayList<Double>()
            val woodVolumes = ArrayList<Double>()
            val woodTypes = ArrayList<String>()

            // Egy adott session-höz lekérdezzük az összes típusú item-eket
            val cachedImages = dataClient.local.cache.getBySessionId(sessionId)
            val processedImages = dataClient.local.processed.getBySessionId(sessionId)
            val unprocessedImages = dataClient.local.unprocessed.getBySessionId(sessionId)

            for (cachedImage in cachedImages) {
                woodLengths.add(cachedImage.woodLength)
                woodTypes.add(cachedImage.woodType)
                woodVolumes.add(cachedImage.woodVolume)
                if (cachedImage.timestamp == sessionId) sessionImage = cachedImage.image
            }

            for (processedImage in processedImages) {
                woodLengths.add(processedImage.woodLength)
                woodTypes.add(processedImage.woodType)
                woodVolumes.add(processedImage.woodVolume)
                if (processedImage.timestamp == sessionId) sessionImage = processedImage.image
            }

            for (unprocessedImage in unprocessedImages) {
                woodLengths.add(unprocessedImage.woodLength)
                woodTypes.add(unprocessedImage.woodType)
                if (unprocessedImage.timestamp == sessionId) sessionImage = unprocessedImage.image
            }

            if (woodLengths.distinct().size == 1) {
                woodLength = woodLengths[0]
            }

            if (woodTypes.distinct().size == 1) {
                woodType = woodTypes[0]
            }

            if (unprocessedImages.isNotEmpty()) {
                woodVolume = woodVolumes.sum()
            }

            // SessionItem hozzáadása a listához
            sessionItemList.add(SessionItem(
                woodLength = woodLength,
                woodType = woodType,
                woodVolume = woodVolume,
                timestamp = sessionId,
                sessionImage = sessionImage))
        }
        sessionItems.postValue(sessionItemList)
    }

    private fun getSessionIdList() : ArrayList<Long> {
        val sessionIdListUnprocessed = dataClient.local.unprocessed.getAllSessionIds()
        val sessionIdListProcessed = dataClient.local.processed.getAllSessionIds()
        val sessionIdListCached = dataClient.local.cache.getAllSessionIds()

        val sessionIdList = ArrayList<Long>()
        sessionIdList.addAll(sessionIdListCached)
        sessionIdList.addAll(sessionIdListProcessed)
        sessionIdList.addAll(sessionIdListUnprocessed)
        return ArrayList(sessionIdList.distinct())
    }

    private suspend fun updateLocalCache() {
        val cachedImageItemIdsNotToDownload = dataClient.local.cache.getAllTimestamps()
        val cachedImageItemsToSave = dataClient.fireBase.downloadImageItems(cachedImageItemIdsNotToDownload)
        for (cachedImageItem in cachedImageItemsToSave) {
            dataClient.local.cache.add(cachedImageItem)
        }
    }
}