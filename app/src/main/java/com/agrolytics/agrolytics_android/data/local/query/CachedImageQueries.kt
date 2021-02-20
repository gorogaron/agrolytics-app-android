package com.agrolytics.agrolytics_android.data.local.query

import com.agrolytics.agrolytics_android.data.local.dao.CachedImageItemDao
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem

class CachedImageQueries(var cachedImageItemDao: CachedImageItemDao) {

    fun getImageById(id: String) : CachedImageItem? {
        return cachedImageItemDao.getById(id)
    }

    fun getImagesBySessionId(sessionId: String) : List<CachedImageItem>? {
        return cachedImageItemDao.getBySessionId(sessionId)
    }

    fun getAllImages() : List<CachedImageItem>? {
        return cachedImageItemDao.getAll()
    }

    fun addImage(cachedImageItem: CachedImageItem) {
        cachedImageItemDao.add(cachedImageItem)
    }

    fun deleteImage(cachedImageItem: CachedImageItem) {
        cachedImageItemDao.delete(cachedImageItem)
    }

    fun deleteImagesBySessionId(sessionId: String) {
        cachedImageItemDao.deleteBySessionId(sessionId)
    }

    fun deleteAllImage() {
        cachedImageItemDao.deleteAll()
    }
}