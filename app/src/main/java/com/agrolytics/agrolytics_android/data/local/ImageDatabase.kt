package com.agrolytics.agrolytics_android.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.agrolytics.agrolytics_android.data.local.dao.CachedImageItemDao
import com.agrolytics.agrolytics_android.data.local.dao.ProcessedImageItemDao
import com.agrolytics.agrolytics_android.data.local.dao.UnprocessedImageItemDao
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem

@TypeConverters(TypeConverter::class)
@Database(entities = [
    CachedImageItem::class,
    UnprocessedImageItem::class,
    ProcessedImageItem::class], version = 1)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun cachedImageDao() : CachedImageItemDao
    abstract fun unprocessedImageDao(): UnprocessedImageItemDao
    abstract fun processedImageDao(): ProcessedImageItemDao
}