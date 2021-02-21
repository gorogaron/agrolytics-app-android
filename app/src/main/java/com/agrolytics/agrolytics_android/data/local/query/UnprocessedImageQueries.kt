package com.agrolytics.agrolytics_android.data.local.query

import com.agrolytics.agrolytics_android.data.local.dao.UnprocessedImageItemDao
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.UnprocessedImageItem

class UnprocessedImageQueries(var unprocessedImageItemDao : UnprocessedImageItemDao) {

    fun addUnprocessedImageItem(unprocessedImageItem: UnprocessedImageItem){
        unprocessedImageItemDao.add(unprocessedImageItem)
    }

    fun deleteUnprocessedImageItemById(id : Long){
        unprocessedImageItemDao.deleteById(id)
    }

    fun getBySessionId(sessionId : String) : List<UnprocessedImageItem>{
        return unprocessedImageItemDao.getBySessionId(sessionId)
    }
}