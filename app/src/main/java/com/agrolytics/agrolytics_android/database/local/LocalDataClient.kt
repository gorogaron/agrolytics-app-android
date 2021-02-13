package com.agrolytics.agrolytics_android.database.local

import android.content.Context
import androidx.room.Room
import org.jetbrains.anko.doAsync


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

    fun getImageById(id: String) {
        database?.imageItemDao()?.getImageById(id)
    }

    fun getImagesBySessionId(sessionId: String) {
        database?.imageItemDao()?.getImagesBySessionId(sessionId)
    }

    fun getAllImages() {
        database?.imageItemDao()?.getAllImage()
    }

    fun getSessionIdForImageId(id: String) {
        database?.imageItemDao()?.getSessionIdForImageId(id)
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