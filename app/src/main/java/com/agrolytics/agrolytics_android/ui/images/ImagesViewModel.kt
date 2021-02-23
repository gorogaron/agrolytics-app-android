package com.agrolytics.agrolytics_android.ui.images

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem
import com.agrolytics.agrolytics_android.utils.SessionManager
import org.jetbrains.anko.doAsync
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


@KoinApiExtension
class ImagesViewModel: ViewModel(), KoinComponent {

    private val dataClient: DataClient by inject()
    private val sessionManager: SessionManager by inject()

    var cachedImageItems = MutableLiveData<List<CachedImageItem>>()
    var processedImageItems = MutableLiveData<List<ProcessedImageItem>>()
    var unprocessedImageItems = MutableLiveData<List<UnprocessedImageItem>>()

    fun getUnprocessedImageItems() {
        doAsync {
            unprocessedImageItems.value = dataClient.local.unprocessed.getBySessionId(sessionManager.sessionId)
        }
    }

    fun getProcessedImageItems() {
        processedImageItems.value = dataClient.local.processed.getBySessionId(sessionManager.sessionId)
    }

    fun getCachedImageItems() {
        cachedImageItems.value = dataClient.local.cache.getImagesBySessionId(sessionManager.sessionId)
    }
}