package com.agrolytics.agrolytics_android.data.database

import android.content.Context
import androidx.room.Room
import com.agrolytics.agrolytics_android.data.database.tables.CachedImageItem

class LocalDataClient(context: Context) {

    private val databaseName: String = "image-list.db"

    var database: ImageDatabase? = null

    init {
        database = Room
            .databaseBuilder(
                context,
                ImageDatabase::class.java,
                databaseName)
            .build()
    }

    fun getImageById(id: String) : CachedImageItem? {
        return database?.cachedImageDao()?.getById(id)
    }

    fun getImagesBySessionId(sessionId: String) : List<CachedImageItem>? {
        return database?.cachedImageDao()?.getBySessionId(sessionId)
    }

    fun getAllImages() : List<CachedImageItem>? {
        return database?.cachedImageDao()?.getAll()
    }

    fun addImage(cachedImageItem: CachedImageItem) {
        database?.cachedImageDao()?.add(cachedImageItem)
    }

    fun deleteImage(cachedImageItem: CachedImageItem) {
        database?.cachedImageDao()?.delete(cachedImageItem)
    }

    fun deleteImagesBySessionId(sessionId: String) {
        database?.cachedImageDao()?.deleteBySessionId(sessionId)
    }

    fun deleteAllImage() {
        database?.cachedImageDao()?.deleteAll()
    }

    fun clearDatabase() {
        database?.clearAllTables()
    }
}