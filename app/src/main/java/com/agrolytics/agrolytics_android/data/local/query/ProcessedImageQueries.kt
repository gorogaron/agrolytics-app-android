package com.agrolytics.agrolytics_android.data.local.query

import com.agrolytics.agrolytics_android.data.local.dao.ProcessedImageItemDao
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem

class ProcessedImageQueries(var processedImageItemDao: ProcessedImageItemDao) {
    fun add(processedImageItem: ProcessedImageItem){
        processedImageItemDao.add(processedImageItem)
    }

    fun getBySessionId(sessionId : String) : List<ProcessedImageItem>{
        return processedImageItemDao.getBySessionId(sessionId)
    }
}