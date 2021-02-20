package com.agrolytics.agrolytics_android.data.local

import android.content.Context
import androidx.room.Room
import com.agrolytics.agrolytics_android.data.local.query.CachedImageQueries
import com.agrolytics.agrolytics_android.data.local.query.ProcessedImageQueries
import com.agrolytics.agrolytics_android.data.local.query.UnprocessedImageQueries
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem

class LocalDataClient(context: Context) {

    private val databaseName: String = "image-list.db"

    var database: ImageDatabase
    var processed : ProcessedImageQueries
    var unprocessed : UnprocessedImageQueries
    var cache : CachedImageQueries

    init {
        database = Room
            .databaseBuilder(
                context,
                ImageDatabase::class.java,
                databaseName)
            .build()
        processed = ProcessedImageQueries(database.processedImageDao())
        unprocessed = UnprocessedImageQueries(database.unprocessedImageDao())
        cache = CachedImageQueries(database.cachedImageDao())
    }

    fun clearDatabase() {
        database.clearAllTables()
    }

}