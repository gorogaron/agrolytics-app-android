package com.agrolytics.agrolytics_android.data.local

import android.content.Context
import androidx.room.Room
import com.agrolytics.agrolytics_android.data.local.dao.CachedImageItemDao
import com.agrolytics.agrolytics_android.data.local.dao.ProcessedImageItemDao
import com.agrolytics.agrolytics_android.data.local.dao.UnprocessedImageItemDao

class LocalDataClient(context: Context) {

    private val databaseName: String = "image-list.db"

    var database: ImageDatabase
    var processed : ProcessedImageItemDao
    var unprocessed : UnprocessedImageItemDao
    var cache : CachedImageItemDao

    init {
        database = Room
            .databaseBuilder(
                context,
                ImageDatabase::class.java,
                databaseName)
            .build()
        processed = database.processedImageDao()
        unprocessed = database.unprocessedImageDao()
        cache = database.cachedImageDao()
    }

    fun clearDatabase() {
        database.clearAllTables()
    }

}