package com.agrolytics.agrolytics_android.database.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agrolytics.agrolytics_android.database.local.ImageItemDao
import com.agrolytics.agrolytics_android.database.local.ImageItem


@Database(entities = [ImageItem::class], version = 1)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageItemDao() : ImageItemDao
}