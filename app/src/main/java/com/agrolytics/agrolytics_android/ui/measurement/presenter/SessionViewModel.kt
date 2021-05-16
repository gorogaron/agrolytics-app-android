package com.agrolytics.agrolytics_android.ui.measurement.presenter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import com.agrolytics.agrolytics_android.ui.images.recyclerview.SessionItem
import com.agrolytics.agrolytics_android.ui.measurement.activity.SessionActivity
import com.agrolytics.agrolytics_android.utils.SessionManager
import com.agrolytics.agrolytics_android.utils.Util.Companion.round
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


@KoinApiExtension
class SessionViewModel: ViewModel(), KoinComponent {

    private val dataClient: DataClient by inject()
    private val sessionManager: SessionManager by inject()

    var imageItemsInSession = MutableLiveData<ArrayList<BaseImageItem>>()

    fun getAllLocalImagesInSession(sessionId : Long) = viewModelScope.launch(Dispatchers.IO) {
        val processedImageItemsInSession = dataClient.local.processed.getBySessionId(sessionId)
        val unprocessedImageItemsInSession = dataClient.local.unprocessed.getBySessionId(sessionId)
        val cachedImageItemsInSession = dataClient.local.cache.getBySessionId(sessionId)

        /**A sessionben lévő cached itemekhez töltsük le a képet, ha nincs letöltve*/
        for (cachedImageItem in cachedImageItemsInSession) {
            if (cachedImageItem.image == null) {
                cachedImageItem.image = dataClient.fireBase.storage.downloadImage(sessionManager.forestryName, sessionManager.userId, cachedImageItem.timestamp)
                dataClient.local.cache.update(cachedImageItem)
            }
        }

        val imageItemList = ArrayList<BaseImageItem>(processedImageItemsInSession)
        imageItemList.addAll(unprocessedImageItemsInSession)
        imageItemList.addAll(cachedImageItemsInSession)
        imageItemList.sortByDescending { it.timestamp }
        imageItemsInSession.postValue(imageItemList)
    }

    fun isSessionDone() : Boolean {
        if (imageItemsInSession.value != null) {
            for (imageItem in imageItemsInSession.value!!) {
                if (imageItem.getItemType() == ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED) {
                    return false
                }
            }
        }
        else {
            return false
        }
        return true
    }

}