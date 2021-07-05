package com.agrolytics.agrolytics_android.ui.images

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.images.recyclerview.SessionItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util.Companion.round
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


@KoinApiExtension
class ImagesViewModel: ViewModel(), KoinComponent {

    private val dataClient: DataClient by inject()
    private val sessionManager: SessionManager by inject()

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
            var sessionThumbnailImageItem : BaseImageItem

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
                    woodVolumes.add((imageItem as ProcessedImageItem).woodVolume + imageItem.addedWoodVolume)
                }
                else if (imageItem.getItemType() == ConfigInfo.IMAGE_ITEM_TYPE.CACHED) {
                    woodVolumes.add((imageItem as CachedImageItem).woodVolume + imageItem.addedWoodVolume)
                }
                else {
                    sessionContainsUnprocessedImageItem = true
                }
            }

            //Megkeressük a sessionben lévő legrégebbi itemet, és ha nincs letöltve a hozzá tartozó kép, letöltjük
            sessionThumbnailImageItem = imageItems.minBy { it.timestamp }!!
            if (sessionThumbnailImageItem.image == null) {
                sessionThumbnailImageItem.image = dataClient.fireBase.storage.downloadImage(sessionManager.forestryName, sessionManager.userId, sessionThumbnailImageItem.timestamp)
                dataClient.local.cache.update(sessionThumbnailImageItem as CachedImageItem) //Csak cachedImageItemnél lehet olyan, hogy image=null
            }

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
                woodVolume = woodVolume.round(2),
                sessionId = sessionId,
                sessionImage = sessionThumbnailImageItem.image!!))
        }
        sessionItemList.sortByDescending { it.sessionId }
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