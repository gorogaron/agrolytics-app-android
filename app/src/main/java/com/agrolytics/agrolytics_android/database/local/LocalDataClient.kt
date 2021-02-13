package com.agrolytics.agrolytics_android.database.local

import android.content.Context
import androidx.room.Room

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

    fun getImageById(id: String) : ImageItem? {
        return database?.imageItemDao()?.getImageById(id)
    }

    fun getImagesBySessionId(sessionId: String) : List<ImageItem>? {
        return database?.imageItemDao()?.getImagesBySessionId(sessionId)
    }

    fun getAllImages() : List<ImageItem>? {
        return database?.imageItemDao()?.getAllImage()
    }

    fun getAllPushedImages(pushed: Boolean) : List<ImageItem> {
        return database?.imageItemDao()?.getAllImage(pushed)
    }

    fun getSessionIdForImageId(id: String) : String? {
        return database?.imageItemDao()?.getSessionIdForImageId(id)
    }

    fun addImage(imageItem: ImageItem) {
        database?.imageItemDao()?.addImage(imageItem)
    }

    fun deleteImage(imageItem: ImageItem) {
        database?.imageItemDao()?.deleteImage(imageItem)
    }

    fun deleteImageById(id: String) {
        database?.imageItemDao()?.deleteImageById(id)
    }

    fun deleteImagesBySessionId(sessionId: String) {
        database?.imageItemDao()?.deleteImagesBySessionId(sessionId)
    }

    fun deleteAllImage() {
        database?.imageItemDao()?.deleteAllImage()
    }

    fun clearDatabase() {
        database?.clearAllTables()
    }
}