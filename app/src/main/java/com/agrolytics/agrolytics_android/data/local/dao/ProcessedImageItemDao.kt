package com.agrolytics.agrolytics_android.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem
import com.agrolytics.agrolytics_android.data.local.tables.ProcessedImageItem

@Dao
interface ProcessedImageItemDao {

    @Query("SELECT * FROM processed_images")
    fun getAll(): List<ProcessedImageItem>

    @Query("SELECT * FROM processed_images WHERE session_id LIKE :sessionId")
    fun getBySessionId(sessionId: Long): List<ProcessedImageItem>

    @Query("SELECT * FROM processed_images WHERE timestamp LIKE :timestamp")
    fun getByTimestamp(timestamp: Long): ProcessedImageItem?

    @Query("SELECT DISTINCT session_id FROM processed_images")
    fun getAllSessionIds(): List<Long>

    @Insert
    fun add(processedImageItem: ProcessedImageItem)

    @Insert
    fun addAll(processedImageItems: List<ProcessedImageItem>)

    @Delete
    fun delete(processedImageItem: ProcessedImageItem)

    @Query("DELETE FROM processed_images WHERE timestamp LIKE :timestamp")
    fun deleteByTimestamp(timestamp : Long)

    @Query("DELETE FROM processed_images WHERE session_id LIKE :sessionId")
    fun deleteBySessionId(sessionId: Long)

    @Query("DELETE FROM processed_images")
    fun deleteAll()
}