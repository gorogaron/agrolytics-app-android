package com.agrolytics.agrolytics_android.ui.measurement.utils
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.types.ConfigInfo.PROCESSED_IMAGE_ITEM_SESSION_ID
import com.google.firebase.FirebaseException
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
            val processedImageItemSessionId = inputData.getLong(PROCESSED_IMAGE_ITEM_SESSION_ID, 0)
            val processedImageItems = dataClient.local.processed.getBySessionId(processedImageItemSessionId)
            for (processedImageItem in processedImageItems) {
                try {
                    val cachedImageItem = dataClient.fireBase.uploadProcessedImageItem(processedImageItem = processedImageItem)
                    dataClient.local.processed.delete(processedImageItem)
                    dataClient.local.cache.add(cachedImageItem)
                }
                catch (e: Exception) {
                    // TODO: Exception normális lekezelése.
                }
            }

            // If there were no errors, return SUCCESS
            Result.success()
        } catch (throwable: Throwable) {

            // Technically WorkManager will return Result.failure()
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Log.e(TAG, "Error uploading item", throwable)
            Result.failure()
        }
    }

    companion object {
        private val TAG = UploadWorker::class.java.simpleName
    }
}