package com.agrolytics.agrolytics_android.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.BaseImageItem
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class MainViewModel : ViewModel(), KoinComponent {
    private val dataClient : DataClient by inject()

    var lastMeasurementItems = MutableLiveData<ArrayList<BaseImageItem>>()
    var lastSessionId = MutableLiveData<Long>()

    fun getLastMeasurementItems() = viewModelScope.launch(Dispatchers.IO) {
        val imageItemList = ArrayList<BaseImageItem>()
        val latestId = getSessionIdList().max()

        if (latestId != null){
            lastSessionId.postValue(latestId)
            val cachedImages = dataClient.local.cache.getBySessionId(latestId)
            val processedImages = dataClient.local.processed.getBySessionId(latestId)
            val unprocessedImages = dataClient.local.unprocessed.getBySessionId(latestId)

            imageItemList.addAll(cachedImages)
            imageItemList.addAll(processedImages)
            imageItemList.addAll(unprocessedImages)

            imageItemList.sortBy {
                when (it.getItemType()) {
                    ConfigInfo.IMAGE_ITEM_TYPE.PROCESSED -> { (it as ProcessedImageItem).timestamp }
                    ConfigInfo.IMAGE_ITEM_TYPE.UNPROCESSED -> { (it as UnprocessedImageItem).timestamp }
                    ConfigInfo.IMAGE_ITEM_TYPE.CACHED -> { (it as CachedImageItem).timestamp }
                }
            }
            lastMeasurementItems.postValue(imageItemList)
        }
        else {
            lastMeasurementItems.postValue(null)
        }
    }

    //TODO: Ezt használjuk az ImagesViewModel-ben is, vigyük át egy helyre
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