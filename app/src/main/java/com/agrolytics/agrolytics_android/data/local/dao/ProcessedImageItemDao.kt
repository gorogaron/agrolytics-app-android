package com.agrolytics.agrolytics_android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem

@Dao
interface ProcessedImageItemDao {

    @Query("SELECT * FROM processed_images")
    fun getAll(): List<ProcessedImageItem>

    @Query("SELECT * FROM processed_images WHERE session_id LIKE :sessionId")
    fun getBySessionId(sessionId: String): List<ProcessedImageItem>

    @Insert
    fun add(processedImageItem: ProcessedImageItem)

    @Insert
    fun addAll(processedImageItems: List<ProcessedImageItem>)

    @Delete
    fun delete(processedImageItem: ProcessedImageItem)

    @Query("DELETE FROM processed_images WHERE session_id LIKE :sessionId")
    fun deleteBySessionId(sessionId: String)

    @Query("DELETE FROM processed_images")
    fun deleteAll()
}