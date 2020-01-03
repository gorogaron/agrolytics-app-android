package com.agrolytics.agrolytics_android.database.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.agrolytics.agrolytics_android.database.dao.ImageItemDao
import com.agrolytics.agrolytics_android.networking.model.ImageItem


@Database(entities = [ImageItem::class], version = 1)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageItemDao() : ImageItemDao
}