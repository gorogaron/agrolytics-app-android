package com.agrolytics.agrolytics_android.data.local.dao

import androidx.room.*
import com.agrolytics.agrolytics_android.data.local.tables.CachedImageItem

@Dao
interface CachedImageItemDao {

    @Query("SELECT * FROM cached_images")
    fun getAll(): List<CachedImageItem>

    @Query("SELECT * FROM cached_images WHERE session_id LIKE :sessionId")
    fun getBySessionId(sessionId: Long): List<CachedImageItem>

    @Query("SELECT DISTINCT session_id FROM cached_images")
    fun getAllSessionIds(): List<Long>

    @Query("SELECT timestamp FROM cached_images")
    fun getAllTimestamps(): List<Long>

    @Query("SELECT MAX(timestamp) FROM cached_images")
    fun getLatestTimestamp(): Long

    @Insert
    fun add(cachedImageItem: CachedImageItem)

    @Insert
    fun addAll(cachedImageItems: List<CachedImageItem>)

    @Delete
    fun delete(cachedImageItem: CachedImageItem)

    @Query("DELETE FROM cached_images WHERE timestamp LIKE :timestamp")
    fun deleteByTimestamp(timestamp : Long)

    @Query("DELETE FROM cached_images WHERE session_id LIKE :sessionId")
    fun deleteBySessionId(sessionId: Long)

    @Query("DELETE FROM cached_images")
    fun deleteAll()

    @Update
    fun update(cachedImageItem: CachedImageItem)
}