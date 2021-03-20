package com.agrolytics.agrolytics_android.ui.measurement.utils
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.agrolytics.agrolytics_android.data.DataClient
import com.agrolytics.agrolytics_android.data.firebase.model.FireStoreImageItem
import com.agrolytics.agrolytics_android.types.ConfigInfo.CACHED_IMAGE_ITEM_FIRESTORE_ID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Exception


class DeleteWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    val dataClient: DataClient by inject()

    override suspend fun doWork(): Result {

        return try {
            // Cached image item áthelyezése a deleted_images collectionbe
            val itemFirestoreId = inputData.getString(CACHED_IMAGE_ITEM_FIRESTORE_ID)
            val cachedImageItem = dataClient.local.cache.getByFirestoreId(itemFirestoreId!!)!!
                try {
                    dataClient.fireBase.fireStore.moveImageToDeletedCollection(FireStoreImageItem(cachedImageItem))
                    dataClient.local.cache.delete(cachedImageItem)
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
        private val TAG = DeleteWorker::class.java.simpleName
    }
}