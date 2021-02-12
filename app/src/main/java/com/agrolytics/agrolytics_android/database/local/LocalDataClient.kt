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
        doAsync{
            database?.imageItemDao()?.getImageById(id)
        }
    }

    fun getImagesBySessionId(sessionId: String) {
        doAsync {
            database?.imageItemDao()?.getImagesBySessionId(sessionId)
        }
    }

    fun getAllImages() {
        doAsync {
            database?.imageItemDao()?.getAllImage()
        }
    }

    fun getSessionIdForImageId(id: String) {
        doAsync {
            database?.imageItemDao()?.getSessionIdForImageId(id)
        }
    }

    fun addImage(imageItem: ImageItem) {
        doAsync {
            database?.imageItemDao()?.addImage(imageItem)
        }
    }

    fun deleteImage(imageItem: ImageItem) {
        doAsync {
            database?.imageItemDao()?.deleteImage(imageItem)
        }
    }

    fun deleteImageById(id: String) {
        doAsync {
            database?.imageItemDao()?.deleteImageById(id)
        }
    }

    fun deleteImagesBySessionId(sessionId: String) {
        doAsync {
            database?.imageItemDao()?.deleteImagesBySessionId(sessionId)
        }
    }

    fun deleteAllImage() {
        doAsync {
            database?.imageItemDao()?.deleteAllImage()
        }
    }

    fun clearDatabase() {
        doAsync {
            database?.clearAllTables()
        }
    }
}