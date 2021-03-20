package com.agrolytics.agrolytics_android.ui.measurement.utils
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.types.ConfigInfo.PROCESSED_IMAGE_ITEM_TIMESTAMP
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Exception


class UploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    val dataClient: DataClient by inject()

    override suspend fun doWork(): Result {

        return try {
            // ProcessedImageItem feltöltése Firebase-re ha van internet
            val processedImageItemTimestamp = inputData.getLong(PROCESSED_IMAGE_ITEM_TIMESTAMP, 0)
            val processedImageItem = dataClient.local.processed.getByTimestamp(processedImageItemTimestamp)!!
            try {
                val cachedImageItem = dataClient.fireBase.uploadProcessedImageItem(processedImageItem)
                dataClient.local.processed.delete(processedImageItem)
                dataClient.local.cache.add(cachedImageItem)
            }
            catch (e: Exception) {
                // TODO: Exception normális lekezelése.
            }
            Result.success()
        } catch (throwable: Throwable) {
            Log.e(TAG, "Error uploading item", throwable)
            Result.failure()
        }
    }

    companion object {
        private val TAG = UploadWorker::class.java.simpleName
    }
}