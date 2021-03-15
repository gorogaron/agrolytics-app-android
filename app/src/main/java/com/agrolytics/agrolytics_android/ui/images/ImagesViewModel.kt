package com.agrolytics.agrolytics_android.ui.images

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
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
            val imageItems = mutableListOf<BaseImageItem>()
            var sessionContainsUnprocessedImageItem = false
            imageItems.addAll(dataClient.local.cache.getBySessionId(sessionId))
            imageItems.addAll(dataClient.local.processed.getBySessionId(sessionId))
            imageItems.addAll(dataClient.local.unprocessed.getBySessionId(sessionId))

            for (imageItem in imageItems) {
                woodLengths.add(imageItem.woodLength)
                woodTypes.add(imageItem.woodType)

                if (imageItem.getItemType() == ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED) {
                    woodVolumes.add((imageItem as ProcessedImageItem).woodVolume)
                }
                else if (imageItem.getItemType() == ConfigInfo.IMAGE_ITEM_TYPE.CACHED) {
                    woodVolumes.add((imageItem as CachedImageItem).woodVolume)
                }
                else {
                    sessionContainsUnprocessedImageItem = true
                }
            }

            /**Kiszűrjük azokat az itemeket ahol az image null, és ezek közül megkeressük a legrégebbit*/
            sessionImage = imageItems.filter { it.image != null }.minBy { it.timestamp }!!.image

            if (woodLengths.distinct().size == 1) {
                woodLength = woodLengths[0]
            }

            if (woodTypes.distinct().size == 1) {
                woodType = woodTypes[0]
            }

            if (!sessionContainsUnprocessedImageItem) {
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

}