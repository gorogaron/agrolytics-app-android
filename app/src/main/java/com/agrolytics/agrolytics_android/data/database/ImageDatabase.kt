package com.agrolytics.agrolytics_android.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agrolytics.agrolytics_android.data.database.dao.CachedImageItemDao
import com.agrolytics.agrolytics_android.data.database.dao.ProcessedImageItemDao
import com.agrolytics.agrolytics_android.data.database.dao.UnprocessedImageItemDao
import com.agrolytics.agrolytics_android.data.database.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.database.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.database.tables.UnprocessedImageItem

@Database(entities = [
    CachedImageItem::class,
    UnprocessedImageItem::class,
    ProcessedImageItem::class], version = 1)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun cachedImageDao() : CachedImageItemDao
    abstract fun unprocessedImageDao(): UnprocessedImageItemDao
    abstract fun processedImageDao(): ProcessedImageItemDao
}